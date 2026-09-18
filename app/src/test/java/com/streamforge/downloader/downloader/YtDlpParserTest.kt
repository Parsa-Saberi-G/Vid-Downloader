package com.streamforge.downloader.downloader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class YtDlpParserTest {
    @Test fun parsesProgress() { assertNotNull(YtDlpParser.parseProgress("[download] 42.0% of 10MiB at 2MiB/s ETA 00:03")) }
    @Test fun parsesInfoPlaceholder() { assertEquals("Title", YtDlpParser.parseInfo("Title").title) }
}
