package com.bt.swmetrics.vcs

import spock.lang.Specification


class RevisionTreeNodePathIteratorSpec extends Specification {
    RevisionTreeNode root = RevisionTreeNode.EMPTY
    RevisionTreeNodePathIterator iterator

    def "Empty tree should immediately give hasNext as false"() {
        when:
        iterator = new RevisionTreeNodePathIterator(root)

        then:
        !iterator.hasNext()
    }

    def "Tree with top-level child only should yield that"() {
        given:
        def tree = root.add('a')
        iterator = new RevisionTreeNodePathIterator(tree)

        expect:
        iterator.hasNext()
        iterator.next() == 'a'

        and:
        !iterator.hasNext()
    }

    def "Tree with sub-child should list all levels"() {
        given:
        def tree = root.add('a/b').add('a/c')
        iterator = new RevisionTreeNodePathIterator(tree)

        expect:
        iterator.hasNext()
        iterator.collect().sort() == ['a', 'a/b', 'a/c']
    }
}