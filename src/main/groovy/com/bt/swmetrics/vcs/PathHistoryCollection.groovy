package com.bt.swmetrics.vcs

import com.bt.swmetrics.PathOperations
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic @Slf4j
class PathHistoryCollection {
    private PathTree historyPathTree = new PathTree()
    private int lastRevision = -1

    def addCommit(String path, Commit commit, String copyPath = null, int copyRevision = -1) {
        if (commit.revision % 1000 == 0) {
            log.debug "Adding revision $commit.revision for $path to history collection"
        }
        ensureMonotonicallyIncreasingRevision(commit.revision)
        PathHistory historyEntry = findOrAddPathHistoryInTree(path)
        historyEntry.addCommit(commit)
        copyHistoryIfNecessary(path, historyEntry, copyPath, copyRevision)
        addDeletesForChildrenIfNecessary(path, commit)
    }

    PathHistory findOrAddPathHistoryInTree(String path) {
        PathHistory historyEntry = historyPathTree[path]?.history ?: new PathHistory()
        historyPathTree.add(path, historyEntry)
        historyEntry
    }

    def ensureMonotonicallyIncreasingRevision(int revision) {
        if (revision < lastRevision) {
            throw new IllegalStateException("Revision number not monotonically increasing: revision $revision < $lastRevision")
        }
        lastRevision = revision
    }

    private addDeletesForChildrenIfNecessary(String path, Commit parentCommit) {
        if (parentCommit.action == 'D') {
            findAllMatchingChildPathsWithActiveCommits(path).each {
                historyPathTree[it].history.addCommit(parentCommit)
            }
        }
    }

    private copyHistoryIfNecessary(String path, PathHistory history, String copyPath, int copyRevision) {
        if (copyRevision < 0) {
            return
        }
        def sourceHistory = historyPathTree[copyPath]?.history
        if (!sourceHistory) {
            return
        }
        history.copyHistory(sourceHistory, copyRevision)
        addCopiedChildrenAndHistory(path, copyPath, copyRevision)
    }

    private void addCopiedChildrenAndHistory(String path, String copyPath, int copyRevision) {
        def sourceChildPaths = findAllMatchingChildPathsWithHistoryAtRevision(copyPath, copyRevision)
        sourceChildPaths.each { sourcePath ->
            def childPath = path + (sourcePath - copyPath)
            addChildFromSourcePathAndRevision(childPath, sourcePath, copyRevision)
        }
    }

    private addChildFromSourcePathAndRevision(String childPath, String sourcePath, int copyRevision) {
        PathHistory sourceHistory = historyPathTree[sourcePath].history
        PathHistory history = findOrAddPathHistoryInTree(childPath)
        history.copyHistory(sourceHistory, copyRevision)
    }

    List<String> findAllMatchingChildPathsWithHistoryAtRevision(String path, int copyRevision) {
        historyPathTree[path].findAll { PathTree child ->
            def revisions = child.history?.allCommits?.collect { it.revision }
            revisions && revisions.last() <= copyRevision
        }.collect { it.path }.grep { it != path }
    }

    List<String> findAllMatchingChildPathsWithActiveCommits(String path) {
        historyPathTree[path].findAll { PathTree child ->
            child.history && child.history.commitTotal > 0
        }.collect { it.path }.grep { it != path }
    }

    Map<String,PathHistory> findOnlyActiveHistoryMapEntries() {
        historyPathTree.findAll { PathTree child -> child.history?.commitTotal > 0 }.collectEntries { [(it.path): it.history] }
    }

    PathInfoMap toPathInfoMap() {
        def active = findOnlyActiveHistoryMapEntries()
        def leafPaths = PathOperations.findLeafPaths(active.keySet()) as Set
        PathInfoMap map = new PathInfoMap()
        active.each { path, history ->
            map[path] = new PathInfo(lastCommit: history.activeCommits[0], size: leafPaths.contains(path) ? 1 : 0, history: history)
        }
        map
    }
}
