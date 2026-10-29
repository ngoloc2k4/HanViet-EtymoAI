# Bài Học Kỹ Thuật Đúc Kết Từ V1 (Lessons Learned from VietnamKoreanDictionary)

Tài liệu này ghi lại các bài học xương máu (hard-earned lessons) thu được trong quá trình phát triển, kiểm thử và tối ưu dự án từ điển phiên bản V1. Những bài học này là kim chỉ nam để đảm bảo kiến trúc của **HanViet-EtymoAI** đạt chuẩn kỹ thuật cao cấp, không lặp lại các vết xe đổ.

---

## 1. Dữ liệu từ điển & Chỉ mục tìm kiếm (Data & Indexing)

### Vấn đề ở V1:
1. **Bộ dữ liệu cũ kỹ (StarDict/EV-Dict)**:
   - File dữ liệu `korvi_dictionary.db` ban đầu kế thừa từ các bộ từ điển mã nguồn mở đời cũ (thập niên 2000). Nhiều từ thông dụng bị thiếu hoặc chứa định nghĩa sai ngữ cảnh (ví dụ `기분` bị dịch thành *"sự sờ mó, bắt mạch"* theo thuật ngữ y học cổ truyền).
2. **Tìm kiếm bằng `LIKE %keyword%` quá chậm và nhạy cảm với dấu**:
   - Truy vấn SQL `WHERE word LIKE '%...%'` khiến Room phải scan toàn bộ bảng, gây giật lag khi danh mục từ vượt 50,000 từ.
   - Khi tìm kiếm tiếng Việt không dấu (ví dụ `"tam trang"`), truy vấn `LIKE` không khớp với `"tâm trạng"` nếu không tạo cột phụ unaccented.
3. **Cắt cụm từ thô sơ (Tokenization & N-grams)**:
   - Khi người dùng nhập cả câu `"học chào hỏi rồi học văn hoá"`, code logic ban đầu chỉ `split(" ")[0]` lấy đúng chữ `"học"`, làm mất hoàn toàn ngữ nghĩa của cụm 2 chữ (`"chào hỏi"`, `"văn hoá"`).

### Giải pháp bắt buộc ở HanViet-EtymoAI:
- **Chuẩn hóa Unicode NFC 100%**: Tất cả headword và text nhập vào phải đi qua bộ chuẩn hóa NFC trước khi tìm kiếm hoặc lưu trữ.
- **Sử dụng SQLite FTS5 với chỉ mục đa tầng**:
  - Tạo bảng ảo FTS5 hỗ trợ token hóa prefix, unaccented Vietnamese tokenizer.
  - Phân tầng kết quả tìm kiếm theo thứ tự ưu tiên:
    1. Khớp chính xác Headword (Exact match).
    2. Khớp từ viết tắt/Chosung (Alias / Chosung match: `ㄱㅂ` -> `기분`).
    3. Khớp biến thể ngữ pháp đã tính trước (Precomputed inflected form: `갔다` -> `가다`).
    4. Khớp tiền tố (Prefix match).
    5. Khớp nội dung FTS (Full-Text Search trong định nghĩa).
- **Trích xuất N-Gram đa từ**: Khi người dùng nhập một câu, thuật toán tìm kiếm trích xuất các n-gram từ dài nhất (Longest Match First: 3-gram -> 2-gram -> 1-gram) để ưu tiên nhận diện cụm danh từ/thành ngữ.

---

## 2. Kiến trúc Cơ sở dữ liệu: Tách biệt Core & Workspace (Dual-DB Architecture)

### Vấn đề ở V1:
- Dữ liệu từ điển gốc và dữ liệu người dùng (từ yêu thích, lịch sử tìm kiếm) bị nhồi chung vào một cơ sở dữ liệu Room duy nhất.
- Khi cần cập nhật database từ điển (ví dụ bổ sung thêm 20,000 từ vựng mới), ứng dụng gặp nguy cơ xóa trắng hoặc xung đột với lịch sử và từ đánh dấu yêu thích của người dùng nếu Room migration không được cấu hình cực kỳ phức tạp.

### Giải pháp bắt buộc ở HanViet-EtymoAI:
- **2 File Cơ sở dữ liệu hoàn toàn độc lập**:
  - `dictionary_core.db`: File SQLite chỉ đọc (Read-only, đóng gói trong APK hoặc tải theo gói phiên bản `dictionary_v1.db`). Không bao giờ có transaction ghi vào file này trên máy người dùng.
  - `user_workspace.db`: Cơ sở dữ liệu Room ghi độc lập, lưu trữ:
    - Bảng `saved_words` (từ vựng yêu thích, tag phân loại).
    - Bảng `notes` (ghi chú cá nhân của người học).
    - Bảng `srs_cards` (dữ liệu ôn tập Flashcard theo thuật toán SuperMemo SM-2).
    - Bảng `search_history` (lịch sử tra từ).
    - Bảng `ai_cache` (bộ nhớ đệm câu trả lời của AI).
- Cập nhật gói từ điển `dictionary_core.db` diễn ra tức thì, an toàn tuyệt đối 100% cho dữ liệu cá nhân của người dùng.

---

## 3. Khả năng chạy AI trên thiết bị di động (Local AI Realities)

### Vấn đề ở V1:
- Khi người dùng cấu hình nạp model Qwen 0.6B hoặc Qwen 4B GGUF vào máy Android, việc thực thi thực tế đòi hỏi dung lượng RAM thực từ 1GB đến 3.5GB.
- Ứng dụng V1 phản hồi sau 2 giây do vô tình rơi vào cơ chế fallback/heuristic mà không báo rõ ràng cho người dùng biết trạng thái model có đang nạp hay không, gây hiểu lầm là model AI đã chạy cục bộ siêu tốc.

### Giải pháp bắt buộc ở HanViet-EtymoAI:
- **Xác thực định dạng Binary GGUF tường minh**:
  - Bắt buộc kiểm tra Header binary GGUF: Magic number `GGUF` (`0x46554747`), Architecture, Tensor count, Quantization format, Context length.
  - Đọc trực tiếp `/proc/meminfo` (MemAvailable) trước khi cấp phát bộ nhớ để tránh crash `OutOfMemory` (OOM).
- **AI Grounding Protocol (Chống ảo giác)**:
  - AI không bao giờ được phép tự tiện "bịa" ra từ nguyên Hán-Việt hay chữ Hán khi không có trong cơ sở dữ liệu.
  - Truy vấn luôn lấy dữ liệu từ `dictionary_core.db` trước làm Grounding Context, sau đó chuyển cho AI (dù là Local LLM hay Online API qua BYOK) để tổng hợp giải thích dưới dạng JSON có schema kiểm định nghiêm ngặt.
- **BYOK (Bring Your Own Key) & Privacy**:
  - Cung cấp giao diện chuẩn hóa hỗ trợ OpenAI, Google Gemini, Anthropic Claude, DeepSeek cho phép người dùng dùng API Key cá nhân với chi phí rẻ và tốc độ tức thì.

---

## 4. Tích hợp Hệ thống Android (System Integration)

### Vấn đề ở V1:
- Việc tra cứu nhanh phụ thuộc vào mở ứng dụng hoặc popup trong app.

### Giải pháp ở HanViet-EtymoAI:
- Đăng ký `ACTION_PROCESS_TEXT` trong `AndroidManifest.xml`.
- Khi người dùng bôi đen bất kỳ từ tiếng Hàn hoặc tiếng Việt nào trong bất kỳ ứng dụng nào khác (trình duyệt, Zalo, Telegram, tài liệu PDF, v.v.), menu ngữ cảnh của Android sẽ xuất hiện nút **"Tra Từ Điển Hán-Việt"**.
- Nhấp vào sẽ mở một BottomSheet tra từ cực nhanh, trả về từ nguyên, biến thể ngữ pháp và nghĩa tức thì mà **không cần xin quyền nguy hiểm `SYSTEM_ALERT_WINDOW` (vẽ trên màn hình khác)**.

---

## 5. Môi trường Termux & Tự động hóa CI/CD

### Vấn đề ở V1:
- Chạy `./gradlew assembleDebug` trực tiếp trên Termux tiêu tốn từ 10-20 phút, dễ bị tràn RAM, và lỗi tương thích kiến trúc Android NDK / SDK tooling.
- Chạy các lệnh kiểm thử tự động đòi hỏi môi trường chuẩn.

### Giải pháp bắt buộc:
- **Tuyệt đối không compile APK trên Termux**:
  - Giữ Termux làm môi trường viết mã, phân tích AST, chạy kiểm thử Python/Unit test nhẹ và quản lý Git.
  - Mọi quy trình build APK, linting sâu, Robolectric tests, và ký số release đều thực thi tự động qua GitHub Actions CI/CD.
  - Sử dụng GitHub CLI (`gh`) và `curl` để tải trực tiếp APK đã build xong về máy khi workflow thành công.
