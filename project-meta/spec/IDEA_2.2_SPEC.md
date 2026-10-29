# HanViet EtymoAI — Idea 2.2
> **Tài liệu định hướng sản phẩm, kiến trúc kỹ thuật và lộ trình triển khai**  
> Phiên bản: 2.2  
> Trạng thái: Đề xuất nền tảng để bắt đầu prototype và kiểm chứng dữ liệu  
> Nền tảng mục tiêu: Android native, Kotlin, Jetpack Compose  
> Nguyên tắc: Offline-first · Lightweight · Open-source-friendly · AI optional / BYOK

---

## 1. Tóm tắt dự án

**HanViet EtymoAI** là ứng dụng từ điển Hàn–Việt chuyên sâu về nguồn gốc từ vựng tiếng Hàn, ưu tiên giải thích có căn cứ về từ Hán-Hàn, từ vay mượn, từ thuần Hàn, từ lai và biến đổi hình thái/ngữ pháp. Ứng dụng được thiết kế để tra cứu nhanh ngay cả khi không có mạng; AI là lớp bổ trợ tùy chọn, không phải thành phần bắt buộc để tra từ.

Ứng dụng không chỉ hiển thị nghĩa từ. Với mỗi mục từ phù hợp, nó có thể giúp người học hiểu:

- Từ thuộc loại nguồn gốc nào và mức độ chắc chắn của phân loại.
- Nếu là từ Hán-Hàn: Hanja nào cấu thành từ, cách đọc Hán-Hàn, âm Hán-Việt và nghĩa từng chữ trong **đúng ngữ cảnh từ đó**.
- Nếu là từ vay mượn: dạng gốc được xác minh, ngôn ngữ nguồn và quá trình thích nghi âm thanh/chính tả nếu có dữ liệu.
- Nếu là từ thuần Hàn: nghĩa, cách dùng, biến đổi và các từ liên quan; không cố gán một nguồn gốc giả.
- Nếu người dùng nhập dạng đã chia như `들었어요`, hệ thống tìm các nguyên thể khả dĩ, giải thích quy tắc và cho thấy mức độ chắc chắn.
- Lưu từ cá nhân, ghi chú, ôn tập lặp lại ngắt quãng (SRS), lịch sử tra cứu và phân tích AI có kiểm soát.

### 1.1 Mục tiêu sản phẩm

1. Tra từ Hàn–Việt nhanh, dễ dùng và có thể hoạt động offline.
2. Phân biệt rõ dữ liệu từ điển đã xác minh với suy luận hoặc giải thích do AI tạo.
3. Giải thích nguồn gốc từ dựa trên dữ liệu có thể truy nguyên nguồn.
4. Phân tích dạng từ đã biến đổi mà không mặc định chỉ có một nguyên thể.
5. Giữ ứng dụng gọn, ít phụ thuộc dịch vụ đám mây và không bắt buộc tài khoản.
6. Cho phép người dùng dùng API key riêng (BYOK) nếu muốn bật AI.
7. Xây dựng cơ sở dữ liệu và mô hình dữ liệu có thể cập nhật mà không làm mất từ vựng cá nhân.

### 1.2 Không phải mục tiêu của MVP

- Thay thế hoàn toàn từ điển chuyên ngành hoặc từ điển ngôn ngữ học lịch sử.
- Đảm bảo phân tích đúng 100% mọi dạng chia, từ lóng hoặc nguồn gốc từ.
- Tự động suy ra Hanja chỉ từ mặt chữ Hangul khi không có bằng chứng.
- Bắt buộc AI hoặc tài khoản đám mây để tra cứu cơ bản.
- Xây dựng ngay hệ sinh thái đồng bộ đa thiết bị, cộng đồng hoặc mạng xã hội.
- Tích hợp overlay luôn nổi cần quyền `SYSTEM_ALERT_WINDOW`.
- Hỗ trợ đầy đủ mọi nguồn từ điển trước khi chất lượng nguồn dữ liệu được kiểm chứng.

---

## 2. Đối tượng và tình huống sử dụng

### 2.1 Đối tượng chính

- Người Việt đang học tiếng Hàn từ sơ cấp đến trung cấp.
- Người học muốn hiểu logic từ Hán-Hàn và liên hệ với âm Hán-Việt.
- Người thường gặp từ đã chia đuôi trong câu, phụ đề, tin nhắn hoặc tài liệu.
- Người muốn xây dựng sổ từ cá nhân và ôn tập có hệ thống.
- Người dùng ưu tiên phần mềm native, offline, mã nguồn mở và hạn chế quảng cáo.

### 2.2 Luồng sử dụng chính

**A. Tra một từ đơn**
1. Nhập hoặc dán từ Hangul.
2. Chuẩn hóa Unicode và tìm chính xác.
3. Nếu không có kết quả, tìm alias/biến thể và thử phân tích hình thái.
4. Hiển thị các kết quả có thể có cùng nguồn dữ liệu, nghĩa, từ loại và mức độ chắc chắn.

**B. Hiểu một từ Hán-Hàn**
1. Mở mục từ.
2. Xem nghĩa tiếng Việt và định nghĩa tiếng Hàn nếu có.
3. Xem từng Hanja, âm Hán-Hàn, âm Hán-Việt và nghĩa chữ.
4. Xem ghi chú về cách đọc nhiều âm hoặc quy tắc đầu âm (두음법칙) khi dữ liệu có căn cứ.
5. Phân biệt rõ thông tin được trích từ nguồn với ghi chú giải thích.

**C. Tra dạng từ đã chia**
1. Nhập `들었어요`.
2. Hệ thống đưa ra danh sách ứng viên như `듣다` và `들다` nếu phù hợp với dữ liệu/rule.
3. Mỗi ứng viên đi kèm phân tích quy tắc, nghĩa và bằng chứng.
4. Người dùng chọn ứng viên đúng theo ngữ cảnh; AI có thể hỗ trợ nếu người dùng cung cấp câu.

**D. Tra nhanh từ ứng dụng khác**
1. Người dùng chọn văn bản trong ứng dụng khác.
2. Android hiển thị mục tiêu chia sẻ/chọn văn bản do `ACTION_PROCESS_TEXT` hỗ trợ.
3. HanViet EtymoAI mở màn hình tra cứu với nội dung đã chọn.
4. Không cần quyền overlay toàn hệ thống.

**E. Học và ôn từ**
1. Lưu một từ hoặc một nghĩa cụ thể.
2. Thêm ghi chú cá nhân hoặc câu ví dụ.
3. Tạo thẻ ôn tập.
4. Ôn theo lịch SRS; chỉnh sửa hoặc xóa dữ liệu cá nhân mà không ảnh hưởng từ điển lõi.

---

## 3. Nguyên tắc thiết kế không được phá vỡ

### 3.1 Offline-first

- Tra cứu từ điển, tìm kiếm cơ bản, xem dữ liệu Hanja đã đóng gói và ôn thẻ phải hoạt động offline.
- AI, cập nhật gói dữ liệu và tính năng cần nguồn ngoài mạng là tùy chọn.
- Không chặn tra cứu chỉ vì AI provider lỗi hoặc không cấu hình API key.

### 3.2 Phân tách dữ liệu và suy luận

Mọi thông tin về nguồn gốc cần có tối thiểu hai chiều độc lập:

- `OriginType`: loại nguồn gốc được cho là đúng với từ.
- `VerificationStatus`: mức độ xác minh của thông tin đó.

Ví dụ: một từ có thể được phân loại là từ Hán-Hàn (`SINO_KOREAN`) nhưng nguồn dữ liệu về Hanja của mục từ cụ thể vẫn chưa được xác minh (`UNVERIFIED`). Không được xem loại từ là bằng chứng xác minh.

AI không được phép ghi đè trực tiếp dữ liệu từ điển đã xác minh. Kết quả AI phải được lưu thành phân tích riêng, kèm nguồn đầu vào, thời điểm, model/provider và trạng thái xác minh.

### 3.3 Không bịa nguồn gốc

- Không gán Hanja cho từ chỉ dựa trên âm Hangul.
- Không gán ngôn ngữ nguồn cho từ vay mượn nếu chưa có bằng chứng.
- Không biến giải thích suy đoán thành dữ kiện từ điển.
- Khi thiếu dữ liệu, giao diện phải nói rõ “chưa có dữ liệu xác minh” hoặc “giả thuyết cần kiểm chứng”.

### 3.4 Tra cứu theo tầng

Thứ tự mặc định:

1. Chuẩn hóa Unicode và tìm khớp chính xác.
2. Tra biến thể/alias đã biết.
3. Tra dạng chia đã được tiền tính (precomputed forms).
4. Phân tích hình thái bằng quy tắc xác định (deterministic rules).
5. Tìm kiếm toàn văn/fuzzy có giới hạn.
6. Gọi AI chỉ khi người dùng yêu cầu, hoặc khi cấu hình sản phẩm cho phép và tình huống thực sự cần.

AI không được gọi ngầm cho mọi lần tra từ.

### 3.5 Dữ liệu từ điển và dữ liệu cá nhân tách biệt

- `dictionary_core.db`: dữ liệu từ điển, chỉ đọc trong runtime.
- `user_workspace.db`: từ đã lưu, ghi chú, SRS, lịch sử và cache cá nhân.
- Cập nhật từ điển không được làm mất dữ liệu cá nhân.
- Gỡ hoặc thay gói dữ liệu không được tự ý xóa sổ từ cá nhân.

### 3.6 Đặt mục tiêu hiệu năng có đo lường

Các mục tiêu độ trễ là **mục tiêu kiểm thử**, không phải cam kết trước khi benchmark:

- Tra khớp chính xác trong DB cục bộ: mục tiêu dưới 50 ms trên thiết bị mục tiêu.
- Tra cứu từ đã có alias/precomputed form: mục tiêu dưới 100 ms.
- Phân tích hình thái: mục tiêu dưới 100 ms cho trường hợp thông thường.
- Không khóa main thread; truy cập DB và xử lý chuỗi nặng chạy trên dispatcher phù hợp.
- Đo trên thiết bị Android cấp thấp thực tế, gồm cold start và DB lớn.

Không khẳng định mọi truy vấn dưới 2 ms hay đổi DB luôn hoàn tất dưới 5 ms nếu chưa có số đo thực tế.

---

## 4. Phạm vi chức năng

### 4.1 MVP bắt buộc

1. Tìm kiếm Hàn–Việt offline với DB đã đóng gói.
2. Chuẩn hóa Unicode NFC cho dữ liệu và truy vấn.
3. Tìm chính xác, tìm alias và tìm nghĩa tiếng Việt.
4. Hiển thị nghĩa, từ loại, phát âm và ví dụ khi nguồn có dữ liệu.
5. Phân loại nguồn gốc có trạng thái xác minh.
6. Hiển thị Hanja và âm Hán-Việt khi có liên kết dữ liệu được xác minh.
7. Phân tích một tập dạng chia phổ biến với nhiều ứng viên.
8. Lưu từ cá nhân, ghi chú, lịch sử tra cứu và flashcard cơ bản.
9. Cập nhật gói dữ liệu an toàn, có kiểm tra toàn vẹn.
10. Màn hình thông tin nguồn dữ liệu, giấy phép và ngày cập nhật.
11. Tích hợp Android `ACTION_PROCESS_TEXT`.
12. Xử lý lỗi rõ ràng khi dữ liệu thiếu, gói dữ liệu lỗi hoặc API AI không hoạt động.

### 4.2 Sau MVP

- AI phân tích câu theo ngữ cảnh.
- AI giải thích ngữ pháp và so sánh các ứng viên.
- Nhiều nhà cung cấp LLM, cấu hình BYOK.
- Cache phân tích AI có quản lý và xóa được.
- Import/export từ vựng cá nhân.
- Tích hợp AnkiDroid nếu API/intent và hành vi được xác minh.
- Gói dữ liệu chuyên biệt hoặc dữ liệu bổ sung tùy chọn.
- Đồng bộ đa thiết bị, chỉ khi có nhu cầu rõ ràng và thiết kế quyền riêng tư phù hợp.

### 4.3 Không triển khai sớm

- Overlay nổi toàn hệ thống.
- Tự động gọi LLM với mọi từ.
- Tự động chấp nhận mọi nguồn gốc do AI đề xuất.
- Crawler hoặc scraping nguồn dữ liệu không rõ quyền sử dụng.
- Hệ thống plugin phức tạp trước khi lõi tìm kiếm ổn định.

---

## 5. Phân loại nguồn gốc và trạng thái xác minh

### 5.1 `OriginType`

Đề xuất enum:

```kotlin
enum class OriginType {
    SINO_KOREAN,   // 한자어: từ Hán-Hàn
    LOANWORD,      // 외래어: từ vay mượn
    NATIVE_KOREAN, // 고유어: từ thuần Hàn
    MIXED,         // 혼종어: từ có thành tố thuộc nhiều nguồn
    UNKNOWN
}
```

Không nên dùng `KONGlish` làm loại bao trùm cho mọi từ vay mượn. Konglish có thể là nhãn mô tả cho một số dạng từ vay mượn/biến đổi dùng trong tiếng Hàn, không phải đồng nghĩa chính xác với toàn bộ `LOANWORD`.

### 5.2 `VerificationStatus`

```kotlin
enum class VerificationStatus {
    VERIFIED_PRIMARY,   // có nguồn đáng tin cậy và liên kết cụ thể
    VERIFIED_SECONDARY, // có nguồn thứ cấp đáng tin cậy
    CURATED,            // được biên tập thủ công, có ghi nhận người/nguồn
    INFERRED,           // suy luận theo quy tắc hoặc dữ liệu liên quan
    AI_SUGGESTED,       // đề xuất từ AI, chưa được xác minh
    UNVERIFIED,         // chưa đủ bằng chứng
    DISPUTED            // nguồn không thống nhất
}
```

Có thể lưu enum dưới dạng text để dễ đọc trong DB, nhưng cần migration rõ ràng khi đổi tên giá trị.

### 5.3 Mô hình nguồn gốc chi tiết

Một từ có thể có nhiều ghi chú nguồn gốc hoặc nhiều nguồn tham chiếu. Không ép tất cả thông tin vào một trường `origin_type` duy nhất.

Mỗi bản ghi nguồn gốc nên có:

- `stable_key`
- `origin_type`
- `verification_status`
- `source_id` và phiên bản nguồn
- `source_reference` hoặc đường dẫn/định danh mục từ trong nguồn
- `evidence_note`
- `created_at` / `updated_at`
- `reviewed_by` hoặc nhãn quy trình biên tập nếu phù hợp

Nguồn trích dẫn và giấy phép cần được quản lý ở cấp dataset; không giả định tất cả nguồn từ điển/Hanja có cùng giấy phép.

---

## 6. Nguồn dữ liệu, quyền sử dụng và ETL

### 6.1 Nguyên tắc chọn dữ liệu

Trước khi đóng gói dữ liệu, cần kiểm chứng từng nguồn:

- Nội dung thực sự có ngôn ngữ nào: định nghĩa tiếng Hàn, nghĩa tiếng Việt, ví dụ, từ loại, phát âm, Hanja, nguồn gốc.
- Giấy phép áp dụng cho dữ liệu, schema, ảnh/âm thanh và các thành phần đi kèm.
- Có yêu cầu ghi công, chia sẻ tương tự, cung cấp bản sửa đổi hoặc công bố source hay không.
- Có được phép phân phối bản SQLite đã biến đổi và bản nén trong ứng dụng hay không.
- Có giới hạn API, điều khoản tải hàng loạt hoặc điều kiện sử dụng thương mại hay không.

**Không coi các con số về số lượng mục từ, mức độ bao phủ tiếng Việt hay giấy phép là sự thật cho đến khi kiểm tra tài liệu chính thức của nguồn tại thời điểm triển khai.**

### 6.2 Pipeline ETL đề xuất

Không chạy ETL toàn bộ dữ liệu lần đầu trên điện thoại người dùng. ETL được thực hiện ở môi trường build/CI hoặc máy phát triển; app nhận gói dữ liệu đã chuẩn hóa.

```text
Nguồn dữ liệu gốc
    ↓
Kiểm tra giấy phép + manifest nguồn
    ↓
Tải / giải nén / parse
    ↓
Chuẩn hóa Unicode NFC
    ↓
Chuẩn hóa schema và mã hóa
    ↓
Lọc trường không cần thiết
    ↓
Tạo stable keys + alias
    ↓
Liên kết Hanja / âm Hán-Việt có căn cứ
    ↓
Tạo dạng chia tiền tính trong phạm vi cho phép
    ↓
Tạo index B-tree / FTS
    ↓
Chạy kiểm thử dữ liệu và integrity_check
    ↓
Đóng gói dictionary_core.db + manifest + checksum
    ↓
Nén gói phát hành
```

### 6.3 Định dạng gói dữ liệu

Mỗi bản phát hành nên gồm:

- `dictionary_core.db`: SQLite đã xây dựng sẵn.
- `manifest.json`: phiên bản schema, phiên bản dữ liệu, ngày tạo, nguồn và giấy phép.
- `SHA-256` hoặc checksum tương đương cho DB và tệp manifest.
- Tùy chọn: tệp nén `.gz` hoặc `.zst`.

**Lựa chọn nén cần dựa trên đo đạc.** Gzip có hỗ trợ nền tảng thuận tiện hơn; Zstandard có thể cho tỉ lệ/tốc độ tốt nhưng cần chọn thư viện Android, kiểm tra kích thước APK/AAB, native ABI và bảo trì. Không chọn `.zst` chỉ vì giả định nó luôn tốt hơn.

### 6.4 Quy tắc ETL

- Mọi chuỗi Hangul và dữ liệu tìm kiếm được chuẩn hóa NFC ở cả thời điểm nhập dữ liệu lẫn truy vấn.
- Giữ lại văn bản gốc khi cần truy nguyên; có thể lưu thêm trường normalized riêng.
- Không xóa thông tin nguồn hoặc ID nguồn.
- Không suy đoán Hanja cho từ không có liên kết đáng tin cậy.
- Phân biệt `null`/không có dữ liệu với chuỗi rỗng.
- Dữ liệu lỗi phải được ghi báo cáo; không âm thầm bỏ qua hàng loạt.
- Pipeline có thể chạy lại cho kết quả giống nhau (deterministic/reproducible) với cùng nguồn và phiên bản ETL.
- Tạo báo cáo thống kê: số mục từ, số nghĩa, số alias, số liên kết Hanja, số bản ghi lỗi, số trùng khóa.
- Lưu manifest nguồn và license notices trong repository.

---

## 7. Kiến trúc ứng dụng

### 7.1 Kiến trúc logic

```text
┌────────────────────────────────────────────────┐
│ Presentation: Jetpack Compose                  │
│ Search · Detail · Hanja · Saved · Review · AI  │
└───────────────────────┬────────────────────────┘
                        │
┌───────────────────────▼────────────────────────┐
│ Domain / Use Cases                             │
│ Search · Analyze Form · Save Word · Review SRS │
│ Resolve Origin · Manage Data Pack · AI Analyze │
└─────────────┬─────────────────────┬────────────┘
              │                     │
┌─────────────▼──────────┐  ┌───────▼────────────┐
│ Local Data Layer       │  │ Optional AI Layer │
│ Read-only dictionary   │  │ Provider interface│
│ Writable user DB       │  │ Grounding/validate│
│ Search/index adapters  │  │ Timeout/cache     │
└─────────────┬──────────┘  └────────────────────┘
              │
┌─────────────▼──────────────────────────────────┐
│ Data Pack Manager                              │
│ Download · Verify · Stage · Activate · Rollback│
└────────────────────────────────────────────────┘
```

### 7.2 Stack đề xuất

- **Language:** Kotlin.
- **UI:** Jetpack Compose.
- **App architecture:** ViewModel + StateFlow, repository/use-case layer.
- **Concurrency:** Kotlin Coroutines.
- **Dictionary DB:** SQLite read-only, được build sẵn; có thể dùng adapter SQLite trực tiếp nếu giúp kiểm soát lifecycle và mở DB rõ ràng hơn.
- **User DB:** Room, migrations và DAO.
- **Search:** SQLite B-tree, alias table, bảng thành tố và FTS4/FTS5 tùy khả năng runtime/đóng gói đã kiểm tra.
- **Preferences:** DataStore.
- **DI:** Có thể bắt đầu với constructor injection thủ công; thêm framework khi độ phức tạp thực sự yêu cầu.
- **Tests:** JUnit, instrumented tests, test fixture dữ liệu, benchmark Android.
- **Min Android:** mục tiêu Android 7 (API 24) theo yêu cầu dự án; phải xác minh từng thư viện và tính năng với minSdk.

Không cần thêm nhiều thư viện cho MVP. Ưu tiên dependency ít, được duy trì và có license tương thích.

### 7.3 Module/package gợi ý

```text
app/
core/
  model/
  database/
  search/
  normalization/
  morphology/
  data-pack/
feature/
  search/
  word-detail/
  hanja/
  saved-vocabulary/
  review/
  settings/
  ai-analysis/       # tùy chọn, có thể triển khai sau
```

Có thể bắt đầu dưới dạng một module Android duy nhất với package phân lớp. Chỉ tách Gradle modules khi ranh giới code đã rõ và lợi ích build/test đủ lớn.

---

## 8. Thiết kế cơ sở dữ liệu

### 8.1 Tách hai database

#### `dictionary_core.db`

- Chỉ đọc trong runtime.
- Được tạo ở pipeline ETL.
- Có phiên bản schema và phiên bản dữ liệu riêng.
- Được thay bằng gói phiên bản mới; không ghi lịch sử hoặc dữ liệu người dùng vào đây.

#### `user_workspace.db`

- Đọc/ghi qua Room.
- Chứa từ đã lưu, ghi chú, thẻ SRS, lịch sử, tùy chọn người dùng và cache AI.
- Có migration và chính sách sao lưu riêng.
- Xóa cache không xóa dữ liệu từ vựng cá nhân.

### 8.2 Schema từ điển lõi

Schema dưới đây là mô hình logic ban đầu; cần điều chỉnh sau khi kiểm tra dữ liệu nguồn thật và tạo migration/versioning cụ thể.

#### `lexemes`

| Trường | Ý nghĩa |
|---|---|
| `stable_key` TEXT PK | Khóa ổn định do pipeline tạo |
| `source_id` TEXT NULL | ID bền vững của nguồn, nếu có |
| `source_dataset` TEXT | Dataset/nhà cung cấp |
| `headword` TEXT | Dạng hiển thị gốc |
| `headword_nfc` TEXT | Dạng chuẩn hóa NFC |
| `homograph_no` TEXT NULL | Mã phân biệt đồng âm nếu nguồn cung cấp |
| `pos` TEXT NULL | Từ loại chuẩn hóa |
| `pronunciation` TEXT NULL | Phát âm |
| `level` TEXT NULL | Cấp độ, nếu có |
| `origin_type` TEXT | Nhãn loại nguồn gốc tổng quát |
| `origin_status` TEXT | Trạng thái xác minh tổng quát |
| `source_version` TEXT | Phiên bản nguồn |

`stable_key` phải được tạo theo chính sách canonicalization có phiên bản. Nếu nguồn có ID ổn định thì ưu tiên ánh xạ ID đó. Không mặc định khóa ghép `headword + homograph + pos` luôn ổn định qua mọi phiên bản.

#### `senses`

| Trường | Ý nghĩa |
|---|---|
| `sense_id` TEXT PK | ID nghĩa ổn định |
| `stable_key` TEXT FK | Liên kết mục từ |
| `sense_order` INTEGER | Thứ tự hiển thị |
| `definition_ko` TEXT NULL | Định nghĩa tiếng Hàn |
| `meaning_vi` TEXT NULL | Nghĩa tiếng Việt |
| `meaning_vi_normalized` TEXT NULL | Dạng phục vụ tìm kiếm không dấu |
| `usage_note` TEXT NULL | Ghi chú sử dụng nếu có |
| `source_reference` TEXT NULL | Vị trí trong nguồn |

ID nghĩa nên dựa trên ID nghĩa từ nguồn nếu có. Nếu không có, pipeline cần định nghĩa chính sách tạo khóa, xử lý nghĩa trùng và migration; không dựa duy nhất vào thứ tự hiển thị vì thứ tự có thể thay đổi.

#### `examples`

- `example_id` TEXT PK
- `sense_id` TEXT FK
- `sentence_ko` TEXT
- `translation_vi` TEXT NULL
- `source_reference` TEXT NULL
- `license_group` TEXT NULL

#### `aliases`

- `alias_nfc` TEXT
- `alias_type` TEXT (`SPELLING_VARIANT`, `KNOWN_FORM`, `ABBREVIATION`, `COMMON_TYPO`, ...)
- `stable_key` TEXT FK
- `sense_id` TEXT NULL
- `source` TEXT
- `verification_status` TEXT
- Khóa/index theo `alias_nfc`, loại alias và khóa mục từ.

Không đưa mọi dạng có thể chia vào alias thủ công. Alias chỉ dành cho biến thể đã biết hoặc được tạo theo chính sách xác định, có nguồn gốc truy vết.

#### `hanja_master`

- `hanja_char` TEXT PK
- `unicode_codepoint` INTEGER
- `meaning_vi` TEXT NULL
- `meaning_vi_status` TEXT
- `notes` TEXT NULL
- `source_id` TEXT NULL
- `source_version` TEXT NULL

#### `hanja_readings`

- `hanja_char` TEXT FK
- `reading_hangul` TEXT
- `reading_hanviet` TEXT NULL
- `meaning_vi` TEXT NULL
- `reading_context` TEXT NULL
- `source_id` TEXT NULL
- `verification_status` TEXT
- Khóa chính ghép theo ký tự, cách đọc và nguồn/ngữ cảnh thích hợp.

Một chữ Hanja có thể có nhiều cách đọc và nhiều nghĩa; không được lưu một âm Hán-Việt duy nhất như thể luôn đúng trong mọi từ.

#### `lexeme_hanja_segments`

- `stable_key` TEXT FK
- `segment_index` INTEGER
- `surface_hangul` TEXT
- `hanja_char` TEXT
- `reading_hangul_contextual` TEXT NULL
- `reading_hanviet_contextual` TEXT NULL
- `meaning_vi_contextual` TEXT NULL
- `dueum_applied` INTEGER
- `mapping_status` TEXT
- `source_reference` TEXT NULL

Mỗi liên kết Hanja phải thuộc một mục từ/cách dùng cụ thể và có trạng thái xác minh. Không tự suy ra mapping chỉ bằng việc tách Hangul thành âm tiết.

#### `word_origins`

- `origin_record_id` TEXT PK
- `stable_key` TEXT FK
- `origin_type` TEXT
- `source_language` TEXT NULL
- `source_form` TEXT NULL
- `adaptation_note` TEXT NULL
- `verification_status` TEXT
- `source_id` TEXT NULL
- `source_reference` TEXT NULL
- `evidence_note` TEXT NULL

#### `precomputed_forms`

- `surface_nfc` TEXT
- `stable_key` TEXT FK
- `form_rule_id` TEXT
- `form_type` TEXT
- `generation_version` TEXT
- `confidence` REAL
- Khóa/index theo `surface_nfc`; có thể thêm index theo `stable_key`.

Chỉ sinh dạng phổ biến và có giá trị tra cứu. Không tạo toàn bộ tổ hợp đuôi ngữ pháp vô hạn.

#### `search_fts`

Virtual table cho trường văn bản phù hợp: headword, nghĩa, định nghĩa và alias tùy tokenizer/schema. FTS chỉ là một lớp tìm kiếm; không thay thế exact lookup, alias hoặc bảng thành tố.

### 8.3 Schema dữ liệu cá nhân

#### `saved_vocabulary`

- `saved_id` TEXT PK
- `stable_key` TEXT NULL
- `target_sense_id` TEXT NULL
- `custom_headword` TEXT NULL
- `custom_meaning` TEXT NULL
- `personal_note` TEXT NULL
- `created_at` INTEGER
- `updated_at` INTEGER
- `is_favorite` INTEGER

Cho phép lưu ghi chú ngay cả khi một mục từ không còn tồn tại trong gói dữ liệu mới. Nếu `stable_key` không resolve được sau cập nhật, giữ bản ghi và đánh dấu mục từ chưa liên kết thay vì xóa.

#### `srs_cards`

- `card_id` TEXT PK
- `saved_id` TEXT FK NULL
- `stable_key` TEXT NULL
- `sense_id` TEXT NULL
- `front_text` TEXT
- `back_text` TEXT
- `due_at` INTEGER
- `interval_days` REAL
- `ease_or_stability` REAL NULL
- `repetitions` INTEGER
- `lapses` INTEGER
- `last_review_at` INTEGER NULL
- `scheduler_version` TEXT
- `created_at` INTEGER

Bắt đầu với thuật toán đơn giản và dễ kiểm thử. Chỉ triển khai SM-2/FSRS đầy đủ khi đã xác định rõ mô hình dữ liệu, lịch sử review và cách migration scheduler.

#### `search_history`

- `history_id` TEXT PK
- `query_original` TEXT
- `query_nfc` TEXT
- `selected_stable_key` TEXT NULL
- `searched_at` INTEGER
- `search_method` TEXT

Lịch sử có thể được tắt, xóa riêng hoặc giới hạn dung lượng.

#### `ai_analysis_cache`

- `cache_key` TEXT PK
- `input_hash` TEXT
- `provider_id` TEXT
- `model_id` TEXT NULL
- `prompt_version` TEXT
- `result_json` TEXT
- `candidate_keys_json` TEXT
- `created_at` INTEGER
- `expires_at` INTEGER NULL
- `validation_status` TEXT

Không lưu API key trong bảng cache. Cache phải có chức năng xóa, giới hạn dung lượng và tùy chọn tắt.

#### `installed_packs`

- `pack_id` TEXT PK
- `version` TEXT
- `schema_version` INTEGER
- `checksum_sha256` TEXT
- `installed_at` INTEGER
- `source_manifest` TEXT
- `status` TEXT

Bảng này thuộc vùng quản lý ứng dụng hoặc workspace DB; không nên là nguồn duy nhất để quyết định file nào đang hoạt động nếu trạng thái file chưa được kiểm tra.

---

## 9. Stable keys và quản lý thay đổi dữ liệu

### 9.1 Chính sách khóa

Ưu tiên theo thứ tự:

1. ID bền vững của mục từ trong nguồn chính thức.
2. Bảng ánh xạ ID nguồn sang `stable_key` nội bộ.
3. Khóa ghép canonical có phiên bản khi nguồn không có ID phù hợp.

Khóa ghép có thể dựa trên dạng từ chuẩn hóa, từ loại chuẩn hóa, homograph marker và dataset; tuy nhiên đây là phương án dự phòng, không phải bảo đảm tuyệt đối.

### 9.2 Yêu cầu canonicalization

Pipeline cần tài liệu hóa:

- Unicode normalization.
- Quy tắc khoảng trắng, dấu câu và biến thể chính tả.
- Chuẩn hóa từ loại.
- Cách xử lý homograph.
- Cách phân biệt mục từ cùng mặt chữ nhưng khác nguồn/cách dùng.
- Quy tắc tạo ID nghĩa.
- Cách phát hiện collision.
- Phiên bản thuật toán khóa.
- Báo cáo các khóa bị thêm, xóa, đổi hoặc không ánh xạ được qua mỗi lần cập nhật.

Không tự động ghép hai mục từ chỉ vì cùng headword.

### 9.3 Migration giữa phiên bản từ điển

Khi phát hành gói mới, tạo báo cáo:

- `stable_key` được giữ nguyên.
- Mục từ mới.
- Mục từ đã bị loại khỏi nguồn.
- Mục từ có nghĩa thay đổi.
- Alias mới hoặc bị loại.
- Các mục từ cá nhân không resolve được.
- Các liên kết Hanja thay đổi trạng thái.

Người dùng không cần biết toàn bộ chi tiết, nhưng ứng dụng phải giữ dữ liệu cá nhân an toàn và có thể hiển thị mục từ đã lưu không còn trong phiên bản hiện hành.

---

## 10. Tìm kiếm

### 10.1 Pipeline tìm kiếm

```text
Query input
  ↓
Giữ query gốc + NFC normalization
  ↓
Exact headword lookup
  ↓ không có kết quả
Known alias lookup
  ↓ không có kết quả
Precomputed form lookup
  ↓ không có kết quả
Morphology analyzer
  ↓
Candidate ranking
  ↓
Meaning/FTS/fuzzy search (nếu phù hợp)
  ↓
AI contextual analysis (chỉ khi được yêu cầu/cho phép)
```

Có thể kết hợp kết quả từ nhiều tầng; không phải lúc nào cũng dừng ở tầng đầu tiên. Ví dụ dạng chia có thể khớp một mục từ chính xác đồng thời là dạng của một động từ khác; UI nên ưu tiên kết quả exact nhưng vẫn cho mở rộng sang phân tích hình thái.

### 10.2 NFC và chuỗi tiếng Hàn

- Chuẩn hóa NFC cả lúc ETL lẫn lúc truy vấn.
- Giữ query gốc để hiển thị hoặc debug.
- Có thể thêm trường normalized cho tìm kiếm không dấu tiếng Việt.
- Không coi NFC là giải pháp cho mọi vấn đề tìm kiếm; nó chỉ giải quyết tương đương Unicode ở một lớp.

### 10.3 Tìm kiếm nghĩa tiếng Việt không dấu

- Lưu `meaning_vi` gốc có dấu.
- Tạo trường phụ `meaning_vi_normalized` để tìm không dấu.
- Xác định quy tắc xử lý chữ `đ/Đ`, dấu câu, khoảng trắng và ký tự đặc biệt.
- Kết quả hiển thị luôn dùng nghĩa gốc có dấu.
- Thử nghiệm với dữ liệu tiếng Việt thật, không chỉ kiểm thử tiếng Anh.

### 10.4 FTS và fuzzy

- Dùng B-tree/index cho exact lookup và lookup theo khóa.
- Dùng alias table cho dạng biến thể đã biết.
- Dùng FTS cho tìm kiếm nội dung dài.
- Dùng bảng thành tố cho tìm theo Hanja/âm Hán-Việt nếu cần.
- Fuzzy search là fallback có giới hạn; tránh quét toàn DB trên main thread.
- Kiểm tra tokenizer thực tế trên Android và dữ liệu Hangul/tiếng Việt; không giả định FTS tự xử lý tốt mọi hậu tố tiếng Hàn.
- Nếu cần prefix/substring search, thiết kế riêng và đo kích thước index.

### 10.5 Xếp hạng kết quả

Thứ tự gợi ý:

1. Khớp chính xác headword.
2. Khớp chính xác alias.
3. Dạng chia có phân tích hình thái tin cậy.
4. Khớp tiền tố/biến thể chính tả.
5. Khớp nghĩa hoặc định nghĩa.
6. Fuzzy match.

Có thể điều chỉnh theo loại query. Không xếp hạng chỉ theo tần suất nếu chưa có dữ liệu tần suất đáng tin cậy.

---

## 11. Bộ phân tích hình thái tiếng Hàn

### 11.1 Mục tiêu và giới hạn

Bộ phân tích hình thái giúp truy ngược dạng chia phổ biến về nguyên thể. Nó không phải bộ phân tích cú pháp hoàn chỉnh và không đảm bảo giải quyết được mọi trường hợp đa nghĩa.

Ví dụ:

- `들었어요` có thể liên quan đến `듣다` (bất quy tắc ㄷ) hoặc `들다` (động từ khác, tùy ngữ cảnh).
- `걸어요` có thể liên quan đến `걷다` (ㄷ bất quy tắc) hoặc `걸다` (quy tắc).
- `불어요` có thể có phân tích khác nhau tùy từ và ngữ cảnh.
- `나아요` thường có thể là dạng của `낫다`; cần phân biệt với lỗi gõ hoặc dạng khác khi câu không đầy đủ.

Đây là ví dụ về sự nhập nhằng, không phải danh sách kết quả cố định cho mọi ngữ cảnh. Candidate phải được xác nhận qua từ điển và quy tắc.

### 11.2 Kiến trúc

1. **Precomputed forms:** dạng chia thường gặp được sinh ở pipeline build.
2. **Rule-based analyzer:** áp dụng quy tắc hữu hạn, có ID và phiên bản.
3. **Dictionary validation:** kiểm tra nguyên thể ứng viên có tồn tại và loại từ có tương thích không.
4. **Candidate ranking:** xếp hạng bằng loại khớp, độ tin cậy của rule, từ loại và dữ liệu tần suất nếu có.
5. **Context reranking:** tùy chọn; dùng câu đầy đủ để cải thiện lựa chọn.
6. **AI explanation:** lớp riêng, không thay đổi kết quả gốc nếu không có kiểm chứng.

### 11.3 Phạm vi quy tắc đầu tiên

- Động từ/tính từ cơ bản và một tập đuôi lịch sự thường gặp.
- Các nhóm bất quy tắc: `ㅂ`, `ㄷ`, `ㅅ`, `ㅎ`, `르`, `으`, `ㄹ`.
- Một số biến đổi phổ biến khác như co rút nguyên âm và dạng phủ định, nếu có thể kiểm thử.
- Các dạng thì quá khứ, hiện tại, tương lai và đuôi lịch sự thường gặp.

Không nên sinh mọi tổ hợp ngữ pháp trong MVP. Mỗi quy tắc cần:
- `rule_id`
- `rule_version`
- điều kiện áp dụng
- phép biến đổi
- điều kiện loại trừ
- ví dụ chuẩn
- test case dương và âm
- ghi chú về trường hợp nhập nhằng

### 11.4 Kết quả phân tích

Mỗi candidate nên chứa:

- `stable_key`
- `lemma`
- `pos`
- `matched_surface`
- `rule_id` hoặc `precomputed_form_id`
- `analysis_type` (`EXACT`, `PRECOMPUTED`, `RULE_INFERRED`, `AI_RERANKED`)
- `confidence` hoặc tier định tính
- `evidence`
- `warnings` nếu có nhiều cách phân tích

Không hiển thị điểm số như xác suất khoa học nếu chưa hiệu chỉnh bằng tập dữ liệu đánh giá. UI có thể dùng “khớp trực tiếp”, “phân tích theo quy tắc”, “cần ngữ cảnh”.

### 11.5 Xử lý khi không có kết quả

- Hiển thị gợi ý tra cứu gần đúng nếu có.
- Cho người dùng xem query đã chuẩn hóa.
- Không tự tạo nguyên thể bằng cách cắt đuôi rồi trình bày như sự thật.
- Có thể đề xuất phân tích AI nếu người dùng bật tính năng; phải gắn nhãn là gợi ý.

---

## 12. Hanja, âm Hán-Hàn và âm Hán-Việt

### 12.1 Mô hình dữ liệu đúng ngữ cảnh

Một ký tự Hanja có thể có nhiều cách đọc hoặc nghĩa. Ví dụ minh họa:
- `行` có thể có cách đọc như `행` hoặc `항` trong các từ khác nhau.
- `樂` có thể có các cách đọc như `락`, `악`, `요` tùy từ/cách dùng.
- Quy tắc đầu âm (두음법칙) có thể khiến dạng hiển thị ở đầu từ khác với dạng đọc trong cấu trúc khác, ví dụ một số cặp như `녀/여`, `리/이`, `력/역`.

Các ví dụ này chỉ minh họa tính đa dạng; bảng mapping thực tế phải dựa trên dữ liệu nguồn và từng mục từ.

### 12.2 Quy tắc an toàn

- Không suy luận Hanja chỉ từ Hangul.
- Không suy luận âm Hán-Việt từ âm đọc tiếng Hàn bằng quy tắc đơn giản.
- Tách dữ liệu chữ Hanja chung khỏi cách đọc/nghĩa theo từ cụ thể.
- Ghi nhận nguồn và trạng thái xác minh cho từng liên kết.
- Nếu có nhiều cách đọc, hiển thị đúng cách đọc được liên kết với mục từ đang xem.
- Nếu chưa có mapping, hiển thị “chưa có dữ liệu Hanja đã xác minh” thay vì đoán.

### 12.3 Giao diện từ Hán-Hàn

Có thể hiển thị:

- Từ Hangul.
- Hanja đầy đủ, nếu đã xác minh.
- Phân đoạn Hanja theo vị trí.
- Cách đọc Hán-Hàn theo từng thành tố.
- Âm Hán-Việt và nghĩa chữ trong ngữ cảnh.
- Nghĩa toàn từ.
- Ghi chú về biến đổi đầu âm hoặc cách đọc nhiều âm.
- Nguồn và trạng thái xác minh.

Cần tránh khiến người học hiểu rằng nghĩa từng chữ luôn cộng lại thành nghĩa hiện đại của toàn từ. Nghĩa từ vựng hóa, chuyển nghĩa hoặc nghĩa chuyên ngành có thể khác.

---

## 13. Lớp AI tùy chọn: BYOK và grounding

### 13.1 Nguyên tắc

- AI tắt mặc định hoặc chỉ bật sau khi người dùng chủ động cấu hình.
- Không cần AI key để dùng từ điển cơ bản.
- Hỗ trợ nhiều provider qua một interface chung.
- Không gắn logic AI vào player/search engine lõi.
- Không gửi lịch sử hoặc dữ liệu cá nhân ngoài thiết bị nếu không cần thiết.
- Hiển thị provider/model và trạng thái nguồn dữ liệu trong phần phân tích.
- Không tuyên bố grounding loại bỏ hoàn toàn hallucination.

### 13.2 Giao diện provider

```kotlin
interface AiProvider {
    val providerId: String

    suspend fun analyze(request: AiAnalysisRequest): AiAnalysisResponse
}
```

Request/response cần được định nghĩa bằng DTO độc lập, có schema version. Tránh để UI phụ thuộc trực tiếp vào cấu trúc JSON riêng của một provider.

### 13.3 Grounding/RAG-lite cục bộ

Trước khi gọi LLM:

1. Tìm candidate từ từ điển cục bộ.
2. Đính kèm ID ổn định, nghĩa, từ loại, dạng chia và quy tắc đã khớp.
3. Đính kèm Hanja/âm Hán-Việt chỉ khi mapping đã được xác minh.
4. Nếu người dùng cung cấp câu, gửi đúng phần văn bản cần phân tích, có thông báo phù hợp về việc dữ liệu được gửi tới provider.
5. Yêu cầu model chọn candidate trong danh sách hoặc trả về `NO_MATCH`.
6. Yêu cầu giải thích ngắn, phân biệt dữ kiện được cung cấp với suy luận.

### 13.4 Ví dụ cấu trúc request

```json
{
  "schema_version": 1,
  "task": "analyze_korean_form",
  "surface": "들었어요",
  "context": null,
  "candidates": [
    {
      "stable_key": "example:deutda",
      "lemma": "듣다",
      "pos": "VERB",
      "meaning_vi": ["nghe"],
      "match_type": "RULE_INFERRED",
      "rule_id": "KOR_D_IRREGULAR_PAST_POLITE"
    },
    {
      "stable_key": "example:deulda",
      "lemma": "들다",
      "pos": "VERB",
      "meaning_vi": ["mang/vào/đưa vào — tùy nghĩa từ điển"],
      "match_type": "RULE_INFERRED",
      "rule_id": "KOR_REGULAR_FORM_CANDIDATE"
    }
  ]
}
```

Các `stable_key` trong ví dụ là placeholder minh họa, không phải ID thực. Trong app, chỉ gửi candidate thực tế từ DB.

### 13.5 Ví dụ response chuẩn hóa

```json
{
  "schema_version": 1,
  "selected_candidate_key": "example:deutda",
  "alternative_candidate_keys": ["example:deulda"],
  "explanation_vi": "Dạng này có thể xuất phát từ ...",
  "needs_context": true,
  "unsupported_claims": []
}
```

### 13.6 Kiểm tra response

- Parse JSON bằng serializer có schema rõ ràng.
- Kiểm tra `selected_candidate_key` có thuộc danh sách candidate đã gửi.
- Kiểm tra mọi alternative ID cũng thuộc danh sách.
- Loại bỏ hoặc đánh dấu kết quả tham chiếu ID ngoài danh sách.
- Không để response AI cập nhật `dictionary_core.db`.
- Ghi `validation_status`; nếu không hợp lệ, hiển thị lỗi hoặc yêu cầu thử lại.
- Có timeout, giới hạn token/độ dài, hủy request và retry có kiểm soát.
- Không retry vô hạn; tôn trọng rate limit của provider.
- Nếu provider không hỗ trợ structured output, vẫn phải parse và validate ở phía app.
- Không ghi API key vào log, analytics, crash report hoặc cache.

### 13.7 Bảo mật API key

- Dùng cơ chế lưu trữ bảo mật phù hợp Android, ví dụ mã hóa bằng khóa được bảo vệ bởi Android Keystore.
- Không nhúng API key dùng chung trong APK.
- Không đưa key vào URL hoặc log.
- Có nút xóa key và chọn provider/model.
- Giải thích BYOK có thể phát sinh chi phí theo chính sách provider.
- Nếu dùng provider proxy do dự án vận hành, phải công bố dữ liệu nào được gửi và chi phí vận hành; không coi proxy là miễn phí mặc định.

### 13.8 Cache AI

Cache key nên bao gồm hash của:
- query đã chuẩn hóa,
- context nếu có,
- danh sách candidate và version,
- provider/model,
- prompt version,
- schema version.

Cache cần TTL hoặc giới hạn dung lượng, nút xóa và không lưu API key. Kết quả cache không phải dữ kiện đã xác minh.

---

## 14. Cập nhật gói dữ liệu an toàn

### 14.1 Mục tiêu

Cập nhật từ điển mà:
- Không để app mở một DB dở dang.
- Không xóa DB hiện hành trước khi gói mới được kiểm tra.
- Không làm mất dữ liệu người dùng.
- Có thể rollback nếu gói mới lỗi.
- Không giả định thao tác đổi tên file đơn lẻ đủ đảm bảo an toàn khi DB đang mở.

### 14.2 Quy trình đề xuất

1. Tải hoặc nhận gói vào file tạm.
2. Kiểm tra checksum/signature nếu có.
3. Kiểm tra dung lượng và manifest.
4. Giải nén vào thư mục staging.
5. Kiểm tra schema version tương thích.
6. Chạy `PRAGMA integrity_check`.
7. Xác minh các bảng/index bắt buộc.
8. Chạy smoke tests trên một số truy vấn lõi.
9. Đọc phiên bản DB và so với manifest.
10. Chỉ sau khi tất cả kiểm tra thành công mới đánh dấu gói sẵn sàng.
11. Quản lý việc đóng/mở kết nối DB tập trung; không đổi file đang được sử dụng một cách tùy tiện.
12. Chuyển sang bản mới theo cơ chế versioned-file/active-version đã thiết kế.
13. Mở DB mới và chạy kiểm tra sau khi kích hoạt.
14. Nếu lỗi, quay về bản cũ.
15. Chỉ dọn bản cũ khi chắc chắn không còn kết nối đang dùng và chính sách rollback cho phép.

### 14.3 Versioned immutable files

Đề xuất dùng tên file theo phiên bản, ví dụ:

```text
dictionary/
  dictionary_v12.db
  dictionary_v13.db
  active_pack.json
```

`active_pack.json` chỉ là gợi ý minh họa; cần ghi file nguyên tử và xác thực nội dung. Không nên dựa duy nhất vào file pointer nếu có nguy cơ hỏng hoặc cập nhật đồng thời. Có thể dùng metadata lưu trong app-private storage cùng kiểm tra tồn tại/checksum.

### 14.4 Rollback và dọn dẹp

- Giữ ít nhất một bản trước đó cho tới khi DB mới hoạt động ổn định.
- Không xóa bản cũ khi có repository/connection đang giữ handle.
- Tránh copy DB lớn trên main thread.
- Theo dõi dung lượng trống trước khi tải và giải nén.
- Có thông báo lỗi dễ hiểu và cho phép thử lại.
- Kiểm thử quá trình bị giết giữa chừng, hết dung lượng, mất mạng và checksum sai.

---

## 15. SRS và quản lý từ vựng cá nhân

### 15.1 Nguyên tắc

- Thẻ có thể liên kết tới một nghĩa cụ thể, không chỉ mục từ chung.
- Cho phép người dùng sửa mặt trước/mặt sau và thêm ghi chú.
- Tách lịch sử ôn tập khỏi dữ liệu từ điển.
- Không bắt buộc đồng bộ đám mây.
- Có export/import để người dùng không bị khóa vào ứng dụng.

### 15.2 MVP SRS

- Tạo thẻ từ mục từ hoặc nghĩa.
- Các nút đánh giá đơn giản như “Chưa nhớ”, “Khó”, “Đã nhớ”.
- Tính ngày ôn tiếp theo theo scheduler versioned.
- Hiển thị thẻ đến hạn và thống kê tối thiểu.
- Có unit tests cho ngày đến hạn, khoảng cách ôn và timezone.
- Có thể bổ sung FSRS sau khi đã có dữ liệu lịch sử đủ tốt và thiết kế migration.

### 15.3 AnkiDroid

Tích hợp sau MVP. Trước khi xây dựng, xác minh API/intent được hỗ trợ ở phiên bản mục tiêu, điều kiện cài AnkiDroid và hành vi trên Android 7+. Nếu không có tích hợp ổn định, cung cấp export định dạng phổ biến làm phương án dự phòng.

---

## 16. Giao diện và trải nghiệm người dùng

### 16.1 Màn hình chính

- Ô tìm kiếm lớn, hỗ trợ dán văn bản.
- Lịch sử gần đây và từ đã lưu tùy chọn.
- Nút xóa truy vấn.
- Chỉ báo offline/data pack nếu cần, không chiếm diện tích.
- AI là hành động riêng, không làm nút tra cứu cơ bản phụ thuộc AI.

### 16.2 Trang kết quả

Mỗi kết quả nên thể hiện:
- Headword.
- Từ loại.
- Nghĩa ngắn.
- Loại khớp: chính xác, alias, dạng chia hoặc fuzzy.
- Nhãn nguồn gốc nếu có dữ liệu.
- Trạng thái xác minh ngắn gọn.

### 16.3 Trang chi tiết từ

Các khối có thể gồm:
1. Nghĩa và từ loại.
2. Phát âm/biến thể.
3. Ví dụ.
4. Nguồn gốc từ.
5. Hanja và âm Hán-Việt.
6. Dạng chia và ngữ pháp.
7. Từ liên quan.
8. Nguồn tham khảo.
9. Nút lưu từ / tạo thẻ.
10. Nút phân tích AI tùy chọn.

Ẩn hoặc rút gọn khối không có dữ liệu; không hiển thị phần Hanja trống chỉ để lấp giao diện.

### 16.4 Hiển thị mức độ tin cậy

Dùng nhãn dễ hiểu:
- **Đã xác minh**: có nguồn cụ thể và mapping đúng mục từ.
- **Được biên tập**: được rà soát thủ công, có dấu vết.
- **Phân tích theo quy tắc**: kết quả của morphology engine.
- **Gợi ý AI**: chưa phải dữ kiện đã xác minh.
- **Chưa có dữ liệu**: không đủ căn cứ.

Tránh dùng phần trăm “độ chính xác” nếu chưa có bộ đánh giá và hiệu chỉnh.

### 16.5 Accessibility và hiệu năng

- Hỗ trợ cỡ chữ hệ thống.
- Có tương phản đủ và semantics cho screen reader.
- Danh sách lớn dùng lazy components.
- Không thực hiện truy vấn DB hoặc gọi AI trên main thread.
- Tránh animation nặng và hình nền lớn.
- Test trên thiết bị Android cấp thấp và chế độ tiết kiệm pin.

---

## 17. Android `ACTION_PROCESS_TEXT`

### 17.1 Mục tiêu

Cho phép người dùng chọn văn bản trong ứng dụng khác và gửi sang HanViet EtymoAI để tra cứu, tránh quyền overlay toàn hệ thống.

### 17.2 Hành vi dự kiến

- Nhận văn bản qua intent.
- Kiểm tra null/chuỗi rỗng và giới hạn độ dài.
- Giữ nguyên query gốc, sau đó chuẩn hóa NFC.
- Mở màn hình kết quả hoặc tìm kiếm.
- Không tự gửi nội dung sang AI.
- Không ghi văn bản được chọn vào lịch sử nếu người dùng tắt lịch sử.
- Xử lý trường hợp ứng dụng gọi intent không đúng định dạng.

Manifest và intent filter cụ thể cần được kiểm tra trên phiên bản Android mục tiêu; không dựa vào một ví dụ manifest chưa chạy thử.

---

## 18. Quyền riêng tư và bảo mật

- Từ điển offline không cần gửi truy vấn ra ngoài.
- Lịch sử tìm kiếm và ghi chú ở local theo mặc định.
- AI chỉ hoạt động sau khi người dùng cấu hình/bật.
- Trước khi gửi câu hoặc đoạn văn tới provider, thông báo phạm vi dữ liệu gửi đi.
- Không gửi danh bạ, vị trí hoặc dữ liệu không liên quan.
- API key không được log hoặc lưu dạng rõ.
- Cho phép xóa cache AI, lịch sử và toàn bộ dữ liệu cá nhân.
- Export dữ liệu phải do người dùng chủ động thực hiện.
- Không dùng analytics mặc định nếu chưa có lý do và thông báo rõ ràng.
- Cần có chính sách quyền riêng tư nếu phát hành công khai, đặc biệt khi tích hợp provider bên ngoài.

---

## 19. Kiểm thử và tiêu chí nghiệm thu

### 19.1 Data QA

- Không có `stable_key` trùng.
- Không có foreign key lỗi.
- Kiểm tra số lượng record trước/sau ETL.
- Kiểm tra mẫu mục từ theo từ loại và loại nguồn gốc.
- Kiểm tra nguồn/license cho từng dataset.
- Kiểm tra Unicode NFC và ký tự Hangul.
- Kiểm tra mapping Hanja có source/status.
- Kiểm tra không gán Hanja cho từ thiếu chứng cứ.
- `PRAGMA integrity_check` thành công.
- Báo cáo diff giữa các bản dữ liệu.

### 19.2 Search QA

Tạo test set có query:
- Hangul NFC và chuỗi tương đương Unicode.
- Từ có nhiều nghĩa.
- Đồng âm khác từ loại.
- Nghĩa tiếng Việt có dấu/không dấu.
- Alias và biến thể chính tả.
- Query rỗng hoặc rất dài.
- Không có kết quả.
- Exact match đồng thời có candidate hình thái.
- FTS/fuzzy trên dữ liệu thực.

### 19.3 Morphology QA

Tập kiểm thử cần bao gồm:
- Các nhóm bất quy tắc `ㅂ`, `ㄷ`, `ㅅ`, `ㅎ`, `르`, `으`, `ㄹ`.
- Dạng quy tắc có bề mặt giống dạng bất quy tắc.
- Các candidate đa nghĩa.
- Trường hợp thiếu ngữ cảnh.
- Từ không nằm trong từ điển.
- Lỗi gõ phổ biến.
- Các đuôi lịch sự, quá khứ và dạng thường gặp.
- Test âm để bảo đảm rule không tạo quá nhiều candidate sai.

Đánh giá bằng precision/recall trên tập kiểm thử đã gán nhãn nếu có đủ dữ liệu; không chỉ kiểm tra vài ví dụ thủ công.

### 19.4 Data-pack QA

- Checksum sai.
- DB bị hỏng.
- Schema không tương thích.
- Thiếu bảng/index bắt buộc.
- Mất mạng khi tải.
- Hết dung lượng.
- App bị kill giữa lúc cài đặt.
- Rollback sau khi mở DB mới thất bại.
- Không làm mất `saved_vocabulary` và `srs_cards`.

### 19.5 AI QA

- Response JSON sai schema.
- Candidate ID không thuộc request.
- Provider timeout/rate limit.
- Không có mạng.
- API key sai hoặc bị thu hồi.
- Context quá dài.
- Model trả lời không chắc chắn.
- Không có candidate phù hợp.
- Xác nhận AI không thể sửa dữ liệu từ điển lõi.
- Không có API key vẫn dùng được toàn bộ tính năng offline MVP.

### 19.6 Hiệu năng

Đo:
- Cold start.
- Kích thước APK/AAB.
- Kích thước DB và index.
- Thời gian truy vấn p50/p95.
- RAM khi tìm kiếm và mở chi tiết.
- CPU/IO trong ETL chỉ ở pipeline build.
- Thời gian tải/giải nén/cài gói.
- Hiệu năng trên thiết bị cấp thấp và dataset kích thước mục tiêu.

Không chốt ngưỡng cuối cùng cho tới khi có thiết bị tham chiếu và kích thước dataset đã kiểm chứng.

---

## 20. Lộ trình triển khai

Các ước lượng là khoảng dự kiến cho một người phát triển có thể dành thời gian tập trung; sẽ thay đổi theo chất lượng dữ liệu, kinh nghiệm Kotlin/Compose và số lỗi tìm thấy.

### Phase 0 — Data & feasibility spike (khoảng 4–6 ngày)

**Mục tiêu:** kiểm chứng dữ liệu trước khi đầu tư UI lớn.

- Chọn một số nguồn dữ liệu khả thi.
- Đọc điều khoản và giấy phép.
- Tải mẫu nhỏ và xác định schema thực tế.
- Kiểm tra mức độ có nghĩa tiếng Việt, Hanja, ví dụ và nguồn gốc.
- Viết ETL prototype.
- Chuẩn hóa NFC.
- Tạo DB nhỏ, index và manifest.
- Thử truy vấn exact, FTS và không dấu tiếng Việt.
- Tạo ít nhất 200 test case dữ liệu/rule cơ bản nếu khả thi.

**Đầu ra:** báo cáo coverage/license, DB mẫu, script ETL và danh sách rủi ro.  
**Điều kiện qua phase:** xác định được nguồn dữ liệu có thể phân phối hợp pháp và đáp ứng phạm vi tối thiểu. Nếu không, thu hẹp scope hoặc tìm nguồn thay thế.

### Phase 1 — Search core (khoảng 1–2 tuần)

- Xây dựng model và repository.
- Tích hợp DB read-only.
- Exact lookup, alias và tìm nghĩa.
- UI tìm kiếm và chi tiết tối thiểu.
- NFC normalization.
- Unit/instrumented tests.
- Benchmark trên thiết bị mục tiêu.

**Đầu ra:** bản offline dictionary có thể sử dụng.

### Phase 2 — Stable IDs & data-pack manager (khoảng 1 tuần)

- Hoàn thiện stable-key policy.
- Manifest, checksum và versioning.
- Staging/validation/activation/rollback.
- Migration mapping report.
- Kiểm thử lỗi cập nhật và giữ dữ liệu cá nhân.

**Đầu ra:** cập nhật từ điển không làm hỏng dữ liệu hoặc mất sổ từ.

### Phase 3 — Hanja & morphology (khoảng 1–2 tuần hoặc hơn)

- Bảng Hanja và readings theo nguồn.
- Liên kết Hanja theo mục từ.
- Bộ quy tắc hình thái cơ bản.
- Candidate ranking và nhãn độ tin cậy.
- Test set có trường hợp nhập nhằng.
- UI giải thích dạng chia và Hanja.

**Đầu ra:** giải thích có căn cứ, không chỉ trả một kết quả phỏng đoán.

### Phase 4 — Personal vocabulary & SRS (khoảng 1 tuần)

- Room workspace DB.
- Lưu từ, ghi chú và lịch sử.
- Flashcards và lịch ôn cơ bản.
- Export/import nếu nằm trong MVP.
- Migration tests.

**Đầu ra:** vòng lặp học từ cá nhân hoàn chỉnh.

### Phase 5 — Android share intent (khoảng 2–4 ngày)

- `ACTION_PROCESS_TEXT`.
- Xử lý văn bản được chọn.
- Kiểm thử trên nhiều ứng dụng.
- Không tự gửi query tới AI.

**Đầu ra:** tra từ từ ứng dụng khác mà không cần overlay.

### Phase 6 — AI BYOK (khoảng 1–2 tuần)

- Provider interface.
- Cài đặt API key bảo mật.
- Grounding từ DB.
- Structured response và validation.
- Timeout, quota, cache, xóa cache.
- UI gắn nhãn gợi ý AI.
- Kiểm thử lỗi provider và quyền riêng tư.

**Đầu ra:** AI là lớp bổ trợ có kiểm soát, không phải dependency của từ điển.

### Phase 7 — Hardening & release

- Kiểm thử regression.
- Kiểm tra license notices và attribution.
- Accessibility.
- Kiểm tra kích thước và hiệu năng.
- Crash/error handling.
- Chính sách quyền riêng tư và tài liệu dự án.
- CI tạo artifact dữ liệu tái lập được.

---

## 21. Cấu trúc repository đề xuất

```text
hanviet-etymoai/
├── app/
├── core/
│   ├── model/
│   ├── normalization/
│   ├── search/
│   ├── morphology/
│   ├── database/
│   ├── data-pack/
│   └── ai/
├── feature/
│   ├── search/
│   ├── word-detail/
│   ├── hanja/
│   ├── vocabulary/
│   ├── review/
│   └── settings/
├── data-pipeline/
│   ├── README.md
│   ├── source-manifests/
│   ├── etl/
│   ├── schema/
│   ├── validation/
│   └── tests/
├── docs/
│   ├── IDEA.md
│   ├── DATA_SOURCES_AND_LICENSES.md
│   ├── DATABASE_SCHEMA.md
│   ├── MORPHOLOGY_RULES.md
│   ├── AI_PRIVACY.md
│   └── RELEASE_PROCESS.md
├── scripts/
├── LICENSE
├── NOTICE
└── README.md
```

Không cần tạo tất cả module/thư mục ngay ngày đầu. Đây là cấu trúc đích để giữ ranh giới logic rõ ràng.

---

## 22. Các quyết định cần chốt trước khi code production

1. **Nguồn từ điển chính:** xác nhận schema, coverage tiếng Việt, ID ổn định và quyền phân phối.
2. **Nguồn Hanja/Hán-Việt:** xác định dữ liệu nào có thể dùng, giấy phép và cách liên kết.
3. **Mức độ offline:** chọn dung lượng DB mục tiêu và phần dữ liệu được đóng gói.
4. **Stable keys:** xác định canonicalization, homograph, sense ID và migration.
5. **Search engine:** benchmark FTS4/FTS5 và chiến lược tokenizer trên minSdk.
6. **Morphology:** chọn phạm vi rule MVP và tạo bộ test gán nhãn.
7. **Data-pack update:** xác định cơ chế active version, rollback và cleanup.
8. **AI provider:** chọn provider ban đầu hoặc giữ interface mà chưa bật provider mặc định.
9. **SRS scheduler:** bắt đầu đơn giản hay dùng FSRS; phải xác định schema và migration.
10. **Phân phối:** GitHub Releases, Play Store hoặc cách phát hành khác; ảnh hưởng tới cập nhật dữ liệu và license notices.

---

## 23. Rủi ro chính và cách giảm thiểu

| Rủi ro | Tác động | Giảm thiểu |
|---|---|---|
| Nguồn dữ liệu không đủ tiếng Việt hoặc không được phép phân phối | Không thể đạt mục tiêu từ điển hoặc phát hành | Kiểm tra license/coverage trong Phase 0 |
| Khóa mục từ thay đổi giữa các bản | Mất liên kết từ đã lưu | Source ID mapping, canonicalization version và migration report |
| Gán Hanja sai | Dạy sai nguồn gốc từ | Chỉ dùng mapping theo mục từ, có nguồn và trạng thái |
| Morphology tạo nhiều candidate sai | Người học mất tin tưởng | Candidate list, test set, confidence tier và ngữ cảnh |
| FTS không phù hợp Hangul/tiếng Việt | Tìm kiếm kém | Benchmark tokenizer, kết hợp index chuyên biệt |
| Gói DB mới lỗi | App không tra cứu được | Staging, integrity check, versioned files và rollback |
| AI bịa hoặc chọn candidate sai | Thông tin sai lệch | Grounding, ID validation, nhãn AI và không ghi vào core DB |
| API key bị lộ | Chi phí hoặc truy cập trái phép | Keystore, không log, không nhúng key chung |
| DB/ứng dụng quá lớn | Tải chậm, tốn bộ nhớ | Lọc trường, đo dung lượng, gói dữ liệu tùy chọn nếu cần |
| Scope phình to | Chậm ra bản dùng được | MVP ưu tiên search/offline; AI và tính năng nâng cao sau |

---

## 24. Định nghĩa hoàn thành MVP

MVP chỉ được coi là đạt khi:

- [ ] Người dùng có thể cài app và tra cứu từ lõi khi offline.
- [ ] Tìm kiếm exact và nghĩa tiếng Việt hoạt động với dữ liệu mẫu đã xác minh.
- [ ] NFC được xử lý nhất quán ở ETL và runtime.
- [ ] Mục từ có stable key và có báo cáo thay đổi giữa hai bản data pack.
- [ ] Thông tin nguồn gốc luôn có trạng thái xác minh và không bị AI ghi đè.
- [ ] Hanja chỉ hiển thị khi mapping có bằng chứng.
- [ ] Morphology trả nhiều candidate khi cần, có rule/evidence và test case.
- [ ] Dữ liệu từ vựng cá nhân tồn tại qua cập nhật từ điển.
- [ ] Gói dữ liệu lỗi không thay thế bản đang hoạt động.
- [ ] Có thể dùng `ACTION_PROCESS_TEXT` để tra văn bản đã chọn.
- [ ] Không cần API key để dùng tính năng offline.
- [ ] Có trang ghi nguồn dữ liệu, giấy phép và ngày cập nhật.
- [ ] Không còn lỗi nghiêm trọng trong các bài kiểm thử cốt lõi.

---

## 25. Kết luận

HanViet EtymoAI nên được phát triển theo hướng **từ điển offline có dữ liệu đáng tin cậy trước, công cụ giải thích thông minh sau**. Phần khó nhất không phải gọi LLM mà là xây dựng dữ liệu nguồn có giấy phép rõ ràng, khóa mục từ ổn định, mapping Hanja đúng ngữ cảnh và bộ phân tích hình thái có thể kiểm thử.

Bản 2.2 chốt các ranh giới kiến trúc sau:

1. ETL chạy trước ở pipeline build; app không phải tự xử lý toàn bộ dataset lúc cài.
2. Tách database từ điển chỉ đọc khỏi database người dùng.
3. Quản lý stable key, sense ID và migration như hợp đồng dữ liệu.
4. Không suy ra Hanja chỉ từ Hangul; mapping cần nguồn và ngữ cảnh.
5. Morphology trả danh sách ứng viên có căn cứ, không hứa đúng tuyệt đối.
6. Tìm kiếm kết hợp exact, alias, morphology, FTS và fuzzy có giới hạn.
7. Data pack được kiểm tra, kích hoạt theo phiên bản và có rollback.
8. AI dùng BYOK, grounding cục bộ và validation; không phải nguồn sự thật của từ điển.
9. Bảo vệ quyền riêng tư và dữ liệu cá nhân ngay từ thiết kế.
10. Kiểm chứng dữ liệu, giấy phép và hiệu năng trước khi cam kết con số.

**Bước tiếp theo nên làm:** hoàn thành Phase 0, tạo báo cáo nguồn dữ liệu/giấy phép và một database mẫu nhỏ. Chỉ sau khi xác minh được dữ liệu thực tế mới chốt schema cuối cùng và bắt đầu xây dựng app production.
