-- ============================================================================
-- HanViet-EtymoAI: Core Dictionary SQLite Schema (core_schema.sql)
-- Architecture: Read-Only SQLite Core (dictionary_core.db)
-- Principles: Offline-First, Sub-50ms Lookups, FTS5 Prefix/Unaccented Search
-- ============================================================================

PRAGMA foreign_keys = ON;

-- ----------------------------------------------------------------------------
-- 1. METADATA: Quản lý phiên bản gói dữ liệu và tính toàn vẹn
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS db_metadata (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL
);

-- ----------------------------------------------------------------------------
-- 2. LEXEMES: Mục từ chính (Headwords)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS lexemes (
    lexeme_id INTEGER PRIMARY KEY AUTOINCREMENT,
    stable_key TEXT UNIQUE NOT NULL,            -- Khóa ổn định: e.g. "ko_gibun_01"
    headword_nfc TEXT NOT NULL,                 -- Chuỗi chuẩn hóa NFC tuyệt đối
    origin_type TEXT NOT NULL,                  -- SINO_KOREAN, NATIVE_KOREAN, LOANWORD, MIXED, UNKNOWN
    origin_status TEXT NOT NULL,                -- VERIFIED_PRIMARY, VERIFIED_SECONDARY, CURATED, INFERRED, UNVERIFIED
    phonetic_vi TEXT,                           -- Phiên âm tiếng Việt dễ đọc (e.g. "ki-bun")
    topik_level INTEGER,                        -- 1 -> 6 (0 nếu không có)
    source_dataset TEXT NOT NULL,               -- NIKL, UNIHAN, WIKTIONARY, CURATED_LEGACY
    source_id TEXT,                             -- ID mục từ ở nguồn gốc (e.g. NIKL target_code)
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 3. SENSES: Các nét nghĩa của từ vị
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS senses (
    sense_id INTEGER PRIMARY KEY AUTOINCREMENT,
    lexeme_id INTEGER NOT NULL REFERENCES lexemes(lexeme_id) ON DELETE CASCADE,
    part_of_speech TEXT,                        -- Danh từ, Động từ, Tính từ...
    meaning_vi TEXT NOT NULL,                   -- Nghĩa tiếng Việt chuẩn có dấu
    meaning_vi_normalized TEXT NOT NULL,        -- Dạng không dấu phục vụ tìm kiếm nhanh
    definition_ko TEXT,                         -- Định nghĩa tiếng Hàn gốc
    examples_json TEXT,                         -- JSON array chứa các câu ví dụ [{ko: "...", vi: "..."}]
    display_order INTEGER DEFAULT 1
);

-- ----------------------------------------------------------------------------
-- 4. ALIASES: Tên viết tắt, Chosung gõ tắt, biến thể chính tả
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS aliases (
    alias_id INTEGER PRIMARY KEY AUTOINCREMENT,
    lexeme_id INTEGER NOT NULL REFERENCES lexemes(lexeme_id) ON DELETE CASCADE,
    alias_text TEXT NOT NULL,                   -- e.g. "ㄱㅂ" cho "기분"
    alias_type TEXT NOT NULL                    -- CHOSUNG, SPELLING_VARIANT, ABBREVIATION, COMMON_TYPO
);

-- ----------------------------------------------------------------------------
-- 5. PRECOMPUTED_FORMS: Dạng chia ngữ pháp tính sẵn (Inflected Forms)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS precomputed_forms (
    inflected_form TEXT NOT NULL,               -- e.g. "갔어요", "들었습니다"
    lexeme_id INTEGER NOT NULL REFERENCES lexemes(lexeme_id) ON DELETE CASCADE,
    rule_applied TEXT NOT NULL,                 -- e.g. "PAST_INFORMAL_POLITE"
    grammar_desc TEXT,                          -- e.g. "Thì quá khứ đuôi thân mật -았/었어요"
    confidence REAL DEFAULT 1.0,
    PRIMARY KEY (inflected_form, lexeme_id)
);

-- ----------------------------------------------------------------------------
-- 6. HANJA_MASTER: Danh mục chữ Hán đơn lẻ (Unihan Database)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hanja_master (
    hanja_char TEXT PRIMARY KEY,                -- Ký tự Hán: e.g. "氣", "學"
    radical TEXT,                               -- Bộ thủ Khang Hy
    stroke_count INTEGER,                       -- Tổng số nét
    meaning_vi TEXT,                            -- Nghĩa của từng chữ Hán
    general_level TEXT                          -- Cấp độ thông dụng (Hanja Level 1 -> 8)
);

-- ----------------------------------------------------------------------------
-- 7. HANJA_READINGS: Âm đọc của chữ Hán trong tiếng Hàn và tiếng Hán-Việt
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hanja_readings (
    reading_id INTEGER PRIMARY KEY AUTOINCREMENT,
    hanja_char TEXT NOT NULL REFERENCES hanja_master(hanja_char) ON DELETE CASCADE,
    reading_ko TEXT NOT NULL,                   -- Âm Hàn (e.g. "기")
    reading_vi TEXT NOT NULL,                   -- Âm Hán-Việt (e.g. "khí")
    is_primary INTEGER DEFAULT 1                -- 1: Âm chính, 0: Âm phụ/biến thể
);

-- ----------------------------------------------------------------------------
-- 8. LEXEME_HANJA_SEGMENTS: Chiết tự từng âm tiết (Hán-Hàn-Việt Contextual Segments)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS lexeme_hanja_segments (
    segment_id INTEGER PRIMARY KEY AUTOINCREMENT,
    lexeme_id INTEGER NOT NULL REFERENCES lexemes(lexeme_id) ON DELETE CASCADE,
    segment_index INTEGER NOT NULL,             -- Thứ tự âm tiết: 0, 1, 2...
    syllable_ko TEXT NOT NULL,                  -- Âm tiết Hangul (e.g. "기")
    hanja_char TEXT REFERENCES hanja_master(hanja_char), -- Chữ Hán tương ứng (e.g. "氣")
    reading_vi TEXT,                            -- Âm Hán-Việt tương ứng (e.g. "khí")
    meaning_vi TEXT                             -- Nghĩa cụ thể của âm tiết trong từ
);

-- ----------------------------------------------------------------------------
-- 9. WORD_ORIGINS: Hồ sơ chi tiết nguồn gốc từ vựng (Origins & Etymology)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS word_origins (
    origin_record_id INTEGER PRIMARY KEY AUTOINCREMENT,
    lexeme_id INTEGER NOT NULL REFERENCES lexemes(lexeme_id) ON DELETE CASCADE,
    origin_type TEXT NOT NULL,                  -- SINO_KOREAN, LOANWORD, NATIVE_KOREAN, MIXED
    source_language TEXT,                       -- English, French, Sanskrit, etc.
    source_form TEXT,                           -- Từ nguyên bản gốc (e.g. "computer")
    adaptation_note TEXT,                       -- Diễn giải quá trình chuyển hóa ngữ âm
    verification_status TEXT NOT NULL,          -- VERIFIED_PRIMARY, CURATED, INFERRED
    evidence_note TEXT                          -- Căn cứ từ điển học
);

-- ----------------------------------------------------------------------------
-- 10. CHỈ MỤC B-TREE TĂNG TỐC TRA CỨU (< 5ms)
-- ----------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_lexemes_headword ON lexemes(headword_nfc);
CREATE INDEX IF NOT EXISTS idx_lexemes_stable_key ON lexemes(stable_key);
CREATE INDEX IF NOT EXISTS idx_senses_lexeme ON senses(lexeme_id);
CREATE INDEX IF NOT EXISTS idx_aliases_text ON aliases(alias_text);
CREATE INDEX IF NOT EXISTS idx_aliases_lexeme ON aliases(lexeme_id);
CREATE INDEX IF NOT EXISTS idx_precomputed_form ON precomputed_forms(inflected_form);
CREATE INDEX IF NOT EXISTS idx_hanja_segments_lexeme ON lexeme_hanja_segments(lexeme_id);
CREATE INDEX IF NOT EXISTS idx_hanja_readings_char ON hanja_readings(hanja_char);
CREATE INDEX IF NOT EXISTS idx_word_origins_lexeme ON word_origins(lexeme_id);

-- ----------------------------------------------------------------------------
-- 11. BẢNG ẢO FTS5: TÌM KIẾM TOÀN VĂN & TIẾNG VIỆT KHÔNG DẤU
-- ----------------------------------------------------------------------------
CREATE VIRTUAL TABLE IF NOT EXISTS search_fts USING fts5(
    headword_nfc,                               -- Từ gốc tiếng Hàn
    alias_chosung,                              -- Gõ tắt Chosung (e.g. "ㄱㅂ")
    meaning_vi,                                 -- Nghĩa tiếng Việt có dấu
    unaccented_vi,                              -- Nghĩa tiếng Việt không dấu (e.g. "tam trang")
    tokenize = 'unicode61 remove_diacritics 2'
);
