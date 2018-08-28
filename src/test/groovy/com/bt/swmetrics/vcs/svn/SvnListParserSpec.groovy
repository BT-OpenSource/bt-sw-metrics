package com.bt.swmetrics.vcs.svn

import com.bt.swmetrics.vcs.Commit
import spock.lang.Specification

import java.time.Instant

class SvnListParserSpec extends Specification {
    SvnListParser parser = new SvnListParser()

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

    def "Should be able to parse the text to yield a list of path/commit/kind/size tuples"() {
        when:
        List<Tuple> result = parser.parseToTupleList(LIST_TEXT)

        then:
        result.size() == 3

        result[0][0] == 'Backend'
        result[0][1] == new Commit(
                revision:  247217,
                author: 'alice',
                timestamp: Instant.parse('2017-06-14T07:25:43.945801Z'),
        )
        result[0][2] == 'dir'
        result[0][3] == 0

        result[1][0] == 'Backend/ASG'
        result[1][1] == new Commit(
                author: 'bob',
                revision: 246170,
                timestamp: Instant.parse('2017-05-25T10:01:49.823265Z'),
        )
        result[1][2] == 'dir'
        result[1][3] == 0

        result[2][0] == 'Backend/ASG/103_BTGCE_13790.mig'
        result[2][1] == new Commit(
                author: 'charlie',
                revision: 234837,
                timestamp: Instant.parse('2016-09-14T13:19:11.658242Z'),
        )
        result[2][2] == 'file'
        result[2][3] == 832
    }

    def "Should be able to parse file to a PathInfoMap"() {
        when:
        def result = parser.parseToPathInfoMap(new File(LIST_FILE))

        then:
        result.size() == 4
        result['Backend'].lastCommit.author == 'alice'
        result['Backend/ASG'].lastCommit.author == 'bob'

        result['Backend/ASG/103_BTGCE_13790.mig'].lastCommit.author == 'charlie'
        result['Backend/ASG/103_BTGCE_13790.mig'].lastCommit.revision == 234837
        result['Backend/ASG/103_BTGCE_13790.mig'].size == 832

        result['Backend/ASG/103_BTGCE_13790_MSI.met'].lastCommit.author == 'dan'
        result['Backend/ASG/103_BTGCE_13790_MSI.met'].lastCommit.revision == 234915
        result['Backend/ASG/103_BTGCE_13790_MSI.met'].size == 2814
    }
}