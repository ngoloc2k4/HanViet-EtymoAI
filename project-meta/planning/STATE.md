# Trạng Thái Dự Án (STATE.md)

Tài liệu này phản ánh trạng thái thực thi hiện tại của **HanViet-EtymoAI** theo phương pháp luận GSD.

---

## 1. Trạng Thái Hiện Tại (Current Execution State)

- **Trạng thái quy trình**: **IN EXECUTION - PHASE 1 IN PROGRESS**
- **Khóa kế hoạch (Planning Lock)**: **LOCKED** (Đặc tả đã chốt tại `SPEC.md`, sẵn sàng bước vào thực thi)
- **Giai đoạn hiện tại**: **Phase 1 - Dual-DB Architecture & Search Core**
- **Kế hoạch vừa hoàn thành**: `PLAN-01-01-dual-db-foundation.md` (Summary: `01-SUMMARY.md`)
- **Kế hoạch tiếp theo**: `PLAN-01-02-fts5-search-pipeline.md`
- **Mã nguồn tiền nhiệm**: Đã đúc kết bài học tại `project-meta/history/LESSONS_LEARNED_FROM_V1.md`

---

## 2. Tiến Độ Chi Tiết Các Giai Đoạn (Phase Progress)

| Giai đoạn | Tiêu đề | Kế hoạch hoàn thành | Trạng thái |
| :---: | :--- | :---: | :---: |
| **Phase 0** | Data Feasibility & ETL Spike | 2/2 | **COMPLETED** |
| **Phase 1** | Dual-DB Architecture & Search Core | 1/2 | **IN PROGRESS** |
| **Phase 2** | Morphology Rule Engine | 0/2 | BACKLOG |
| **Phase 3** | Contextual Hanja & Etymology Display | 0/2 | BACKLOG |
| **Phase 4** | User Workspace & SRS Flashcards | 0/2 | BACKLOG |
| **Phase 5** | Android System Integration | 0/2 | BACKLOG |
| **Phase 6** | Optional AI Layer (BYOK & Grounding) | 0/2 | BACKLOG |
| **Phase 7** | Packaging, Hardening & CI/CD | 0/2 | BACKLOG |

---

## 3. Nhật Ký Thay Đổi Gần Nhất (Recent Decisions)

- **2026-10-29**:
  - Chốt đặc tả `Idea 2.2` từ `/sdcard/code/viet-kor-dict-idea2.2.md`.
  - Khởi tạo repo độc lập `HanViet-EtymoAI`.
  - Thiết lập thư mục cô lập `project-meta/` để chứa toàn bộ spec, roadmap, chat history, bài học V1.
  - Phân rã 8 giai đoạn chi tiết với các kế hoạch nguyên tử theo chuẩn GSD Planner.
  - **Hoàn thành Phase 0 (Data Feasibility & ETL Spike)**:
    - Plan 0.1: `SOURCES.md` + `audit_sources.py` (66.7% từ Hán-Hàn, 100% chuẩn NFC).
    - Plan 0.2: `core_schema.sql` + `spike_build_core_db.py` + `test_core_db_integrity.py` (100% tests pass, độ trễ 0.025 ms).
  - **Hoàn thành Plan 1.1 (Dual-DB Foundation)**:
    - Triển khai `CoreDatabaseHelper` mở SQLite Read-Only file `dictionary_core.db` từ Assets.
    - Khởi tạo `WorkspaceDatabase` bằng Room gồm các entity `SavedWordEntity`, `SearchHistoryEntity`, `AiCacheEntity` và các DAO tương ứng.
    - Tạo `DatabaseModule` tách rời 2 luồng Core và Workspace độc lập.
    - Viết unit test Robolectric kiểm tra chế độ chỉ đọc và các thao tác Room Database.
