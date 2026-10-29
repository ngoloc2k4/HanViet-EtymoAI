---
phase: 0
plan: 2
completed_at: 2026-10-29T13:15:45Z
duration_minutes: 8
---

# Summary: Plan 0.2 - Khởi Tạo Schema & Thử Nghiệm Pipeline ETL (ETL Spike)

## Results
- 2 tasks completed
- All verifications passed (100% test pass rate)
- Average query latency: 0.025 ms (< 5.0 ms threshold)

## Tasks Completed
| Task | Description | Commit | Status |
|------|-------------|--------|--------|
| 1 | Viết DDL chuẩn `core_schema.sql` với FTS5 Virtual Table & B-tree indexes | d0f8507 | ✅ |
| 2 | Viết script ETL Spike (`spike_build_core_db.py`) và kiểm định tính toàn vẹn FTS5 (`test_core_db_integrity.py`) | c3995e1 | ✅ |

## Deviations Applied
- [Rule 1 - Bug] Đã bổ sung `import json` và cập nhật `datetime.now(timezone.utc)` trong `spike_build_core_db.py` để tương thích hoàn toàn với Python 3.14.

## Files Changed
- `data-pipeline/schema/core_schema.sql` - Đặc tả DDL cho 8 bảng quan hệ + 1 bảng ảo FTS5 (`search_fts`) + các chỉ mục B-tree
- `data-pipeline/etl/spike_build_core_db.py` - Script ETL spike nạp schema, chuẩn hóa Unicode NFC, sinh gõ tắt Chosung, dạng thức chia trước và chiết tự Hán-Hàn-Việt
- `data-pipeline/etl/test_core_db_integrity.py` - Bộ kiểm thử tự động kiểm tra toàn vẹn khóa ngoại, truy vấn tiếng Việt không dấu, Chosung và benchmark độ trễ
- `data-pipeline/output/dictionary_sample.db` - CSDL mẫu nhị phân SQLite (124 KB) sẵn sàng phục vụ kiểm thử cho Android client ở Phase 1

## Verification
- `core_schema.sql` nạp sạch vào SQLite engine: ✅ Passed
- `spike_build_core_db.py` sinh database `dictionary_sample.db` với 0 vi phạm khóa ngoại: ✅ Passed
- FTS5 tìm kiếm tiếng Việt không dấu (`"tam trang"` -> `기분`): ✅ Passed (0.20 ms)
- Tìm kiếm gõ tắt Chosung (`"ㄱㅂ"` -> `기분`): ✅ Passed (0.05 ms)
- Truy xuất dạng chia tính trước (`"갔어요"` -> `가다`, `"들었어요"` -> `듣다`): ✅ Passed (0.04 ms)
- Trích xuất chiết tự Hán-Hàn-Việt (`기분` -> `氣` *khí* + `分` *phân*): ✅ Passed
- Benchmark độ trễ trung bình: 0.025 ms / truy vấn: ✅ Passed
