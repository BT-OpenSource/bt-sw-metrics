package com.bt.swmetrics.vcs


abstract class LogParser {

    abstract List<Tuple> parseToTupleList(File file)

    abstract List<Tuple> parseToTupleList(String text)

    abstract Map<String, AuthorStats> parseToAuthorStatsMap(File file)

    PathHistoryCollection parseToPathHistoryCollection(File file) {
        def parsed = parseToTupleList(file)
        PathHistoryCollection collection = new PathHistoryCollection()
        parsed.sort { a, b ->
            Commit commitA = a[1] as Commit
            Commit commitB = b[1] as Commit
            commitA.revision <=> commitB.revision

        }.each {
            collection.addCommit(*it)
        }
        collection
    }
}