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

    def "Configuration option changes behaviour to generate single summary"() {
        given: "a stub Configurator"
        Configurator configurator = Stub()
        configurator.includedPatterns >> [/simple.*\.txt/]
        configurator.excludedPatterns >> []
        configurator.overallOnly >> true
        configurator.arguments >> ['src/test/resources']

        and: "a FileMetricsGenerator with that configuration"
        generator = new FileMetricsGenerator(configurator: configurator)

        when:
        def metricsForFiles = generator.generateAllMetrics()

        then:
        metricsForFiles.size() == 1
        metricsForFiles.every { file, metricsCalculator ->
            file.path == 'OVERALL' && metricsCalculator.nonBlankLineCount == 10
        }
    }

    def "Should be able to create CSV output with default fields"() {
        given:
        def baos = new ByteArrayOutputStream()
        Configurator stubConfigurator = Stub()
        stubConfigurator.full >> false
        generator = new FileMetricsGenerator(stream: new PrintStream(baos), configurator: stubConfigurator)
        MetricsCalculator calculator = new MetricsCalculator([0, 1, 1, 2, -1, 1, 2, 3, 1, 0])
        File file = new File('/somewhere/somefile.txt')

        when:
        generator.generateCsvOutput([(file): calculator])
        def outputLines = baos.toString().split("\n")

        then:
        def headers = outputLines[0].trim().split(/,/) as List

        headers == ['Path', 'Total Lines', 'Non-Blank Lines', 'Total Indent', 'Mean Indent', 'Std Dev Indent',
                    'Max Indent', 'Mode Indent', 'Level 1+ %', 'Level 2+ %', 'Level 3+ %', 'Level 4+ %',
                    'Level 5+ %', 'Level 6+ %', 'Level 7+ %', 'Level 8+ %', 'Level 9+ %', 'Span 1 Count',
                    'Span 1 Max', 'Span 1 Mean', 'Span 2 Count', 'Span 2 Max', 'Span 2 Mean', 'Span 3 Count',
                    'Span 3 Max', 'Span 3 Mean']
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
        valueOf.'Level 1+ %' == '77.8'
        valueOf.'Level 2+ %' == '33.3'
        valueOf.'Level 3+ %' == '11.1'
        valueOf.'Level 4+ %' == '0.0'
        valueOf.'Level 5+ %' == '0.0'
        valueOf.'Level 6+ %' == '0.0'
        valueOf.'Level 7+ %' == '0.0'
        valueOf.'Level 8+ %' == '0.0'
        valueOf.'Level 9+ %' == '0.0'
        valueOf.'Span 1 Count' == '1'
        valueOf.'Span 1 Max' == '7'
        valueOf.'Span 1 Mean' == '7.000'
        valueOf.'Span 2 Count' == '2'
        valueOf.'Span 2 Max' == '2'
        valueOf.'Span 2 Mean' == '1.500'
        valueOf.'Span 3 Count' == '1'
        valueOf.'Span 3 Max' == '1'
        valueOf.'Span 3 Mean' == '1.000'
    }

    def "Should be able to create CSV output with additional fields"() {
        given:
        def baos = new ByteArrayOutputStream()
        Configurator stubConfiguration = Stub()
        stubConfiguration.full >> true
        generator = new FileMetricsGenerator(stream: new PrintStream(baos), configurator: stubConfiguration)
        MetricsCalculator calculator = new MetricsCalculator([0, 1, 1, 2, -1, 1, 2, 3, 1, 0])
        File file = new File('/somewhere/somefile.txt')

        when:
        generator.generateCsvOutput([(file): calculator])
        def outputLines = baos.toString().split("\n")

        then:
        def headers = outputLines[0].trim().split(/,/) as List

        headers == [
                'Path', 'Total Lines', 'Non-Blank Lines', 'Total Indent', 'Mean Indent', 'Std Dev Indent',
                'Max Indent', 'Mode Indent', 'Level 1+ %', 'Level 2+ %', 'Level 3+ %', 'Level 4+ %',
                'Level 5+ %', 'Level 6+ %', 'Level 7+ %', 'Level 8+ %', 'Level 9+ %', 'Span 1 Count',
                'Span 1 Max', 'Span 1 Mean', 'Span 2 Count', 'Span 2 Max', 'Span 2 Mean', 'Span 3 Count',
                'Span 3 Max', 'Span 3 Mean',
                'Level 0 Lines', 'Level 1 Lines', 'Level 2 Lines', 'Level 3 Lines', 'Level 4 Lines',
                'Level 5 Lines', 'Level 6 Lines', 'Level 7 Lines', 'Level 8 Lines', 'Level 9 Lines',
                'Span 1 Min', 'Span 1 Std Dev', 'Span 2 Min', 'Span 2 Std Dev', 'Span 3 Min', 'Span 3 Std Dev'
        ]

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
        valueOf.'Level 0 Lines' == '2'
        valueOf.'Level 1 Lines' == '4'
        valueOf.'Level 2 Lines' == '2'
        valueOf.'Level 3 Lines' == '1'
        valueOf.'Level 4 Lines' == '0'
        valueOf.'Level 5 Lines' == '0'
        valueOf.'Level 6 Lines' == '0'
        valueOf.'Level 7 Lines' == '0'
        valueOf.'Level 8 Lines' == '0'
        valueOf.'Level 9 Lines' == '0'
        valueOf.'Level 1+ %' == '77.8'
        valueOf.'Level 2+ %' == '33.3'
        valueOf.'Level 3+ %' == '11.1'
        valueOf.'Level 4+ %' == '0.0'
        valueOf.'Level 5+ %' == '0.0'
        valueOf.'Level 6+ %' == '0.0'
        valueOf.'Level 7+ %' == '0.0'
        valueOf.'Level 8+ %' == '0.0'
        valueOf.'Level 9+ %' == '0.0'
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
    }

    def "Missing spans should generate zero fields"() {
        given:
        def baos = new ByteArrayOutputStream()
        Configurator stubConfigurator = Stub()
        stubConfigurator.full >> true
        generator = new FileMetricsGenerator(stream: new PrintStream(baos), configurator: stubConfigurator)
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
        valueOf.'Level 0 Lines' == '2'
        valueOf.'Level 1 Lines' == '4'
        valueOf.'Level 2 Lines' == '3'
        valueOf.'Level 3 Lines' == '0'
        valueOf.'Level 4 Lines' == '0'
        valueOf.'Level 5 Lines' == '0'
        valueOf.'Level 6 Lines' == '0'
        valueOf.'Level 7 Lines' == '0'
        valueOf.'Level 8 Lines' == '0'
        valueOf.'Level 9 Lines' == '0'
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
    }

    def "Printing metrics should generate expected output"() {
        given:
        def baos = new ByteArrayOutputStream()
        Configurator stubConfigurator = Stub()
        stubConfigurator.full >> true
        generator = new FileMetricsGenerator(stream: new PrintStream(baos), configurator: stubConfigurator)
        MetricsCalculator calculator = new MetricsCalculator([0, 1, 1, 2, -1, 1, 2, 2, 1, 0])
        File file = new File('somefile.txt')

        when:
        generator.printMetricsForFiles([(file): calculator])
        def outputLines = baos.toString().split("\n").collect { it.trim() }

        then:
        outputLines[0] =~ /\==== .*somefile.txt ====/
        outputLines[1 .. -1] == [
                'Total lines: 10, Non-blank lines: 9',
                'Total indent: 10, Min indent: 0, Max indent: 2',
                'Mean indent: 1.111, Std Deviation: 0.737, Mode indent: 1, Probable function indent: 1',
                '----',
                'Histogram:',
                '0: ######################### 2',
                '1: ################################################## 4',
                '2: ##################################### 3',
                '----',
                'Span statistics:',
                'Level 1 - Count: 1, Min: 7, Max: 7, Mean: 7.000, StdDev: 0.000',
                'Level 2 - Count: 2, Min: 1, Max: 2, Mean: 1.500, StdDev: 0.500',
                '----',
        ]
    }
}