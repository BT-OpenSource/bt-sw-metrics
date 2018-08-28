package com.bt.swmetrics.vcs

import com.bt.swmetrics.PathOperations

class PathInfoMap implements Map<String, PathInfo> {
    @Delegate Map<String,PathInfo> pathInfoMap = [:]
    List<String> stripPrefixes

    def mergeHistory(PathHistoryCollection historyCollection) {
        def activeHistoryMap = historyCollection.findOnlyActiveHistoryMapEntries()
        def strippedHistoryToFullPathMap = mapStrippedPathsToOriginalPaths(activeHistoryMap)
        def fullListToStrippedPathMap = mapOriginalPathsToStrippedPaths(pathInfoMap)

        pathInfoMap.each { path, info ->
            info.history = activeHistoryMap[strippedHistoryToFullPathMap[fullListToStrippedPathMap[path]]]
        }
    }

    private Map<String,String> mapStrippedPathsToOriginalPaths(Map<String,Object> pathIndexedMap) {
        pathIndexedMap.keySet().collectEntries {
            // In the next line a straight access to "this.stripPrefixes" does not work because
            // (it seems) of the @Delegate which somehow sends the access to the map instead
            // of the local method.
            def stripped = PathOperations.stripAnyMatchingPrefixFromPath(getStripPrefixes(), it)
            [(stripped): it]
        }
    }

    private Map<String,String> mapOriginalPathsToStrippedPaths(Map<String,Object> pathIndexedMap) {
        mapStrippedPathsToOriginalPaths(pathIndexedMap).collectEntries {
            String k, String v -> [(v): k]
        } as Map<String,String>
    }
}
