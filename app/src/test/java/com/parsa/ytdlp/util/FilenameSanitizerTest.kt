package com.parsa.ytdlp.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FilenameSanitizerTest { @Test fun removesUnsafeCharacters() { assertEquals("a_b_c", FilenameSanitizer.sanitize("a/b:c")) } }
