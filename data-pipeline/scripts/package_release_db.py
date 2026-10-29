#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Script đóng gói cơ sở dữ liệu Core (Release Data Pack Packager)
- Tối ưu hóa SQLite B-Tree (VACUUM, PRAGMA optimize)
- Nén tối đa bằng Gzip (Level 9)
- Sinh mã băm toàn vẹn SHA-256 Checksum
- Xuất manifest.json phục vụ DataPackIntegrityVerifier trên Android Client
"""

import os
import sys
import json
import gzip
import sqlite3
import hashlib
from datetime import datetime, timezone

def calculate_sha256(filepath: str) -> str:
    hasher = hashlib.sha256()
    with open(filepath, 'rb') as f:
        while chunk := f.read(65536):
            hasher.update(chunk)
    return hasher.hexdigest()

def optimize_and_package(
    db_path: str = "app/src/main/assets/dictionary_core.db",
    output_dir: str = "data-pipeline/dist",
    version: str = "1.0.0"
):
    if not os.path.exists(db_path):
        print(f"[ERROR] Không tìm thấy file database tại: {db_path}")
        sys.exit(1)

    os.makedirs(output_dir, exist_ok=True)
    out_db_name = "dictionary_core.db"
    target_db_path = os.path.join(output_dir, out_db_name)

    print(f"[*] Sao chép và tối ưu hóa database từ: {db_path} -> {target_db_path}")
    # Đọc ghi tối ưu
    src_conn = sqlite3.connect(db_path)
    dst_conn = sqlite3.connect(target_db_path)
    src_conn.backup(dst_conn)
    dst_conn.execute("VACUUM;")
    dst_conn.execute("PRAGMA optimize;")
    dst_conn.commit()

    # Thu thập thống kê
    cur = dst_conn.cursor()
    cur.execute("SELECT COUNT(*) FROM lexemes;")
    lexeme_count = cur.fetchone()[0]
    cur.execute("SELECT COUNT(*) FROM senses;")
    sense_count = cur.fetchone()[0]
    cur.execute("SELECT COUNT(*) FROM lexeme_hanja_segments;")
    segment_count = cur.fetchone()[0]
    cur.execute("SELECT COUNT(*) FROM aliases;")
    alias_count = cur.fetchone()[0]

    src_conn.close()
    dst_conn.close()

    raw_size = os.path.getsize(target_db_path)
    raw_sha256 = calculate_sha256(target_db_path)

    # Nén Gzip level 9
    gz_filename = f"{out_db_name}.gz"
    gz_path = os.path.join(output_dir, gz_filename)
    print(f"[*] Nén tối đa Gzip (Level 9) -> {gz_path}")
    with open(target_db_path, 'rb') as f_in:
        with gzip.open(gz_path, 'wb', compresslevel=9) as f_out:
            while chunk := f_in.read(65536):
                f_out.write(chunk)

    gz_size = os.path.getsize(gz_path)
    gz_sha256 = calculate_sha256(gz_path)
    compression_ratio = (1.0 - (gz_size / raw_size)) * 100.0

    # Tạo manifest.json
    manifest = {
        "schema_version": 1,
        "package_version": version,
        "build_date": datetime.now(timezone.utc).isoformat(),
        "db_filename": out_db_name,
        "db_size_bytes": raw_size,
        "db_sha256": raw_sha256,
        "compressed_filename": gz_filename,
        "compressed_size_bytes": gz_size,
        "compressed_sha256": gz_sha256,
        "compression_ratio_percent": round(compression_ratio, 2),
        "statistics": {
            "lexeme_count": lexeme_count,
            "sense_count": sense_count,
            "hanja_segment_count": segment_count,
            "alias_count": alias_count
        }
    }

    manifest_path = os.path.join(output_dir, "manifest.json")
    with open(manifest_path, 'w', encoding='utf-8') as f:
        json.dump(manifest, f, ensure_ascii=False, indent=2)

    # Xuất file SHA256SUMS
    sha_path = os.path.join(output_dir, "SHA256SUMS")
    with open(sha_path, 'w', encoding='utf-8') as f:
        f.write(f"{raw_sha256}  {out_db_name}\n")
        f.write(f"{gz_sha256}  {gz_filename}\n")

    print(f"[+] Đóng gói hoàn tất!")
    print(f"    - Dung lượng gốc: {raw_size:,} bytes | SHA256: {raw_sha256}")
    print(f"    - Dung lượng nén: {gz_size:,} bytes ({compression_ratio:.1f}% tiết kiệm) | SHA256: {gz_sha256}")
    print(f"    - Tổng số từ vị: {lexeme_count:,} | Nét nghĩa: {sense_count:,} | Chiết tự: {segment_count:,}")
    print(f"    - Manifest: {manifest_path}")

if __name__ == "__main__":
    optimize_and_package()
