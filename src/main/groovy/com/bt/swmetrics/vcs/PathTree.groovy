package com.bt.swmetrics.vcs

import groovy.transform.CompileStatic

@CompileStatic
class PathTree {
    String name
    PathHistory history
    private Map<String,PathTree> childMap = [:]
    private iterator = childMap.iterator()
    private PathTree parent

    def add(String path, PathHistory pathHistory) {
        addSplitPath(splitPath(path), 0, pathHistory)
    }

    private List<String> splitPath(String path) {
        def List<String> parts
        if (path == '/') {
            parts = ['']
        } else {
            parts = path.split('/') as List<String>
        }
        parts
    }

    private def addSplitPath(List<String> parts, int headIndex, PathHistory pathHistory) {
        PathTree child = childMap[parts[headIndex]] ?: new PathTree(name: parts[headIndex], parent: this)
        if (headIndex == parts.size() - 1) {
            child.history = pathHistory
        } else {
            child.addSplitPath(parts, headIndex + 1, pathHistory)
        }
        childMap[parts[headIndex]] = child
    }

    PathTree getAt(String path) {
        if (path == null) { return null }
        findChildFromSplitPath(splitPath(path), 0)
    }

    private PathTree findChildFromSplitPath(List<String> parts, int headIndex) {
        def child = childMap[parts[headIndex]]
        if (child && headIndex < parts.size() - 1) {
            child.findChildFromSplitPath(parts, headIndex + 1)
        } else {
            child
        }
    }

    String getPath() {
        buildPath(new StringBuilder()).toString()
    }

    StringBuilder buildPath(StringBuilder resultBuilder) {
        if (parent == null) {
            resultBuilder
        } else {
            def initialSize = resultBuilder.length()
            resultBuilder.insert(0, name)
            if (initialSize > 0) {
                resultBuilder.insert(name.length(),'/')
            }
            parent.buildPath(resultBuilder)
        }
    }

    List<PathTree> getChildren() {
        childMap.values().collect()
    }

    Collection<PathTree> findAll(Closure closure) {
        def childResults = childMap.collectMany { childName, child -> child.findAll(closure) }
        if (closure.call(this)) {
            childResults << this
        }
        childResults
    }
}
