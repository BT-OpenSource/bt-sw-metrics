package com.bt.swmetrics.csv

import com.bt.swmetrics.Configurator
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter

class CsvJoinRunner {
    PrintStream stream
    Configurator configurator

    def joinFiles() {
        if (configurator.arguments.size() != 2) {
            throw new IllegalArgumentException("Must supply exactly two CSV files to join")
        }
        CsvJoiner joiner = new CsvJoiner(new File(configurator.arguments[0]).text, new File(configurator.arguments[1]).text)
        joiner.leftField = configurator.csvJoinFields[0]
        joiner.rightField = configurator.csvJoinFields[1]
        joiner.ignorePrefixes = configurator.ignorePrefixes
        def result = (configurator.joinType == Configurator.JOIN_TYPE_INNER) ? joiner.innerJoinResult : joiner.outerJoinResult
        CSVPrinter printer = new CSVPrinter(stream, CSVFormat.DEFAULT)
        printer.printRecord(joiner.headers)
        printer.printRecords(result)
    }
}
