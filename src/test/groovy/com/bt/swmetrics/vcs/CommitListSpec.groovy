package com.bt.swmetrics.vcs

import spock.lang.Specification

class CommitListSpec extends Specification {

    static final Commit COMMIT_1 = new Commit(revision: 1)
    static final Commit COMMIT_2 = new Commit(revision: 2)
    static final Commit COMMIT_3 = new Commit(revision: 3)
    static final Commit COMMIT_4 = new Commit(revision: 4)

    def "Size of NIL CommitList should be zero"() {
        expect:
        CommitList.NIL.size() == 0
    }

    def "Should be possible to create a new list from a single commit and NIL"() {
        given:
        def list = new CommitList(COMMIT_1, CommitList.NIL)

        expect:
        list.head.revision == 1
        list.size() == 1
    }

    def "Prepending a new commit gives a new list"() {
        given:
        def list = new CommitList(COMMIT_1, CommitList.NIL)

        when:
        CommitList result = list.cons(COMMIT_2)

        then:
        result.size() == 2
        result.head == COMMIT_2
        result.tail.head == COMMIT_1
    }

    def "Should be possible to test for nil-ness"() {
        expect:
        CommitList.NIL.isNil()
        !new CommitList(new Commit(revision: 1), CommitList.NIL).isNil()
    }

    def "Should be possible to convert a list of commits into a CommitList"() {
        given:
        def list = [COMMIT_1, COMMIT_2, COMMIT_3]

        when:
        CommitList commitList = CommitList.from(list)

        then:
        commitList.size() == 3
        commitList.head == COMMIT_1
        commitList.tail.head == COMMIT_2
        commitList.tail.tail.head == COMMIT_3
    }

    def "Should be possible to convert a varargs list of commits into a CommitList"() {
        when:
        CommitList commitList = CommitList.from(COMMIT_1, COMMIT_2, COMMIT_3)

        then:
        commitList.size() == 3
        commitList.head == COMMIT_1
        commitList.tail.head == COMMIT_2
        commitList.tail.tail.head == COMMIT_3
    }

    def "Should be possible to reverse a CommitList"() {
        when:
        CommitList commitList = CommitList.from(COMMIT_1, COMMIT_2, COMMIT_3).reverse()

        then:
        commitList.size() == 3
        commitList.head == COMMIT_3
        commitList.tail.head == COMMIT_2
        commitList.tail.tail.head == COMMIT_1
    }

    def "takeWhile should generate a new list"() {
        given:
        def list = CommitList.from(COMMIT_4, COMMIT_3, COMMIT_2, COMMIT_1)

        when:
        CommitList taken = list.takeWhile { it.revision > 2 }

        then:
        taken.size() == 2
        taken == CommitList.from(COMMIT_4, COMMIT_3)
    }

    def "dropWhile should generate a new list"() {
        given:
        def list = CommitList.from(COMMIT_4, COMMIT_3, COMMIT_2, COMMIT_1)

        when:
        CommitList result = list.dropWhile { it.revision > 2 }

        then:
        result.size() == 2
        result == CommitList.from(COMMIT_2, COMMIT_1)
    }

    def "Should implement Iterable/Iterator interfaces"() {
        given:
        def list = CommitList.from(COMMIT_1, COMMIT_2, COMMIT_3, COMMIT_4)

        when:
        Iterator<Commit> iterator = list.iterator()

        then:
        iterator.hasNext()
        iterator.next() == COMMIT_1
        list.collect { it } == [COMMIT_1, COMMIT_2, COMMIT_3, COMMIT_4]
    }

    def "Concatenating NIL list to a list should yield the original list"() {
        given:
        def list = CommitList.from(COMMIT_1, COMMIT_2, COMMIT_3, COMMIT_4)

        expect:
        list.concat(CommitList.NIL) is list
    }

    def "Concatenating to a NIL list should yield the original list"() {
        given:
        def list = CommitList.from(COMMIT_1, COMMIT_2, COMMIT_3, COMMIT_4)

        expect:
        CommitList.NIL.concat(list) is list
    }

    def "Should be possible to concatenate two lists to yield another"() {
        given:
        def list1 = CommitList.from(COMMIT_1, COMMIT_2)
        def list2 = CommitList.from(COMMIT_3, COMMIT_4)

        expect:
        list1.concat(list2).collect { it } == [COMMIT_1, COMMIT_2, COMMIT_3, COMMIT_4]
    }
}