package com.bt.swmetrics.vcs

import spock.lang.Specification


class PathInfoMapSpec extends Specification {
    def "Should be able to merge history collection into map entries"() {
        given:
        PathHistoryCollection collection = new PathHistoryCollection()
        collection.addCommit('foo/bar/a.java', new Commit(revision: 1, author: 'alice', action: 'A'))
        collection.addCommit('foo/bar/baz', new Commit(revision: 2, author: 'bob', action: 'A'))
        collection.addCommit('foo/bar/baz/b.java', new Commit(revision: 3, author: 'bob', action: 'A'))

        and:
        PathInfoMap map = new PathInfoMap()
        map['foo/bar/a.java'] = new PathInfo(lastCommit: new Commit(revision: 1, author: 'alice'), size: 42)
        map['foo/bar/baz/b.java'] = new PathInfo(lastCommit: new Commit(revision: 3, author: 'bob'), size: 123)

        when:
        map.mergeHistory(collection)

        then:
        map.size() == 2
        map['foo/bar/a.java'].history.commitTotal == 1
        map['foo/bar/baz/b.java'].history.commitTotal == 1
    }

    def "Should be able to strip prefixes when merging"() {
        given:
        PathHistoryCollection collection = new PathHistoryCollection()
        collection.addCommit('/trunk/foo/bar/a.java', new Commit(revision: 1, author: 'alice', action: 'A'))
        collection.addCommit('/trunk/foo/bar/baz', new Commit(revision: 2, author: 'bob', action: 'A'))
        collection.addCommit('/trunk/foo/bar/baz/b.java', new Commit(revision: 3, author: 'bob', action: 'A'))

        and:
        PathInfoMap map = new PathInfoMap(stripPrefixes: ['../', '/trunk/'])
        map['../foo/bar/a.java'] = new PathInfo(lastCommit: new Commit(revision: 1, author: 'alice'), size: 42)
        map['../foo/bar/baz/b.java'] = new PathInfo(lastCommit: new Commit(revision: 3, author: 'bob'), size: 123)

        when:
        map.mergeHistory(collection)

        then:
        map.size() == 2
        map['../foo/bar/a.java'].history.commitTotal == 1
        map['../foo/bar/baz/b.java'].history.commitTotal == 1
    }

    def "Unstripped prefixes should be preserved"() {
        given:
        PathHistoryCollection collection = new PathHistoryCollection()
        collection.addCommit('/trunk/foo/bar/a.java', new Commit(revision: 1, author: 'alice', action: 'A'))
        collection.addCommit('/trunk/foo/bar/baz', new Commit(revision: 2, author: 'bob', action: 'A'))
        collection.addCommit('/trunk/foo/bar/baz/b.java', new Commit(revision: 3, author: 'bob', action: 'A'))
        collection.addCommit('/branches/xyzzy.java', new Commit(revision: 4, author: 'bob', action: 'A'))

        and:
        PathInfoMap map = new PathInfoMap(stripPrefixes: ['../', '/trunk/'])
        map['../foo/bar/a.java'] = new PathInfo(lastCommit: new Commit(revision: 1, author: 'alice'), size: 42)
        map['../foo/bar/baz/b.java'] = new PathInfo(lastCommit: new Commit(revision: 3, author: 'bob'), size: 123)
        map['/branches/xyzzy.java'] = new PathInfo(lastCommit: new Commit(revision: 4, author: 'bob'), size: 567)

        when:
        map.mergeHistory(collection)

        then:
        map.size() == 3
        map['../foo/bar/a.java'].history.commitTotal == 1
        map['../foo/bar/baz/b.java'].history.commitTotal == 1
        map['/branches/xyzzy.java'].history.commitTotal == 1
    }

    def "Prefixes should have implicit trailing / if omitted"() {
        given:
        PathHistoryCollection collection = new PathHistoryCollection()
        collection.addCommit('/trunk/foo/bar/a.java', new Commit(revision: 1, author: 'alice', action: 'A'))
        collection.addCommit('/trunk/foo/bar/baz', new Commit(revision: 2, author: 'bob', action: 'A'))
        collection.addCommit('/trunk/foo/bar/baz/b.java', new Commit(revision: 3, author: 'bob', action: 'A'))
        collection.addCommit('/trunky/foo/bar/baz/b.java', new Commit(revision: 4, author: 'bob', action: 'A'))

        and:
        PathInfoMap map = new PathInfoMap(stripPrefixes: ['..', '/trunk'])
        map['../foo/bar/a.java'] = new PathInfo(lastCommit: new Commit(revision: 1, author: 'alice'), size: 42)
        map['../foo/bar/baz/b.java'] = new PathInfo(lastCommit: new Commit(revision: 3, author: 'bob'), size: 123)
        map['..y/foo/bar/baz/b.java'] = new PathInfo(lastCommit: new Commit(revision: 5, author: 'bob'), size: 567)

        when:
        map.mergeHistory(collection)

        then:
        map.size() == 3
        map['../foo/bar/a.java'].history.commitTotal == 1
        map['../foo/bar/baz/b.java'].history.commitTotal == 1
        map['..y/foo/bar/baz/b.java'].history == null
    }
}