# Tài Liệu Thiết Kế Kiến Trúc Hệ Thống (ARCHITECTURE.md)

Dự án **HanViet-EtymoAI** được thiết kế dựa trên triết lý: **Offline-First, Nhẹ, Chính xác học thuật ("Không bịa nguồn gốc"), và Tách biệt ranh giới rõ ràng (Deep Modules)**.

---

## 1. Sơ đồ Tổng quan Kiến trúc Hệ thống

```mermaid
flowchart TD
    subgraph Host["Máy Tính / CI Pipeline (Offline Data Build)"]
        RawData["Nguồn Dữ Liệu Thô (NIKL, StarDict, Hanja Dict, Wiktionary)"]
        PythonETL["Data Pipeline & ETL Engine (Python/Polars/SQLite)"]
        CleanCoreDB[("dictionary_core.db (SQLite Read-Only, NFC, FTS5)")]
        RawData --> PythonETL
        PythonETL --> CleanCoreDB
    end

    subgraph Android["Thiết Bị Di Động Android (Client Runtime)"]
        CleanCoreDB -.->|"Đóng gói trong APK / Tải bản cập nhật"| CoreStorage[("dictionary_core.db (Read-Only)")]
        
        subgraph DataLayer["Tầng Dữ Liệu Tách Biệt (Dual-Database)"]
            CoreStorage
            UserWorkspaceDB[("user_workspace.db (Room Writable)")]
        end

        subgraph EngineLayer["Bộ Máy Xử Lý Cốt Lõi (Core Engines)"]
            SearchPipeline["5-Tier Search Pipeline (Exact -> Chosung -> Precomputed -> FTS5)"]
            MorphologyEngine["Morphology Rule Engine (Deconjugation Bất Quy Tắc)"]
            EtymologyEngine["Etymology & Hanja Graph Builder"]
            SRSEngine["Spaced Repetition Engine (SM-2 Algorithm)"]
        end

        subgraph AILayer["Tầng Trí Tuệ Nhân Tạo (AI Layer - Tuỳ Chọn)"]
            GroundingGuard["Grounding Context Guard (Ngăn Chặn Ảo Giác)"]
            LocalLLM["Local GGUF Engine (Tầng 2 Cục Bộ)"]
            OnlineBYOK["Cloud AI Providers (OpenAI, Gemini, Claude, DeepSeek)"]
        end

        subgraph UILayer["Giao Diện & Tích Hợp Hệ Thống"]
            MainActivity["Main Dictionary & Search View"]
            WordDetailView["Word Detail & Hanja Segments View"]
            FlashcardSRSView["Flashcard SRS Review View"]
            ProcessTextReceiver["Android ACTION_PROCESS_TEXT (Quick Lookup BottomSheet)"]
        end
    end

    CoreStorage --> SearchPipeline
    CoreStorage --> MorphologyEngine
    CoreStorage --> EtymologyEngine
    
    UserWorkspaceDB <--> SRSEngine
    UserWorkspaceDB <--> MainActivity
    
    SearchPipeline --> MainActivity
    MorphologyEngine --> SearchPipeline
    EtymologyEngine --> WordDetailView
    
    SearchPipeline --> GroundingGuard
    GroundingGuard --> LocalLLM
    GroundingGuard --> OnlineBYOK
    
    ProcessTextReceiver --> SearchPipeline
```

---

## 2. Kiến trúc Hai Cơ sở Dữ liệu Độc lập (Dual-Database Architecture)

### 2.1. `dictionary_core.db` (SQLite Chỉ Đọc - Read-Only)
File cơ sở dữ liệu này được sinh ra 100% từ quy trình ETL trên máy tính, nén chặt và đóng gói vào Assets của APK hoặc tải về theo từng phiên bản. Ứng dụng Android chỉ mở kết nối dạng `openDatabase(..., OPEN_READONLY)`.

```mermaid
erDiagram
    lexemes ||--o{ senses : contains
    lexemes ||--o{ aliases : has
    lexemes ||--o{ precomputed_forms : generates
    lexemes ||--o{ lexeme_hanja_segments : maps
    hanja_master ||--o{ lexeme_hanja_segments : references
    hanja_master ||--o{ hanja_readings : reads

    lexemes {
        INTEGER lexeme_id PK
        TEXT stable_key UK
        TEXT headword_nfc
        TEXT origin_type
        TEXT origin_status
        TEXT phonetic_vi
        INTEGER topik_level
    }

    senses {
        INTEGER sense_id PK
        INTEGER lexeme_id FK
        TEXT part_of_speech
        TEXT meaning_vi
        TEXT definition_ko
        TEXT examples_json
        INTEGER display_order
    }

    aliases {
        INTEGER alias_id PK
        INTEGER lexeme_id FK
        TEXT alias_text
        TEXT alias_type
    }

    precomputed_forms {
        TEXT inflected_form PK
        INTEGER lexeme_id FK
        TEXT rule_applied
        TEXT grammar_desc
    }

    hanja_master {
        TEXT hanja_char PK
        TEXT radical
        INTEGER stroke_count
        TEXT meaning_vi
        TEXT general_level
    }

    hanja_readings {
        INTEGER reading_id PK
        TEXT hanja_char FK
        TEXT reading_ko
        TEXT reading_vi
        INTEGER is_primary
    }

    lexeme_hanja_segments {
        INTEGER segment_id PK
        INTEGER lexeme_id FK
        INTEGER segment_index
        TEXT syllable_ko
        TEXT hanja_char FK
        TEXT reading_vi
        TEXT meaning_vi
    }
```

### 2.2. `user_workspace.db` (Room Writable - Cơ sở Dữ liệu Người Dùng)
Lưu toàn bộ hành vi và dữ liệu cá nhân của người dùng. Room quản lý version và migrations độc lập hoàn toàn với dữ liệu từ điển core.

```mermaid
erDiagram
    saved_words ||--o{ word_tag_cross_ref : tags
    word_tags ||--o{ word_tag_cross_ref : tagged_by
    saved_words ||--o{ personal_notes : contains
    saved_words ||--o| srs_cards : reviews

    saved_words {
        INTEGER id PK
        TEXT stable_key
        TEXT headword_snapshot
        TEXT meaning_snapshot
        INTEGER created_at
    }

    word_tags {
        INTEGER tag_id PK
        TEXT tag_name
        TEXT tag_color
    }

    personal_notes {
        INTEGER note_id PK
        INTEGER saved_word_id FK
        TEXT content
        INTEGER updated_at
    }

    srs_cards {
        INTEGER card_id PK
        INTEGER saved_word_id FK
        REAL interval_days
        REAL ease_factor
        INTEGER repetitions
        INTEGER due_date
        INTEGER last_reviewed
    }

    search_history {
        INTEGER id PK
        TEXT query_text
        INTEGER searched_at
        INTEGER result_count
    }

    ai_cache {
        TEXT prompt_hash PK
        TEXT response_json
        INTEGER cached_at
        INTEGER expiry_time
    }
```

---

## 3. Quy trình Tra Cứu 5 Tầng (5-Tier Search Pipeline)

Khi người dùng nhập chuỗi tìm kiếm (ví dụ: `"gibun"`, `"ㄱㅂ"`, `"갔어요"`, `"tam trang"`):

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant UI as Giao diện Tìm kiếm
    participant Pipeline as 5-Tier Search Pipeline
    participant CoreDB as dictionary_core.db
    participant MorphEngine as Morphology Rule Engine

    User->>UI: Nhập từ khóa ("갔어요")
    UI->>Pipeline: search("갔어요")
    
    rect rgb(240, 248, 255)
    Note over Pipeline, CoreDB: Tầng 1: Khớp chính xác Headword (NFC Exact)
    Pipeline->>CoreDB: SELECT * FROM lexemes WHERE headword_nfc = '갔어요'
    CoreDB-->>Pipeline: (Không có kết quả)
    end

    rect rgb(255, 250, 240)
    Note over Pipeline, CoreDB: Tầng 2: Khớp Alias / Chosung (ㄱㅂ)
    Pipeline->>CoreDB: SELECT * FROM aliases WHERE alias_text = '갔어요'
    CoreDB-->>Pipeline: (Không có kết quả)
    end

    rect rgb(245, 255, 245)
    Note over Pipeline, CoreDB: Tầng 3: Khớp Biến thể Tính sẵn (Precomputed Forms)
    Pipeline->>CoreDB: SELECT * FROM precomputed_forms WHERE inflected_form = '갔어요'
    CoreDB-->>Pipeline: Khớp: lexeme_id (가다), rule="았/었 quá khứ"
    end

    opt Nếu Tầng 3 không có kết quả
    rect rgb(255, 245, 245)
    Note over Pipeline, MorphEngine: Tầng 4: Bộ máy Hình thái (Deconjugation Engine)
    Pipeline->>MorphEngine: deconjugate("갔어요")
    MorphEngine-->>Pipeline: Danh sách ứng viên: [가다 (độ tin cậy 0.95)]
    end
    end

    rect rgb(248, 248, 255)
    Note over Pipeline, CoreDB: Tầng 5: Full-Text Search (FTS5 Prefix & Unaccented Vi)
    Pipeline->>CoreDB: SELECT * FROM search_fts WHERE search_fts MATCH '갔어요*'
    CoreDB-->>Pipeline: Danh sách kết quả FTS
    end

    Pipeline-->>UI: Hợp nhất & Xếp hạng kết quả theo Confidence Score
    UI-->>User: Hiển thị kết quả: "가다 (Đi) [Quá khứ: 갔어요]"
```

---

## 4. Bộ Máy Giải Phân Tích Hình Thái (Morphology Rule Engine)

Xử lý triệt để 7 nhóm động từ/tính từ bất quy tắc tiếng Hàn:
1. **Bất quy tắc `ㅂ`**: `덥다` $\to$ `더워`, `춥다` $\to$ `추워`, `돕다` $\to$ `도와`.
2. **Bất quy tắc `ㄷ`**: `듣다` $\to$ `들어요`, `걷다` $\to$ `걸어요`.
3. **Bất quy tắc `ㅅ`**: `짓다` $\to$ `지어요`, `낫다` $\to$ `나아요`.
4. **Bất quy tắc `ㅎ`**: `파랗다` $\to$ `파래`, `그렇다` $\to$ `그래`.
5. **Bất quy tắc `르`**: `모르다` $\to$ `몰라요`, `빠르다` $\to$ `빨라요`.
6. **Bất quy tắc `으`**: `바쁘다` $\to$ `바빠`, `예쁘다` $\to$ `예뻐`.
7. **Bất quy tắc `ㄹ`**: `만들다` $\to$ `만듭니다` (rơi `ㄹ` trước `ㄴ, ㅂ, ㅅ`).

Engine trả về kiểu dữ liệu:
```kotlin
data class DeconjugationResult(
    val inputForm: String,
    val candidates: List<CandidateLemma>
)

data class CandidateLemma(
    val lemma: String,
    val ruleId: String,
    val ruleCategory: RuleCategory,
    val confidenceScore: Float,
    val explanationVi: String
)
```

---

## 5. Tích hợp Hệ thống Android (`ACTION_PROCESS_TEXT`)

Thay vì bắt người dùng cấp quyền nguy hiểm `SYSTEM_ALERT_WINDOW` (quyền vẽ đè màn hình mà Android ngày càng siết chặt và làm chậm máy):
- Đăng ký `IntentFilter` với action `android.intent.action.PROCESS_TEXT`.
- Khi người dùng bôi đen văn bản trong bất kỳ ứng dụng nào khác (Chrome, Zalo, PDF reader), Android tự động thêm mục: **"Tra Từ Điển Hán-Việt"** vào menu copy/paste của hệ điều hành.
- Khởi động một `QuickLookupDialog` trong suốt hoặc `BottomSheetActivity` để hiển thị ngay lập tức phân tích từ nguyên và nghĩa mà không làm gián đoạn ngữ cảnh của người dùng.
