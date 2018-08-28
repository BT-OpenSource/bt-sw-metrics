package com.bt.swmetrics.vcs.git

import com.bt.swmetrics.vcs.AuthorStats
import com.bt.swmetrics.vcs.Commit
import spock.lang.Specification

import java.time.Instant
import java.time.format.DateTimeParseException


class GitLogParserSpec extends Specification {
    GitLogParser parser = new GitLogParser()

    def "Empty text should yield an empty list"() {
        expect:
        parser.parseToTupleList('') == []
    }

    def "Blank line should be ignored"() {
        expect:
        parser.parseToTupleList('''
  \t
  
\t

''') == []
    }

    def "Should be able to parse text to give a list of path/commit tuples"() {
        given:
        def logtext = '''date 2017-08-01T10:06:04+01:00
author neil

M\tsrc/main/groovy/com/bt/swmetrics/svn/SvnRunner.groovy
M\tsrc/test/groovy/com/bt/swmetrics/svn/AuthorStatsSpec.groovy
M\tsrc/test/groovy/com/bt/swmetrics/svn/SvnRunnerSpec.groovy
date 2017-08-02T10:48:12+01:00
author fred

M\tsrc/main/groovy/com/bt/swmetrics/svn/SvnRunner.groovy
M\tsrc/test/groovy/com/bt/swmetrics/svn/AuthorStatsSpec.groovy

date 2017-08-03T11:54:15+01:00
author neil

M\tsrc/main/groovy/com/bt/swmetrics/svn/SvnRunner.groovy
M\tsrc/test/groovy/com/bt/swmetrics/svn/SvnRunnerSpec.groovy

'''
        when:
        def result = parser.parseToTupleList(logtext)

        then:
        result.size() == 7

        result[0][0] == 'src/main/groovy/com/bt/swmetrics/svn/SvnRunner.groovy'
        result[0][1] == new Commit(
                revision:  1,
                author: 'neil',
                timestamp: Instant.parse('2017-08-01T09:06:04Z'),
                action: 'M'
        )

        result[6][0] == 'src/test/groovy/com/bt/swmetrics/svn/SvnRunnerSpec.groovy'
        result[6][1] == new Commit(
                revision:  3,
                author: 'neil',
                timestamp: Instant.parse('2017-08-03T10:54:15Z'),
                action: 'M'
        )
    }

    /*

     */
    def "Should parse a rename as an add and delete"() {
        given:
        def logtext = '''date 2017-05-18T15:09:11+01:00
author neil

R063\tsrc/test/groovy/com/bt/swmetrics/VisualisationBuilderSpec.groovy\tsrc/test/groovy/com/bt/swmetrics/TreeMapVisualisationGeneratorSpec.groovy

'''
        when:
        def result = parser.parseToTupleList(logtext)

        then:
        result.size() == 2

        result[0][0] == 'src/test/groovy/com/bt/swmetrics/VisualisationBuilderSpec.groovy'
        result[0][1] == new Commit(
                revision:  1,
                author: 'neil',
                timestamp: Instant.parse('2017-05-18T14:09:11Z'),
                action: 'A'
        )
        // Next are "copy-from-path" and "copy-from-revision" in svn terms
        result[0][2] == 'src/test/groovy/com/bt/swmetrics/TreeMapVisualisationGeneratorSpec.groovy'
        result[0][3] == 0   // One less than current revision

        result[1][0] == 'src/test/groovy/com/bt/swmetrics/TreeMapVisualisationGeneratorSpec.groovy'
        result[1][1] == new Commit(
                revision:  1,
                author: 'neil',
                timestamp: Instant.parse('2017-05-18T14:09:11Z'),
                action: 'D'
        )
    }

    def "Should parse a copy as an add with a source"() {
        given:
        def logtext = '''date 2017-05-18T15:09:11+01:00
author neil

C100\tsrc/test/groovy/com/bt/swmetrics/VisualisationBuilderSpec.groovy\tsrc/test/groovy/com/bt/swmetrics/TreeMapVisualisationGeneratorSpec.groovy

'''
        when:
        def result = parser.parseToTupleList(logtext)

        then:
        result.size() == 1

        result[0][0] == 'src/test/groovy/com/bt/swmetrics/VisualisationBuilderSpec.groovy'
        result[0][1] == new Commit(
                revision: 1,
                author: 'neil',
                timestamp: Instant.parse('2017-05-18T14:09:11Z'),
                action: 'A'
        )
        // Next are "copy-from-path" and "copy-from-revision" in svn terms
        result[0][2] == 'src/test/groovy/com/bt/swmetrics/TreeMapVisualisationGeneratorSpec.groovy'
        result[0][3] == 0   // One less than current revision
    }

    def "Should be able to handle commits in youngest-to-oldest order"() {
        given:
        def logtext = '''date 2017-08-03T11:54:15+01:00
author neil

M\tsrc/main/groovy/com/bt/swmetrics/svn/SvnRunner.groovy
M\tsrc/test/groovy/com/bt/swmetrics/svn/SvnRunnerSpec.groovy
date 2017-08-02T10:48:12+01:00
author fred

M\tsrc/main/groovy/com/bt/swmetrics/svn/SvnRunner.groovy
M\tsrc/test/groovy/com/bt/swmetrics/svn/AuthorStatsSpec.groovy
date 2017-08-01T10:06:04+01:00
author neil

M\tsrc/test/groovy/com/bt/swmetrics/svn/AuthorStatsSpec.groovy
M\tsrc/main/groovy/com/bt/swmetrics/svn/SvnRunner.groovy
M\tsrc/test/groovy/com/bt/swmetrics/svn/SvnRunnerSpec.groovy
'''
        when:
        def result = parser.parseToTupleList(logtext)

        then:
        result.size() == 7

        result[0][0] == 'src/main/groovy/com/bt/swmetrics/svn/SvnRunner.groovy'
        result[0][1] == new Commit(
                revision:  3,
                author: 'neil',
                timestamp: Instant.parse('2017-08-03T10:54:15Z'),
                action: 'M'
        )

        result[6][0] == 'src/test/groovy/com/bt/swmetrics/svn/SvnRunnerSpec.groovy'
        result[6][1] == new Commit(
                revision:  1,
                author: 'neil',
                timestamp: Instant.parse('2017-08-03T09:06:04Z'),
                action: 'M'
        )
    }

    def "Should be able to parse file to map of author activities"() {
        given:
        def file = new File('src/test/resources/git.log')

        when:
        Map<String,AuthorStats> map = parser.parseToAuthorStatsMap(file)

        then:
        map.size() == 2
        map['neil'].commitTimestamps.size() == 2
        map['neil'].tenure == 3
        map['neil'].pathCommits == [
                'src/test/groovy/com/bt/swmetrics/svn/AuthorStatsSpec.groovy': 1,
                'src/main/groovy/com/bt/swmetrics/svn/SvnRunner.groovy': 2,
                'src/main/groovy/com/bt/swmetrics/svn/SvnRunner.groovy.orig': 1,
                'src/test/groovy/com/bt/swmetrics/svn/SvnRunnerSpec.groovy': 2
        ]
        map['fred'].commitTimestamps.size() == 1
        map['fred'].tenure == 1
        map['fred'].pathCommits == [
                'src/test/groovy/com/bt/swmetrics/svn/AuthorStatsSpec.groovy': 1,
                'src/main/groovy/com/bt/swmetrics/svn/SvnRunner.groovy': 1,
        ]
    }

    def "Should be able to parse author line in default format"() {
        given:
        def logtext = '''date 2017-05-18T15:09:11+01:00
Author: Sam Body <sam.body@somwhere.com>

M\tsrc/test/groovy/com/bt/swmetrics/VisualisationBuilderSpec.groovy

'''
        when:
        def result = parser.parseToTupleList(logtext)

        then:
        result.size() == 1

        result[0][1] == new Commit(
                revision: 1,
                author: 'Sam Body',
                timestamp: Instant.parse('2017-05-18T14:09:11Z'),
                action: 'M'
        )
    }

    def "Should be able to parse date line in default format"() {
        given:
        def logtext = '''Date:   Thu May 18 15:09:11 2017 +0100
author Sam Body

M\tsrc/test/groovy/com/bt/swmetrics/VisualisationBuilderSpec.groovy

'''
        when:
        def result = parser.parseToTupleList(logtext)

        then:
        result.size() == 1

        result[0][1] == new Commit(
                revision: 1,
                author: 'Sam Body',
                timestamp: Instant.parse('2017-05-18T14:09:11Z'),
                action: 'M'
        )
    }

    def "Should throw an exception for unparseable dates"() {
        given:
        def logtext = '''commit fc14dde7655782214913c630612063c0f9cbfe8e (HEAD -> master, origin/master)
Author: Sam Body <same.body@somwhere.com>
Date:   2017-08-08 08:52:38 +0100

    Renamed classes for consistency

M       src/main/groovy/com/bt/swmetrics/Main.groovy

'''
        when:
        def result = parser.parseToTupleList(logtext)

        then:
        thrown(DateTimeParseException)
    }
}