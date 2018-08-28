package com.bt.swmetrics.filemetrics

class IndentCalculator {
    public final static int EMPTY_LINE = -1

    static List<Integer> calculateIndents(List<String> lines, int tabSize = 4) {
        def wsCalculator = new WhitespacePrefixCalculator(tabSize: tabSize)
        def absoluteIndents = lines.collect { wsCalculator.countWhitespace(it) }
        Stack<Integer> indentStack = new Stack<Integer>()
        indentStack.push(0)
        absoluteToRelativeIndent(absoluteIndents)
    }

    private static List<Integer> absoluteToRelativeIndent(List<Integer> absoluteIndents) {
        List<Integer> relativeIndents = []
        Stack<Integer> absoluteIndentStack = new Stack<>()
        absoluteIndentStack.push(0)
        int nextRelativeIndent

        absoluteIndents.each { nextAbsoluteIndent ->
            if (nextAbsoluteIndent == WhitespacePrefixCalculator.EMPTY_LINE) {
                nextRelativeIndent = EMPTY_LINE
            } else if (nextAbsoluteIndent == 0) {
                absoluteIndentStack.clear()
                absoluteIndentStack.push(0)
                nextRelativeIndent = 0
            } else if (nextAbsoluteIndent > absoluteIndentStack.peek()) {
                absoluteIndentStack.push(nextAbsoluteIndent)
                nextRelativeIndent = absoluteIndentStack.size() - 1
            } else {
                while (nextAbsoluteIndent < absoluteIndentStack.peek()) {
                    absoluteIndentStack.pop()
                }
                nextRelativeIndent = absoluteIndentStack.size() - 1
            }
            relativeIndents << nextRelativeIndent
        }
        relativeIndents
    }
}
