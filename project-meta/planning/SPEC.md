---
title: "HanViet-EtymoAI Project Specification"
status: "FINALIZED"
version: "2.2.0"
approved_at: "2026-10-29T11:53:00Z"
methodology: "GSD (Get Shit Done) + Matt Pocock Engineering Skills"
---

# HanViet-EtymoAI: Từ Điển Hán-Hàn-Việt Từ Nguyên & Trợ Lý Ngôn Ngữ Thông Minh

## 1. Tầm Nhìn Dự Án (Project Vision)

**HanViet-EtymoAI** là ứng dụng từ điển tiếng Hàn - tiếng Việt mã nguồn mở thế hệ mới, tiên phong khai thác sức mạnh của **từ nguyên học Hán tự (Hanja)** để giúp người Việt Nam giải mã, hiểu sâu và ghi nhớ từ vựng tiếng Hàn nhanh gấp nhiều lần so với cách học vẹt truyền thống.

### Các Nguyên Lý Bất Biến (Non-Negotiable Principles):
1. **Offline-First Tuyệt Đối**: Toàn bộ chức năng tra cứu, giải hình thái ngữ pháp (deconjugation), phân tích chiết tự Hán-Việt, và ôn tập Flashcard SRS phải hoạt động 100% trơn tru không cần kết nối mạng.
2. **Liêm Chính Học Thuật ("Không Bịa Nguồn Gốc")**: Tuyệt đối không bao giờ cho phép AI hay thuật toán tự tiện gán ghép nguồn gốc Hán tự khi không có căn cứ từ điển học. Mọi dữ liệu từ nguyên phải có nhãn xác thực tường minh (`VERIFIED_PRIMARY`, `CURATED`, `INFERRED`, `AI_SUGGESTED`, `UNVERIFIED`).
3. **Kiến Trúc Hai Cơ Sở Dữ Liệu Tách Biệt (Dual-Database)**:
   - `dictionary_core.db`: CSDL chỉ đọc (Read-only SQLite) sinh ra từ quy trình ETL trên máy tính.
   - `user_workspace.db`: CSDL có thể ghi (Room Writable) lưu trữ dữ liệu cá nhân của người học. Nâng cấp từ điển không bao giờ làm mất dữ liệu người dùng.
4. **Quy Trình ETL Ngoài Thiết Bị (Zero Heavy ETL on Client)**: Toàn bộ quá trình làm sạch dữ liệu, chuẩn hóa Unicode NFC, sinh dạng thức chia trước và lập chỉ mục FTS5 phải diễn ra trên máy phát triển hoặc CI/CD, không chạy ngầm gây nóng máy hao pin người dùng.
5. **AI Là Trợ Lý Có Neo (Grounded BYOK AI)**: AI chỉ hỗ trợ giải thích dựa trên ngữ cảnh đã được truy xuất thực tế từ từ điển (Grounding Context). Hỗ trợ mô hình BYOK (Bring Your Own Key) bảo vệ quyền riêng tư và tùy chọn model local.

---

## 2. Các Tiêu Chí Nghiệm Thu Tổng Thể (Acceptance Criteria)

### A. Tra Cứu & Dữ Liệu:
- [ ] Tra cứu tức thì (< 50ms) cho các dạng: Khớp chính xác tiếng Hàn, Chosung (`ㄱㅂ`), dạng đã chia đuôi (`갔어요`), và tiếng Việt có dấu lẫn không dấu (`"tâm trạng"` / `"tam trang"`).
- [ ] Hỗ trợ tách câu / trích xuất N-Gram thông minh (ưu tiên cụm 2-3 từ như `"chào hỏi"`, `"văn hoá"` thay vì chỉ cắt 1 chữ đầu tiên).

### B. Hình Thái Ngữ Pháp (Morphology):
- [ ] Tự động deconjugate chính xác 7 nhóm bất quy tắc (`ㅂ`, `ㄷ`, `ㅅ`, `ㅎ`, `르`, `으`, `ㄹ`).
- [ ] Trả về danh sách ứng viên (candidate lemmas) có xếp hạng độ tin cậy và giải thích quy tắc rõ ràng.

### C. Từ Nguyên & Chiết Tự Hán-Việt:
- [ ] Hiển thị phân đoạn Hán tự trực quan cho từng âm tiết: Âm Hàn $\to$ Chữ Hán $\to$ Âm Hán-Việt $\to$ Nghĩa gốc Hán tự.
- [ ] Cây phả hệ từ vựng (Etymology Graph): Bấm vào một chữ Hán (ví dụ `學` - *Học*) sẽ hiển thị tất cả từ vựng tiếng Hàn và tiếng Việt có chung gốc chữ Hán này.

### D. Không Gian Người Dùng & Ôn Tập SRS:
- [ ] Lưu từ vựng với thẻ phân loại màu sắc và ghi chú cá nhân dạng Markdown.
- [ ] Hệ thống Flashcard SRS tích hợp thuật toán SuperMemo SM-2 tự động tính toán chu kỳ ôn tập.
- [ ] Hỗ trợ xuất/nhập danh sách từ vựng dạng JSON chuẩn hóa.

### E. Tích Hợp Hệ Thống Android:
- [ ] Bôi đen chữ ở bất kỳ ứng dụng nào $\to$ Chọn "Tra Từ Điển Hán-Việt" qua `ACTION_PROCESS_TEXT` $\to$ Hiện popup/BottomSheet tra từ tức thì mà không cần cấp quyền vẽ màn hình nguy hiểm `SYSTEM_ALERT_WINDOW`.

### F. CI/CD & Đóng Gói:
- [ ] GitHub Actions tự động kiểm thử (Unit tests + Robolectric), build APK, ký số tự động và phát hành lên GitHub Releases.

---

## 3. Danh Sách Các Mục Ngoài Phạm Vi (Non-Goals)

- **Không** xây dựng mạng xã hội chat hay kết bạn trong app.
- **Không** chạy huấn luyện (training/fine-tuning) model AI trên điện thoại.
- **Không** thu thập dữ liệu tra cứu cá nhân của người dùng về máy chủ trung tâm (Privacy First).
- **Không** cố gắng nhồi nhét quy trình ETL dữ liệu thô vào thiết bị di động.
