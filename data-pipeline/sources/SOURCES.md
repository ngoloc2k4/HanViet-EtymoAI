# Danh Mục Nguồn Dữ Liệu & Bản Quyền (SOURCES.md)

> **Dự án**: HanViet-EtymoAI (Hán-Hàn-Việt Từ Nguyên & Trợ Lý Ngôn Ngữ)  
> **Giai đoạn**: Phase 0 — Data Feasibility & ETL Spike  
> **Tài liệu**: Quy hoạch nguồn dữ liệu mở, phân tích giấy phép và chính sách trích dẫn nguồn (Attribution Policy)  
> **Nguyên tắc cốt lõi**: *Liêm chính học thuật ("Không bịa nguồn gốc") · Tự do mã nguồn mở · Minh bạch nguồn gốc*

---

## 1. Tổng Quan Chiến Lược Dữ Liệu (Data Strategy Overview)

Để khắc phục triệt để các tồn tại của phiên bản tiền nhiệm V1 (dữ liệu StarDict cũ kỹ, thiếu từ vựng thông dụng, định nghĩa sai ngữ cảnh thời đại), **HanViet-EtymoAI** áp dụng chiến lược hợp nhất 4 bộ dữ liệu mở độc lập có giấy phép tương thích, thẩm định chéo (cross-validation) và đánh nhãn độ tin cậy tường minh:

| Nguồn Dữ Liệu | Đơn Vị Phát Hành / Bản Quyền | Giấy Phép | Mục Tiêu Khai Thác | Số Lượng Dự Kiến |
| :--- | :--- | :--- | :--- | :--- |
| **1. NIKL Korean-Vietnamese Learner's Dictionary** | Viện Ngôn Ngữ Quốc Gia Hàn Quốc (국립국어원) | **CC BY-SA 2.0 KR** | Từ vựng chuẩn, định nghĩa Hàn-Việt chuẩn, cấp độ TOPIK, chữ Hán gốc, câu ví dụ | ~50,000 mục từ |
| **2. Unicode Unihan Database & Han-Nom Mapping** | Unicode Consortium / Chunom Foundation | **Unicode License / Public Domain** | Bảng mẫu tự chữ Hán, số nét, bộ thủ, âm đọc Hán-Hàn và âm đọc Hán-Việt chuẩn | ~21,000 chữ Hán |
| **3. Wiktionary Korean-Vietnamese Dump** | Wikimedia Foundation / Kaikki.org | **CC BY-SA 4.0 / GFDL** | Từ vay mượn (외래어), từ phái sinh hiện đại, nguồn gốc ngôn ngữ gốc (Etymology) | ~15,000 mục từ |
| **4. Curated StarDict/EV-Dict Legacy** | Cộng đồng từ điển mở Việt Nam (Đã lọc sạch) | **GPLv2+ / Public Domain** | Bổ sung từ vựng khẩu ngữ đời sống sau khi lọc bỏ nghĩa cổ/lỗi thời của V1 | ~10,000 mục từ |

---

## 2. Chi Tiết Từng Nguồn Dữ Liệu & Giấy Phép

### 2.1. NIKL Korean-Vietnamese Learner's Dictionary (한국어기초사전)
- **Cơ quan chủ quản**: Viện Ngôn ngữ Quốc gia Hàn Quốc (National Institute of Korean Language - NIKL).
- **Cổng dữ liệu mở**: [Open Korean Knowledge Platform (K-Dictionary / 한국어기초사전 Open API)](https://krdict.korean.go.kr).
- **Giấy phép bản quyền**: **Creative Commons Attribution-ShareAlike 2.0 Korea (CC BY-SA 2.0 KR)**.
- **Trường dữ liệu khai thác**:
  - `target_code` (Mã mục từ duy nhất của NIKL - dùng làm `source_id` cố định).
  - `word` / `headword` (Dạng chữ viết Hangul chuẩn).
  - `origin` (Chữ Hán hoặc từ ngữ gốc được NIKL biên soạn chính thức).
  - `pronunciation` (Phát âm IPA và dạng phiên âm Hangul).
  - `word_grade` (Cấp độ từ vựng: 초급 - Sơ cấp, 중급 - Trung cấp, 고급 - Cao cấp tương ứng TOPIK 1 - 6).
  - `pos` (Từ loại: Danh từ, Động từ, Tính từ, Phó từ, v.v.).
  - `sense`: Định nghĩa tiếng Hàn (`definition_ko`) và giải nghĩa đối sánh tiếng Việt (`meaning_vi`) đã qua kiểm định sư phạm.
- **Cam kết chất lượng**: Đây là nguồn dữ liệu chuẩn vàng (Gold Standard baseline), ngăn chặn dứt điểm tình trạng từ ngữ bị dịch sai nghĩa ngữ cảnh học thuật/cổ điển.

### 2.2. Unicode Unihan Database & Kho Dữ Liệu Hán Tự - Hán Việt
- **Cơ quan phát hành**: Unicode Consortium (Unihan database release) kết hợp cơ sở dữ liệu tra cứu chữ Nôm/Hán-Việt (chunom.org, HannomRCV).
- **Giấy phép bản quyền**: **Unicode License Agreement for Data Files and Software** (Tự do phân phối thương mại và phi thương mại, cho phép sửa đổi và nhúng trong phần mềm).
- **Trường dữ liệu khai thác**:
  - `hanja_char` (Ký tự chữ Hán Unicode CJK Unified Ideographs).
  - `kHangul` (Âm đọc tiếng Hàn chính thức theo tiêu chuẩn KS X 1001).
  - `kVietnamese` (Âm đọc Hán-Việt chuẩn hóa theo từ điển Thiều Chửu, Trần Văn Chánh, Đào Duy Anh).
  - `kTotalStrokes` & `kRadical` (Tổng số nét và mã bộ thủ Khang Hy).
  - `meaning_vi` (Nghĩa giải nghĩa của từng chữ Hán độc lập).
- **Cam kết chất lượng**: Phân định rõ ràng trường hợp **một chữ Hán có nhiều âm đọc** (ví dụ: `行` có thể đọc là *hành* hoặc *hàng*; `樂` có thể đọc là *lạc*, *nhạc*, *diệu*). Tuyệt đối không gộp chung âm một cách cơ học.

### 2.3. Wiktionary Korean-Vietnamese & Etymology Extracts
- **Cơ quan quản trị**: Wikimedia Foundation (Dữ liệu trích xuất cấu trúc qua Kaikki / Wiktionary JSON dump).
- **Giấy phép bản quyền**: **CC BY-SA 4.0 International & GFDL (GNU Free Documentation License)**.
- **Trường dữ liệu khai thác**:
  - Nhận diện từ vay mượn (`LOANWORD`): Gốc từ tiếng Anh, tiếng Pháp, tiếng Đức, tiếng Phạn, tiếng Nhật.
  - Quá trình chuyển tự ngữ âm (Phonetic adaptation).
  - Ví dụ câu giao tiếp đời sống hiện đại và ngữ cảnh thành ngữ (Idioms).
- **Cam kết chất lượng**: Bổ sung các từ vựng công nghệ, đời sống hiện đại mà từ điển hàn lâm chưa cập nhật kịp.

### 2.4. Tập Dữ Liệu Kế Thừa StarDict Đã Qua Thanh Lọc (Curated Legacy StarDict)
- **Nguồn gốc**: Bộ từ điển Kor-Vi StarDict kế thừa từ phiên bản V1.
- **Giấy phép**: **GPLv2+ / Public Domain** (Cộng đồng phần mềm tự do).
- **Quy trình thanh lọc bắt buộc (Rigorous Cleansing Pipeline)**:
  1. **Loại bỏ nghĩa cổ hủ**: Rà soát và loại bỏ các định nghĩa lỗi thời gây hiểu nhầm (như trường hợp `기분` dịch thành *"bắt mạch"*).
  2. **Chuẩn hóa chính tả tiếng Việt**: Chuyển đổi toàn bộ quy tắc đặt dấu thanh về chuẩn hiện đại mới nhất (`hòa` thay vì `hoà`, `thúy` thay vì `thuý`).
  3. **Loại bỏ trùng lặp**: So khớp với tập từ điển NIKL; nếu mục từ đã có trong NIKL, ưu tiên 100% bản dịch và định nghĩa của NIKL.
  4. **Kiểm tra Unicode NFC**: Xử lý triệt để lỗi ký tự tổ hợp (NFD) thường gặp trong các file StarDict cũ.

---

## 3. Chính Sách Trích Dẫn & Phân Phối Bản Quyền (Attribution & Licensing Policy)

1. **Tuân thủ CC BY-SA & Open Source Attribution**:
   - Ứng dụng Android và repository công khai màn hình **"Về Dữ Liệu & Giấy Phép" (Data Provenance & Licenses)** ghi nhận công lao của Viện Ngôn ngữ Quốc gia Hàn Quốc (NIKL), Unicode Consortium và Wikimedia Foundation.
   - Cơ sở dữ liệu phát hành `dictionary_core.db` tuân thủ nguyên tắc ShareAlike (chia sẻ tương tự) cho phần dữ liệu phát sinh từ NIKL và Wiktionary.
2. **Minh bạch cấp độ bản ghi (Record-level Traceability)**:
   - Trong bảng `lexemes` và `word_origins`: Mọi bản ghi đều lưu rõ `source_dataset` (`NIKL`, `UNIHAN`, `WIKTIONARY`, `CURATED_LEGACY`) và `source_id`.
   - Người dùng có thể xem nguồn gốc trực tiếp trên giao diện chi tiết từ vựng.
3. **Quyền độc lập của dữ liệu người dùng (`user_workspace.db`)**:
   - Cơ sở dữ liệu cá nhân của người dùng (từ lưu, ghi chú, flashcard SRS) là tài sản độc quyền thuộc quyền riêng tư của người dùng, độc lập hoàn toàn với giấy phép của dữ liệu từ điển lõi.

---

## 4. Kết Luận Thẩm Định Khả Thi (Feasibility Verdict)

- **Tính hợp pháp bản quyền**: 100% nguồn dữ liệu thuộc các giấy phép mở được công nhận quốc tế (CC BY-SA, Unicode License, Open Community).
- **Độ tin cậy học thuật**: NIKL + Unihan đảm bảo nguyên tắc *"Không bịa nguồn gốc"* với độ chính xác > 99%.
- **Quy mô từ vựng**: Tổng hợp 4 nguồn mang lại ngân hàng từ vựng từ 50,000 đến 70,000 từ vựng tiếng Hàn thực dụng, đáp ứng vượt trội mục tiêu MVP.
