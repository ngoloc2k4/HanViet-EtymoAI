---
phase: 1
plan: 1
wave: 1
depends_on: ["0.2"]
files_modified:
  - "app/src/main/java/com/hanviet/etymoai/data/db/core/CoreDatabaseHelper.kt"
  - "app/src/main/java/com/hanviet/etymoai/data/db/workspace/WorkspaceDatabase.kt"
  - "app/src/main/java/com/hanviet/etymoai/di/DatabaseModule.kt"
autonomous: true
user_setup: []

must_haves:
  truths:
    - "CoreDatabaseHelper mở kết nối read-only đến assets/dictionary_core.db an toàn."
    - "WorkspaceDatabase khởi tạo Room DB độc lập không đụng chạm đến core DB."
    - "Tách biệt hoàn toàn hai luồng DI: Core (Read-Only SQLite) và Workspace (Writable Room)."
  artifacts:
    - "app/src/main/java/com/hanviet/etymoai/data/db/core/CoreDatabaseHelper.kt"
    - "app/src/main/java/com/hanviet/etymoai/data/db/workspace/WorkspaceDatabase.kt"
---

# Plan 1.1: Thiết Lập Nền Tảng Kiến Trúc Dual-Database (Dual-DB Foundation)

<objective>
Xây dựng lớp truy cập dữ liệu tách rời trên Android Client theo đặc tả Dual-Database Architecture: `CoreDatabaseHelper` mở file SQLite chỉ đọc `dictionary_core.db` từ Assets, và `WorkspaceDatabase` quản lý cơ sở dữ liệu Room cục bộ cho người dùng.

Mục đích: Ngăn chặn triệt để rủi ro xung đột hoặc xóa mất dữ liệu người dùng khi cập nhật từ điển core.
Đầu ra: `CoreDatabaseHelper.kt`, `WorkspaceDatabase.kt`, và DI module Hilt/Koin kết nối 2 nguồn.
</objective>

<context>
Xem xét các tài liệu:
- project-meta/spec/ARCHITECTURE.md (Mục 2. Kiến trúc Dual-Database)
- project-meta/history/LESSONS_LEARNED_FROM_V1.md (Bài học về gộp chung DB ở V1)
</context>

<tasks>

<task type="auto">
  <name>Tạo CoreDatabaseHelper mở SQLite Read-Only</name>
  <files>app/src/main/java/com/hanviet/etymoai/data/db/core/CoreDatabaseHelper.kt</files>
  <action>
    Triển khai `CoreDatabaseHelper`:
    - Quản lý copy file `dictionary_core.db` từ assets vào thư mục nội bộ ứng dụng (nếu chưa có hoặc khi hash phiên bản thay đổi).
    - Mở kết nối bằng cờ `SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS`.
    - Cung cấp API truy vấn thread-safe thông qua Coroutines Dispatchers.IO.
    - Tuyệt đối không cho phép bất kỳ câu lệnh INSERT/UPDATE/DELETE nào trên `CoreDatabaseHelper`.
  </action>
  <verify>Viết unit test xác nhận mở DB thành công và ném lỗi `SQLiteException` nếu cố ý thực thi lệnh ghi.</verify>
  <done>CoreDatabaseHelper hoạt động ổn định ở chế độ chỉ đọc.</done>
</task>

<task type="auto">
  <name>Khởi tạo WorkspaceDatabase bằng Android Room</name>
  <files>app/src/main/java/com/hanviet/etymoai/data/db/workspace/WorkspaceDatabase.kt</files>
  <action>
    Triển khai `WorkspaceDatabase` kế thừa `RoomDatabase`:
    - Định nghĩa các entity ban đầu: `SavedWordEntity`, `SearchHistoryEntity`, `AiCacheEntity`.
    - Tạo các DAO tương ứng: `SavedWordDao`, `SearchHistoryDao`.
    - Đặt tên file CSDL vật lý: `user_workspace.db`.
    - Cấu hình cơ chế Room migration tự động hoặc fallbackToDestructiveMigration cho giai đoạn dev ban đầu.
  </action>
  <verify>Viết unit test kiểm tra ghi 1 bản ghi vào `SavedWordDao` và đọc lại thành công.</verify>
  <done>WorkspaceDatabase hoạt động độc lập, dữ liệu ghi thành công không chạm vào Core DB.</done>
</task>

</tasks>

<verification>
Sau khi hoàn thành:
- [ ] Cả hai đối tượng Database được khởi tạo đồng thời mà không xung đột file lock.
- [ ] Dữ liệu người dùng lưu vào `user_workspace.db` tồn tại nguyên vẹn khi đóng/mở lại app.
</verification>

<success_criteria>
- [ ] Tất cả nhiệm vụ đều được xác thực bằng unit test.
- [ ] Các tiêu chí must-haves được đáp ứng trọn vẹn.
</success_criteria>
