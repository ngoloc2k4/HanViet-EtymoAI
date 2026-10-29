# Trạng Thái Dự Án (STATE.md)

Tài liệu này phản ánh trạng thái thực thi hiện tại của **HanViet-EtymoAI** theo phương pháp luận GSD.

---

## 1. Trạng Thái Hiện Tại (Current Execution State)

- **Trạng thái quy trình**: **PHASE 6 COMPLETED - PROCEEDING TO PHASE 7**
- **Khóa kế hoạch (Planning Lock)**: **LOCKED** (Đặc tả đã chốt tại `SPEC.md`, sẵn sàng hoàn tất)
- **Giai đoạn hiện tại**: **Phase 7 - Packaging, Hardening & CI/CD**
- **Kế hoạch vừa hoàn thành**: `PLAN-06-02-local-grounding-guard.md` (Summary: `02-SUMMARY.md`)
- **Kế hoạch tiếp theo**: `PLAN-07-01-github-actions-ci-cd.md`
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
| **Phase 5** | Android System Integration | 2/2 | **COMPLETED** |
| **Phase 6** | Optional AI Layer (BYOK & Grounding) | 2/2 | **COMPLETED** |
| **Phase 7** | Packaging, Hardening & CI/CD | 0/2 | **IN PROGRESS** |

---

## 3. Nhật Ký Thay Đổi Gần Nhất (Recent Decisions)

- **2026-10-29**:
  - Chốt đặc tả `Idea 2.2` từ `/sdcard/code/viet-kor-dict-idea2.2.md`.
  - Khởi tạo repo độc lập `HanViet-EtymoAI`.
  - Thiết lập thư mục cô lập `project-meta/` để chứa toàn bộ spec, roadmap, chat history, bài học V1.
  - Phân rã 8 giai đoạn chi tiết với các kế hoạch nguyên tử theo chuẩn GSD Planner.
  - **Hoàn thành Phase 0 (Data Feasibility & ETL Spike)** (2/2 plans).
  - **Hoàn thành Phase 1 (Dual-DB Architecture & Search Core)** (2/2 plans).
  - **Hoàn thành Phase 2 (Morphology Rule Engine)** (2/2 plans).
  - **Hoàn thành Phase 3 (Contextual Hanja & Etymology Display)** (2/2 plans, APK #12 released).
  - **Hoàn thành Phase 4 (User Workspace & SRS Flashcards)** (2/2 plans, APK #14 released).
  - **Hoàn thành Phase 5 (Android System Integration)** (2/2 plans, 62/62 tests pass, APK #20 released).
  - **Hoàn thành Phase 6 (Optional AI Layer - BYOK & Grounding)**:
    - Plan 6.1: `AiProvider.kt`, `SecureApiKeyStorage.kt` (AES-256-GCM), `OpenAiCompatibleClient.kt`, `GeminiClient.kt`, `ApiKeyStorageTest.kt`, `AiProviderClientTest.kt`.
    - Plan 6.2: `EtymologyPromptBuilder.kt`, `GroundingGuard.kt`, `GroundingGuardTest.kt` (chống ảo giác chữ Hán, phát hiện chữ Hán lạ, tích hợp AiCache).
    - Cập nhật DI trong `DatabaseModule.kt`.

    - Triển khai `HanjaSegmentModel.kt` (`OriginType`, `VerificationStatus`, `HanjaMaster`, `HanjaSegment`, `WordEtymology`, `EtymologyFamilyNode`).
    - Triển khai `EtymologyRepository.kt` hỗ trợ truy vấn chiết tự và trích xuất đồ thị họ từ vựng theo chữ Hán chung (`getEtymologyFamily`).
    - Viết `EtymologyRepositoryTest.kt` kiểm định trích xuất phân đoạn `기분` (氣 + 分) và họ từ của `學` (`학생`, `학교`).
    - Xây dựng hệ thống UI Jetpack Compose: `OriginTypeBadge`, `VerificationStatusBadge`, `EtymologyBreakdownView`, `WordDetailScreen` tích hợp BottomSheet phả hệ.
    - Viết `EtymologyViewTest.kt` kiểm định model và UI logic.
