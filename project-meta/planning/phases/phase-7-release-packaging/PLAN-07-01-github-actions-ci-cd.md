---
phase: 7
plan: 1
wave: 1
depends_on: ["1.1", "1.2", "2.1", "4.1"]
files_modified:
  - ".github/workflows/build-apk.yml"
  - ".github/workflows/test-and-lint.yml"
autonomous: true
user_setup:
  - service: github_secrets
    why: "Ký số tự động Android APK Release"
    env_vars:
      - name: ANDROID_KEYSTORE_BASE64
        source: "Tạo từ lệnh keytool và chuyển thành base64"
      - name: KEYSTORE_PASSWORD
        source: "Mật khẩu keystore"
      - name: KEY_ALIAS
        source: "Alias key"
      - name: KEY_PASSWORD
        source: "Mật khẩu key"

must_haves:
  truths:
    - "GitHub Actions workflow tự động chạy Unit Tests và Robolectric Tests trên runner Ubuntu."
    - "Tự động build và ký số APK release (HanVietEtymoAI.apk) khi push tag hoặc commit lên nhánh main."
    - "Áp dụng cơ chế cache Maven/Gradle và retry kiểm thử để tránh lỗi mạng flakey."
  artifacts:
    - ".github/workflows/build-apk.yml"
    - ".github/workflows/test-and-lint.yml"
---

# Plan 7.1: Tự Động Hóa Kiểm Thử & Đóng Gói APK (GitHub Actions CI/CD)

<objective>
Thiết lập toàn bộ quy trình tích hợp và chuyển giao liên tục (CI/CD) trên GitHub Actions, tự động chạy toàn bộ test suite (Unit test, Robolectric), kiểm tra cú pháp và độ an toàn bảo mật, sau đó biên dịch và ký số file APK hoàn chỉnh phát hành lên GitHub Releases.

Mục đích: Tuân thủ nguyên tắc cốt lõi: Tuyệt đối không build APK trên máy Termux di động; toàn bộ quy trình nặng được đưa lên đám mây GitHub Actions một cách tự động, minh bạch và ổn định.
Đầu ra: `.github/workflows/build-apk.yml` và `.github/workflows/test-and-lint.yml`.
</objective>

<context>
Xem xét các tài liệu:
- project-meta/history/LESSONS_LEARNED_FROM_V1.md (Mục 5. Môi trường Termux & Tự động hóa CI/CD)
- project-meta/history/CHAT_HISTORY_AND_DECISIONS.md (Lịch sử khắc phục Robolectric cache timeout ở V1)
</context>

<tasks>

<task type="auto">
  <name>Tạo workflow test-and-lint.yml kiểm thử đa ma trận</name>
  <files>.github/workflows/test-and-lint.yml</files>
  <action>
    Tạo workflow GitHub Actions:
    - Kích hoạt khi có pull request hoặc push vào nhánh `main` và nhánh phát triển.
    - Cài đặt JDK 17 (Eclipse Temurin).
    - Cấu hình Gradle Cache và Robolectric Maven Cache (`~/.m2/repository/org/robolectric`).
    - Chạy `./gradlew lintDebug testDebugUnitTest --continue`.
    - Xuất báo cáo kết quả kiểm thử dạng JUnit Test Report.
  </action>
  <verify>Kiểm tra cú pháp file yaml bằng actionlint hoặc python yaml parser.</verify>
  <done>Workflow kiểm thử tự động sẵn sàng chạy trên GitHub Actions.</done>
</task>

<task type="auto">
  <name>Tạo workflow build-apk.yml đóng gói và phát hành Release</name>
  <files>.github/workflows/build-apk.yml</files>
  <action>
    Tạo workflow build phát hành:
    - Kích hoạt thủ công qua `workflow_dispatch` hoặc tự động khi gắn tag phiên bản `v*.*.*`.
    - Giải mã keystore từ GitHub Secret `ANDROID_KEYSTORE_BASE64`.
    - Chạy `./gradlew assembleRelease`.
    - Sử dụng `rclone` hoặc GitHub CLI `gh release create` tạo bản phát hành kèm file APK đã ký chuẩn: `HanVietEtymoAI.apk`.
    - Tính toán và đính kèm mã SHA-256 Checksum công khai để người dùng xác thực tính toàn vẹn của ứng dụng.
  </action>
  <verify>Kiểm tra cú pháp yaml và logic giải mã base64 keystore.</verify>
  <done>Quy trình build APK release hoàn thiện, bảo mật và hoàn toàn tự động.</done>
</task>

</tasks>

<verification>
Sau khi hoàn thành:
- [ ] Workflow YAML hợp lệ 100% không lỗi định dạng.
- [ ] Quy trình giải mã base64 keystore và ký số tuân thủ tiêu chuẩn bảo mật Google Play / F-Droid.
</verification>

<success_criteria>
- [ ] Tất cả nhiệm vụ đều được xác thực bằng cú pháp hợp lệ.
- [ ] Các tiêu chí must-haves được đáp ứng trọn vẹn.
</success_criteria>
