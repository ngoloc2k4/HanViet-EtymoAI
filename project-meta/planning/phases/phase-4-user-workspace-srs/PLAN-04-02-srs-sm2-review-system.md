---
phase: 4
plan: 2
wave: 2
depends_on: ["4.1"]
files_modified:
  - "app/src/main/java/com/hanviet/etymoai/domain/srs/SuperMemo2Engine.kt"
  - "app/src/main/java/com/hanviet/etymoai/domain/srs/SrsCardModel.kt"
  - "app/src/main/java/com/hanviet/etymoai/ui/srs/SrsReviewScreen.kt"
autonomous: true
user_setup: []

must_haves:
  truths:
    - "Thuật toán SuperMemo SM-2 tính toán chính xác chu kỳ lặp lại (interval), hệ số dễ dàng (ease factor) dựa trên đánh giá của người học (Quality 0-5)."
    - "Màn hình ôn tập Flashcard SRS hỗ trợ lật thẻ xem từ nguyên Hán tự và ghi nhận tiến độ học tập vào user_workspace.db."
  artifacts:
    - "app/src/main/java/com/hanviet/etymoai/domain/srs/SuperMemo2Engine.kt"
    - "app/src/main/java/com/hanviet/etymoai/domain/srs/SrsCardModel.kt"
---

# Plan 4.2: Hệ Thống Ôn Tập Flashcard SRS SuperMemo SM-2 (SRS Review System)

<objective>
Lập trình thuật toán ôn tập ngắt quãng (Spaced Repetition System - SRS) chuẩn SuperMemo SM-2, quản lý lịch trình các thẻ từ vựng đến hạn ôn tập (`due_date`), và xây dựng giao diện lật thẻ Flashcard tương tác mượt mà trong Jetpack Compose.

Mục đích: Biến từ điển thành công cụ ghi nhớ dài hạn, giúp người học khắc sâu từ vựng tiếng Hàn và nguồn gốc Hán-Việt vào trí nhớ vĩnh viễn.
Đầu ra: Class `SuperMemo2Engine.kt`, model `SrsCardModel.kt`, và màn hình ôn tập `SrsReviewScreen.kt`.
</objective>

<context>
Xem xét các tài liệu:
- project-meta/spec/ARCHITECTURE.md (Mục 2.2 Thực thể srs_cards)
- project-meta/spec/GLOSSARY.md (Khái niệm SRS SuperMemo SM-2)
</context>

<tasks>

<task type="auto">
  <name>Hiện thực hóa thuật toán SuperMemo SM-2 nguyên bản</name>
  <files>
    - app/src/main/java/com/hanviet/etymoai/domain/srs/SuperMemo2Engine.kt
    - app/src/main/java/com/hanviet/etymoai/domain/srs/SrsCardModel.kt
  </files>
  <action>
    Triển khai `SuperMemo2Engine`:
    1. Định nghĩa mức đánh giá: `enum class ReviewRating(val quality: Int)`:
       - `AGAIN (0)`: Quên hoàn toàn.
       - `HARD (3)`: Nhớ nhưng vất vả.
       - `GOOD (4)`: Nhớ chuẩn xác sau chút suy nghĩ.
       - `EASY (5)`: Nhớ tức thì hoàn hảo.
    2. Hàm tính toán `calculateNextState(currentCard: SrsCard, rating: ReviewRating, reviewTimestamp: Long): SrsCard`:
       - Công thức Ease Factor: $EF' = EF + (0.1 - (5 - q) \times (0.08 + (5 - q) \times 0.02))$, với $EF \ge 1.3$.
       - Nếu $q < 3$: Reset số lần lặp $repetitions = 0$, $interval = 1$ ngày.
       - Nếu $q \ge 3$:
         - Lần 1: $interval = 1$ ngày.
         - Lần 2: $interval = 6$ ngày.
         - Lần $n > 2$: $interval = interval_{n-1} \times EF'$.
       - Tính ngày đến hạn ôn tiếp theo: $dueTimestamp = reviewTimestamp + interval \times 86400 \times 1000$.
  </action>
  <verify>Viết test suite kiểm tra các chu kỳ lặp lại với các mức rating khác nhau (Again -> Good -> Easy).</verify>
  <done>Thuật toán SM-2 tính toán chuẩn xác theo công thức khoa học của tiến sĩ Piotr Woźniak.</done>
</task>

<task type="auto">
  <name>Xây dựng màn hình Flashcard SRS (SrsReviewScreen)</name>
  <files>app/src/main/java/com/hanviet/etymoai/ui/srs/SrsReviewScreen.kt</files>
  <action>
    Xây dựng giao diện Jetpack Compose:
    - Hiệu ứng lật thẻ 3D (3D Card Flip Animation) mượt mà:
      - Mặt trước: Từ tiếng Hàn, nút phát âm audio, và gợi ý phân đoạn âm tiết.
      - Mặt sau: Chiết tự chữ Hán, âm Hán-Việt in đậm, nghĩa tiếng Việt, câu ví dụ, và ghi chú cá nhân.
    - 4 nút bấm chấm điểm ở đáy màn hình: `Quên` (Đỏ), `Khó` (Cam), `Tốt` (Xanh lục), `Dễ` (Xanh dương) kèm hiển thị khoảng thời gian ôn tiếp theo (ví dụ: `<1 ngày`, `3 ngày`, `6 ngày`).
    - Cập nhật kết quả vào `user_workspace.db` ngay khi người dùng chọn nút.
  </action>
  <verify>Viết UI test kiểm tra flow lật thẻ và chọn nút rating chuyển sang thẻ tiếp theo.</verify>
  <done>Màn hình ôn tập Flashcard hoàn thiện, trải nghiệm học tập lôi cuốn và mượt mà.</done>
</task>

</tasks>

<verification>
Sau khi hoàn thành:
- [ ] Thẻ ôn tập được tính toán lịch hẹn chính xác, không bị lỗi lệch ngày do múi giờ.
- [ ] Tiến trình ôn tập được lưu tức thì vào Room Database.
</verification>

<success_criteria>
- [ ] Tất cả nhiệm vụ đều được xác thực bằng unit test và UI preview.
- [ ] Các tiêu chí must-haves được đáp ứng trọn vẹn.
</success_criteria>
