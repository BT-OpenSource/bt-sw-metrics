package com.bt.swmetrics.visualisation

import com.bt.swmetrics.Configurator
import com.bt.swmetrics.PathOperations
import spock.lang.Specification
import spock.lang.Unroll

class TreeMapVisualisationSpec extends Specification {

    def "Should be able to calculate automatic thresholds for a list"() {
        given:
        Configurator stubConfigurator = Stub()
        stubConfigurator.topThreshold >> Configurator.AUTO_THRESHOLD
        stubConfigurator.bottomThreshold >> Configurator.AUTO_THRESHOLD
        def vis = new TreeMapVisualisation(configurator: stubConfigurator)

        when:
        def results = vis.setOrCalculateThresholds([-100] + [0] * 100 + [100])

        then:
        Math.round(results[0]) == -42
        Math.round(results[1]) == 42
    }

    def "Should be able to set lower threshold for a list"() {
        given:
        Configurator stubConfigurator = Stub()
        stubConfigurator.topThreshold >> Configurator.AUTO_THRESHOLD
        stubConfigurator.bottomThreshold >> 0
        def vis = new TreeMapVisualisation(configurator: stubConfigurator)

        when:
        def results = vis.setOrCalculateThresholds([-100] + [0] * 100 + [100])

        then:
        Math.round(results[0]) == 0
        Math.round(results[1]) == 42
    }

    def "Should be able to set uppper threshold for a list"() {
        given:
        Configurator stubConfigurator = Stub()
        stubConfigurator.topThreshold >> 0
        stubConfigurator.bottomThreshold >> Configurator.AUTO_THRESHOLD
        def vis = new TreeMapVisualisation(configurator: stubConfigurator)

        when:
        def results = vis.setOrCalculateThresholds([-100] + [0] * 100 + [100])

        then:
        Math.round(results[0]) == -42
        Math.round(results[1]) == 0
    }

    class TestProducer implements VisualisationProducer {
        {
            resourceNames = [
                    htmlTemplate: 'test.template',
                    jsLibrary1: 'js.1.data',
                    jsLibrary2: 'js.2.data',
                    initialisationCode: 'js.3.data',
                    styleSheet: 'css.data'
            ]
        }
        List addedPaths = []

        def addData(String path, BigDecimal size, BigDecimal colour, List extra) {
            addedPaths << [path: path, size: size, colour: colour, extra: extra]
        }

        def toJsonable() {
            return ["JSON"]
        }

        @Override
        List<String> getPathComponentIdList() {
            ['A', 'B']
        }
    }

    def "Should be able to generate visualisation from CSV file and producer"() {
        given:
        Configurator stubConfigurator = Stub()
        stubConfigurator.bottomThreshold >> Configurator.AUTO_THRESHOLD
        stubConfigurator.topThreshold >> Configurator.AUTO_THRESHOLD
        stubConfigurator.sizeColumn >> 'Total Lines'
        stubConfigurator.pathColumn >> 'File URI'
        stubConfigurator.colourColumn >> 'Total Indent'
        stubConfigurator.extraColumns >> ['Level 0', 'Level 1']
        stubConfigurator.title >> 'TITLE'
        stubConfigurator.topColour >> 0xffffff
        stubConfigurator.bottomColour >> 0x000000
        stubConfigurator.resourcePath >> 'PATH'

        and:
        def producer = new TestProducer()
        def csvData = producer.loadResourceText('sample.csv')
        def visualisation = new TreeMapVisualisation(configurator: stubConfigurator, producer: producer, csvData: csvData)

        when:
        def result = visualisation.generate()

        then:
        producer.addedPaths.size() == 48
        result == '''title
TITLE
---
treeMapJson
["JSON"]
---
resourcePath
PATH
---
jsLibrary1File
js.1.data
---
jsLibrary1Text
JS1
---
jsLibrary2File
js.2.data
---
jsLibrary2Text
JS2
---
initialisationCodeFile
js.3.data
---
initialisationCodeText
JS3
---
styleSheetFile
css.data
---
styleSheetText
CSS
---
sizeMetric
Total Lines
---
sizeMetricId
@1
---
colourMetric
Total Indent
---
colourMetricId
@3
---
pathPrefix
file:///E:/work/streetworks/streetworks/trunk/trunk
---
topThreshold
2386.928
---
bottomThreshold
0.000
---
topColour
#ffffff
---
bottomColour
#000000
---
extraColumnIdsJson
["@8","@9"]
---
extraColumnNamesJson
["Level 0","Level 1"]
---
pathComponentIdListJson
["A","B"]
---
colourFields
[@3, @8, @9, @1]
---
sizeFields
[@1, @8, @9, @3]
---
extraFields
[@8, @9]
---
columnIdToNameMap
[@0:File URI, @1:Total Lines, @2:Non-Blank Lines, @3:Total Indent, @4:Mean Indent, @5:StdDev Indent, @6:Max Indent, @7:Mode Indent, @8:Level 0, @9:Level 1, @10:Level 2, @11:Level 3, @12:Level 4, @13:Level 5, @14:Level 6, @15:Level 7, @16:Level 8, @17:Level 9, @18:Span 1 Count, @19:Span 1 Min, @20:Span 1 Max, @21:Span 1 Mean, @22:Span 1 Std Dev, @23:Span 2 Count, @24:Span 2 Min, @25:Span 2 Max, @26:Span 2 Mean, @27:Span 2 Std Dev, @28:Span 3 Count, @29:Span 3 Min, @30:Span 3 Max, @31:Span 3 Mean, @32:Span 3 Std Dev, @33:Mode Span Count, @34:Mode Span Min, @35:Mode Span Max, @36:Mode Span Mean, @37:Mode Span Std Dev]
---
columnIdToNameMapJson
{"@0":"File URI","@1":"Total Lines","@2":"Non-Blank Lines","@3":"Total Indent","@4":"Mean Indent","@5":"StdDev Indent","@6":"Max Indent","@7":"Mode Indent","@8":"Level 0","@9":"Level 1","@10":"Level 2","@11":"Level 3","@12":"Level 4","@13":"Level 5","@14":"Level 6","@15":"Level 7","@16":"Level 8","@17":"Level 9","@18":"Span 1 Count","@19":"Span 1 Min","@20":"Span 1 Max","@21":"Span 1 Mean","@22":"Span 1 Std Dev","@23":"Span 2 Count","@24":"Span 2 Min","@25":"Span 2 Max","@26":"Span 2 Mean","@27":"Span 2 Std Dev","@28":"Span 3 Count","@29":"Span 3 Min","@30":"Span 3 Max","@31":"Span 3 Mean","@32":"Span 3 Std Dev","@33":"Mode Span Count","@34":"Mode Span Min","@35":"Mode Span Max","@36":"Mode Span Mean","@37":"Mode Span Std Dev"}
---'''
    }

    def "Should be able to filter included/excluded paths"() {
        given:
        Configurator stubConfigurator = Stub()
        stubConfigurator.bottomThreshold >> Configurator.AUTO_THRESHOLD
        stubConfigurator.topThreshold >> Configurator.AUTO_THRESHOLD
        stubConfigurator.sizeColumn >> 'Total Lines'
        stubConfigurator.pathColumn >> 'File URI'
        stubConfigurator.colourColumn >> 'Total Indent'
        stubConfigurator.extraColumns >> ['Level 0', 'Level 1']
        stubConfigurator.title >> 'TITLE'
        stubConfigurator.topColour >> 0xffffff
        stubConfigurator.bottomColour >> 0x000000
        stubConfigurator.resourcePath >> 'PATH'
        stubConfigurator.includedPatterns >> ['.*/Construction/.*']
        stubConfigurator.excludedPatterns >> ['/(docs|reports|scripts)/']

        and:
        def producer = new TestProducer()
        def csvData = producer.loadResourceText('sample.csv')
        def visualisation = new TreeMapVisualisation(configurator: stubConfigurator, producer: producer, csvData: csvData)

        when:
        def result = visualisation.generate()

        then:
        producer.addedPaths*.path == [
                'build.properties',
                'build.xml',
                'dbmaintain.properties',
                'ivy.xml',
                'ivysettings.xml'
        ]

        producer.addedPaths*.size == [ 79, 371, 174, 19, 14 ]
    }
}