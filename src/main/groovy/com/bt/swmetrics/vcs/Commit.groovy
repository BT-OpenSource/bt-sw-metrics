package com.bt.swmetrics.vcs

import groovy.transform.Canonical
import groovy.transform.Immutable

import java.time.Instant

@Immutable(knownImmutableClasses = [Instant])
class Commit implements Comparable {
    int revision
    String author
    Instant timestamp
    String action = ''

    @Override
    int compareTo(Object o) {
        Commit otherCommit = o as Commit
        otherCommit.revision <=> revision
    }
}
