package com.bt.swmetrics

import spock.lang.Specification

class ConfiguratorSpec extends Specification {
    Configurator configurator = new Configurator([] as String)

    def "Should be able to create Configurator with command-line args"() {
        when:
        configurator = new Configurator('foo', 'bar')

        then:
        configurator.arguments == ['foo', 'bar']
    }

    def "Should be able to mark the end of options"() {
        when:
        configurator = new Configurator('--', 'foo', 'bar')

        then:
        configurator.arguments == ['foo', 'bar']
    }

    def "Should be able to turn on debug logging"() {
        given:
        Configurator configurator1 = new Configurator(['--debug'] as String[])
        Configurator configurator2 = new Configurator(['-d'] as String[])

        expect:
        !configurator.debug
        configurator1.debug
        configurator2.debug
        System.getProperty('org.slf4j.simpleLogger.defaultLogLevel') == 'debug'
    }

    def "Should be able to turn on CSV-file generation"() {
        given:
        Configurator configurator1 = new Configurator(['--csv'] as String[])
        Configurator configurator2 = new Configurator(['-c'] as String[])

        expect:
        !configurator.csvOutput
        configurator1.csvOutput
        configurator2.csvOutput
    }

    def "Should be able to turn on annotated output"() {
        given:
        Configurator configurator1 = new Configurator(['--show-annotated'] as String[])
        Configurator configurator2 = new Configurator(['-a'] as String[])

        expect:
        !configurator.showAnnotated
        configurator1.showAnnotated
        configurator2.showAnnotated
    }

    def "Should be able to specify help ouput"() {
        given:
        Configurator configurator1 = new Configurator(['--help'] as String[])
        Configurator configurator2 = new Configurator(['-h'] as String[])

        expect:
        !configurator.helpRequested
        configurator1.helpRequested
        configurator2.helpRequested
    }

    def "Should be able to specify path exclusion patterns"() {
        given:
        Configurator configurator = new Configurator(['-x', 'pat1', '--exclude', 'pat2', 'foo'] as String[])

        expect:
        configurator.excludedPatterns == ['pat1', 'pat2']
    }

    def "Exclusion patterns should default to an empty list"() {
        given:
        Configurator configurator = new Configurator(['foo'] as String[])

        expect:
        configurator.excludedPatterns == []
    }

    def "Should be able to specify path inclusion patterns"() {
        given:
        Configurator configurator = new Configurator(['-i', 'pat1', '--include', 'pat2', 'foo'] as String[])

        expect:
        configurator.includedPatterns == ['pat1', 'pat2']
    }

    def "Inclusion patterns should default to an empty list"() {
        given:
        Configurator configurator = new Configurator(['foo'] as String[])

        expect:
        configurator.includedPatterns == []
    }

    def "Should be able to request treemap generation"() {
        given:
        Configurator configurator1 = new Configurator(['--treemap', 'jit'] as String[])
        Configurator configurator2 = new Configurator(['-t', 'd3plus'] as String[])

        expect:
        !configurator.treeMapVisualisation
        configurator1.treeMapVisualisation == 'jit'
        configurator2.treeMapVisualisation == 'd3plus'
    }

    def "Should be able to specify path column"() {
        given:
        Configurator configurator = new Configurator(['--path-column', 'Col Name'] as String[])

        expect:
        configurator.pathColumn == 'Col Name'
    }

    def "Path column should default to '1'"() {
        given:
        Configurator configurator = new Configurator(['foo'] as String[])

        expect:
        configurator.pathColumn == '1'
    }

    def "Should be able to specify size column"() {
        given:
        Configurator configurator = new Configurator(['--size-column', 'Col Name'] as String[])

        expect:
        configurator.sizeColumn == 'Col Name'
    }

    def "Size column should default to '2'"() {
        given:
        Configurator configurator = new Configurator(['foo'] as String[])

        expect:
        configurator.sizeColumn == '2'
    }

    def "Should be able to specify colour column"() {
        given:
        Configurator configurator = new Configurator(['--colour-column', 'Col Name'] as String[])

        expect:
        configurator.colourColumn == 'Col Name'
    }

    def "Path column should default to '3'"() {
        given:
        Configurator configurator = new Configurator(['foo'] as String[])

        expect:
        configurator.colourColumn == '3'
    }

    def "Should be able to specify extra columns, optionally separated by commas"() {
        given:
        Configurator configurator = new Configurator(['--extra-column', 'Col 1', '-A', 'Col 2,Col 3'] as String[])

        expect:
        configurator.extraColumns == ['Col 1', 'Col 2', 'Col 3']
    }

    def "Extra columns should default to an empty list"() {
        given:
        Configurator configurator = new Configurator(['foo'] as String[])

        expect:
        configurator.extraColumns == []
    }

    def "Should be able to specify bottom colour"() {
        given:
        Configurator configurator = new Configurator(['--bottom-colour', '#f00b1e'] as String[])

        expect:
        configurator.bottomColour == 0xf00b1e
    }

    def "Bottom colour should default if not provided"() {
        given:
        Configurator configurator = new Configurator(['foo'] as String[])

        expect:
        configurator.bottomColour == Configurator.DEFAULT_BOTTOM_COLOUR
    }

    def "Should be able to specify top colour"() {
        given:
        Configurator configurator = new Configurator(['--top-colour', '#f00b1e'] as String[])

        expect:
        configurator.topColour == 0xf00b1e
    }

    def "Top colour should default if not provided"() {
        given:
        Configurator configurator = new Configurator(['foo'] as String[])

        expect:
        configurator.topColour == Configurator.DEFAULT_TOP_COLOUR
    }

    def "Should be able to convert colours with different prefix on hex string"() {
        given:
        Configurator configurator = new Configurator(['--top-colour', 'f00b1e', '--bottom-colour', '0xf00b1e'] as String[])

        expect:
        configurator.topColour == 0xf00b1e
        configurator.bottomColour == 0xf00b1e
    }

    def "Should be able to specify top colour threshold"() {
        given:
        Configurator configurator = new Configurator(['--top-threshold', '50'] as String[])

        expect:
        configurator.topThreshold == 50.0
    }

    def "Default hot threshold should be automatically calculated sentinel value"() {
        given:
        Configurator configurator = new Configurator(['foo'] as String[])

        expect:
        configurator.topThreshold == Configurator.AUTO_THRESHOLD
    }

    def "Should be able to specify bottom colour threshold"() {
        given:
        Configurator configurator = new Configurator(['--bottom-threshold', '50'] as String[])

        expect:
        configurator.bottomThreshold == 50.0
    }

    def "Default cold threshold should be automatically calculated sentinel value"() {
        given:
        Configurator configurator = new Configurator(['foo'] as String[])

        expect:
        configurator.bottomThreshold == Configurator.AUTO_THRESHOLD
    }

    def "Should be possible to explicitly specify auto thresholds"() {
        given:
        Configurator configurator = new Configurator(['--top-threshold', 'auto', '--bottom-threshold', 'auto', 'foo'] as String[])

        expect:
        configurator.topThreshold == Configurator.AUTO_THRESHOLD
        configurator.bottomThreshold == Configurator.AUTO_THRESHOLD
    }

    def "Should be able to specify the partition size"() {
        given:
        Configurator configurator = new Configurator(['--partition-size', '50'] as String[])

        expect:
        configurator.partitionSize == 50
    }

    def "Default partition size threshold should be provided if not specified"() {
        given:
        Configurator configurator = new Configurator(['foo'] as String[])

        expect:
        configurator.partitionSize == Configurator.DEFAULT_PARTITION_SIZE
    }

    def "Should be able to specify the tree-map title"() {
        given:
        Configurator configurator = new Configurator(['--title', 'Some Title'] as String[])

        expect:
        configurator.title == 'Some Title'
    }

    def "A default title should be provided if none specified"() {
        given:
        Configurator configurator = new Configurator(['foo'] as String[])

        expect:
        configurator.title == 'Tree-Map Visualisation'
    }

    def "Should be able to specify a path for referenced resources (disabled embedding)"() {
        given:
        Configurator configurator1 = new Configurator(['--resource-path', 'path1'] as String[])
        Configurator configurator2 = new Configurator(['-R', 'path2'] as String[])

        expect:
        !configurator.resourcePath
        configurator1.resourcePath == 'path1'
        configurator2.resourcePath == 'path2'
    }

    def "Should be able to specify a VCS log-file"() {
        given:
        Configurator configurator1 = new Configurator(['--vcs-log', 'file'] as String[])

        expect:
        !configurator.vcsLogFile
        configurator1.vcsLogFile == 'file'
    }

    def "Should be able to specify a set of path prefixes to ignore"() {
        given:
        Configurator configurator1 = new Configurator(['--ignore-prefix', 'alpha', '-n', 'beta'] as String[])

        expect:
        !configurator.ignorePrefixes
        configurator1.ignorePrefixes == ['alpha', 'beta']
    }

    def "Should be able to specify a VCS list file"() {
        given:
        Configurator configurator1 = new Configurator(['--vcs-list', 'file'] as String[])

        expect:
        !configurator.vcsListFile
        configurator1.vcsListFile == 'file'
    }

    def "Should be able to specify joining CSV files"() {
        Configurator configurator1 = new Configurator(['--join-csv-files', 'LEFT=RIGHT'] as String[])
        Configurator configurator2 = new Configurator(['-j', 'Left=Right'] as String[])

        expect:
        !configurator.csvJoinFields
        configurator1.csvJoinFields == ['LEFT', 'RIGHT']
        configurator2.csvJoinFields == ['Left', 'Right']
    }

    def "Should be able to specify join type"() {
        Configurator configurator1 = new Configurator(['--join-type', 'inner'] as String[])
        Configurator configurator2 = new Configurator(['--join-type', 'outer'] as String[])

        expect:
        configurator.joinType == Configurator.JOIN_TYPE_INNER
        configurator1.joinType == Configurator.JOIN_TYPE_INNER
        configurator2.joinType == Configurator.JOIN_TYPE_OUTER
    }

    def "Should be able to specify the tab size"() {
        Configurator configurator1 = new Configurator(['--tab-size', '8'] as String[])

        expect:
        configurator.tabSize == Configurator.DEFAULT_TAB_SIZE
        configurator1.tabSize == 8
    }

    def "Should be able to turn on author stats output"() {
        given:
        Configurator configurator1 = new Configurator(['--author-stats'] as String[])

        expect:
        !configurator.authorStats
        configurator1.authorStats
    }

    def "Should be able to turn on author paths output"() {
        given:
        Configurator configurator1 = new Configurator(['--author-paths'] as String[])

        expect:
        !configurator.authorPaths
        configurator1.authorPaths
    }

    def "Should be able to specify a VCS type"() {
        given:
        Configurator configurator1 = new Configurator(['--vcs-type', 'git'] as String[])
        Configurator configurator2 = new Configurator(['-v', 'svn'] as String[])
        expect:
        !configurator.vcsType
        configurator1.vcsType == 'git'
        configurator2.vcsType == 'svn'
    }

    def "Should be able to specify a VCS diff file"() {
        given:
        Configurator configurator1 = new Configurator(['--vcs-diff', 'file'] as String[])

        expect:
        !configurator.vcsDiffFile
        configurator1.vcsDiffFile == 'file'
    }
}