package com.bt.swmetrics.filemetrics

import com.bt.swmetrics.filemetrics.WhitespacePrefixCalculator
import spock.lang.Specification
import spock.lang.Unroll

class WhitespacePrefixCalculatorSpec extends Specification {
    WhitespacePrefixCalculator calculator = new WhitespacePrefixCalculator()

    def "Should give sentinel value for for an empty line"() {
        expect:
        calculator.countWhitespace("") == WhitespacePrefixCalculator.EMPTY_LINE
    }

    def "Should give sentinel value for for a line with only whitespace"() {
        expect:
        calculator.countWhitespace("") == WhitespacePrefixCalculator.EMPTY_LINE
    }

    def "Should give zero for a line with no prefixing space"() {
        expect:
        calculator.countWhitespace("a b c") == 0
    }

    def "Should count spaces before first non-space character"() {
        expect:
        calculator.countWhitespace("  a b c") == 2
    }

    def "Should not include line ending characters"() {
        expect:
        calculator.countWhitespace(" \r\nX") == 1
    }

    def "Tabs should count as 4 spaces by default"() {
        expect:
        calculator.countWhitespace("\tfoo bar") == 4
    }

    @Unroll
    def "Embedded tabs should expand correctly"() {
        expect:
        length == calculator.countWhitespace(input)

        where:
        input           || length
        "\tX"           || 4
        " \tX"          || 4
        "  \tX"         || 4
        "   \tX"        || 4
        "    \tX"       || 8
        " \t \tX"       || 8
    }

    @Unroll
    def "Should be able to specify a different tab size"() {
        given:
        calculator = new WhitespacePrefixCalculator(tabSize: 8)

        expect:
        length == calculator.countWhitespace(input)

        where:
        input           || length
        "\tX"           || 8
        " \tX"          || 8
        "  \tX"         || 8
        "   \tX"        || 8
        "    \tX"       || 8
        " \t \tX"       || 16
    }
}