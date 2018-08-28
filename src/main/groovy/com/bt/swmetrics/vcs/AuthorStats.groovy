package com.bt.swmetrics.vcs

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class AuthorStats {
    String author
    private Map<String, List<Instant>> pathCommitInstants = [:]
    private SortedMap<Instant,Integer> commitPathCounts = new TreeMap<>()

    void addCommit(Instant when, List<String> paths) {
        commitPathCounts[when] = paths.size()
        paths.each { path ->
            pathCommitInstants[path] = pathCommitInstants[path] ?: []
            pathCommitInstants[path] << when
        }
    }

    SortedSet<Instant> getCommitDates() {
        commitPathCounts.keySet()
    }

    long getTenure() {
        ChronoUnit.DAYS.between(commitDates[0], commitDates[-1]) + 1
    }

    Map<String,Integer> getPathCommits() {
        this.pathCommitInstants.collectEntries { path, times -> [(path): times.size()] } as Map<String,Integer>
    }

    Map<OffsetDateTime,Tuple2<Integer,Integer>> monthlyTotals(Instant start, Instant end) {
        def startOfPeriod = OffsetDateTime.ofInstant(start, ZoneOffset.UTC)
        def startMonth = OffsetDateTime.of(startOfPeriod.year, startOfPeriod.monthValue, 1, 0, 0, 0, 0, ZoneOffset.UTC)

        def endOfPeriod = OffsetDateTime.ofInstant(end, ZoneOffset.UTC)
        def endMonth = OffsetDateTime.of(endOfPeriod.year, endOfPeriod.monthValue, 1, 0, 0, 0, 0, ZoneOffset.UTC).plusMonths(1)

        Map<OffsetDateTime,Tuple2<Integer,Integer>> result = [:]

        def byYear = commitPathCounts.groupBy { OffsetDateTime.ofInstant(it.key, ZoneOffset.UTC).year }

        OffsetDateTime currentMonth = startMonth
        while (currentMonth < endMonth) {
            def byMonth = byYear[currentMonth.year]?.groupBy { OffsetDateTime.ofInstant(it.key, ZoneOffset.UTC).monthValue } ?: [:]

            def commits = byMonth[currentMonth.monthValue]?.size() ?: 0
            def paths = byMonth[currentMonth.monthValue]?.values()?.inject(0) { sum, count -> sum + count } ?: 0
            result[currentMonth] = new Tuple2<Integer,Integer>(commits, paths)
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

    Map<Instant,Integer> getPathModificationCounts() {
        this.commitPathCounts
    }
}
