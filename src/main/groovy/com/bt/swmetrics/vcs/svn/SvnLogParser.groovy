package com.bt.swmetrics.vcs.svn

import com.bt.swmetrics.vcs.AuthorStats
import com.bt.swmetrics.vcs.Commit
import com.bt.swmetrics.vcs.LogParser
import groovy.util.logging.Slf4j
import groovy.util.slurpersupport.GPathResult

import java.time.Instant

@Slf4j
class SvnLogParser extends LogParser {

    @Override
    List<Tuple> parseToTupleList(File file) {
        doParseToTupleList(new XmlSlurper().parse(file))
    }

    @Override
    List<Tuple> parseToTupleList(String text) {
        doParseToTupleList(new XmlSlurper().parseText(text))
    }

    private static List<Tuple> doParseToTupleList(GPathResult xml) {
        xml.logentry.collectMany { entry ->
            def author = entry.author.text()
            def date = Instant.parse(entry.date.text())
            def revision = Integer.parseInt(entry.@revision.text())
            if (revision % 1000 == 0) {
                log.debug "Processing revision $revision"
            }
            entry.paths.path.collect { path ->
                def action = path.@action.text()
                def copyPath = path.@'copyfrom-path'?.text()
                def copyRev = copyPath ? Integer.parseInt(path.@'copyfrom-rev'.text()) : -1
                new Tuple(path.text(), new Commit(revision, author, date, action), copyPath, copyRev)
            }
        }
    }

    @Override
    Map<String, AuthorStats> parseToAuthorStatsMap(File file) {
        def xml = new XmlSlurper().parse(file)
        xml.logentry.inject([:]) { result, entry ->
            def author = entry.author.text()
            def date = Instant.parse(entry.date.text())
            def revision = Integer.parseInt(entry.@revision.text())
            if (revision % 1000 == 0) {
                log.debug "Processing revision $revision"
            }
            def paths = entry.paths.path*.text()
            result[author] = result[author] ?: new AuthorStats(author: author)
            result[author].addCommit(date, paths)
            result
        } as Map<String, AuthorStats>
    }
}
