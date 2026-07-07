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
    echo [+] Dang giu adb reverse tcp:3000 cho emulator/thiet bi...
    start /b powershell -NoProfile -ExecutionPolicy Bypass -Command "$adb='%ADB_PATH%'; $parent=(Get-CimInstance Win32_Process -Filter ('ProcessId=' + $PID)).ParentProcessId; $seen=@{}; while (Get-Process -Id $parent -ErrorAction SilentlyContinue) { $devices=& $adb devices | Select-Object -Skip 1; foreach ($line in $devices) { if ($line -match '^(\S+)\s+device$') { $serial=$matches[1]; & $adb -s $serial reverse tcp:3000 tcp:3000 | Out-Null; if (-not $seen.ContainsKey($serial)) { Write-Host ('[+][adb] Da reverse localhost:3000 cho ' + $serial); $seen[$serial]=$true } } }; Start-Sleep -Seconds 2 }"
) else (
    echo [!] Khong tim thay adb.exe tai %ADB_PATH%, bo qua phan reverse port.
)
echo.

rem =======================================================
rem    KHOI DONG BACKEND SPRING BOOT
rem =======================================================
if "%JAVA_HOME%"=="" set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "PATH=%JAVA_HOME%\bin;%~dp0apache-maven-3.9.6\bin;%PATH%"

if exist "%~dp0.env" (
    echo [+] Dang nap cau hinh tu app-backend\.env...
    for /f "usebackq eol=# tokens=1,* delims==" %%A in ("%~dp0.env") do (
        if not "%%A"=="" set "%%A=%%B"
    )
)

echo [+] Kiem tra Java:
java -version
echo.

rem MoMo config. Sandbox keys should live in app-backend\.env.
if "%MOMO_PARTNER_CODE%"=="" set "MOMO_PARTNER_CODE=MOMO"
if "%MOMO_ENDPOINT%"=="" set "MOMO_ENDPOINT=https://test-payment.momo.vn/v2/gateway/api/create"
if "%MOMO_REQUEST_TYPE%"=="" set "MOMO_REQUEST_TYPE=captureWallet"
if "%MOMO_LANG%"=="" set "MOMO_LANG=vi"

rem Set PUBLIC_BASE_URL to your current ngrok URL for real sandbox IPN.
rem Example: set PUBLIC_BASE_URL=https://abc-123.ngrok-free.app
set NGROK_EXE=
if exist "%~dp0ngrok.exe" set "NGROK_EXE=%~dp0ngrok.exe"
if not defined NGROK_EXE if exist "%~dp0tools\ngrok.exe" set "NGROK_EXE=%~dp0tools\ngrok.exe"
if not defined NGROK_EXE (
  for /f "usebackq delims=" %%g in (`powershell -NoProfile -Command "$root = Join-Path $env:LOCALAPPDATA 'Microsoft\WinGet\Packages'; if (Test-Path $root) { Get-ChildItem $root -Filter ngrok.exe -Recurse -ErrorAction SilentlyContinue | Where-Object Length -gt 0 | Select-Object -First 1 -ExpandProperty FullName }"`) do if not defined NGROK_EXE set "NGROK_EXE=%%g"
)
if not defined NGROK_EXE (
  for /f "delims=" %%g in ('where ngrok.exe 2^>nul') do if not defined NGROK_EXE set "NGROK_EXE=%%g"
)

rem Auto-start ngrok for MoMo sandbox if no tunnel is already available.
if "%PUBLIC_BASE_URL%"=="" (
  powershell -NoProfile -Command "try { $t = Invoke-RestMethod http://127.0.0.1:4040/api/tunnels -TimeoutSec 2; if ($t.tunnels | Where-Object { $_.proto -eq 'https' -and $_.config.addr -match '3000' }) { exit 0 } else { exit 1 } } catch { exit 1 }" >nul 2>&1
  if errorlevel 1 (
    if defined NGROK_EXE (
      echo [+] Dang khoi dong ngrok http 3000...
      start "VuaVuiVe Ngrok" /min "%NGROK_EXE%" http 3000
      powershell -NoProfile -Command "$deadline=(Get-Date).AddSeconds(15); do { Start-Sleep -Milliseconds 500; try { $t=Invoke-RestMethod http://127.0.0.1:4040/api/tunnels -TimeoutSec 2; if ($t.tunnels | Where-Object { $_.proto -eq 'https' -and $_.config.addr -match '3000' }) { exit 0 } } catch {} } while ((Get-Date) -lt $deadline); exit 1" >nul 2>&1
    ) else (
      echo [!] Khong tim thay ngrok.exe trong PATH, app-backend\, hoac app-backend\tools\.
      echo [!] Cai ngrok va chay: ngrok config add-authtoken YOUR_TOKEN
    )
  )
)

if "%PUBLIC_BASE_URL%"=="" (
  for /f "usebackq delims=" %%u in (`powershell -NoProfile -Command "try { $t = Invoke-RestMethod http://127.0.0.1:4040/api/tunnels -TimeoutSec 3; $t.tunnels | Where-Object { $_.proto -eq 'https' -and $_.config.addr -match '3000' } | Select-Object -First 1 -ExpandProperty public_url } catch { '' }"`) do set PUBLIC_BASE_URL=%%u
)

if "%PUBLIC_BASE_URL%"=="" (
  set MOCK_MOMO_MODE=true
  set MOMO_REDIRECT_URL=http://127.0.0.1:3000/api/payments/momo/return
  set MOMO_IPN_URL=http://127.0.0.1:3000/api/payments/momo/ipn
) else (
  if "%MOMO_ACCESS_KEY%"=="" (
    set MOCK_MOMO_MODE=true
  ) else if "%MOMO_SECRET_KEY%"=="" (
    set MOCK_MOMO_MODE=true
  ) else (
    set MOCK_MOMO_MODE=false
  )
  set MOMO_REDIRECT_URL=%PUBLIC_BASE_URL%/api/payments/momo/return
  set MOMO_IPN_URL=%PUBLIC_BASE_URL%/api/payments/momo/ipn
)

if "%ZALOPAY_MOCK_MODE%"=="" (
  if "%PUBLIC_BASE_URL%"=="" (
    set ZALOPAY_MOCK_MODE=true
  ) else if "%ZALOPAY_APP_ID%"=="" (
    set ZALOPAY_MOCK_MODE=true
  ) else if "%ZALOPAY_KEY1%"=="" (
    set ZALOPAY_MOCK_MODE=true
  ) else if "%ZALOPAY_KEY2%"=="" (
    set ZALOPAY_MOCK_MODE=true
  ) else (
    set ZALOPAY_MOCK_MODE=false
  )
)

echo Public base URL (ngrok): %PUBLIC_BASE_URL%
echo MoMo mock mode: %MOCK_MOMO_MODE%
echo MoMo return URL: %MOMO_REDIRECT_URL%
echo MoMo IPN URL: %MOMO_IPN_URL%
echo ZaloPay mock mode: %ZALOPAY_MOCK_MODE%

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
