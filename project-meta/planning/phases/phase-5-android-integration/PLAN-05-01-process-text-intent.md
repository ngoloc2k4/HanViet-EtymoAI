---
phase: 5
plan: 1
wave: 1
depends_on: ["1.2", "2.2", "3.1"]
files_modified:
  - "app/src/main/AndroidManifest.xml"
  - "app/src/main/java/com/hanviet/etymoai/system/ProcessTextActivity.kt"
autonomous: true
user_setup: []

must_haves:
  truths:
    - "AndroidManifest.xml đăng ký IntentFilter ACTION_PROCESS_TEXT nhận văn bản bôi đen từ ứng dụng ngoài."
    - "ProcessTextActivity khởi chạy trong suốt (transparent theme), trích xuất văn bản từ Intent và chuyển tiếp đến Quick Lookup Engine mà không cần quyền SYSTEM_ALERT_WINDOW."
  artifacts:
    - "app/src/main/AndroidManifest.xml"
    - "app/src/main/java/com/hanviet/etymoai/system/ProcessTextActivity.kt"
---

# Plan 5.1: Tiếp Nhận Văn Bản Toàn Hệ Thống (Android Process Text Intent)

<objective>
Cấu hình và triển khai thành phần nhận diện văn bản toàn hệ thống thông qua `android.intent.action.PROCESS_TEXT`, cho phép người dùng bôi đen văn bản tiếng Hàn ở bất kỳ ứng dụng nào khác trên Android và kích hoạt tính năng tra cứu từ nguyên ngay lập tức.

Mục đích: Cung cấp tính năng tra nhanh tiện dụng tối đa mà TUYỆT ĐỐI KHÔNG CẦN xin quyền nguy hiểm `SYSTEM_ALERT_WINDOW` (quyền vẽ đè màn hình vốn làm giảm hiệu năng và tiềm ẩn rủi ro bảo mật).
Đầu ra: Cấu hình `AndroidManifest.xml` và Activity tiếp nhận `ProcessTextActivity.kt`.
</objective>

<context>
Xem xét các tài liệu:
- project-meta/spec/ARCHITECTURE.md (Mục 5. Tích hợp Hệ thống Android)
- project-meta/history/LESSONS_LEARNED_FROM_V1.md (Bài học về tích hợp hệ thống)
</context>

<tasks>

<task type="auto">
  <name>Khai báo PROCESS_TEXT Intent Filter trong AndroidManifest.xml</name>
  <files>app/src/main/AndroidManifest.xml</files>
  <action>
    Cập nhật file `AndroidManifest.xml`:
    - Khai báo activity `com.hanviet/etymoai.system.ProcessTextActivity`:
      - Gán theme trong suốt: `android:theme="@style/Theme.HanVietEtymoAI.Transparent"`.
      - Khai báo `android:exported="true"`.
      - Khai báo `android:label="@string/action_process_text_lookup"` (Tên hiển thị trên menu context: *"Tra Từ Điển Hán-Việt"*).
      - Đăng ký `intent-filter` với action `android.intent.action.PROCESS_TEXT` và category `android.intent.category.DEFAULT`, mimeType `text/plain`.
  </action>
  <verify>Kiểm tra cú pháp manifest bằng aapt2 hoặc script kiểm tra XML.</verify>
  <done>Manifest cấu hình hợp lệ, sẵn sàng nhận action từ hệ điều hành Android.</done>
</task>

<task type="auto">
  <name>Hiện thực hóa ProcessTextActivity và bóc tách dữ liệu Intent</name>
  <files>app/src/main/java/com/hanviet/etymoai/system/ProcessTextActivity.kt</files>
  <action>
    Triển khai `ProcessTextActivity`:
    - Kế thừa `ComponentActivity`.
    - Trích xuất chuỗi văn bản được gửi qua:
      `val selectedText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()`.
    - Kiểm tra tính hợp lệ: Nếu rỗng hoặc chỉ toàn khoảng trắng, kết thúc activity nhẹ nhàng (`finish()`).
    - Khởi chạy `QuickLookupBottomSheet` truyền `selectedText` vào và lắng nghe sự kiện đóng để giải phóng activity ngay lập tức.
  </action>
  <verify>Viết Robolectric test giả lập gửi Intent PROCESS_TEXT chứa chuỗi `"기분"` và xác nhận Activity nhận đúng dữ liệu.</verify>
  <done>ProcessTextActivity xử lý an toàn mọi dạng Intent ngoại lệ mà không gây crash ứng dụng.</done>
</task>

</tasks>

<verification>
Sau khi hoàn thành:
- [ ] Bôi đen chuỗi chữ trên trình duyệt hoặc tài liệu xuất hiện mục "Tra Từ Điển Hán-Việt".
- [ ] Intent được chuyển tiếp chính xác đến bộ xử lý trong thời gian < 20ms.
</verification>

<success_criteria>
- [ ] Tất cả nhiệm vụ đều được xác thực bằng Robolectric test.
- [ ] Các tiêu chí must-haves được đáp ứng trọn vẹn.
</success_criteria>
