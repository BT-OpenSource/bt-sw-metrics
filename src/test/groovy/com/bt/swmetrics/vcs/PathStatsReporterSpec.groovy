package com.bt.swmetrics.vcs

import com.bt.swmetrics.Configurator
import spock.lang.Specification

class PathStatsReporterSpec extends Specification {
    PathStatsReporter reporter

    def "Should be able to generate 'svn ls' derived CSV"() {
        given:
        def baos = new ByteArrayOutputStream()
        Configurator stubConfigurator = Stub()
        stubConfigurator.vcsListFile >> 'src/test/resources/svn.list'
        stubConfigurator.vcsType >> 'svn'
        stubConfigurator.ignorePrefixes >> ['Backend/']
        reporter = new PathStatsReporter(stream: new PrintStream(baos), configurator: stubConfigurator)

        when:
        reporter.generateVcsMetricsCsv()
        def outputLines = baos.toString().split("\n")

        then:
        def headers = outputLines[0].trim().split(/,/)
        def values = outputLines[1 .. -1].sort().collect { it.trim() .split(/,/) }.transpose()
        def valueOf = [headers, values].transpose().collectEntries()

        valueOf.'Path' == ['""', '"ASG"', '"ASG/103_BTGCE_13790.mig"', '"ASG/103_BTGCE_13790_MSI.met"']
        valueOf.'Bytes' == ['0', '0', '832', '2814']
        valueOf.'Last Commit Age Days' == ['0', '19', '272', '270']
        valueOf.'Last Commit Date' == ['2017-06-14T07:25:43.945801Z', '2017-05-25T10:01:49.823265Z',
                                       '2016-09-14T13:19:11.658242Z', '2016-09-16T13:02:20.279856Z']
        valueOf.'Last Committer' == ['alice', 'bob', 'charlie', 'dan']
    }

    def "Should be able to generate 'svn log' derived CSV"() {
        given:
        def baos = new ByteArrayOutputStream()
        Configurator stubConfigurator = Stub()
        stubConfigurator.vcsLogFile >> 'src/test/resources/svn.log'
        stubConfigurator.vcsType >> 'svn'
        stubConfigurator.ignorePrefixes >> ['/trunk/']
        reporter = new PathStatsReporter(stream: new PrintStream(baos), configurator: stubConfigurator)

        when:
        reporter.generateVcsMetricsCsv()
        def outputLines = baos.toString().split("\n")

        then:
        def headers = outputLines[0].trim().split(/,/)
        def values = outputLines[1 .. -1].sort().collect { it.trim() .split(/,/) }.transpose()
        def valueOf = [headers, values].transpose().collectEntries()

        valueOf.'Path' == ['"build/SWNS/PACKAGES/ADMIN_DEFECTS.pkb"', '"build/SWNS/PACKAGES/ADMIN_DEFECTS.pks"',
                           '"build/SWNS/PACKAGES/ADMIN_MISC.pkb"', '"build/SWNS/PACKAGES/ADMIN_MISC.pks"',
                           '"build/SWNS/PACKAGES/ADMIN_PERMITS_DB.pkb"', '"build/SWNS/PACKAGES/ADMIN_PERMITS_DB.pks"',
                           '"build/SWNS/PACKAGES/ADMIN_S74.pkb"', '"build/SWNS/PACKAGES/ADMIN_S74.pks"',
                           '"mbe2/settings.xml"', '"src/Shell%20Scripts/frontendtest.sh"']
        valueOf.'Bytes' == ['1'] * 10
        valueOf.'Last Commit Age Days' == ['15', '15', '15', '15', '15', '14', '15', '15', '14', '0']
        valueOf.'Last Commit Date' == ['2015-12-22T14:21:27.443167Z',
                                       '2015-12-22T14:21:27.443167Z',
                                       '2015-12-22T14:21:27.443167Z',
                                       '2015-12-22T14:21:27.443167Z',
                                       '2015-12-22T14:21:27.443167Z',
                                       '2015-12-22T18:39:50.562712Z',
                                       '2015-12-22T14:21:27.443167Z',
                                       '2015-12-22T14:21:27.443167Z',
                                       '2015-12-22T18:39:50.562712Z',
                                       '2016-01-06T15:01:04.364200Z']
        valueOf.'Last Committer' == ['steve'] * 9 + ['simon']
        valueOf.'Total Commits' == ['1', '1', '1', '1', '1', '2', '1', '1', '1', '10']
        valueOf.'Aged Commit Value' == ['0.944', '0.944', '0.944', '0.944', '0.944', '1.891', '0.944', '0.944', '0.948', '9.902']
        valueOf.'Lifetime Days' == ['1', '1', '1', '1', '1', '2', '1', '1', '1', '14']
        valueOf.'Active Days' == ['1', '1', '1', '1', '1', '2', '1', '1', '1', '2']
        valueOf.'Lifetime Change Rate' == ['1.000'] * 9 + ['0.714']
        valueOf.'Main Committer' == ['steve'] * 9 + ['simon']
        valueOf.'Main Committer Percent' == ['100.0'] * 9 + ['90.0']
        valueOf.'Total Committers' == ['1'] * 9 + ['2']
    }

    def "Should be able to generate CSV from combined svn ls and log output"() {
        given:
        def baos = new ByteArrayOutputStream()
        Configurator stubConfigurator = Stub()
        stubConfigurator.vcsListFile >> 'src/test/resources/svn2.list'
        stubConfigurator.vcsLogFile >> 'src/test/resources/svn2.log'
        stubConfigurator.vcsType >> 'svn'
        stubConfigurator.ignorePrefixes >> ['Backend/pacs_server', '/WSPACS/trunk/Backend/pacs_server']
        reporter = new PathStatsReporter(stream: new PrintStream(baos), configurator: stubConfigurator)

        when:
        reporter.generateVcsMetricsCsv()
        def outputLines = baos.toString().split("\n")

        then:
        def headers = outputLines[0].trim().split(/,/)
        def values = outputLines[1 .. -1].sort().collect { it.trim() .split(/,/) }.transpose()
        def valueOf = [headers, values].transpose().collectEntries()

        valueOf.'Path' == ['"Plsql/errpkg.pkb"']
        valueOf.'Bytes' == ['12488']
        valueOf.'Last Commit Age Days' == ['0']
        valueOf.'Last Commit Date' == ['2015-05-20T11:58:03.329338Z']
        valueOf.'Last Committer' == ['neil']
        valueOf.'Total Commits' == ['2']
        valueOf.'Aged Commit Value' == ['1.009']
        valueOf.'Lifetime Days' == ['1231']
        valueOf.'Active Days' == ['2']
        valueOf.'Lifetime Change Rate' == ['0.002']
        valueOf.'Main Committer' == ['neil']
        valueOf.'Main Committer Percent' == ['100.0']
    }

    def "Should be able to generate 'git log' derived CSV"() {
        given:
        def baos = new ByteArrayOutputStream()
        Configurator stubConfigurator = Stub()
        stubConfigurator.vcsLogFile >> 'src/test/resources/git.log'
        stubConfigurator.vcsType >> 'git'
        reporter = new PathStatsReporter(stream: new PrintStream(baos), configurator: stubConfigurator)

        when:
        reporter.generateVcsMetricsCsv()
        def outputLines = baos.toString().split("\n")

        then:
        def headers = outputLines[0].trim().split(/,/)
        def values = outputLines[1 .. -1].sort().collect { it.trim() .split(/,/) }.transpose()
        def valueOf = [headers, values].transpose().collectEntries()

        valueOf.'Path' == ['"src/main/groovy/com/bt/swmetrics/svn/SvnRunner.groovy"',
                           '"src/test/groovy/com/bt/swmetrics/svn/AuthorStatsSpec.groovy"',
                           '"src/test/groovy/com/bt/swmetrics/svn/SvnRunnerSpec.groovy"',]
        valueOf.'Bytes' == ['1'] * 3
        valueOf.'Last Commit Age Days' == ['0', '1', '0']
        valueOf.'Last Commit Date' == ['2017-08-03T10:54:15Z',
                                       '2017-08-02T09:48:12Z',
                                       '2017-08-03T10:54:15Z']
        valueOf.'Last Committer' == ['neil', 'fred', 'neil']
        valueOf.'Total Commits' == ['3', '2', '2']
        valueOf.'Aged Commit Value' == ['2.988', '1.988', '1.992']
        valueOf.'Lifetime Days' == ['3', '2', '3']
        valueOf.'Active Days' == ['3', '2', '2']
        valueOf.'Lifetime Change Rate' == ['1.000', '1.000', '0.667']
        valueOf.'Main Committer' == ['neil', 'fred', 'neil']
        valueOf.'Main Committer Percent' == ['66.7', '50.0', '100.0']
        valueOf.'Total Committers' == ['2', '2', '1']
    }
}