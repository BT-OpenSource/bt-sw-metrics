package com.bt.swmetrics.vcs

import com.bt.swmetrics.Configurator
import spock.lang.Specification


class DiffStatsGeneratorSpec extends Specification {
    DiffStatsGenerator generator

    def "An empty svn diff file should just output the CSV headers"() {
        given:
        def baos = new ByteArrayOutputStream()
        Configurator stubConfigurator = Stub()
        stubConfigurator.vcsDiffFile >> 'src/test/resources/empty.txt'
        stubConfigurator.vcsType >> 'svn'
        generator = new DiffStatsGenerator(stream: new PrintStream(baos), configurator: stubConfigurator)

        when:
        generator.generateCsvReport()
        def outputLines = baos.toString().split("\n").collect { it.trim() }

        then:
        outputLines.size() == 1
        outputLines[0] == 'Path,Chunks,Lines Added,Lines Removed,Indent Change,Mean Before,Mean After,Std Dev Before,Std Dev After,Max Before,Max After'
    }

    def "Should be able to generate stats for a real svn diff file"() {
        given:
        def baos = new ByteArrayOutputStream()
        Configurator stubConfigurator = Stub()
        stubConfigurator.vcsDiffFile >> 'src/test/resources/svn.diff'
        stubConfigurator.vcsType >> 'svn'
        generator = new DiffStatsGenerator(stream: new PrintStream(baos), configurator: stubConfigurator)

        when:
        generator.generateCsvReport()
        def outputLines = baos.toString().split("\n").collect { it.trim() }

        then:
        outputLines.size() == 3
        outputLines[1 .. -1] ==
                ['scripts/repeatable/#test_#packagebody_!swns_ut_eton_xml_processing.sql,7,663,517,190,2.624,2.390,1.294,0.975,7,5',
                 'OD File/0030_OD.xml,1,1,1,0,1.000,1.000,0.000,0.000,1,1']
    }

    def "Should be able to generate textual report for a real svn diff file"() {
        given:
        def baos = new ByteArrayOutputStream()
        Configurator stubConfigurator = Stub()
        stubConfigurator.vcsDiffFile >> 'src/test/resources/svn.diff'
        stubConfigurator.vcsType >> 'svn'
        generator = new DiffStatsGenerator(stream: new PrintStream(baos), configurator: stubConfigurator)

        when:
        generator.generateTextReport()
        def outputLines = baos.toString().split("\n").collect { it.trim() }

        then:
        outputLines == '''=== scripts/repeatable/#test_#packagebody_!swns_ut_eton_xml_processing.sql
7 chunks, 663 lines added, 517 lines removed
Change in total indent: 190
Before: mean indent: 2.624, max indent: 7, standard deviation: 1.294
After:  mean indent: 2.390, max indent: 5, standard deviation: 0.975

=== OD File/0030_OD.xml
1 chunk, 1 line added, 1 line removed
Change in total indent: 0
Before: mean indent: 1.000, max indent: 1, standard deviation: 0.000
After:  mean indent: 1.000, max indent: 1, standard deviation: 0.000

'''.split("\n").collect { it.trim() }
    }

    def "CSV generation should be selected based on options supplied"() {
        def baos = new ByteArrayOutputStream()
        Configurator stubConfigurator = Stub()
        stubConfigurator.vcsDiffFile >> 'src/test/resources/empty.txt'
        stubConfigurator.vcsType >> 'svn'
        stubConfigurator.csvOutput >> true
        generator = new DiffStatsGenerator(stream: new PrintStream(baos), configurator: stubConfigurator)
    }
}