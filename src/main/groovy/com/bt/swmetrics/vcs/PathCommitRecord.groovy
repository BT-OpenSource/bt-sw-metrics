package com.bt.swmetrics.vcs

class PathCommitRecord {
    public final String path
    public final Commit commit
    public final String copyPath
    public final int copyRevision
    public final long size

    PathCommitRecord(String path, Commit commit, String copyPath, int copyRevision) {
        this.path = path
        this.commit = commit
        this.copyPath = copyPath
        this.copyRevision = copyRevision
        this.size = -1
    }

    PathCommitRecord(String path, Commit commit, long size) {
        this.path = path
        this.commit = commit
        this.copyPath = null
        this.copyRevision = -1
        this.size = size
    }
}
