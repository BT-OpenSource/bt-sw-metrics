package com.bt.swmetrics.vcs


abstract class ListParser implements PathProcessor {
    List<PathCommitRecord> parseToPathCommitRecordList(String text) {
        throw new UnsupportedOperationException("ListParser parseToPathCommitRecordList not supported")
    }

    List<PathCommitRecord> parseToPathCommitRecordList(File file) {
        throw new UnsupportedOperationException("ListParser parseToPathCommitRecordList not supported")
    }
}