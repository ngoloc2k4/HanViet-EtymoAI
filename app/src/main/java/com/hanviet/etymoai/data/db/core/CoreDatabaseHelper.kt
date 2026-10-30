package com.hanviet.etymoai.data.db.core

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * CoreDatabaseHelper: Quản lý truy cập file CSDL SQLite dictionary_core.db ở chế độ CHỈ ĐỌC (Read-Only).
 *
 * Triết lý kiến trúc:
 * 1. Read-Only tuyệt đối: Luôn mở với SQLiteDatabase.OPEN_READONLY. Bất kỳ lệnh INSERT/UPDATE nào đều ném SQLiteException.
 * 2. Tự động sao chép từ assets vào bộ nhớ trong (filesDir/databases) khi chưa có hoặc khi phiên bản thay đổi.
 * 3. Hỗ trợ Dispatchers.IO cho mọi thao tác I/O để không bao giờ block main UI thread.
 */
class CoreDatabaseHelper(
    private val databaseFile: File
) {
    companion object {
        const val DB_NAME = "dictionary_core.db"
        private const val TAG = "CoreDatabaseHelper"

        @Volatile
        private var instance: CoreDatabaseHelper? = null

        /**
         * Lấy instance Singleton của CoreDatabaseHelper từ Android Context.
         */
        fun getInstance(context: Context): CoreDatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: run {
                    val dbFile = ensureDatabaseFile(context)
                    CoreDatabaseHelper(dbFile).also { instance = it }
                }
            }
        }

        /**
         * Factory method phục vụ kiểm thử đơn vị độc lập (chỉ định trực tiếp file CSDL).
         */
        fun fromFile(file: File): CoreDatabaseHelper {
            return CoreDatabaseHelper(file)
        }

        /**
         * Sao chép file dictionary_core.db từ assets vào thư mục nội bộ nếu cần thiết.
         */
        fun ensureDatabaseFile(context: Context): File {
            val dbDir = context.getDatabasePath(DB_NAME).parentFile ?: File(context.filesDir, "databases")
            if (!dbDir.exists()) {
                dbDir.mkdirs()
            }
            val destinationFile = File(dbDir, DB_NAME)

            val assetLength = try {
                context.assets.openFd(DB_NAME).length
            } catch (_: Exception) {
                -1L
            }

            val needsCopy = !destinationFile.exists() ||
                destinationFile.length() == 0L ||
                destinationFile.length() < 1_000_000L ||
                (assetLength > 0 && destinationFile.length() != assetLength)

            if (needsCopy) {
                Log.i(TAG, "Đang sao chép $DB_NAME từ Assets vào ${destinationFile.absolutePath} (kích thước cũ: ${destinationFile.length()} bytes)...")
                copyFromAssets(context, DB_NAME, destinationFile)
            }
            return destinationFile
        }

        private fun copyFromAssets(context: Context, assetName: String, destinationFile: File) {
            try {
                val inputStream = try {
                    context.assets.open(assetName)
                } catch (e: Exception) {
                    val fallbackFile = File("src/main/assets/$assetName")
                    if (fallbackFile.exists()) {
                        fallbackFile.inputStream()
                    } else {
                        throw e
                    }
                }
                inputStream.use { inStream ->
                    FileOutputStream(destinationFile).use { outputStream ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (inStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                        outputStream.flush()
                    }
                }
                Log.i(TAG, "Sao chép hoàn tất: ${destinationFile.length()} bytes")
            } catch (e: IOException) {
                Log.e(TAG, "Lỗi sao chép cơ sở dữ liệu từ Assets: ${e.message}", e)
                throw IllegalStateException("Không thể khởi tạo file cơ sở dữ liệu lõi từ assets: $assetName", e)
            }
        }
    }

    private var database: SQLiteDatabase? = null

    /**
     * Mở kết nối Read-Only một cách an toàn và thread-safe.
     */
    @Synchronized
    fun getReadableDatabase(): SQLiteDatabase {
        val existing = database
        if (existing != null && existing.isOpen) {
            return existing
        }

        if (!databaseFile.exists()) {
            throw IllegalStateException("File CSDL lõi không tồn tại tại: ${databaseFile.absolutePath}")
        }

        // Bắt buộc cờ OPEN_READONLY và NO_LOCALIZED_COLLATORS
        val flags = SQLiteDatabase.OPEN_READONLY or SQLiteDatabase.NO_LOCALIZED_COLLATORS
        val opened = SQLiteDatabase.openDatabase(databaseFile.absolutePath, null, flags)
        database = opened
        return opened
    }

    /**
     * Thực thi truy vấn rawQuery bất đồng bộ trên Coroutines Dispatchers.IO.
     */
    suspend fun <T> useQuery(
        sql: String,
        selectionArgs: Array<String>? = null,
        block: (Cursor) -> T
    ): T = withContext(Dispatchers.IO) {
        val db = getReadableDatabase()
        db.rawQuery(sql, selectionArgs).use { cursor ->
            block(cursor)
        }
    }

    /**
     * Đóng kết nối cơ sở dữ liệu khi không còn sử dụng.
     */
    @Synchronized
    fun close() {
        database?.let {
            if (it.isOpen) {
                it.close()
            }
        }
        database = null
    }

    /**
     * Kiểm tra trạng thái sẵn sàng của CSDL.
     */
    fun isDatabaseOpen(): Boolean = database?.isOpen == true
}
