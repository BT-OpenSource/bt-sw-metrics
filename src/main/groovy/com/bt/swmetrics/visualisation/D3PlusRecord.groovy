package com.bt.swmetrics.visualisation

class D3PlusRecord {
    String path
    BigDecimal size
    BigDecimal colour
    List extra = []
    private List<String> components
    int levelLimit = 0

    List<String> getPathComponents() {
        components = components ?: (path.split('/', levelLimit) as List<String>).collect { it.contains('%') ? new URI(it).path : it }
    }

    Map toJsonable(String sizeName, String colourName, List<String> extraNames) {
        def result = [:]
        pathComponents.eachWithIndex{ String component, int index ->
            def key = '#' + index
            result[key] = component
        }
        result[sizeName] = size
        result[colourName] = colour
        [extraNames, extra].transpose().each { name, data -> result[name] = convertToBigDecimalIfPossible(data) }
        result.findAll { it.value != '' }
    }

    static def convertToBigDecimalIfPossible(value) {
        try {
            value as BigDecimal
        } catch (e) {
            value
        }
    }
}
