---
phase: 5
plan: 2
wave: 2
depends_on: ["5.1"]
files_modified:
  - "app/src/main/java/com/hanviet/etymoai/ui/quicklookup/QuickLookupBottomSheet.kt"
  - "app/src/main/java/com/hanviet/etymoai/ui/quicklookup/QuickLookupViewModel.kt"
autonomous: true
user_setup: []

must_haves:
  truths:
    - "QuickLookupBottomSheet hiển thị kết quả tra từ nhanh trong < 100ms khi được gọi từ ứng dụng ngoài."
    - "Hiển thị đồng thời: Nghĩa tiếng Việt, phân đoạn Hán tự, dạng thức ngữ pháp nguyên thể, và nút lưu nhanh vào sổ tay cá nhân."
  artifacts:
    - "app/src/main/java/com/hanviet/etymoai/ui/quicklookup/QuickLookupBottomSheet.kt"
    - "app/src/main/java/com/hanviet/etymoai/ui/quicklookup/QuickLookupViewModel.kt"
---

# Plan 5.2: Giao Diện Tra Cứu Nhanh Đáy Màn Hình (Quick Lookup Bottom Sheet)

<objective>
Xây dựng thành phần giao diện BottomSheet siêu nhẹ `QuickLookupBottomSheet` được tối ưu hóa để hiển thị tức thì khi người dùng kích hoạt `ACTION_PROCESS_TEXT`, hỗ trợ đầy đủ các tính năng: hiển thị nghĩa, chiết tự Hán-Hàn-Việt, phát âm, và 1-tap lưu vào sổ tay cá nhân.

Mục đích: Giúp người dùng đọc sách báo, xem tin tức hoặc học ngoại ngữ trên ứng dụng khác mà không phải chuyển đổi ứng dụng qua lại, nâng tầm năng suất học tập.
Đầu ra: `QuickLookupBottomSheet.kt` và ViewModel tương ứng `QuickLookupViewModel.kt`.
</objective>

<context>
Xem xét các tài liệu:
- project-meta/spec/ARCHITECTURE.md (Mục 1 & 5. UILayer và Android System Integration)
- project-meta/history/LESSONS_LEARNED_FROM_V1.md (Bài học về trải nghiệm popup)
</context>

<tasks>

<task type="auto">
  <name>Xây dựng QuickLookupViewModel kết nối SearchPipeline</name>
  <files>app/src/main/java/com/hanviet/etymoai/ui/quicklookup/QuickLookupViewModel.kt</files>
  <action>
    Triển khai `QuickLookupViewModel`:
    - Nhận chuỗi từ `selectedText`.
    - Gọi song song:
      1. `SearchPipeline.search(selectedText)` -> Lấy kết quả từ điển và biến thể hình thái.
      2. `EtymologyRepository.getEtymologyForWord(...)` -> Lấy phân đoạn chữ Hán và âm Hán-Việt.
    - Cung cấp hàm `saveToWorkspace(wordId: Long)`: Lưu nhanh vào sổ tay người dùng chỉ với 1 cú chạm.
    - Xử lý trạng thái Loading mượt mà (Skeleton loader) và trạng thái không tìm thấy từ (No Result).
  </action>
  <verify>Viết test xác nhận ViewModel phát ra State đầy đủ trong vòng 1 flow emission.</verify>
  <done>QuickLookupViewModel hoạt động mượt mà, phản hồi tức thì.</done>
</task>

<task type="auto">
  <name>Xây dựng QuickLookupBottomSheet bằng Jetpack Compose</name>
  <files>app/src/main/java/com/hanviet/etymoai/ui/quicklookup/QuickLookupBottomSheet.kt</files>
  <action>
    Triển khai giao diện BottomSheet:
    - Sử dụng `ModalBottomSheetLayout` hoặc `ModalBottomSheet` của Material3.
    - Bố cục thông tin cô đọng:
      - Tiêu đề: Từ gốc (`headword`), phát âm IPA/Hán-Việt, nút loa nghe âm thanh TTS.
      - Phần giữa: Thẻ chiết tự Hán tự thu gọn (Âm Hàn - Chữ Hán - Âm Hán-Việt in đậm).
      - Phần dưới: 2 nét nghĩa tiếng Việt phổ biến nhất.
      - Góc trên bên phải: Nút Bookmark (Ngôi sao) để lưu ngay vào Flashcard SRS.
      - Nút "Mở rộng": Chuyển sang màn hình chính của ứng dụng để xem toàn bộ thông tin chi tiết.
  </action>
  <verify>Viết Compose Preview và kiểm tra thao tác vuốt đóng / mở rộng BottomSheet.</verify>
  <done>Giao diện tra nhanh hoàn thành, thiết kế hiện đại, tinh gọn và tối ưu trải nghiệm người dùng.</done>
</task>

</tasks>

<verification>
Sau khi hoàn thành:
- [ ] BottomSheet xuất hiện mượt mà ngay khi bôi đen từ ở app ngoài.
- [ ] Thao tác bấm nút Bookmark lưu ngay từ vào `user_workspace.db` mà không gián đoạn giao diện.
</verification>

<success_criteria>
- [ ] Tất cả nhiệm vụ đều được xác thực bằng Compose preview và test.
- [ ] Các tiêu chí must-haves được đáp ứng trọn vẹn.
</success_criteria>
