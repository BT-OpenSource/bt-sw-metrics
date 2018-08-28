package com.bt.swmetrics.visualisation

import com.bt.swmetrics.Configurable
import com.bt.swmetrics.Configurator
import com.bt.swmetrics.filemetrics.ListStatsCalculator
import com.bt.swmetrics.PathOperations
import groovy.json.JsonOutput
import groovy.text.GStringTemplateEngine

import java.math.RoundingMode

class TreeMapVisualisation implements Configurable {
    String csvData
    VisualisationProducer producer
    private CsvDataParser parser
    private static final String OMIT_PATH = '\u0000\u0000'  // Will never be a valid path

    String generate() {
        parser = new CsvDataParser(
                csvData,
                configurator.pathColumn,
                configurator.sizeColumn,
                configurator.colourColumn,
                configurator.extraColumns,
                { checkIfPathIncluded(it) }
        )

        def (pathPrefix, stripped) = findCommonPrefixAndStripFromPaths()
        def (lowerThreshold, upperThreshold) = setOrCalculateThresholds(parser.colours)

        configureProducer(upperThreshold, lowerThreshold)
        addNonOmittedStrippedPathsToProducer(stripped)
        renderTemplateWithComputedValues(pathPrefix, upperThreshold, lowerThreshold)
    }

    private Tuple2<String,List<String>> findCommonPrefixAndStripFromPaths() {
        def pathPrefix = PathOperations.longestCommonPathPrefix(parser.paths)
        new Tuple2<String,List<String>>(pathPrefix, parser.paths.collect { PathOperations.stripPrefixFromPath(pathPrefix, it) })
    }

    private boolean checkIfPathIncluded(String path) {
        def included = configurator.includedPatterns.every { path =~ it }
        def excluded = configurator.excludedPatterns.any { path =~ it }
        included && !excluded
    }

    List<BigDecimal> setOrCalculateThresholds(List<BigDecimal> colourValues) {
        ListStatsCalculator listStatsCalculator = new ListStatsCalculator(colourValues)

        [calculateBottomThreshold(listStatsCalculator), calculateTopThreshold(listStatsCalculator)]
    }

    private BigDecimal calculateTopThreshold(ListStatsCalculator listStatsCalculator) {
        def top = configurator.topThreshold
        if (configurator.topThreshold == Configurator.AUTO_THRESHOLD) {
            top = listStatsCalculator.mean + 3 * listStatsCalculator.standardDeviation
            if (top > listStatsCalculator.max) {
                top = listStatsCalculator.max
            }
        }
        top
    }

    private BigDecimal calculateBottomThreshold(ListStatsCalculator listStatsCalculator) {
        def bottom = configurator.bottomThreshold
        if (configurator.bottomThreshold == Configurator.AUTO_THRESHOLD) {
            bottom = listStatsCalculator.mean - 3 * listStatsCalculator.standardDeviation
            if (bottom < listStatsCalculator.min) {
                bottom = listStatsCalculator.min
            }
        }
        bottom
    }

    private int configureProducer(upperThreshold, lowerThreshold) {
        producer.with {
            sizeMetricName = parser.sizeColumnId.toString()
            colourMetricName = parser.colourColumnId.toString()
            extraMetricNames = parser.extraColumnIds.collect { it.toString() }
            partitionSize = configurator.partitionSize
            topThreshold = upperThreshold
            bottomThreshold = lowerThreshold
            bottomColourValue = configurator.bottomColour
            topColourValue = configurator.topColour
        }
    }

    private List addNonOmittedStrippedPathsToProducer(List<String> stripped) {
        [stripped, parser.sizes, parser.colours, parser.extraData].transpose().each { path, size, colour, extra ->
            if (path != OMIT_PATH) {
                producer.addData(path, size, colour, extra)
            }
        }
    }

    private String renderTemplateWithComputedValues(String prefix, upperThreshold, lowerThreshold) {
        def json = JsonOutput.toJson(producer.toJsonable())
        def template = new GStringTemplateEngine().createTemplate(producer.htmlTemplateText)
        def binding = [
                title                  : configurator.title,
                treeMapJson            : json,
                resourcePath           : configurator.resourcePath,
                jsLibrary1File         : producer.resourceNames.jsLibrary1,
                jsLibrary2File         : producer.resourceNames.jsLibrary2,
                jsLibrary1Text         : producer.jsLibrary1Text,
                jsLibrary2Text         : producer.jsLibrary2Text,
                initialisationCodeFile : producer.resourceNames.initialisationCode,
                initialisationCodeText : producer.initialisationCodeText,
                styleSheetFile         : producer.resourceNames.styleSheet,
                styleSheetText         : producer.styleSheetText,
                sizeMetric             : parser.sizeColumnName,
                sizeMetricId           : parser.sizeColumnId,
                colourMetric           : parser.colourColumnName,
                colourMetricId         : parser.colourColumnId,
                pathPrefix             : prefix,
                topThreshold           : upperThreshold.setScale(3, RoundingMode.HALF_EVEN),
                bottomThreshold        : lowerThreshold.setScale(3, RoundingMode.HALF_EVEN),
                topColour              : sprintf("#%06x", configurator.topColour),
                bottomColour           : sprintf("#%06x", configurator.bottomColour),
                extraColumnIdsJson     : JsonOutput.toJson(parser.extraColumnIds.collect { it.toString() }),
                extraColumnNamesJson   : JsonOutput.toJson(parser.extraColumnNames),
                pathComponentIdListJson: JsonOutput.toJson(producer.pathComponentIdList),
                colourFields           : [parser.colourColumnId] + parser.extraColumnIds + [parser.sizeColumnId],
                sizeFields             : [parser.sizeColumnId] + parser.extraColumnIds + [parser.colourColumnId],
                extraFields            : parser.extraColumnIds,
                columnIdToNameMap      : parser.columnIdToNameMap,
                columnIdToNameMapJson  : JsonOutput.toJson(parser.columnIdToNameMap),
        ]
        template.make(binding).toString()
    }
}
