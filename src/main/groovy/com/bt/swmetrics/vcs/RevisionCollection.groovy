package com.bt.swmetrics.vcs

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic @Slf4j
class RevisionCollection {

    private boolean keepingAllRevisions = true  // By default, keep all unless an explicit list is given
    private Map<Integer,RevisionTreeNode> keptRevisions = [:]
    private Map<Integer,Commit> revisionCommits = [:]
    private RevisionTreeNode currentTree = RevisionTreeNode.EMPTY

    private int lastRevision = -1
    private long additionCount = 0

    void setKeepRevisions(List<Integer> revisions) {
        keepingAllRevisions = false
        keptRevisions = [:]
        revisions.each { keptRevisions[it] = RevisionTreeNode.EMPTY }
    }

    void addCommit(String path, Commit commit, String copyPath = null, int copyRevision = -1) {
        assert !path.startsWith('/') && (copyPath == null || !copyPath.startsWith('/'))

        additionCount++
        if (additionCount % 1000 == 0) {
            log.debug "Path addition $additionCount: path = $path, revision = ${commit.revision}"
        }
        ensureMonotonicallyIncreasingRevision(commit.revision)
        currentTree = buildNextTreeBasedOnCommitAction(commit, path, copyPath, copyRevision)
        keepRevisionIfNeeded(currentTree, commit)
        revisionCommits[commit.revision] = commit
    }

    private def ensureMonotonicallyIncreasingRevision(int revision) {
        if (revision < lastRevision) {
            throw new IllegalStateException("Revision number not monotonically increasing: revision $revision < $lastRevision")
        }
        lastRevision = revision
    }

    private RevisionTreeNode buildNextTreeBasedOnCommitAction(Commit commit, String path, String copyPath, int copyRevision) {
        if (commit.action == 'D') {
            currentTree.delete(path, commit.revision)
        } else if (copyPath != null && copyRevision >= 0 && keptRevisions[copyRevision]?.get(copyPath)) {
            currentTree.copy(keptRevisions[copyRevision].get(copyPath), path, commit.revision)
        } else {
            currentTree.add(path, commit.revision)
        }
    }

    private void keepRevisionIfNeeded(RevisionTreeNode currentTree, Commit commit) {
        if (keepingAllRevisions || keptRevisions[commit.revision]) {
            keptRevisions[commit.revision] = currentTree
        }
    }

    Map<String,PathHistory> getFullPathHistoryMap() {
        currentTree.pathIterator.collectEntries { path ->
            [(path): makePathHistory(currentTree.listRevisions(path))]
        }
    }

    private PathHistory makePathHistory(List<Integer> revisions) {
        def history = new PathHistory()
        revisions.reverse().each { history.addCommit(revisionCommits[it]) }
        history
    }

    Collection<PathCommitRecord> getLeafPathCommitRecords() {
        (currentTree.pathIterator.findAll {
            String path -> currentTree.get(path).isLeaf()
        } as Collection<String>)
        .collect { String path ->
            def lastCommit = revisionCommits[currentTree.get(path).revision]
            new PathCommitRecord(path, lastCommit, 1)
        }
    }

    PathHistory historyForPath(String path) {
        if (path != null) {
            currentTree.get(path) ? makePathHistory(currentTree.listRevisions(path)) : null
        } else {
            null
        }
    }

    Collection<String> getFinalPaths() {
        currentTree.pathIterator.collect { String path -> path }
    }
}
