# Ngôn Ngữ Miền Chung (Ubiquitous Domain Language - GLOSSARY)

Tài liệu này định nghĩa chính xác và nhất quán các khái niệm nghiệp vụ trong toàn bộ dự án **HanViet-EtymoAI**. Tất cả mã nguồn, biến, lớp đối tượng, tài liệu thiết kế và giao diện người dùng phải tuân thủ nghiêm ngặt các thuật ngữ sau đây:

---

## 1. Khái niệm Từ vựng & Cấu trúc Dữ liệu

| Thuật ngữ | Tiếng Anh | Định nghĩa chuẩn |
| :--- | :--- | :--- |
| **Từ vị** | `Lexeme` | Đơn vị từ vựng cơ sở độc lập trong từ điển, có khóa định danh duy nhất không đổi (`stable_key`) và từ đầu mục chuẩn hóa Unicode NFC (`headword_nfc`). |
| **Nét nghĩa** | `Sense` | Một nét nghĩa ngữ nghĩa cụ thể của một từ vị, bao gồm định nghĩa tiếng Việt (`meaning_vi`), giải thích tiếng Hàn (`definition_ko`), từ loại (`part_of_speech`), cấp độ từ vựng (`topik_level`). Một Lexeme có thể có 1 hoặc nhiều Sense. |
| **Từ gốc / Nguyên thể** | `Lemma` | Dạng thức cơ bản chưa biến đổi ngữ pháp của một từ (ví dụ: động từ nguyên thể kết thúc bằng `-다` như `가다`, `듣다`, `춥다`). |
| **Biến thể ngữ pháp tính trước** | `PrecomputedForm` | Dạng thức đã chia đuôi ngữ pháp thông dụng được sinh sẵn trong pha ETL (ví dụ: `갔어요`, `추워`, `들으면`) để hỗ trợ tra cứu trực tiếp O(1) mà không cần tính toán runtime. |
| **Chosung (Phụ âm đầu)** | `ChosungAlias` | Chuỗi ký tự biểu diễn các phụ âm đầu của một từ tiếng Hàn (ví dụ: `ㄱㅂ` đại diện cho `기분`), hỗ trợ gõ tắt siêu tốc trên bàn phím di động. |

---

## 2. Từ nguyên & Nguồn gốc Ngôn ngữ (Etymology)

| Thuật ngữ | Khóa Enum | Định nghĩa chuẩn |
| :--- | :--- | :--- |
| **Nguồn gốc từ vị** | `OriginType` | Phân loại gốc gác ngôn ngữ của từ vị, gồm 5 giá trị bất biến: |
| - *Từ gốc Hán* | `SINO_KOREAN` | Từ có nguồn gốc Hán-Hàn (Hanja), có thể đối chiếu trực tiếp với âm Hán-Việt tương đương (ví dụ: `기분` - 氣分 - *khí phân*). |
| - *Từ thuần Hàn* | `NATIVE_KOREAN` | Từ thuần túy của ngôn ngữ Hàn Quốc, không có chữ Hán tương ứng (ví dụ: `하늘`, `구름`, `사랑`). |
| - *Từ mượn / Ngoại lai* | `LOANWORD` | Từ mượn từ các ngôn ngữ phương Tây (tiếng Anh, Pháp, v.v., ví dụ: `컴퓨터`, `버스`). |
| - *Từ hỗn hợp* | `MIXED` | Từ kết hợp giữa yếu tố Hán và thuần Hàn hoặc từ ngoại lai (ví dụ: `기차표` = `기차` (汽車: Hán) + `표` (票: Hán)). |
| - *Chưa xác định* | `UNKNOWN` | Từ chưa đủ bằng chứng ngôn ngữ học để phân loại chắc chắn. |
| **Mức độ kiểm chứng** | `VerificationStatus` | Thước đo độ tin cậy học thuật của thông tin từ nguyên ("Không bịa nguồn gốc"): |
| - *Kiểm chứng cấp 1* | `VERIFIED_PRIMARY` | Dữ liệu được xác nhận chính thức từ Từ điển Chuẩn Quốc gia Hàn Quốc (National Institute of Korean Language - NIKL) hoặc Viện Hán Nôm Việt Nam. |
| - *Biên tập viên duyệt* | `CURATED` | Được các chuyên gia ngôn ngữ học hoặc nhóm phát triển kiểm tra, xác nhận thủ công từ tài liệu uy tín. |
| - *Quy tắc suy luận* | `INFERRED` | Được sinh ra bởi thuật toán biến âm ngữ âm học có độ tin cậy cao nhưng chưa qua duyệt người. |
| - *AI đề xuất* | `AI_SUGGESTED` | Được mô hình AI dự đoán; bắt buộc phải hiển thị nhãn cảnh báo cho người dùng. |
| - *Chưa kiểm chứng* | `UNVERIFIED` | Dữ liệu thô từ cộng đồng, chưa được xác thực. |

---

## 3. Chữ Hán & Phân đoạn Hán tự

| Thuật ngữ | Tiếng Anh | Định nghĩa chuẩn |
| :--- | :--- | :--- |
| **Chữ Hán gốc** | `HanjaMaster` | Thực thể lưu thông tin của một ký tự chữ Hán duy nhất: ký tự Unicode (`hanja_char`), âm Hán Hàn (`korean_reading`), âm Hán Việt chuẩn (`vietnamese_reading`), bộ thủ (`radical`), số nét (`stroke_count`). |
| **Phân đoạn Hán tự** | `HanjaSegment` | Mối liên kết giữa một âm tiết cụ thể trong từ tiếng Hàn với một ký tự Hán tự tương ứng (vị trí `segment_index`, âm tiết tiếng Hàn, chữ Hán, âm Hán Việt và nghĩa Hán Việt). |
| **Cây phả hệ từ vựng** | `EtymologyGraph` | Đồ thị kết nối các từ vị có chung các gốc Hán tự (ví dụ: chữ `학` (學 - *Học*) kết nối tới `학생` (學生), `학교` (學校), `học tập` (學習)). |

---

## 4. Ngữ pháp & Giải phân tích hình thái (Morphology)

| Thuật ngữ | Tiếng Anh | Định nghĩa chuẩn |
| :--- | :--- | :--- |
| **Giải phân tích hình thái** | `Deconjugation` | Thuật toán phân tích một từ đã bị biến điệu ngữ pháp trở về một hoặc nhiều động từ/tính từ nguyên thể (`Lemma`) tương ứng. |
| **Động từ bất quy tắc** | `IrregularVerb` | Các động từ/tính từ tiếng Hàn có thân từ bị biến đổi khi gặp nguyên âm (gồm 7 nhóm: `ㅂ`, `ㄷ`, `ㅅ`, `ㅎ`, `르`, `으`, `ㄹ`). |
| **Ứng viên nguyên thể** | `CandidateLemma` | Đối tượng kết quả trả về từ bộ máy deconjugation, chứa: từ gốc (`lemma`), mã quy tắc ngữ pháp đã kích hoạt (`rule_id`), điểm tin cậy (`confidence_score`), và phần giải thích quy tắc (`rule_notes`). |

---

## 5. Kiến trúc Hệ thống & AI

| Thuật ngữ | Tiếng Anh | Định nghĩa chuẩn |
| :--- | :--- | :--- |
| **Cơ sở dữ liệu Lõi** | `CoreDatabase` | File SQLite chỉ đọc `dictionary_core.db`, sinh ra từ quy trình ETL máy tính, chứa toàn bộ từ điển, Hán tự, hình thái và chỉ mục FTS5. |
| **Không gian Người dùng** | `UserWorkspace` | Cơ sở dữ liệu Room `user_workspace.db`, lưu trữ dữ liệu cá nhân hóa (từ đã lưu, sổ tay, flashcard SRS, lịch sử, AI cache). |
| **Ôn tập ngắt quãng** | `SRS (Spaced Repetition)` | Thuật toán lặp lại ngắt quãng (dựa trên SuperMemo SM-2) tính toán khoảng thời gian lặp lại tối ưu cho các thẻ từ vựng (`srs_cards`). |
| **Tự cung cấp khóa** | `BYOK` | *Bring Your Own Key*: Người dùng tự nhập API Key cho các dịch vụ AI đám mây (OpenAI, Gemini, Anthropic, DeepSeek). |
| **Ngữ cảnh Neo** | `GroundingContext` | Toàn bộ dữ liệu xác thực trích xuất từ `CoreDatabase` được đưa vào câu lệnh prompt gửi cho AI nhằm loại bỏ hoàn toàn hiện tượng ảo giác (hallucination). |
