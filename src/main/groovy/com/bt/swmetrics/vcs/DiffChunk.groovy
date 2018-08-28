package com.bt.swmetrics.vcs

import com.bt.swmetrics.filemetrics.IndentCalculator

class DiffChunk {
    private List<String> chunkText
    int oldStart
    int oldSize
    int newStart
    int newSize

    DiffChunk(List<String> chunkText) {
        this.chunkText = chunkText
        parseStartsAndSizes(chunkText[0])
    }

    private void parseStartsAndSizes(String line) {
        def matches = (line =~ /^@@ \-(\d+),?(\d+)? \+(\d+),?(\d+)? @@/)
        oldStart = (matches[0][1]) as Integer
        oldSize = (matches[0][2] ?: '1') as Integer
        newStart = (matches[0][3]) as Integer
        newSize = (matches[0][4] ?: '1') as Integer

    }

    List<String> getOldLines() {
        chunkText.grep { it =~ /^[ \-]/ }.collect { it.substring(1) }
    }

    List<String> getNewLines() {
        chunkText.grep { it =~ /^[ \+]/ }.collect { it.substring(1) }

    }

    int getRemovedCount() {
        chunkText.inject(0) { count, line -> count + (line =~ /^\-/ ? 1 : 0)}
    }

    int getAddedCount() {
        chunkText.inject(0) { count, line -> count + (line =~ /^\+/ ? 1 : 0)}
    }

    List<Integer> getOldLineIndents() {
        IndentCalculator.calculateIndents(oldLines)
    }

    List<Integer> getNewLineIndents() {
        IndentCalculator.calculateIndents(newLines)
    }
}
