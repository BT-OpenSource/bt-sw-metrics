package com.bt.swmetrics.vcs

import spock.lang.Specification

import java.time.Instant
import java.time.temporal.ChronoUnit

class PathHistoryCollectionSpec extends Specification {
    PathHistoryCollection collection = new PathHistoryCollection()
    Instant time1 = Instant.parse("2017-06-15T15:49:03.141593Z")
    Instant time2 = time1.plus(1, ChronoUnit.MINUTES)
    Instant time3 = time1.plus(2, ChronoUnit.MINUTES)
    Instant time4 = time1.plus(3, ChronoUnit.MINUTES)

    def "Should be possible to add entries"() {
        when:
        collection.addCommit("/alpha/beta", new Commit(1, "Sam Boddy", time1, "A"))
        collection.addCommit("/gamma/delta", new Commit(2, "Ann Oneimus", time2, "A"))

        then:
        collection.findOnlyActiveHistoryMapEntries().size() == 2
        collection.findOnlyActiveHistoryMapEntries().keySet().containsAll(['/alpha/beta', '/gamma/delta'])
        collection.findOnlyActiveHistoryMapEntries()['/alpha/beta'].authors == ['Sam Boddy']
    }

    def "Adding entries in non-ascending revision order should cause an exception"() {
        given:
        collection.addCommit("/alpha/beta", new Commit(4, "Sam Boddy", time1, "A"))
        collection.addCommit("/gamma/delta", new Commit(5, "Ann Oneimus", time2, "A"))

        when:
        collection.addCommit("/zeta", new Commit(3, "Ann Oneimus", time3, "A"))

        then:
        thrown IllegalStateException
    }

    def "Adding multiple entries for the same path should update the history for that path"() {
        when:
        collection.addCommit('/alpha', new Commit(1, 'Ann Oneimus', time1, 'A'))
        collection.addCommit("/alpha", new Commit(2, 'Ann Oneimus', time2, 'M'))
        collection.addCommit('/alpha', new Commit(3, 'Ann Oneimus', time3, 'M'))

        then:
        collection.findOnlyActiveHistoryMapEntries().size() == 1
        collection.findOnlyActiveHistoryMapEntries()['/alpha'].revisions == [3, 2, 1]
    }

    def "Deleting a path should eliminate path and children from map"() {
        given:
        collection.addCommit("/alpha/beta", new Commit(1, "Sam Boddy", time1, "A"))
        collection.addCommit("/gamma/delta", new Commit(2, "Ann Oneimus", time2, "A"))
        collection.addCommit('/alpha', new Commit(3, 'Sam Boddy', time3, 'M'))

        when:
        collection.addCommit('/alpha', new Commit(4, 'Ann Oneimus', time4, 'D'))

        then:
        collection.findOnlyActiveHistoryMapEntries().size() == 1
        collection.findOnlyActiveHistoryMapEntries().keySet() == ['/gamma/delta'] as Set
    }

    def "Deleting should be possible to delete parts of hierarchy separately"() {
        given:
        collection.addCommit("/alpha/beta", new Commit(1, "Sam Boddy", time1, "A"))
        collection.addCommit("/gamma/delta", new Commit(2, "Ann Oneimus", time2, "A"))
        collection.addCommit('/alpha/beta/zeta/theta', new Commit(3, 'Sam Boddy', time3, 'M'))
        collection.addCommit('/alpha/beta/epsilon', new Commit(4, 'Sam Boddy', time3, 'M'))
        collection.addCommit('/alpha/beta/zeta', new Commit(5, 'Sam Boddy', time3, 'M'))
        collection.addCommit('/alpha/beta/epsilson/eta', new Commit(6, 'Sam Boddy', time3, 'M'))
        collection.addCommit('/alpha/beta/zeta/theta', new Commit(7, 'Sam Boddy', time3, 'M'))

        when:
        collection.addCommit('/alpha/beta/zeta', new Commit(8, 'Ann Oneimus', time4, 'D'))
        collection.addCommit('/alpha/beta', new Commit(9, 'Ann Oneimus', time4, 'D'))
        collection.addCommit('/alpha', new Commit(10, 'Ann Oneimus', time4, 'D'))

        then:
        collection.findOnlyActiveHistoryMapEntries().size() == 1
        collection.findOnlyActiveHistoryMapEntries().keySet() == ['/gamma/delta'] as Set
    }

    def "Copying a not-found path should just add a new commit"() {
        when:
        collection.addCommit("/alpha/beta", new Commit(10, "Sam Boddy", time1, "A"), '/gamma', 2)

        then:
        collection.findOnlyActiveHistoryMapEntries().size() == 1
        collection.findOnlyActiveHistoryMapEntries().keySet() == ['/alpha/beta'] as Set
    }

    def "Copying an existing path should copy history up to given revision"() {
        given:
        collection.addCommit("/alpha/beta", new Commit(1, "Sam Boddy", time1, "A"))
        collection.addCommit("/alpha/beta", new Commit(2, "Sam Boddy", time2, "M"))

        when:
        collection.addCommit("/gamma", new Commit(3, "Sam Boddy", time3, "A"), '/alpha/beta', 1)

        then:
        collection.findOnlyActiveHistoryMapEntries()['/gamma'].revisions == [3, 1]
    }

    def "Copying an existing path should create and copy history for children"() {
        given:
        collection.addCommit("/alpha", new Commit(1, "Sam Boddy", time1, "A"))
        collection.addCommit("/alpha/beta", new Commit(2, "Sam Boddy", time2, "A"))
        collection.addCommit("/alpha/gamma", new Commit(3, "Sam Boddy", time3, "A"))

        when:
        collection.addCommit("/delta", new Commit(4, "Sam Boddy", time4, "A"), '/alpha', 2)

        then:
        collection.findOnlyActiveHistoryMapEntries()['/delta'].revisions == [4, 1]
        collection.findOnlyActiveHistoryMapEntries()['/delta/beta'].revisions == [2]
        collection.findOnlyActiveHistoryMapEntries()['/delta/gamma'] == null
    }

    def "Copying a deleted path (such as in a rename) should create and copy history prior to deletion for children"() {
        given:
        collection.addCommit("/old", new Commit(1, "Sam Boddy", time1, "A"))
        collection.addCommit("/old/file.txt", new Commit(2, "Sam Boddy", time1, "A"))

        when:
        collection.addCommit("/old", new Commit(3, "Sam Boddy", time2, "D"))
        collection.addCommit("/new", new Commit(3, "Sam Boddy", time2, "A"), '/old', 2)

        then:
        collection.findOnlyActiveHistoryMapEntries()['/new'].revisions == [3, 1]
        collection.findOnlyActiveHistoryMapEntries()['/new/file.txt'].revisions == [2]
        collection.findOnlyActiveHistoryMapEntries()['/old'] == null
    }

    def "Should be able to handle a complicated series of moves"() {
        given:
        collection.addCommit("/alpha", new Commit(1, "Sam Boddy", Instant.now(), "A"))
        collection.addCommit("/alpha/hello.txt", new Commit(2, "Sam Boddy", Instant.now(), "A"))
        collection.addCommit("/alpha/greet.txt", new Commit(3, "Sam Boddy", Instant.now(), "A"), '/alpha/hello.txt', 2)
        collection.addCommit("/alpha/hello.txt", new Commit(4, "Sam Boddy", Instant.now(), "D"))
        collection.addCommit("/alpha/hi.txt", new Commit(4, "Sam Boddy", Instant.now(), "A"), '/alpha/hello.txt', 3)
        collection.addCommit("/alpha", new Commit(5, "Sam Boddy", Instant.now(), "D"))
        collection.addCommit("/beta", new Commit(5, "Sam Boddy", Instant.now(), "A"), '/alpha', 4)

        expect:
        collection.findOnlyActiveHistoryMapEntries()['/beta'].revisions == [5, 1]
        collection.findOnlyActiveHistoryMapEntries()['/beta/greet.txt'].revisions == [3, 2]
        collection.findOnlyActiveHistoryMapEntries()['/beta/hi.txt'].revisions == [4, 2]
    }

    def "Should be able to generate a PathInfoMap from a collection"() {
        given:
        collection.addCommit("/alpha", new Commit(1, "Sam Boddy", time1, "A"))
        collection.addCommit("/alpha/beta", new Commit(2, "Sam Boddy", time2, "A"))
        collection.addCommit("/alpha/gamma", new Commit(3, "Sam Boddy", time3, "A"))
        collection.addCommit("/alpha/gamma", new Commit(4, "Sam Boddy", time3, "M"))

        when:
        PathInfoMap map = collection.toPathInfoMap()

        then:
        map.size() == 3
        map['/alpha'].lastCommit.revision == 1
        map['/alpha'].size == 0 // Non-leaf size is zero
        map['/alpha/beta'].lastCommit.revision == 2
        map['/alpha/beta'].size == 1
        map['/alpha/beta'].history.commitTotal == 1
        map['/alpha/gamma'].lastCommit.revision == 4
        map['/alpha/gamma'].size == 1
        map['/alpha/gamma'].history.commitTotal == 2
    }

    def "Should be able to copy a path under itself"() {
        given:
        collection.addCommit("/top", new Commit(1, "Sam Boddy", time1, "A"))
        collection.addCommit("/top/file.txt", new Commit(2, "Sam Boddy", time1, "A"))

        when:
        collection.addCommit("/top/copy", new Commit(3, "Sam Boddy", time2, "A"), '/top', 2)

        then:
        collection.findOnlyActiveHistoryMapEntries()['/top/copy'].revisions == [3, 1]
        collection.findOnlyActiveHistoryMapEntries()['/top/copy/file.txt'].revisions == [2]
    }
}