#!/usr/bin/env powershell
# run_customer.ps1 — Build & Install app-customer lên emulator Pixel_7
# Sử dụng: .\run_customer.ps1

$JAVA_HOME_PATH  = "C:\Program Files\Android\Android Studio\jbr"
$ADB             = "C:\Users\ADMIN\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$EMULATOR        = "C:\Users\ADMIN\AppData\Local\Android\Sdk\emulator\emulator.exe"
$AVD_NAME        = "Pixel_7"
$APK_PATH        = "app-customer\build\outputs\apk\debug\app-customer-debug.apk"
$PACKAGE         = "vn.vuavuive.customer"
$LAUNCHER        = "$PACKAGE/.ui.auth.LoginActivity"

# ── 1. Kiểm tra & khởi động emulator nếu chưa chạy ────────────────────────
$devices = & $ADB devices 2>&1 | Select-String "emulator"
if (-not $devices) {
    Write-Host "🚀 Đang khởi động emulator $AVD_NAME..." -ForegroundColor Yellow
    Start-Process -FilePath $EMULATOR -ArgumentList "-avd $AVD_NAME -no-snapshot-load" -WindowStyle Minimized
    Write-Host "⏳ Chờ emulator boot (có thể mất 30-60 giây)..."
    & $ADB wait-for-device
    Start-Sleep -Seconds 10
} else {
    Write-Host "✅ Emulator đang chạy: $devices" -ForegroundColor Green
}

# ── 2. Build APK ────────────────────────────────────────────────────────────
Write-Host "`n🔨 Building app-customer debug APK..." -ForegroundColor Cyan
$env:JAVA_HOME = $JAVA_HOME_PATH
.\gradlew :app-customer:assembleDebug --no-daemon -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build FAILED!" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Build thành công!" -ForegroundColor Green

# ── 3. Install APK ──────────────────────────────────────────────────────────
Write-Host "`n📦 Installing APK..." -ForegroundColor Cyan
& $ADB install -r $APK_PATH
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Install FAILED!" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Install thành công!" -ForegroundColor Green

# ── 4. Khởi chạy app ────────────────────────────────────────────────────────
Write-Host "`n▶️  Khởi chạy $PACKAGE..." -ForegroundColor Cyan
& $ADB shell am start -n $LAUNCHER
Write-Host "`n🎉 App đang chạy trên emulator!" -ForegroundColor Green
Write-Host "📡 Backend URL (debug): http://10.0.2.2:3000" -ForegroundColor Yellow
Write-Host "   → Đảm bảo backend đang chạy trên máy host port 3000`n"

# ── 5. Theo dõi log (Ctrl+C để thoát) ──────────────────────────────────────
Write-Host "📋 Logcat (Ctrl+C để dừng):" -ForegroundColor Magenta
& $ADB logcat -s "VuaVuiVe" "*:E" "AndroidRuntime:E" 2>&1
