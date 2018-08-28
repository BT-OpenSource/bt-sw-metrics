package com.bt.swmetrics.visualisation

import com.bt.swmetrics.Configurable


class VisualisationRunner implements Configurable {
    PrintStream stream

    def generateTreeMapVisualisation() {
        String dataFile = configurator.arguments[0]
        if (!dataFile) {
            throw new IllegalArgumentException("Input CSV filename is needed for treemap visualisation")
        }

        def csvData = new File(dataFile).text
        VisualisationProducer producer = new VisualisationProducerFactory(configurator: configurator).producer
        TreeMapVisualisation visualisation = new TreeMapVisualisation(configurator: configurator, csvData: csvData, producer: producer)
        stream.println visualisation.generate()
    }
}
