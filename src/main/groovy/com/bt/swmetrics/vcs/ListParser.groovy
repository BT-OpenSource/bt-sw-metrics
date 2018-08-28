package com.bt.swmetrics.vcs

abstract class ListParser {
    List<Tuple> parseToTupleList(String text) {
        throw new UnsupportedOperationException("ListParser parseToTupleList not supported")
    }

    PathInfoMap parseToPathInfoMap(File file) {
        throw new UnsupportedOperationException("ListParser parseToPathInfoMap not supported")
    }
}