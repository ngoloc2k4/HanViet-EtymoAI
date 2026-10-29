package com.hanviet.etymoai.data.db.core

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class DataPackIntegrityVerifierTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testCalculateSha256_knownContent() {
        val testFile = tempFolder.newFile("sample.txt")
        testFile.writeText("Hello World", Charsets.UTF_8)

        // SHA-256 chuẩn của chuỗi "Hello World"
        val expectedSha = "a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e"
        val actualSha = DataPackIntegrityVerifier.calculateSha256(testFile)

        assertEquals(expectedSha, actualSha)
    }

    @Test
    fun testVerifyChecksum_matchingAndCaseInsensitive() {
        val testFile = tempFolder.newFile("test_db.db")
        testFile.writeBytes(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8))

        val sha = DataPackIntegrityVerifier.calculateSha256(testFile)
        assertTrue(DataPackIntegrityVerifier.verifyChecksum(testFile, sha))
        assertTrue(DataPackIntegrityVerifier.verifyChecksum(testFile, sha.uppercase()))
        assertFalse(DataPackIntegrityVerifier.verifyChecksum(testFile, "0000000000000000000000000000000000000000000000000000000000000000"))
    }

    @Test
    fun testParseManifest_validJson() {
        val json = """
            {
               "schema_version": 1,
               "package_version": "1.0.0-beta",
               "build_date": "2026-10-29T12:00:00Z",
               "db_filename": "dictionary_core.db",
               "db_size_bytes": 1048576,
               "db_sha256": "abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890",
               "compressed_filename": "dictionary_core.db.gz",
               "compressed_size_bytes": 262144,
               "compressed_sha256": "123456abcdef7890123456abcdef7890123456abcdef7890123456abcdef7890",
               "statistics": {
                  "lexeme_count": 50000
               }
            }
        """.trimIndent()

        val result = DataPackIntegrityVerifier.parseManifest(json)
        assertTrue(result.isSuccess)

        val manifest = result.getOrNull()
        assertNotNull(manifest)
        assertEquals("1.0.0-beta", manifest!!.packageVersion)
        assertEquals("dictionary_core.db", manifest.dbFilename)
        assertEquals(1048576L, manifest.dbSizeBytes)
        assertEquals(50000L, manifest.lexemeCount)
    }

    @Test
    fun testVerifyAndInstall_success() {
        val sourceFile = tempFolder.newFile("downloaded.db")
        sourceFile.writeText("VALID_SQLITE_CONTENT", Charsets.UTF_8)
        val validSha = DataPackIntegrityVerifier.calculateSha256(sourceFile)

        val destFile = File(tempFolder.root, "installed.db")
        assertFalse(destFile.exists())

        val result = DataPackIntegrityVerifier.verifyAndInstall(sourceFile, destFile, validSha)
        assertTrue(result.isSuccess)
        assertTrue(destFile.exists())
        assertEquals("VALID_SQLITE_CONTENT", destFile.readText(Charsets.UTF_8))
        assertFalse("Source file phải được dọn dẹp sau khi cài đặt", sourceFile.exists())
    }

    @Test
    fun testVerifyAndInstall_tampered_deletesCorruptedFile() {
        val corruptedFile = tempFolder.newFile("corrupted.db")
        corruptedFile.writeText("CORRUPTED_BYTES", Charsets.UTF_8)

        val destFile = File(tempFolder.root, "should_not_exist.db")
        val fakeExpectedSha = "deadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeef"

        val result = DataPackIntegrityVerifier.verifyAndInstall(corruptedFile, destFile, fakeExpectedSha)
        assertTrue(result.isFailure)
        assertFalse(destFile.exists())
        assertFalse("File hỏng phải bị xóa ngay lập tức để bảo vệ bộ nhớ", corruptedFile.exists())
    }
}
