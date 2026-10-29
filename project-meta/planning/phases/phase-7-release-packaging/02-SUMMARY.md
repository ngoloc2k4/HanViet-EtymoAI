# Summary Plan 7.2: Data Pack Packaging & Client Integrity Verifier

## Hoàn thành
- **package_release_db.py**: Script Python tối ưu hóa SQLite B-Tree (`VACUUM`, `PRAGMA optimize`), nén Gzip level 9 (đạt 88.2% tỷ lệ nén), tính toán SHA-256 Checksum và xuất `manifest.json` cùng `SHA256SUMS`.
- **DataPackIntegrityVerifier.kt**: Bộ kiểm định toàn vẹn gói dữ liệu trên client Android:
  - Tính toán SHA-256 qua stream buffer 64KB.
  - Đối chiếu mã băm không phân biệt hoa thường.
  - Parse `manifest.json`.
  - Tự động xóa file hỏng tải dở dang (`CORRUPTED_DOWNLOAD_PACK`) và cài đặt an toàn vào thư mục Core database khi hash khớp 100%.
- **DataPackIntegrityVerifierTest.kt**: 5 unit tests kiểm tra tính SHA-256, kiểm định khớp mã băm, kiểm tra parse manifest và cơ chế tự động xóa file hỏng.
