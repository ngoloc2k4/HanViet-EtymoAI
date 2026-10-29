#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
test_core_db_integrity.py — Kiểm định tính toàn vẹn CSDL và hiệu năng FTS5
Thuộc Phase 0: Data Feasibility & ETL Spike (Plan 0.2)

Chức năng:
1. Xác minh tính hợp lệ và không có lỗi khoá ngoại (PRAGMA foreign_key_check).
2. Kiểm thử FTS5 với tiếng Việt không dấu: "tam trang" -> khớp "기분".
3. Kiểm thử FTS5 / Alias với Chosung gõ tắt: "ㄱㅂ" -> khớp "기분".
4. Kiểm thử Precomputed Forms: "갔어요" -> khớp "가다", "들었어요" -> khớp "듣다".
5. Kiểm thử trích xuất Chiết tự Hán-Hàn-Việt (lexeme_hanja_segments).
6. Đo lường tốc độ truy vấn (Latency Benchmark < 5ms).
"""

import os
import sys
import sqlite3
import time
import unicodedata
from typing import List, Tuple, Any


def run_tests(db_path: str) -> bool:
    if not os.path.exists(db_path):
        print(f"[!] Lỗi: Không tìm thấy file CSDL tại {db_path}")
        return False

    print("=" * 70)
    print("   BỘ KIỂM THỬ TÍNH TOÀN VẸN CSDL & TỐC ĐỘ FTS5 — HANVIET-ETYMOAI")
    print("=" * 70)
    print(f"File CSDL kiểm thử: {db_path}")

    conn = sqlite3.connect(db_path)
    conn.execute("PRAGMA foreign_keys = ON;")

    all_passed = True

    # -------------------------------------------------------------------------
    # TEST 1: Kiểm tra toàn vẹn khóa ngoại (Foreign Key Integrity)
    # -------------------------------------------------------------------------
    fk_violations = conn.execute("PRAGMA foreign_key_check;").fetchall()
    if len(fk_violations) == 0:
        print("  [TEST 1] Kiểm tra toàn vẹn khóa ngoại (Foreign Keys) : [PASS] (0 vi phạm)")
    else:
        print(f"  [TEST 1] Kiểm tra toàn vẹn khóa ngoại (Foreign Keys) : [FAIL] ({len(fk_violations)} vi phạm)")
        all_passed = False

    # -------------------------------------------------------------------------
    # TEST 2: FTS5 Tìm kiếm tiếng Việt không dấu ("tam trang" -> "기분")
    # -------------------------------------------------------------------------
    t0 = time.perf_counter()
    res_vi = conn.execute(
        """
        SELECT headword_nfc, meaning_vi
        FROM search_fts
        WHERE search_fts MATCH 'tam trang*'
        LIMIT 5
        """
    ).fetchall()
    t_vi_ms = (time.perf_counter() - t0) * 1000

    found_gibun = any(r[0] == "기분" for r in res_vi)
    if found_gibun:
        print(f"  [TEST 2] FTS5 Tiếng Việt không dấu ('tam trang' -> '기분'): [PASS] ({t_vi_ms:.2f} ms)")
    else:
        print(f"  [TEST 2] FTS5 Tiếng Việt không dấu ('tam trang' -> '기분'): [FAIL] Kết quả: {res_vi}")
        all_passed = False

    # -------------------------------------------------------------------------
    # TEST 3: Tìm kiếm gõ tắt Chosung ("ㄱㅂ" -> "기분")
    # -------------------------------------------------------------------------
    t0 = time.perf_counter()
    res_chosung = conn.execute(
        """
        SELECT l.headword_nfc, a.alias_text
        FROM aliases a
        JOIN lexemes l ON a.lexeme_id = l.lexeme_id
        WHERE a.alias_text = 'ㄱㅂ' AND a.alias_type = 'CHOSUNG'
        LIMIT 5
        """
    ).fetchall()
    t_chosung_ms = (time.perf_counter() - t0) * 1000

    found_chosung = any(r[0] == "기분" for r in res_chosung)
    if found_chosung:
        print(f"  [TEST 3] Tìm kiếm gõ tắt Chosung ('ㄱㅂ' -> '기분')     : [PASS] ({t_chosung_ms:.2f} ms)")
    else:
        print(f"  [TEST 3] Tìm kiếm gõ tắt Chosung ('ㄱㅂ' -> '기분')     : [FAIL] Kết quả: {res_chosung}")
        all_passed = False

    # -------------------------------------------------------------------------
    # TEST 4: Dạng chia tính sẵn ("갔어요" -> "가다", "들었어요" -> "듣다")
    # -------------------------------------------------------------------------
    t0 = time.perf_counter()
    res_past_ga = conn.execute(
        """
        SELECT l.headword_nfc, p.rule_applied, p.grammar_desc
        FROM precomputed_forms p
        JOIN lexemes l ON p.lexeme_id = l.lexeme_id
        WHERE p.inflected_form = '갔어요'
        """
    ).fetchone()
    t_past_ga_ms = (time.perf_counter() - t0) * 1000

    t0 = time.perf_counter()
    res_past_deut = conn.execute(
        """
        SELECT l.headword_nfc, p.rule_applied, p.grammar_desc
        FROM precomputed_forms p
        JOIN lexemes l ON p.lexeme_id = l.lexeme_id
        WHERE p.inflected_form = '들었어요'
        """
    ).fetchone()
    t_past_deut_ms = (time.perf_counter() - t0) * 1000

    ok_ga = (res_past_ga is not None and res_past_ga[0] == "가다")
    ok_deut = (res_past_deut is not None and res_past_deut[0] == "듣다")

    if ok_ga and ok_deut:
        print(f"  [TEST 4] Biến thể chia sẵn ('갔어요'->'가다', '들었어요'->'듣다'): [PASS] ({t_past_ga_ms:.2f} ms / {t_past_deut_ms:.2f} ms)")
    else:
        print(f"  [TEST 4] Biến thể chia sẵn: [FAIL] ga={res_past_ga}, deut={res_past_deut}")
        all_passed = False

    # -------------------------------------------------------------------------
    # TEST 5: Chiết tự Hán-Hàn-Việt (기분 -> 氣 (khí) + 分 (phân))
    # -------------------------------------------------------------------------
    res_segments = conn.execute(
        """
        SELECT s.syllable_ko, s.hanja_char, s.reading_vi, s.meaning_vi
        FROM lexeme_hanja_segments s
        JOIN lexemes l ON s.lexeme_id = l.lexeme_id
        WHERE l.headword_nfc = '기분'
        ORDER BY s.segment_index ASC
        """
    ).fetchall()

    expected_chars = ["氣", "分"]
    actual_chars = [r[1] for r in res_segments]
    if actual_chars == expected_chars:
        print(f"  [TEST 5] Chiết tự Hán-Hàn-Việt ('기분' -> 氣 khí + 分 phân): [PASS] ({res_segments})")
    else:
        print(f"  [TEST 5] Chiết tự Hán-Hàn-Việt: [FAIL] actual={actual_chars}")
        all_passed = False

    # -------------------------------------------------------------------------
    # TEST 6: Benchmark độ trễ (100 lần lặp liên tục)
    # -------------------------------------------------------------------------
    iterations = 100
    start_bench = time.perf_counter()
    for _ in range(iterations):
        conn.execute("SELECT headword_nfc FROM search_fts WHERE search_fts MATCH 'tam trang*'").fetchall()
        conn.execute("SELECT headword_nfc FROM precomputed_forms p JOIN lexemes l ON p.lexeme_id = l.lexeme_id WHERE p.inflected_form = '들었어요'").fetchall()
    total_time = (time.perf_counter() - start_bench) * 1000
    avg_latency = total_time / (iterations * 2)

    c_perf = avg_latency < 5.0
    print(f"  [TEST 6] Benchmark độ trễ trung bình (< 5.0 ms)        : {'[PASS]' if c_perf else '[FAIL]'} ({avg_latency:.3f} ms / truy vấn)")
    if not c_perf:
        all_passed = False

    print("-" * 70)
    if all_passed:
        print("KẾT LUẬN: 100% BÀI KIỂM THỬ THÀNH CÔNG (Plan 0.2 Integrity: VERIFIED)")
    else:
        print("KẾT LUẬN: CÓ BÀI KIỂM THỬ THẤT BÀI - CẦN KHẮC PHỤC")
    print("=" * 70)

    conn.close()
    return all_passed


def main():
    root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    db_path = os.path.join(root_dir, "output", "dictionary_sample.db")
    success = run_tests(db_path)
    if not success:
        sys.exit(1)


if __name__ == "__main__":
    main()
