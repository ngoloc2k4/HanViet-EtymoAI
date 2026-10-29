---
phase: 4
plan: 1
wave: 1
depends_on: ["1.1"]
files_modified:
  - "app/src/main/java/com/hanviet/etymoai/data/db/workspace/entity/SavedWordEntity.kt"
  - "app/src/main/java/com/hanviet/etymoai/data/db/workspace/entity/PersonalNoteEntity.kt"
  - "app/src/main/java/com/hanviet/etymoai/data/repository/UserWorkspaceRepository.kt"
  - "app/src/test/java/com/hanviet/etymoai/data/repository/UserWorkspaceRepositoryTest.kt"
autonomous: true
user_setup: []

must_haves:
  truths:
    - "Lưu trữ từ vựng yêu thích, thẻ tag và ghi chú cá nhân hoạt động độc lập trên user_workspace.db."
    - "Hỗ trợ xuất (export) và nhập (import) toàn bộ sổ tay từ vựng của người dùng dưới dạng JSON chuẩn hóa."
  artifacts:
    - "app/src/main/java/com/hanviet/etymoai/data/db/workspace/entity/SavedWordEntity.kt"
    - "app/src/main/java/com/hanviet/etymoai/data/repository/UserWorkspaceRepository.kt"
---

# Plan 4.1: Quản Lý Sổ Tay Từ Vựng & Dữ Liệu Cá Nhân (User Workspace Storage)

<objective>
Hoàn thiện tầng dữ liệu Room cho cơ sở dữ liệu `user_workspace.db`, cung cấp đầy đủ các thao tác CRUD cho từ vựng đã lưu, hệ thống gắn nhãn phân loại (tags), ghi chú học tập dạng Markdown, và tính năng xuất/nhập file sao lưu JSON an toàn.

Mục đích: Đem lại cho người học một không gian lưu trữ từ vựng riêng tư, ổn định, không bao giờ bị ghi đè khi cập nhật gói từ điển lõi.
Đầu ra: Các Room Entity, DAO, Repository `UserWorkspaceRepository.kt`, và test suite `UserWorkspaceRepositoryTest.kt`.
</objective>

<context>
Xem xét các tài liệu:
- project-meta/spec/ARCHITECTURE.md (Mục 2.2 Sơ đồ user_workspace.db)
- project-meta/spec/GLOSSARY.md (Khái niệm UserWorkspace)
</context>

<tasks>

<task type="auto">
  <name>Thiết kế Room Entities và DAOs cho Saved Words, Tags và Notes</name>
  <files>
    - app/src/main/java/com/hanviet/etymoai/data/db/workspace/entity/SavedWordEntity.kt
    - app/src/main/java/com/hanviet/etymoai/data/db/workspace/entity/PersonalNoteEntity.kt
    - app/src/main/java/com/hanviet/etymoai/data/db/workspace/dao/SavedWordDao.kt
  </files>
  <action>
    Triển khai cấu trúc dữ liệu Room:
    1. `SavedWordEntity`: `id (PK autoincrement)`, `stableKey (UK)`, `headwordSnapshot`, `meaningSnapshot`, `phoneticVi`, `originType`, `savedAtTimestamp`.
    2. `WordTagEntity`: `tagId (PK)`, `tagName`, `tagColorHex`.
    3. `WordTagCrossRef`: Bảng quan hệ nhiều - nhiều giữa từ đã lưu và nhãn phân loại.
    4. `PersonalNoteEntity`: `noteId (PK)`, `savedWordId (FK)`, `markdownContent`, `updatedAtTimestamp`.
    5. Viết DAO với các truy vấn Reactive `Flow<List<SavedWordWithTags>>`.
  </action>
  <verify>Viết Room in-memory test kiểm tra lưu từ, gắn tag, và thêm ghi chú cá nhân.</verify>
  <done>Các bảng và DAO trong Room hoạt động hoàn hảo với liên kết khóa ngoại.</done>
</task>

<task type="auto">
  <name>Xây dựng UserWorkspaceRepository với chức năng Export/Import JSON</name>
  <files>
    - app/src/main/java/com/hanviet/etymoai/data/repository/UserWorkspaceRepository.kt
    - app/src/test/java/com/hanviet/etymoai/data/repository/UserWorkspaceRepositoryTest.kt
  </files>
  <action>
    Triển khai `UserWorkspaceRepository`:
    - Quản lý các hàm nghiệp vụ: `saveWord`, `removeWord`, `toggleTag`, `updateNote`.
    - Triển khai hàm `exportWorkspaceToJson(): String`: Xuất toàn bộ danh sách từ, tag, ghi chú sang định dạng JSON phiên bản `v1` (tương thích sao lưu Google Drive / thẻ nhớ ngoài).
    - Triển khai hàm `importWorkspaceFromJson(jsonString: String, strategy: ConflictStrategy)`: Hỗ trợ nạp lại dữ liệu với tùy chọn gộp (Merge) hoặc ghi đè (Overwrite).
  </action>
  <verify>Chạy test xuất dữ liệu ra chuỗi JSON rồi import lại trên DB trống; xác nhận dữ liệu được khôi phục 100%.</verify>
  <done>Hệ thống quản lý dữ liệu người dùng và sao lưu JSON hoàn thành.</done>
</task>

</tasks>

<verification>
Sau khi hoàn thành:
- [ ] Dữ liệu từ đã lưu và ghi chú được bảo toàn nguyên vẹn.
- [ ] File backup JSON có thể xuất và nhập lại mà không làm hỏng khóa định danh `stable_key`.
</verification>

<success_criteria>
- [ ] Tất cả nhiệm vụ đều được xác thực bằng unit test.
- [ ] Các tiêu chí must-haves được đáp ứng trọn vẹn.
</success_criteria>
