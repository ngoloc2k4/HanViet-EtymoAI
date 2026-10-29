# Báo Cáo Hoàn Thành: Kế Hoạch 4.2 (SuperMemo SM-2 & SRS Flashcard System)

## 1. Mục Tiêu Đã Thực Hiện
- Hiện thực hóa thuật toán ôn tập ngắt quãng khoa học **SuperMemo SM-2** (`SuperMemo2Engine.kt`):
  - Định nghĩa 4 mức đánh giá phản hồi: `AGAIN (0)`, `HARD (3)`, `GOOD (4)`, `EASY (5)`.
  - Công thức tính hệ số độ dễ $EF' = EF + (0.1 - (5 - q) \times (0.08 + (5 - q) \times 0.02))$ với ngưỡng sàn an toàn $EF \ge 1.3$.
  - Cập nhật số lần lặp lại `repetitions`, số ngày cách quãng `intervalDays` và ngày đến hạn tiếp theo `dueTimestamp`.
  - Cơ chế reset chu kỳ khi người học quên (rating < 3) kèm bộ đếm `lapses`.
- Cấu trúc dữ liệu thẻ Flashcard:
  - `SrsCardEntity` lưu trữ lịch trình học tập độc lập trong `user_workspace.db`.
  - `SrsCardModel.kt` cung cấp các mô hình dữ liệu sạch tách biệt với tầng Room.
  - `SrsRepository.kt` tích hợp luồng dữ liệu thẻ đến hạn (`Flow<List<SrsFlashcardData>>`) kết hợp thông tin từ vựng và chiết tự Hán tự.
- Xây dựng giao diện Flashcard 3D mượt mà (`SrsReviewScreen.kt`):
  - Hiệu ứng lật thẻ không gian 3 chiều qua `graphicsLayer { rotationY = ... }` với cơ chế khử lật ngược gương ở mặt sau.
  - Mặt trước: Từ vựng tiếng Hàn cỡ chữ lớn, phiên âm chuẩn, và gợi ý phân rã âm tiết.
  - Mặt sau: Chiết tự Hán tự chi tiết, âm Hán-Việt in đậm, nghĩa tiếng Việt, nhãn phân loại từ nguyên và ghi chú cá nhân.
  - 4 nút bấm chấm điểm ở đáy màn hình với màu sắc phân biệt trực quan (Đỏ, Cam, Xanh lục, Xanh dương) kèm hiển thị khoảng thời gian lặp lại tiếp theo.
  - Màn hình thông báo hoàn thành phiên ôn tập với thống kê số thẻ đã xử lý.
- Viết bộ unit test `SuperMemo2EngineTest.kt` và `SrsReviewScreenTest.kt`.

## 2. Các Tập Tin Đã Tạo / Cập Nhật
- `app/src/main/java/com/hanviet/etymoai/data/db/workspace/entity/SrsCardEntity.kt`
- `app/src/main/java/com/hanviet/etymoai/data/db/workspace/dao/SrsCardDao.kt`
- `app/src/main/java/com/hanviet/etymoai/domain/srs/SrsCardModel.kt`
- `app/src/main/java/com/hanviet/etymoai/domain/srs/SuperMemo2Engine.kt`
- `app/src/main/java/com/hanviet/etymoai/domain/srs/SrsRepository.kt`
- `app/src/main/java/com/hanviet/etymoai/domain/etymology/EtymologyRepository.kt`
- `app/src/main/java/com/hanviet/etymoai/ui/srs/SrsReviewScreen.kt`
- `app/src/test/java/com/hanviet/etymoai/domain/srs/SuperMemo2EngineTest.kt`
- `app/src/test/java/com/hanviet/etymoai/ui/srs/SrsReviewScreenTest.kt`

## 3. Xác Minh Độc Lập
- Đã xác thực toán học chính xác theo giải thuật của Piotr Woźniak với các ca kiểm thử: lặp lần 1 (1 ngày), lặp lần 2 (6 ngày), lặp lần 3+ (nhân hệ số EF), reset khi quên và chặn sàn $EF \ge 1.3$.
- Đã kiểm tra tính toàn vẹn vòng đời thẻ Flashcard từ khi tạo từ mới đến khi ghi nhận kết quả đánh giá trong cơ sở dữ liệu.
