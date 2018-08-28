package com.bt.swmetrics.filemetrics

import com.bt.swmetrics.Configurator
import com.bt.swmetrics.PathOperations
import groovy.util.logging.Slf4j

@Slf4j
class FileMetricsGenerator {
    Configurator configurator
    PrintStream stream
    
    Map<File, MetricsCalculator> generateAllMetrics() {
        TextFileFinder finder = new TextFileFinder(
                excludedPatterns: configurator.excludedPatterns,
                includedPatterns: configurator.includedPatterns)

        finder.findFiles(configurator.arguments).collectEntries { file ->
            def indents = IndentCalculator.calculateIndents(file.readLines(), configurator.tabSize)
            def calculator = new MetricsCalculator(indents)
            [(file) : calculator]
        }
    }

    def generateCsvOutput(Map<File,MetricsCalculator> metricsForFiles) {
        stream.print "Path,Total Lines,Non-Blank Lines,Total Indent,Mean Indent,Std Dev Indent,Max Indent,Mode Indent,Probable Function Indent"
        (0 .. 9).each { stream.print ",Level $it" }
        (1 .. 9).each { stream.print ",Pct Level $it+" }
        (1 .. 3).each { stream.print ",Span $it Count,Span $it Min,Span $it Max,Span $it Mean,Span $it Std Dev"}
        stream.println ",Function Span Count,Function Span Min,Function Span Max,Function Span Mean,Function Span Std Dev"

        metricsForFiles.each { file, calc ->
            stream.printf("%s,%d,%d,%d,%.3f,%.3f,%d,%d,%d", PathOperations.csvQuote(PathOperations.uriEncodePath(file.absolutePath)),
                    calc.lineCount,
                    calc.nonBlankLineCount,
                    calc.total,
                    calc.mean,
                    calc.standardDeviation,
                    calc.max,
                    calc.mode,
                    calc.probableFunctionLevel
            )
            def histogram = calc.histogram
            (0 .. 9).each { level -> stream.printf(",%d", histogram[level] ?: 0) }
            def percentiles = calc.percentileAtOrAbove
            (1 .. 9).each { level -> stream.printf(",%.1f", (percentiles[level] ?: 0.0) * 100) }
            [1, 2, 3, calc.mode].each { level ->
                def stats = calc.spanMetrics[level]
                if (stats) {
                    stream.printf(",%d,%d,%d,%.3f,%.3f", stats.count, stats.min, stats.max, stats.mean, stats.stdDev)
                } else {
                    stream.printf(",0,0,0,0,0")
                }
            }
            stream.println ""
        }
    }


    void printMetricsForFiles(Map<File,MetricsCalculator> metricsForFiles) {
        metricsForFiles.each { file, calculator ->
            stream.println "==== ${file.canonicalPath} ===="

            printAnnotatedSourceIfNecessary(file, calculator)
            printOverallMetrics(calculator)
            printHistogram(calculator.histogram)
            printSpanStatistics(calculator.spanMetrics)
        }
    }

    private void printOverallMetrics(MetricsCalculator calculator) {
        stream.println "Total lines: ${calculator.lineCount}, Non-blank lines: ${calculator.nonBlankLineCount}"
        stream.println "Total indent: ${calculator.total}, Min indent: ${calculator.min}, Max indent: ${calculator.max}"
        stream.printf("Mean indent: %.3f, Std Deviation: %.3f, Mode indent: %d, Probable function indent: %d\n",
                calculator.mean, calculator.standardDeviation, calculator.mode, calculator.probableFunctionLevel)
        stream.println("----")
    }

     void printAnnotatedSourceIfNecessary(File file, MetricsCalculator metricsCalculator) {
        if (!configurator.showAnnotated) {
            return
        }

        stream.println "Indents:\n"
        file.eachLine(0) { line, index ->
            if (metricsCalculator.data[index] >= 0) {
                stream.printf("%2d: %s\n", metricsCalculator.data[index], line)
            } else {
                stream.println "  :"
            }
        }
        stream.println "----"
    }

     void printSpanStatistics(Map<Integer,MetricsCalculator.SpanMetrics> spanStats) {
         stream.println "Span statistics:"
         spanStats.keySet().toSorted().each { level ->
             spanStats[level].with {
                 stream.printf("Level %d - Count: %d, Min: %d, Max: %d, Mean: %.3f, StdDev: %.3f\n", level, count, min, max, mean, stdDev)
             }
         }
         stream.println("----")
    }

     void printHistogram(Map<Integer, Integer> map) {
         def keys = map.keySet().toSorted()
         def maxValue = map.values().max()
         stream.println "Histogram:"
         keys.each {
             stream.printf("%2d: %s %d\n", it, ('#' * (50 * (map[it] / maxValue))), map[it])
         }
         stream.println("----")
    }
}
