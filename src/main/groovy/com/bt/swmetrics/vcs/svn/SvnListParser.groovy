package com.bt.swmetrics.vcs.svn

import com.bt.swmetrics.vcs.Commit
import com.bt.swmetrics.vcs.ListParser
import com.bt.swmetrics.vcs.PathCommitRecord
import groovy.util.slurpersupport.GPathResult

import java.time.Instant

class SvnListParser extends ListParser {

    @Override
    List<PathCommitRecord> parseToPathCommitRecordList(String text) {
        def xml = new XmlSlurper().parseText(text)
        parseXml(xml)
    }

    @Override
    List<PathCommitRecord> parseToPathCommitRecordList(File file) {
        def xml = new XmlSlurper().parse(file)
        parseXml(xml)
    }

    private List<PathCommitRecord> parseXml(GPathResult xml) {
        xml.list.entry.findAll { checkIfPathIncluded(stripPrefixes(it.name.text())) }.collect { entry ->
            def name = stripPrefixes(entry.name.text())
            def author = entry.commit.author.text()
            def date = Instant.parse(entry.commit.date.text())
            def revision = Integer.parseInt(entry.commit.@revision.text())
            def kind = entry.@kind.text()
            def size = (kind == 'file') ? Long.parseLong(entry.size.text()) : 0

            new PathCommitRecord(name, new Commit(revision, author, date, null), size)
        }
    }
}
