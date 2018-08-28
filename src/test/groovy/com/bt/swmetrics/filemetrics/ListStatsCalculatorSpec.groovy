package com.bt.swmetrics.filemetrics

import com.bt.swmetrics.filemetrics.ListStatsCalculator
import spock.lang.Specification

class ListStatsCalculatorSpec extends Specification {
    static final DATA_LIST = [0.5, 1, 1.0, 2, 1, 2, 3.0f, 1, 0.1]

    ListStatsCalculator calculator = new ListStatsCalculator(DATA_LIST)


    def "Should be able to get the min, max and total"() {
        expect:
        calculator.min == 0.1
        calculator.max == 3.0
        calculator.total == 11.6
    }

    def "Should be able to calculate the mean value"() {
        expect:
        Math.abs(calculator.mean - 1.288888) < 0.0001
    }

    def "Should be able to calculate the  standard deviation"() {
        expect:
        Math.abs(calculator.standardDeviation - 0.83725) < 0.0001
    }
}