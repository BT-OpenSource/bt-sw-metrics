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
        history.allCommits.size() == 2
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

    def "Commits up to and including a delete should not be included in stats"() {
        given:
        def now = history.baseDate
        def age1 = now.minus(1, ChronoUnit.DAYS)
        def age2 = now.minus(2, ChronoUnit.DAYS)
        def age3 = now.minus(3, ChronoUnit.DAYS)
        def author1 = "Sam Body"
        def author2 = "Ann Oniemus"

        when:
        history.addCommit(new Commit(3, author1, age1, 'A'))
        history.addCommit(new Commit(2, author2, age2, 'D'))
        history.addCommit(new Commit(1, author2, age3, 'A'))

        then:
        history.authors == [author1]
        history.commitTotal == 1
        history.agedCommitTotal <= 1.0
        history.commitAges == [1]
        history.activeCommits.size() == 1
        history.allCommits.size() == 3
    }

    def "Should be able to copy a stream of commits up to a revision"() {
        given:
        def age1 = Instant.now().minus(1, ChronoUnit.DAYS)
        def age2 = Instant.now().minus(2, ChronoUnit.DAYS)
        def age3 = Instant.now().minus(3, ChronoUnit.DAYS)
        def author1 = "Sam Body"
        def author2 = "Ann Oniemus"
        PathHistory sourceHistory = new PathHistory()
        sourceHistory.addCommit(new Commit(1, author1, age1, 'A'))
        sourceHistory.addCommit(new Commit(2, author2, age2, 'M'))

        when:
        history.copyHistory(sourceHistory, 2)
        history.addCommit(new Commit(3, author1, age3, 'M'))

        then:
        history.authors == [author1, author2, author1]
        history.commitTotal == 3
        history.revisions == [3, 2, 1]
    }

    def "Should be able to copy handle multi-level self-referential copies"() {
        given:
        def age1 = Instant.now().minus(3, ChronoUnit.DAYS)
        def age2 = Instant.now().minus(2, ChronoUnit.DAYS)
        def age3 = Instant.now().minus(1, ChronoUnit.DAYS)
        def author1 = "Sam Body"
        def author2 = "Ann Oniemus"
        PathHistory history1 = new PathHistory()
        PathHistory history2 = new PathHistory()
        PathHistory history3 = new PathHistory()

        history1.addCommit(new Commit(1, author1, age1, 'A'))

        history2.copyHistory(history1, 1)
        history2.addCommit(new Commit(2, author2, age2, 'M'))

        history3.copyHistory(history2, 2)
        history3.addCommit(new Commit(3, author1, age3, 'M'))

        when:
        history1.copyHistory(history3, 3)
        history2.copyHistory(history3, 3)

        then:
        history1.authors == [author1, author2, author1]
        history1.commitTotal == 3
        history1.revisions == [3, 2, 1]
    }
}