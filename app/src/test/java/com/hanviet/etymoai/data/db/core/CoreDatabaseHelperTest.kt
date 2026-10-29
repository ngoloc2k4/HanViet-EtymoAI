package com.hanviet.etymoai.data.db.core

import android.database.sqlite.SQLiteException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

/**
 * Kiểm thử tính năng Read-Only và toàn vẹn của CoreDatabaseHelper bằng Robolectric.
 */
@RunWith(RobolectricTestRunner::class)
class CoreDatabaseHelperTest {

    @Test
    fun testCoreDatabase_opensSuccessfully_andRejectsWrites() {
        val dbFile = File("src/main/assets/dictionary_core.db")
        assertTrue("File dictionary_core.db phải tồn tại trong assets", dbFile.exists())

        val helper = CoreDatabaseHelper.fromFile(dbFile)
        val db = helper.getReadableDatabase()
        
        try {
            assertTrue("Database phải được mở", db.isOpen)
            assertTrue("Database bắt buộc ở chế độ Read-Only", db.isReadOnly)

            // Kiểm tra truy vấn đọc
            val cursor = db.rawQuery("SELECT headword_nfc FROM lexemes WHERE headword_nfc = ?", arrayOf("기분"))
            assertTrue("Phải tìm thấy mục từ 기분", cursor.moveToFirst())
            assertEquals("기분", cursor.getString(0))
            cursor.close()

            // Kiểm tra bảo mật: Cố ý ghi vào DB bắt buộc phải ném ngoại lệ SQLiteException
            try {
                db.execSQL("INSERT INTO lexemes (stable_key, headword_nfc, origin_type, origin_status, source_dataset) VALUES ('k1', 'w1', 'TEST', 'TEST', 'TEST')")
                fail("Cố tình thực thi lệnh INSERT trên Read-Only DB phải ném ngoại lệ SQLiteException!")
            } catch (expected: SQLiteException) {
                // Thành công: Ngoại lệ bị ném ra như mong đợi
                assertTrue(
                    "Thông báo lỗi phải liên quan đến readonly",
                    expected.message?.contains("readonly", ignoreCase = true) == true
                )
            }
        } finally {
            helper.close()
        }
    }
}
