package com.bt.swmetrics.vcs

import spock.lang.Specification

import java.time.Instant
import java.time.LocalDateTime
import java.time.Month
import java.time.OffsetDateTime
import java.time.ZoneOffset

class AuthorStatsSpec extends Specification {
    AuthorStats stats = new AuthorStats(author: 'Sam Boddy')

    def "Should be able to add new commit details and accumulate path stats"() {
        given:
        stats.addCommit(Instant.now(), ['path-1', 'path-2'])
        stats.addCommit(Instant.now(), ['path-2', 'path-3'])

        expect:
        stats.pathCommits == ['path-1': 1, 'path-2': 2, 'path-3': 1]
    }

    def "Should be able to add new commit details and calculate tenure"() {
        given:
        stats.addCommit(LocalDateTime.of(2017, 7, 1, 12, 34).toInstant(ZoneOffset.UTC), ['path-1'])
        stats.addCommit(LocalDateTime.of(2017, 7, 20, 12, 34).toInstant(ZoneOffset.UTC), ['path-2'])

        expect:
        stats.tenure == 20
    }

    def "Should be able to calculate commit stats over time period"() {
        given:
        [(Month.JANUARY) : 1, (Month.FEBRUARY): 2, (Month.MARCH): 3, (Month.APRIL): 4,
         (Month.MAY): 5, (Month.JUNE): 6, (Month.JULY): 7, (Month.AUGUST): 8,
         (Month.SEPTEMBER): 9, (Month.OCTOBER): 10, (Month.NOVEMBER): 11, (Month.DECEMBER): 12].each { month, count ->
            (1 .. count).each { stats.addCommit(LocalDateTime.of(2017, month, it, it, it).toInstant(ZoneOffset.UTC), ['path1', 'path-2'])}
        }

        expect:
        def jan1 = LocalDateTime.of(2017, 1, 1, 0, 0).toInstant(ZoneOffset.UTC)
        def dec31 = LocalDateTime.of(2017, 12, 31, 23, 59).toInstant(ZoneOffset.UTC)
        def jun30 = LocalDateTime.of(2017, 6, 30, 23, 59).toInstant(ZoneOffset.UTC)
        def result = stats.monthlyTotals(jan1, dec31)
        result[OffsetDateTime.of(2017, 9, 1, 0, 0, 0, 0, ZoneOffset.UTC)] == new Tuple2(9, 18)
        def values = result.values() as List
        values.collect { Tuple2 pair -> pair.first } == [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]
        values.collect { Tuple2 pair -> pair.second } == [2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24]
        stats.monthlyTotals(jan1, jun30).values().collect { it.first } as List == [1, 2, 3, 4, 5, 6]
        stats.monthlyTotals(jun30, dec31).values().collect { it.first } as List == [6, 7, 8, 9, 10, 11, 12]
    }

    def "Should be able to handle stats over year boundaries"() {
        given:
        [(Month.JANUARY) : 1, (Month.FEBRUARY): 2, (Month.MARCH): 3, (Month.APRIL): 4,
         (Month.MAY): 5, (Month.JUNE): 6, (Month.JULY): 7, (Month.AUGUST): 8,
         (Month.SEPTEMBER): 9, (Month.OCTOBER): 10, (Month.NOVEMBER): 11, (Month.DECEMBER): 12].each { month, count ->
            (1 .. count).each { stats.addCommit(LocalDateTime.of(2016, month, it, it, it).toInstant(ZoneOffset.UTC), ['path-1', 'path-2'])}
            (1 .. count).each { stats.addCommit(LocalDateTime.of(2017, month, it, it, it).toInstant(ZoneOffset.UTC), ['path-1', 'path-2'])}
            (1 .. count).each { stats.addCommit(LocalDateTime.of(2018, month, it, it, it).toInstant(ZoneOffset.UTC), ['path-1', 'path-2'])}
        }

        expect:
        def jun2016 = LocalDateTime.of(2016, 6, 1, 0, 0).toInstant(ZoneOffset.UTC)
        def jun2018 = LocalDateTime.of(2018, 6, 1, 0, 0).toInstant(ZoneOffset.UTC)
        stats.monthlyTotals(jun2016, jun2018).values().collect { it.first } as List ==
                [6, 7, 8, 9, 10, 11, 12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3, 4, 5, 6]
        stats.monthlyTotals(jun2016, jun2018).values().collect { it.second } as List ==
                [12, 14, 16, 18, 20, 22, 24, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 2, 4, 6, 8, 10, 12]
    }

    def "Should be able to calculate commit stats with dormant months"() {
        given:
        [(Month.FEBRUARY): 2, (Month.APRIL): 4,
         (Month.JUNE): 6, (Month.AUGUST): 8,
         (Month.OCTOBER): 10, (Month.DECEMBER): 12].each { month, count ->
            (1 .. count).each {
                (1 .. count).each { stats.addCommit(LocalDateTime.of(2016, month, it, it, it).toInstant(ZoneOffset.UTC), ['path-1', 'path-2'])}
                (1 .. count).each { stats.addCommit(LocalDateTime.of(2018, month, it, it, it).toInstant(ZoneOffset.UTC), ['path-1', 'path-2'])}
            }
        }

        expect:
        def jan2016 = LocalDateTime.of(2016, 1, 1, 0, 0).toInstant(ZoneOffset.UTC)
        def dec2018 = LocalDateTime.of(2018, 12, 31, 23, 59).toInstant(ZoneOffset.UTC)
        def result = stats.monthlyTotals(jan2016, dec2018)
        result.values().collect { it.first } as List ==
                [0, 2, 0, 4, 0, 6, 0, 8, 0, 10, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 4, 0, 6, 0, 8, 0, 10, 0, 12]
        result.values().collect { it.second } as List ==
                [0, 4, 0, 8, 0, 12, 0, 16, 0, 20, 0, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 8, 0, 12, 0, 16, 0, 20, 0, 24]
    }

    def "Should be able to get commit timestamps"() {
        given:
        stats.addCommit(LocalDateTime.of(2017, 7, 1, 12, 34).toInstant(ZoneOffset.UTC), ['path-1'])
        stats.addCommit(LocalDateTime.of(2017, 7, 20, 12, 34).toInstant(ZoneOffset.UTC), ['path-2'])

        expect:
        stats.commitTimestamps.collect { it.toString() } ==  ['2017-07-01T12:34:00Z', '2017-07-20T12:34:00Z']
    }

    def "Should be able to get earliest and latest commit instants"() {
        given:
        stats.addCommit(LocalDateTime.of(2017, 7, 1, 12, 34).toInstant(ZoneOffset.UTC), ['path-1'])
        stats.addCommit(LocalDateTime.of(2017, 7, 15, 12, 34).toInstant(ZoneOffset.UTC), ['path-2'])
        stats.addCommit(LocalDateTime.of(2017, 7, 20, 12, 34).toInstant(ZoneOffset.UTC), ['path-3'])

        expect:
        stats.firstCommitTimestamp == LocalDateTime.of(2017, 7, 1, 12, 34).toInstant(ZoneOffset.UTC)
        stats.lastCommitTimestamp == LocalDateTime.of(2017, 7, 20, 12, 34).toInstant(ZoneOffset.UTC)
    }

    def "Should be able to get path counts and dates"() {
        given:
        Instant instant1 = LocalDateTime.of(2017, 7, 1, 12, 34).toInstant(ZoneOffset.UTC)
        Instant instant2 = LocalDateTime.of(2017, 7, 15, 12, 34).toInstant(ZoneOffset.UTC)
        Instant instant3 = LocalDateTime.of(2017, 7, 20, 12, 34).toInstant(ZoneOffset.UTC)
        stats.addCommit(instant1, ['path-1'])
        stats.addCommit(instant2, ['path-2', 'path-3'])
        stats.addCommit(instant3, ['path-1', 'path-3'])

        expect:
        stats.pathCommitDates['path-1'] == [instant1, instant3]
        stats.pathCommitDates['path-2'] == [instant2]
        stats.pathCommitDates['path-3'] == [instant2, instant3]
    }

    def "Should be able to get path modification counts by date"() {
        given:
        Instant instant1 = LocalDateTime.of(2017, 7, 1, 12, 34).toInstant(ZoneOffset.UTC)
        Instant instant2 = LocalDateTime.of(2017, 7, 15, 12, 34).toInstant(ZoneOffset.UTC)
        Instant instant3 = LocalDateTime.of(2017, 7, 20, 12, 34).toInstant(ZoneOffset.UTC)
        stats.addCommit(instant1, ['path-1'])
        stats.addCommit(instant2, ['path-2', 'path-3'])
        stats.addCommit(instant3, ['path-1', 'path-3'])

        expect:
        stats.pathModificationCounts[instant1] == 1
        stats.pathModificationCounts[instant2] == 2
        stats.pathModificationCounts[instant3] == 2
    }
}