package com.hanviet.etymoai.data.db.core

import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

/**
 * Thông tin chi tiết gói dữ liệu từ điển phát hành (Release Data Pack Manifest)
 */
data class DataPackManifest(
    val schemaVersion: Int,
    val packageVersion: String,
    val buildDate: String,
    val dbFilename: String,
    val dbSizeBytes: Long,
    val dbSha256: String,
    val compressedFilename: String? = null,
    val compressedSizeBytes: Long? = null,
    val compressedSha256: String? = null,
    val lexemeCount: Long? = null
)

/**
 * DataPackIntegrityVerifier: Bộ kiểm định tính toàn vẹn và chống giả mạo gói dữ liệu Core
 * Tuân thủ nguyên tắc: Không bao giờ nạp file CSDL hỏng, tải dở dang hoặc bị sửa đổi.
 */
object DataPackIntegrityVerifier {

    private const val BUFFER_SIZE = 65536 // 64KB buffer đọc stream

    /**
     * Tính toán mã băm SHA-256 chuẩn của một file
     */
    fun calculateSha256(file: File): String {
        if (!file.exists() || !file.isFile) {
            throw IllegalArgumentException("File không tồn tại hoặc không hợp lệ: ${file.absolutePath}")
        }

        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(BUFFER_SIZE)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        val bytes = digest.digest()
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Xác thực file so với mã băm kỳ vọng (không phân biệt hoa thường)
     */
    fun verifyChecksum(file: File, expectedSha256: String): Boolean {
        if (!file.exists() || expectedSha256.isBlank()) return false
        val actual = try {
            calculateSha256(file)
        } catch (_: Exception) {
            return false
        }
        return actual.equals(expectedSha256.trim(), ignoreCase = true)
    }

    /**
     * Parse nội dung manifest.json đi kèm gói phát hành
     */
    fun parseManifest(manifestJson: String): Result<DataPackManifest> {
        return try {
            val root = JSONObject(manifestJson)
            val stats = root.optJSONObject("statistics")
            val manifest = DataPackManifest(
                schemaVersion = root.optInt("schema_version", 1),
                packageVersion = root.optString("package_version", "1.0.0"),
                buildDate = root.optString("build_date", ""),
                dbFilename = root.getString("db_filename"),
                dbSizeBytes = root.getLong("db_size_bytes"),
                dbSha256 = root.getString("db_sha256"),
                compressedFilename = root.optString("compressed_filename").takeIf { it.isNotBlank() },
                compressedSizeBytes = root.optLong("compressed_size_bytes").takeIf { it > 0 },
                compressedSha256 = root.optString("compressed_sha256").takeIf { it.isNotBlank() },
                lexemeCount = stats?.optLong("lexeme_count")
            )
            Result.success(manifest)
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Manifest JSON không hợp lệ: ${e.message}", e))
        }
    }

    /**
     * Kiểm tra toàn vẹn và cài đặt an toàn file database mới vào thư mục hoạt động
     * Nếu mã hash không khớp, lập tức xóa file tải về để tránh rò rỉ dữ liệu lỗi.
     */
    fun verifyAndInstall(
        downloadedFile: File,
        destinationFile: File,
        expectedSha256: String
    ): Result<Unit> {
        if (!verifyChecksum(downloadedFile, expectedSha256)) {
            // Xóa file hỏng ngay lập tức
            if (downloadedFile.exists()) {
                downloadedFile.delete()
            }
            return Result.failure(
                SecurityException("Mã băm SHA-256 không khớp! Gói dữ liệu có thể bị lỗi mạng hoặc bị giả mạo.")
            )
        }

        return try {
            destinationFile.parentFile?.mkdirs()
            downloadedFile.copyTo(destinationFile, overwrite = true)
            downloadedFile.delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
