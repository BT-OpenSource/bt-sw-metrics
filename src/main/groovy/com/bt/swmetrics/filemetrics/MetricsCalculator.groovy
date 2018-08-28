package com.bt.swmetrics.filemetrics

class MetricsCalculator {
    final List<Integer> data
    private final BigDecimal mean
    private final Integer total
    private final BigDecimal standardDeviation
    private final Integer max
    private final Integer min
    private final int maxSpanMetricsLevel
    private Map<Integer,Integer> histogram

    class SpanMetrics {
        Integer count
        Integer min
        Integer max
        BigDecimal mean
        BigDecimal stdDev
    }

    MetricsCalculator(List<Integer> baseData, int maxSpanMetricsLevel = 3) {
        this.data = baseData
        List<Integer> nonBlankIndents = data.grep { it != IndentCalculator.EMPTY_LINE }

        ListStatsCalculator listStatsCalculator = new ListStatsCalculator(nonBlankIndents)
        this.min = listStatsCalculator.min as Integer
        this.max = listStatsCalculator.max as Integer
        this.total = listStatsCalculator.total as Integer
        this.mean = listStatsCalculator.mean
        this.standardDeviation = listStatsCalculator.standardDeviation

        this.histogram = nonBlankIndents.groupBy { it }.collectEntries { Integer k, List<Integer> v -> [(k): v.size()]}
        this.maxSpanMetricsLevel = maxSpanMetricsLevel
    }

    Integer getMax() {
        this.max
    }

    Integer getMin() {
        this.min
    }

    Integer getTotal() {
        this.total
    }

    BigDecimal getMean() {
        this.mean
    }

    int getLineCount() {
        data.size()
    }

    int getNonBlankLineCount() {
        data.inject(0) { total, next -> total + (next == IndentCalculator.EMPTY_LINE ? 0 : 1) }
    }

    BigDecimal getStandardDeviation() {
        this.standardDeviation
    }

    Map<Integer,Integer> getHistogram() {
        this.histogram
    }

    Integer getMode() {
        histogram ? this.histogram.max { a, b -> a.value <=> b.value }.key : 0
    }

    Map<Integer,List<Integer>> getSpanDetails() {
        def results = [:]

        def nonBlank = data.grep { it != IndentCalculator.EMPTY_LINE }
        (1 .. Math.min(max, maxSpanMetricsLevel)).each { int level  ->
            results[level] = findSpanForLevel(level, nonBlank)
        }
        if (probableFunctionLevel > maxSpanMetricsLevel || probableFunctionLevel < 1) {
            results[probableFunctionLevel] = findSpanForLevel(probableFunctionLevel, nonBlank)
        }
        results
    }

    private static List<Integer> findSpanForLevel(int level, List<Integer> indentList) {
        List<Integer> spanList = []
        int currentCount = 0

        indentList.each { indent ->
            if (indent >= level) {
                currentCount++
            } else {
                if (currentCount) {
                    spanList << currentCount
                }
                currentCount = 0
            }
        }

        if (currentCount) {
            spanList << currentCount
        }

        spanList
    }

    Map<Integer,SpanMetrics> getSpanMetrics() {
        def details = spanDetails
        details.collectEntries { Integer level, List<Integer> spanData ->
            ListStatsCalculator calc = new ListStatsCalculator(spanData)
            def stats = new SpanMetrics(
                    count: spanData.size(),
                    min: calc.min,
                    max: calc.max,
                    mean: calc.mean,
                    stdDev: calc.standardDeviation
            )
            [(level): stats]
        } as Map<Integer,SpanMetrics>
    }

    SpanMetrics getProbableFunctionSpanMetrics() {
        spanMetrics[probableFunctionLevel]
    }

    int getProbableFunctionLevel() {
        if (histogram) {
            def modeNonZeroLevel = this.histogram.findAll { it.key != 0 }.max { a, b -> a.value <=> b.value }?.key
            return modeNonZeroLevel ?: 0
        } else {
            return 0
        }
    }

    Map<Integer, BigDecimal> getPercentileAtOrAbove() {
        if (!histogram) {
            return [:]
        }
        int currentTotal = 0
        int nonBlank = nonBlankLineCount
        (histogram.keySet().max() .. 0).collectEntries {
            currentTotal += (histogram[it] ?: 0)
            [(it): currentTotal / nonBlank]
        }
    }
}
