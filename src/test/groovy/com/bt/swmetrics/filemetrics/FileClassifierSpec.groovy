package com.bt.swmetrics.filemetrics

import com.bt.swmetrics.filemetrics.FileClassifier
import spock.lang.Specification

class FileClassifierSpec extends Specification {
    def "Should be able to determine if a file is a binary file"() {
        given:
        def binaryFile = new File('src/test/resources/blob.bin')

        expect:
        FileClassifier.isBinary(binaryFile)
    }

    def "Should be able to determine if a file is a text file"() {
        given:
        def textFile = new File('src/test/resources/simple-1.txt')

        expect:
        FileClassifier.isText(textFile)
    }

    def "Empty file should classify as text"() {
        given:
        def emptyFile = new File('src/test/resources/empty.txt')

        expect:
        FileClassifier.isText(emptyFile)
    }
}