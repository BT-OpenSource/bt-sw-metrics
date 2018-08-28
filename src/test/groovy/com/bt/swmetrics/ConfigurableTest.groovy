package com.bt.swmetrics

import spock.lang.Specification


class ConfigurableTest extends Specification {
    class Implementor implements Configurable {
    }

    def "Configurable trait provides configurator"() {
        given:
        Configurator stub = Stub()
        Implementor implementor = new Implementor(configurator: stub)

        expect:
        implementor.getConfigurator() is stub
    }

    trait SubConfigurable implements Configurable {
        Configurator getSubConfigurator() {
            this.configurator
        }
    }

    class SubImplementor implements SubConfigurable, Configurable {
    }

    def "Sub-trait can safely implement Configurable and sub-class configurator instance not affected"() {
        given:
        Configurator stub = Stub()
        SubImplementor implementor = new SubImplementor(configurator: stub)

        expect:
        implementor.getConfigurator() is stub
        implementor.getSubConfigurator() is stub
    }
}