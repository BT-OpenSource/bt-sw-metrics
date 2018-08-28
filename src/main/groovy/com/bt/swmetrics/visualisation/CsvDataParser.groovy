package com.bt.swmetrics.visualisation

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser

class CsvDataParser {
    public static final String SELECT_ALL_EXTRA = 'ALL'
    private String pathColumn
    private String sizeColumn
    private String colourColumn
    private List<String> extraColumns

    private List<String> paths
    private List<BigDecimal> sizes
    private List<BigDecimal> colours
    private CSVParser csvParser
    private List<List<String>> extraData

    CsvDataParser(String csvData, String pathColumn, String sizeColumn, String colourColumn, List<String> extraColumns = []) {
        this.pathColumn = pathColumn
        this.sizeColumn = sizeColumn
        this.colourColumn = colourColumn
        this.extraColumns = extraColumns
        parseCsvData(csvData)
    }

    def parseCsvData(String data) {
        csvParser = CSVParser.parse(data, CSVFormat.DEFAULT.withHeader())

        int pathIndex = findIndexFor(pathColumn)
        int sizeIndex = findIndexFor(sizeColumn)
        int colourIndex = findIndexFor(colourColumn)
        def extraIndices = extraColumnNames.collect { findIndexFor(it) }

        (paths, sizes, colours, extraData) = this.csvParser.records.collect {
            String path = it[pathIndex]
            BigDecimal size = convertPotentiallyBlankStringToBigDecimal(it[sizeIndex])
            BigDecimal colour = convertPotentiallyBlankStringToBigDecimal(it[colourIndex])

            [path, size, colour, extraIndices.collect { index -> it[index]}]
        }.transpose()
    }

    List<String> getPaths() {
        paths
    }

    List<BigDecimal> getSizes() {
        sizes
    }

    List<BigDecimal> getColours() {
        colours
    }

    List<List<String>> getExtraData() {
        extraData
    }

    private int findIndexFor(String name) {
        if (name =~ /^\d+$/) {
            (name as Integer) - 1
        } else if (csvParser.headerMap[name] == null) {
            throw new IllegalArgumentException("Invalid column name: $name - possible values are: ${csvParser.headerMap.keySet().join(',')}")
        } else {
            csvParser.headerMap[name]
        }
    }

    private static BigDecimal convertPotentiallyBlankStringToBigDecimal(String str) {
        str ? str as BigDecimal : 0.0
    }

    String getPathColumnName() {
        nameOfColumn(pathColumn)
    }

    String getPathColumnId() {
        indexToId(findIndexFor(pathColumn))
    }

    private static indexToId(int index) {
        '@' + index
    }

    String getSizeColumnName() {
        nameOfColumn(sizeColumn)
    }

    String getSizeColumnId() {
        indexToId(findIndexFor(sizeColumn))
    }

    String getColourColumnName() {
        nameOfColumn(colourColumn)
    }

    String getColourColumnId() {
        indexToId(findIndexFor(colourColumn))
    }

    List<String> getExtraColumnNames() {
        if (extraColumns.contains(SELECT_ALL_EXTRA)) {
            csvParser.headerMap.keySet().findAll {
                !(it in [pathColumnName, sizeColumnName, colourColumnName])
            } as List<String>
        } else {
            extraColumns.collect { nameOfColumn(it) }
        }
    }

    private String nameOfColumn(String nameOrNumber) {
        def index = findIndexFor(nameOrNumber)
        csvParser.headerMap.collectEntries { k, v -> [(v): k] }[index]
    }

    List<String> getExtraColumnIds() {
         extraColumnNames.collect { indexToId(findIndexFor(it)) }
    }

    Map<String,String> getColumnIdToNameMap() {
        csvParser.headerMap.collectEntries { k, v -> [(indexToId(v)): k] }
    }
}
