package com.bt.swmetrics

import com.bt.swmetrics.csv.CsvJoinRunner
import com.bt.swmetrics.filemetrics.FileMetricsGenerator
import com.bt.swmetrics.vcs.AuthorStatsGenerator
import com.bt.swmetrics.vcs.DiffStatsGenerator
import com.bt.swmetrics.vcs.PathStatsReporter
import com.bt.swmetrics.visualisation.VisualisationRunner

// SLF4J logging is NOT initialised here as its configuration may be
// changed by the Configurator settings
class Main {
    public static void main(String[] args) {
        Configurator configurator = new Configurator(args)
        FileMetricsGenerator fileMetricsRunner = new FileMetricsGenerator(configurator: configurator, stream: System.out)
        VisualisationRunner visualisationRunner = new VisualisationRunner(configurator: configurator, stream: System.out)
        PathStatsReporter pathReporter = new PathStatsReporter(configurator: configurator, stream: System.out)
        AuthorStatsGenerator authorReporter = new AuthorStatsGenerator(configurator: configurator, stream: System.out)
        CsvJoinRunner joinRunner = new CsvJoinRunner(configurator: configurator, stream: System.out)
        DiffStatsGenerator diffStatsReporter = new DiffStatsGenerator(configurator: configurator, stream: System.out)

        if (configurator.helpRequested) {
            configurator.showHelp()
            System.exit(0)
        }

        try {
             if (configurator.treeMapVisualisation) {
                 visualisationRunner.generateTreeMapVisualisation()
             } else if (configurator.vcsLogFile && configurator.authorStats) {
                 authorReporter.generateAuthorStatsCsv()
             } else if (configurator.vcsLogFile && configurator.authorPaths) {
                authorReporter.generateAuthorPathsCsv()
             } else if (configurator.vcsLogFile || configurator.vcsListFile) {
                 pathReporter.generateVcsMetricsCsv()
             } else if (configurator.csvJoinFields) {
                 joinRunner.joinFiles()
             } else if (configurator.vcsDiffFile) {
                 diffStatsReporter.generateReport()
            } else if (configurator.csvOutput) {
                 fileMetricsRunner.generateCsvOutput(fileMetricsRunner.generateAllMetrics())
             } else {
                fileMetricsRunner.printMetricsForFiles(fileMetricsRunner.generateAllMetrics())
            }
        } catch (Exception e) {
            System.err.println("ERROR: ${e.message}")
            if (configurator.debug) {
                e.printStackTrace(System.err)
            }
            System.exit(1)
        }
    }
}
