# VuaVuiVe

VuaVuiVe là đồ án bán thực phẩm gồm 1 backend Spring Boot và 3 app Android:

- `app-backend`: REST API Spring Boot, Firebase Realtime Database, JWT, Swagger, thanh toán mock/sandbox.
- `shared`: thư viện Android dùng chung cho DTO, API client, session, formatter.
- `app-customer`: app khách hàng.
- `app-admin`: app quản trị.
- `app-shipper`: app giao hàng.

## 1. Yêu cầu môi trường

- Android Studio, Android SDK Platform 34, Android SDK Platform-Tools, Emulator.
- JDK 21 để chạy backend.
- Maven: cài Maven vào `PATH`, hoặc dùng Maven đi kèm trong `tools/apache-maven-3.9.6/bin/mvn.cmd`.

## 2. Cấu hình sau khi clone

Clone project rồi mở thư mục gốc `VuaVuiVe` bằng Android Studio để Gradle sync.

Nếu Android Studio chưa tự tạo `local.properties`, tạo file này ở thư mục gốc:

```properties
sdk.dir=C\:\\Users\\<ten-user>\\AppData\\Local\\Android\\Sdk
```

Backend cần Firebase Admin SDK key. Đặt file service account tại:

```text
app-backend/serviceAccountKey.json
```

Tạo `app-backend/.env` nếu cần override cấu hình:

```env
FIREBASE_DATABASE_URL=https://vua-vui-ve-default-rtdb.firebaseio.com
FIREBASE_CONFIG_PATH=file:./serviceAccountKey.json
APP_SEED_ENABLED=false
```

Không cần bật seed nếu Firebase đã có dữ liệu realtime dùng để chấm bài. Chỉ đổi `APP_SEED_ENABLED=true` khi dùng Firebase trống hoặc muốn tạo lại dữ liệu demo ban đầu. Nếu để bật, backend sẽ reset mật khẩu `admin@vuavuive.vn` về `Admin@123` mỗi lần khởi động, đồng thời có thể tạo thêm tài khoản/demo order nếu thiếu.

Ba file `google-services.json` cho Android đã nằm trong:

- `app-customer/google-services.json`
- `app-admin/google-services.json`
- `app-shipper/google-services.json`

Nếu đổi Firebase project thì thay 3 file này theo project mới.

## 3. Khởi động backend

Cách nhanh nhất trên Windows:

```bat
cd app-backend
run_backend.bat
```

Nếu máy chưa có Maven trong `PATH`, chạy trực tiếp bằng Maven đi kèm:

```bat
cd app-backend
..\tools\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run
```

Backend chạy ở:

```text
http://localhost:3000
```

Swagger:

```text
http://localhost:3000/swagger-ui.html
```

API public kiểm tra nhanh:

```text
GET http://localhost:3000/api/products
```

## 4. Khởi động app Android

Mặc định debug build của 3 app gọi backend qua:

```text
http://10.0.2.2:3000
```

Vì vậy cách dễ chấm nhất là dùng Android Emulator. Hãy chạy backend trước, sau đó mở emulator.

Cài và mở cả 3 app:

```bat
launch_apps.bat
```

Hoặc cài thủ công:

```bat
gradlew.bat :app-customer:installDebug :app-admin:installDebug :app-shipper:installDebug
```

Có thể build APK debug bằng:

```bat
gradlew.bat :app-customer:assembleDebug :app-admin:assembleDebug :app-shipper:assembleDebug
```

APK nằm trong:

```text
app-customer/build/outputs/apk/debug/
app-admin/build/outputs/apk/debug/
app-shipper/build/outputs/apk/debug/
```

Nếu dùng máy Android thật, cần đổi `BASE_URL` trong `build.gradle.kts` của 3 app sang IP LAN của máy chạy backend, ví dụ `http://192.168.1.10:3000`.

## 5. Tài khoản demo

Các tài khoản này được tạo khi bật seed:

| App | Email | Mật khẩu |
| --- | --- | --- |
| Customer | `customer@gmail.com` | `Customer@123` |
| Admin | `admin@vuavuive.vn` | `Admin@123` |
| Shipper | `shipper@gmail.com` | `Shipper@123` |


## 6. Lệnh test/build

Backend:

```bat
cd app-backend
..\tools\apache-maven-3.9.6\bin\mvn.cmd test
..\tools\apache-maven-3.9.6\bin\mvn.cmd clean package
```

Android/shared:

```bat
gradlew.bat :shared:testDebugUnitTest
gradlew.bat :app-customer:assembleDebug :app-admin:assembleDebug :app-shipper:assembleDebug
```

## 7. Flow test nhanh

### Smoke test

1. Chạy backend.
2. Mở `http://localhost:3000/swagger-ui.html`.
3. Gọi `GET /api/products`, kiểm tra có dữ liệu hoặc response hợp lệ.
4. Mở emulator và chạy 3 app.

### Customer

1. Đăng nhập `customer@gmail.com` / `Customer@123`.
2. Xem danh sách sản phẩm, tìm kiếm/lọc category.
3. Mở chi tiết sản phẩm, tăng/giảm số lượng, thêm vào giỏ.
4. Vào giỏ hàng, chỉnh số lượng, checkout.
5. Chọn COD, đặt hàng, kiểm tra đơn mới trong Orders.
6. Thử MoMo/ZaloPay; mặc định backend đang dùng mock mode nếu chưa cấu hình key sandbox.

### Admin

1. Đăng nhập `admin@vuavuive.vn` / `Admin@123`.
2. Xem dashboard.
3. Quản lý sản phẩm: thêm/sửa/xóa hoặc đổi trạng thái active.
4. Mở danh sách đơn hàng, xác nhận đơn COD mới.
5. Cập nhật trạng thái đơn theo luồng: chờ duyệt -> xác nhận -> đang giao -> đã giao.

### Shipper

1. Đăng nhập `shipper@gmail.com` / `Shipper@123`.
2. Bật/tắt trạng thái online.
3. Kiểm tra tab cần giao.
4. Mở chi tiết đơn, xem địa chỉ/số điện thoại/tổng tiền.
5. Bắt đầu giao, sau đó giao thành công hoặc giao thất bại.
6. Kiểm tra đơn chuyển sang lịch sử và thống kê cập nhật.

### Flow liên thông 3 app

1. Customer tạo đơn COD.
2. Admin xác nhận đơn.
3. Shipper nhận/giao đơn.
4. Customer refresh Orders và kiểm tra trạng thái mới.
5. Với đơn đã giao, Customer thử review hoặc yêu cầu trả hàng.


## 8. Lưu ý

- Không commit hoặc gửi công khai `.env`, `serviceAccountKey.json`, API key thanh toán, Cloudinary, Gemini, Telegram, Resend.
- Backend local dùng port `3000`.
- Debug Android đang tối ưu cho emulator qua `10.0.2.2`.
- Thanh toán MoMo/ZaloPay mặc định có mock mode để demo không cần tài khoản sandbox thật.
- Nếu app giữ session cũ sau khi đổi dữ liệu Firebase, hãy Clear Storage hoặc gỡ app rồi cài lại.
- Thư mục `scratch/` chứa script/log/screenshot phục vụ test nội bộ, không cần để chạy app.
