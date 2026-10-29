# Lịch Sử Trao Đổi, Phân Tích & Các Quyết Định Kỹ Thuật (V1 -> Idea 2.2)

Tài liệu này lưu trữ toàn bộ bối cảnh quá trình phát triển từ ứng dụng tiền nhiệm (`VietnamKoreanDictionary`) đến quyết định tái thiết toàn diện dự án thành **HanViet EtymoAI (Idea 2.2)**.

---

## 1. Dòng thời gian các yêu cầu người dùng (User Requests Timeline)

1. **Khởi tạo & Rà soát bảo mật toàn diện**:
   - Yêu cầu quét toàn bộ codebase bằng OpenCodeReview (`ocr`).
   - Xử lý các phát hiện từ High -> Medium -> Low severity (hardcoded keys, memory leak, unsafe file operations).
   - Quyết định giữ cổng LAN exposure nhưng bảo mật và tối ưu hơn để phục vụ kết nối log giữa PC/Agent và thiết bị Termux.

2. **Thiết lập CI/CD & Quản lý Release APK**:
   - Chuyển toàn bộ quy trình build APK lên GitHub Actions (không chạy `./gradlew` local trên Termux do giới hạn tài nguyên và kiến trúc aarch64).
   - Sinh Android Keystore an toàn, lưu trữ secret qua GitHub Actions Secrets dưới dạng Base64.
   - Tự động tạo bản phát hành `beta` trên GitHub Releases và hỗ trợ lệnh `curl` tự động tải APK về `/sdcard/Download/VietnamKoreanDictionary.apk`.

3. **Mở rộng kiến trúc AI 2 tầng (2-Tier AI Architecture)**:
   - Tầng 1: Model Embedding / Tra cứu ngữ nghĩa cục bộ.
   - Tầng 2: Local LLM (GGUF 0.5B, 0.6B, 0.8B, Qwen3.8-4B) chạy trực tiếp trên thiết bị, kết hợp tùy chọn Online LLM (BYOK).
   - Hỗ trợ nhập (import) model tùy chọn từ bộ nhớ máy, hiển thị chi tiết metadata của model đã nhập thay vì bị ẩn.

4. **Trải nghiệm người dùng (UX/UI)**:
   - Phân loại từ vựng đa dạng: cấp độ TOPIK 1 - 6, chủ đề đời sống, ẩm thực, văn phòng.
   - Dịch thuật song ngữ: Chuyển modal bottom sheet nửa màn hình thành Fullscreen Popup chuyên nghiệp. Hỗ trợ đa dạng engine (DeepL, Google Translate, Local AI Tier 2, Online Cloud).
   - Hiển thị văn bản AI mượt mà (Render Markdown chuẩn phong cách Word, typography rõ ràng, có nút xóa lịch sử).

5. **Phát hiện nút thắt dữ liệu & giới hạn của V1**:
   - **Vấn đề số lượng và chất lượng từ vựng**: Tra cứu các từ đời sống thiết yếu như `"tâm trạng"` không ra kết quả hoặc cho nghĩa cổ lỗ sĩ (`기분` dịch là *"sự sờ mó, sự bắt mạch"* do nguồn dữ liệu StarDict từ thời Babylone/EV-Dictionary cũ kỹ).
   - **Vấn đề tìm kiếm cụm từ ghép**: Người dùng tra cứu *"học chào hỏi rồi học văn hoá"* nhưng hệ thống chỉ tách lấy đúng chữ đầu tiên (*"học"*) để giải thích, gây ức chế và làm mất giá trị từ điển.
   - **Vấn đề AI Local Model**: Người dùng import model Qwen 0.6B (Tầng 1) và Qwen 4B (Tầng 2) nhưng thời gian phản hồi quá nhanh (~2 giây) trong khi thực tế model 4B local trên mobile phải mất >30 giây. Điều này hé lộ model local chưa thực sự chạy suy luận cục bộ mà đang fallback sang heuristic giả định.

6. **Bước ngoặt: Chuyển đổi sang HanViet EtymoAI (Idea 2.2)**:
   - Người dùng cung cấp tài liệu chi tiết `viet-kor-dict-idea2.2.md` (`Idea 2.2`).
   - Quyết định: Ngừng chắp vá (patching) trên nền tảng V1. Tách riêng thành một Repository hoàn toàn mới (`HanViet-EtymoAI`), áp dụng quy chuẩn kỹ thuật nghiêm ngặt (GSD, Matt Pocock, TDD, Dual-Database, Morphology Engine).

---

## 2. Các vấn đề cốt lõi đã được giải quyết & Kiểm chứng ở V1

Trước khi đóng băng V1, các bản vá kiểm chứng (spikes) đã được triển khai thành công:

| Vấn đề | Hiện tượng ở V1 | Giải pháp kỹ thuật đã áp dụng |
| :--- | :--- | :--- |
| **Nghĩa từ cổ hủ & Thiếu từ** | `기분`, `심정`, `감정`, `마음` bị thiếu hoặc dịch sai lệch | Trực tiếp cập nhật SQLite `korvi_dictionary.db` với nghĩa hiện đại, thêm unaccented compound indices trong cột `pronunciation`. |
| **Tìm kiếm tiếng Việt không dấu** | Tra `"tam trang"`, `"gibun"`, `"ㄱㅂ"` không ra từ | Chuẩn hóa Unicode NFC, tích hợp tìm kiếm tiền tố Chosung (`ㄱㅂ`), unaccented Vietnamese search. |
| **Độ trễ AI & Tính minh bạch GGUF** | Không rõ model có thật sự nạp vào RAM không | Xây dựng binary parser `GgufModelInspector.kt` đọc trực tiếp magic header `0x46554747`, tensor count, context length, architecture, và kiểm tra `/proc/meminfo`. |
| **Độ ổn định CI/CD GitHub Actions** | Lỗi Robolectric Maven Central timeout | Cache thư mục `~/.m2/repository/org/robolectric` và thêm cơ chế retry lệnh test trên GitHub Actions. |

---

## 3. Các quyết định kiến trúc chiến lược cho Repository mới

Dựa trên bản đặc tả `Idea 2.2`, repository mới `HanViet-EtymoAI` sẽ được xây dựng theo các nguyên lý bất biến:

1. **Kiến trúc tách rời hai cơ sở dữ liệu (Dual-Database Architecture)**:
   - `dictionary_core.db`: Cơ sở dữ liệu SQLite chỉ đọc (Read-only), được đóng gói trong APK hoặc tải dưới dạng file nén phiên bản (`dictionary_v1.db`). Tuyệt đối không bao giờ ghi dữ liệu người dùng vào file này.
   - `user_workspace.db`: Cơ sở dữ liệu Room có thể ghi (Writable), quản lý danh sách từ lưu trữ, ghi chú cá nhân, lịch sử tra cứu, flashcard SRS (`srs_cards`), và AI cache. Việc cập nhật từ điển core không bao giờ làm mất hay ghi đè dữ liệu người dùng.
2. **Quy trình Data Pipeline / ETL chạy độc lập**:
   - Quá trình làm sạch dữ liệu, chuẩn hóa Unicode NFC, phân tích hình thái, sinh chỉ mục FTS5 phải chạy hoàn toàn trên máy tính/CI pipeline trước khi phát hành. Không bao giờ chạy ETL nặng trên thiết bị di động của người dùng.
3. **Bộ máy giải phân tích hình thái (Morphology Rule Engine)**:
   - Xây dựng engine tách hậu tố ngữ pháp (deconjugation) cho các biến thể động từ/tính từ bất quy tắc (`ㅂ`, `ㄷ`, `ㅅ`, `ㅎ`, `르`, `으`, `ㄹ`). Trả về danh sách ứng viên (candidate lemmas) có xếp hạng độ tin cậy.
4. **Minh bạch hóa từ nguyên Hán-Hàn và Hán-Việt**:
   - Nguyên tắc *"Không bịa nguồn gốc"*: Phân loại rõ ràng nguồn gốc (`OriginType`: `SINO_KOREAN`, `NATIVE_KOREAN`, `LOANWORD`, `MIXED`, `UNKNOWN`) và cấp độ xác thực (`VerificationStatus`: `VERIFIED_PRIMARY`, `CURATED`, `INFERRED`, `AI_SUGGESTED`, `UNVERIFIED`).
5. **AI đóng vai trò hỗ trợ cục bộ (BYOK & Grounding)**:
   - AI không bao giờ tự sinh từ mới từ hư vô; AI chỉ nhận vào ngữ cảnh đã được truy xuất từ cơ sở dữ liệu có kiểm chứng và giải thích trên cơ sở đó. Người dùng có toàn quyền cấu hình API Key cá nhân (OpenAI, Gemini, DeepSeek, Claude) hoặc dùng model local.
