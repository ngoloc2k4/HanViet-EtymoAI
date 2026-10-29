# Trạng Thái Dự Án (STATE.md)

Tài liệu này phản ánh trạng thái thực thi hiện tại của **HanViet-EtymoAI** theo phương pháp luận GSD.

---

## 1. Trạng Thái Hiện Tại (Current Execution State)

- **Trạng thái quy trình**: **PHASE 2 COMPLETED - READY FOR PHASE 3**
- **Khóa kế hoạch (Planning Lock)**: **LOCKED** (Đặc tả đã chốt tại `SPEC.md`, sẵn sàng bước vào thực thi)
- **Giai đoạn hiện tại**: **Phase 3 - Contextual Hanja & Etymology Display**
- **Kế hoạch vừa hoàn thành**: `PLAN-02-02-deconjugation-ranking.md` (Summary: `02-SUMMARY.md`)
- **Kế hoạch tiếp theo**: `PLAN-03-01-hanja-graph-segmentation.md`
- **Mã nguồn tiền nhiệm**: Đã đúc kết bài học tại `project-meta/history/LESSONS_LEARNED_FROM_V1.md`

---

## 2. Tiến Độ Chi Tiết Các Giai Đoạn (Phase Progress)

| Giai đoạn | Tiêu đề | Kế hoạch hoàn thành | Trạng thái |
| :---: | :--- | :---: | :---: |
| **Phase 0** | Data Feasibility & ETL Spike | 2/2 | **COMPLETED** |
| **Phase 1** | Dual-DB Architecture & Search Core | 2/2 | **COMPLETED** |
| **Phase 2** | Morphology Rule Engine | 2/2 | **COMPLETED** |
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
  - **Hoàn thành Plan 1.2 (Search Pipeline & 5 Tầng Tra Cứu)**:
    - Triển khai `SearchPipeline.kt` với 5 tầng tra cứu offline dưới 50ms (Exact, Hanja, Precomputed, Morphology, FTS5).
    - Tạo `SearchPipelineTest.kt` với 16 unit tests kiểm tra đa dạng từ đơn, từ ghép, Hán-Hàn và bỏ dấu tiếng Việt.
  - **Hoàn thành Plan 2.1 (Korean Irregular Verb Rules)**:
    - Triển khai `HangulUtil.kt`, `IrregularGroup`, `MorphologyRule`, và `IrregularVerbRules.kt` xử lý 7 nhóm động/tính từ bất quy tắc (ㅂ, ㄷ, ㅅ, ㅎ, 르, 으, ㄹ).
    - Viết `IrregularVerbRulesTest.kt` với 8 unit tests kiểm tra phân rã/ghép âm Hangul và 7 nhóm quy tắc.
    - Sửa lỗi formal ending '-ㅂ니다' trong nhóm RIEUL drop. Đã pass 25/25 unit tests và đóng gói APK Beta #8.
  - **Hoàn thành Plan 2.2 (Deconjugation Ranking & Pipeline Integration)**:
    - Triển khai `CandidateLemma.kt` chuẩn và `MorphologyEngine.kt` kết hợp 7 nhóm bất quy tắc + các đuôi thường.
    - Tích hợp đối chiếu từ điển Core SQLite: tăng điểm tin cậy đối với từ gốc có thực trong từ điển, hạ điểm và lọc bỏ các suy diễn ảo.
    - Tích hợp trực tiếp vào Tầng 4 của `SearchPipeline.kt`.
    - Viết `MorphologyEngineTest.kt` và bổ sung test vào `SearchPipelineTest.kt` kiểm tra giải quyết đa nghĩa (ambiguity resolution: "들었어요" -> "듣다" & "들다").
    - Cập nhật mục từ `들다` vào CSDL mẫu `dictionary_core.db` và vượt qua 100% bài test tính toàn vẹn CSDL.
