#!/usr/bin/env bash
# ============================================================================
# setup_keystore_secrets.sh — Khởi tạo Keystore & Cấu hình GitHub Secrets
# Dành cho dự án: HanViet-EtymoAI
# ============================================================================

set -euo pipefail

KEYSTORE_PATH="hanviet-release.keystore"
KEY_ALIAS="hanviet_key"
VALIDITY_DAYS=10000

echo "======================================================================"
echo "    KHỞI TẠO KEYSTORE & CẤU HÌNH GITHUB SECRETS — HANVIET-ETYMOAI"
echo "======================================================================"

# 1. Kiểm tra công cụ cần thiết
command -v keytool >/dev/null 2>&1 || { echo "[!] Thiếu 'keytool'. Hãy cài OpenJDK: pkg install openjdk-17"; exit 1; }
command -v base64 >/dev/null 2>&1 || { echo "[!] Thiếu 'base64'. Hãy cài coreutils: pkg install coreutils"; exit 1; }

# 2. Nhập mật khẩu bí mật
echo ""
read -rsp "Nhập mật khẩu cho Keystore (Store Password): " KEYSTORE_PASS
echo ""
read -rsp "Nhập lại mật khẩu để xác nhận: " KEYSTORE_PASS_CONFIRM
echo ""

if [ "$KEYSTORE_PASS" != "$KEYSTORE_PASS_CONFIRM" ]; then
    echo "[!] Mật khẩu không khớp! Vui lòng thử lại."
    exit 1
fi

if [ -z "$KEYSTORE_PASS" ]; then
    echo "[!] Mật khẩu không được để trống."
    exit 1
fi

# 3. Tạo Keystore bằng keytool (RSA 4096-bit an toàn cao cấp)
if [ -f "$KEYSTORE_PATH" ]; then
    echo "[WARN] Đã tồn tại file $KEYSTORE_PATH."
    read -rp "Bạn có muốn ghi đè không? (y/N): " OVERWRITE
    if [[ "$OVERWRITE" =~ ^[Yy]$ ]]; then
        rm -f "$KEYSTORE_PATH"
    else
        echo "[INFO] Giữ nguyên keystore cũ."
    fi
fi

if [ ! -f "$KEYSTORE_PATH" ]; then
    echo "[*] Đang tạo Keystore mới với thuật toán RSA 4096-bit..."
    keytool -genkeypair -v \
        -keystore "$KEYSTORE_PATH" \
        -alias "$KEY_ALIAS" \
        -keyalg RSA \
        -keysize 4096 \
        -validity "$VALIDITY_DAYS" \
        -storepass "$KEYSTORE_PASS" \
        -keypass "$KEYSTORE_PASS" \
        -dname "CN=HanViet EtymoAI, OU=Mobile, O=OpenSource, L=Hanoi, ST=Hanoi, C=VN"
    echo "[✓] Đã tạo thành công Keystore tại: $KEYSTORE_PATH"
fi

# 4. Mã hóa Keystore thành Base64
KEYSTORE_BASE64=$(base64 -w 0 "$KEYSTORE_PATH" 2>/dev/null || base64 "$KEYSTORE_PATH" | tr -d '\r\n')

echo ""
echo "----------------------------------------------------------------------"
echo "DANH SÁCH 4 GITHUB SECRETS CẦN CẤU HÌNH:"
echo "----------------------------------------------------------------------"
echo "1. ANDROID_KEYSTORE_BASE64  : (Chuỗi mã hóa Base64 của file .keystore)"
echo "2. KEYSTORE_PASSWORD        : [Mật khẩu bạn vừa nhập]"
echo "3. KEY_ALIAS                : $KEY_ALIAS"
echo "4. KEY_PASSWORD             : [Mật khẩu bạn vừa nhập]"
echo "----------------------------------------------------------------------"

# 5. Tự động đẩy lên GitHub Secrets nếu đã đăng nhập GitHub CLI
if command -v gh >/dev/null 2>&1 && gh auth status >/dev/null 2>&1; then
    echo ""
    read -rp "Phát hiện GitHub CLI (gh) đã đăng nhập. Bạn có muốn tự động cấu hình 4 Secrets lên GitHub ngay không? (Y/n): " AUTO_SET
    if [[ "$AUTO_SET" =~ ^[Yy]$ ]] || [[ -z "$AUTO_SET" ]]; then
        echo "[*] Đang cập nhật Secrets lên GitHub repository..."
        echo "$KEYSTORE_BASE64" | gh secret set ANDROID_KEYSTORE_BASE64
        echo -n "$KEYSTORE_PASS" | gh secret set KEYSTORE_PASSWORD
        echo -n "$KEY_ALIAS" | gh secret set KEY_ALIAS
        echo -n "$KEYSTORE_PASS" | gh secret set KEY_PASSWORD
        echo "[✓] Đã cấu hình thành công cả 4 Secrets lên GitHub!"
        exit 0
    fi
fi

# Lưu chuỗi Base64 ra file bảo vệ quyền đọc nếu chưa đẩy tự động
OUTPUT_SECRET_FILE=".keystore_base64.txt"
echo "$KEYSTORE_BASE64" > "$OUTPUT_SECRET_FILE"
chmod 600 "$OUTPUT_SECRET_FILE"
echo "[INFO] Chuỗi Base64 đã được lưu tạm tại '$OUTPUT_SECRET_FILE' (đã khóa quyền 600)."
echo "Bạn có thể sao chép nội dung file này vào Settings -> Secrets and variables -> Actions trên GitHub."
