---
phase: 2
plan: 2
completed_at: 2026-10-29T15:15:00Z
duration_minutes: 12
---

# Summary: Plan 2.2 - Tích Hợp Bộ Máy Hình Thái & Xếp Hạng Ứng Viên (Deconjugation Ranking)

## Results
- 2 tasks completed
- `CandidateLemma.kt` được tách thành model chuẩn độc lập với các trường xác thực từ điển (`isInDictionary`, `topikLevel`, `pos`).
- `MorphologyEngine.kt` tích hợp đồng thời 7 nhóm bất quy tắc (`IrregularVerbRules`) và các đuôi chia thông dụng (quá khứ, kính ngữ, liên từ, điều kiện).
- Tích hợp đối chiếu từ điển Core SQLite: tăng điểm tin cậy đối với từ gốc có thực trong từ điển, hạ điểm và lọc bỏ các suy diễn ảo.
- Tích hợp hoàn hảo vào Tầng 4 của `SearchPipeline.kt`.
- `MorphologyEngineTest.kt` và `SearchPipelineTest.kt` kiểm tra giải quyết đa nghĩa (ambiguity resolution): "들었어요" trả về cả "듣다" (nghe) và "들다" (cầm/nhấc).

## Tasks Completed
| Task | Description | Status |
|------|-------------|--------|
| 1 | Xây dựng `MorphologyEngine` với cơ chế đối chiếu CSDL Core và xếp hạng ứng viên | ✅ |
| 2 | Tách và chuẩn hóa model `CandidateLemma.kt` với các trường xác thực từ điển | ✅ |
| 3 | Tích hợp `MorphologyEngine` vào Tầng 4 của `SearchPipeline.kt` | ✅ |
| 4 | Xây dựng bộ test suite `MorphologyEngineTest.kt` và bổ sung test tích hợp vào `SearchPipelineTest.kt` | ✅ |

## Deviations Applied
- Bổ sung mục từ `들다` (cầm, nhấc, tốn) vào CSDL mẫu `dictionary_core.db` và `dictionary_sample.db` để phục vụ kịch bản kiểm thử giải quyết đa nghĩa ngữ pháp đối chiếu song song với `듣다` (nghe).

## Files Changed
- `app/src/main/java/com/hanviet/etymoai/domain/morphology/CandidateLemma.kt` - Model ứng viên từ gốc sau deconjugation
- `app/src/main/java/com/hanviet/etymoai/domain/morphology/MorphologyEngine.kt` - Bộ máy hình thái xếp hạng ứng viên và xác thực từ điển Core
- `app/src/main/java/com/hanviet/etymoai/domain/morphology/IrregularVerbRules.kt` - Tinh chỉnh import và sửa lỗi deconjugation đuôi trang trọng
- `app/src/main/java/com/hanviet/etymoai/domain/search/SearchPipeline.kt` - Tích hợp MorphologyEngine vào Tầng 4
- `app/src/main/assets/dictionary_core.db` - CSDL mẫu cập nhật mục từ `들다`
- `data-pipeline/etl/spike_build_core_db.py` - Cập nhật dữ liệu hạt giống và xây dựng lại CSDL mẫu
- `data-pipeline/output/dictionary_sample.db` - File CSDL mẫu đã build lại
- `app/src/test/java/com/hanviet/etymoai/domain/morphology/MorphologyEngineTest.kt` - Unit tests cho MorphologyEngine
- `app/src/test/java/com/hanviet/etymoai/domain/search/SearchPipelineTest.kt` - Tests tích hợp Tầng 4 và xử lý đa nghĩa

## Verification
- Tra cứu "들었어요" trả về cả 2 từ gốc "듣다" và "들다": ✅ Passed
- Tra cứu "추워서" trả về từ gốc "춥다" kèm ghi chú bất quy tắc ㅂ: ✅ Passed
- Tra cứu "만듭니다" trả về từ gốc "만들다" kèm ghi chú rơi patchim ㄹ: ✅ Passed
- Tốc độ tra cứu hình thái kết hợp CSDL: < 10ms: ✅ Passed
