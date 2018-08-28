package com.bt.swmetrics.vcs

import groovy.transform.Immutable
import groovy.transform.TailRecursive

@Immutable
class CommitList implements Iterable<Commit> {
    public static final CommitList NIL = new CommitList(null, null)

    Commit head
    CommitList tail

    static CommitList from(List<Commit> commits) {
        commits.reverse().inject(NIL) { CommitList result, Commit commit ->
            result.cons(commit)
        }
    }

    static CommitList from(Commit... commits) {
        from(commits as List<Commit>)
    }

    String toString() {
        "CommitList($head . $tail)"
    }

    int size() {
        sizeRecursive(this, 0)
    }

    @TailRecursive
    private int sizeRecursive(CommitList remainder, int total) {
        if (remainder == null) {
            total
        } else {
            sizeRecursive(remainder.tail, total + (remainder.head ? 1 : 0))
        }
    }

    CommitList cons(Commit commit) {
        new CommitList(commit, this)
    }

    CommitList takeWhile(Closure<Boolean> closure) {
        takeWhileRecursive(this, NIL, closure)
    }

    @TailRecursive
    private CommitList takeWhileRecursive(CommitList remainder, CommitList result, Closure<Boolean> closure) {
        if (remainder.isNil() || !closure.call(remainder.head)) {
            reverseRecursive(result)
        } else {
            takeWhileRecursive(remainder.tail, result.cons(remainder.head), closure)
        }
    }

    CommitList reverse() {
        reverseRecursive(this)
    }

    @TailRecursive
    private CommitList reverseRecursive(CommitList remainder, CommitList result = NIL) {
        if (remainder.isNil()) {
            result
        } else {
            reverseRecursive(remainder.tail, result.cons(remainder.head))
        }
    }

    boolean isNil() {
        this == NIL
    }

    CommitList dropWhile(Closure<Boolean> closure) {
        dropWhileRecursive(this, closure)
    }

    @TailRecursive
    private CommitList dropWhileRecursive(CommitList remainder, Closure<Boolean> closure) {
        if (remainder.isNil() || !closure.call(remainder.head)) {
            remainder
        } else {
            dropWhileRecursive(remainder.tail, closure)
        }
    }

    CommitList concat(CommitList other) {
        if (other.isNil()) {
            this
        } else {
            this.reverse().inject(other) { CommitList result, Commit commit -> result.cons(commit) }
        }
    }

    class CommitListIterator implements Iterator<Commit> {
        CommitList currentElement

        CommitListIterator(CommitList list) {
            this.currentElement = list
        }

        @Override
        boolean hasNext() {
            return currentElement != NIL
        }

        @Override
        Commit next() {
            Commit result = currentElement.head
            currentElement = currentElement.tail
            return result
        }
    }

    @Override
    Iterator<Commit> iterator() {
        new CommitListIterator(this)
    }
}
