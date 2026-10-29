---
phase: 2
plan: 2
wave: 2
depends_on: ["2.1"]
files_modified:
  - "app/src/main/java/com/hanviet/etymoai/domain/morphology/MorphologyEngine.kt"
  - "app/src/main/java/com/hanviet/etymoai/domain/morphology/CandidateLemma.kt"
  - "app/src/main/java/com/hanviet/etymoai/domain/search/SearchPipeline.kt"
autonomous: true
user_setup: []

must_haves:
  truths:
    - "MorphologyEngine trả về danh sách ứng viên (CandidateLemma) được xếp hạng theo điểm tin cậy."
    - "Tích hợp hoàn hảo vào Tầng 4 của SearchPipeline: Khi tìm kiếm '들었어요', trả về cả '듣다' (nghe) và '들다' (cầm/nhấc) kèm ghi chú quy tắc."
  artifacts:
    - "app/src/main/java/com/hanviet/etymoai/domain/morphology/MorphologyEngine.kt"
    - "app/src/main/java/com/hanviet/etymoai/domain/morphology/CandidateLemma.kt"
---

# Plan 2.2: Tích Hợp Bộ Máy Hình Thái & Xếp Hạng Ứng Viên (Deconjugation Ranking)

<objective>
Tích hợp `MorphologyEngine` vào `SearchPipeline`, xây dựng thuật toán xếp hạng đa ứng viên (Candidate Ranking) dựa trên tần suất từ vựng và cấp độ ngữ pháp, đồng thời xác thực kết quả đối chiếu với cơ sở dữ liệu `dictionary_core.db`.

Mục đích: Đảm bảo khi người dùng gõ một từ đã chia đuôi ngữ pháp, hệ thống không chỉ đoán mò mà còn kiểm chứng xem từ gốc có thực sự tồn tại trong từ điển hay không và hiển thị giải thích ngữ pháp ngắn gọn.
Đầu ra: Class `MorphologyEngine.kt`, model `CandidateLemma.kt`, và cập nhật tích hợp vào `SearchPipeline.kt`.
</objective>

<context>
Xem xét các tài liệu:
- project-meta/spec/ARCHITECTURE.md (Mục 3 & 4. Search Pipeline & Morphology)
- project-meta/spec/GLOSSARY.md (CandidateLemma definition)
</context>

<tasks>

<task type="auto">
  <name>Xây dựng MorphologyEngine với cơ chế đối chiếu Core Database</name>
  <files>
    - app/src/main/java/com/hanviet/etymoai/domain/morphology/MorphologyEngine.kt
    - app/src/main/java/com/hanviet/etymoai/domain/morphology/CandidateLemma.kt
  </files>
  <action>
    Triển khai `MorphologyEngine`:
    1. Nhận chuỗi từ người dùng (ví dụ: `"추워"`).
    2. Chạy qua tất cả các quy tắc trong `IrregularVerbRules` và các đuôi liên từ/thì thông dụng (`-았/었-`, `-아서/어서-`, `-으면-`, `-니까-`, `-고-`).
    3. Thu được danh sách các `rawCandidateLemmas`.
    4. Đối chiếu từng ứng viên với `dictionary_core.db`:
       - Nếu ứng viên TỒN TẠI trong bảng `lexemes`, tăng điểm tin cậy (`confidenceScore += 0.4`).
       - Nếu ứng viên là từ thông dụng (TOPIK 1-3), cộng thêm điểm ưu tiên.
       - Nếu ứng viên KHÔNG tồn tại trong từ điển, loại bỏ hoặc hạ điểm xuống mức thấp nhất.
    5. Trả về danh sách `List<CandidateLemma>` đã sắp xếp giảm dần theo `confidenceScore`.
  </action>
  <verify>Viết unit test cho `MorphologyEngine` kiểm tra các trường hợp đa nghĩa (`들었어요`, `걸어요`, `나아요`).</verify>
  <done>Bộ máy trả về đúng danh sách ứng viên có trong từ điển kèm ghi chú giải thích ngữ pháp tiếng Việt.</done>
</task>

<task type="auto">
  <name>Tích hợp MorphologyEngine vào SearchPipeline (Tầng 4)</name>
  <files>app/src/main/java/com/hanviet/etymoai/domain/search/SearchPipeline.kt</files>
  <action>
    Cập nhật `SearchPipeline.kt`:
    - Sau khi Tầng 1 (Exact) và Tầng 3 (Precomputed) không tìm thấy hoặc kết quả ít, kích hoạt Tầng 4 (`MorphologyEngine.deconjugate(query)`).
    - Với mỗi ứng viên `CandidateLemma` trả về, truy vấn lấy thông tin nét nghĩa (`Sense`), từ nguyên Hán tự (`HanjaSegment`) và đóng gói vào `SearchResult.DeconjugatedMatch`.
    - Đảm bảo hiển thị nhãn UI rõ ràng: *"Từ gốc: [가다] - Quy tắc: [Thì quá khứ -았/었-]"*.
  </action>
  <verify>Chạy test tích hợp `SearchPipelineTest` với đầu vào `"들었어요"` và xác nhận kết quả trả về chứa thông tin từ gốc `듣다`.</verify>
  <done>SearchPipeline tích hợp thành công, hoàn thiện luồng tra cứu hình thái tự động 100% offline.</done>
</task>

</tasks>

<verification>
Sau khi hoàn thành:
- [ ] Tra cứu `"추워서"` trả về ngay lập tức từ gốc `"춥다"` (Lạnh) kèm giải thích ngữ pháp.
- [ ] Không có hiện tượng giật lag: Thời gian giải phân tích hình thái và truy vấn < 40ms.
</verification>

<success_criteria>
- [ ] Tất cả nhiệm vụ đều được xác thực bằng unit test.
- [ ] Các tiêu chí must-haves được đáp ứng trọn vẹn.
</success_criteria>
