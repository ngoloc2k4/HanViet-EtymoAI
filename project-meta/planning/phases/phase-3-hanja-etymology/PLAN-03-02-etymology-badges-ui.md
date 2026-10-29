---
phase: 3
plan: 2
wave: 2
depends_on: ["3.1"]
files_modified:
  - "app/src/main/java/com/hanviet/etymoai/ui/detail/EtymologyBreakdownView.kt"
  - "app/src/main/java/com/hanviet/etymoai/ui/components/EtymologyBadges.kt"
  - "app/src/main/java/com/hanviet/etymoai/ui/detail/WordDetailScreen.kt"
autonomous: true
user_setup: []

must_haves:
  truths:
    - "Hiển thị huy hiệu nguồn gốc (OriginType: SINO_KOREAN, NATIVE_KOREAN, LOANWORD) với màu sắc nhận diện trực quan."
    - "Hiển thị nhãn cấp độ xác thực (VerificationStatus: VERIFIED, INFERRED, AI_SUGGESTED) theo nguyên tắc 'Không bịa nguồn gốc'."
    - "Thẻ phân đoạn Hán tự hiển thị rõ ràng: Âm Hàn -> Chữ Hán -> Âm Hán-Việt -> Nghĩa."
  artifacts:
    - "app/src/main/java/com/hanviet/etymoai/ui/detail/EtymologyBreakdownView.kt"
    - "app/src/main/java/com/hanviet/etymoai/ui/components/EtymologyBadges.kt"
---

# Plan 3.2: Giao Diện Phân Tích Từ Nguyên & Huy Hiệu Minh Bạch (Etymology Badges UI)

<objective>
Thiết kế và xây dựng các thành phần giao diện Jetpack Compose chuyên biệt để hiển thị thẻ chiết tự Hán-Hàn-Việt trực quan, danh sách phả hệ từ vựng, và hệ thống huy hiệu phân loại nguồn gốc (`OriginType`) cùng mức độ kiểm chứng (`VerificationStatus`).

Mục đích: Giúp người dùng ngay lập tức nắm bắt nguồn gốc từ vựng mà không cảm thấy rối mắt, đồng thời đảm bảo tính minh bạch học thuật tuyệt đối.
Đầu ra: `EtymologyBreakdownView.kt`, `EtymologyBadges.kt`, và màn hình chi tiết `WordDetailScreen.kt`.
</objective>

<context>
Xem xét các tài liệu:
- project-meta/spec/GLOSSARY.md (Mục 2. Nguồn gốc ngôn ngữ & Mức độ kiểm chứng)
- project-meta/spec/ARCHITECTURE.md (Mục 1 & 2. Sơ đồ giao diện và thực thể)
</context>

<tasks>

<task type="auto">
  <name>Xây dựng hệ thống Huy hiệu Nguồn gốc và Kiểm chứng (EtymologyBadges)</name>
  <files>app/src/main/java/com/hanviet/etymoai/ui/components/EtymologyBadges.kt</files>
  <action>
    Tạo các Composable Badge bằng Jetpack Compose:
    1. `OriginTypeBadge(originType: OriginType)`:
       - `SINO_KOREAN` (Hán-Hàn): Màu chàm (Indigo), icon ký tự Hán `漢`.
       - `NATIVE_KOREAN` (Thuần Hàn): Màu xanh ngọc (Teal), icon lá cây `순`.
       - `LOANWORD` (Từ mượn/Ngoại lai): Màu hổ phách (Amber), icon quả địa cầu `외`.
       - `MIXED` (Hỗn hợp): Màu tím (Purple).
    2. `VerificationStatusBadge(status: VerificationStatus)`:
       - `VERIFIED_PRIMARY`: Huy hiệu tích xanh (Verified Icon) - *"Từ điển chuẩn quốc gia"*.
       - `CURATED`: Huy hiệu biên tập viên.
       - `INFERRED`: Nhãn cam nhạt - *"Quy tắc ngữ âm"*.
       - `AI_SUGGESTED`: Nhãn cảnh báo vàng - *"AI đề xuất (Chưa duyệt)"*.
  </action>
  <verify>Viết Compose Preview hiển thị đầy đủ các trạng thái của Badge.</verify>
  <done>Các huy hiệu hiển thị đẹp mắt, typography rõ nét, độ tương phản màu sắc đạt chuẩn WCAG AA.</done>
</task>

<task type="auto">
  <name>Xây dựng EtymologyBreakdownView phân đoạn âm tiết</name>
  <files>
    - app/src/main/java/com/hanviet/etymoai/ui/detail/EtymologyBreakdownView.kt
    - app/src/main/java/com/hanviet/etymoai/ui/detail/WordDetailScreen.kt
  </files>
  <action>
    Triển khai `EtymologyBreakdownView`:
    - Với từ gốc Hán: Hiển thị hàng thẻ ngang (Horizontal Card Row). Mỗi thẻ đại diện cho 1 âm tiết tiếng Hàn:
      - Phía trên: Âm tiết tiếng Hàn (ví dụ: `기`, `분`).
      - Ở giữa: Chữ Hán to bản rõ nét (ví dụ: `氣`, `分`).
      - Phía dưới: Âm Hán-Việt viết hoa in đậm (*KHÍ*, *PHÂN*) và nghĩa gốc tóm tắt (*khí sắc/hơi thở*, *chia phần*).
    - Hỗ trợ sự kiện click vào từng thẻ âm tiết: Mở dialog hoặc bottom sheet liệt kê ngay các từ khác có chung chữ Hán này (Etymology Family Tree).
    - Với từ thuần Hàn: Hiển thị giải thích nguồn gốc dân gian/ngữ pháp và ví dụ tiêu biểu.
  </action>
  <verify>Viết Compose UI test kiểm tra hiển thị từ `기분` chứa đầy đủ 2 thẻ chữ Hán và badge `SINO_KOREAN`.</verify>
  <done>Giao diện chiết tự từ nguyên hoàn thành, đem lại trải nghiệm học từ vựng trực quan và thẩm mỹ cao.</done>
</task>

</tasks>

<verification>
Sau khi hoàn thành:
- [ ] Giao diện chi tiết từ hiển thị phân tách từng âm tiết rõ ràng.
- [ ] Nhấp vào chữ Hán `氣` hiển thị danh sách từ vựng liên quan (`không khí`, `khí hậu`, `dũng khí`).
- [ ] Nhãn kiểm định hiển thị chính xác theo dữ liệu từ điển.
</verification>

<success_criteria>
- [ ] Tất cả nhiệm vụ đều được xác thực bằng Compose Preview / UI tests.
- [ ] Các tiêu chí must-haves được đáp ứng trọn vẹn.
</success_criteria>
