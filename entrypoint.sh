#!/bin/sh

# Nếu biến môi trường FIREBASE_CONFIG_BASE64 có tồn tại
if [ -n "$FIREBASE_CONFIG_BASE64" ]; then
    echo "Creating firebase-service-account.json from env var..."
    # Giải mã Base64 và ghi ra file
    echo "$FIREBASE_CONFIG_BASE64" | base64 -d > /app/firebase-service-account.json
fi

# Chạy ứng dụng Java
exec java -jar app.jar