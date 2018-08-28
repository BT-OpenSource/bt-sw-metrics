package com.bt.swmetrics.vcs


abstract class LogParser implements PathProcessor {

    abstract List<PathCommitRecord> parseToPathCommitRecordList(File file)

    abstract List<PathCommitRecord> parseToPathCommitRecordList(String text)

    abstract Map<String, AuthorStats> parseToAuthorStatsMap(File file)

    RevisionCollection parseToRevisionCollection(File file) {
        def parsed = parseToPathCommitRecordList(file)
        RevisionCollection collection = new RevisionCollection()
        collection.keepRevisions = findCopyBaseRevisions(parsed)
        def firstCommit = parsed[0].commit
        def lastCommit = parsed[-1].commit

        if (firstCommit.revision < lastCommit.revision) {
            parsed.each { collection.addCommit(it.path, it.commit, it.copyPath, it.copyRevision)}
        } else {
            parsed.reverseEach { collection.addCommit(it.path, it.commit, it.copyPath, it.copyRevision)}
        }
        collection
    }

    private static List<Integer> findCopyBaseRevisions(List<PathCommitRecord> entries) {
        entries.findAll { it.copyRevision >= 0 }.collect { it.copyRevision }
    }
}