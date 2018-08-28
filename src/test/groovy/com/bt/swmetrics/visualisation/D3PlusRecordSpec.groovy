package com.bt.swmetrics.visualisation

import spock.lang.Specification


class D3PlusRecordSpec extends Specification {
    D3PlusRecord record

    def "Path should be split into components"() {
        given:
        record = new D3PlusRecord(path: 'a/b/c')

        expect:
        record.pathComponents == ['a', 'b', 'c']
    }

    def "URI-encoded path should be split into decoded components"() {
        given:
        record = new D3PlusRecord(path: 'a/b%23c/c%2Fd')

        expect:
        record.pathComponents == ['a', 'b#c', 'c/d']
    }

    def "Paths without apparent URI-encoding should be split without decoding"() {
        given:
        record = new D3PlusRecord(path: 'a/b c/d?')

        expect:
        record.pathComponents == ['a', 'b c', 'd?']
    }

    def "Path splitting can be limited in levels"() {
        given:
        record = new D3PlusRecord(path: 'a/b/c', levelLimit: 2)

        expect:
        record.pathComponents == ['a', 'b/c']
    }

    def "Should be possible to supply size, colour and extra values"() {
        given:
        record = new D3PlusRecord(size: 1.23, colour: 4.56, extra: [1, 'foo'])

        expect:
        record.size == 1.23
        record.colour == 4.56
        record.extra == [1, 'foo']
    }

    def "Should be possible to convert to JSON-able form"() {
        given:
        record = new D3PlusRecord(path: 'a/b/c', size: 1.23, colour: 4.56, extra: [1, 'foo'])

        when:
        def jsonish = record.toJsonable('Size', 'Colour', ['Alpha', 'Beta'])

        then:
        jsonish == ['#0': 'a', '#1': 'b', '#2': 'c', 'Size': 1.23, 'Colour': 4.56, 'Alpha': 1, 'Beta': 'foo']
    }

    def "Empty values shouldn't be included in output"() {
        given:
        record = new D3PlusRecord(path: 'a/b/c', size: 1.23, colour: 4.56, extra: ['', 'foo'])

        when:
        def jsonish = record.toJsonable('Size', 'Colour', ['Alpha', 'Beta'])

        then:
        jsonish == ['#0': 'a', '#1': 'b', '#2': 'c', 'Size': 1.23, 'Colour': 4.56, 'Beta': 'foo']
    }

    def "Extra numeric values should be converted from strings"() {
        given:
        record = new D3PlusRecord(path: 'a/b/c', size: 1.23, colour: 4.56, extra: ["1.23", 'foo'])

        when:
        def jsonish = record.toJsonable('Size', 'Colour', ['Alpha', 'Beta'])

        then:
        jsonish == ['#0': 'a', '#1': 'b', '#2': 'c', 'Size': 1.23, 'Colour': 4.56, 'Alpha': 1.23, 'Beta': 'foo']
    }
}