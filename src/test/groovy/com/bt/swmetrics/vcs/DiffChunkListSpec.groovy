package com.bt.swmetrics.vcs

import com.bt.swmetrics.filemetrics.MetricsCalculator
import spock.lang.Specification


class DiffChunkListSpec extends Specification {
    DiffChunkList chunkList

    def "Should be able to add chunks to the list using the normal list methods"() {
        given:
        chunkList = new DiffChunkList()

        when:
        chunkList << new DiffChunk(['@@ -0,0 +1 @@'])
        chunkList.add(new DiffChunk(['@@ -0,0 +2 @@']))
        chunkList[2] = new DiffChunk(['@@ -0,0 +3 @@'])

        then:
        chunkList.size() == 3
    }

    def "Should be able to get the total number of lines added and removed"() {
        given:
        chunkList = new DiffChunkList()
        DiffChunk chunk = Stub()
        chunk.addedCount >> 7
        chunk.removedCount >> 5
        chunkList << chunk
        chunkList << chunk

        expect:
        chunkList.totalAddedCount == 14
        chunkList.totalRemovedCount == 10
    }

    def "Should be able to get the total number of lines removed"() {

    }

    def "Should be able to get a combined list of old indents across chunks"() {
        given:
        DiffChunk chunk1 = Stub()
        DiffChunk chunk2 = Stub()
        chunk1.oldLineIndents >> [1, 2, 3]
        chunk2.oldLineIndents >> [4, 5, 6]
        chunkList = new DiffChunkList()
        chunkList << chunk1
        chunkList << chunk2

        expect:
        chunkList.oldLineIndents == [1, 2, 3, 4, 5, 6]
    }

    def "Should be able to get a combined list of new indents across chunks"() {
        given:
        DiffChunk chunk1 = Stub()
        DiffChunk chunk2 = Stub()
        chunk1.newLineIndents >> [1, 2, 3]
        chunk2.newLineIndents >> [4, 5, 6]
        chunkList = new DiffChunkList()
        chunkList << chunk1
        chunkList << chunk2

        expect:
        chunkList.newLineIndents == [1, 2, 3, 4, 5, 6]
    }

    def "Should be able to get a consolidated old metrics calculator"() {
        given:
        DiffChunk chunk1 = Stub()
        DiffChunk chunk2 = Stub()
        chunk1.oldLineIndents >> [1, 2, 3]
        chunk2.oldLineIndents >> [3, 2, 1]
        chunkList = new DiffChunkList()
        chunkList << chunk1
        chunkList << chunk2

        when:
        MetricsCalculator calc = chunkList.oldMetricsCalculator

        then:
        calc.data == [1, 2, 3, 3, 2, 1]
        calc.histogram == [1: 2, 2: 2, 3: 2]
    }

    def "Should be able to get a consolidated new metrics calculator"() {
        given:
        DiffChunk chunk1 = Stub()
        DiffChunk chunk2 = Stub()
        chunk1.newLineIndents >> [1, 2, 3]
        chunk2.newLineIndents >> [3, 2, 1]
        chunkList = new DiffChunkList()
        chunkList << chunk1
        chunkList << chunk2

        when:
        MetricsCalculator calc = chunkList.newMetricsCalculator

        then:
        calc.data == [1, 2, 3, 3, 2, 1]
        calc.histogram == [1: 2, 2: 2, 3: 2]
    }
}