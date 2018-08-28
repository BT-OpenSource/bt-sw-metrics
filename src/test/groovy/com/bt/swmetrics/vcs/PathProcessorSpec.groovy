package com.bt.swmetrics.vcs

import com.bt.swmetrics.Configurator
import spock.lang.Specification
import spock.lang.Unroll


class PathProcessorSpec extends Specification {
    class TestProcessor implements PathProcessor {
        Configurator configurator
    }

    Configurator stubConfigurator
    TestProcessor processor

    @Unroll
    def "With no other prefixes specified, initial slashes are stripped - #path"() {
        given:
        stubConfigurator = Stub(Configurator)
        stubConfigurator.ignorePrefixes >> []
        processor = new TestProcessor(configurator: stubConfigurator)

        when:
        String stripped = processor.stripPrefixes(path)

        then:
        stripped == expected

        where:
        path    || expected
        null    || null
        ''      || ''
        '/'     || ''
        '/a/b'  || 'a/b'
        'a/b'   || 'a/b'
        'a/b/'  || 'a/b/'
    }

    @Unroll
    def "With prefixes specified, they and initial slashes are stripped - #path"() {
        given:
        stubConfigurator = Stub(Configurator)
        stubConfigurator.ignorePrefixes >> ['/a', 'b']
        processor = new TestProcessor(configurator: stubConfigurator)

        when:
        String stripped = processor.stripPrefixes(path)

        then:
        stripped == expected

        where:
        path        || expected
        null        || null
        ''          || ''
        '/'         || ''
        '/a/b'      || 'b'
        'a/b'       || 'b'
        'a/b/'      || 'b/'
        'b/c'       || 'c'
        '/b/c'      || 'c'
    }

    @Unroll
    def "Checking for path inclusion/exclusion"() {
        given:
        stubConfigurator = Stub(Configurator)
        stubConfigurator.includedPatterns >> included
        stubConfigurator.excludedPatterns >> excluded
        processor = new TestProcessor(configurator: stubConfigurator)

        when:
        def result = processor.checkIfPathIncluded('a/b/c/d')

        then:
        result == expected

        where:
        included    | excluded      || expected
        []          | []            || true
        ['.*/b/.*'] | []            || true
        []          | ['.*/b/.*']   || false
        ['.*/b/.*'] | ['.*/d$']     || false

    }
}