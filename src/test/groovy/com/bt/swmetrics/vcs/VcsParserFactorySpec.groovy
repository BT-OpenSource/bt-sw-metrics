package com.bt.swmetrics.vcs

import com.bt.swmetrics.Configurator
import com.bt.swmetrics.vcs.git.GitDiffParser
import com.bt.swmetrics.vcs.git.GitListParser
import com.bt.swmetrics.vcs.git.GitLogParser
import com.bt.swmetrics.vcs.svn.SvnDiffParser
import com.bt.swmetrics.vcs.svn.SvnListParser
import com.bt.swmetrics.vcs.svn.SvnLogParser
import spock.lang.Specification


class VcsParserFactorySpec extends Specification {
    def "Unset VCS type should throw an exception getting log parser"() {
        given:
        Configurator configurator = Stub()
        configurator.vcsType >> null

        when:
        VcsParserFactory.getLogParserInstance(configurator)

        then:
        thrown(IllegalArgumentException)
    }

    def "Unknown VCS type should throw an exception getting log parser"() {
        given:
        Configurator configurator = Stub()
        configurator.vcsType >> 'unknown'

        when:
        VcsParserFactory.getLogParserInstance(configurator)

        then:
        thrown(IllegalArgumentException)
    }

    def "VCS type 'svn' should give an SvnLogParser"() {
        given:
        Configurator configurator = Stub()
        configurator.vcsType >> 'svn'

        when:
        def parser = VcsParserFactory.getLogParserInstance(configurator)

        then:
        parser instanceof SvnLogParser
    }

    def "VCS type 'git' should give a GitLogParser"() {
        given:
        Configurator configurator = Stub()
        configurator.vcsType >> 'git'

        when:
        def parser = VcsParserFactory.getLogParserInstance(configurator)

        then:
        parser instanceof GitLogParser
    }

    def "Unset VCS type should throw an exception getting list parser"() {
        given:
        Configurator configurator = Stub()
        configurator.vcsType >> null

        when:
        VcsParserFactory.getListParserInstance(configurator)

        then:
        thrown(IllegalArgumentException)
    }

    def "Unknown VCS type should throw an exception getting list parser"() {
        given:
        Configurator configurator = Stub()
        configurator.vcsType >> 'unknown'

        when:
        VcsParserFactory.getListParserInstance(configurator)

        then:
        thrown(IllegalArgumentException)
    }

    def "VCS type 'svn' should give an SvnListParser"() {
        given:
        Configurator configurator = Stub()
        configurator.vcsType >> 'svn'

        when:
        def parser = VcsParserFactory.getListParserInstance(configurator)

        then:
        parser instanceof SvnListParser
    }

    def "VCS type 'git' should give a GitListParser"() {
        given:
        Configurator configurator = Stub()
        configurator.vcsType >> 'git'

        when:
        def parser = VcsParserFactory.getListParserInstance(configurator)

        then:
        parser instanceof GitListParser
    }

    def "VCS type 'svn' should give an SvnDiffParser"() {
        given:
        Configurator configurator = Stub()
        configurator.vcsType >> 'svn'

        when:
        def parser = VcsParserFactory.getDiffParserInstance(configurator)

        then:
        parser instanceof SvnDiffParser
    }

    def "VCS type 'git' should give a GitDiffParser"() {
        given:
        Configurator configurator = Stub()
        configurator.vcsType >> 'git'

        when:
        def parser = VcsParserFactory.getDiffParserInstance(configurator)

        then:
        parser instanceof GitDiffParser
    }
}