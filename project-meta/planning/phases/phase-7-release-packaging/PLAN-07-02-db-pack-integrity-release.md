---
phase: 7
plan: 2
wave: 2
depends_on: ["7.1"]
files_modified:
  - "data-pipeline/scripts/package_release_db.py"
  - "app/src/main/java/com/hanviet/etymoai/data/db/core/DataPackIntegrityVerifier.kt"
autonomous: true
user_setup: []

must_haves:
  truths:
    - "Script package_release_db.py nén tối ưu dictionary_core.db bằng zstandard/gzip và sinh file SHA256SUMS."
    - "DataPackIntegrityVerifier trên client kiểm tra hash SHA-256 trước khi giải nén hoặc thay thế file database lõi."
  artifacts:
    - "data-pipeline/scripts/package_release_db.py"
    - "app/src/main/java/com/hanviet/etymoai/data/db/core/DataPackIntegrityVerifier.kt"
---

# Plan 7.2: Đóng Gói Gói Dữ Liệu & Kiểm Tra Tính Toàn Vẹn (Data Pack Integrity)

<objective>
Xây dựng công cụ đóng gói phát hành cho cơ sở dữ liệu `dictionary_core.db`, áp dụng nén thuật toán tối ưu (Zstandard/Gzip), sinh mã băm SHA-256 Checksum, và phát triển thành phần kiểm định tính toàn vẹn `DataPackIntegrityVerifier` trên client để ngăn ngừa rủi ro dữ liệu bị hỏng trong quá trình tải hoặc cập nhật.

Mục đích: Đảm bảo người dùng luôn nhận được file từ điển chính xác từng byte, tuyệt đối không xảy ra tình trạng crash do file SQLite bị ngắt quãng hoặc tải dở dang.
Đầu ra: Script Python `package_release_db.py` và class Android `DataPackIntegrityVerifier.kt`.
</objective>

<context>
Xem xét các tài liệu:
- project-meta/spec/ARCHITECTURE.md (Mục 1 & 2. Data Layer & Core Storage)
- project-meta/spec/SPEC.md (Mục 1. Offline-First & Dual-DB)
</context>

<tasks>

<task type="auto">
  <name>Viết script package_release_db.py đóng gói và sinh SHA-256 Checksum</name>
  <files>data-pipeline/scripts/package_release_db.py</files>
  <action>
    Triển khai script Python `package_release_db.py`:
    1. Chạy `VACUUM;` và `PRAGMA optimize;` trên file `dictionary_core.db` để thu nhỏ dung lượng tối đa và sắp xếp chỉ mục B-Tree gọn gàng.
    2. Nén file bằng định dạng gzip hoặc zstd với tỷ lệ nén cao nhất.
    3. Tính toán mã băm SHA-256 của cả file gốc và file nén:
       `sha256 = hashlib.sha256(open(file, 'rb').read()).hexdigest()`.
    4. Ghi thông tin metadata vào file `manifest.json`: phiên bản (`version`), ngày đóng gói (`build_date`), tổng số từ vị (`lexeme_count`), dung lượng (`byte_size`), và mã hash `sha256`.
  </action>
  <verify>Chạy script trên file database mẫu và kiểm tra file manifest.json sinh ra.</verify>
  <done>Script đóng gói hoạt động trơn tru, nén giảm hơn 60% dung lượng ban đầu.</done>
</task>

<task type="auto">
  <name>Hiện thực hóa DataPackIntegrityVerifier trên Android Client</name>
  <files>app/src/main/java/com/hanviet/etymoai/data/db/core/DataPackIntegrityVerifier.kt</files>
  <action>
    Triển khai class `DataPackIntegrityVerifier`:
    - Đọc file `manifest.json` đi kèm gói cập nhật.
    - Hàm `verifyChecksum(file: File, expectedSha256: String): Boolean`:
      - Đọc file theo stream buffer 64KB, tính `MessageDigest.getInstance("SHA-256")`.
      - So sánh không phân biệt hoa thường với `expectedSha256`.
    - Chỉ cho phép di chuyển hoặc giải nén file vào thư mục hoạt động của `CoreDatabaseHelper` khi mã hash khớp 100%.
    - Nếu hash sai lệch: Xóa ngay file hỏng và trả về mã lỗi `CORRUPTED_DOWNLOAD_PACK` để ứng dụng thông báo cho người dùng.
  </action>
  <verify>Viết unit test kiểm tra hàm verifyChecksum với file mẫu hợp lệ và file cố ý bị thay đổi nội dung.</verify>
  <done>Cơ chế phòng vệ tính toàn vẹn dữ liệu hoàn thiện, bảo vệ ứng dụng an toàn tuyệt đối.</done>
</task>

</tasks>

<verification>
Sau khi hoàn thành:
- [ ] Gói dữ liệu nén kèm mã hash SHA-256 đầy đủ.
- [ ] Client Android từ chối nạp bất kỳ file cơ sở dữ liệu nào bị sửa đổi hoặc tải lỗi.
</verification>

<success_criteria>
- [ ] Tất cả nhiệm vụ đều được xác thực bằng unit test.
- [ ] Các tiêu chí must-haves được đáp ứng trọn vẹn.
</success_criteria>
