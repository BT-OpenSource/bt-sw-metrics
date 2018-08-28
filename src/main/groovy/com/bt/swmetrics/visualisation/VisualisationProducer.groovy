package com.bt.swmetrics.visualisation

trait VisualisationProducer {
    String sizeMetricName
    String colourMetricName
    List<String> extraMetricNames = []
    int partitionSize = 500
    BigDecimal topThreshold = -1
    BigDecimal bottomThreshold = 0
    int bottomColourValue = 0x00ff00
    int topColourValue = 0xff0000

    Map resourceNames

     String loadResourceText(String resourceName) {
         if (resourceName != null) {
             this.class.classLoader.getResourceAsStream(resourceName).getText('UTF-8')
         } else {
             ''
         }
    }

    abstract def addData(String path, BigDecimal size, BigDecimal colour, List extra = [])

    abstract def toJsonable()

    List<String> getPathComponentIdList() {
        []
    }

    String getHtmlTemplateText() {
        loadResourceText(resourceNames.htmlTemplate)
    }

     String getJsLibrary1Text() {
        loadResourceText(resourceNames.jsLibrary1)
    }

    String getJsLibrary2Text() {
        loadResourceText(resourceNames.jsLibrary2)
    }

    String getInitialisationCodeText() {
        loadResourceText(resourceNames.initialisationCode)
    }

    String getStyleSheetText() {
        loadResourceText(resourceNames.styleSheet)
    }
}