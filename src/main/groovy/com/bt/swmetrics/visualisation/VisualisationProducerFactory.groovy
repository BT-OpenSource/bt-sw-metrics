package com.bt.swmetrics.visualisation

import com.bt.swmetrics.Configurable

class VisualisationProducerFactory implements Configurable {

    VisualisationProducer getProducer() {
        switch (configurator.treeMapVisualisation.toLowerCase()) {
            case 'jit':
                new JitProducer(levelLimit: configurator.levelLimit)
                break

            case 'd3':
            case 'd3plus':
                new D3PlusProducer(levelLimit: configurator.levelLimit)
                break

            default:
                throw new IllegalArgumentException("Invalid tree-map visualisation type: ${configurator.treeMapVisualisation}")
        }
    }
}
