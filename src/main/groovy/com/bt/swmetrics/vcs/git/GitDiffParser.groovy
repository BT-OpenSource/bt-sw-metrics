package com.bt.swmetrics.vcs.git

import com.bt.swmetrics.vcs.DiffChunk
import com.bt.swmetrics.vcs.DiffParser

class GitDiffParser extends DiffParser {
    @Override
    List<String> getPaths() {
        lines.findAll { it =~ /^diff --git/ }.collect { extractPath(it) }
    }

    private String extractPath(String diffLine) {
        stripPrefixes(diffLine.replaceFirst(/.* b\//, '').trim())
    }

    @Override
    Map<String, List<String>> getLinesByPath() {
        def result = [:]
        def currentPath = ''
        lines.each {
            if (it =~ /^diff --git/) {
                currentPath = extractPath(it)
                result[currentPath] = []
            } else if (currentPath) {
                result[currentPath] << it
            }
        }
        result
    }
}
