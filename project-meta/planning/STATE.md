# Trạng Thái Dự Án (STATE.md)

Tài liệu này phản ánh trạng thái thực thi hiện tại của **HanViet-EtymoAI** theo phương pháp luận GSD.

---

## 1. Trạng Thái Hiện Tại (Current Execution State)

- **Trạng thái quy trình**: **IN EXECUTION - PHASE 0 IN PROGRESS**
- **Khóa kế hoạch (Planning Lock)**: **LOCKED** (Đặc tả đã chốt tại `SPEC.md`, sẵn sàng bước vào thực thi)
- **Giai đoạn hiện tại**: **Phase 0 - Data Feasibility & ETL Spike**
- **Kế hoạch đã hoàn thành**: `PLAN-00-01-open-datasets-audit.md` (Summary: `01-SUMMARY.md`)
- **Kế hoạch tiếp theo**: `PLAN-00-02-schema-and-etl-spike.md`
- **Mã nguồn tiền nhiệm**: Đã đúc kết bài học tại `project-meta/history/LESSONS_LEARNED_FROM_V1.md`

---

## 2. Tiến Độ Chi Tiết Các Giai Đoạn (Phase Progress)

| Giai đoạn | Tiêu đề | Kế hoạch hoàn thành | Trạng thái |
| :---: | :--- | :---: | :---: |
| **Phase 0** | Data Feasibility & ETL Spike | 1/2 | **IN PROGRESS** |
| **Phase 1** | Dual-DB Architecture & Search Core | 0/2 | BACKLOG |
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
  - Thiết lập thư mục cô lập `project-meta/` để chứa toàn bộ spec, roadmap, chat history, bài học V1, hoàn toàn không chạm lẫn vào workspace ứng dụng Android.
  - Phân rã 8 giai đoạn chi tiết với các kế hoạch nguyên tử theo chuẩn GSD Planner.
  - **Hoàn thành Plan 0.1 (Open Datasets Audit)**:
    - Tạo `data-pipeline/sources/SOURCES.md` quy định 4 nguồn dữ liệu mở (NIKL, Unihan, Wiktionary, Curated StarDict) kèm chính sách bản quyền CC BY-SA & Unicode License.
    - Viết script `data-pipeline/scripts/audit_sources.py` thẩm định thực nghiệm: 100% đạt chuẩn Unicode NFC, 100% độ phủ nghĩa tiếng Việt, tỷ lệ từ Hán-Hàn đạt 66.7% (vượt ngưỡng yêu cầu 60%).
