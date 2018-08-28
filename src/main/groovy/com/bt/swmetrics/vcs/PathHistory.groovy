package com.bt.swmetrics.vcs

import java.time.Instant
import java.time.temporal.ChronoUnit



class PathHistory {

    public static final int CHANGE_HALF_LIFE_DAYS = 180
    private SortedSet<Commit> commitStream = new TreeSet<>()
    private int activeCommitTotal = 0   // Can be computed (activeCommits.size()) but maintained for efficiency
    Instant baseDate = Instant.now()

    static BigDecimal decayFactor(long ageInDays) {
        Math.pow(2, (ageInDays * -1.0) / CHANGE_HALF_LIFE_DAYS)
    }

    def addCommit(Commit commit) {
        if (commit.action == 'D') {
            activeCommitTotal = 0
        } else {
            activeCommitTotal++
        }
        commitStream << commit
        commit
    }

     SortedSet<Commit> getActiveCommits() {
         allCommits.takeWhile { it.action != 'D' }
     }

    SortedSet<Commit> getAllCommits() {
        commitStream
    }

    int getCommitTotal() {
        activeCommitTotal
    }

    BigDecimal getAgedCommitTotal() {
        activeCommits.inject(0.0) { accumulator, commit -> accumulator + decayedChangeValue(commit.timestamp) }
    }

    private BigDecimal decayedChangeValue(Instant timestamp) {
        decayFactor(ageOfInstantInDays(timestamp))
    }

    private long ageOfInstantInDays(Instant timestamp) {
        ChronoUnit.DAYS.between(timestamp, baseDate)
    }

    List<String> getAuthors() {
        activeCommits.collect { it.author }
    }

    List<Long> getCommitAges() {
        activeCommits.collect { ageOfInstantInDays(it.timestamp) }
    }

    List<Integer> getRevisions() {
        activeCommits.collect { it.revision }
    }

    List<Instant> getTimestamps() {
        activeCommits.collect { it.timestamp }
    }

    List<String> getActions() {
        activeCommits.collect { it.action }
    }

    def copyHistory(PathHistory sourceHistory, int sourceRevision) {
        commitStream += sourceHistory.commitStream.dropWhile { Commit commit -> commit.revision > sourceRevision }
        activeCommitTotal = allCommits.takeWhile { it.action != 'D' }.size()
    }
}
