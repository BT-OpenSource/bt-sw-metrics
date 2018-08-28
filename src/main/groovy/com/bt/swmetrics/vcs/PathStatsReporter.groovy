package com.bt.swmetrics.vcs

import com.bt.swmetrics.Configurator
import com.bt.swmetrics.PathOperations

import java.time.Instant
import java.time.temporal.ChronoUnit

class PathStatsReporter {
    Configurator configurator
    PrintStream stream

    LogParser getLogParser() {
        VcsParserFactory.getLogParserInstance(configurator)
    }

    ListParser getListParser() {
        VcsParserFactory.getListParserInstance(configurator)
    }

    void generateVcsMetricsCsv() {
        PathInfoMap pathInfoMap = buildPathInfoMap()
        Instant youngest = pathInfoMap.values().lastCommit.timestamp.max()

        outputVcsMetricsHeaders()

        pathInfoMap.each { path, pathInfo ->
            outputCsvDataFromCorePathInfo(path, pathInfo, youngest)
            outputCsvDataFromHistoryIfPresent(youngest, pathInfo.history)
            stream.println ""
        }
    }

    private void outputVcsMetricsHeaders() {
        stream.print "Path,Bytes,Last Commit Age Days,Last Commit Date,Last Committer"
        if (configurator.vcsLogFile) {
            stream.print ",Total Commits,Aged Commit Value,Lifetime Days,Active Days,Lifetime Change Rate,Main Committer,Main Committer Percent,Total Committers"
        }
        stream.println ""
    }

    private void outputCsvDataFromCorePathInfo(String path, PathInfo pathInfo, Instant youngest) {
        def strippedPath = PathOperations.stripAnyMatchingPrefixFromPath(configurator.ignorePrefixes, path)
        def quotedEncodedPath = PathOperations.csvQuote(PathOperations.uriEncodePath(strippedPath))
        def ageInDays = ChronoUnit.DAYS.between(pathInfo.lastCommit.timestamp, youngest)
        stream.print "$quotedEncodedPath,$pathInfo.size,$ageInDays,$pathInfo.lastCommit.timestamp,$pathInfo.lastCommit.author"
    }

    private void outputCsvDataFromHistoryIfPresent(Instant youngest, PathHistory history) {
        if (!history) {
            return
        }
        history.baseDate = youngest
        def agedCommitString = sprintf("%.3f", history.agedCommitTotal)
        def lifetime = history.commitAges[-1] - history.commitAges[0] + 1
        def activeDays = history.commitAges.groupBy { it }.size()
        def lifetimeRate = sprintf("%.3f", history.commitTotal / lifetime)
        def mainAuthorData = history.authors.groupBy { it }.max { it.value.size() }
        def mainAuthorName = mainAuthorData.key
        def mainAuthorFraction = mainAuthorData.value.size() / history.commitTotal
        def mainAuthorPercent = sprintf("%.1f", mainAuthorFraction * 100)
        def totalCommitters = history.authors.toUnique().size()

        stream.print ",$history.commitTotal,$agedCommitString,$lifetime,$activeDays,$lifetimeRate,$mainAuthorName,$mainAuthorPercent,$totalCommitters"
    }

    PathInfoMap buildPathInfoMap() {
        PathInfoMap pathInfoMap
        if (configurator.vcsListFile && configurator.vcsLogFile) {
            pathInfoMap = buildPathInfoMapFromCombinedListAndLogFiles()
        } else if (configurator.vcsLogFile) {
            pathInfoMap = buildPathInfoMapFromLogFile()
        } else if (configurator.vcsListFile) {
            pathInfoMap = buildPathInfoMapFromListFile()
        } else {
            throw new InternalError("Expected to be building a PathInfoMap")
        }
        pathInfoMap
    }

    private PathInfoMap buildPathInfoMapFromCombinedListAndLogFiles() {
        PathInfoMap pathInfoMap = listParser.parseToPathInfoMap(new File(configurator.vcsListFile))
        def historyCollection = logParser.parseToPathHistoryCollection(new File(configurator.vcsLogFile))
        pathInfoMap.setStripPrefixes(configurator.ignorePrefixes)
        pathInfoMap.mergeHistory(historyCollection)
        pathInfoMap
    }

    private PathInfoMap buildPathInfoMapFromListFile() {
        PathInfoMap pathInfoMap = listParser.parseToPathInfoMap(new File(configurator.vcsListFile))
        pathInfoMap.setStripPrefixes(configurator.ignorePrefixes)
        pathInfoMap
    }

    private PathInfoMap buildPathInfoMapFromLogFile() {
        def historyCollection = logParser.parseToPathHistoryCollection(new File(configurator.vcsLogFile))
        PathInfoMap pathInfoMap = historyCollection.toPathInfoMap()
        pathInfoMap.setStripPrefixes(configurator.ignorePrefixes)
        pathInfoMap
    }
}
