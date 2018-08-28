package com.bt.swmetrics.vcs.git

import com.bt.swmetrics.vcs.svn.SvnDiffParser
import spock.lang.Specification


class GitDiffParserSpec extends Specification {
    final static List<String> DIFF_LINES = new File('src/test/resources/git.diff').readLines()
    final static PATH_INFO = [
            'src/main/groovy/com/bt/swmetrics/vcs/DiffParser.groovy': [lines: 38, chunks: 1],
            'src/main/groovy/com/bt/swmetrics/vcs/DiffStatsReporter.groovy': [lines: 19, chunks: 2],
    ]

    GitDiffParser parser = new GitDiffParser(lines: DIFF_LINES)

    def "Should be able to obtain a list of all paths from the Index lines"() {
        expect:
        parser.paths as Set == PATH_INFO.keySet()
    }

    def "Should be able to split lines by path"() {
        expect:
        parser.linesByPath.each { path, lines ->
            assert PATH_INFO[path].lines == lines.size()
        }
    }

    def "Should be able to get info for each @@-delimited chunk per file"() {
        when:
        def chunks = parser.chunksByPath

        then:
        PATH_INFO.each { path, info ->
            assert chunks[path].size() == info.chunks
        }
    }

}