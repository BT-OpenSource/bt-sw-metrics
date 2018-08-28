package com.bt.swmetrics.csv

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser

import static com.bt.swmetrics.PathOperations.stripAnyMatchingPrefixFromPath

class CsvJoiner {
    private CSVParser leftParser
    private CSVParser rightParser
    private String leftField
    private String rightField
    List<String> ignorePrefixes = []

    CsvJoiner(String leftData, String rightData) {
        leftParser = CSVParser.parse(leftData, CSVFormat.DEFAULT.withHeader())
        rightParser = CSVParser.parse(rightData, CSVFormat.DEFAULT.withHeader())
    }

    String setLeftField(String leftField) {
        checkFieldExists('left', leftField, leftParser)
        this.leftField = leftField
    }

    String setRightField(String rightField) {
        checkFieldExists('right', rightField, rightParser)
        this.rightField = rightField
    }

    private static checkFieldExists(String name, String field, CSVParser parser) {
        if (parser.headerMap[field] == null) {
            throw new IllegalArgumentException("No such field '$field' in $name file. Valid values are: ${parser.headerMap.keySet().join(',')}")
        }
    }

    List<String> getHeaders() {
        def rightHeaders = (rightParser.headerMap.keySet() - rightField).collect {
            (leftParser.headerMap[it] == null) ? it : (it + ' (other)')
        }
        (leftParser.headerMap.keySet() + rightHeaders) as List<String>
    }

    List<List<String>> getOuterJoinResult() {
        def headerSize = headers.size()
        internalJoin().collect { it + ([''] * (headerSize - it.size())) }
    }

    private List<List<String>> internalJoin() {
        def leftMapped = leftParser.records.collectEntries {
            def stripped = stripAnyMatchingPrefixFromPath(ignorePrefixes, it.get(leftField))
            [(stripped): it]
        }
        def rightMapped = rightParser.records.collectEntries{
            def stripped = stripAnyMatchingPrefixFromPath(ignorePrefixes, it.get(rightField))
            [(stripped): it]
        }
        def rightFieldIndex = rightParser.headerMap[rightField]
        leftMapped.collect { key, value ->
            def leftValues = value.collect() as List<String>
            def rightValues = rightMapped[key].collect() as List<String>
            if (rightValues) {
                rightValues.removeAt(rightFieldIndex)
            }
            leftValues + rightValues
        }
    }

    List<List<String>> getInnerJoinResult() {
        def headerSize = headers.size()
        internalJoin().grep { it.size() == headerSize }
    }
}
