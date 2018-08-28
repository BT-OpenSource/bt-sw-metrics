package com.bt.swmetrics

import spock.lang.Specification
import spock.lang.Unroll


class PathOperationsSpec extends Specification {
    def "Longest common prefix of two identical paths should be the path"() {
        expect:
        PathOperations.longestCommonPathPrefix(['/foo', '/foo']) == '/foo'
    }

    def "Longest common prefix of anything with empty paths should be the empty path"() {
        expect:
        PathOperations.longestCommonPathPrefix(['/foo', '']) == ''
    }

    def "LCP of two overlapping paths should be the shorter"() {
        expect:
        PathOperations.longestCommonPathPrefix(['/foo', '/foo/bar']) == '/foo'
    }

    def "LCP of two disjoint paths should be empty"() {
        expect:
        PathOperations.longestCommonPathPrefix(['/foo', '/bar/foo']) == ''
    }

    def "Should be able to find the LCP for a set of non-prefixed paths"() {
        given:
        def paths = [
                'alpha/beta/gamma',
                'alpha/beta/delta',
                'alpha/epsilon',
                'alpha/beta/zeta'
        ]

        expect:
        PathOperations.longestCommonPathPrefix(paths) == 'alpha'
    }

    @Unroll
    def "Should be able to strip a prefix from a path"() {
        expect:
        PathOperations.stripPrefixFromPath(prefix, path) == expected

        where:
        prefix  | path      || expected
        '/a'    | '/a/b'    || 'b'
        'a'     | 'a/b'     || 'b'
        '/a/'   | '/a/b'    || 'b'
        '/'     | '/a/b'    || 'a/b'
        '/a'    | '/c/a/b'  || '/c/a/b'
        ''      | '/a/b'    || '/a/b'
        ''      | 'a/b'     || 'a/b'
        '/a'    | '/ab'     || '/ab'
    }

    def "LCP for a set of non-prefixed disjoint paths should be empty"() {
        given:
        def paths = [
                'alpha/beta/gamma',
                'alpha/beta/delta',
                'alpha/epsilon',
                'beta/zeta'
        ]

        expect:
        PathOperations.longestCommonPathPrefix(paths) == ''
    }

    def "Should be able to encode a relative path with /-delimiter"() {
        expect:
        PathOperations.uriEncodePath('a/b/c d') == 'a/b/c%20d'
    }

    def "Should be able to encode an absolute path with /-delimiter"() {
        expect:
        PathOperations.uriEncodePath('/a/b/c d') == '/a/b/c%20d'
    }

    def "Should be able to encode a relative path with platform delimiter"() {
        expect:
        PathOperations.uriEncodePath('a' + File.separator + 'b') == 'a/b'
    }

    def "Should be able to encode an absolute path with platform delimiter"() {
        expect:
        PathOperations.uriEncodePath(File.separator + 'a' + File.separator + 'b') == '/a/b'
    }

    def "Should be able to get the set of leaf paths (no other path is a prefix of this)"() {
        given:
        Collection paths = [
                'a',
                'a/b',
                'a/b/c',
                'a/b/d',
                'a/b/d/e',
                'a/b/d/f',
                'x',
                '/y',
                '/'
        ]

        expect:
        PathOperations.findLeafPaths(paths) == ['a/b/c', 'a/b/d/e', 'a/b/d/f', 'x', '/y', '/']
    }

    def "Should be able to CSV-quote a file path"() {
        expect:
        PathOperations.csvQuote(path) == quoted

        where:
        path                    || quoted
        'file:///foo/bar'       || '"file:///foo/bar"'
        'file:///foo,bar'       || '"file:///foo,bar"'
        'file:///foo"bar'       || '"file:///foo""bar"'
    }

    @Unroll
    def "Should be able to strip a path prefix - |#prefix|#path|"() {
        when:
        def stripped = PathOperations.stripPrefixFromPath(prefix, path)

        then:
        stripped == expected

        where:
        path            || prefix       || expected
        'alpha'         || 'alpha'      || ''
        'alpha/'        || 'alpha'      || ''
        'alpha'         || 'alpha/'     || ''
        'alpha/'        || 'alpha/'     || ''
        'alpha/beta'    || 'alpha'      || 'beta'
        'alpha/beta'    || 'alpha/'     || 'beta'
        'alpha/beta'    || 'beta'       || 'alpha/beta'
        'alphabet'      || 'alpha'      || 'alphabet'
        'alpha/'        || '/'          || 'alpha/'
        'alpha/'        || ''           || 'alpha/'
        '/alpha'        || '/'          || 'alpha'
    }

    @Unroll
    def "Should be able to strip any matching path prefix - #path"() {
        given:
        def prefixes = ['alpha', 'beta/', 'alpha/delta']

        when:
        def stripped = PathOperations.stripAnyMatchingPrefixFromPath(prefixes, path)

        then:
        stripped == expected

        where:
        path            || expected
        'alpha'         || ''
        'alpha/'        || ''
        'alpha/beta'    || 'beta'
        'alphabet'      || 'alphabet'
        'beta'          || ''
        'beta/'         || ''
        'beta/alpha'    || 'alpha'
        'gamma/alpha'   || 'gamma/alpha'
        'alpha/delta/x' || 'x'
    }

    def "Stripping with an empty prefix list should just yield the path"() {
        given:
        def path = 'a/b/c'

        when:
        def stripped = PathOperations.stripAnyMatchingPrefixFromPath([], path)

        then:
        stripped == path
    }
}