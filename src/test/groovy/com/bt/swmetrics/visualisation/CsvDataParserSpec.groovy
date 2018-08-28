package com.bt.swmetrics.visualisation

import spock.lang.Specification

class CsvDataParserSpec extends Specification {
    CsvDataParser parser

    static final def TEST_DATA = '''Path,Other,Size,Metric 1,Metric 2
a/b,A,1,1.1,1.2
a/b/c,B,2,2.1,2.2
a/b/d/e,C,3,3.1,3.2
a/b/d/f,D,4,4.1,4.2'''

    def "Should be able to extract the column values once the data has been parsed"() {
        given:
        parser = new CsvDataParser(TEST_DATA, 'Path', 'Size', 'Metric 1')

        expect:
        parser.paths == ['a/b', 'a/b/c', 'a/b/d/e', 'a/b/d/f']
        parser.sizes == [1.0, 2.0, 3.0, 4.0]
        parser.colours == [1.1, 2.1, 3.1, 4.1]
    }

    def "Should be able to tolerate blank values in size and colour"() {
        given:
        def dataWithBlanks = '''Path,Size,Colour
a,,1
b,2,
'''
        parser = new CsvDataParser(dataWithBlanks, 'Path', 'Size', 'Colour')

        expect:
        parser.paths == ['a', 'b']
        parser.sizes == [0.0, 2.0]
        parser.colours == [1.0, 0.0]
    }

    def "Column names may be numeric strings (1-based)"() {
        given:
        parser = new CsvDataParser(TEST_DATA, '1', '3', '5')

        expect:
        parser.paths == ['a/b', 'a/b/c', 'a/b/d/e', 'a/b/d/f']
        parser.sizes == [1.0, 2.0, 3.0, 4.0]
        parser.colours == [1.2, 2.2, 3.2, 4.2]
    }

    def "Should be able to retrieve column names"() {
        given:
        parser = new CsvDataParser(TEST_DATA, '1', '3', '5')

        expect:
        parser.pathColumnName == 'Path'
        parser.sizeColumnName == 'Size'
        parser.colourColumnName == 'Metric 2'
    }

    def "Should be able to retrieve column IDs"() {
        given:
        parser = new CsvDataParser(TEST_DATA, '1', '3', '5')

        expect:
        parser.pathColumnId == '@0'
        parser.sizeColumnId == '@2'
        parser.colourColumnId == '@4'
    }

    def "Invalid column names should result in a sensible exception"() {
        when:
        parser = new CsvDataParser(TEST_DATA, 'NO-SUCH-COLUMN', '3', '5')

        then:
        IllegalArgumentException e = thrown()
        e.message == 'Invalid column name: NO-SUCH-COLUMN - possible values are: Path,Other,Size,Metric 1,Metric 2'
    }

    def "Should be possible to specify extra column values to include"() {
        given:
        parser = new CsvDataParser(TEST_DATA, '1', '3', '5', ['Metric 1', 'Other'])

        expect:
        parser.pathColumnName == 'Path'
        parser.sizeColumnName == 'Size'
        parser.colourColumnName == 'Metric 2'
        parser.extraData == [['1.1', 'A'], ['2.1', 'B'], ['3.1', 'C'], ['4.1', 'D']]
    }

    def "Should be possible to retrieve extra column names"() {
        given:
        parser = new CsvDataParser(TEST_DATA, '1', '3', '5', ['4', '2'])

        expect:
        parser.extraColumnNames == ['Metric 1', 'Other']
    }

    def "Should be possible to retrieve extra column IDs"() {
        given:
        parser = new CsvDataParser(TEST_DATA, '1', '3', '5', ['4', '2'])

        expect:
        parser.extraColumnIds == ['@3', '@1']
    }

    def "Should be possible to select all extra columns (excluding size, colour and path)"() {
        given:
        parser = new CsvDataParser(TEST_DATA, '1', '3', '5', ['ALL'])

        expect:
        parser.extraColumnNames == ['Other', 'Metric 1']
        parser.extraColumnIds == ['@1', '@3']
    }

    def "Should be possible to retrieve ID-to-name map"() {
        given:
        parser = new CsvDataParser(TEST_DATA, '1', '3', '5', ['4', '2'])

        expect:
        parser.columnIdToNameMap == ['@0': 'Path', '@1': 'Other', '@2': 'Size', '@3': 'Metric 1', '@4': 'Metric 2']
    }
}