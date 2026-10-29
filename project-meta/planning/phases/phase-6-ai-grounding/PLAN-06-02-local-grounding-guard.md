---
phase: 6
plan: 2
wave: 2
depends_on: ["6.1"]
files_modified:
  - "app/src/main/java/com/hanviet/etymoai/ai/grounding/GroundingGuard.kt"
  - "app/src/main/java/com/hanviet/etymoai/ai/grounding/EtymologyPromptBuilder.kt"
  - "app/src/test/java/com/hanviet/etymoai/ai/grounding/GroundingGuardTest.kt"
autonomous: true
user_setup: []

must_haves:
  truths:
    - "GroundingGuard kiểm tra và tiêm 100% dữ liệu từ điển thực tế (Headword, Sense, HanjaSegments) vào prompt trước khi gửi tới AI."
    - "Nếu AI trả về thông tin từ nguyên mâu thuẫn hoặc 'bịa' chữ Hán không có trong Grounding Context, hệ thống tự động từ chối hoặc gắn nhãn UNVERIFIED."
  artifacts:
    - "app/src/main/java/com/hanviet/etymoai/ai/grounding/GroundingGuard.kt"
    - "app/src/main/java/com/hanviet/etymoai/ai/grounding/EtymologyPromptBuilder.kt"
---

# Plan 6.2: Bộ Lọc Neo Dữ Liệu & Chống Ảo Giác AI (Local Grounding Guard)

<objective>
Xây dựng thành phần `GroundingGuard` và bộ sinh câu lệnh có cấu trúc `EtymologyPromptBuilder` để thực thi nguyên tắc cốt lõi: *"Không bịa nguồn gốc"*. AI chỉ được phép đóng vai trò trợ lý ngôn ngữ giải thích sâu dựa trên dữ liệu có thật đã được truy xuất từ cơ sở dữ liệu lõi `dictionary_core.db`.

Mục đích: Loại bỏ hoàn toàn hiện tượng AI tự sáng tác chữ Hán, suy diễn sai lệch âm Hán-Việt hoặc đưa ra thông tin giả mạo.
Đầu ra: Class `GroundingGuard.kt`, `EtymologyPromptBuilder.kt`, và test suite `GroundingGuardTest.kt`.
</objective>

<context>
Xem xét các tài liệu:
- project-meta/spec/SPEC.md (Mục 1. Nguyên lý Liêm chính học thuật)
- project-meta/spec/GLOSSARY.md (Khái niệm GroundingContext)
</context>

<tasks>

<task type="auto">
  <name>Xây dựng EtymologyPromptBuilder với System Guard chặt chẽ</name>
  <files>app/src/main/java/com/hanviet/etymoai/ai/grounding/EtymologyPromptBuilder.kt</files>
  <action>
    Triển khai `EtymologyPromptBuilder`:
    - Nhận dữ liệu thực tế: `WordEtymology` (gồm danh sách `HanjaSegment`, `OriginType`, định nghĩa từ CSDL).
    - Sinh prompt có cấu trúc JSON Schema:
      - System Prompt: *"Bạn là chuyên gia ngôn ngữ học Hán-Nôm và Tiếng Hàn. Bạn CHỈ ĐƯỢC PHÉP giải thích dựa trên các chữ Hán và dữ liệu được cung cấp dưới đây. TUYỆT ĐỐI KHÔNG tự sáng tác thêm chữ Hán mới. Nếu từ là từ thuần Hàn (NATIVE_KOREAN), hãy nêu rõ là không có chữ Hán."*
      - Context Injection: Truyền toàn bộ âm tiết, chữ Hán, âm Hán Việt tương ứng dưới dạng JSON rõ ràng.
      - Yêu cầu định dạng đầu ra chuẩn JSON gồm: `etymologySummaryVi`, `deepNuanceExplanationVi`, `culturalContextVi`, `mnemonicsTipVi`.
  </action>
  <verify>Viết test kiểm tra chuỗi prompt sinh ra chứa đầy đủ các phân đoạn Hán tự từ DB.</verify>
  <done>EtymologyPromptBuilder sinh prompt có tính cấu trúc cao, tuân thủ nghiêm ngặt nguyên lý Grounding.</done>
</task>

<task type="auto">
  <name>Hiện thực hóa GroundingGuard kiểm duyệt phản hồi đầu ra</name>
  <files>
    - app/src/main/java/com/hanviet/etymoai/ai/grounding/GroundingGuard.kt
    - app/src/test/java/com/hanviet/etymoai/ai/grounding/GroundingGuardTest.kt
  </files>
  <action>
    Triển khai `GroundingGuard`:
    1. Tiếp nhận phản hồi thô từ AI model.
    2. Parse và xác thực cấu trúc JSON theo Schema.
    3. Kiểm tra tính nhất quán (Consistency Check):
       - Đối chiếu các chữ Hán xuất hiện trong văn bản giải thích của AI với tập hợp chữ Hán hợp lệ trong `HanjaSegment` ban đầu.
       - Nếu phát hiện chữ Hán lạ chưa từng có trong từ điển lõi, gắn cờ cảnh báo: `hasUnverifiedHanja = true` và hạ `VerificationStatus` xuống `AI_SUGGESTED` kèm nhãn cảnh báo đỏ cho người dùng.
    4. Ghi kết quả hợp lệ vào `ai_cache` trong `user_workspace.db` để tái sử dụng O(1) lần sau mà không tốn token/quota.
  </action>
  <verify>Chạy `GroundingGuardTest.kt` kiểm tra trường hợp AI cố ý bịa chữ Hán và xác nhận Guard phát hiện thành công.</verify>
  <done>Bộ lọc GroundingGuard hoạt động xuất sắc, bảo vệ tính trung thực và học thuật của ứng dụng.</done>
</task>

</tasks>

<verification>
Sau khi hoàn thành:
- [ ] Phản hồi từ AI luôn tuân thủ đúng từ nguyên thực tế trong từ điển.
- [ ] Mọi trường hợp nghi vấn đều được gắn nhãn cảnh báo rõ ràng.
</verification>

<success_criteria>
- [ ] Tất cả nhiệm vụ đều được xác thực bằng unit test.
- [ ] Các tiêu chí must-haves được đáp ứng trọn vẹn.
</success_criteria>
