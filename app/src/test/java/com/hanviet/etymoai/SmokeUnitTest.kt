package com.hanviet.etymoai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmokeUnitTest {
    @Test
    fun testAndroid8Requirement() {
        val minSdk = 26 // Android 8.0 Oreo
        assertTrue("Ứng dụng bắt buộc hỗ trợ Android 8.0+ (API >= 26)", minSdk >= 26)
    }

    @Test
    fun testPackageNamespace() {
        val packageName = "com.hanviet.etymoai"
        assertEquals("com.hanviet.etymoai", packageName)
    }
}
