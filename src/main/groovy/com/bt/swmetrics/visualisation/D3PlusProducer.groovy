package com.bt.swmetrics.visualisation

class D3PlusProducer implements VisualisationProducer {
    {
        resourceNames = [
                htmlTemplate: 'treemap-d3plus-template.html',
                jsLibrary1: 'd3.min.js',
                jsLibrary2: 'd3plus.min.js',
                initialisationCode: 'init-d3plus.js',
                styleSheet: 'treemap-d3plus.css'
        ]
    }

    List<D3PlusRecord> records = []

    def addData(String path, BigDecimal size, BigDecimal colour, List extraData) {
        D3PlusRecord record = new D3PlusRecord(path: path, size: size, colour: colour, extra: extraData)
        records << record
    }

    def toJsonable() {
        records.collect { it.toJsonable(sizeMetricName, colourMetricName, extraMetricNames) }
    }

    List<String> getPathComponentIdList() {
        def maxComponents = records.collect { it.pathComponents.size() }.max()
        (0 ..< maxComponents).collect { '#' + it }
    }
}
