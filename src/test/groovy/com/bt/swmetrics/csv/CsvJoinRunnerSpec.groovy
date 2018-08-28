package com.bt.swmetrics.csv

import com.bt.swmetrics.Configurator
import spock.lang.Specification


class CsvJoinRunnerSpec extends Specification {
    CsvJoinRunner runner

    def "Should throw an exception unless two files are supplied"() {
        given:
        def baos = new ByteArrayOutputStream()
        Configurator configurator = Stub()
        configurator.joinType >> Configurator.JOIN_TYPE_INNER
        configurator.csvJoinFields >> ['A', 'B']
        configurator.arguments >> ['Just One']

        runner = new CsvJoinRunner(stream: new PrintStream(baos), configurator: configurator)

        when:
        runner.joinFiles()

        then:
        IllegalArgumentException e = thrown()
        e.message =~ /exactly two/
    }

    def "Should be able to create CSV output with inner join on all matching lines"() {
        given:
        def baos = new ByteArrayOutputStream()
        Configurator configurator = Stub()
        configurator.joinType >> Configurator.JOIN_TYPE_INNER
        configurator.csvJoinFields >> ['PathL', 'PathR']
        def leftFile = 'src/test/resources/test-left.csv'
        def rightFile ='src/test/resources/test-right-1.csv'
        configurator.arguments >> [leftFile, rightFile]
        runner = new CsvJoinRunner(stream: new PrintStream(baos), configurator: configurator)


        when:
        runner.joinFiles()
        def outputLines = baos.toString().split("\n").collect { it.trim() }

        then:
        outputLines == [
                'PathL,L1,L2,L3,L4,R1,R2,R3,R4',
                'a/b,A,1,1.1,1.2,W,5,5.1,5.2',
                'a/b/c,B,2,2.1,2.2,X,6,6.1,6.2',
                'a/b/d/e,C,3,3.1,3.2,Y,7,7.1,7.2',
                'a/b/d/f,D,4,4.1,4.2,Z,8,8.1,8.2'
        ]
    }

    def "Should be able to create CSV output with inner join and ignored prefixes"() {
        given:
        def baos = new ByteArrayOutputStream()
        Configurator configurator = Stub()
        configurator.joinType >> Configurator.JOIN_TYPE_INNER
        configurator.csvJoinFields >> ['PathL', 'PathR']
        configurator.ignorePrefixes >> ['a/', 'X/']
        def leftFile = 'src/test/resources/test-left.csv'
        def rightFile ='src/test/resources/test-right-2.csv'
        configurator.arguments >> [leftFile, rightFile]

        runner = new CsvJoinRunner(stream: new PrintStream(baos), configurator: configurator)

        when:
        runner.joinFiles()
        def outputLines = baos.toString().split("\n").collect { it.trim() }

        then:
        outputLines == [
                'PathL,L1,L2,L3,L4,R1,R2,R3,R4',
                'a/b,A,1,1.1,1.2,W,5,5.1,5.2',
                'a/b/c,B,2,2.1,2.2,X,6,6.1,6.2',
                'a/b/d/e,C,3,3.1,3.2,Y,7,7.1,7.2',
        ]
    }

    def "Should be able to create CSV output with outer join and ignored prefixes"() {
        given:
        def baos = new ByteArrayOutputStream()
        Configurator configurator = Stub()
        configurator.joinType >> Configurator.JOIN_TYPE_OUTER
        configurator.csvJoinFields >> ['PathL', 'PathR']
        configurator.ignorePrefixes >> ['a/', 'X/']
        def leftFile = 'src/test/resources/test-left.csv'
        def rightFile ='src/test/resources/test-right-2.csv'
        configurator.arguments >> [leftFile, rightFile]
        runner = new CsvJoinRunner(stream: new PrintStream(baos), configurator: configurator)

        when:
        runner.joinFiles()
        def outputLines = baos.toString().split("\n").collect { it.trim() }

        then:
        outputLines == [
                'PathL,L1,L2,L3,L4,R1,R2,R3,R4',
                'a/b,A,1,1.1,1.2,W,5,5.1,5.2',
                'a/b/c,B,2,2.1,2.2,X,6,6.1,6.2',
                'a/b/d/e,C,3,3.1,3.2,Y,7,7.1,7.2',
                'a/b/d/f,D,4,4.1,4.2,,,,'
        ]
    }
}