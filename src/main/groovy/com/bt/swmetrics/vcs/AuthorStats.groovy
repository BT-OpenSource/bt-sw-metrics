package com.bt.swmetrics.vcs

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class AuthorStats {
    String author
    private SortedSet<Instant> commitDates = new TreeSet<>()
    private Map<String, List<Instant>> pathCommitInstants = [:]

    void addCommit(Instant when, List<String> paths) {
        commitDates << when
        paths.each { path ->
            pathCommitInstants[path] = pathCommitInstants[path] ?: []
            pathCommitInstants[path] << when
        }
    }

    long getTenure() {
        ChronoUnit.DAYS.between(commitDates[0], commitDates[-1]) + 1
    }

    Map<String,Integer> getPathCommits() {
        this.pathCommitInstants.collectEntries { path, times -> [(path): times.size()] } as Map<String,Integer>
    }

    Map<OffsetDateTime,Integer> monthlyTotals(Instant start, Instant end) {
        def startOfPeriod = OffsetDateTime.ofInstant(start, ZoneOffset.UTC)
        def startMonth = OffsetDateTime.of(startOfPeriod.year, startOfPeriod.monthValue, 1, 0, 0, 0, 0, ZoneOffset.UTC)

        def endOfPeriod = OffsetDateTime.ofInstant(end, ZoneOffset.UTC)
        def endMonth = OffsetDateTime.of(endOfPeriod.year, endOfPeriod.monthValue, 1, 0, 0, 0, 0, ZoneOffset.UTC).plusMonths(1)

        Map<OffsetDateTime,Integer> result = [:]

        def byYear = commitDates.groupBy { OffsetDateTime.ofInstant(it, ZoneOffset.UTC).year }

        OffsetDateTime currentMonth = startMonth
        while (currentMonth < endMonth) {
            def byMonth = byYear[currentMonth.year]?.groupBy { OffsetDateTime.ofInstant(it, ZoneOffset.UTC).monthValue } ?: [:]

            result[currentMonth] = 0
            result[currentMonth] = byMonth[currentMonth.monthValue]?.size() ?: 0
            currentMonth = currentMonth.plusMonths(1)
        }

        result
    }

    SortedSet<Instant> getCommitTimestamps() {
        commitDates
    }

    Instant getFirstCommitTimestamp() {
        commitDates[0]
    }

    Instant getLastCommitTimestamp() {
        commitDates[-1]
    }

    Map<String,List<Instant>> getPathCommitDates() {
        this.pathCommitInstants
    }
}
