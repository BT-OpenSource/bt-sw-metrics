package com.bt.swmetrics.vcs

import spock.lang.Specification

import java.time.Instant
import java.time.temporal.ChronoUnit


class PathHistorySpec extends Specification {
    PathHistory history = new PathHistory()

    def "Should be able to calculate the decay factor for a half life of 180 days"() {
        expect:
        Math.abs(PathHistory.decayFactor(days) - expected) < 0.0001

        where:
        days    || expected
        0       || 1
        180     || 0.5
        360     || 0.25
        540     || 0.125
        720     || 0.0625
    }

    def "Should be able to add commit details and accumulate information"() {
        given:
        def now = history.baseDate
        def ageHalf = now.minus(PathHistory.CHANGE_HALF_LIFE_DAYS, ChronoUnit.DAYS)
        def ageQuarter = now.minus(2 * PathHistory.CHANGE_HALF_LIFE_DAYS, ChronoUnit.DAYS)
        def author1 = "Sam Boddy"
        def author2 = "Ann Oniemus"
        def action = 'M'
        when:
        def commit = history.addCommit(new Commit(1, author1, ageQuarter, 'M'))

        then:
        commit.revision == 1
        history.commitTotal == 1
        Math.abs(history.agedCommitTotal - 0.25) < 0.0001
        history.authors == [author1]
        history.commitAges == [2 * PathHistory.CHANGE_HALF_LIFE_DAYS]
        history.revisions == [1]
        history.timestamps == [ageQuarter]
        history.actions == ['M']

        when:
        history.addCommit(new Commit(2, author2, ageHalf, 'A'))

        then:
        history.commitTotal == 2
        Math.abs(history.agedCommitTotal - 0.75) < 0.0001
        history.authors.size() == 2
        history.authors == [author2, author1]
        history.revisions == [2, 1]
        history.timestamps == [ageHalf, ageQuarter]
        history.actions == ['A', 'M']
        history.activeCommits.size() == 2
        history.activeCommits[0].revision == 2
    }

    def "Should be able to set a base date for age-related calculations"() {
        given:
        def baseDate = Instant.parse('2017-01-01T12:34:56.789Z')
        def ageHalf = baseDate.minus(PathHistory.CHANGE_HALF_LIFE_DAYS, ChronoUnit.DAYS)
        def ageQuarter = baseDate.minus(2 * PathHistory.CHANGE_HALF_LIFE_DAYS, ChronoUnit.DAYS)
        def author1 = "Sam Boddy"
        def author2 = "Ann Oniemus"
        history.addCommit(new Commit(1, author1, ageQuarter, 'M'))
        history.addCommit(new Commit(2, author2, ageHalf, 'A'))

        when:
        history.baseDate = baseDate

        then:
        Math.abs(history.agedCommitTotal - 0.75) < 0.0001
        history.commitAges == [PathHistory.CHANGE_HALF_LIFE_DAYS, 2 * PathHistory.CHANGE_HALF_LIFE_DAYS]
    }

    def "Should be able to get path lifetime regardless of commit order"() {
        given:
        def now = history.baseDate
        def age1 = now.minus(10, ChronoUnit.DAYS)
        def age2 = now.minus(1, ChronoUnit.DAYS)
        def age3 = now.minus(5, ChronoUnit.DAYS)
        def author1 = "Sam Boddy"
        def author2 = "Ann Oniemus"
        def action = 'M'

        when:
        history.addCommit(new Commit(1, author1, age1, 'M'))
        history.addCommit(new Commit(2, author2, age2, 'M'))
        history.addCommit(new Commit(3, author1, age3, 'M'))

        then:
        history.lifetimeInDays == 10
    }
}