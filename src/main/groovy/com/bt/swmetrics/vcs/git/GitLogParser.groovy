package com.bt.swmetrics.vcs.git

import com.bt.swmetrics.vcs.AuthorStats
import com.bt.swmetrics.vcs.Commit
import com.bt.swmetrics.vcs.LogParser
import groovy.util.logging.Slf4j

import java.text.ParseException
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * This requires git log output from either of two forms. The first is:
 *
 *   git log --name-status --find-renames --find-copies
 *
 * The second is a more concise version:
 *
 *  git log --format='date %cI%nauthor %ce' --name-status --date=iso-strict --find-renames --find-copies
 */

@Slf4j
class GitLogParser extends LogParser {

    private static final DateTimeFormatter DEFAULT_DATE_FORMAT = DateTimeFormatter.ofPattern('E MMM d HH:mm:ss yyyy xx')

    @Override
    List<Tuple> parseToTupleList(String text) {
        String author
        Instant dateTime
        List<Tuple> result = []

        def nonBlankLines = text.split(/\n/).collect { it.trim() }.grep { it }

        def (int revisionCount, int increment) = determineRevisionCountAndIncrement(nonBlankLines)
        int pseudoRevision = (increment > 0 ? 0 : revisionCount + 1)

        nonBlankLines.each { line ->
            switch (line) {
                case ~/^[Dd]ate:?\s+.*/:
                    dateTime = parseDateLine(line)
                    pseudoRevision += increment
                    break

                case ~/^[Aa]uthor:?\s+.*/:
                    author = line.replaceFirst(/^[Aa]uthor:?\s+/, '')
                    break

                case ~/^[ADM]\t.*/:
                    result << createTupleForAddDeleteOrModify(pseudoRevision, author, dateTime, line)
                    break

                case ~/^[CR]\d+\t.*/:
                    result.addAll(createTuplesForRenameOrCopy(pseudoRevision, author, dateTime, line))
                    break

                default:
                    log.debug "Ignored git log line: $line"
            }
        }

        result
    }

    @Override
    List<Tuple> parseToTupleList(File file) {
        parseToTupleList(file.text)
    }

    private static Tuple determineRevisionCountAndIncrement(List <String> lines) {
        def dateLines = lines.grep  { it =~ /^[Dd]ate:?\s+/ }
        int revisionCount = dateLines.size()
        int increment = revisionCount && (dateLines.first() < dateLines.last()) ? 1 : -1
        new Tuple(revisionCount, increment)
    }

    private static Instant parseDateLine(String line) {
        def noPrefix = line.replaceFirst(/^[Dd]ate:?\s+/, '')
        switch (noPrefix) {
            case ~/\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}[\+\-]\d{2}:\d{2}/:
                // Strict ISO date format
                OffsetDateTime.parse(noPrefix).toInstant()
                break

            case ~/\w{3} \w{3} \d{1,2} \d{2}:\d{2}:\d{2} \d{4} [\-\+]\d{4}/:
                // Default Git format
                OffsetDateTime.parse(noPrefix, DEFAULT_DATE_FORMAT).toInstant()
                break

            default:
                throw new DateTimeParseException("Can't parse date: $noPrefix", noPrefix, 0)
        }
    }

    private static Tuple createTupleForAddDeleteOrModify(int pseudoRevision, String author, Instant dateTime, String line) {
        def fields = line.split(/\t/) as List
        def action = fields[0]
        def path = fields[1]
        def tuple = new Tuple(path, new Commit(pseudoRevision, author, dateTime, action), null, -1)
        tuple
    }

    private static List createTuplesForRenameOrCopy(int pseudoRevision, String author, Instant dateTime, String line) {
        def fields = line.split(/\t/) as List
        def newPath = fields[1]
        def oldPath = fields[2]
        List<Tuple> tuples = []

        tuples << new Tuple(newPath, new Commit(pseudoRevision, author, dateTime, 'A'), oldPath, pseudoRevision - 1)
        if (line.startsWith('R')) {
            tuples << new Tuple(oldPath, new Commit(pseudoRevision, author, dateTime, 'D'), null, -1)
        }
        tuples
    }

    @Override
    Map<String, AuthorStats> parseToAuthorStatsMap(File file) {
        def parsed = parseToTupleList(file.text)
        def tuplesByRevision = parsed.groupBy { String path, Commit commit, String copyPath, Integer copyRev ->
            commit.revision
        }
        createAuthorStatsMapFromRevisions(tuplesByRevision)
    }

    private static Map<String, AuthorStats> createAuthorStatsMapFromRevisions(Map<Integer, List<Tuple>> tuplesByRevision) {
        Map<String, AuthorStats> result = [:]
        tuplesByRevision.each { int revision, List<Tuple> tuples ->
            def (_, Commit firstCommit) = tuples[0]
            def author = firstCommit.author
            List<String> paths = tuples.collect { it[0] as String }
            result[author] = result[author] ?: new AuthorStats(author: author)
            result[author].addCommit(firstCommit.timestamp, paths)
        }
        result
    }
}
