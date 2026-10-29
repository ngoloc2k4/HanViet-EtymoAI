---
phase: 1
plan: 2
wave: 2
depends_on: ["1.1"]
files_modified:
  - "app/src/main/java/com/hanviet/etymoai/domain/search/SearchPipeline.kt"
  - "app/src/main/java/com/hanviet/etymoai/domain/search/SearchResult.kt"
  - "app/src/test/java/com/hanviet/etymoai/domain/search/SearchPipelineTest.kt"
autonomous: true
user_setup: []

must_haves:
  truths:
    - "Pipeline tra cứu thực hiện tuần tự qua 5 tầng ưu tiên (Exact -> Chosung -> Precomputed -> Morphology -> FTS5)."
    - "Truy vấn tiếng Việt không dấu ('tam trang') trả về kết quả chính xác trong < 50ms."
    - "Truy vấn Chosung ('ㄱㅂ') trả về từ vựng tương ứng ('기분')."
  artifacts:
    - "app/src/main/java/com/hanviet/etymoai/domain/search/SearchPipeline.kt"
    - "app/src/test/java/com/hanviet/etymoai/domain/search/SearchPipelineTest.kt"
---

# Plan 1.2: Triển Khai Bộ Máy Tra Cứu 5 Tầng (5-Tier Search Pipeline)

<objective>
Xây dựng pipeline tra cứu từ vựng đa tầng trên Android Client theo đặc tả tại ARCHITECTURE.md, tích hợp tìm kiếm FTS5, chuẩn hóa Unicode NFC, nhận diện Chosung và tiếng Việt không dấu.

Mục đích: Khắc phục triệt để nhược điểm tìm kiếm chậm, nhạy cảm với dấu và bỏ sót kết quả ở ứng dụng tiền nhiệm V1.
Đầu ra: Class `SearchPipeline.kt`, dữ liệu kết quả chuẩn `SearchResult.kt`, và bộ kiểm thử `SearchPipelineTest.kt`.
</objective>

<context>
Xem xét các tài liệu:
- project-meta/spec/ARCHITECTURE.md (Mục 3. Quy trình Tra cứu 5 tầng)
- project-meta/history/LESSONS_LEARNED_FROM_V1.md (Bài học về tìm kiếm tiếng Việt không dấu)
</context>

<tasks>

<task type="auto">
  <name>Xây dựng thuật toán xếp hạng và truy vấn 5 tầng trong SearchPipeline</name>
  <files>
    - app/src/main/java/com/hanviet/etymoai/domain/search/SearchPipeline.kt
    - app/src/main/java/com/hanviet/etymoai/domain/search/SearchResult.kt
  </files>
  <action>
    Triển khai `SearchPipeline`:
    1. Nhận chuỗi `query`, chuẩn hóa `java.text.Normalizer.normalize(query.trim(), Normalizer.Form.NFC)`.
    2. Tầng 1 (Exact): Truy vấn `headword_nfc = ?` (ưu tiên cao nhất, điểm tin cậy 1.0).
    3. Tầng 2 (Chosung & Alias): Nếu chuỗi chỉ chứa ký tự Hangul Chosung (ví dụ `ㄱ-ㅎ`), truy vấn bảng `aliases` loại `CHOSUNG`.
    4. Tầng 3 (Precomputed Forms): Truy vấn bảng `precomputed_forms` để tìm các dạng đã chia đuôi ngữ pháp thông dụng.
    5. Tầng 4 (FTS5): Sử dụng `search_fts MATCH ?` với toán tử prefix (`query*`) và cột `unaccented_vi`.
    6. Tổng hợp danh sách, loại bỏ trùng lặp (deduplicate theo `lexeme_id`), và sắp xếp theo điểm trọng số (MatchConfidence).
  </action>
  <verify>Viết test cases trong `SearchPipelineTest.kt` kiểm tra cả 5 luồng tra cứu.</verify>
  <done>Pipeline hoàn thành với tốc độ tra cứu < 30ms trên tập dữ liệu mẫu.</done>
</task>

<task type="auto">
  <name>Tích hợp bộ trích xuất cụm từ N-Gram (Longest Match First)</name>
  <files>app/src/main/java/com/hanviet/etymoai/domain/search/SearchPipeline.kt</files>
  <action>
    Bổ sung hàm `extractCompoundKeywords(sentence: String): List<String>`:
    - Khi người dùng nhập một câu dài (ví dụ: *"học chào hỏi rồi học văn hoá"*), không chỉ cắt 1 chữ đầu tiên mà sinh các cửa sổ trượt: 3-gram -> 2-gram -> 1-gram.
    - So khớp với từ điển lõi để ưu tiên giữ lại các cụm từ có nghĩa hoàn chỉnh (`"chào hỏi"`, `"văn hoá"`).
    - Trả về danh sách từ khóa thực sự có trong từ điển để tra cứu song song.
  </action>
  <verify>Test với câu dài và xác nhận output chứa cả hai cụm `"chào hỏi"` và `"văn hoá"`.</verify>
  <done>Thuật toán trích xuất cụm từ ghép hoạt động chính xác, giải quyết dứt điểm phản hồi bức xúc của người dùng ở V1.</done>
</task>

</tasks>

<verification>
Sau khi hoàn thành:
- [ ] Tất cả các test cases trong `SearchPipelineTest.kt` đạt màu xanh (PASS).
- [ ] Tra cứu `"tam trang"` ra kết quả `"기분"`.
- [ ] Tra cứu `"ㄱㅂ"` ra kết quả `"기분"`.
- [ ] Tra cứu `"học chào hỏi"` tách đúng cụm từ ghép thay vì chỉ lấy chữ `"học"`.
</verification>

<success_criteria>
- [ ] Tất cả nhiệm vụ đều được xác thực bằng unit test.
- [ ] Các tiêu chí must-haves được đáp ứng trọn vẹn.
</success_criteria>
