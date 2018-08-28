package com.bt.swmetrics.vcs

abstract class DiffParser implements PathProcessor {
    List<String> lines

    abstract List<String> getPaths()
    abstract Map<String,List<String>> getLinesByPath()

    Map<String, DiffChunkList> getChunksByPath() {
        linesByPath.collectEntries { String path, List<String> fileLines ->

            [(path): findChunks(fileLines)]
        } as Map<String, DiffChunkList>
    }

    static DiffChunkList findChunks(List<String> fileLines) {
        def linesWithoutHeader = fileLines.dropWhile { !(it.startsWith('@@')) }
        linesWithoutHeader.inject([]) { List<List<String>> chunked, String line ->
            appendNewChunkLineListOrAddLineToLatest(line, chunked)
        }.collect { new DiffChunk(it) }
    }

    private static List<List<String>> appendNewChunkLineListOrAddLineToLatest(String line, List<List<String>> chunked) {
        if (line.startsWith('@@')) {
            chunked << [line]
        } else {
            chunked[-1] << line
        }
        chunked
    }
}