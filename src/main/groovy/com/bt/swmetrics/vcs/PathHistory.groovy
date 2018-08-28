package com.bt.swmetrics.vcs

import java.time.Instant
import java.time.temporal.ChronoUnit



class PathHistory {

    public static final int CHANGE_HALF_LIFE_DAYS = 180
    private List<Commit> commitList = []
    Instant baseDate = Instant.now()

    static BigDecimal decayFactor(long ageInDays) {
        Math.pow(2, (ageInDays * -1.0) / CHANGE_HALF_LIFE_DAYS)
    }

    def addCommit(Commit commit) {
        commitList.add(0, commit)
        commit
    }

     List<Commit> getActiveCommits() {
         commitList
     }

    int getCommitTotal() {
        commitList.size()
    }

    BigDecimal getAgedCommitTotal() {
        commitList.inject(0.0) { accumulator, commit -> accumulator + decayedChangeValue(commit.timestamp) }
    }

    private BigDecimal decayedChangeValue(Instant timestamp) {
        decayFactor(ageOfInstantInDays(timestamp))
    }

    private long ageOfInstantInDays(Instant timestamp) {
        ChronoUnit.DAYS.between(timestamp, baseDate)
    }

    List<String> getAuthors() {
        commitList.collect { it.author }
    }

    List<Long> getCommitAges() {
        commitList.collect { ageOfInstantInDays(it.timestamp) }
    }

    List<Integer> getRevisions() {
        commitList.collect { it.revision }
    }

    List<Instant> getTimestamps() {
        commitList.collect { it.timestamp }
    }

    List<String> getActions() {
        commitList.collect { it.action }
    }

    long getLifetimeInDays() {
        def sortedAges = commitAges.sort()
        sortedAges[-1] - sortedAges[0] + 1
    }
}
