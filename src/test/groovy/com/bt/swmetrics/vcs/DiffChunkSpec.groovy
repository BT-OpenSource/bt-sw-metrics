package com.bt.swmetrics.vcs

import spock.lang.Specification

class DiffChunkSpec extends Specification {
    DiffChunk chunk

    final static def CHUNK_TEXT = '''@@ -287,9 +290,10 @@
       <DistrictFunction>3</DistrictFunction>
       <DistrictFTPdirectory>incoming</DistrictFTPdirectory> 
       <DistrictFTPserverName>ftp.swns.bt.com</DistrictFTPserverName> 
       <DistrictURLs>
-        <DistrictNotificationsURL>http://webservices.telent.com/swns_ct_eton/ws_eton.asmx</DistrictNotificationsURL>
+        <DistrictNotificationsURL>http://www.swns.bt.com/EToNHAServices/EToNSoap12</DistrictNotificationsURL>
+        <DistrictNotificationsURL2>http://www.swns.bt.com/Other</DistrictNotificationsURL2>
         <AttachmentURLprefix>http://www.swns.bt.com/attachments/</AttachmentURLprefix>
       </DistrictURLs>
       <DistrictFaxNumber>01332 822444</DistrictFaxNumber>
       <DistrictPostalAddress>'''.split(/\n/) as List<String>

    def "Should be able to get the starting points and sizes of a simple chunk"() {
        given:
        chunk = new DiffChunk(CHUNK_TEXT)

        expect:
        chunk.oldStart == 287
        chunk.oldSize == 9
        chunk.newStart == 290
        chunk.newSize == 10
    }

    def "Should be able to get the starting point and size of an added single-line chunk to an empty file"() {
        given:
        def text = ['@@ -0,0 +1 @@', '+hello']
        chunk = new DiffChunk(text)

        expect:
        chunk.oldStart == 0
        chunk.oldSize == 0
        chunk.newStart == 1
        chunk.newSize == 1
    }

    def "Should be able to get the starting point and size of an added single-line chunk to a non-empty file"() {
        given:
        def text = ['@@ -1 +1,2 @@', ' hello', '+goodbye']
        chunk = new DiffChunk(text)

        expect:
        chunk.oldStart == 1
        chunk.oldSize == 1
        chunk.newStart == 1
        chunk.newSize == 2
    }

    def "Should be able to get the starting point and size of a removed single-line chunk"() {
        given:
        def text = ['@@ -1,2 +1 @@', ' hello', '-goodbye']
        chunk = new DiffChunk(text)

        expect:
        chunk.oldStart == 1
        chunk.oldSize == 2
        chunk.newStart == 1
        chunk.newSize == 1
    }

    def "Should be able to get the old text lines"() {
        given:
        chunk = new DiffChunk(CHUNK_TEXT)

        expect:
        chunk.oldLines == '''      <DistrictFunction>3</DistrictFunction>
      <DistrictFTPdirectory>incoming</DistrictFTPdirectory> 
      <DistrictFTPserverName>ftp.swns.bt.com</DistrictFTPserverName> 
      <DistrictURLs>
        <DistrictNotificationsURL>http://webservices.telent.com/swns_ct_eton/ws_eton.asmx</DistrictNotificationsURL>
        <AttachmentURLprefix>http://www.swns.bt.com/attachments/</AttachmentURLprefix>
      </DistrictURLs>
      <DistrictFaxNumber>01332 822444</DistrictFaxNumber>
      <DistrictPostalAddress>'''.split(/\n/) as List<String>
    }

    def "Should be able to get the removed line count"() {
        given:
        chunk = new DiffChunk(CHUNK_TEXT)

        expect:
        chunk.removedCount == 1
    }

    def "Should be able to get the added line count"() {
        given:
        chunk = new DiffChunk(CHUNK_TEXT)

        expect:
        chunk.addedCount == 2
    }

    def "Should be able to get the new text lines"() {
        given:
        chunk = new DiffChunk(CHUNK_TEXT)

        expect:
        chunk.newLines == '''      <DistrictFunction>3</DistrictFunction>
      <DistrictFTPdirectory>incoming</DistrictFTPdirectory> 
      <DistrictFTPserverName>ftp.swns.bt.com</DistrictFTPserverName> 
      <DistrictURLs>
        <DistrictNotificationsURL>http://www.swns.bt.com/EToNHAServices/EToNSoap12</DistrictNotificationsURL>
        <DistrictNotificationsURL2>http://www.swns.bt.com/Other</DistrictNotificationsURL2>
        <AttachmentURLprefix>http://www.swns.bt.com/attachments/</AttachmentURLprefix>
      </DistrictURLs>
      <DistrictFaxNumber>01332 822444</DistrictFaxNumber>
      <DistrictPostalAddress>'''.split(/\n/) as List<String>
    }

    def "Should be able to get the old line indents"() {
        given:
        chunk = new DiffChunk(CHUNK_TEXT)

        expect:
        chunk.oldLineIndents == [1, 1, 1, 1, 2, 2, 1, 1, 1]
    }

    def "Should be able to get the new line indents"() {
        given:
        chunk = new DiffChunk(CHUNK_TEXT)

        expect:
        chunk.newLineIndents == [1, 1, 1, 1, 2, 2, 2, 1, 1, 1]
    }
}