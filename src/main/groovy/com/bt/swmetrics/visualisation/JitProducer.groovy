package com.bt.swmetrics.visualisation

class JitProducer implements VisualisationProducer {
    {
        resourceNames = [
                htmlTemplate      : 'treemap-jit-template.html',
                jsLibrary1        : 'jit-yc.js',
                initialisationCode: 'init-jit.js',
                styleSheet        : 'treemap-jit.css'
        ]
    }

    JitTreeNode root = new JitTreeNode(path: '')

    def addData(String path, BigDecimal size, BigDecimal colour, List extra) {
        root.levelLimit = levelLimit
        return root.addChild(path, size, colour, extra)
    }

    def toJsonable() {
        root.toJsonable(partitionSize, topThreshold, bottomThreshold, topColourValue, bottomColourValue)
    }
}
