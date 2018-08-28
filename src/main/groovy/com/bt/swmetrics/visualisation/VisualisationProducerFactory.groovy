package com.bt.swmetrics.visualisation

import com.bt.swmetrics.Configurator

class VisualisationProducerFactory {
    Configurator configurator

    VisualisationProducer getProducer() {
        switch (configurator.treeMapVisualisation.toLowerCase()) {
            case 'jit':
                new JitProducer()
                break

            case 'd3':
            case 'd3plus':
                new D3PlusProducer()
                break

            default:
                throw new IllegalArgumentException("Invalid tree-map visualisation type: ${configurator.treeMapVisualisation}")
        }
    }
}
