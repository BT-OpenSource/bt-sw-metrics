package com.bt.swmetrics.vcs

import groovy.transform.Canonical

import java.time.Instant

@Canonical
class Commit implements Comparable {
    int revision
    String author
    Instant timestamp
    String action

    @Override
    int compareTo(Object o) {
        Commit otherCommit = o as Commit
        otherCommit.revision <=> revision
    }
}
