package com.bt.swmetrics.filemetrics


class ListStatsCalculator {
    private final BigDecimal min
    private final BigDecimal max
    private final BigDecimal total
    private final BigDecimal mean
    private final BigDecimal standardDeviation

    ListStatsCalculator(List<Number> numbers) {
        BigDecimal endOfRange = Double.MAX_VALUE as BigDecimal
        def minMaxTotal = numbers.inject(new Tuple(endOfRange, -endOfRange, 0.0)) { tuple, next ->
            BigDecimal currentMin, currentMax, currentTotal
            (currentMin, currentMax, currentTotal) = tuple
            new Tuple(minOf(currentMin, next as BigDecimal), maxOf(currentMax, next as BigDecimal), currentTotal + next)
        }
        (min, max, total) = minMaxTotal
        this.mean = total / (numbers.size() ?: 1)
        this.standardDeviation = calculateStandardDeviation(numbers, mean)
    }

    private static minOf(BigDecimal a, BigDecimal b) {
        a < b ? a : b
    }

    private static maxOf(BigDecimal a, b) {
        a > b ? a : b
    }

    private static calculateStandardDeviation(List<Number> data, BigDecimal mean) {
        if (data.size() < 1) {
            return 0.0
        }

        def sumMeanDifferenceSquared = data.inject(0.0) { total, next -> total + (next - mean) * (next - mean) }
        def variance = sumMeanDifferenceSquared / data.size()
        Math.sqrt(variance) as BigDecimal
    }

    BigDecimal getMax() {
        this.max
    }

    BigDecimal getMin() {
        this.min
    }

    BigDecimal getTotal() {
        this.total
    }

    BigDecimal getMean() {
        this.mean
    }

    BigDecimal getStandardDeviation() {
        this.standardDeviation
    }
}
