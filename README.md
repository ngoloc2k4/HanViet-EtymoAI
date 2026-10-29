# HanViet-EtymoAI: Từ Điển Hán-Hàn-Việt Từ Nguyên & Trợ Lý Ngôn Ngữ Thông Minh

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Platform: Android](https://img.shields.io/badge/Platform-Android_8.0+-green.svg)](https://developer.android.com)
[![Architecture: Dual--Database](https://img.shields.io/badge/Architecture-Dual--Database-orange.svg)](project-meta/spec/ARCHITECTURE.md)
[![Philosophy: Offline--First](https://img.shields.io/badge/Philosophy-Offline--First-red.svg)](project-meta/spec/SPEC.md)

**HanViet-EtymoAI** là ứng dụng từ điển tiếng Hàn - tiếng Việt mã nguồn mở thế hệ mới, khai thác sức mạnh của **từ nguyên học Hán tự (Hanja)** để giúp người học giải mã bản chất từ vựng, hiểu sâu gốc rễ và ghi nhớ lâu bền thông qua mối tương quan ngữ âm kỳ diệu giữa tiếng Hán-Hàn và tiếng Hán-Việt.

---

## 🌟 Các Tính Năng Nổi Bật

- 🔍 **Tra Cứu 5 Tầng Siêu Tốc (< 50ms)**: Khớp chính xác (Exact Match), nhận diện gõ tắt Chosung (`ㄱㅂ`), dạng thức chia ngữ pháp tính trước, và tìm kiếm tiếng Việt không dấu (`"tam trang"` $\to$ `기분`).
- 🧩 **Bộ Máy Giải Phân Tích Hình Thái (Deconjugation Engine)**: Tự động tách ngược 7 nhóm động từ/tính từ bất quy tắc (`ㅂ`, `ㄷ`, `ㅅ`, `ㅎ`, `르`, `으`, `ㄹ`) về dạng nguyên thể kèm giải thích ngữ pháp.
- 🏛️ **Chiết Tự Hán-Hàn-Việt Độc Bản ("Không Bịa Nguồn Gốc")**: Phân tách từng âm tiết ra chữ Hán và âm Hán-Việt tương ứng kèm huy hiệu minh bạch nguồn gốc (`SINO_KOREAN`, `NATIVE_KOREAN`, `LOANWORD`).
- 🌳 **Cây Phả Hệ Từ Vựng (Etymology Graph)**: Nhấp vào một chữ Hán (ví dụ: `學` - *Học*) để mở rộng mạng lưới tất cả các từ tiếng Hàn và tiếng Việt có chung gốc chữ Hán này.
- 🗄️ **Kiến Trúc Hai Cơ Sở Dữ Liệu Tách Biệt (Dual-Database)**:
  - `dictionary_core.db`: Cơ sở dữ liệu SQLite chỉ đọc (Read-only), sinh từ máy tính.
  - `user_workspace.db`: Cơ sở dữ liệu Room có thể ghi, lưu trữ sổ tay từ vựng, ghi chú và tiến độ Flashcard cá nhân. Cập nhật từ điển không bao giờ làm mất dữ liệu người dùng.
- 🧠 **Ôn Tập Flashcard SRS SuperMemo SM-2**: Tự động tính toán khoảng cách ôn tập tối ưu để ghi nhớ vĩnh viễn.
- ⚡ **Tra Cứu Toàn Hệ Thống Không Cần Quyền Nguy Hiểm**: Tích hợp `android.intent.action.PROCESS_TEXT` cho phép bôi đen từ ở bất kỳ app nào (Chrome, Zalo, PDF) để tra nhanh qua BottomSheet.
- 🤖 **AI Trợ Lý Có Neo (Grounded BYOK AI)**: Hỗ trợ tự cấu hình khóa API (OpenAI, Gemini, Anthropic Claude, DeepSeek) hoặc dùng mô hình local; AI chỉ giải thích dựa trên dữ liệu có thật từ từ điển lõi.

---

## 📂 Cấu Trúc Dự Án (Repository Architecture)

Toàn bộ tài liệu quy hoạch, đặc tả chi tiết, lộ trình và lịch sử trao đổi được đặt trong thư mục cô lập **`project-meta/`** để hoàn toàn tách biệt khỏi mã nguồn ứng dụng Android (`app/`) và quy trình xử lý dữ liệu (`data-pipeline/`):

```
HanViet-EtymoAI/
├── project-meta/                  # KHÔNG GIAN DỰ ÁN & ĐẶC TẢ (HOÀN TOÀN TÁCH BIỆT)
│   ├── spec/                      # Các tài liệu đặc tả kiến trúc kỹ thuật
│   │   ├── IDEA_2.2_SPEC.md       # Bản đặc tả gốc Idea 2.2
│   │   ├── ARCHITECTURE.md        # Kiến trúc hệ thống & Dual-Database
│   │   ├── GLOSSARY.md            # Ngôn ngữ miền chung (Ubiquitous Domain Language)
│   │   └── SPEC.md                # Đặc tả GSD chính thức (Planning Lock: Finalized)
│   ├── planning/                  # Lộ trình & Kế hoạch phân rã theo GSD Planner
│   │   ├── ROADMAP.md             # Lộ trình 8 giai đoạn (Phase 0 -> Phase 7)
│   │   ├── STATE.md               # Trạng thái thực thi hiện tại của dự án
│   │   └── phases/                # 16 kế hoạch chi tiết cho từng giai đoạn
│   │       ├── phase-0-data-feasibility/
│   │       ├── phase-1-dual-db-search/
│   │       ├── phase-2-morphology/
│   │       ├── phase-3-hanja-etymology/
│   │       ├── phase-4-user-workspace-srs/
│   │       ├── phase-5-android-integration/
│   │       ├── phase-6-ai-grounding/
│   │       └── phase-7-release-packaging/
│   └── history/                   # Lịch sử phát triển & Đúc kết kinh nghiệm
│       ├── CHAT_HISTORY_AND_DECISIONS.md # Toàn bộ lịch sử các quyết định kỹ thuật
│       ├── LESSONS_LEARNED_FROM_V1.md    # Bài học xương máu từ phiên bản tiền nhiệm V1
│       ├── TRANSCRIPT_DIGEST.md          # Bản trích xuất hội thoại có cấu trúc
│       └── TRANSCRIPT_RAW.jsonl          # Nhật ký hội thoại gốc
│
├── data-pipeline/                 # [Phase 0] ETL & Sinh cơ sở dữ liệu dictionary_core.db
└── app/                           # [Phase 1+] Mã nguồn ứng dụng Android (Kotlin, Compose, Room)
```

---

## 🚀 Kế Hoạch 8 Giai Đoạn (8-Phase Roadmap - 100% COMPLETE)

- [x] **Phase 0: Data Feasibility & ETL Spike** (Rà soát nguồn mở NIKL/Wiktionary, schema SQLite core, ETL spike)
- [x] **Phase 1: Dual-Database Architecture & Search Core** (SQLite read-only + Room user workspace, search 5 tầng)
- [x] **Phase 2: Morphology Rule Engine** (Deconjugation 7 nhóm bất quy tắc: ㅂ, ㄷ, ㅅ, ㅎ, 르, 으, ㄹ)
- [x] **Phase 3: Contextual Hanja & Etymology Display** (Phân đoạn Hán tự, đồ thị từ nguyên, huy hiệu minh bạch)
- [x] **Phase 4: User Workspace & SRS Flashcards** (Sổ tay từ vựng, tag, ghi chú Markdown, thuật toán SuperMemo SM-2)
- [x] **Phase 5: Android System Integration** (`ACTION_PROCESS_TEXT` popup không cần quyền overlay)
- [x] **Phase 6: Optional AI Layer (BYOK & Local Grounding)** (Hỗ trợ đa provider OpenAI/Gemini/DeepSeek/Claude/Ollama, kiểm soát chống ảo giác GroundingGuard)
- [x] **Phase 7: Packaging, Hardening & CI/CD** (GitHub Actions CI/CD, auto-release APK, kiểm tra mã băm SHA-256 DataPackIntegrityVerifier)

Xem chi tiết từng kế hoạch hành động tại: [`project-meta/planning/ROADMAP.md`](project-meta/planning/ROADMAP.md)

---

## 📜 Giấy Phép & Đóng Góp

Dự án được phát hành theo giấy phép [MIT License](LICENSE).
Mọi đóng góp từ cộng đồng học tiếng Hàn và ngôn ngữ học Hán-Nôm đều được hoan nghênh nồng nhiệt!
