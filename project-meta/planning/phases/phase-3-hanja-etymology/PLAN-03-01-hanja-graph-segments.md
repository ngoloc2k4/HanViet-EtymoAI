---
phase: 3
plan: 1
wave: 1
depends_on: ["1.2"]
files_modified:
  - "app/src/main/java/com/hanviet/etymoai/domain/etymology/HanjaSegmentModel.kt"
  - "app/src/main/java/com/hanviet/etymoai/domain/etymology/EtymologyRepository.kt"
  - "app/src/test/java/com/hanviet/etymoai/domain/etymology/EtymologyRepositoryTest.kt"
autonomous: true
user_setup: []

must_haves:
  truths:
    - "HanjaSegmentModel phân tách chính xác từng âm tiết tiếng Hàn sang chữ Hán và âm Hán-Việt."
    - "EtymologyRepository cung cấp phương thức trích xuất cây phả hệ từ vựng (Etymology Graph) theo chữ Hán chung."
  artifacts:
    - "app/src/main/java/com/hanviet/etymoai/domain/etymology/HanjaSegmentModel.kt"
    - "app/src/main/java/com/hanviet/etymoai/domain/etymology/EtymologyRepository.kt"
---

# Plan 3.1: Mô Hình Phân Đoạn Hán Tự & Đồ Thị Từ Nguyên (Hanja Graph & Segments)

<objective>
Xây dựng mô hình dữ liệu và tầng truy vấn chiết tự Hán tự cho từng âm tiết tiếng Hàn, liên kết với âm Hán-Việt tương đương, và thiết lập thuật toán trích xuất Đồ thị từ nguyên (Etymology Graph) để tìm tất cả các từ vựng phái sinh có chung gốc chữ Hán.

Mục đích: Hiện thực hóa giá trị độc bản lớn nhất của dự án: học 1 chữ Hán, hiểu sâu 100 từ tiếng Hàn và tiếng Việt.
Đầu ra: Model `HanjaSegmentModel.kt`, Repository `EtymologyRepository.kt`, và test suite `EtymologyRepositoryTest.kt`.
</objective>

<context>
Xem xét các tài liệu:
- project-meta/spec/ARCHITECTURE.md (Mục 2.1 Bảng hanja_master và lexeme_hanja_segments)
- project-meta/spec/GLOSSARY.md (Khái niệm HanjaSegment, EtymologyGraph)
</context>

<tasks>

<task type="auto">
  <name>Định nghĩa mô hình HanjaSegment và HanjaMaster</name>
  <files>app/src/main/java/com/hanviet/etymoai/domain/etymology/HanjaSegmentModel.kt</files>
  <action>
    Tạo các cấu trúc dữ liệu:
    - `data class HanjaMaster`: `hanjaChar: String`, `radical: String`, `strokeCount: Int`, `vietnameseMeaning: String`, `pinyin: String`.
    - `data class HanjaSegment`: `segmentIndex: Int`, `syllableKo: String`, `hanjaChar: String?`, `readingVi: String?`, `meaningVi: String?`.
    - `data class WordEtymology`: `lexemeId: Long`, `headword: String`, `originType: OriginType`, `verificationStatus: VerificationStatus`, `segments: List<HanjaSegment>`.
    - `data class EtymologyFamilyNode`: `hanjaChar: String`, `readingVi: String`, `meaningVi: String`, `relatedWords: List<RelatedWordSummary>`.
  </action>
  <verify>Viết test khởi tạo và serialize JSON cho các model trên.</verify>
  <done>Cấu trúc dữ liệu hoàn thành, hỗ trợ biểu diễn đầy đủ cả từ thuần Hàn lẫn từ gốc Hán phức hợp.</done>
</task>

<task type="auto">
  <name>Xây dựng EtymologyRepository truy vấn phân đoạn và cây từ vựng</name>
  <files>
    - app/src/main/java/com/hanviet/etymoai/domain/etymology/EtymologyRepository.kt
    - app/src/test/java/com/hanviet/etymoai/domain/etymology/EtymologyRepositoryTest.kt
  </files>
  <action>
    Triển khai `EtymologyRepository`:
    1. Hàm `getEtymologyForWord(lexemeId: Long): Flow<WordEtymology>`:
       - Truy vấn bảng `lexeme_hanja_segments` kết hợp với `hanja_master`.
       - Ghép từng âm tiết tiếng Hàn với chữ Hán và âm Hán-Việt tương ứng (ví dụ: `기` -> 氣 - *Khí*, `분` -> 分 - *Phân*).
    2. Hàm `getEtymologyFamily(hanjaChar: String): Flow<EtymologyFamilyNode>`:
       - Tìm kiếm tất cả các từ trong `lexemes` có chứa `hanjaChar` trong bảng `lexeme_hanja_segments`.
       - Trả về danh sách từ họ hàng (ví dụ: chữ `學` (Học) -> `학생` (Học sinh), `학교` (Trường học), `học tập` (Học tập)).
  </action>
  <verify>Chạy `EtymologyRepositoryTest.kt` kiểm tra trích xuất từ nguyên cho từ `기분` và cây phả hệ cho chữ `學`.</verify>
  <done>Repository trả về chính xác cấu trúc phân đoạn và đồ thị phả hệ từ vựng trong < 15ms.</done>
</task>

</tasks>

<verification>
Sau khi hoàn thành:
- [ ] Truy vấn từ nguyên của `기분` trả về đúng 2 phân đoạn: `기` (氣 - Khí) và `분` (分 - Phân).
- [ ] Bấm vào chữ `學` trả về danh sách tất cả các từ có chứa chữ `學` trong từ điển.
</verification>

<success_criteria>
- [ ] Tất cả nhiệm vụ đều được xác thực bằng unit test.
- [ ] Các tiêu chí must-haves được đáp ứng trọn vẹn.
</success_criteria>
