package com.bt.swmetrics

class PathOperations {
    static final String CURRENT_DIR_PREFIX = new File('').toPath().toUri().rawPath
    static final List<String> ROOT_PREFIXES = File.listRoots().collect { it.toPath().toUri().rawPath.replaceAll(/\/$/, '') }

    static String longestCommonPathPrefix(List<String> paths) {
        def splitAndSortedBySize = paths.collect { it.split('/') }.sort { it.size() }
        def shortest = splitAndSortedBySize[0]

        int index = 0
        while (index < shortest.size()) {
            if (splitAndSortedBySize.every { it[index] == shortest[index] }) {
                index++
            } else {
                return index == 0 ? '' : shortest[0 .. (index - 1)].join('/')
            }
        }
        shortest.join('/')
    }

    static String stripPrefixFromPath(String prefix, String path) {
        if (prefix == '') {
            return path
        }

        def strippedPrefix = prefix - ~/\/+$/
        def canonicalPrefix = strippedPrefix + '/'

        if (path == strippedPrefix) {
            return ''
        }

        if (path.startsWith(canonicalPrefix)) {
            return path - canonicalPrefix
        }

        path
    }

    static String uriEncodePath(String path) {
        def file = new File(path)
        String rawPath = file.toPath().toUri().rawPath
        def strippedRelative = stripPrefixFromPath(CURRENT_DIR_PREFIX, rawPath)
        if (strippedRelative != rawPath) {
            strippedRelative
        } else {
            stripAnyRootPrefix(rawPath)
        }
    }

    private static String stripAnyRootPrefix(String rawPath) {
        ROOT_PREFIXES.collect {
            (it.empty ? '' : '/') + stripPrefixFromPath(it, rawPath)
        }.sort { a, b ->
            a.size() <=> b.size()
        }.head()
    }

    static Collection<String> findLeafPaths(Collection<String> paths) {
        def groupedByStem = paths.groupBy {
            it.replaceFirst('/[^/]*$', '')
        }
        groupedByStem.collectMany { stem, subPaths ->
            subPaths.findAll { !groupedByStem[it] || groupedByStem[it].size() == 1 }
        }
    }

    static String csvQuote(String path) {
        '"' + path.replaceAll(/"/, '""') + '"'
    }

    static def stripAnyMatchingPrefixFromPath(List<String> prefixes, String path) {
        if (!prefixes) {
            return path
        }
        prefixes.collect { stripPrefixFromPath(it, path) }.min { it.size() }
    }
}
