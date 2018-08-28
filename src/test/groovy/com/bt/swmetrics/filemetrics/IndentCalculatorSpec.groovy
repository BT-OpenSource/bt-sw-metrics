package com.bt.swmetrics.filemetrics

import com.bt.swmetrics.filemetrics.IndentCalculator
import spock.lang.Specification


class IndentCalculatorSpec extends Specification {
    IndentCalculator calculator = new IndentCalculator()

    def "Should be able to calculate relative indent for increasingly indented"() {
        given:
        def lines = [
                'alpha',
                '  beta',
                '      gamma'
        ]

        when:
        def result = calculator.calculateIndents(lines)

        then:
        result == [0, 1, 2]
    }

    def "Should be able to calculate indent with expanded tabs"() {
        given:
        def lines = [
                'alpha',
                '  beta',
                '\tgamma'
        ]

        when:
        def result = calculator.calculateIndents(lines, 8)

        then:
        result == [0, 1, 2]
    }

    def "Matched decreasing indents should decrease relative indent"() {
        given:
        def lines = [
                'alpha',
                '  beta',
                '    gamma',
                '  delta',
                'epsilon'
        ]

        when:
        def result = calculator.calculateIndents(lines)

        then:
        result == [0, 1, 2, 1, 0]
    }

    def "Zero indent always resets relative indent"() {
        given:
        def lines = [
                'alpha',
                '  beta',
                '      gamma',
                'delta',
                '            epsilon',
                'zeta'
        ]

        when:
        def result = calculator.calculateIndents(lines)

        then:
        result == [0, 1, 2, 0, 1, 0]
    }

    def "Decrease of absolute indent below previous level matches nearest lower level"() {
        given:
        def lines = [
                'alpha',
                '    beta',
                '        gamma',
                '        delta',
                ' epsilon',
                'zeta'
        ]

        when:
        def result = calculator.calculateIndents(lines)

        then:
        result == [0, 1, 2, 2, 0, 0]
    }

    def "Unbalanced indents should converge on stability"() {
        given:
        def lines = [
                '     alpha',
                '        beta',
                '   gamma',
                '  delta',
                ' epsilon',
                ' zeta',
                ' eta',
                ' theta',
                '  iota'
        ]

        when:
        def result = calculator.calculateIndents(lines)

        then:
        result == [1, 2, 0, 1, 0, 1, 1, 1, 2]
    }

    def "Blank lines should have sentinel values"() {
        given:
        def lines = [
                'alpha',
                '  beta',
                '    gamma',
                '    ',
                '  delta',
                'epsilon'
        ]

        when:
        def result = calculator.calculateIndents(lines)

        then:
        result == [0, 1, 2, IndentCalculator.EMPTY_LINE, 1, 0]
    }
}