package com.bt.swmetrics.vcs.svn

import com.bt.swmetrics.Configurator
import spock.lang.Specification


class SvnDiffParserSpec extends Specification {
    final static List<String> DIFF_LINES = new File('src/test/resources/svn.diff').readLines()
    public static final String XML_FILE_PATH = 'OD File/0030_OD.xml'
    public static final String SQL_FILE_PATH = 'scripts/repeatable/#test_#packagebody_!swns_ut_eton_xml_processing.sql'

    Configurator stubConfigurator
    SvnDiffParser parser

    def setup() {
        stubConfigurator = Stub(Configurator)
        stubConfigurator.ignorePrefixes >> []
        parser = new SvnDiffParser(lines: DIFF_LINES, configurator: stubConfigurator)
    }

    def "Should be able to obtain a list of all paths from the Index lines"() {
        expect:
        parser.paths.toSorted() == [
                XML_FILE_PATH,
                SQL_FILE_PATH
                ]
    }

    def "Should be able to split lines by path"() {
        expect:
        parser.linesByPath[XML_FILE_PATH].size() == 12
        parser.linesByPath[SQL_FILE_PATH].size() == 1343
    }

    def "Should be able to get info for each @@-delimited chunk per file"() {
        when:
        def chunks = parser.chunksByPath

        then:
        chunks[XML_FILE_PATH].size() == 1

        chunks[SQL_FILE_PATH].size() == 7
        chunks[SQL_FILE_PATH][1].oldStart == 5765
        chunks[SQL_FILE_PATH][1].newStart == 5771
    }

    def "Should strip prefixes when configured"() {
        given:
        stubConfigurator = Stub(Configurator)
        stubConfigurator.ignorePrefixes >> ['scripts/repeatable']
        parser.configurator = stubConfigurator

        expect:
        parser.paths.contains('#test_#packagebody_!swns_ut_eton_xml_processing.sql')
        parser.chunksByPath['#test_#packagebody_!swns_ut_eton_xml_processing.sql'].size() == 7
    }
}