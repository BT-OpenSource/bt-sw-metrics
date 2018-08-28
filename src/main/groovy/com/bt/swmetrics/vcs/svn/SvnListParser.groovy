package com.bt.swmetrics.vcs.svn

import com.bt.swmetrics.vcs.Commit
import com.bt.swmetrics.vcs.ListParser
import com.bt.swmetrics.vcs.PathInfo
import com.bt.swmetrics.vcs.PathInfoMap

import java.time.Instant

class SvnListParser extends ListParser {

    @Override
    List<Tuple> parseToTupleList(String text) {
        def xml = new XmlSlurper().parseText(text)
        xml.list.entry.collect { entry ->
            def name = entry.name.text()
            def author = entry.commit.author.text()
            def date = Instant.parse(entry.commit.date.text())
            def revision = Integer.parseInt(entry.commit.@revision.text())
            def kind = entry.@kind.text()
            def size = (kind == 'file') ? Long.parseLong(entry.size.text()) : 0

            new Tuple(name, new Commit(revision, author, date), kind, size)
        }
    }

    @Override
    PathInfoMap parseToPathInfoMap(File file) {
        def parsed = parseToTupleList(file.text)
        parsed.collectEntries { String path, Commit commit, String kind, long size ->
            [(path): new PathInfo(lastCommit: commit, size: size)]
        } as PathInfoMap
    }
}
