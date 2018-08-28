package com.bt.swmetrics.visualisation

import groovy.json.JsonOutput
import spock.lang.Specification
import spock.lang.Unroll


class JitTreeNodeSpec extends Specification {

    def setup() {
        JitTreeNode.nodeId = 0
    }

    def "Setting path should also set name"() {
        given:
        def node = new JitTreeNode(path: 'foo/bar')

        expect:
        node.path == 'foo/bar'
        node.name == 'bar'
    }

    def "Already URI-encoded paths should be unaltered"() {
        given:
        def node = new JitTreeNode(path: 'foo/bar%20baz')

        expect:
        node.path == 'foo/bar%20baz'
        node.name == 'bar%20baz'
    }

    def "Paths needing URI-encoding should have it applied"() {
        given:
        def node = new JitTreeNode(path: 'foo/bar baz')

        expect:
        node.path == 'foo/bar%20baz'
        node.name == 'bar%20baz'
    }

    def "Should be able to create and add a child from data"() {
        given:
        def root = new JitTreeNode(path: 'some')

        when:
        JitTreeNode node = root.addChild('some/name', 1234, 1.23)

        then:
        root.children.size() == 1
        root.children[0] == node

        node.path == 'some/name'
        node.name == 'name'
        node.size == 1234.0
        node.colourMetric == 1.23
    }

    def "Node should be added to its parent path"() {
        given:
        def root = new JitTreeNode(path: 'foo')
        def child = new JitTreeNode(path: 'foo/bar')
        root.children << child

        when:
        def node = root.addChild('foo/bar/baz', 42, 123)

        then:
        root.children.size() == 1
        root.children[0].children.size() == 1
        root.children[0].children[0] == node
    }

    def "Intermediate nodes should be created automatically if necessary"() {
        given:
        def root = new JitTreeNode(path: 'foo')

        when:
        def node = root.addChild('foo/bar/baz/xyzzy', 42, 123)

        then:
        root.children.size() == 1
        root.children[0].children.size() == 1
        root.children[0].path == 'foo/bar'
        root.children[0].name == 'bar'
        root.children[0].children[0].children.size() == 1
        root.children[0].children[0].path == 'foo/bar/baz'
        root.children[0].children[0].children[0] == node
    }

    def "Adding children should increment sum of size metric values up the tree"() {
        given:
        def root = new JitTreeNode(path: 'foo')

        when:
        def rootSize0 = root.size
        root.addChild('foo/bar/baz/xyzzy', 42, 123)
        def bazNode = root.children[0].children[0]
        def bazSize = bazNode.size
        def rootSize1 = root.size
        root.addChild('foo/bar/plugle', 100, 321)
        def rootSize2 = root.size

        then:
        rootSize0 == 0.0
        rootSize1 == 42.0
        bazSize == 42
        rootSize2 == 142
    }

    def "Adding children should propagate average and max of colour metric values up the tree"() {
        given:
        def root = new JitTreeNode(path: 'foo')

        when:
        def rootColour0 = root.colourMetric
        def rootMaxColour0 = root.maxColourMetric
        root.addChild('foo/bar/baz/xyzzy', 4, 100)
        def rootColour1 = root.colourMetric
        def rootMaxColour1 = root.maxColourMetric
        root.addChild('foo/bar/plugle', 1, 200)
        root.addChild('foo/bar/plugh', 2, 300)
        def rootColour2 = root.colourMetric
        def rootMaxColour2 = root.maxColourMetric

        then:
        rootColour0 == 0.0
        rootMaxColour0 == 0.0
        rootColour1 == 100
        rootMaxColour1 == 100
        rootColour2 == (4 * 100.0 + 1 * 200.0 + 2 * 300.0) / 7.0
        rootMaxColour2 == 300
    }

    def "Adding children of zero size or colour should be allowed"() {
        given:
        def root = new JitTreeNode(path: 'foo', size: 0, colourMetric: 0, maxColourMetric: 0)

        when:
        root.addChild('foo/bar', 0, 0)

        then:
        root.colourMetric == 0
        root.maxColourMetric == 0
        root.size == 0
    }

    def "Simple node should be converted to JSON-able form"() {
        given:
        def node = new JitTreeNode(path: 'a/b', size: 123, colourMetric: 1.23, maxColourMetric: 2.34)

        when:
        def result = node.toJsonable(100, 1, 0)

        then:
        result.id =~ /ID-\d+/
        result.children == []
        result.name == 'b'
        result.data.$color == '#ff0000'
        result.data.$area == 123
        result.data.c == 1.23
        result.data.mc == 2.34
        result.data.ms == '#ff0000'
        result.data.p == 'a/b'
    }

    @Unroll
    def "Colour string values should be controlled by thresholds"() {
        given:
        def node = new JitTreeNode(path: 'a', colourMetric: 10)

        when:
        node.colourMetric = colour
        def result = node.toJsonable(100, hot, cold)
        then:
        result.data.$color == expected

        where:
        colour  | hot   | cold  || expected
        10      | 10    | 0     || '#ff0000'
        10      | 20    | 0     || '#7f7f00'
        10      | 20    | 10    || '#00ff00'
        0       | 10    | -10   || '#7f7f00'
    }

    def "Should be able to convert full tree to JSON-able structure"() {
        given:
        def root = new JitTreeNode(path: 'a')
        root.addChild('a/b/c', 1, 2)
        root.addChild('a/b/d', 3, 4)
        root.addChild('a/b/e/f', 5, 6)
        root.addChild('a/x/y', 7, 8)

        when:
        def result = root.toJsonable(500, 10, 0)

        then:
        result.id =~ /ID-\d+/
        result.data.'$area' == 16
        result.data.$color == '#9f5f00'

        def json = JsonOutput.toJson(result)
        json == '{"children":[{"children":[{"children":[],"id":"ID-0","name":"c","data":{"$area":1,"$color":"#33cb00",' +
                '"c":2.000,"mc":0.000,"ms":"#00ff00","p":"a/b/c"}},{"children":[],"id":"ID-1","name":"d","data":{"$area":3,' +
                '"$color":"#669800","c":4.000,"mc":0.000,"ms":"#00ff00","p":"a/b/d"}},{"children":[{"children":[],"id":"ID-2",' +
                '"name":"f","data":{"$area":5,"$color":"#986600","c":6.000,"mc":0.000,"ms":"#00ff00","p":"a/b/e/f"}}],"id":"ID-3",' +
                '"name":"e","data":{"$area":5.0,"$color":"#986600","c":6.000,"mc":6.000,"ms":"#986600","p":"a/b/e"}}],"id":"ID-4",' +
                '"name":"b","data":{"$area":9.0,"$color":"#7c8200","c":4.889,"mc":6.000,"ms":"#986600","p":"a/b"}},' +
                '{"children":[{"children":[],"id":"ID-5","name":"y","data":{"$area":7,"$color":"#cc3200","c":8.000,"mc":0.000,' +
                '"ms":"#00ff00","p":"a/x/y"}}],"id":"ID-6","name":"x","data":{"$area":7.0,"$color":"#cc3200","c":8.000,"mc":8.000,' +
                '"ms":"#cc3200","p":"a/x"}}],"id":"ID-7","name":"a","data":{"$area":16.0,"$color":"#9f5f00","c":6.250,"mc":8.000,' +
                '"ms":"#cc3200","p":"a"}}'
    }

    def "Should be able to partition children into smaller subsets"() {
        given:
        def root = new JitTreeNode(path: 'a')
        (1 .. 10).each {
            root.addChild("a/$it", it, 0)
        }

        when:
        def result = root.toJsonable(5, 10, 0)

        then:
        result.children.size() == 6

        def json = JsonOutput.toJson(result)
        json == '{"children":[{"children":[],"id":"ID-9","name":"10","data":{"$area":10,"$color":"#00ff00",' +
                '"c":0.000,"mc":0.000,"ms":"#00ff00","p":"a/10"}},{"children":[],"id":"ID-8","name":"9",' +
                '"data":{"$area":9,"$color":"#00ff00","c":0.000,"mc":0.000,"ms":"#00ff00","p":"a/9"}},{"children":[],' +
                '"id":"ID-7","name":"8","data":{"$area":8,"$color":"#00ff00","c":0.000,"mc":0.000,"ms":"#00ff00","p":"a/8"}},' +
                '{"children":[],"id":"ID-6","name":"7","data":{"$area":7,"$color":"#00ff00","c":0.000,"mc":0.000,"ms":"#00ff00",' +
                '"p":"a/7"}},{"children":[],"id":"ID-5","name":"6","data":{"$area":6,"$color":"#00ff00","c":0.000,"mc":0.000,' +
                '"ms":"#00ff00","p":"a/6"}},{"children":[{"children":[],"id":"ID-4","name":"5","data":{"$area":5,' +
                '"$color":"#00ff00","c":0.000,"mc":0.000,"ms":"#00ff00","p":"a/5"}},{"children":[],"id":"ID-3","name":"4",' +
                '"data":{"$area":4,"$color":"#00ff00","c":0.000,"mc":0.000,"ms":"#00ff00","p":"a/4"}},{"children":[],' +
                '"id":"ID-2","name":"3","data":{"$area":3,"$color":"#00ff00","c":0.000,"mc":0.000,"ms":"#00ff00","p":"a/3"}},' +
                '{"children":[],"id":"ID-1","name":"2","data":{"$area":2,"$color":"#00ff00","c":0.000,"mc":0.000,"ms":"#00ff00",' +
                '"p":"a/2"}},{"children":[],"id":"ID-0","name":"1","data":{"$area":1,"$color":"#00ff00","c":0.000,"mc":0.000,' +
                '"ms":"#00ff00","p":"a/1"}}],"id":"ID-10","name":"5 smaller items ...","data":{"$area":15,"$color":"#7f7f7f",' +
                '"p":"a"}}],"id":"ID-11","name":"a","data":{"$area":55.0,"$color":"#00ff00","c":0.000,"mc":0.000,"ms":"#00ff00",' +
                '"p":"a"}}'
    }

    def "Should be able to specify 'top' and 'bottom' colours whe converting to JSON"() {
        given:
        def root = new JitTreeNode(path: 'a')
        [0, 1, 3, 7, 15, 31, 63, 127, 255].each {
            root.addChild("a/$it", it, it)
        }

        when:
        def result = root.toJsonable(100, 255, 0, 0xffff00, 0x0000ff)

        then:
        result.data.$color == '#acac52'
        result.children.collect { it.data.$color } == [
                '#0000ff', '#0000fe', '#0202fc', '#0606f8', '#0e0ef0', '#1e1ee0', '#3f3fbf', '#7f7f7f', '#ffff00'
        ]
    }

    def "Should be able to attach extra data to a node"() {
        given:
        def root = new JitTreeNode(path: 'a')

        when:
        root.addChild('a/b', 1, 1, [42])

        then:
        root.children[0].extra == [42]
    }

    def "Extra data should appear in JSONable format, if present"() {
        given:
        def root = new JitTreeNode(path: 'a')
        root.addChild('a/b', 1, 1)
        root.addChild('a/x', 1, 1, [42])

        when:
        def result = root.toJsonable(100, 255, 0)

        then:
        result.children[0].data.x == null
        result.children[1].data.x == [42]
    }

    def "Name elements should be URI-decoded in JSON data"() {
        given:
        def root = new JitTreeNode(path: 'hash%23hash')
        root.addChild('hash%23hash/%25percent', 1, 1)
        root.addChild('hash#hash/hello world', 1, 1)


        when:
        def result = root.toJsonable(100, 255, 0)

        then:
        result.name == 'hash#hash'
        result.data.p == 'hash%23hash'
        result.children[0].name == '%percent'
        result.children[0].data.p == 'hash%23hash/%25percent'
        result.children[1].name == 'hello world'
        result.children[1].data.p == 'hash%23hash/hello%20world'
    }
}
