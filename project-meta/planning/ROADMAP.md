# Lộ Trình Phát Triển Dự Án (ROADMAP.md)

Lộ trình được thiết kế theo phương pháp luận **GSD (Get Shit Done)** và nguyên tắc kỹ thuật **Matt Pocock**: chia nhỏ thành các pha độc lập (Phase), mỗi pha bao gồm các kế hoạch nguyên tử (Plan) với 2-3 nhiệm vụ (Task) cụ thể. Tuyệt đối không ôm đồm nhiều việc cùng lúc; mỗi bước đều có lệnh kiểm chứng thực nghiệm rõ ràng.

---

## Bảng Tổng Quan Các Giai Đoạn (Phases Overview)

| Phase | Tên Giai Đoạn | Mục Tiêu Cốt Lõi | Số Kế Hoạch | Trạng Thái |
| :---: | :--- | :--- | :---: | :---: |
| **Phase 0** | **Data Feasibility & ETL Spike** | Rà soát nguồn dữ liệu mở, thiết kế schema SQLite core, xây dựng spike ETL tạo file database mẫu. | 2 Plans | **READY** |
| **Phase 1** | **Dual-DB Architecture & Search Core** | Khung client Android: Tách 2 DB (`dictionary_core.db` + `user_workspace.db`), bộ máy tra cứu 5 tầng. | 2 Plans | BACKLOG |
| **Phase 2** | **Morphology Rule Engine** | Bộ máy deconjugation xử lý 7 nhóm bất quy tắc (`ㅂ`, `ㄷ`, `ㅅ`, `ㅎ`, `르`, `으`, `ㄹ`), xếp hạng ứng viên. | 2 Plans | BACKLOG |
| **Phase 3** | **Contextual Hanja & Etymology** | Ánh xạ âm tiết - chữ Hán - âm Hán-Việt, xây dựng đồ thị từ nguyên và huy hiệu minh bạch nguồn gốc. | 2 Plans | BACKLOG |
| **Phase 4** | **User Workspace & SRS Flashcards** | Quản lý sổ tay từ vựng, ghi chú Markdown, thuật toán lặp lại ngắt quãng SuperMemo SM-2. | 2 Plans | BACKLOG |
| **Phase 5** | **Android System Integration** | Tích hợp `ACTION_PROCESS_TEXT` để tra nhanh từ mọi app khác qua BottomSheet không cần quyền vẽ màn hình. | 2 Plans | BACKLOG |
| **Phase 6** | **Optional AI Layer & Grounding** | Giao diện BYOK thống nhất (OpenAI/Gemini/DeepSeek/Claude), bộ lọc chống ảo giác bằng dữ liệu từ điển. | 2 Plans | BACKLOG |
| **Phase 7** | **Packaging, Hardening & CI/CD** | Tự động hóa GitHub Actions build APK, kiểm thử Robolectric, ký số và phát hành release tự động. | 2 Plans | BACKLOG |

---

## Chi Tiết Các Kế Hoạch Theo Từng Giai Đoạn

### Phase 0: Data Feasibility & ETL Spike
- **Plan 00-01**: Rà soát nguồn dữ liệu từ điển tiếng Hàn - tiếng Việt, chữ Hán mở (NIKL, StarDict, Wiktionary, Unihan) và giấy phép bản quyền.
- **Plan 00-02**: Viết pipeline ETL độc lập (Python/SQLite), chuẩn hóa Unicode NFC, sinh bảng chỉ mục FTS5 và kiểm tra tính toàn vẹn của schema `dictionary_core.db`.

### Phase 1: Dual-DB Architecture & Search Core
- **Plan 01-01**: Thiết lập kiến trúc Dual-DB trên Android: Adapter SQLite đọc chỉ đọc cho `dictionary_core.db` và Room Database cho `user_workspace.db`.
- **Plan 01-02**: Xây dựng Pipeline tra cứu 5 tầng: Khớp chính xác $\to$ Alias/Chosung (`ㄱㅂ`) $\to$ Dạng chia tính trước $\to$ Deconjugation $\to$ FTS5 Prefix/Unaccented.

### Phase 2: Morphology Rule Engine (Deconjugation)
- **Plan 02-01**: Xây dựng mô hình quy tắc hình thái học cho 7 nhóm động từ/tính từ bất quy tắc tiếng Hàn kèm bộ test suite toàn diện.
- **Plan 02-02**: Tích hợp Deconjugator vào Search Pipeline, trả về danh sách ứng viên (`CandidateLemma`) kèm điểm tin cậy và giải thích quy tắc.

### Phase 3: Contextual Hanja & Etymology Display
- **Plan 03-01**: Xây dựng bảng ánh xạ âm tiết `lexeme_hanja_segments`, truy vấn từ nguyên Hán-Hàn và Hán-Việt, trích xuất cây phả hệ từ vựng (Etymology Graph).
- **Plan 03-02**: Thiết kế giao diện chi tiết từ vựng với các huy hiệu phân loại nguồn gốc (`OriginType`) và cấp độ xác thực (`VerificationStatus`).

### Phase 4: User Workspace & SRS Flashcards
- **Plan 04-01**: Triển khai tầng lưu trữ Room cho từ vựng đã lưu (`saved_words`), thẻ tag, ghi chú cá nhân và xuất/nhập file JSON.
- **Plan 04-02**: Xây dựng hệ thống ôn tập Flashcard SRS áp dụng thuật toán SuperMemo SM-2 (tính toán `interval`, `ease_factor`, `due_date`).

### Phase 5: Android System Integration
- **Plan 05-01**: Đăng ký `ACTION_PROCESS_TEXT` intent filter trong `AndroidManifest.xml` tiếp nhận chuỗi văn bản bôi đen từ ứng dụng ngoài.
- **Plan 05-02**: Xây dựng `QuickLookupBottomSheet` tối ưu tốc độ phản hồi (< 100ms) để hiển thị nghĩa, từ nguyên và dạng thức ngữ pháp.

### Phase 6: Optional AI Layer (BYOK & Local Grounding)
- **Plan 06-01**: Xây dựng client tích hợp AI thống nhất hỗ trợ BYOK (OpenAI, Gemini, Anthropic, DeepSeek) và giao diện Local GGUF.
- **Plan 06-02**: Xây dựng cơ chế Grounding Guard: Ép AI chỉ giải thích dựa trên dữ liệu từ điển đã truy xuất, kiểm định JSON Schema chặt chẽ.

### Phase 7: Packaging, Hardening & CI/CD
- **Plan 07-01**: Thiết lập GitHub Actions workflow kiểm thử tự động, build APK release đa kiến trúc và ký số an toàn.
- **Plan 07-02**: Cơ chế kiểm tra SHA-256 Checksum gói dữ liệu từ điển `dictionary_core.db` và phát hành GitHub Release tự động.
