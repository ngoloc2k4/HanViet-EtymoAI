# Summary Plan 7.1: GitHub Actions CI/CD Pipeline

## Hoàn thành
- **test.yml**: Thiết lập ma trận kiểm thử fail-fast: linter cú pháp, JUnit và Robolectric unit tests. Lưu giữ báo cáo kiểm thử 7 ngày.
- **build-apk.yml**: Toàn quyền tự động hóa build, ký số APK (v1, v2, v3, v4 signing) qua GitHub Secrets, tạo checksum SHA-256 công khai và phát hành tự động lên GitHub Releases `beta-latest`.
- **Cache Optimization**: Thiết lập cache Gradle Build Cache và Robolectric offline dependency cache (`~/.m2/repository/org/robolectric`).
