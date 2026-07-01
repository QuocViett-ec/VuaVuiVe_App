@echo off
title Launch VuaVuiVe Android Apps
echo =======================================================
echo    DANG KHOI DONG 3 UNG DUNG DI DONG (VVV)
echo =======================================================

set "ADB_PATH=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"

if not exist "%ADB_PATH%" (
    echo [!] LOI: Khong tim thay adb.exe tai %ADB_PATH%
    pause
    exit /b
)

echo [+] Dang kiem tra ket noi thiet bi/may ao...
"%ADB_PATH%" devices | findstr /c:"device" | findstr /v "attached" >nul
if %errorlevel% neq 0 (
    echo [!] LOI: Chua co thiet bi hay may ao nao online.
    echo Vui long mo may ao truoc khi chay script nay!
    pause
    exit /b
)

echo [+] Dang bien dich va cai dat 3 ung dung len thiet bi...
call .\gradlew.bat :app-customer:installDebug :app-admin:installDebug :app-shipper:installDebug

if %errorlevel% neq 0 (
    echo [!] LOI: Build va cai dat that bai!
    pause
    exit /b
)

echo [+] Da cai dat thanh cong! Dang khoi dong cac activity...
echo [+] Khoi dong Customer App...
"%ADB_PATH%" shell am start -n vn.vuavuive.customer/vn.vuavuive.customer.ui.auth.LoginActivity
echo [+] Khoi dong Admin App...
"%ADB_PATH%" shell am start -n vn.vuavuive.admin/vn.vuavuive.admin.ui.auth.AdminLoginActivity
echo [+] Khoi dong Shipper App...
"%ADB_PATH%" shell am start -n vn.vuavuive.shipper/vn.vuavuive.shipper.ui.auth.ShipperLoginActivity

echo =======================================================
echo    DA KHOI DONG THANH CONG CA 3 UNG DUNG!
echo =======================================================
pause
