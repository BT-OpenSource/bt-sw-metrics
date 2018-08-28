package com.bt.swmetrics.visualisation

import com.bt.swmetrics.Configurator
import spock.lang.Specification
import spock.lang.Unroll


class VisualisationProducerFactorySpec extends Specification {

    @Unroll
    def "Should generate an appropriate instance given different treemap types"() {
        given:
        Configurator stubConfigurator = Stub()
        def factory = new VisualisationProducerFactory(configurator: stubConfigurator)
        stubConfigurator.treeMapVisualisation >> name

        expect:
        factory.producer.class == type

        where:
        name        || type
        'd3'        || D3PlusProducer
        'd3plus'    || D3PlusProducer
        'D3PluS'    || D3PlusProducer
        'jit'       || JitProducer
        'JIT'       || JitProducer
    }

    def "Should throw an exception for an unknown treemap type"() {
        given:
        Configurator stubConfigurator = Stub()
        def factory = new VisualisationProducerFactory(configurator: stubConfigurator)
        stubConfigurator.treeMapVisualisation >> 'UNKNOWN'

        when:
        factory.producer

        then:
        thrown IllegalArgumentException
    }
}