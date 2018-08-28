package com.bt.swmetrics.visualisation

import spock.lang.Specification

class D3PlusProducerSpec extends Specification {
    D3PlusProducer producer

    def "Should be possible to name the initial size field"() {
        given:
        producer = new D3PlusProducer(sizeMetricName: 'Size')

        expect:
        producer.sizeMetricName == 'Size'
    }

    def "Should be possible to name the initial colour field"() {
        given:
        producer = new D3PlusProducer(colourMetricName: 'Colour')

        expect:
        producer.colourMetricName == 'Colour'
    }

    def "Should be possible to name the set of extra metrics"() {
        given:
        producer = new D3PlusProducer(extraMetricNames: ['Foo', 'Bar'])

        expect:
        producer.extraMetricNames == ['Foo', 'Bar']
    }

    def "Record set should initially be empty"() {
        given:
        producer = new D3PlusProducer()

        expect:
        producer.records == []
    }

    def "Should be possible to create a new record"() {
        given:
        producer = new D3PlusProducer()

        when:
        producer.addData('some/path', 1.23, 4.56, [1, 'foo'])

        then:
        producer.records.size() == 1
        with(producer.records[0]) {
            path == 'some/path'
            pathComponents == ['some', 'path']
            size == 1.23
            colour == 4.56
            extra == [1, 'foo']
        }
    }

    def "Should be possible to convert to JSON-able form"() {
        given:
        producer = new D3PlusProducer(sizeMetricName: 'Size', colourMetricName: 'Colour', extraMetricNames: ['Foo', 'Bar'])
        producer.addData('a/b/c', 1.23, 4.56, ['foo-1', 'bar-1'])
        producer.addData('a/b/d/e', 2.34, 5.67, ['foo-2', 'bar-2'])

        when:
        def jsonish = producer.toJsonable()

        then:
        jsonish.size() == 2
        jsonish[0] == ['#0': 'a', '#1': 'b', '#2': 'c', 'Size': 1.23, 'Colour': 4.56, 'Foo': 'foo-1', 'Bar': 'bar-1']
        jsonish[1] == ['#0': 'a', '#1': 'b', '#2': 'd', '#3': 'e', 'Size': 2.34, 'Colour': 5.67, 'Foo': 'foo-2', 'Bar': 'bar-2']
    }

    def "Empty values shouldn't appear in the output"() {
        given:
        producer = new D3PlusProducer(sizeMetricName: 'Size', colourMetricName: 'Colour', extraMetricNames: ['Foo', 'Bar'])
        producer.addData('a/b/c', 1.23, 4.56, ['foo-1', ''])
        producer.addData('a/b/d/e', 2.34, 5.67, ['', 'bar-2'])

        when:
        def jsonish = producer.toJsonable()

        then:
        jsonish.size() == 2
        jsonish[0] == ['#0': 'a', '#1': 'b', '#2': 'c', 'Size': 1.23, 'Colour': 4.56, 'Foo': 'foo-1']
        jsonish[1] == ['#0': 'a', '#1': 'b', '#2': 'd', '#3': 'e', 'Size': 2.34, 'Colour': 5.67, 'Bar': 'bar-2']
    }

    def "Should be possible to get path component list"() {
        given:
        producer = new D3PlusProducer(sizeMetricName: 'Size', colourMetricName: 'Colour', extraMetricNames: ['Foo', 'Bar'])
        producer.addData('a/b/c', 1.23, 4.56, ['foo-1', 'bar-1'])
        producer.addData('a/b/d/e', 2.34, 5.67, ['foo-2', 'bar-2'])

        when:
        def list = producer.pathComponentIdList

        then:
        list == ['#0', '#1', '#2', '#3']
    }
}