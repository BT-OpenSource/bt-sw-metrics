package com.bt.swmetrics.filemetrics

class WhitespacePrefixCalculator {
    public static final int EMPTY_LINE = -1

    int tabSize = 4
    // In theory the tab padding string might not be long enough for big values of tabSize.
    // But in practice it should be OK for anything reasonable!
    private final static String TAB_PADDING = '                '

    int countWhitespace(String line) {
        if (line.isAllWhitespace()) {
            return EMPTY_LINE
        }
        def initialWhitespace = line.takeWhile { it == ' ' || it == '\t' }
        expandTabs(initialWhitespace).size()
    }

    private String expandTabs(CharSequence input) {
        input.inject('') { prefix, nextChar ->
            if (nextChar == "\t") {
                prefix + TAB_PADDING.substring(0, (tabSize - (prefix.size() % tabSize)))
            } else {
                prefix + nextChar
            }
        }
    }
}
