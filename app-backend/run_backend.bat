@echo off
title VuaVuiVe Backend Server
echo =======================================================
echo    KHOI DONG VUAVUIVE BACKEND - PORT FORWARD
echo =======================================================

rem =======================================================
rem    KHOI DONG REVERSE PORT 3000
rem =======================================================
set "SDK_DIR=%LOCALAPPDATA%\Android\Sdk"
set "ADB_PATH=%SDK_DIR%\platform-tools\adb.exe"

if exist "%ADB_PATH%" (
    echo [+] Kiem tra thiet bi dang ket noi...
    "%ADB_PATH%" devices | findstr /c:"device" | findstr /v "attached" >nul
    if %errorlevel% neq 0 (
        echo [+] Khong phat hien thiet bi/may ao nao dang ket noi.
        echo [+] Vui long ket noi thiet bi hoac bat may ao de tu dong reverse port 3000.
    ) else (
        echo [+] Da phat hien thiet bi/may ao dang chay san.
    )
    echo [+] Dang kich hoat dung luong tu dong reverse port 3000...
    start /b cmd /c "echo [adb] Dang cho thiet bi... && %ADB_PATH% wait-for-device && %ADB_PATH% reverse tcp:3000 tcp:3000 && echo [+][adb] Da reverse port 3000 thanh cong!"
) else (
    echo [!] Khong tim thay adb.exe tai %ADB_PATH%, bo qua phan reverse port.
)
echo.

rem =======================================================
rem    KHOI DONG BACKEND SPRING BOOT
rem =======================================================
if "%JAVA_HOME%"=="" set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "PATH=%JAVA_HOME%\bin;%~dp0apache-maven-3.9.6\bin;%PATH%"

echo [+] Kiem tra Java:
java -version
echo.

rem MoMo sandbox test keys. Keep these in backend only, never Android.
set MOMO_PARTNER_CODE=MOMO
set MOMO_ACCESS_KEY=F8BBA842ECF85
set MOMO_SECRET_KEY=K951B6PE1waDMi640xX08PD3vg6EkVlz
set MOMO_ENDPOINT=https://test-payment.momo.vn/v2/gateway/api/create
set MOMO_REQUEST_TYPE=captureWallet
set MOMO_LANG=vi

rem Set PUBLIC_BASE_URL to your current ngrok URL for real sandbox IPN.
rem Example: set PUBLIC_BASE_URL=https://abc-123.ngrok-free.app
if "%PUBLIC_BASE_URL%"=="" (
  for /f "usebackq delims=" %%u in (`powershell -NoProfile -Command "try { $t = Invoke-RestMethod http://127.0.0.1:4040/api/tunnels; $t.tunnels | Where-Object proto -eq 'https' | Select-Object -First 1 -ExpandProperty public_url } catch { '' }"`) do set PUBLIC_BASE_URL=%%u
)

if "%PUBLIC_BASE_URL%"=="" (
  set MOCK_MOMO_MODE=true
  set MOMO_REDIRECT_URL=http://10.0.2.2:3000/api/payments/momo/return
  set MOMO_IPN_URL=http://10.0.2.2:3000/api/payments/momo/ipn
) else (
  set MOCK_MOMO_MODE=false
  set MOMO_REDIRECT_URL=%PUBLIC_BASE_URL%/api/payments/momo/return
  set MOMO_IPN_URL=%PUBLIC_BASE_URL%/api/payments/momo/ipn
)

echo Public base URL (ngrok): %PUBLIC_BASE_URL%
echo MoMo mock mode: %MOCK_MOMO_MODE%

call mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [!] LOI: May ban chua cài Maven.
    echo -------------------------------------------------------
    echo Vui long mo Terminal trong Android Studio va chay:
    echo winget install Apache.Maven
    echo -------------------------------------------------------
    pause
    exit /b
)

echo [+] Dang khoi dong Spring Boot...
call mvn spring-boot:run
pause
