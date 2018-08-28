package com.bt.swmetrics.vcs

import com.bt.swmetrics.Configurator
import spock.lang.Specification


class AuthorStatsGeneratorSpec extends Specification {
    AuthorStatsGenerator generator

    def "Should be able to generate 'svn log' derived author stats, with monthly data (--full-stats)"() {
        given:
        def baos = new ByteArrayOutputStream()
        Configurator stubConfigurator = Stub()
        stubConfigurator.vcsLogFile >> 'src/test/resources/svn.log'
        stubConfigurator.vcsType >> 'svn'
        stubConfigurator.authorStats >> true
        stubConfigurator.full >> true
        generator = new AuthorStatsGenerator(stream: new PrintStream(baos), configurator: stubConfigurator)

        when:
        generator.generateAuthorStatsCsv()
        def outputLines = baos.toString().split("\n")

        then:
        def headers = outputLines[0].trim()
        def valueLines = outputLines[1 .. -1].sort().collect { it.trim() }

        headers == 'Author,Total Commits,Total Paths,Revisited Paths,Tenure,Active Days,First Commit,First Commit Age,Last Commit,Last Commit Age,Dec-15 Commits,Jan-16 Commits,Dec-15 Paths,Jan-16 Paths'
        valueLines == [
                '"luke",1,1,0,1,1,2016-01-06 11:45:25,0,2016-01-06 11:45:25,0,0,1,0,1',
                '"simon",9,1,1,14,2,2015-12-23 15:41:26,13,2016-01-06 15:01:04,0,2,7,2,7',
                '"steve",2,10,1,1,1,2015-12-22 14:21:27,15,2015-12-22 18:39:50,14,2,0,11,0',
        ]
    }

    def "Should be able to generate 'svn log' derived author stats, without monthly data"() {
        given:
        def baos = new ByteArrayOutputStream()
        Configurator stubConfigurator = Stub()
        stubConfigurator.vcsLogFile >> 'src/test/resources/svn.log'
        stubConfigurator.vcsType >> 'svn'
        stubConfigurator.authorStats >> true
        stubConfigurator.full >> false
        generator = new AuthorStatsGenerator(stream: new PrintStream(baos), configurator: stubConfigurator)

        when:
        generator.generateAuthorStatsCsv()
        def outputLines = baos.toString().split("\n")

        then:
        def headers = outputLines[0].trim()
        def valueLines = outputLines[1 .. -1].sort().collect { it.trim() }

        headers == 'Author,Total Commits,Total Paths,Revisited Paths,Tenure,Active Days,First Commit,First Commit Age,Last Commit,Last Commit Age'
        valueLines == [
                '"luke",1,1,0,1,1,2016-01-06 11:45:25,0,2016-01-06 11:45:25,0',
                '"simon",9,1,1,14,2,2015-12-23 15:41:26,13,2016-01-06 15:01:04,0',
                '"steve",2,10,1,1,1,2015-12-22 14:21:27,15,2015-12-22 18:39:50,14',
        ]
    }

    def "Should be able to generate 'svn log' derived author path stats"() {
        given:
        def baos = new ByteArrayOutputStream()
        Configurator stubConfigurator = Stub()
        stubConfigurator.vcsLogFile >> 'src/test/resources/svn.log'
        stubConfigurator.vcsType >> 'svn'
        stubConfigurator.authorStats >> true
        generator = new AuthorStatsGenerator(stream: new PrintStream(baos), configurator: stubConfigurator)

        when:
        generator.generateAuthorPathsCsv()
        def outputLines = baos.toString().split("\n")

        then:
        def headers = outputLines[0].trim()
        def valueLines = outputLines[1 .. -1].sort().collect { it.trim() }
        headers == 'Author,Path,First Commit,Last Commit,Total Commits'
        valueLines == [
                '"luke",trunk/src/Shell%20Scripts/frontendtest.sh,2016-01-06 11:45:25,2016-01-06 11:45:25,1',
                '"simon",trunk/src/Shell%20Scripts/frontendtest.sh,2015-12-23 15:41:26,2016-01-06 15:01:04,9',
                '"steve",trunk/build/SWNS/PACKAGES/ADMIN_DEFECTS.pkb,2015-12-22 14:21:27,2015-12-22 14:21:27,1',
                '"steve",trunk/build/SWNS/PACKAGES/ADMIN_DEFECTS.pks,2015-12-22 14:21:27,2015-12-22 14:21:27,1',
                '"steve",trunk/build/SWNS/PACKAGES/ADMIN_MISC.pkb,2015-12-22 14:21:27,2015-12-22 14:21:27,1',
                '"steve",trunk/build/SWNS/PACKAGES/ADMIN_MISC.pks,2015-12-22 14:21:27,2015-12-22 14:21:27,1',
                '"steve",trunk/build/SWNS/PACKAGES/ADMIN_PERMITS_DB.pkb,2015-12-22 14:21:27,2015-12-22 14:21:27,1',
                '"steve",trunk/build/SWNS/PACKAGES/ADMIN_PERMITS_DB.pks,2015-12-22 14:21:27,2015-12-22 18:39:50,2',
                '"steve",trunk/build/SWNS/PACKAGES/ADMIN_S74.pkb,2015-12-22 14:21:27,2015-12-22 14:21:27,1',
                '"steve",trunk/build/SWNS/PACKAGES/ADMIN_S74.pks,2015-12-22 14:21:27,2015-12-22 14:21:27,1',
                '"steve",trunk/mbe2/scripts/runMBE_ST.sh,2015-12-22 18:39:50,2015-12-22 18:39:50,1',
                '"steve",trunk/mbe2/settings.xml,2015-12-22 18:39:50,2015-12-22 18:39:50,1',
        ]
    }

    def "Should be able to generate 'git log' derived author stats, with monthly data (--full-stats)"() {
        AuthorStatsGenerator reporter

        given:
        def baos = new ByteArrayOutputStream()
        Configurator stubConfigurator = Stub()
        stubConfigurator.vcsLogFile >> 'src/test/resources/git.log'
        stubConfigurator.vcsType >> 'git'
        stubConfigurator.authorStats >> true
        stubConfigurator.full >> true
        reporter = new AuthorStatsGenerator(stream: new PrintStream(baos), configurator: stubConfigurator)

        when:
        reporter.generateAuthorStatsCsv()
        def outputLines = baos.toString().split("\n")

        then:
        def headers = outputLines[0].trim()
        def valueLines = outputLines[1 .. -1].sort().collect { it.trim() }

        headers == 'Author,Total Commits,Total Paths,Revisited Paths,Tenure,Active Days,First Commit,First Commit Age,Last Commit,Last Commit Age,Aug-17 Commits,Aug-17 Paths'
        valueLines == [
                '"fred",1,2,0,1,1,2017-08-02 10:48:12,1,2017-08-02 10:48:12,1,1,2',
                '"neil",2,4,2,3,2,2017-08-01 10:06:04,2,2017-08-03 11:54:15,0,2,6',
        ]
    }

    def "Should be able to generate 'git log' derived author stats, without monthly data"() {
        AuthorStatsGenerator reporter

        given:
        def baos = new ByteArrayOutputStream()
        Configurator stubConfigurator = Stub()
        stubConfigurator.vcsLogFile >> 'src/test/resources/git.log'
        stubConfigurator.vcsType >> 'git'
        stubConfigurator.authorStats >> true
        stubConfigurator.full >> false
        reporter = new AuthorStatsGenerator(stream: new PrintStream(baos), configurator: stubConfigurator)

        when:
        reporter.generateAuthorStatsCsv()
        def outputLines = baos.toString().split("\n")

        then:
        def headers = outputLines[0].trim()
        def valueLines = outputLines[1 .. -1].sort().collect { it.trim() }

        headers == 'Author,Total Commits,Total Paths,Revisited Paths,Tenure,Active Days,First Commit,First Commit Age,Last Commit,Last Commit Age'
        valueLines == [
                '"fred",1,2,0,1,1,2017-08-02 10:48:12,1,2017-08-02 10:48:12,1',
                '"neil",2,4,2,3,2,2017-08-01 10:06:04,2,2017-08-03 11:54:15,0',
        ]
    }

    def "Should be able to generate 'git log' derived author path stats"() {
        AuthorStatsGenerator reporter

        given:
        def baos = new ByteArrayOutputStream()
        Configurator stubConfigurator = Stub()
        stubConfigurator.vcsLogFile >> 'src/test/resources/git.log'
        stubConfigurator.vcsType >> 'git'
        stubConfigurator.authorStats >> true
        reporter = new AuthorStatsGenerator(stream: new PrintStream(baos), configurator: stubConfigurator)

        when:
        reporter.generateAuthorPathsCsv()
        def outputLines = baos.toString().split("\n")

        then:
        def headers = outputLines[0].trim()
        def valueLines = outputLines[1 .. -1].sort().collect { it.trim() }

        headers == 'Author,Path,First Commit,Last Commit,Total Commits'
        valueLines == [
                '"fred",src/main/groovy/com/bt/swmetrics/svn/SvnRunner.groovy,2017-08-02 10:48:12,2017-08-02 10:48:12,1',
                '"fred",src/test/groovy/com/bt/swmetrics/svn/AuthorStatsSpec.groovy,2017-08-02 10:48:12,2017-08-02 10:48:12,1',
                '"neil",src/main/groovy/com/bt/swmetrics/svn/SvnRunner.groovy,2017-08-01 10:06:04,2017-08-03 11:54:15,2',
                '"neil",src/main/groovy/com/bt/swmetrics/svn/SvnRunner.groovy.orig,2017-08-01 10:06:04,2017-08-01 10:06:04,1',
                '"neil",src/test/groovy/com/bt/swmetrics/svn/AuthorStatsSpec.groovy,2017-08-01 10:06:04,2017-08-01 10:06:04,1',
                '"neil",src/test/groovy/com/bt/swmetrics/svn/SvnRunnerSpec.groovy,2017-08-01 10:06:04,2017-08-03 11:54:15,2',
        ]
    }
}