package com.bt.swmetrics.filemetrics

import com.bt.swmetrics.filemetrics.TextFileFinder
import spock.lang.Specification

class TextFileFinderSpec extends Specification {
    TextFileFinder finder = new TextFileFinder()

    def "Should return list of multiple files for multiple text file inputs"() {
        when:
        List<File> result = finder.findFiles(['src/test/resources/simple-1.txt', 'src/test/resources/simple-2.txt'])

        then:
        result.size() == 2
        result[0].isFile()
        result[0].path =~ /simple-1.txt$/
        result[1].isFile()
        result[1].path =~ /simple-2.txt$/

    }

    def "Should not return binary files in output list"() {
        when:
        List<File> result = finder.findFiles(
                ['src/test/resources/simple-1.txt',
                'src/test/resources/blob.bin',
                'src/test/resources/simple-2.txt']
        )

        then:
        result.size() == 2
        result.each {
            assert it.path =~ /\.txt$/
        }
    }

    def "Should not return non-existent files in output list"() {
        when:
        List<File> result = finder.findFiles(
                ['src/test/resources/simple-1.txt',
                'this/does/not/exist',
                'src/test/resources/simple-2.txt']
        )

        then:
        result.size() == 2
        result.each {
            assert it.path =~ /\.txt$/
        }
    }

    def "Should not include unreadable files in the output list"() {
        given:
        File stubFile = new File('src/test/resources/simple-1.txt')
        stubFile.metaClass.canRead = { -> false }

        when:
        List<File> result = finder.findFiles(
                [new File('src/test/resources/simple-1.txt'),
                stubFile,
                new File('src/test/resources/simple-2.txt')]
        )

        then:
        result.size() == 2
        result.each {
            assert it.path =~ /\.txt$/
        }
    }

    def "Should be able to find all text files under a given directory"() {
        when:
        def result = finder.findFiles(['src/test'])

        then:
        def paths = result.collect { it.path }
        paths.findAll { it =~ /simple.*\.txt$/ }.size() == 2
        paths.every { !it.endsWith('blob.bin') }
    }

    def "Should exclude files based on patterns, if specified"() {
        given:
        finder = new TextFileFinder(excludedPatterns: [/simple-1/])
        when:
        def result = finder.findFiles(['src/test'])

        then:
        def paths = result.collect { it.path }
        paths.findAll { it =~ /simple.*\.txt$/ }.size() == 1
    }

    def "Should include files based on patterns, if specified"() {
        given:
        finder = new TextFileFinder(includedPatterns: [/simple-1/])
        when:
        def result = finder.findFiles(['src/test'])

        then:
        def paths = result.collect { it.path }
        paths.findAll { it =~ /simple.*\.txt$/ }.size() == 1
    }
}