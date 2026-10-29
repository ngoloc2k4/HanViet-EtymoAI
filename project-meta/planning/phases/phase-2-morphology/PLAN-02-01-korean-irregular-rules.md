---
phase: 2
plan: 1
wave: 1
depends_on: ["1.2"]
files_modified:
  - "app/src/main/java/com/hanviet/etymoai/domain/morphology/MorphologyRule.kt"
  - "app/src/main/java/com/hanviet/etymoai/domain/morphology/IrregularVerbRules.kt"
  - "app/src/test/java/com/hanviet/etymoai/domain/morphology/IrregularVerbRulesTest.kt"
autonomous: true
user_setup: []

must_haves:
  truths:
    - "Triển khai đầy đủ 7 nhóm quy tắc biến đổi bất quy tắc tiếng Hàn (ㅂ, ㄷ, ㅅ, ㅎ, 르, 으, ㄹ)."
    - "Test suite kiểm tra độc lập từng nhóm bất quy tắc đạt 100% tỷ lệ đỗ (pass)."
  artifacts:
    - "app/src/main/java/com/hanviet/etymoai/domain/morphology/MorphologyRule.kt"
    - "app/src/main/java/com/hanviet/etymoai/domain/morphology/IrregularVerbRules.kt"
    - "app/src/test/java/com/hanviet/etymoai/domain/morphology/IrregularVerbRulesTest.kt"
---

# Plan 2.1: Xây Dựng Bộ Quy Tắc Động Từ Bất Quy Tắc (Irregular Verb Rules)

<objective>
Thiết kế và lập trình các quy tắc biến đổi ngược hình thái học (reverse morphology rules) cho 7 nhóm động từ và tính từ bất quy tắc tiếng Hàn, kèm theo bộ kiểm thử đơn vị bao phủ toàn diện các trường hợp ngoại lệ.

Mục đích: Cho phép người học tìm kiếm bất kỳ dạng chia đuôi phức tạp nào (ví dụ: `추워`, `들어요`, `지은`, `파래`, `몰라요`, `바빠`, `만듭니다`) mà vẫn truy xuất chính xác về từ gốc tương ứng (`춥다`, `듣다`, `짓다`, `파랗다`, `모르다`, `바쁘다`, `만들다`).
Đầu ra: Model quy tắc `MorphologyRule.kt`, bộ quy tắc `IrregularVerbRules.kt`, và test suite `IrregularVerbRulesTest.kt`.
</objective>

<context>
Xem xét các tài liệu:
- project-meta/spec/ARCHITECTURE.md (Mục 4. Bộ máy giải phân tích hình thái)
- project-meta/spec/GLOSSARY.md (Khái niệm Deconjugation và CandidateLemma)
</context>

<tasks>

<task type="auto">
  <name>Định nghĩa cấu trúc dữ liệu MorphologyRule và RuleCategory</name>
  <files>app/src/main/java/com/hanviet/etymoai/domain/morphology/MorphologyRule.kt</files>
  <action>
    Tạo class và enum:
    - `enum class IrregularGroup`: `BIEUP` (ㅂ), `DIGEUT` (ㄷ), `SIOT` (ㅅ), `HIEUT` (ㅎ), `REU` (르), `EUU` (으), `RIEUL` (ㄹ).
    - `data class MorphologyRule`: Gồm `ruleId`, `group`, `surfacePattern`, `lemmaTransformFn: (String) -> String`, `confidenceWeight: Float`, `explanationVi: String`.
    - Tạo tiện ích xử lý âm tiết tiếng Hàn: Phân tách âm tiết thành Chosung, Jungseong, Jongseong (Jamo decomposition) để thao tác ở cấp độ ngữ âm học chính xác.
  </action>
  <verify>Viết test kiểm tra hàm phân tách và ghép lại Jamo (Decompose/Compose) hoạt động chuẩn với mọi ký tự Hangul.</verify>
  <done>Công cụ thao tác Jamo hoàn thành và không làm hỏng ký tự Unicode.</done>
</task>

<task type="auto">
  <name>Hiện thực hóa 7 bộ luật deconjugation cho các nhóm bất quy tắc</name>
  <files>
    - app/src/main/java/com/hanviet/etymoai/domain/morphology/IrregularVerbRules.kt
    - app/src/test/java/com/hanviet/etymoai/domain/morphology/IrregularVerbRulesTest.kt
  </files>
  <action>
    Triển khai chi tiết trong `IrregularVerbRules`:
    1. `ㅂ` bất quy tắc: Biến đổi `워/와` ngược về gốc có patchim `ㅂ` (`추워` -> `춥다`, `도와` -> `돕다`).
    2. `ㄷ` bất quy tắc: Biến đổi patchim `ㄹ` trước nguyên âm ngược về `ㄷ` (`들어요` -> `듣다`, `걸어요` -> `걷다`).
    3. `ㅅ` bất quy tắc: Khôi phục patchim `ㅅ` khi bị rụng trước nguyên âm (`지어요` -> `짓다`, `나아요` -> `낫다`).
    4. `ㅎ` bất quy tắc: Xử lý biến âm `애/얘` trở về `ㅎ` (`파래` -> `파랗다`, `하얘` -> `하얗다`).
    5. `르` bất quy tắc: Xử lý dạng lặp `ㄹㄹ` trở về nguyên thể `르` (`몰라요` -> `모르다`, `빨라` -> `빠르다`).
    6. `으` bất quy tắc: Khôi phục nguyên âm `ㅡ` khi bị nuốt bởi `아/어` (`바빠` -> `바쁘다`, `예뻐` -> `예쁘다`).
    7. `ㄹ` biến đổi: Khôi phục patchim `ㄹ` bị rơi trước `ㄴ, ㅂ, ㅅ` (`만듭니다` -> `만들다`, `아는` -> `알다`).
  </action>
  <verify>Chạy test suite `IrregularVerbRulesTest.kt` kiểm tra ít nhất 5 từ tiêu biểu cho mỗi nhóm bất quy tắc.</verify>
  <done>Tất cả 7 nhóm quy tắc đều vượt qua bài kiểm thử với kết quả từ gốc chính xác.</done>
</task>

</tasks>

<verification>
Sau khi hoàn thành:
- [ ] 100% test cases trong `IrregularVerbRulesTest.kt` chuyển màu xanh.
- [ ] Xử lý chính xác cả từ có nhiều khả năng phân tích (ví dụ: `들었어요` -> có thể từ `듣다` hoặc `들다`).
</verification>

<success_criteria>
- [ ] Tất cả nhiệm vụ đều được xác thực bằng unit test.
- [ ] Các tiêu chí must-haves được đáp ứng trọn vẹn.
</success_criteria>
