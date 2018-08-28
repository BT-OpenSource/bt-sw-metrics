package com.bt.swmetrics.vcs

import com.bt.swmetrics.Configurable
import com.bt.swmetrics.filemetrics.MetricsCalculator

import java.math.RoundingMode

class DiffStatsGenerator implements Configurable {
    PrintStream stream

    DiffParser getDiffParser() {
        VcsParserFactory.getDiffParserInstance(configurator)
    }

    def generateCsvReport() {
        stream.println "Path,Chunks,Lines Added,Lines Removed,Indent Change,Mean Before,Mean After,Std Dev Before,Std Dev After,Max Before,Max After"
        diffParser.lines = new File(configurator.vcsDiffFile).readLines()
        diffParser.chunksByPath.each { path, diffChunkList ->
            outputCsvDataForPath(path, diffChunkList)
        }
    }

    def outputCsvDataForPath(String path, DiffChunkList chunks) {
        MetricsCalculator oldMetrics = chunks.oldMetricsCalculator
        MetricsCalculator newMetrics = chunks.newMetricsCalculator
        stream.println "$path,${chunks.size()},$chunks.totalAddedCount,$chunks.totalRemovedCount," +
                "${newMetrics.total - oldMetrics.total}," +
                "${rounded(oldMetrics.mean)},${rounded(newMetrics.mean)}," +
                "${rounded(oldMetrics.standardDeviation)},${rounded(newMetrics.standardDeviation)}," +
                "$oldMetrics.max,$newMetrics.max"
    }

    private static BigDecimal rounded(BigDecimal input) {
        input.setScale(3, RoundingMode.HALF_EVEN)
    }

    def generateTextReport() {
        diffParser.lines = new File(configurator.vcsDiffFile).readLines()
        diffParser.chunksByPath.each { path, diffChunkList ->
            outputTextDataForPath(path, diffChunkList)
        }
    }

    def outputTextDataForPath(String path, DiffChunkList chunks) {
        MetricsCalculator oldMetrics = chunks.oldMetricsCalculator
        MetricsCalculator newMetrics = chunks.newMetricsCalculator
        stream.println """=== $path
    ${pluralise(chunks.size(), 'chunk')}, ${pluralise(chunks.totalAddedCount, 'line')} added, ${pluralise(chunks.totalRemovedCount, 'line')} removed
    Change in total indent: ${newMetrics.total - oldMetrics.total}
    Before: mean indent: ${rounded(oldMetrics.mean)}, max indent: $oldMetrics.max, standard deviation: ${rounded(oldMetrics.standardDeviation)}
    After:  mean indent: ${rounded(newMetrics.mean)}, max indent: $newMetrics.max, standard deviation: ${rounded(newMetrics.standardDeviation)}
"""
    }

    static String pluralise(int quantity, String noun) {
        quantity == 1 ? "$quantity $noun" : "$quantity ${noun}s"
    }

    def generateReport() {
        if (configurator.csvOutput) {
            generateCsvReport()
        } else {
            generateTextReport()
        }
    }
}
