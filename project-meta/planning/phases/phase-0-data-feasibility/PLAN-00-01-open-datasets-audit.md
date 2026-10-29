---
phase: 0
plan: 1
wave: 1
depends_on: []
files_modified:
  - "data-pipeline/sources/SOURCES.md"
  - "data-pipeline/scripts/audit_sources.py"
autonomous: true
user_setup: []

must_haves:
  truths:
    - "Tất cả các nguồn dữ liệu từ điển tiếng Hàn - tiếng Việt và Hán tự mở được xác định kèm giấy phép bản quyền rõ ràng."
    - "Script kiểm tra mẫu dữ liệu nguồn NIKL/Wiktionary/StarDict/Unihan chạy thành công và báo cáo số lượng từ khả dụng."
  artifacts:
    - "data-pipeline/sources/SOURCES.md"
    - "data-pipeline/scripts/audit_sources.py"
---

# Plan 0.1: Rà Soát Nguồn Dữ Liệu & Bản Quyền (Open Datasets Audit)

<objective>
Rà soát, lập danh mục và kiểm tra tính khả thi của các bộ dữ liệu từ điển tiếng Hàn - tiếng Việt, dữ liệu Hán tự (Hanja) và Hán-Việt khả dụng từ các nguồn mở uy tín (NIKL Korean-Vietnamese Learner's Dictionary, Wiktionary, Unihan Database, StarDict hiện hữu).

Mục đích: Đảm bảo nền tảng dữ liệu sạch, có bản quyền hợp pháp (Creative Commons / Public Domain) và giải quyết triệt để vấn đề thiếu từ/nghĩa sai lệch ở V1.
Đầu ra: Tài liệu `SOURCES.md` và script Python `audit_sources.py` kiểm định mẫu dữ liệu.
</objective>

<context>
Xem xét các tài liệu:
- project-meta/spec/IDEA_2.2_SPEC.md (Mục 3. Data Engineering & Schema)
- project-meta/history/LESSONS_LEARNED_FROM_V1.md (Bài học về StarDict cũ)
</context>

<tasks>

<task type="auto">
  <name>Lập danh mục nguồn dữ liệu và giấy phép bản quyền</name>
  <files>data-pipeline/sources/SOURCES.md</files>
  <action>
    Tạo tài liệu `SOURCES.md` phân tích chi tiết:
    1. NIKL Korean-Vietnamese Learner's Dictionary (Viện Ngôn ngữ Quốc gia Hàn Quốc - CC BY-SA 2.0 KR): Cung cấp từ vựng chuẩn, TOPIK level, âm Hán Hàn, giải nghĩa chuẩn.
    2. Unihan Database & Hanja-Hangul Mapping: Bản đồ chữ Hán - âm Hàn - âm Hán Việt.
    3. Wiktionary dump: Bổ sung từ phái sinh hiện đại và ví dụ câu.
    4. CSDL StarDict hiện có: Chọn lọc các từ vựng đời sống (sau khi loại bỏ nghĩa cổ và cập nhật dấu tiếng Việt).
    Quy định rõ ràng giấy phép của từng nguồn và chính sách trích dẫn nguồn (Attribution Policy).
  </action>
  <verify>Kiểm tra file `data-pipeline/sources/SOURCES.md` tồn tại và có đầy đủ 4 nguồn dữ liệu kèm giấy phép.</verify>
  <done>Danh mục nguồn dữ liệu được phê duyệt, không có nguồn vi phạm bản quyền thương mại độc quyền.</done>
</task>

<task type="auto">
  <name>Viết script thẩm định cấu trúc mẫu dữ liệu nguồn</name>
  <files>data-pipeline/scripts/audit_sources.py</files>
  <action>
    Viết script Python `audit_sources.py`:
    - Đọc các mẫu file dữ liệu thô (JSON, CSV, SQLite).
    - Thống kê tỷ lệ từ có âm Hán-Hàn, tỷ lệ từ thuần Hàn, tỷ lệ từ có nghĩa tiếng Việt.
    - Phát hiện các ký tự lỗi Unicode hoặc ký tự không chuẩn hóa (chưa chuẩn NFC).
    - Tránh: Không tải toàn bộ dữ liệu hàng trăm MB trong script này; chỉ thẩm định mẫu đại diện (sample batch).
  </action>
  <verify>Chạy `python3 data-pipeline/scripts/audit_sources.py` và in ra bảng thống kê phân bố tỷ lệ từ loại và Hán tự.</verify>
  <done>Script chạy không lỗi, xác nhận tính khả thi của việc trích xuất ít nhất 50,000 từ vị đạt chuẩn.</done>
</task>

</tasks>

<verification>
Sau khi hoàn thành hai nhiệm vụ:
- [ ] `data-pipeline/sources/SOURCES.md` liệt kê đầy đủ 4 nguồn kèm giấy phép mở.
- [ ] `data-pipeline/scripts/audit_sources.py` thẩm định thành công mẫu dữ liệu và chứng minh tỷ lệ từ Hán-Hàn khả dụng > 60%.
</verification>

<success_criteria>
- [ ] Tất cả nhiệm vụ đều được xác thực bằng lệnh thực thi.
- [ ] Các tiêu chí must-haves được đáp ứng trọn vẹn.
</success_criteria>
