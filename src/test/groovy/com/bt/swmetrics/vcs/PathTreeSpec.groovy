package com.bt.swmetrics.vcs

import spock.lang.Specification

class PathTreeSpec extends Specification {
    def "Should be able to add PathHistory to tree"() {
        given:
        PathHistory history = new PathHistory()
        PathTree tree = new PathTree()

        when:
        tree.add('a/b/c', history)

        then:
        tree['a/b/c'].history == history
        tree['a/b/c'].name == 'c'
    }

    def "Should be able to obtain full paths from nodes"() {
        given:
        PathHistory history = new PathHistory()
        PathTree tree = new PathTree()

        when:
        tree.add('a/b/c', history)
        tree.add('/d/e/f', history)

        then:
        tree['a/b/c'].path == 'a/b/c'
        tree['/d/e/f'].path == '/d/e/f'
    }

    def "Should be able to access intermediate nodes"() {
        given:
        PathHistory history = new PathHistory()
        PathTree tree = new PathTree()

        when:
        tree.add('a/b/c', history)

        then:
        tree['a'].name == 'a'
        tree['a/b'].name == 'b'
    }

    def "Should be able to get children of path"() {
        PathHistory history1 = new PathHistory()
        PathHistory history2 = new PathHistory()
        PathTree tree = new PathTree()

        when:
        tree.add('a/b', history1)
        tree.add('a/c', history2)

        then:
        tree['a'].children.size() == 2
        tree['a'].children.name.containsAll(['b', 'c'])
        tree['a'].children.history.containsAll([history1, history2])
    }

    def "Should be able to findAll elements"() {
        given:
        PathTree tree = new PathTree()
        tree.add('a/b', new PathHistory())
        tree.add('/a/b/c', new PathHistory())
        tree.add('/a/d/e', new PathHistory())
        tree.add('f', new PathHistory())
        tree.add('/', new PathHistory())

        when:
        def nodes = tree.findAll { it.name == 'b'}

        then:
        nodes.size() == 2
        nodes.name == ['b', 'b']
    }

    def "Should handle nulls in getAt"() {
        given:
        PathTree tree = new PathTree()

        expect:
        tree[null] == null
    }
}