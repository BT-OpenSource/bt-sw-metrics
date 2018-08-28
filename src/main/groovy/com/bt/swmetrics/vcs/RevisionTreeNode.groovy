package com.bt.swmetrics.vcs

import groovy.transform.CompileStatic

/**
 * The RevisionTreeNode implements a persistent, immutable tree structure.
 * Although the nodes are not marked with @Immutable, for reasons of performance,
 * they are not changed after creation.
 */
@CompileStatic
class RevisionTreeNode {
    // The following value controls whether intermediate (non-leaf) nodes have a back-pointer to
    // their previous revision. Setting this true inhibits the creation of such links.
    // As we are, ultimately, only really concerned with leaf nodes in the history, omitting
    // these links allows there to be fewer object references, and more chance of the GC
    // cleaning up intermediate commits.
    static boolean linkOnlyToPreviousLeaf = true

    public final String name
    public final int revision
    public final RevisionTreeNode previous
    public final RevisionTreeNode[] children

    public final static RevisionTreeNode EMPTY = new RevisionTreeNode(null, 0, null, new RevisionTreeNode[0])

    RevisionTreeNode(String name, int revision, RevisionTreeNode previous, RevisionTreeNode[] children) {
        this.name = name
        this.revision = revision
        this.previous = previous
        this.children = children
    }

    @Override
    String toString() {
        "RevisionTreeNode(${sprintf('%x', System.identityHashCode(this))}: ${name ?: '<null>'}, r = $revision, $children, prev = ${sprintf('%x', System.identityHashCode(previous))})"
    }

    RevisionTreeNode add(String path, int newRevision = (revision + 1)) {
        def pathElements = path.split('/') as List<String>
        addWithContext(pathElements.head(), pathElements.tail(), newRevision, this)
    }

    private RevisionTreeNode addWithContext(String elementName, List<String> remainder, int newRevision, RevisionTreeNode context) {
        def elementNode = findCurrentNodeForElement(elementName, context)
        def nodeChildren = elementNode?.children ?: new RevisionTreeNode[0]
        RevisionTreeNode previousNode = linkToNodeIfRequired(elementNode)
        def nextVersionBase = new RevisionTreeNode(elementName, newRevision, previousNode, nodeChildren)
        def nextVersionFull = remainder ? nextVersionBase.addWithContext(remainder.head(), remainder.tail(), newRevision, elementNode) : nextVersionBase
        new RevisionTreeNode(name, newRevision, context, mergeWithCurrentChildren(nextVersionFull))
    }

    RevisionTreeNode linkToNodeIfRequired(RevisionTreeNode treeNode) {
        linkOnlyToPreviousLeaf && !treeNode?.isLeaf() ? null : treeNode
    }

    private RevisionTreeNode[] mergeWithCurrentChildren(RevisionTreeNode newChild) {
        if (children.size() == 0) {
            return [newChild] as RevisionTreeNode[]
        }

        int insertionIndex = findInsertionIndex(newChild)
        if (shouldReplaceChildAtInsertionIndex(insertionIndex, newChild)) {
            replaceCurrentChildInArrayCopy(insertionIndex, newChild)
        } else {
            insertNewChildIntoArrayCopy(insertionIndex, newChild)
        }
    }

    private boolean shouldReplaceChildAtInsertionIndex(int insertionIndex, RevisionTreeNode newChild) {
        insertionIndex < children.size() && children[insertionIndex].name == newChild.name
    }

    private RevisionTreeNode[] replaceCurrentChildInArrayCopy(int insertionIndex, RevisionTreeNode newChild) {
        RevisionTreeNode[] newChildren = (RevisionTreeNode[]) Arrays.copyOf(children, children.size())
        newChildren[insertionIndex] = newChild
        newChildren
    }

    private RevisionTreeNode[] insertNewChildIntoArrayCopy(int insertionIndex, RevisionTreeNode newChild) {
        RevisionTreeNode[] newChildren = new RevisionTreeNode[children.size() + 1]
        copySlice(children, 0, insertionIndex, newChildren, 0)
        newChildren[insertionIndex] = newChild
        copySlice(children, insertionIndex, children.size(), newChildren, insertionIndex + 1)
        newChildren
    }

    private int findInsertionIndex(RevisionTreeNode child) {
        int index = Arrays.binarySearch(children, child, {RevisionTreeNode a, RevisionTreeNode b -> a.name <=> b.name} as Comparator)
        if (index >= 0) {
            index
        } else {
            (-1 - index)
        }
    }

    private static copySlice(RevisionTreeNode[] from, int fromStart, int endBefore, RevisionTreeNode[] to, int toStart) {
        for (int index = 0; index < (endBefore - fromStart); index++) {
            if (fromStart + index < from.size()) {
                to[toStart + index] = from[fromStart + index]
            }
        }
    }

    private static RevisionTreeNode findCurrentNodeForElement(String element, RevisionTreeNode context) {
        context?.get(element)
    }

    RevisionTreeNode get(String path) {
        def parts = path.split('/', 2)
        if (parts.size() == 1) {
            children.find { it.name == path }
        } else {
            children.find { it.name == parts[0] }?.get(parts[1])
        }
    }

    RevisionTreeNode delete(String path, int newRevision = revision + 1) {
        if (!get(path)) {
            return this
        }

        def pathElements = path.split('/') as List<String>
        deleteWithContext(pathElements.head(), pathElements.tail(), newRevision, this)
    }

    private RevisionTreeNode deleteWithContext(String elementName, List<String> remainder, int newRevision, RevisionTreeNode context) {
        def elementNode = findCurrentNodeForElement(elementName, context)
        def elementChildren = elementNode?.children ?: new RevisionTreeNode[0]
        final modifiedChildren
        if (remainder) {
            def nextVersionBase = new RevisionTreeNode(elementName, newRevision, elementNode, elementChildren)
            def nextVersionFull = nextVersionBase.deleteWithContext(remainder.head(), remainder.tail(), newRevision, elementNode)
            modifiedChildren = mergeWithCurrentChildren(nextVersionFull)
        } else {
            modifiedChildren = filterChildrenByName(elementName)
        }
        new RevisionTreeNode(name, newRevision, context, modifiedChildren)
    }

    private RevisionTreeNode[] filterChildrenByName(String excludedName) {
        children.findAll { it.name != excludedName } as RevisionTreeNode[]
    }

    List<Integer> listRevisions(String path) {
        def node = get(path)
        List<Integer> result = []
        for (RevisionTreeNode next = node; next; next = next.previous) {
            result << next.revision
        }
        result.unique()
    }

    RevisionTreeNode copy(String sourcePath, String targetPath, int newRevision = revision + 1) {
        copy(this.get(sourcePath), targetPath, newRevision)
    }

    RevisionTreeNode copy(RevisionTreeNode sourceNode, String targetPath, int newRevision = revision + 1) {
        def pathElements = targetPath.split('/') as List<String>
        addNodeCopyWithContext(sourceNode, pathElements.head(), pathElements.tail(), newRevision, this)
    }

    private RevisionTreeNode addNodeCopyWithContext(RevisionTreeNode sourceNode, String elementName, List<String> remainder, int newRevision, RevisionTreeNode context) {
        def elementNode = findCurrentNodeForElement(elementName, context)
        def elementChildren = elementNode?.children ?: new RevisionTreeNode[0]
        final modifiedChildren
        if (remainder) {
            def nextVersionBase = new RevisionTreeNode(elementName, newRevision, elementNode, elementChildren)
            def nextVersionFull = nextVersionBase.addNodeCopyWithContext(sourceNode, remainder.head(), remainder.tail(), newRevision, elementNode)
            modifiedChildren = mergeWithCurrentChildren(nextVersionFull)
        } else {
            def copiedNode = new RevisionTreeNode(elementName, newRevision, sourceNode, sourceNode.children)
            modifiedChildren = mergeWithCurrentChildren(copiedNode)
        }
        new RevisionTreeNode(name, newRevision, context, modifiedChildren)
    }

    Iterator<String> getPathIterator() {
        new RevisionTreeNodePathIterator(this)
    }

    boolean isLeaf() {
        children.size() == 0
    }
}
