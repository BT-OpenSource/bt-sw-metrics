package com.bt.swmetrics.filemetrics

import groovy.io.FileType
import groovy.util.logging.Slf4j

@Slf4j
class TextFileFinder {
    List excludedPatterns = []
    List includedPatterns = []

     List<File> findFiles(List fileOrDirectoryNamesOrObjects) {
        log.debug("Finding files")
        def expanded = expandDirectoriesIfNecessary(fileOrDirectoryNamesOrObjects)
        expanded.grep {
            def existsAndReadable = it.exists() && it.canRead()
            log.debug("$it existence and readability = $existsAndReadable")

            existsAndReadable && checkIfFilePathIncludedByPattern(it) && FileClassifier.isText(it)
        }
    }

    static List<File> expandDirectoriesIfNecessary(fileOrDirectoryObjects) {
        fileOrDirectoryObjects.collectMany {
            def file = convertToFileIfNecessary(it)
            if (file.isDirectory()) {
                findAllFilesUnderDirectory(file)
            } else {
                [file]
            }
        }
    }

     static File convertToFileIfNecessary(object) {
        switch (object) {
            case File:
                return object
            case String:
                return new File(object as String)
            default:
                return new File(object)
        }
    }

    static List<File> findAllFilesUnderDirectory(it) {
        def files = []
        it.eachFileRecurse(FileType.FILES) { file ->
            files << file
        }
        files
    }

    private boolean checkIfFilePathIncludedByPattern(File file) {
        def included = includedPatterns.every { file.path =~ it }
        def excluded = excludedPatterns.any { file.path =~ it }
        log.debug("$file matching path by pattern: included = $included, excluded = $excluded")
        included && !excluded
    }
}
