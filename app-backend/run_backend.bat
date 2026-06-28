@echo off
title VuaVuiVe Backend Server
echo =======================================================
echo    KHOI DONG VUAVUIVE BACKEND
echo =======================================================

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
  for /f "usebackq delims=" %%u in (`powershell -NoProfile -Command "try { (Invoke-RestMethod http://127.0.0.1:4040/api/tunnels).tunnels | Where-Object proto -eq 'https' | Select-Object -First 1 -ExpandProperty public_url } catch { '' }"`) do set PUBLIC_BASE_URL=%%u
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
