package com.bt.swmetrics.filemetrics

import com.bt.swmetrics.Configurator
import spock.lang.Specification

class FileMetricsGeneratorSpec extends Specification {
    FileMetricsGenerator generator

    def "Should be able to generate all metrics"() {
        given: "a stub Configurator"
        Configurator configurator = Stub()
        configurator.includedPatterns >> [/simple.*\.txt/]
        configurator.excludedPatterns >> []
        configurator.arguments >> ['src/test/resources']

        and: "a FileMetricsGenerator with that configuration"
        generator = new FileMetricsGenerator(configurator: configurator)

        when:
        def metricsForFiles = generator.generateAllMetrics()

        then:
        metricsForFiles.size() == 2
        metricsForFiles.every { file, metricsCalculator ->
            metricsCalculator.nonBlankLineCount == 5
        }
    }

    def "Should be able to create CSV output"() {
        given:
        def baos = new ByteArrayOutputStream()
        generator = new FileMetricsGenerator(stream: new PrintStream(baos))
        MetricsCalculator calculator = new MetricsCalculator([0, 1, 1, 2, -1, 1, 2, 3, 1, 0])
        File file = new File('/somewhere/somefile.txt')

        when:
        generator.generateCsvOutput([(file): calculator])
        def outputLines = baos.toString().split("\n")

        then:
        def headers = outputLines[0].trim().split(/,/)
        def values = outputLines[1].trim().split(/,/)
        def valueOf = [headers, values].transpose().collectEntries()

        valueOf.'Path' =='"/somewhere/somefile.txt"'
        valueOf.'Total Lines' == '10'
        valueOf.'Non-Blank Lines' == '9'
        valueOf.'Total Indent' == '11'
        valueOf.'Mean Indent' == '1.222'
        valueOf.'Std Dev Indent' == '0.916'
        valueOf.'Max Indent' == '3'
        valueOf.'Mode Indent' == '1'
        valueOf.'Probable Function Indent' == '1'
        valueOf.'Level 0' == '2'
        valueOf.'Level 1' == '4'
        valueOf.'Level 2' == '2'
        valueOf.'Level 3' == '1'
        valueOf.'Level 4' == '0'
        valueOf.'Level 5' == '0'
        valueOf.'Level 6' == '0'
        valueOf.'Level 7' == '0'
        valueOf.'Level 8' == '0'
        valueOf.'Level 9' == '0'
        valueOf.'Pct Level 1+' == '77.8'
        valueOf.'Pct Level 2+' == '33.3'
        valueOf.'Pct Level 3+' == '11.1'
        valueOf.'Pct Level 4+' == '0.0'
        valueOf.'Pct Level 5+' == '0.0'
        valueOf.'Pct Level 6+' == '0.0'
        valueOf.'Pct Level 7+' == '0.0'
        valueOf.'Pct Level 8+' == '0.0'
        valueOf.'Pct Level 9+' == '0.0'
        valueOf.'Span 1 Count' == '1'
        valueOf.'Span 1 Min' == '7'
        valueOf.'Span 1 Max' == '7'
        valueOf.'Span 1 Mean' == '7.000'
        valueOf.'Span 1 Std Dev' == '0.000'
        valueOf.'Span 2 Count' == '2'
        valueOf.'Span 2 Min' == '1'
        valueOf.'Span 2 Max' == '2'
        valueOf.'Span 2 Mean' == '1.500'
        valueOf.'Span 2 Std Dev' == '0.500'
        valueOf.'Span 3 Count' == '1'
        valueOf.'Span 3 Min' == '1'
        valueOf.'Span 3 Max' == '1'
        valueOf.'Span 3 Mean' == '1.000'
        valueOf.'Span 3 Std Dev' == '0.000'
        valueOf.'Function Span Count' == '1'
        valueOf.'Function Span Min' == '7'
        valueOf.'Function Span Max' == '7'
        valueOf.'Function Span Mean' == '7.000'
        valueOf.'Function Span Std Dev' == '0.000'
    }

    def "Missing spans should generate zero fields"() {
        given:
        def baos = new ByteArrayOutputStream()
        generator = new FileMetricsGenerator(stream: new PrintStream(baos))
        MetricsCalculator calculator = new MetricsCalculator([0, 1, 1, 2, -1, 1, 2, 2, 1, 0])
        File file = new File('somefile.txt')

        when:
        generator.generateCsvOutput([(file): calculator])
        def outputLines = baos.toString().split("\n")

        then:
        def headers = outputLines[0].trim().split(/,/)
        def values = outputLines[1].trim().split(/,/)
        def valueOf = [headers, values].transpose().collectEntries()

        valueOf.'Path' == '"somefile.txt"'
        valueOf.'Total Lines' == '10'
        valueOf.'Non-Blank Lines' == '9'
        valueOf.'Total Indent' == '10'
        valueOf.'Mean Indent' == '1.111'
        valueOf.'Std Dev Indent' == '0.737'
        valueOf.'Max Indent' == '2'
        valueOf.'Mode Indent' == '1'
        valueOf.'Probable Function Indent' == '1'
        valueOf.'Level 0' == '2'
        valueOf.'Level 1' == '4'
        valueOf.'Level 2' == '3'
        valueOf.'Level 3' == '0'
        valueOf.'Level 4' == '0'
        valueOf.'Level 5' == '0'
        valueOf.'Level 6' == '0'
        valueOf.'Level 7' == '0'
        valueOf.'Level 8' == '0'
        valueOf.'Level 9' == '0'
        valueOf.'Span 1 Count' == '1'
        valueOf.'Span 1 Min' == '7'
        valueOf.'Span 1 Max' == '7'
        valueOf.'Span 1 Mean' == '7.000'
        valueOf.'Span 1 Std Dev' == '0.000'
        valueOf.'Span 2 Count' == '2'
        valueOf.'Span 2 Min' == '1'
        valueOf.'Span 2 Max' == '2'
        valueOf.'Span 2 Mean' == '1.500'
        valueOf.'Span 2 Std Dev' == '0.500'
        valueOf.'Span 3 Count' == '0'
        valueOf.'Span 3 Min' == '0'
        valueOf.'Span 3 Max' == '0'
        valueOf.'Span 3 Mean' == '0'
        valueOf.'Span 3 Std Dev' == '0'
        valueOf.'Function Span Count' == '1'
        valueOf.'Function Span Min' == '7'
        valueOf.'Function Span Max' == '7'
        valueOf.'Function Span Mean' == '7.000'
        valueOf.'Function Span Std Dev' == '0.000'
    }
}