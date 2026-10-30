#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
build_real_core_db.py — ETL Pipeline sinh CSDL từ điển thực tế dictionary_core.db
Trích xuất dữ liệu chính thức từ Viện Quốc ngữ Hàn Quốc (KRDict NIKL) và ánh xạ âm Hán-Việt.
"""

import os
import sys
import sqlite3
import unicodedata
import re
import json
import csv
import ast
import urllib.request
import time
from typing import List, Dict, Any, Tuple, Optional, Set

CHOSUNG_LIST = [
    'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ',
    'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
]

POS_MAP = {
    "명사": "Danh từ",
    "동사": "Động từ",
    "형용사": "Tính từ",
    "부사": "Phó từ",
    "대명사": "Đại từ",
    "수사": "Số từ",
    "관형사": "Định từ",
    "감탄사": "Thán từ",
    "조사": "Trợ từ",
    "접사": "Tiếp từ",
    "의존 명사": "Danh từ phụ thuộc",
    "보조 동사": "Trợ động từ",
    "보조 형용사": "Trợ tính từ"
}

def nfc(text: str) -> str:
    if not text:
        return ""
    return unicodedata.normalize("NFC", text.strip())

def extract_chosung(text: str) -> str:
    res = []
    for ch in text:
        code = ord(ch)
        if 0xAC00 <= code <= 0xD7A3:
            chosung_idx = (code - 0xAC00) // 588
            res.append(CHOSUNG_LIST[chosung_idx])
        else:
            res.append(ch)
    return "".join(res)

def remove_vietnamese_accents(text: str) -> str:
    if not text:
        return ""
    text_nfd = unicodedata.normalize("NFD", text)
    stripped = re.sub(r"[\u0300-\u036f]", "", text_nfd)
    normalized = unicodedata.normalize("NFC", stripped)
    normalized = normalized.replace("đ", "d").replace("Đ", "D")
    return normalized.lower().strip()

def is_hanja(ch: str) -> bool:
    code = ord(ch)
    return (0x4E00 <= code <= 0x9FFF) or (0x3400 <= code <= 0x4DBF) or (0xF900 <= code <= 0xFAFF)

def load_hanviet_mapping(filepath: str) -> Dict[str, str]:
    mapping = {}
    if not os.path.exists(filepath):
        print(f"[!] Warning: {filepath} không tồn tại.")
        return mapping
    with open(filepath, "r", encoding="utf-8") as f:
        reader = csv.reader(f)
        try:
            next(reader) # skip header
        except StopIteration:
            return mapping
        for row in reader:
            if len(row) >= 2:
                ch = row[0].strip()
                raw_r = row[1].strip()
                try:
                    readings = ast.literal_eval(raw_r)
                    if readings and ch not in mapping:
                        mapping[ch] = readings[0]
                except Exception:
                    pass
    print(f"[✓] Đã nạp {len(mapping)} ký tự Hán tự từ {filepath}")
    return mapping

def generate_phonetic_vi(hangul: str) -> str:
    """Phiên âm tiếng Việt gần đúng cho từ Hangul."""
    # Quy tắc phiên âm cơ bản
    ko_to_vi = {
        "가": "ka", "나": "na", "다": "ta", "라": "ra", "마": "ma", "바": "pa", "사": "sa", "아": "a", "자": "cha", "차": "cha", "카": "kha", "타": "tha", "파": "pha", "하": "ha",
        "기": "ki", "니": "ni", "디": "ti", "리": "ri", "미": "mi", "비": "pi", "시": "si", "이": "i", "지": "chi", "치": "chi", "키": "khi", "티": "thi", "피": "phi", "히": "hi",
        "도": "to", "서": "sơ", "관": "koan", "학": "hắc", "생": "xeng", "교": "ki-ô", "행": "heng", "복": "búc", "사": "sa", "랑": "rang"
    }
    parts = []
    for ch in hangul:
        parts.append(ko_to_vi.get(ch, ch))
    return "-".join(parts)

def parse_entries_from_xml_content(
    content: str,
    hanviet_map: Dict[str, str]
) -> List[Dict[str, Any]]:
    chunks = content.split("<LexicalEntry")
    entries: List[Dict[str, Any]] = []

    re_lemma = re.compile(r"<Lemma>\s*<feat att=\"writtenForm\" val=\"([^\"]+)\"")
    re_pos = re.compile(r"<feat att=\"partOfSpeech\" val=\"([^\"]+)\"")
    re_origin = re.compile(r"<feat att=\"origin\" val=\"([^\"]+)\"")
    re_level = re.compile(r"<feat att=\"vocabularyLevel\" val=\"([^\"]+)\"")
    re_pron = re.compile(r"<feat att=\"pronunciation\" val=\"([^\"]+)\"")
    re_senses = re.compile(r"<Sense[^>]*>(.*?)</Sense>", re.DOTALL)
    re_vi_eq = re.compile(r"<feat att=\"language\" val=\"베트남어\" />\s*<feat att=\"lemma\" val=\"([^\"]+)\"")
    re_vi_def = re.compile(r"<feat att=\"language\" val=\"베트남어\" />.*?<feat att=\"definition\" val=\"([^\"]+)\"", re.DOTALL)
    re_ko_def = re.compile(r"<feat att=\"definition\" val=\"([^\"]+)\"")
    re_examples = re.compile(r"<feat att=\"example\" val=\"([^\"]+)\"")
    re_inflections = re.compile(r"<WordForm>\s*<feat att=\"type\" val=\"활용\" />\s*<feat att=\"writtenForm\" val=\"([^\"]+)\"")

    for chunk in chunks[1:]:
        m_lem = re_lemma.search(chunk)
        if not m_lem:
            continue
        headword = nfc(m_lem.group(1))
        # Bỏ qua phụ tố bắt đầu hoặc kết thúc bằng gạch ngang
        if headword.startswith("-") or headword.endswith("-") or len(headword) < 1:
            continue

        # Tìm nghĩa tiếng Việt
        m_vi = re_vi_eq.search(chunk)
        if not m_vi:
            continue
        meaning_vi = nfc(m_vi.group(1))
        if not meaning_vi or meaning_vi == "(Không có từ tương ứng)":
            continue

        # Định nghĩa tiếng Việt
        m_vi_def = re_vi_def.search(chunk)
        definition_vi = nfc(m_vi_def.group(1)) if m_vi_def else ""

        # Từ loại
        m_pos = re_pos.search(chunk)
        pos_raw = m_pos.group(1) if m_pos else "명사"
        pos_vi = POS_MAP.get(pos_raw, pos_raw)

        # Nguồn gốc (Hanja hoặc Ngoại lai)
        m_orig = re_origin.search(chunk)
        origin_raw = m_orig.group(1).strip() if m_orig else None

        # Cấp độ từ vựng
        m_level = re_level.search(chunk)
        level_str = m_level.group(1) if m_level else ""
        topik_level = 1 if "초급" in level_str else (3 if "중급" in level_str else (5 if "고급" in level_str else 0))

        # Phiên âm
        m_pron = re_pron.search(chunk)
        phonetic = m_pron.group(1) if m_pron else headword

        # Định nghĩa tiếng Hàn
        m_ko_def = re_ko_def.search(chunk)
        definition_ko = nfc(m_ko_def.group(1)) if m_ko_def else ""

        # Câu ví dụ (lấy tối đa 3 ví dụ)
        examples = []
        for m_ex in re_examples.finditer(chunk):
            ex_text = nfc(m_ex.group(1))
            if ex_text and len(ex_text) > 2:
                examples.append({"ko": ex_text, "vi": ""})
            if len(examples) >= 3:
                break

        # Dạng thức chia (inflected forms)
        inflected_forms = []
        for m_inf in re_inflections.finditer(chunk):
            inf_word = nfc(m_inf.group(1))
            if inf_word and inf_word != headword and inf_word not in inflected_forms:
                inflected_forms.append(inf_word)

        # Phân loại origin_type & bóc tách Hanja segments
        origin_type = "NATIVE_KOREAN"
        hanja_segments = []

        if origin_raw:
            hanja_chars_in_origin = [c for c in origin_raw if is_hanja(c)]
            if hanja_chars_in_origin:
                origin_type = "SINO_KOREAN" if len(hanja_chars_in_origin) == len(headword) else "MIXED"
                # Ánh xạ từng âm tiết Hangul với chữ Hán
                # Nếu số chữ Hán khớp với số âm tiết Hangul
                if len(hanja_chars_in_origin) == len(headword):
                    for idx, (syl, h_char) in enumerate(zip(headword, hanja_chars_in_origin)):
                        hv_read = hanviet_map.get(h_char, "")
                        hanja_segments.append({
                            "char": h_char,
                            "hangul": syl,
                            "hanviet": hv_read,
                            "meaning": f"chữ Hán {h_char}"
                        })
                else:
                    # Ánh xạ các ký tự Hán tìm thấy
                    for idx, h_char in enumerate(hanja_chars_in_origin[:len(headword)]):
                        syl = headword[idx] if idx < len(headword) else ""
                        hv_read = hanviet_map.get(h_char, "")
                        hanja_segments.append({
                            "char": h_char,
                            "hangul": syl,
                            "hanviet": hv_read,
                            "meaning": f"chữ Hán {h_char}"
                        })
            elif re.search(r"[a-zA-Z]", origin_raw):
                origin_type = "LOANWORD"

        entries.append({
            "headword": headword,
            "pos": pos_vi,
            "origin_type": origin_type,
            "origin_raw": origin_raw,
            "meaning_vi": meaning_vi,
            "definition_vi": definition_vi,
            "definition_ko": definition_ko,
            "phonetic_vi": generate_phonetic_vi(headword),
            "topik_level": topik_level,
            "examples": examples,
            "inflected_forms": inflected_forms,
            "hanja_segments": hanja_segments
        })

    return entries

def build_database(
    schema_path: str,
    output_path: str,
    hanviet_csv_path: str,
    max_xml_files: int = 11
):
    print("=" * 60)
    print("BẮT ĐẦU XÂY DỰNG CƠ SỞ DỮ LIỆU TỪ ĐIỂN CỐT LÕI (REAL CORE DB)")
    print("=" * 60)

    # 1. Nạp bảng âm Hán-Việt
    hanviet_map = load_hanviet_mapping(hanviet_csv_path)

    # 2. Khởi tạo SQLite
    if os.path.exists(output_path):
        os.remove(output_path)
    os.makedirs(os.path.dirname(output_path), exist_ok=True)

    conn = sqlite3.connect(output_path)
    conn.execute("PRAGMA synchronous = OFF;")
    conn.execute("PRAGMA journal_mode = MEMORY;")

    with open(schema_path, "r", encoding="utf-8") as f:
        schema_sql = f.read()
    conn.executescript(schema_sql)

    # Ghi metadata
    conn.execute(
        "INSERT INTO db_metadata (key, value) VALUES (?, ?)",
        ("version", "2.2.0")
    )
    conn.execute(
        "INSERT INTO db_metadata (key, value) VALUES (?, ?)",
        ("source", "NIKL Korean-Vietnamese Learners Dictionary (국립국어원 한국어기초사전)")
    )
    conn.execute(
        "INSERT INTO db_metadata (key, value) VALUES (?, ?)",
        ("built_at", time.strftime("%Y-%m-%d %H:%M:%S"))
    )

    total_lexemes = 0
    total_senses = 0
    total_aliases = 0
    total_precomputed = 0
    total_hanja_segments = 0

    seen_headwords: Set[str] = set()

    # 3. Lặp tải và xử lý từng file XML từ NIKL
    for file_idx in range(1, max_xml_files + 1):
        num_str = f"{file_idx:03d}"
        url = f"https://raw.githubusercontent.com/spellcheck-ko/korean-dict-nikl/master/krdict/{num_str}.xml"
        print(f"\n[+] Đang tải và phân tích gói dữ liệu {num_str}.xml ...")
        t0 = time.time()
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
            with urllib.request.urlopen(req, timeout=30) as resp:
                raw_xml = resp.read().decode("utf-8", errors="ignore")
        except Exception as e:
            print(f"[-] Lỗi khi tải {num_str}.xml: {e}")
            continue

        entries = parse_entries_from_xml_content(raw_xml, hanviet_map)
        print(f"    -> Đã trích xuất {len(entries)} từ vựng tiếng Việt trong {time.time() - t0:.2f}s")

        # Nạp vào database
        for entry in entries:
            hw = entry["headword"]
            # Tránh trùng lặp hoàn toàn
            key = f"{hw}_{entry['pos']}"
            if key in seen_headwords:
                continue
            seen_headwords.add(key)

            total_lexemes += 1
            lexeme_id = total_lexemes
            stable_key = f"ko_{lexeme_id:06d}_{extract_chosung(hw)}"

            # Insert lexemes
            conn.execute(
                """
                INSERT INTO lexemes (lexeme_id, stable_key, headword_nfc, origin_type, origin_status,
                                     phonetic_vi, topik_level, source_dataset, source_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                (
                    lexeme_id, stable_key, hw, entry["origin_type"], "VERIFIED_PRIMARY",
                    entry["phonetic_vi"], entry["topik_level"], "NIKL", str(lexeme_id)
                )
            )

            # Insert senses
            m_vi = entry["meaning_vi"]
            m_vi_norm = remove_vietnamese_accents(m_vi)
            ex_json = json.dumps(entry["examples"], ensure_ascii=False) if entry["examples"] else None

            conn.execute(
                """
                INSERT INTO senses (lexeme_id, part_of_speech, meaning_vi, meaning_vi_normalized,
                                    definition_ko, examples_json, display_order)
                VALUES (?, ?, ?, ?, ?, ?, 1)
                """,
                (lexeme_id, entry["pos"], m_vi, m_vi_norm, entry["definition_ko"], ex_json)
            )
            total_senses += 1

            # Insert Chosung Alias
            chosung = extract_chosung(hw)
            if chosung and chosung != hw:
                conn.execute(
                    """
                    INSERT INTO aliases (lexeme_id, alias_text, alias_type)
                    VALUES (?, ?, 'CHOSUNG')
                    """,
                    (lexeme_id, chosung)
                )
                total_aliases += 1

            # Insert Hanja Segments
            for seg_idx, seg in enumerate(entry["hanja_segments"]):
                char = seg["char"]
                h_reading = seg["hangul"]
                hv_reading = seg["hanviet"]
                char_meaning = seg["meaning"]

                conn.execute(
                    """
                    INSERT OR IGNORE INTO hanja_master (hanja_char, radical, stroke_count, meaning_vi, general_level)
                    VALUES (?, '', 0, ?, 'Basic')
                    """,
                    (char, char_meaning)
                )
                conn.execute(
                    """
                    INSERT OR IGNORE INTO hanja_readings (hanja_char, reading_ko, reading_vi, is_primary)
                    VALUES (?, ?, ?, 1)
                    """,
                    (char, h_reading, hv_reading)
                )
                conn.execute(
                    """
                    INSERT INTO lexeme_hanja_segments (lexeme_id, segment_index, syllable_ko, hanja_char, reading_vi, meaning_vi)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                    (lexeme_id, seg_idx, h_reading, char, hv_reading, char_meaning)
                )
                total_hanja_segments += 1

            # Insert Precomputed Forms
            for form in entry["inflected_forms"]:
                conn.execute(
                    """
                    INSERT OR REPLACE INTO precomputed_forms (inflected_form, lexeme_id, rule_applied, grammar_desc, confidence)
                    VALUES (?, ?, 'NIKL_INFLECTION', 'Dạng chia thông dụng trong tiếng Hàn', 1.0)
                    """,
                    (form, lexeme_id)
                )
                total_precomputed += 1

            # Insert search_fts (rowid == lexeme_id)
            conn.execute(
                """
                INSERT INTO search_fts (rowid, headword_nfc, alias_chosung, meaning_vi, unaccented_vi)
                VALUES (?, ?, ?, ?, ?)
                """,
                (lexeme_id, hw, chosung, m_vi, m_vi_norm)
            )

        conn.commit()
        print(f"    [OK] Đã tích lũy {total_lexemes} mục từ trong CSDL")

    conn.close()

    file_size_mb = os.path.getsize(output_path) / (1024 * 1024)
    print("\n" + "=" * 60)
    print(f"XÂY DỰNG CSDL HOÀN TẤT: {output_path}")
    print(f"  • Tổng số mục từ (Lexemes)        : {total_lexemes}")
    print(f"  • Tổng nét nghĩa (Senses)         : {total_senses}")
    print(f"  • Gõ tắt Chosung (Aliases)        : {total_aliases}")
    print(f"  • Dạng chia tính sẵn (Precomputed): {total_precomputed}")
    print(f"  • Chiết tự Hán-Hàn (Hanja Segments): {total_hanja_segments}")
    print(f"  • Dung lượng CSDL                 : {file_size_mb:.2f} MB")
    print("=" * 60)

def main():
    base_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    schema_path = os.path.join(base_dir, "schema", "core_schema.sql")
    output_path = os.path.join(base_dir, "output", "dictionary_core.db")
    hanviet_csv = os.path.join(base_dir, "etl", "hanviet.csv")

    build_database(
        schema_path=schema_path,
        output_path=output_path,
        hanviet_csv_path=hanviet_csv,
        max_xml_files=11
    )

    # Sao chép vào app assets
    app_assets = os.path.abspath(os.path.join(base_dir, "..", "app", "src", "main", "assets", "dictionary_core.db"))
    os.makedirs(os.path.dirname(app_assets), exist_ok=True)
    import shutil
    shutil.copyfile(output_path, app_assets)
    print(f"[✓] Đã sao chép CSDL vào Android Assets: {app_assets}")

if __name__ == "__main__":
    main()
