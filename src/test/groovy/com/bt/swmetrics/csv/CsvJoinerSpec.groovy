package com.bt.swmetrics.csv

import spock.lang.Specification


class CsvJoinerSpec extends Specification {
    CsvJoiner joiner

    static final def TEST_DATA_LEFT = '''PathL,L1,L2,L3,L4
a/b,A,1,1.1,1.2
a/b/c,B,2,2.1,2.2
a/b/d/e,C,3,3.1,3.2
a/b/d/f,D,4,4.1,4.2'''

    static final def TEST_DATA_RIGHT = '''PathR,R1,R2,R3,R4
a/b,W,5,5.1,5.2
a/b/c,X,6,6.1,6.2
a/b/d/e,Y,7,7.1,7.2
a/b/d/f,Z,8,8.1,8.2'''

    def "Should throw an exception if left join field does not exist"() {
        given:
        joiner = new CsvJoiner(TEST_DATA_LEFT, TEST_DATA_RIGHT)

        when:
        joiner.leftField = 'Not in Left'

        then:
        IllegalArgumentException e = thrown()
        e.message.contains('Not in Left')
    }

    def "Should throw an exception if right join field does not exist"() {
        given:
        joiner = new CsvJoiner(TEST_DATA_LEFT, TEST_DATA_RIGHT)

        when:
        joiner.rightField = 'Not in Right'

        then:
        IllegalArgumentException e = thrown()
        e.message.contains('Not in Right')
    }

    def "Should be able to obtain joined headers"() {
        given:
        joiner = new CsvJoiner('A,B,C', 'D,E,F')
        joiner.leftField = 'A'
        joiner.rightField = 'D'

        expect:
        joiner.headers == ['A', 'B', 'C', 'E', 'F']
    }

    def "Data from columns where join fields match should be merged"() {
        given:
        joiner = new CsvJoiner(TEST_DATA_LEFT, TEST_DATA_RIGHT)
        joiner.leftField = 'PathL'
        joiner.rightField = 'PathR'

        expect:
        joiner.outerJoinResult == [
                ['a/b', 'A' ,'1', '1.1', '1.2', 'W' ,'5', '5.1', '5.2'],
                ['a/b/c', 'B' ,'2', '2.1', '2.2', 'X' ,'6', '6.1', '6.2'],
                ['a/b/d/e', 'C' ,'3', '3.1', '3.2', 'Y' ,'7', '7.1', '7.2'],
                ['a/b/d/f', 'D' ,'4', '4.1', '4.2', 'Z' ,'8', '8.1', '8.2'],
        ]
    }

    def "Records where there is no match should be padded with empty values for outer join"() {
        given:
        joiner = new CsvJoiner(TEST_DATA_LEFT.replaceFirst('a/b', 'A/B'), TEST_DATA_RIGHT)
        joiner.leftField = 'PathL'
        joiner.rightField = 'PathR'

        expect:
        joiner.outerJoinResult == [
                ['A/B', 'A' ,'1', '1.1', '1.2', '' ,'', '', ''],
                ['a/b/c', 'B' ,'2', '2.1', '2.2', 'X' ,'6', '6.1', '6.2'],
                ['a/b/d/e', 'C' ,'3', '3.1', '3.2', 'Y' ,'7', '7.1', '7.2'],
                ['a/b/d/f', 'D' ,'4', '4.1', '4.2', 'Z' ,'8', '8.1', '8.2'],
        ]
    }

    def "Records where there is no match should be eliminated for join"() {
        given:
        joiner = new CsvJoiner(TEST_DATA_LEFT.replaceFirst('a/b', 'A/B'), TEST_DATA_RIGHT)
        joiner.leftField = 'PathL'
        joiner.rightField = 'PathR'

        expect:
        joiner.innerJoinResult == [
                ['a/b/c', 'B' ,'2', '2.1', '2.2', 'X' ,'6', '6.1', '6.2'],
                ['a/b/d/e', 'C' ,'3', '3.1', '3.2', 'Y' ,'7', '7.1', '7.2'],
                ['a/b/d/f', 'D' ,'4', '4.1', '4.2', 'Z' ,'8', '8.1', '8.2'],
        ]
    }

    def "Should be possible to ignore prefixes and match fields"() {
        given:
        joiner = new CsvJoiner(TEST_DATA_LEFT.replaceAll(/a\//, 'A/'), TEST_DATA_RIGHT)
        joiner.leftField = 'PathL'
        joiner.rightField = 'PathR'

        when:
        joiner.ignorePrefixes = ['A/', 'a/']

        then:
        joiner.outerJoinResult == [
                ['A/b', 'A' ,'1', '1.1', '1.2', 'W' ,'5', '5.1', '5.2'],
                ['A/b/c', 'B' ,'2', '2.1', '2.2', 'X' ,'6', '6.1', '6.2'],
                ['A/b/d/e', 'C' ,'3', '3.1', '3.2', 'Y' ,'7', '7.1', '7.2'],
                ['A/b/d/f', 'D' ,'4', '4.1', '4.2', 'Z' ,'8', '8.1', '8.2'],
        ]
    }

    def "Clashing header names should be renamed with a suffix"() {
        given:
        joiner = new CsvJoiner('A,B,C', 'A,B,C')
        joiner.leftField = 'A'
        joiner.rightField = 'A'

        expect:
        joiner.headers == ['A', 'B', 'C', 'B (other)', 'C (other)']
    }

}