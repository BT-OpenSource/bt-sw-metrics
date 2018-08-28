package com.bt.swmetrics.vcs.svn

import com.bt.swmetrics.Configurator
import com.bt.swmetrics.vcs.Commit
import com.bt.swmetrics.vcs.PathCommitRecord
import spock.lang.Specification

import java.time.Instant

class SvnListParserSpec extends Specification {
    Configurator stubConfigurator
    SvnListParser parser

    def setup() {
        stubConfigurator = Stub(Configurator)
        parser = new SvnListParser(configurator: stubConfigurator)
    }

    static final String LIST_FILE = 'src/test/resources/svn.list'
    static final String LIST_TEXT = '''<?xml version="1.0" encoding="UTF-8"?>
<lists>
<list
   path="https://server/svn/repo/trunk">
<entry
   kind="dir">
<name>Backend</name>
<commit
   revision="247217">
<author>alice</author>
<date>2017-06-14T07:25:43.945801Z</date>
</commit>
</entry>
<entry
   kind="dir">
<name>Backend/ASG</name>
<commit
   revision="246170">
<author>bob</author>
<date>2017-05-25T10:01:49.823265Z</date>
</commit>
</entry>
<entry
   kind="file">
<name>Backend/ASG/103_BTGCE_13790.mig</name>
<size>832</size>
<commit
   revision="234837">
<author>charlie</author>
<date>2016-09-14T13:19:11.658242Z</date>
</commit>
</entry>
</list>
</lists>'''

    def "Should be able to parse the text to yield a list of PathCommitEntry"() {
        when:
        List<PathCommitRecord> result = parser.parseToPathCommitRecordList(LIST_TEXT)

        then:
        result.size() == 3

        result[0].path == 'Backend'
        result[0].commit == new Commit(
                revision:  247217,
                author: 'alice',
                timestamp: Instant.parse('2017-06-14T07:25:43.945801Z'),
        )
        result[0].size == 0

        result[1].path == 'Backend/ASG'
        result[1].commit == new Commit(
                author: 'bob',
                revision: 246170,
                timestamp: Instant.parse('2017-05-25T10:01:49.823265Z'),
        )
        result[1].size == 0

        result[2].path == 'Backend/ASG/103_BTGCE_13790.mig'
        result[2].commit == new Commit(
                author: 'charlie',
                revision: 234837,
                timestamp: Instant.parse('2016-09-14T13:19:11.658242Z'),
        )
        result[2].size == 832
    }

    def "Should be able to parse file to a list of PathCommitEntry"() {
        when:
        def list = parser.parseToPathCommitRecordList(new File(LIST_FILE))

        then:
        list.size() == 4
        Map<String,PathCommitRecord> result = list.collectEntries { [(it.path): it] }
        result['Backend'].commit.author == 'alice'
        result['Backend/ASG'].commit.author == 'bob'

        result['Backend/ASG/103_BTGCE_13790.mig'].commit.author == 'charlie'
        result['Backend/ASG/103_BTGCE_13790.mig'].commit.revision == 234837
        result['Backend/ASG/103_BTGCE_13790.mig'].size == 832

        result['Backend/ASG/103_BTGCE_13790_MSI.met'].commit.author == 'dan'
        result['Backend/ASG/103_BTGCE_13790_MSI.met'].commit.revision == 234915
        result['Backend/ASG/103_BTGCE_13790_MSI.met'].size == 2814
    }

    def "Should strip prefixes if configured"() {
        given:
        stubConfigurator = Stub(Configurator)
        stubConfigurator.ignorePrefixes >> ['Backend']
        parser.configurator = stubConfigurator

        when:
        def result = parser.parseToPathCommitRecordList(new File(LIST_FILE))
        Map<String,PathCommitRecord> map = result.collectEntries { [(it.path): it] }

        then:
        map.size() == 4
        map[''].commit.author == 'alice'
        map['ASG'].commit.author == 'bob'

        map['ASG/103_BTGCE_13790.mig'].commit.author == 'charlie'
        map['ASG/103_BTGCE_13790.mig'].commit.revision == 234837
        map['ASG/103_BTGCE_13790.mig'].size == 832

        map['ASG/103_BTGCE_13790_MSI.met'].commit.author == 'dan'
        map['ASG/103_BTGCE_13790_MSI.met'].commit.revision == 234915
        map['ASG/103_BTGCE_13790_MSI.met'].size == 2814
    }

    def "Should be able to include/exclude paths"() {
        given:
        stubConfigurator.includedPatterns >> [/.*BTGCE.*/]
        stubConfigurator.excludedPatterns >> [/.*\.met$/]

        when:
        def result = parser.parseToPathCommitRecordList(new File(LIST_FILE))

        then:
        result.size() == 1
        result[0].path == 'Backend/ASG/103_BTGCE_13790.mig'
    }
}