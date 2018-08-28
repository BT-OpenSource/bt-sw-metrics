package com.bt.swmetrics.visualisation

import groovy.json.JsonOutput
import spock.lang.Specification

class JitProducerSpec extends Specification {

    def setup() {
        JitTreeNode.nodeId = 0
    }

    def "Should be able to create and add a child from data"() {
        given:
        def set = new JitProducer()

        when:
        set.addData('name', 1234, 1.23)

        then:
        set.root.children.size() == 1

        def node = set.root.children[0]
        node.path == 'name'
        node.name == 'name'
        node.size == 1234.0
        node.colourMetric == 1.23
    }

    def "Node should be added to its parent path"() {
        given:
        def set = new JitProducer()
        set.addData('foo', 1, 1)
        set.addData('foo/bar', 2, 2)

        when:
        set.addData('foo/bar/baz', 42, 123)

        then:
        set.root.children.size() == 1
        set.root.children[0].path == 'foo'
        set.root.children[0].children[0].path == 'foo/bar'
        set.root.children[0].children[0].children[0].path == 'foo/bar/baz'
    }

    def "Intermediate nodes should be created automatically if necessary"() {
        given:
        def set = new JitProducer()

        when:
        set.addData('foo/bar/baz/xyzzy', 42, 123)

        then:
        def root = set.root
        root.children[0].path == 'foo'
        root.children[0].children[0].path == 'foo/bar'
        root.children[0].children[0].children[0].path == 'foo/bar/baz'
        root.children[0].children[0].children[0].children[0].path == 'foo/bar/baz/xyzzy'
    }

    def "Should be able to convert full tree to JSON-able structure"() {
        given:
        def set = new JitProducer(partitionSize: 500, topThreshold: 10)
        set.addData('a/b/c', 1, 2)
        set.addData('a/b/d', 3, 4)
        set.addData('a/b/e/f', 5, 6)
        set.addData('a/x/y', 7, 8)

        when:
        def result = set.toJsonable()

        then:
        result.id =~ /ID-\d+/
        result.data.'$area' == 16
        result.data.$color == '#9f5f00'

        def json = JsonOutput.toJson(result)
        json == '{"children":[{"children":[{"children":[{"children":[],"id":"ID-0","name":"c","data":{"$area":1,"$color":"#33cb00",' +
                '"c":2.000,"mc":0.000,"ms":"#00ff00","p":"a/b/c"}},{"children":[],"id":"ID-1","name":"d","data":{"$area":3,' +
                '"$color":"#669800","c":4.000,"mc":0.000,"ms":"#00ff00","p":"a/b/d"}},{"children":[{"children":[],"id":"ID-2",' +
                '"name":"f","data":{"$area":5,"$color":"#986600","c":6.000,"mc":0.000,"ms":"#00ff00","p":"a/b/e/f"}}],"id":"ID-3",' +
                '"name":"e","data":{"$area":5.0,"$color":"#986600","c":6.000,"mc":6.000,"ms":"#986600","p":"a/b/e"}}],"id":"ID-4",' +
                '"name":"b","data":{"$area":9.0,"$color":"#7c8200","c":4.889,"mc":6.000,"ms":"#986600","p":"a/b"}},' +
                '{"children":[{"children":[],"id":"ID-5","name":"y","data":{"$area":7,"$color":"#cc3200","c":8.000,"mc":0.000,' +
                '"ms":"#00ff00","p":"a/x/y"}}],"id":"ID-6","name":"x","data":{"$area":7.0,"$color":"#cc3200","c":8.000,"mc":8.000,' +
                '"ms":"#cc3200","p":"a/x"}}],"id":"ID-7","name":"a","data":{"$area":16.0,"$color":"#9f5f00","c":6.250,"mc":8.000,' +
                '"ms":"#cc3200","p":"a"}}],"id":"ID-8","name":"","data":{"$area":16.0,"$color":"#9f5f00","c":6.250,"mc":8.000,' +
                '"ms":"#cc3200","p":""}}'
    }

    def "Should be able to limit tree level"() {
        given:
        def set = new JitProducer(partitionSize: 500, topThreshold: 10, levelLimit: 2)
        set.addData('a/b/c', 1, 2)
        set.addData('a/b/d', 3, 4)
        set.addData('a/b/e/f', 5, 6)
        set.addData('a/x/y', 7, 8)

        when:
        def result = set.toJsonable()

        then:
        result.id =~ /ID-\d+/
        result.data.'$area' == 16
        result.data.$color == '#9f5f00'

        def json = JsonOutput.toJson(result)
        json == '{"children":[{"children":[{"children":[],"id":"ID-0","name":"b/c","data":{"$area":1,"$color":"#33cb00",' +
                '"c":2.000,"mc":0.000,"ms":"#00ff00","p":"a/b/c"}},{"children":[],"id":"ID-1","name":"b/d","data":{"$area":3,' +
                '"$color":"#669800","c":4.000,"mc":0.000,"ms":"#00ff00","p":"a/b/d"}},{"children":[],"id":"ID-2","name":"b/e/f",' +
                '"data":{"$area":5,"$color":"#986600","c":6.000,"mc":0.000,"ms":"#00ff00","p":"a/b/e/f"}},{"children":[],"id":"ID-3",' +
                '"name":"x/y","data":{"$area":7,"$color":"#cc3200","c":8.000,"mc":0.000,"ms":"#00ff00","p":"a/x/y"}}],"id":"ID-4"' +
                ',"name":"a","data":{"$area":16.0,"$color":"#9f5f00","c":6.250,"mc":8.000,"ms":"#cc3200","p":"a"}}],"id":"ID-5",' +
                '"name":"","data":{"$area":16.0,"$color":"#9f5f00","c":6.250,"mc":8.000,"ms":"#cc3200","p":""}}'
    }
}
