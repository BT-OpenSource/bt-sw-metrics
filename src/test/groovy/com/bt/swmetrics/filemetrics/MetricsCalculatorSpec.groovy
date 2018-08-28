package com.bt.swmetrics.filemetrics

import com.bt.swmetrics.filemetrics.MetricsCalculator
import spock.lang.Specification
import static com.bt.swmetrics.filemetrics.IndentCalculator.EMPTY_LINE

class MetricsCalculatorSpec extends Specification {
    MetricsCalculator calculator
    static final DATA_LIST = [0, 1, 1, 2, 1, 2, 3, 1, 0]

    def "Should be able to get the min, max and total"() {
        when:
        calculator = new MetricsCalculator(DATA_LIST)

        then:
        calculator.min == 0
        calculator.max == 3
        calculator.total == 11
    }

    def "Should be able to calculate the mean indent"() {
        when:
        calculator = new MetricsCalculator(DATA_LIST)

        then:
        Math.abs(calculator.mean - 1.2222222) < 0.0001
    }

    def "Should be able to calculate the mode indent"() {
        when:
        calculator = new MetricsCalculator(DATA_LIST)

        then:
        calculator.mode == 1
    }

    def "Should be able to calculate the indent standard deviation"() {
        when:
        calculator = new MetricsCalculator(DATA_LIST)

        then:
        Math.abs(calculator.standardDeviation - 0.91624) < 0.0001
    }

    def "Empty files should give meaningful data"() {
        given:
        def data = [EMPTY_LINE, EMPTY_LINE]

        when:
        calculator = new MetricsCalculator(data)

        then:
        with(calculator) {
            min == 0
            max == 0
            mean == 0.0
            standardDeviation == 0.0
            mode == 0
        }
    }

    def "Zero-length files should give meaningful data"() {
        given:
        def data = []

        when:
        calculator = new MetricsCalculator(data)

        then:
        with(calculator) {
            min == 0
            max == 0
            mean == 0.0
            standardDeviation == 0.0
            mode == 0
        }
    }

    def "Should be able to get a frequency histogram"() {
        when:
        calculator = new MetricsCalculator(DATA_LIST)

        then:
        calculator.histogram == [0: 2, 1: 4, 2: 2, 3: 1]
    }

    def "Should be able to get percentiles at or above levels"() {
        when:
        calculator = new MetricsCalculator(DATA_LIST)

        then:
        calculator.percentileAtOrAbove == [0: 1, 1: 7/9, 2: 3/9, 3: 1/9]
    }

    def "Should be able to get percentiles at or above levels, when there is no level zero"() {
        when:
        calculator = new MetricsCalculator([1, 1, 1])

        then:
        calculator.percentileAtOrAbove == [0: 1, 1: 1]
    }

    def "Span details of constant range should be number of elements"() {
        given:
        def data = [1, 1, 1, 1, 1, 1, 1]

        when:
        calculator = new MetricsCalculator(data)

        then:
        calculator.spanDetails == [1: [7]]
    }

    def "Span of range includes everything at greater depth"() {
        given:
        def data = [1, 1, 2, 3, 2, 1, 1]

        when:
        calculator = new MetricsCalculator(data)

        then:
        calculator.spanDetails == [1: [7], 2: [3], 3: [1]]
    }

    def "Mutiple spans of disjoint ranges are calculated correctly"() {
        given:
        def data = [0, 1, 1, 2, 0, 1, 2, 3, 1]

        when:
        calculator = new MetricsCalculator(data)

        then:
        calculator.spanDetails == [1: [3, 4], 2: [1, 2], 3: [1]]
    }

    def "Spans should ignore blank lines"() {
        given:
        def data = [EMPTY_LINE, 0, EMPTY_LINE, 1, EMPTY_LINE, 1, EMPTY_LINE, 2, EMPTY_LINE, 0, EMPTY_LINE, 1, EMPTY_LINE, 2, EMPTY_LINE, 3, EMPTY_LINE, 1, EMPTY_LINE]

        when:
        calculator = new MetricsCalculator(data)

        then:
        calculator.spanDetails == [1: [3, 4], 2: [1, 2], 3: [1]]
    }

    def "Span count, mean, min, max and SD are calculated"() {
        given:
        def data = [0, 1, 1, 2, 0, 1, 2, 3, 1]

        when:
        calculator = new MetricsCalculator(data)

        then:
        with(calculator.spanMetrics[1]) {
            count == 2
            min == 3
            max == 4
            mean == 3.5
            stdDev == 0.5
        }
    }

    def "Should be possible to limit the span level calculations at construction time"() {
        given:
        def data = [0, 1, 1, 2, 0, 1, 2, 3, 1]

        when:
        calculator = new MetricsCalculator(data, 2)

        then:
        calculator.spanMetrics.size() == 2
    }

    def "Should be possible to get the mode span metrics, even if beyond the maxSpanMetricsLevel"() {
        given:
        def data = [0, 1, 1, 2, 3, 3, 0, 1, 2, 3, 3, 3, 3, 3, 1]

        when:
        calculator = new MetricsCalculator(data, 2)

        then:
        with (calculator.probableFunctionSpanMetrics) {
            count == 2
            min == 2
            max == 5
        }
    }

    def "Probable Function level should be most frequent level above zero, if any"() {
        given:
        def data = [0, 0, 0, 0, 0, 1, 1, 2, 0, 1]

        when:
        calculator = new MetricsCalculator(data, 2)

        then:
        calculator.probableFunctionLevel == 1
    }

    def "Probable Function level should be zero, if there is no alternative"() {
        given:
        def data = [0, 0, 0, 0, 0, 0]

        when:
        calculator = new MetricsCalculator(data, 2)

        then:
        calculator.probableFunctionLevel == 0
    }

    def "Probable function span should exclude zero-level if higher levels are present"() {
        given:
        def data = [0, 0, 0, 0, 0, 1, 1, 2, 0, 1]

        when:
        calculator = new MetricsCalculator(data, 2)

        then:
        with (calculator.probableFunctionSpanMetrics) {
            count == 2
            min == 1
            max == 3
        }
    }

    def "Function span should be level zero if no higher levels are present"() {
        given:
        def data = [0, 0, 0, 0, 0]

        when:
        calculator = new MetricsCalculator(data, 2)

        then:
        with (calculator.probableFunctionSpanMetrics) {
            count == 1
            min == 5
            max == 5
        }
    }

    def "Should be able to get total and non-blank line count"() {
        given:
        def data = [0, EMPTY_LINE, 1, 2, EMPTY_LINE]
        when:
        calculator = new MetricsCalculator(data)

        then:
        calculator.lineCount == 5
        calculator.nonBlankLineCount == 3
    }

    def "Blank lines should not affect the mean, stddev and total indent count"() {
        given:
        def data = [0, EMPTY_LINE, 1, 2, EMPTY_LINE]
        when:
        calculator = new MetricsCalculator(data)

        then:
        calculator.total == 3
        calculator.mean == 1
        Math.abs(calculator.standardDeviation - 0.81649) < 0.0001
    }
}