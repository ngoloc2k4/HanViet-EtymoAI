---
phase: 0
plan: 2
wave: 2
depends_on: ["0.1"]
files_modified:
  - "data-pipeline/schema/core_schema.sql"
  - "data-pipeline/etl/spike_build_core_db.py"
  - "data-pipeline/etl/test_core_db_integrity.py"
autonomous: true
user_setup: []

must_haves:
  truths:
    - "Schema SQLite core tuân thủ đầy đủ 8 bảng đã quy định trong ARCHITECTURE.md."
    - "Script ETL spike tạo ra file SQLite `dictionary_core.db` mẫu chuẩn hóa Unicode NFC và FTS5."
    - "Test tự động xác nhận bảng ảo FTS5 tìm kiếm được tiếng Việt không dấu và Chosung."
  artifacts:
    - "data-pipeline/schema/core_schema.sql"
    - "data-pipeline/etl/spike_build_core_db.py"
    - "data-pipeline/etl/test_core_db_integrity.py"
---

# Plan 0.2: Khởi Tạo Schema & Thử Nghiệm Pipeline ETL (ETL Spike)

<objective>
Đặc tả schema chuẩn `core_schema.sql` cho cơ sở dữ liệu `dictionary_core.db`, viết script ETL spike độc lập bằng Python để tạo cơ sở dữ liệu SQLite mẫu, và xây dựng bộ kiểm thử tính toàn vẹn (integrity test) cho FTS5 và NFC normalization.

Mục đích: Chứng minh kiến trúc CSDL lõi đáp ứng 100% các tiêu chí tra cứu siêu tốc (< 50ms) trước khi bước vào lập trình Android.
Đầu ra: File DDL `core_schema.sql`, script ETL `spike_build_core_db.py`, và test script `test_core_db_integrity.py`.
</objective>

<context>
Xem xét các tài liệu:
- project-meta/spec/ARCHITECTURE.md (Mục 2.1 Cấu trúc dictionary_core.db)
- project-meta/spec/GLOSSARY.md (Định nghĩa Lexeme, Sense, HanjaSegment)
</context>

<tasks>

<task type="auto">
  <name>Viết DDL chuẩn core_schema.sql với FTS5 Virtual Table</name>
  <files>data-pipeline/schema/core_schema.sql</files>
  <action>
    Tạo file DDL SQLite `core_schema.sql`:
    - Tạo các bảng quan hệ: `lexemes`, `senses`, `aliases`, `hanja_master`, `hanja_readings`, `lexeme_hanja_segments`, `precomputed_forms`.
    - Tạo bảng ảo FTS5: `search_fts USING fts5(headword_nfc, alias_chosung, meaning_vi, unaccented_vi, content='lexemes', content_rowid='lexeme_id')`.
    - Thiết lập các chỉ mục tăng tốc B-Tree: `idx_lexemes_headword`, `idx_aliases_text`, `idx_precomputed_form`, `idx_hanja_segments_lexeme`.
  </action>
  <verify>Chạy `sqlite3 :memory: < data-pipeline/schema/core_schema.sql` xác nhận schema hợp lệ không lỗi cú pháp.</verify>
  <done>Schema được nạp thành công vào SQLite trong bộ nhớ mà không gặp bất kỳ lỗi cú pháp nào.</done>
</task>

<task type="auto">
  <name>Viết script ETL Spike sinh dữ liệu mẫu và kiểm định FTS5</name>
  <files>
    - data-pipeline/etl/spike_build_core_db.py
    - data-pipeline/etl/test_core_db_integrity.py
  </files>
  <action>
    1. Viết `spike_build_core_db.py`:
       - Sử dụng thư viện `unicodedata.normalize('NFC', text)` chuẩn hóa chuỗi.
       - Nạp dữ liệu mẫu 500 từ vựng cốt lõi (gồm `기분` - 氣分 - *khí phân*, `학생` - 學生 - *học sinh*, `듣다`, `가다`, v.v.).
       - Tự động sinh phân đoạn Hán tự `lexeme_hanja_segments` và dạng chia sẵn `precomputed_forms` (`갔어요`, `들었어요`).
       - Sinh bảng chỉ mục FTS5 hoàn chỉnh và ghi ra file `data-pipeline/output/dictionary_sample.db`.
    2. Viết `test_core_db_integrity.py`:
       - Kiểm thử truy vấn FTS5 với cụm từ tiếng Việt không dấu: `"tam trang"` -> khớp `기분`.
       - Kiểm thử truy vấn Chosung: `"ㄱㅂ"` -> khớp `기분`.
       - Kiểm thử truy vấn biến thể chia sẵn: `"갔어요"` -> khớp `가다`.
  </action>
  <verify>Chạy `python3 data-pipeline/etl/spike_build_core_db.py && python3 data-pipeline/etl/test_core_db_integrity.py` đạt 100% test pass.</verify>
  <done>Bộ kiểm thử xác nhận FTS5 và chuỗi NFC hoạt động hoàn hảo, thời gian truy vấn < 5ms trên mẫu dữ liệu.</done>
</task>

</tasks>

<verification>
Sau khi hoàn thành:
- [ ] Schema `core_schema.sql` nạp sạch vào SQLite engine.
- [ ] Script ETL spike sinh ra file database `dictionary_sample.db` với đầy đủ liên kết khóa ngoại.
- [ ] `test_core_db_integrity.py` chứng minh FTS5 xử lý chính xác cả tiếng Việt không dấu và Chosung Hàn Quốc.
</verification>

<success_criteria>
- [ ] Tất cả nhiệm vụ đều được xác thực bằng lệnh thực thi.
- [ ] Các tiêu chí must-haves được đáp ứng trọn vẹn.
</success_criteria>
