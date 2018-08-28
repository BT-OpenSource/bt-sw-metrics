package com.bt.swmetrics.vcs

import spock.lang.Specification


class RevisionTreeNodeSpec extends Specification {
    RevisionTreeNode root = RevisionTreeNode.EMPTY

    def "Initial empty TreeNode revision should be zero"() {
        expect:
        RevisionTreeNode.EMPTY.revision == 0
    }

    def "Adding a path generates a new tree from which the node can be retrieved"() {
        when:
        RevisionTreeNode tree = root.add('alpha')

        then:
        RevisionTreeNode newNode = tree.get('alpha')
        newNode.name == 'alpha'
    }

    def "Adding a path leaves the original tree unaltered"() {
        when:
        RevisionTreeNode newTree = root.add('alpha')

        then:
        root.get('alpha') == null
    }

    def "Adding a path should increase the version number"() {
        when:
        def tree1 = root.add('alpha')
        def tree2 = tree1.add('beta')

        then:
        tree1.revision == 1
        tree2.revision == 2
    }

    def "Adding multiple paths should allow all to be retrieved"() {
        when:
        def tree1 = root.add('alpha')
        def tree2 = tree1.add('beta')

        then:
        tree2.get('alpha').name == 'alpha'
    }

    def "Adding a slash-delimited path should create intermediate nodes"() {
        when:
        def newTree = root.add('alpha/beta/gamma')

        then:
        newTree.get('alpha/beta/gamma').name == 'gamma'
        newTree.get('alpha/beta').name == 'beta'
        newTree.get('alpha').name == 'alpha'
    }

    def "Added intermediate nodes should all share the same revision"() {
        when:
        def newTree = root.add('alpha/beta/gamma')

        then:
        newTree.get('alpha/beta/gamma').revision == 1
        newTree.get('alpha/beta').revision == 1
        newTree.get('alpha').revision == 1
    }

    def "Adding the same path should create multiple revisions"() {
        when:
        def tree1 = root.add('alpha/beta')
        def tree2 = tree1.add('alpha/beta')

        then:
        tree2.get('alpha').revision > tree1.get('alpha').revision
        tree2.get('alpha/beta').revision > tree1.get('alpha/beta').revision
    }

    def "Adding different paths under the same root should preserve all paths"() {
        when:
        def tree1 = root.add('alpha/beta', 1)
        def tree2 = tree1.add('alpha/gamma', 2)

        then:
        tree2.get('alpha').revision > tree1.get('alpha').revision
        tree2.get('alpha/gamma').revision == 2
        tree2.get('alpha/beta').revision == 1
    }

    def "Adding the same path should result in a pointer to the previous version"() {
        when:
        def tree1 = root.add('alpha/beta')
        def tree2 = tree1.add('alpha/beta')

        then:
        tree2.get('alpha').previous.revision == 1
        tree2.get('alpha').previous.name == 'alpha'
        tree2.get('alpha/beta').previous.revision == 1
        tree2.get('alpha/beta').previous.name == 'beta'
    }

    def "Adding several paths with the same revision should preserve the earliest relevant prior references"() {
        when:
        def tree11 = root.add('alpha/beta', 1)
        def tree12 = tree11.add('alpha/gamma', 1)
        def tree21 = tree12.add('alpha/beta', 2)
        def tree22 = tree21.add('alpha/gamma', 2)

        then:
        tree22.get('alpha/beta').previous.is(tree11.get('alpha/beta'))
        tree22.get('alpha/gamma').previous.is(tree12.get('alpha/gamma'))
    }

    def "Deleting non-existent element from tree should yield original tree"() {
        given:
        def tree1 = root.add('alpha/beta')

        expect:
        tree1.delete('no-such/path') == tree1
    }

    def "Should be possible to delete from a tree"() {
        given:
        def full = root.add('alpha/beta/gamma').add('alpha/delta').add('alpha/beta/epsilon')

        when:
        def deleted = full.delete('alpha/beta')

        then:
        deleted.get('alpha/beta') == null
        deleted.get('alpha/beta/gamma') == null
        deleted.get('alpha/delta') == full.get('alpha/delta')
    }

    def "Should be able to get list of revisions of a modified path"() {
        given:
        def tree = root
                .add('alpha/beta', 1)
                .add('alpha/gamma', 2)
                .add('alpha/epsilon', 3)
                .add('alpha/beta', 4)
                .add('alpha/gamma', 5)
        expect:
        tree.listRevisions('alpha/beta') == [4, 1]
        tree.listRevisions('alpha') == [5, 4, 3, 2, 1]
    }

    def "Revision list for a non-existent path should be empty"() {
        expect:
        root.listRevisions('some-path') == []
    }

    def "Intermediate revisions with the same number should be compressed"() {
        given:
        def tree = root
                .add('alpha/beta', 1)
                .add('alpha/gamma', 2)
                .add('alpha/epsilon', 3)
                .add('alpha/beta', 3)
                .add('alpha/gamma', 4)
                .add('alpha/zeta/eta', 5)
                .add('alpha/zeta/theta', 5)
                .add('alpha/zeta/iota', 5)

        expect:
        tree.listRevisions('alpha/beta') == [3, 1]
        tree.listRevisions('alpha/zeta') == [5]
        tree.listRevisions('alpha') == [5, 4, 3, 2, 1]
    }

    def "Deleted revisions should not show up in the list"() {
        def tree = root
                .add('alpha/beta', 1)
                .add('alpha/gamma', 2)
                .delete('alpha/beta', 3)
                .add('alpha/beta', 4)
                .add('alpha/gamma', 5)

        expect:
        tree.listRevisions('alpha/beta') == [4]
        tree.listRevisions('alpha/gamma') == [5, 2]
        tree.listRevisions('alpha') == [5, 4, 3, 2, 1]
    }

    def "Should be able to copy a sub-tree"() {
        given:
        def source = root.add('alpha/beta/gamma', 1).add('alpha/beta/delta', 2)

        when:
        def copied = source.copy(source.get('alpha/beta'), 'alpha/epsilon', 3)

        then:
        copied.get('alpha/epsilon/gamma').revision == 1
        copied.get('alpha/epsilon/delta').revision == 2
        copied.listRevisions('alpha/epsilon') == [3, 2, 1]
    }

    def "Should be able to copy a sub-tree by name"() {
        given:
        def source = root.add('alpha/beta/gamma', 1).add('alpha/beta/delta', 2)

        when:
        def copied = source.copy('alpha/beta', 'epsilon', 3)

        then:
        copied.get('epsilon').revision == 3
        copied.get('epsilon/gamma').revision == 1
        copied.get('epsilon/delta').revision == 2
        copied.listRevisions('epsilon') == [3, 2, 1]
    }

    def "Should be able to copy a sub-tree from a previous version"() {
        given:
        def original = root.add('alpha/beta/gamma', 1).add('alpha/beta/delta', 2)
        def modified = original.add('alpha/beta/epsilon', 3)

        when:
        def copied = modified.copy(original.get('alpha/beta'), 'alpha/beta', 4)

        then:
        copied.get('alpha/beta/gamma').revision == 1
        copied.get('alpha/beta/delta').revision == 2
        copied.get('alpha/beta/epsilon') == null
        copied.listRevisions('alpha/beta') == [4, 2, 1]
        modified.listRevisions('alpha/beta') == [3, 2, 1]
    }

    def "Should be able to determine if a node is a leaf"() {
        given:
        def tree = root.add('a/b/c')

        expect:
        tree.get('a/b/c').isLeaf()
        !tree.get('a/b').isLeaf()
        !tree.get('a').isLeaf()
    }

    def "Should be able to generate an iterator for all paths"() {
        given:
        def modified = root
                .add('alpha/beta', 1)
                .add('alpha/gamma', 2)
                .add('alpha/epsilon', 3)
                .add('alpha/beta', 3)
                .add('alpha/gamma', 4)
                .add('alpha/zeta/eta', 5)
                .add('alpha/zeta/theta', 5)
                .add('alpha/zeta/iota', 5)
                .copy('alpha/zeta', 'alpha/omega', 7)
                .add('xyzzy',8)

        when:
        Iterator<String> iterator = modified.pathIterator

        then:
        iterator.collect().sort() == [
                'alpha',
                'alpha/beta',
                'alpha/epsilon',
                'alpha/gamma',
                'alpha/omega',
                'alpha/omega/eta',
                'alpha/omega/iota',
                'alpha/omega/theta',
                'alpha/zeta',
                'alpha/zeta/eta',
                'alpha/zeta/iota',
                'alpha/zeta/theta',
                'xyzzy'
        ]
    }
}