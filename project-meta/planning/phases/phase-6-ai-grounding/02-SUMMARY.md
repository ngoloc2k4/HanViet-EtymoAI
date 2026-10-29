# Summary Plan 6.2: Local Grounding Guard & Anti-Hallucination

## Hoàn thành
- **EtymologyPromptBuilder**: Tiêm 100% dữ liệu từ điển thực tế (Headword, OriginType, HanjaSegments) vào System Prompt và User Prompt. Tạo hash SHA-256 tiền định phục vụ bộ nhớ đệm.
- **GroundingGuard**: Bộ lọc độc quyền kiểm định và chống ảo giác Hán tự:
  - Quét biểu thức chính quy CJK Hanja `[\u4E00-\u9FFF\u3400-\u4DBF\uF900-\uFAFF]`.
  - Đối chiếu danh sách chữ Hán mà AI tạo ra với tập hợp chữ Hán đã được kiểm chứng trong Core DB.
  - Tự động đánh cờ `hasUnverifiedHanja = true` nếu AI tự sáng tác chữ Hán mới hoặc gán ghép chữ Hán cho từ thuần Hàn (NATIVE_KOREAN).
  - Tích hợp `AiCacheDao` trong `user_workspace.db` để lưu cache phản hồi hợp lệ, hỗ trợ truy vấn O(1) offline không tốn token/quota API.
- **Unit Tests**: `GroundingGuardTest.kt` kiểm tra trích xuất Hán tự, phát hiện ảo giác chữ Hán lạ, kiểm tra từ thuần Hàn và kiểm định caching.
