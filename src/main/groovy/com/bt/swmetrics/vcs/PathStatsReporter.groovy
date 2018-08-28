package com.bt.swmetrics.vcs

import com.bt.swmetrics.Configurable
import com.bt.swmetrics.Configurator
import com.bt.swmetrics.PathOperations

import java.time.Instant
import java.time.temporal.ChronoUnit

class PathStatsReporter implements Configurable {
    PrintStream stream

    private List<PathCommitRecord> recordList
    private RevisionCollection revisionCollection

    LogParser getLogParser() {
        VcsParserFactory.getLogParserInstance(configurator)
    }

    ListParser getListParser() {
        VcsParserFactory.getListParserInstance(configurator)
    }

    void generateVcsMetricsCsv() {
        buildPathCommitRecordListAndHistoryCollection()
        Instant youngest = recordList*.commit.timestamp.max()

        outputVcsMetricsHeaders()

        recordList.each { record ->
            outputCsvDataFromCorePathCommitRecord(record, youngest)
            outputCsvDataFromHistoryIfPresent(youngest, revisionCollection?.historyForPath(record.path))
            stream.println ""
        }
    }

    private void outputVcsMetricsHeaders() {
        stream.print "Path,Size,Last Commit Age Days,Last Commit Date,Last Committer"
        if (configurator.vcsLogFile) {
            stream.print ",Total Commits,Aged Commit Value,Lifetime Days,Active Days,Lifetime Change Rate,Main Committer,Main Committer Percent,Total Committers"
        }
        stream.println ""
    }

    private void outputCsvDataFromCorePathCommitRecord(PathCommitRecord record, Instant youngest) {
        def strippedPath = PathOperations.stripAnyMatchingPrefixFromPath(configurator.ignorePrefixes, record.path)
        def quotedEncodedPath = PathOperations.csvQuote(PathOperations.uriEncodePath(strippedPath))
        def ageInDays = ChronoUnit.DAYS.between(record.commit.timestamp, youngest)
        stream.print "$quotedEncodedPath,$record.size,$ageInDays,$record.commit.timestamp,${PathOperations.csvQuote(record.commit.author)}"
    }

    private void outputCsvDataFromHistoryIfPresent(Instant youngest, PathHistory history) {
        if (!history) {
            return
        }
        history.baseDate = youngest
        def agedCommitString = sprintf("%.3f", history.agedCommitTotal)
        def lifetime = history.lifetimeInDays
        def activeDays = history.commitAges.groupBy { it }.size()
        def lifetimeRate = sprintf("%.3f", history.commitTotal / lifetime)
        def mainAuthorData = history.authors.groupBy { it }.max { it.value.size() }
        def mainAuthorName = mainAuthorData.key
        def mainAuthorFraction = mainAuthorData.value.size() / history.commitTotal
        def mainAuthorPercent = sprintf("%.1f", mainAuthorFraction * 100)
        def totalCommitters = history.authors.toUnique().size()

        stream.print ",$history.commitTotal,$agedCommitString,$lifetime,$activeDays,$lifetimeRate,${PathOperations.csvQuote(mainAuthorName)},$mainAuthorPercent,$totalCommitters"
    }

    private void buildPathCommitRecordListAndHistoryCollection() {
        if (configurator.vcsListFile && configurator.vcsLogFile) {
            buildPathCommitRecordListFromListFile()
            buildHistoryCollectionFromLogFile()
        } else if (configurator.vcsLogFile) {
            buildPathCommitRecordListAndHistoryCollectionFromLogFile()
        } else if (configurator.vcsListFile) {
            buildPathCommitRecordListFromListFile()
        } else {
            throw new InternalError("Expected to be building path information")
        }
    }

    private void buildPathCommitRecordListFromListFile() {
        recordList = listParser.parseToPathCommitRecordList(new File(configurator.vcsListFile))
    }

    private void buildPathCommitRecordListAndHistoryCollectionFromLogFile() {
        buildHistoryCollectionFromLogFile()
        recordList = revisionCollection.leafPathCommitRecords
    }

    private void buildHistoryCollectionFromLogFile() {
        revisionCollection = logParser.parseToRevisionCollection(new File(configurator.vcsLogFile))
        // Now the final state has been built, we can discard any intermediate revisions.
        // This can allow garbage collection to remove any no-longer referenced trees
        // which is important for large histories.
        revisionCollection.keepRevisions = []
    }
}
