package com.bt.swmetrics.vcs.svn

import com.bt.swmetrics.Configurator
import com.bt.swmetrics.vcs.AuthorStats
import com.bt.swmetrics.vcs.Commit
import com.bt.swmetrics.vcs.PathCommitRecord
import com.bt.swmetrics.vcs.RevisionCollection
import spock.lang.Specification

import java.time.Instant

class SvnLogParserSpec extends Specification {
    static final FULL_LOG_FILE = 'src/test/resources/svn.log'
    Configurator stubConfigurator
    SvnLogParser parser

    def setup() {
        stubConfigurator = Stub(Configurator)
        stubConfigurator.ignorePrefixes >> []
        parser = new SvnLogParser(configurator: stubConfigurator)
    }

    def "Should be able to parse the text to yield a list of PathCommitEntry  - paths with initial slash stripped"() {
        given:
        def text = '''<?xml version="1.0" encoding="UTF-8"?>
<log>
    <logentry revision="4412">
        <author>simon</author>
        <date>2016-01-06T15:01:04.364200Z</date>
        <paths>
            <path kind="" action="A">/trunk/src/Shell Scripts/frontendtest.sh</path>
        </paths>
        <msg>use mail command instead of mailx</msg>
    </logentry>
    <logentry revision="4401">
        <author>steve</author>
        <date>2015-12-22T14:21:27.443167Z</date>
        <paths>
            <path kind="" action="M">/trunk/build/SWNS/PACKAGES/ADMIN_DEFECTS.pkb</path>
            <path action="M" kind="">/trunk/build/SWNS/PACKAGES/ADMIN_DEFECTS.pks</path>            
        </paths>
        <msg>SWNS-173, bug fix for SWNS-119, HSPM C100119546, Bridge Ref BW307203</msg>
    </logentry>
</log>
'''
        when:
        List<PathCommitRecord> result = parser.parseToPathCommitRecordList(text)

        then:
        result.size() == 3

        result[0].path == 'trunk/src/Shell Scripts/frontendtest.sh'
        result[0].commit == new Commit(
                revision:  4412,
                author: 'simon',
                timestamp: Instant.parse('2016-01-06T15:01:04.364200Z'),
                action: 'A'
        )

        result[1].path == 'trunk/build/SWNS/PACKAGES/ADMIN_DEFECTS.pkb'
        result[1].commit == new Commit(
                author: 'steve',
                revision: 4401,
                timestamp: Instant.parse('2015-12-22T14:21:27.443167Z'),
                action: 'M'
        )
        result[2].path == 'trunk/build/SWNS/PACKAGES/ADMIN_DEFECTS.pks'
        result[2].commit == new Commit(
                author: 'steve',
                revision: 4401,
                timestamp: Instant.parse('2015-12-22T14:21:27.443167Z'),
                action: 'M'
        )
    }

    def "Entries with copy-from details should add path and revision to the tuple"() {
        given:
        def text = '''<?xml version="1.0" encoding="UTF-8"?>
<log>
    <logentry revision="4402">
        <author>steve</author>
        <date>2015-12-22T18:39:50.562712Z</date>
        <paths>
            <path kind="" action="M">/trunk/build/SWNS/PACKAGES/ADMIN_PERMITS_DB.pks</path>
            <path
               kind="" action="D">/trunk/mbe2/scripts/runMBE_ST.sh</path>
            <path action="R" kind=""
               copyfrom-path="/branches/mbe-geohub-authentication/settings.xml"
               copyfrom-rev="4373">/trunk/mbe2/settings.xml</path>

        </paths>
        <msg>SWNS-173, bug fix for SWNS-119, HSPM C100119546, Bridge Ref BW307203</msg>
    </logentry>
</log>'''

        when:
        def result = parser.parseToPathCommitRecordList(text)

        then:
        result[2].path == 'trunk/mbe2/settings.xml'
        result[2].copyPath == 'branches/mbe-geohub-authentication/settings.xml'
        result[2].copyRevision == 4373
    }

    def "Ignored prefixes should be stripped from both main and copy-from paths"() {
        given:
        def text = '''<?xml version="1.0" encoding="UTF-8"?>
<log>
    <logentry revision="4402">
        <author>steve</author>
        <date>2015-12-22T18:39:50.562712Z</date>
        <paths>
            <path kind="" action="M">/trunk/build/SWNS/PACKAGES/ADMIN_PERMITS_DB.pks</path>
            <path
               kind="" action="D">/trunk/mbe2/scripts/runMBE_ST.sh</path>
            <path action="R" kind=""
               copyfrom-path="/branches/mbe-geohub-authentication/settings.xml"
               copyfrom-rev="4373">/trunk/mbe2/settings.xml</path>

        </paths>
        <msg>SWNS-173, bug fix for SWNS-119, HSPM C100119546, Bridge Ref BW307203</msg>
    </logentry>
</log>'''
        stubConfigurator = Stub(Configurator)
        stubConfigurator.ignorePrefixes >> ['/trunk', '/branches']
        parser.configurator = stubConfigurator

        when:
        def result = parser.parseToPathCommitRecordList(text)

        then:
        result[2].path == 'mbe2/settings.xml'
        result[2].copyPath == 'mbe-geohub-authentication/settings.xml'
        result[2].copyRevision == 4373
    }

    def "Should be able include/exclude files in PathCommitRecord list"() {
        given:
        def file = new File(FULL_LOG_FILE)
        stubConfigurator.includedPatterns >> [/.*ADMIN_.*/]
        stubConfigurator.excludedPatterns >> [/.*\.pkb$/]

        when:
        List<PathCommitRecord> records = parser.parseToPathCommitRecordList(file)

        then:
        records.collect { it.path }.sort().unique() == [
                'trunk/build/SWNS/PACKAGES/ADMIN_DEFECTS.pks',
                'trunk/build/SWNS/PACKAGES/ADMIN_MISC.pks',
                'trunk/build/SWNS/PACKAGES/ADMIN_PERMITS_DB.pks',
                'trunk/build/SWNS/PACKAGES/ADMIN_S74.pks',
        ]
    }

    def "Should be able to parse file to PathHistoryCollection"() {
        given:
        def file = new File(FULL_LOG_FILE)

        when:
        RevisionCollection collection = parser.parseToRevisionCollection(file)

        then:
        collection.leafPathCommitRecords.collect { it.path }.sort() == [
                'trunk/build/SWNS/PACKAGES/ADMIN_DEFECTS.pkb',
                'trunk/build/SWNS/PACKAGES/ADMIN_DEFECTS.pks',
                'trunk/build/SWNS/PACKAGES/ADMIN_MISC.pkb',
                'trunk/build/SWNS/PACKAGES/ADMIN_MISC.pks',
                'trunk/build/SWNS/PACKAGES/ADMIN_PERMITS_DB.pkb',
                'trunk/build/SWNS/PACKAGES/ADMIN_PERMITS_DB.pks',
                'trunk/build/SWNS/PACKAGES/ADMIN_S74.pkb',
                'trunk/build/SWNS/PACKAGES/ADMIN_S74.pks',
                'trunk/mbe2/settings.xml',
                'trunk/src/Shell Scripts/frontendtest.sh',
        ]
        def entry = collection.fullPathHistoryMap['trunk/src/Shell Scripts/frontendtest.sh']
        entry.revisions == [4412, 4411, 4410, 4409, 4408, 4407, 4406, 4405, 4404, 4403]
    }

    def "Should be able to parse file to map of author activities"() {
        given:
        def file = new File(FULL_LOG_FILE)

        when:
        Map<String,AuthorStats> map = parser.parseToAuthorStatsMap(file)

        then:
        map.size() == 3
        map['simon'].commitTimestamps.size() == 9
        map['simon'].pathModificationCounts.size() == 9
        map['simon'].tenure == 14
        map['luke'].commitTimestamps.size() == 1
        map['steve'].commitTimestamps.size() == 2
        map['steve'].pathCommits == [
                'trunk/build/SWNS/PACKAGES/ADMIN_PERMITS_DB.pks': 2,
                'trunk/mbe2/scripts/runMBE_ST.sh': 1,
                'trunk/mbe2/settings.xml': 1,
                'trunk/build/SWNS/PACKAGES/ADMIN_DEFECTS.pkb': 1,
                'trunk/build/SWNS/PACKAGES/ADMIN_DEFECTS.pks': 1,
                'trunk/build/SWNS/PACKAGES/ADMIN_MISC.pkb': 1,
                'trunk/build/SWNS/PACKAGES/ADMIN_MISC.pks': 1,
                'trunk/build/SWNS/PACKAGES/ADMIN_PERMITS_DB.pkb': 1,
                'trunk/build/SWNS/PACKAGES/ADMIN_S74.pkb': 1,
                'trunk/build/SWNS/PACKAGES/ADMIN_S74.pks' :1
        ]

        map['luke'].pathCommits == ['trunk/src/Shell Scripts/frontendtest.sh': 1]
    }
}