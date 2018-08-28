package com.bt.swmetrics.vcs

import com.bt.swmetrics.filemetrics.MetricsCalculator

class DiffChunkList implements List<DiffChunk> {
    @Delegate private List<DiffChunk> chunkList

    DiffChunkList() {
        chunkList = []
    }

    DiffChunkList(List<DiffChunk> chunks) {
        chunkList = chunks
    }

    List<Integer> getOldLineIndents() {
        chunkList.collectMany { it.oldLineIndents }
    }

    List<Integer> getNewLineIndents() {
        chunkList.collectMany { it.newLineIndents }
    }

    MetricsCalculator getOldMetricsCalculator() {
        new MetricsCalculator(oldLineIndents)
    }

    MetricsCalculator getNewMetricsCalculator() {
        new MetricsCalculator(newLineIndents)
    }

    int getTotalAddedCount() {
        chunkList.inject(0) { int total, chunk -> total + chunk.addedCount }
    }

    int getTotalRemovedCount() {
        chunkList.inject(0) { int total, chunk -> total + chunk.removedCount }
    }
}
