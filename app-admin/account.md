# Hướng dẫn Cấu hình & Tài khoản Thử nghiệm (VuaVuiVe App)

Tài liệu này cung cấp thông tin chi tiết về cơ sở dữ liệu, cách khởi chạy Backend và danh sách các tài khoản kiểm thử được tích hợp sẵn phục vụ cho việc chạy thử nghiệm và bảo vệ đồ án.

---

## 1. Cấu hình Cơ sở dữ liệu (Database)

Dự án đã được chuyển đổi sang sử dụng **SQLite** thay vì PostgreSQL để tối giản hóa quá trình cài đặt và deploy local. 

- **Ưu điểm**: Không cần cài đặt máy chủ cơ sở dữ liệu riêng biệt, không lo lỗi kết nối do sai cổng/mật khẩu. Cơ sở dữ liệu được lưu dưới dạng file cục bộ (`vuavuive_v2.db` nằm ở thư mục gốc của module `app-backend`).
- **Tự động tạo bảng & Seed dữ liệu**: Hệ thống sử dụng chế độ `ddl-auto: update` trong JPA/Hibernate để tự tạo cấu trúc bảng, đồng thời lớp `DataSeeder` sẽ tự động chèn dữ liệu mẫu (Danh mục, Sản phẩm, Tài khoản) ngay khi khởi chạy backend lần đầu tiên.

### Chi tiết cấu hình trong [application-dev.yml](file:///e:/Nam3/TaiLieuHocKi8/APP/VuaVuiVe/app-backend/src/main/resources/application-dev.yml):
```yaml
spring:
  datasource:
    url: jdbc:sqlite:vuavuive_v2.db?date_string_format=yyyy-MM-dd%20HH:mm:ss.SSS&date_class=text&busy_timeout=5000
    driver-class-name: org.sqlite.JDBC
    username: sa
    password: sa
```

---

## 2. Các bước khởi chạy Backend

Mở cửa sổ dòng lệnh (Terminal, PowerShell hoặc Command Prompt) tại thư mục `app-backend` (`E:\Nam3\TaiLieuHocKi8\APP\VuaVuiVe\app-backend`) và chạy lệnh Maven sau:

```bash
mvn.cmd spring-boot:run
```
*(Nếu sử dụng macOS/Linux, chạy lệnh: `./mvnw spring-boot:run`)*

Sau khi khởi chạy thành công, máy chủ REST API sẽ chạy tại cổng **`3000`**. Bạn có thể kiểm tra qua:
- **Tài liệu API (Swagger UI)**: [http://localhost:3000/swagger-ui/index.html](http://localhost:3000/swagger-ui/index.html)
- **Đường dẫn API Docs**: [http://localhost:3000/api-docs](http://localhost:3000/api-docs)

---

## 3. Tài khoản kiểm thử có sẵn (Seed Data)

Dữ liệu kiểm thử mặc định được tự động khởi tạo bởi `DataSeeder.java`. Bạn có thể sử dụng các tài khoản này để đăng nhập trực tiếp trên các ứng dụng tương ứng:

| Vai trò (Role) | Email | Mật khẩu | Ứng dụng khuyên dùng | Mục đích kiểm thử |
| :--- | :--- | :--- | :--- | :--- |
| **Admin** | `admin@vuavuive.vn` | `Admin@123` | **app-admin** | Quản lý sản phẩm, đơn hàng, khách hàng, cấu hình khuyến mãi/voucher, theo dõi log và chat hỗ trợ. |
| **Customer** | `customer@gmail.com` | `Customer@123` | **app-customer** | Đặt hàng, quản lý giỏ hàng, thanh toán qua MoMo/ZaloPay Sandbox, xem lịch sử mua hàng, chat AI. |
| **Shipper** | `shipper@gmail.com` | `Shipper@123` | **Shipper Client / Postman** | Nhận đơn hàng, cập nhật trạng thái vận chuyển và giao hàng. |

---

## 4. Kết nối từ Android Emulator đến Backend

Trong môi trường giả lập Android (Android Emulator), địa chỉ `localhost` đại diện cho chính máy ảo đó. Để kết nối với Backend đang chạy trên máy chủ vật lý (máy tính của bạn), các ứng dụng Android sử dụng IP đặc biệt: **`10.0.2.2`**.

- **Cổng kết nối**: `3000` (địa chỉ đầy đủ: `http://10.0.2.2:3000`).
- **Địa chỉ cấu hình**:
  - Đối với Admin App: Cấu hình trong `buildConfigField` ở file [app-admin/build.gradle.kts](file:///e:/Nam3/TaiLieuHocKi8/APP/VuaVuiVe/app-admin/build.gradle.kts).
  - Đối với Customer App: Cấu hình trong `buildConfigField` ở file [app-customer/build.gradle.kts](file:///e:/Nam3/TaiLieuHocKi8/APP/VuaVuiVe/app-customer/build.gradle.kts).

```kotlin
buildTypes {
    debug {
        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:3000\"")
        buildConfigField("String", "PORTAL_SCOPE", "\"admin\"") // Hoặc "customer"
    }
}
```
