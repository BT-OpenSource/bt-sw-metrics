package com.bt.swmetrics.vcs

import com.bt.swmetrics.Configurator
import com.bt.swmetrics.PathOperations

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class AuthorStatsGenerator {
    Configurator configurator
    PrintStream stream

    LogParser getLogParser() {
        VcsParserFactory.getLogParserInstance(configurator)
    }

    def generateAuthorStatsCsv() {
        Map<String,AuthorStats> statsMap = logParser.parseToAuthorStatsMap(new File(configurator.vcsLogFile))

        Instant youngest = statsMap.values().lastCommitTimestamp.max()
        Instant oldest = statsMap.values().firstCommitTimestamp.min()

        Set<OffsetDateTime> months = generateSetOfLifespanMonthStartTimes(statsMap, oldest, youngest)
        outputAuthStatsHeaderRow(months)

        statsMap.each { author, stats ->
            outputStatsForAuthor(stats, oldest, youngest)
        }
    }

    private
    static Set<OffsetDateTime> generateSetOfLifespanMonthStartTimes(Map<String, AuthorStats> statsMap, Instant oldest, Instant youngest) {
        def firstAuthor = statsMap.keySet().first()
        def months = statsMap[firstAuthor].monthlyTotals(oldest, youngest).keySet()
        months
    }

    private void outputAuthStatsHeaderRow(Set<OffsetDateTime> months) {
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern('MMM-yy').withZone(ZoneId.of('UTC'))
        stream.print "Author,First Commit,First Commit Age,Last Commit,Last Commit Age,Tenure,Active Days,Total Commits,Total Paths,Revisited Paths"
        months.each { stream.print ",${monthFormatter.format(it)}" }
        stream.println ""
    }

    private void outputStatsForAuthor(AuthorStats stats, Instant oldest, Instant youngest) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern('yyyy-MM-dd HH:mm:ss').withZone(ZoneId.systemDefault())

        def pathCount = stats.pathCommits.size()
        def revisitedCount = stats.pathCommits.findAll { it.value > 1 }.size()
        def firstDateString = timeFormatter.format(stats.firstCommitTimestamp)
        def lastDateString = timeFormatter.format(stats.lastCommitTimestamp)
        def firstAge = instantAgeInDaysRelativeToBase(stats.firstCommitTimestamp, youngest)
        def lastAge = instantAgeInDaysRelativeToBase(stats.lastCommitTimestamp, youngest)
        def activeDays = calculateActiveDayCount(stats.commitTimestamps)
        stream.print "$stats.author,$firstDateString,$firstAge,$lastDateString,$lastAge,$stats.tenure,$activeDays,${stats.commitTimestamps.size()},$pathCount,$revisitedCount"
        stats.monthlyTotals(oldest, youngest).each { date, total -> stream.print ",$total" }
        stream.println ""
    }

    private static int instantAgeInDaysRelativeToBase(Instant instant, Instant base) {
        ChronoUnit.DAYS.between(instant, base)
    }

    private static int calculateActiveDayCount(SortedSet<Instant> commitInstants) {
        commitInstants.groupBy {
            def utcTime = OffsetDateTime.ofInstant(it, ZoneOffset.UTC)
            (utcTime.year * 1000 + utcTime.dayOfYear)
        }.size()
    }

    def generateAuthorPathsCsv() {
        Map<String,AuthorStats> statsMap = logParser.parseToAuthorStatsMap(new File(configurator.vcsLogFile))

        Instant youngest = statsMap.values().lastCommitTimestamp.max()
        Instant oldest = statsMap.values().firstCommitTimestamp.min()

        stream.println "Author,Path,First Commit,Last Commit,Total Commits"

        statsMap.each { author, stats ->
            outputPathStatsForAuthor(stats)
        }
    }

    def outputPathStatsForAuthor(AuthorStats stats) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern('yyyy-MM-dd HH:mm:ss').withZone(ZoneId.systemDefault())

        stats.pathCommitDates.each { path, instants ->
            stream.println "$stats.author,${PathOperations.uriEncodePath(path)},${timeFormatter.format(instants.min())},${timeFormatter.format(instants.max())},${instants.size()}"
        }
    }
}
