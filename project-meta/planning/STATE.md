# Trạng Thái Dự Án (STATE.md)

Tài liệu này phản ánh trạng thái thực thi hiện tại của **HanViet-EtymoAI** theo phương pháp luận GSD.

---

## 1. Trạng Thái Hiện Tại (Current Execution State)

- **Trạng thái quy trình**: **PHASE 4 COMPLETED - READY FOR PHASE 5**
- **Khóa kế hoạch (Planning Lock)**: **LOCKED** (Đặc tả đã chốt tại `SPEC.md`, sẵn sàng bước vào thực thi)
- **Giai đoạn hiện tại**: **Phase 5 - Android System Integration**
- **Kế hoạch vừa hoàn thành**: `PLAN-04-02-srs-sm2-review-system.md` (Summary: `02-SUMMARY.md`)
- **Kế hoạch tiếp theo**: `PLAN-05-01-process-text-intent.md`
- **Mã nguồn tiền nhiệm**: Đã đúc kết bài học tại `project-meta/history/LESSONS_LEARNED_FROM_V1.md`

---

## 2. Tiến Độ Chi Tiết Các Giai Đoạn (Phase Progress)

| Giai đoạn | Tiêu đề | Kế hoạch hoàn thành | Trạng thái |
| :---: | :--- | :---: | :---: |
| **Phase 0** | Data Feasibility & ETL Spike | 2/2 | **COMPLETED** |
| **Phase 1** | Dual-DB Architecture & Search Core | 2/2 | **COMPLETED** |
| **Phase 2** | Morphology Rule Engine | 2/2 | **COMPLETED** |
| **Phase 3** | Contextual Hanja & Etymology Display | 2/2 | **COMPLETED** |
| **Phase 4** | User Workspace & SRS Flashcards | 2/2 | **COMPLETED** |
| **Phase 5** | Android System Integration | 0/2 | **IN PROGRESS** |
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
  - **Hoàn thành Phase 3 (Contextual Hanja & Etymology Display)**:
    - Triển khai `HanjaSegmentModel.kt`, `EtymologyRepository.kt` hỗ trợ truy vấn cấu trúc từ nguyên và cây gia đình chữ Hán qua Reactive Flow.
    - Xây dựng giao diện Jetpack Compose: `OriginTypeBadge`, `VerificationStatusBadge`, `EtymologyBreakdownView`, `WordDetailScreen` với BottomSheet tra cứu họ hàng chữ Hán.
    - Đã xác thực 100% unit tests và phát hành bản Beta APK #12 trên GitHub Releases `beta-latest`.
  - **Bắt đầu Phase 4 (User Workspace & SRS Flashcards)**:
    - Thực thi Plan 4.1: Quản lý sổ tay từ vựng, nhãn (tags), ghi chú cá nhân (notes) và xuất/nhập sao lưu JSON.
    - Thực thi Plan 4.2: Thuật toán SuperMemo SM-2 và màn hình Flashcard SRS.

    - Triển khai `HanjaSegmentModel.kt` (`OriginType`, `VerificationStatus`, `HanjaMaster`, `HanjaSegment`, `WordEtymology`, `EtymologyFamilyNode`).
    - Triển khai `EtymologyRepository.kt` hỗ trợ truy vấn chiết tự và trích xuất đồ thị họ từ vựng theo chữ Hán chung (`getEtymologyFamily`).
    - Viết `EtymologyRepositoryTest.kt` kiểm định trích xuất phân đoạn `기분` (氣 + 分) và họ từ của `學` (`학생`, `학교`).
    - Xây dựng hệ thống UI Jetpack Compose: `OriginTypeBadge`, `VerificationStatusBadge`, `EtymologyBreakdownView`, `WordDetailScreen` tích hợp BottomSheet phả hệ.
    - Viết `EtymologyViewTest.kt` kiểm định model và UI logic.
