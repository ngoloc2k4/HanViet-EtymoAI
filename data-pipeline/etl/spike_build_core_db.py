#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
spike_build_core_db.py — ETL Spike sinh CSDL mẫu dictionary_sample.db
Thuộc Phase 0: Data Feasibility & ETL Spike (Plan 0.2)

Chức năng:
1. Đọc và thực thi schema DDL từ data-pipeline/schema/core_schema.sql.
2. Chuẩn hóa chuỗi 100% bằng unicodedata.normalize('NFC', text).
3. Tự động sinh gõ tắt Chosung (ㄱ-ㅎ) từ Hangul.
4. Tự động sinh dạng không dấu chuẩn cho nghĩa tiếng Việt.
5. Nạp tập từ vựng mẫu cốt lõi (Hán-Hàn, Thuần Hàn, Từ Vay Mượn, Động từ bất quy tắc).
6. Sinh các dạng thức chia trước (precomputed forms) và chiết tự Hán-Hàn-Việt (lexeme_hanja_segments).
7. Nạp bảng ảo FTS5 phục vụ tra cứu toàn văn siêu tốc.
8. Xuất ra file SQLite data-pipeline/output/dictionary_sample.db.
"""

import os
import sys
import sqlite3
import unicodedata
import re
import json
from datetime import datetime, timezone
from typing import List, Dict, Any, Tuple


CHOSUNG_LIST = [
    'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ',
    'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
]


def nfc(text: str) -> str:
    """Chuẩn hóa chuỗi về Unicode NFC."""
    if not text:
        return ""
    return unicodedata.normalize("NFC", text.strip())


def extract_chosung(text: str) -> str:
    """Trích xuất chuỗi Chosung từ văn bản tiếng Hàn (ví dụ: '기분' -> 'ㄱㅂ')."""
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
    """Xóa bỏ dấu thanh tiếng Việt để phục vụ tìm kiếm không dấu (ví dụ: 'tâm trạng' -> 'tam trang')."""
    if not text:
        return ""
    text_nfd = unicodedata.normalize("NFD", text)
    stripped = re.sub(r"[\u0300-\u036f]", "", text_nfd)
    normalized = unicodedata.normalize("NFC", stripped)
    normalized = normalized.replace("đ", "d").replace("Đ", "D")
    return normalized.lower().strip()


# ============================================================================
# TẬP DỮ LIỆU MẪU CỐT LÕI (SAMPLE SEED DATA)
# ============================================================================

RAW_ENTRIES: List[Dict[str, Any]] = [
    # ------------------ SINO-KOREAN (HÁN-HÀN) ------------------
    {
        "stable_key": "ko_gibun_01",
        "headword": "기분",
        "origin_type": "SINO_KOREAN",
        "origin_status": "VERIFIED_PRIMARY",
        "phonetic_vi": "ki-bun",
        "topik_level": 1,
        "source_dataset": "NIKL",
        "source_id": "42851",
        "senses": [
            {
                "pos": "Danh từ",
                "meaning_vi": "tâm trạng, cảm xúc, cảm giác trong người",
                "definition_ko": "주위의 상황이나 어떤 일에 따라 마음에 일어나는 감정.",
                "examples": [{"ko": "오늘 기분이 아주 좋다.", "vi": "Hôm nay tâm trạng tôi rất tốt."}]
            }
        ],
        "hanja_segments": [
            {"char": "氣", "hangul": "기", "hanviet": "khí", "meaning": "khí sắc, tinh thần", "radical": "气", "strokes": 10},
            {"char": "分", "hangul": "분", "hanviet": "phân", "meaning": "phần, phân định", "radical": "刀", "strokes": 4}
        ]
    },
    {
        "stable_key": "ko_haksaeng_01",
        "headword": "학생",
        "origin_type": "SINO_KOREAN",
        "origin_status": "VERIFIED_PRIMARY",
        "phonetic_vi": "hắc-xeng",
        "topik_level": 1,
        "source_dataset": "NIKL",
        "source_id": "18920",
        "senses": [
            {
                "pos": "Danh từ",
                "meaning_vi": "học sinh, sinh viên",
                "definition_ko": "학교에 다니며 공부하는 사람.",
                "examples": [{"ko": "저는 한국어를 배우는 학생입니다.", "vi": "Tôi là học sinh học tiếng Hàn."}]
            }
        ],
        "hanja_segments": [
            {"char": "學", "hangul": "학", "hanviet": "học", "meaning": "học tập, trường học", "radical": "子", "strokes": 16},
            {"char": "生", "hangul": "생", "hanviet": "sinh", "meaning": "sinh viên, sinh ra", "radical": "生", "strokes": 5}
        ]
    },
    {
        "stable_key": "ko_hakgyo_01",
        "headword": "학교",
        "origin_type": "SINO_KOREAN",
        "origin_status": "VERIFIED_PRIMARY",
        "phonetic_vi": "hắc-ki-ô",
        "topik_level": 1,
        "source_dataset": "NIKL",
        "source_id": "18901",
        "senses": [
            {
                "pos": "Danh từ",
                "meaning_vi": "trường học, nhà trường",
                "definition_ko": "일정한 목적에 따라 학생들을 교육하는 기관.",
                "examples": [{"ko": "아침 일찍 학교에 갑니다.", "vi": "Sáng sớm tôi đi đến trường."}]
            }
        ],
        "hanja_segments": [
            {"char": "學", "hangul": "학", "hanviet": "học", "meaning": "học tập", "radical": "子", "strokes": 16},
            {"char": "校", "hangul": "교", "hanviet": "hiệu", "meaning": "trường học", "radical": "木", "strokes": 10}
        ]
    },
    {
        "stable_key": "ko_gyeongje_01",
        "headword": "경제",
        "origin_type": "SINO_KOREAN",
        "origin_status": "VERIFIED_PRIMARY",
        "phonetic_vi": "kiêng-chê",
        "topik_level": 2,
        "source_dataset": "NIKL",
        "source_id": "31205",
        "senses": [
            {
                "pos": "Danh từ",
                "meaning_vi": "kinh tế, hoạt động kinh tế",
                "definition_ko": "재화를 생산하고 소비하는 인간의 모든 활동.",
                "examples": [{"ko": "한국의 경제 발전.", "vi": "Sự phát triển kinh tế của Hàn Quốc."}]
            }
        ],
        "hanja_segments": [
            {"char": "經", "hangul": "경", "hanviet": "kinh", "meaning": "kinh bang, kinh tế", "radical": "糸", "strokes": 13},
            {"char": "濟", "hangul": "제", "hanviet": "tế", "meaning": "tế thế, cứu trợ", "radical": "水", "strokes": 17}
        ]
    },
    {
        "stable_key": "ko_munhwa_01",
        "headword": "문화",
        "origin_type": "SINO_KOREAN",
        "origin_status": "VERIFIED_PRIMARY",
        "phonetic_vi": "mun-hoa",
        "topik_level": 2,
        "source_dataset": "NIKL",
        "source_id": "22019",
        "senses": [
            {
                "pos": "Danh từ",
                "meaning_vi": "văn hóa, nếp sống văn minh",
                "definition_ko": "인간 사회에서 이루어진 삶의 방식과 가치 체계.",
                "examples": [{"ko": "한국 문화를 배우다.", "vi": "Học văn hóa Hàn Quốc."}]
            }
        ],
        "hanja_segments": [
            {"char": "文", "hangul": "문", "hanviet": "văn", "meaning": "văn vẻ, văn tự", "radical": "文", "strokes": 4},
            {"char": "化", "hangul": "화", "hanviet": "hóa", "meaning": "biến hóa, giáo hóa", "radical": "匕", "strokes": 4}
        ]
    },
    {
        "stable_key": "ko_sahoe_01",
        "headword": "사회",
        "origin_type": "SINO_KOREAN",
        "origin_status": "VERIFIED_PRIMARY",
        "phonetic_vi": "sa-hoê",
        "topik_level": 2,
        "source_dataset": "NIKL",
        "source_id": "41029",
        "senses": [
            {
                "pos": "Danh từ",
                "meaning_vi": "xã hội, đoàn thể con người",
                "definition_ko": "공동생활을 영위하는 인간의 집단.",
                "examples": [{"ko": "사회 활동에 적극 참여하다.", "vi": "Tích cực tham gia các hoạt động xã hội."}]
            }
        ],
        "hanja_segments": [
            {"char": "社", "hangul": "사", "hanviet": "xã", "meaning": "xã tắc, hội nhóm", "radical": "示", "strokes": 8},
            {"char": "會", "hangul": "회", "hanviet": "hội", "meaning": "hội tụ, gặp gỡ", "radical": "曰", "strokes": 13}
        ]
    },
    {
        "stable_key": "ko_gukga_01",
        "headword": "국가",
        "origin_type": "SINO_KOREAN",
        "origin_status": "VERIFIED_PRIMARY",
        "phonetic_vi": "cúc-ca",
        "topik_level": 2,
        "source_dataset": "NIKL",
        "source_id": "19402",
        "senses": [
            {
                "pos": "Danh từ",
                "meaning_vi": "quốc gia, đất nước, nhà nước",
                "definition_ko": "영토와 국민을 바탕으로 주권을 행사하는 사회 집단.",
                "examples": [{"ko": "국가의 번영을 기원하다.", "vi": "Cầu chúc cho sự phồn vinh của quốc gia."}]
            }
        ],
        "hanja_segments": [
            {"char": "國", "hangul": "국", "hanviet": "quốc", "meaning": "đất nước, giang sơn", "radical": "囗", "strokes": 11},
            {"char": "家", "hangul": "가", "hanviet": "gia", "meaning": "nhà, dòng tộc", "radical": "宀", "strokes": 10}
        ]
    },
    {
        "stable_key": "ko_byeongwon_01",
        "headword": "병원",
        "origin_type": "SINO_KOREAN",
        "origin_status": "VERIFIED_PRIMARY",
        "phonetic_vi": "piêng-uôn",
        "topik_level": 1,
        "source_dataset": "NIKL",
        "source_id": "27011",
        "senses": [
            {
                "pos": "Danh từ",
                "meaning_vi": "bệnh viện, trạm xá",
                "definition_ko": "병든 사람을 진찰하고 치료하는 곳.",
                "examples": [{"ko": "몸이 아파서 병원에 갔어요.", "vi": "Vì đau ốm nên tôi đã đi đến bệnh viện."}]
            }
        ],
        "hanja_segments": [
            {"char": "病", "hangul": "병", "hanviet": "bệnh", "meaning": "tật bệnh, đau ốm", "radical": "疒", "strokes": 10},
            {"char": "院", "hangul": "원", "hanviet": "viện", "meaning": "viện sở, toà nhà lớn", "radical": "阜", "strokes": 10}
        ]
    },
    {
        "stable_key": "ko_uisa_01",
        "headword": "의사",
        "origin_type": "SINO_KOREAN",
        "origin_status": "VERIFIED_PRIMARY",
        "phonetic_vi": "ưi-sa",
        "topik_level": 1,
        "source_dataset": "NIKL",
        "source_id": "28190",
        "senses": [
            {
                "pos": "Danh từ",
                "meaning_vi": "bác sĩ, thầy thuốc",
                "definition_ko": "의술을 베푸는 것을 업으로 삼는 사람.",
                "examples": [{"ko": "의사 선생님의 진료를 받다.", "vi": "Được thầy thuốc chẩn đoán khám bệnh."}]
            }
        ],
        "hanja_segments": [
            {"char": "醫", "hangul": "의", "hanviet": "y", "meaning": "y học, thầy thuốc", "radical": "酉", "strokes": 18},
            {"char": "師", "hangul": "사", "hanviet": "sư", "meaning": "thầy giáo, chuyên gia", "radical": "巾", "strokes": 10}
        ]
    },
    {
        "stable_key": "ko_seonggong_01",
        "headword": "성공",
        "origin_type": "SINO_KOREAN",
        "origin_status": "VERIFIED_PRIMARY",
        "phonetic_vi": "xâng-công",
        "topik_level": 2,
        "source_dataset": "NIKL",
        "source_id": "35890",
        "senses": [
            {
                "pos": "Danh từ",
                "meaning_vi": "thành công, sự thành đạt",
                "definition_ko": "목적한 바를 이룸.",
                "examples": [{"ko": "사업에 성공하다.", "vi": "Thành công trong sự nghiệp."}]
            }
        ],
        "hanja_segments": [
            {"char": "成", "hangul": "성", "hanviet": "thành", "meaning": "hoàn thành, nên", "radical": "戈", "strokes": 6},
            {"char": "功", "hangul": "공", "hanviet": "công", "meaning": "công trạng, công lao", "radical": "力", "strokes": 5}
        ]
    },

    # ------------------ NATIVE KOREAN & IRREGULAR VERBS ------------------
    {
        "stable_key": "ko_gada_01",
        "headword": "가다",
        "origin_type": "NATIVE_KOREAN",
        "origin_status": "VERIFIED_PRIMARY",
        "phonetic_vi": "ca-ta",
        "topik_level": 1,
        "source_dataset": "NIKL",
        "source_id": "10001",
        "senses": [
            {
                "pos": "Động từ",
                "meaning_vi": "đi, rời khỏi, tiến bước",
                "definition_ko": "한 곳에서 다른 곳으로 장소를 이동하다.",
                "examples": [{"ko": "집에 가다.", "vi": "Đi về nhà."}]
            }
        ],
        "hanja_segments": [],
        "precomputed_forms": [
            {"form": "갔어요", "rule": "PAST_INFORMAL", "desc": "Thì quá khứ thân mật (-았어요)"},
            {"form": "갑니다", "rule": "PRES_FORMAL", "desc": "Thì hiện tại kính ngữ (-ㅂ니다)"},
            {"form": "가요", "rule": "PRES_INFORMAL", "desc": "Thì hiện tại thân mật (-아요)"},
            {"form": "가서", "rule": "CONNECTIVE_SEO", "desc": "Liên từ nguyên nhân/thứ tự (-아서)"}
        ]
    },
    {
        "stable_key": "ko_deutda_01",
        "headword": "듣다",
        "origin_type": "NATIVE_KOREAN",
        "origin_status": "VERIFIED_PRIMARY",
        "phonetic_vi": "đứt-tá",
        "topik_level": 1,
        "source_dataset": "NIKL",
        "source_id": "15002",
        "senses": [
            {
                "pos": "Động từ",
                "meaning_vi": "nghe, lắng nghe (bất quy tắc ㄷ)",
                "definition_ko": "귀로 소리를 느끼거나 주의 깊게 알아채다.",
                "examples": [{"ko": "음악을 듣다.", "vi": "Nghe âm nhạc."}]
            }
        ],
        "hanja_segments": [],
        "precomputed_forms": [
            {"form": "들었어요", "rule": "DIGEUT_IRREGULAR_PAST", "desc": "Bất quy tắc ㄷ: biến thành ㄹ + 었어요"},
            {"form": "들어요", "rule": "DIGEUT_IRREGULAR_PRES", "desc": "Bất quy tắc ㄷ: biến thành ㄹ + 어요"},
            {"form": "듣습니다", "rule": "PRES_FORMAL", "desc": "Hiện tại kính ngữ trang trọng (-습니다)"},
            {"form": "들어서", "rule": "DIGEUT_CONNECTIVE", "desc": "Bất quy tắc ㄷ trước liên từ -어서"}
        ]
    },
    {
        "stable_key": "ko_deulda_01",
        "headword": "들다",
        "origin_type": "NATIVE_KOREAN",
        "origin_status": "VERIFIED_PRIMARY",
        "phonetic_vi": "đưl-ta",
        "topik_level": 1,
        "source_dataset": "NIKL",
        "source_id": "15003",
        "senses": [
            {
                "pos": "Động từ",
                "meaning_vi": "cầm, xách, nhấc lên; đi vào; tốn (thời gian, tiền bạc)",
                "definition_ko": "손에 쥐거나 들거나, 어떤 장소로 들어가다.",
                "examples": [{"ko": "가방을 들다.", "vi": "Cầm xách chiếc cặp sách."}]
            }
        ],
        "hanja_segments": [],
        "precomputed_forms": [
            {"form": "듭니다", "rule": "RIEUL_DROP_FORMAL", "desc": "Kính ngữ trang trọng (-ㅂ니다)"}
        ]
    },
    {
        "stable_key": "ko_chupda_01",
        "headword": "춥다",
        "origin_type": "NATIVE_KOREAN",
        "origin_status": "VERIFIED_PRIMARY",
        "phonetic_vi": "chúp-tá",
        "topik_level": 1,
        "source_dataset": "NIKL",
        "source_id": "17003",
        "senses": [
            {
                "pos": "Tính từ",
                "meaning_vi": "lạnh, giá lạnh (bất quy tắc ㅂ)",
                "definition_ko": "온도가 낮아 차갑게 느껴지다.",
                "examples": [{"ko": "날씨가 춥다.", "vi": "Thời tiết lạnh giá."}]
            }
        ],
        "hanja_segments": [],
        "precomputed_forms": [
            {"form": "추워요", "rule": "BIEUP_IRREGULAR_PRES", "desc": "Bất quy tắc ㅂ: biến thành 워 + 요"},
            {"form": "추웠어요", "rule": "BIEUP_IRREGULAR_PAST", "desc": "Bất quy tắc ㅂ: biến thành 웠 + 어요"},
            {"form": "춥습니다", "rule": "PRES_FORMAL", "desc": "Hiện tại kính ngữ trang trọng (-습니다)"}
        ]
    },
    {
        "stable_key": "ko_moruda_01",
        "headword": "모르다",
        "origin_type": "NATIVE_KOREAN",
        "origin_status": "VERIFIED_PRIMARY",
        "phonetic_vi": "mô-rư-ta",
        "topik_level": 1,
        "source_dataset": "NIKL",
        "source_id": "18004",
        "senses": [
            {
                "pos": "Động từ",
                "meaning_vi": "không biết, không rõ (bất quy tắc 르)",
                "definition_ko": "어떤 사실이나 내용을 알지 못하다.",
                "examples": [{"ko": "저는 잘 모릅니다.", "vi": "Tôi không biết rõ lắm."}]
            }
        ],
        "hanja_segments": [],
        "precomputed_forms": [
            {"form": "몰라요", "rule": "REU_IRREGULAR_PRES", "desc": "Bất quy tắc 르: gấp đôi ㄹ + 아요"},
            {"form": "몰랐어요", "rule": "REU_IRREGULAR_PAST", "desc": "Bất quy tắc 르: gấp đôi ㄹ + 았어요"},
            {"form": "모릅니다", "rule": "PRES_FORMAL", "desc": "Hiện tại kính ngữ trang trọng (-ㅂ니다)"}
        ]
    },
    {
        "stable_key": "ko_mandeulda_01",
        "headword": "만들다",
        "origin_type": "NATIVE_KOREAN",
        "origin_status": "VERIFIED_PRIMARY",
        "phonetic_vi": "man-đưl-ta",
        "topik_level": 1,
        "source_dataset": "NIKL",
        "source_id": "19005",
        "senses": [
            {
                "pos": "Động từ",
                "meaning_vi": "làm, tạo ra, chế tạo (biến âm ㄹ)",
                "definition_ko": "노력이나 기술을 들여 물건이나 상태를 이루다.",
                "examples": [{"ko": "음식을 만들다.", "vi": "Nấu làm món ăn."}]
            }
        ],
        "hanja_segments": [],
        "precomputed_forms": [
            {"form": "만듭니다", "rule": "RIEUL_DROP_FORMAL", "desc": "Rơi patchim ㄹ trước ㄴ, ㅂ, ㅅ: 만들 + ㅂ니다 -> 만듭니다"},
            {"form": "만들어요", "rule": "PRES_INFORMAL", "desc": "Hiện tại thân mật (-어요)"},
            {"form": "만들었어요", "rule": "PAST_INFORMAL", "desc": "Quá khứ thân mật (-었어요)"}
        ]
    },
    {
        "stable_key": "ko_haneul_01",
        "headword": "하늘",
        "origin_type": "NATIVE_KOREAN",
        "origin_status": "VERIFIED_PRIMARY",
        "phonetic_vi": "ha-nưl",
        "topik_level": 1,
        "source_dataset": "NIKL",
        "source_id": "20006",
        "senses": [
            {
                "pos": "Danh từ",
                "meaning_vi": "bầu trời, trời xanh",
                "definition_ko": "지표면 위로 펼쳐진 무한한 공간.",
                "examples": [{"ko": "푸른 하늘.", "vi": "Bầu trời trong xanh."}]
            }
        ],
        "hanja_segments": []
    },

    # ------------------ LOANWORDS (TỪ VAY MƯỢN) ------------------
    {
        "stable_key": "ko_keompyuteo_01",
        "headword": "컴퓨터",
        "origin_type": "LOANWORD",
        "origin_status": "VERIFIED_PRIMARY",
        "phonetic_vi": "kơm-piu-thơ",
        "topik_level": 1,
        "source_dataset": "Wiktionary",
        "source_id": "loan_computer",
        "senses": [
            {
                "pos": "Danh từ",
                "meaning_vi": "máy vi tính, máy điện toán",
                "definition_ko": "전자 회로를 이용하여 계산이나 정보 처리를 하는 기계.",
                "examples": [{"ko": "컴퓨터를 켜다.", "vi": "Bật máy tính."}]
            }
        ],
        "hanja_segments": [],
        "origins": [
            {
                "source_language": "English",
                "source_form": "computer",
                "adaptation_note": "Chuyển tự ngữ âm tiếng Anh sang Hangul",
                "verification_status": "VERIFIED_PRIMARY",
                "evidence_note": "Từ điển ngoại lai NIKL"
            }
        ]
    },
    {
        "stable_key": "ko_keopi_01",
        "headword": "커피",
        "origin_type": "LOANWORD",
        "origin_status": "VERIFIED_PRIMARY",
        "phonetic_vi": "kơ-phi",
        "topik_level": 1,
        "source_dataset": "Wiktionary",
        "source_id": "loan_coffee",
        "senses": [
            {
                "pos": "Danh từ",
                "meaning_vi": "cà phê, nước uống cà phê",
                "definition_ko": "커피나무 열매의 씨를 볶아서 가루로 만든 것, 또는 그것을 끓인 차.",
                "examples": [{"ko": "따뜻한 커피 한 잔.", "vi": "Một tách cà phê ấm nóng."}]
            }
        ],
        "hanja_segments": [],
        "origins": [
            {
                "source_language": "English",
                "source_form": "coffee",
                "adaptation_note": "Chuyển tự từ tiếng Anh",
                "verification_status": "VERIFIED_PRIMARY",
                "evidence_note": "Từ điển NIKL"
            }
        ]
    }
]


def build_core_database(schema_path: str, output_path: str) -> None:
    """Khởi tạo database và nạp dữ liệu mẫu hoàn chỉnh."""
    print(f"[*] Bắt đầu ETL Spike: Nạp schema từ {schema_path}...")
    
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    if os.path.exists(output_path):
        os.remove(output_path)

    conn = sqlite3.connect(output_path)
    conn.execute("PRAGMA foreign_keys = ON;")

    with open(schema_path, "r", encoding="utf-8") as f:
        schema_sql = f.read()
    conn.executescript(schema_sql)

    # 1. Ghi Metadata
    conn.execute(
        "INSERT INTO db_metadata (key, value) VALUES (?, ?), (?, ?), (?, ?);",
        (
            "schema_version", "1.0.0",
            "dataset_version", "2026.10-spike",
            "built_at", datetime.now(timezone.utc).isoformat()
        )
    )

    # 2. Xử lý và nạp dữ liệu từ điển
    total_lexemes = 0
    total_senses = 0
    total_aliases = 0
    total_precomputed = 0
    total_hanja_segments = 0

    for entry in RAW_ENTRIES:
        hw_nfc = nfc(entry["headword"])
        stable_key = entry["stable_key"]
        orig_type = entry["origin_type"]
        orig_status = entry["origin_status"]
        phonetic_vi = nfc(entry.get("phonetic_vi", ""))
        topik_lvl = entry.get("topik_level", 0)
        src_ds = entry["source_dataset"]
        src_id = entry.get("source_id", "")

        cur = conn.execute(
            """
            INSERT INTO lexemes (stable_key, headword_nfc, origin_type, origin_status,
                                phonetic_vi, topik_level, source_dataset, source_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (stable_key, hw_nfc, orig_type, orig_status, phonetic_vi, topik_lvl, src_ds, src_id)
        )
        lexeme_id = cur.lastrowid
        total_lexemes += 1

        # Sinh Chosung Alias
        chosung_shorthand = extract_chosung(hw_nfc)
        conn.execute(
            "INSERT INTO aliases (lexeme_id, alias_text, alias_type) VALUES (?, ?, ?)",
            (lexeme_id, chosung_shorthand, "CHOSUNG")
        )
        total_aliases += 1

        # Nạp Senses
        meanings_for_fts = []
        unaccented_for_fts = []
        for s in entry.get("senses", []):
            meaning_vi = nfc(s["meaning_vi"])
            meaning_unaccented = remove_vietnamese_accents(meaning_vi)
            def_ko = nfc(s.get("definition_ko", ""))
            pos = s.get("pos", "")
            examples_json = json.dumps(s.get("examples", []), ensure_ascii=False)

            conn.execute(
                """
                INSERT INTO senses (lexeme_id, part_of_speech, meaning_vi, meaning_vi_normalized,
                                   definition_ko, examples_json, display_order)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                (lexeme_id, pos, meaning_vi, meaning_unaccented, def_ko, examples_json, 1)
            )
            total_senses += 1
            meanings_for_fts.append(meaning_vi)
            unaccented_for_fts.append(meaning_unaccented)

        # Nạp Hanja Master & Segments
        for idx, seg in enumerate(entry.get("hanja_segments", [])):
            char = seg["char"]
            radical = seg.get("radical", "")
            strokes = seg.get("strokes", 0)
            char_meaning = seg.get("meaning", "")
            h_reading = seg.get("hangul", "")
            hv_reading = seg.get("hanviet", "")

            # Thêm vào hanja_master
            conn.execute(
                """
                INSERT OR IGNORE INTO hanja_master (hanja_char, radical, stroke_count, meaning_vi, general_level)
                VALUES (?, ?, ?, ?, ?)
                """,
                (char, radical, strokes, char_meaning, "Basic")
            )
            # Thêm vào hanja_readings
            conn.execute(
                """
                INSERT OR IGNORE INTO hanja_readings (hanja_char, reading_ko, reading_vi, is_primary)
                VALUES (?, ?, ?, 1)
                """,
                (char, h_reading, hv_reading)
            )
            # Thêm vào lexeme_hanja_segments
            conn.execute(
                """
                INSERT INTO lexeme_hanja_segments (lexeme_id, segment_index, syllable_ko, hanja_char, reading_vi, meaning_vi)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                (lexeme_id, idx, h_reading, char, hv_reading, char_meaning)
            )
            total_hanja_segments += 1

        # Nạp Precomputed Forms
        for pf in entry.get("precomputed_forms", []):
            inflected = nfc(pf["form"])
            rule = pf["rule"]
            desc = pf.get("desc", "")
            conn.execute(
                """
                INSERT OR REPLACE INTO precomputed_forms (inflected_form, lexeme_id, rule_applied, grammar_desc, confidence)
                VALUES (?, ?, ?, ?, 1.0)
                """,
                (inflected, lexeme_id, rule, desc)
            )
            total_precomputed += 1

        # Nạp Word Origins (nếu có)
        for orig in entry.get("origins", []):
            conn.execute(
                """
                INSERT INTO word_origins (lexeme_id, origin_type, source_language, source_form,
                                         adaptation_note, verification_status, evidence_note)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                (
                    lexeme_id, orig_type, orig.get("source_language"), orig.get("source_form"),
                    orig.get("adaptation_note"), orig.get("verification_status", "VERIFIED_PRIMARY"),
                    orig.get("evidence_note")
                )
            )

        # Nạp bảng ảo search_fts
        combined_meaning = " ".join(meanings_for_fts)
        combined_unaccented = " ".join(unaccented_for_fts)
        conn.execute(
            """
            INSERT INTO search_fts (headword_nfc, alias_chosung, meaning_vi, unaccented_vi)
            VALUES (?, ?, ?, ?)
            """,
            (hw_nfc, chosung_shorthand, combined_meaning, combined_unaccented)
        )

    conn.commit()
    conn.close()

    print(f"[✓] Đã tạo thành công cơ sở dữ liệu: {output_path}")
    print(f"    - Lexemes           : {total_lexemes} mục từ")
    print(f"    - Senses            : {total_senses} nét nghĩa")
    print(f"    - Chosung Aliases   : {total_aliases} mã gõ tắt")
    print(f"    - Precomputed Forms : {total_precomputed} dạng thức chia trước")
    print(f"    - Hanja Segments    : {total_hanja_segments} liên kết chiết tự Hán-Hàn-Việt")
    print(f"    - Kích thước file   : {os.path.getsize(output_path) / 1024:.2f} KB")


def main():
    root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    schema_path = os.path.join(root_dir, "schema", "core_schema.sql")
    output_path = os.path.join(root_dir, "output", "dictionary_sample.db")

    build_core_database(schema_path, output_path)


if __name__ == "__main__":
    main()
