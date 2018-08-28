package com.bt.swmetrics.vcs.svn

import com.bt.swmetrics.vcs.DiffChunk
import com.bt.swmetrics.vcs.DiffChunkList
import com.bt.swmetrics.vcs.DiffParser


class SvnDiffParser extends DiffParser {

    @Override
    List<String> getPaths() {
        lines.findAll { it =~ /^Index:/ }.collect { (it - 'Index: ') .trim() }
    }

    Map<String,List<String>> getLinesByPath() {
        def result = [:]
        def currentPath = ''
        lines.each {
            if (it =~ /^Index:/) {
                currentPath = it - 'Index: '
                result[currentPath] = []
            } else if (currentPath) {
                result[currentPath] << it
            }
        }
        result
    }
}
