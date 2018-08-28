package com.bt.swmetrics.vcs

import groovy.transform.Canonical

@Canonical
class PathInfo {
    Commit lastCommit
    long size
    PathHistory history
}
