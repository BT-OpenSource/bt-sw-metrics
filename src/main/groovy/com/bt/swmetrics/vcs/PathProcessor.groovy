package com.bt.swmetrics.vcs

import com.bt.swmetrics.Configurable
import com.bt.swmetrics.Configurator
import com.bt.swmetrics.PathOperations

trait PathProcessor implements Configurable {
    private Configurator lastConfigurator   // Only really needed to enable testing with VcsParserFactory
    private List<String> cleanedPrefixes

    String stripPrefixes(String path) {
        if (path == null) {
            return null
        }
        createCleanedPrefixesIfNecessary()
        PathOperations.stripAnyMatchingPrefixFromPath(cleanedPrefixes, stripAnyInitialSlash(path))
    }

    String stripAnyInitialSlash(String path) {
        if (path && path.startsWith('/')) {
            path - '/'
        } else {
            path
        }
    }

    private void createCleanedPrefixesIfNecessary() {
        if (cleanedPrefixes == null || configurator != lastConfigurator) {
            lastConfigurator = this.configurator
            cleanedPrefixes = configurator.ignorePrefixes.collect { String prefix -> stripAnyInitialSlash(prefix) }
        }
    }

    boolean checkIfPathIncluded(String path) {
        def included = configurator.includedPatterns.every { path =~ it }
        def excluded = configurator.excludedPatterns.any { path =~ it }
        included && !excluded
    }
}