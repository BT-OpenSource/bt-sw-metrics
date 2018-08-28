package com.bt.swmetrics.visualisation

import com.bt.swmetrics.PathOperations

import java.math.RoundingMode

class JitTreeNode {
    static nodeId = 0

    private String path
    private String name
    BigDecimal size = 0.0
    BigDecimal colourMetric = 0.0
    BigDecimal maxColourMetric = 0.0
    List<JitTreeNode> children = []
    JitTreeNode parent
    List extra = []
    int levelLimit = 0

    JitTreeNode addChild(String childPath, BigDecimal sizeMetric, BigDecimal colourMetric, List extraData = []) {
        def node = new JitTreeNode(levelLimit: levelLimit)
        node.path = childPath
        node.size = sizeMetric
        node.colourMetric = colourMetric
        node.extra = extraData
        addNodeToAppropriateParent(node.path, node)
        node
    }

    private void addNodeToAppropriateParent(String childPath, JitTreeNode node) {
        def relativePathComponents = (childPath - this.path).split('/', levelLimit).grep()
        def parent = findOrCreateRelativeParent(relativePathComponents)
        parent.children << node
        node.parent = parent
        node.recalculateParentSizesAndColours(node.size, node.colourMetric)
        node
    }

    def recalculateParentSizesAndColours(BigDecimal size, BigDecimal colour) {
        if (parent && size != 0) {
            parent.colourMetric = ((parent.colourMetric * parent.size) + (colour * size)) / (parent.size + size)
            parent.size += size
            parent.maxColourMetric = Math.max(parent.maxColourMetric, colour)
            parent.recalculateParentSizesAndColours(size, colour)
        }
    }

    JitTreeNode findOrCreateRelativeParent(List<String> relativePathComponents) {
        if (relativePathComponents.size() > 1) {
            def parent = children.find { it.name == relativePathComponents[0] }
            if (!parent) {
                parent = new JitTreeNode(path: this.path + (this.path ? '/' : '') + relativePathComponents[0], parent: this)
                children << parent
            }
            return parent.findOrCreateRelativeParent(relativePathComponents[1 .. -1])
        } else {
            return this
        }
    }

    String setPath(String newPath) {
        this.path = encodePathIfNecessary(newPath)
        this.name = path.split('/', levelLimit)[-1]
    }

    private static String encodePathIfNecessary(String path) {
        try {
            new URI(path)
            path
        } catch (URISyntaxException) {
            PathOperations.uriEncodePath(path)
        }
    }

    String getPath() {
        this.path
    }

    String getName() {
        this.name
    }

    Map toJsonable(int partitionSize, BigDecimal topThreshold, BigDecimal bottomThreshold, int topColourValue = 0xff0000, int bottomColourValue = 0x00ff00) {
        def childData = children.collect {
            it.toJsonable(partitionSize, topThreshold, bottomThreshold, topColourValue, bottomColourValue)
        }

        def partitionedChildData = partitionChildDataIfNecessary(partitionSize, childData)

        BigDecimal rebasedMetric = Math.max(colourMetric, bottomThreshold) - bottomThreshold
        BigDecimal scaledMetric = Math.min(rebasedMetric, topThreshold) / (topThreshold - bottomThreshold)
        int scaledColourValue = scaleColour(scaledMetric, bottomColourValue, topColourValue)

        BigDecimal rebasedMaxMetric =  Math.max(maxColourMetric, bottomThreshold) - bottomThreshold
        BigDecimal scaledMaxMetric = Math.min(rebasedMaxMetric, topThreshold) / (topThreshold - bottomThreshold)
        int scaledMaxColourValue = scaleColour(scaledMaxMetric, bottomColourValue, topColourValue)
        
        def colourString = sprintf("#%06x", scaledColourValue)
        def maxColourString = sprintf("#%06x", scaledMaxColourValue)
        def encodedPath = path.contains('%') ? path : PathOperations.uriEncodePath(path)
        def encodedName = name.contains('%') ? name : PathOperations.uriEncodePath(name)
        def result = [
                children: partitionedChildData,
                id: "ID-${nodeId++}",
                name: new URI(encodedName).path,
                data: [
                        '$area': size,
                        '$color': colourString,
                        c: colourMetric.setScale(3,RoundingMode.HALF_EVEN),
                        mc: maxColourMetric.setScale(3, RoundingMode.HALF_EVEN),
                        ms: maxColourString,
                        p: encodedPath

                ]
        ]
        if (extra) {
            result.data.x = extra
        }
        result
    }

    private static int scaleColour(BigDecimal scale, int bottomColour, int topColour) {
        def (bottomRed, bottomGreen, bottomBlue) = splitIntegerToRgb(bottomColour)
        def (topRed, topGreen, topBlue) = splitIntegerToRgb(topColour)
        def red = scaleColourComponents(scale, bottomRed, topRed)
        def green = scaleColourComponents(scale, bottomGreen, topGreen)
        def blue = scaleColourComponents(scale, bottomBlue, topBlue)
        (red << 16) | (green << 8) | blue
    }

    private static List<Integer> splitIntegerToRgb(int colour) {
        [((colour >> 16) & 0xff), ((colour >> 8) & 0xff), (colour & 0xff)]
    }

    private static int scaleColourComponents(BigDecimal scale, int bottom, int top) {
        def scaledValue = scale * top + (1.0 - scale) * bottom
        scaledValue = scaledValue > 255 ? 255 : scaledValue
        scaledValue < 0 ? 0 : scaledValue
    }

    def partitionChildDataIfNecessary(int partitionSize, childData) {
        if (childData.size() <= partitionSize) {
            return childData
        }

        def sorted = childData.sort { a, b -> b.data.'$area' <=> a.data.'$area' }
        def biggest = sorted.take(partitionSize)
        def remainder = partitionChildDataIfNecessary(partitionSize, sorted.drop(partitionSize))
        def remainderSize = remainder.inject(0) { sum, next -> sum + next.data.'$area' }
        biggest + [
                children: remainder,
                id: "ID-${nodeId++}",
                name: "${childData.size() - partitionSize} smaller items ...",
                data: [
                        '$area': remainderSize,
                        '$color': '#7f7f7f',
                        p: path,
                ]
        ]
    }
}
