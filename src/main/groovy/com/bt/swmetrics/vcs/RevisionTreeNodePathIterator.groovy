package com.bt.swmetrics.vcs

import groovy.transform.CompileStatic

@CompileStatic
class RevisionTreeNodePathIterator implements Iterator<String> {

    class Context {
        Context(RevisionTreeNode node, int childIndex, String pathPrefix) {
            this.node = node
            this.childIndex = childIndex
            this.pathPrefix = pathPrefix
        }

        RevisionTreeNode node
        int childIndex
        String pathPrefix
    }

    Stack<Context> contextStack

    RevisionTreeNodePathIterator(RevisionTreeNode root) {
        contextStack = new Stack<Context>()
        contextStack.push(new Context(root, 0, ''))
    }

    @Override
    boolean hasNext() {
        def context = contextStack.peek()
        contextStack.size() > 1 || context.childIndex < context.node.children.size()
    }

    @Override
    String next() {
        def context = contextStack.pop()
        if (isLeafNode(context) || !nodeHasMoreChildren(context)) {
            createPathWithoutLeadingSlash(context)
        } else {
            def nextNode = context.node.children[context.childIndex++]
            contextStack.push(context)
            contextStack.push(new Context(nextNode, 0, createSubPrefixWithoutNulls(context)))
            next()
        }
    }

    private boolean isLeafNode(Context context) {
        context.node.isLeaf()
    }

    private boolean nodeHasMoreChildren(Context context) {
        context.childIndex < context.node.children.size()
    }

    private String createSubPrefixWithoutNulls(Context context) {
        context.pathPrefix + (context.node.name ? '/' + context.node.name : '')
    }

    private String createPathWithoutLeadingSlash(Context context) {
        (context.pathPrefix + '/' + context.node.name) - '/'
    }
}