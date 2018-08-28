package com.bt.swmetrics.filemetrics

import groovy.util.logging.Slf4j

@Slf4j
class FileClassifier {

    public static final BigDecimal TEXT_THRESHOLD_PERCENTAGE = 95.0

    static boolean isText(File file) {
        byte[] sampleBytes = readSampleBytes(file)
        String sampleAsString = new String(sampleBytes, 'UTF-8')
        def result = isStringMostlyPrintable(sampleAsString)
        log.debug("File $file is ${result ? '' : 'NOT '}a text file")
        result
    }

    private static byte[] readSampleBytes(File file) {
        FileInputStream fis = new FileInputStream(file)
        byte[] readBuffer = new byte[1024]
        int bytesRead = fis.read(readBuffer)
        fis.close()
        if (bytesRead <= 0) {
            return new byte[0]
        } else {
            return readBuffer[0 ..< bytesRead]
        }
    }

    static boolean isBinary(File file) {
        !isText(file)
    }

    private static boolean isStringMostlyPrintable(String inputString) {
        if (inputString.size() < 1) {
            return true
        }

        int unprintableCount = inputString.toCharArray().inject(0) { count, ch ->
            count + (isPrintable(ch) ? 0 : 1)
        }
        def percentagePrintable = ((inputString.size() - unprintableCount) * 100.0) / (inputString.size())
        return percentagePrintable >= TEXT_THRESHOLD_PERCENTAGE
    }

    private static boolean isPrintable(Character ch) {
        if (ch.isLetterOrDigit() || ch.isWhitespace()) {
            return true
        }

        Character.UnicodeBlock block = Character.UnicodeBlock.of(ch)
        return !Character.isISOControl(ch) &&
                block != null &&
                block != Character.UnicodeBlock.SPECIALS
    }
}
