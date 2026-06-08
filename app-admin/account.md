Để khởi động backend của dự án này, bạn cần thực hiện các bước chuẩn bị cơ sở dữ liệu và chạy lệnh Maven. Dưới đây là hướng dẫn chi tiết:

### Bước 1: Chuẩn bị Cơ sở dữ liệu (PostgreSQL)
1. Hãy chắc chắn rằng bạn đã khởi động **PostgreSQL** trên máy của mình.
2. Tạo một database mới tên là: **`vuavuive_db`**
3. Mở file [application-dev.yml](file:///e:/Nam3/TaiLieuHocKi8/APP/VuaVuiVe/app-backend/src/main/resources/application-dev.yml) và kiểm tra/chỉnh sửa thông tin kết nối Database của bạn (nếu mật khẩu PostgreSQL của bạn khác với cấu hình mặc định):
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/vuavuive_db
       username: postgres
       password: Viet0609  # Thay bằng mật khẩu Postgres của bạn nếu có thay đổi
   ```
*(Lưu ý: Hệ thống đã cấu hình `ddl-auto: update` nên các bảng cơ sở dữ liệu sẽ tự động được tạo ra khi khởi chạy backend).*

---

### Bước 2: Chạy Backend bằng Command Line
Mở terminal (PowerShell hoặc Command Prompt) tại thư mục `app-backend` (`E:\Nam3\TaiLieuHocKi8\APP\VuaVuiVe\app-backend`) và chạy lệnh sau:

```bash
mvn.cmd spring-boot:run
```

Nếu chạy thành công, bạn sẽ thấy log Spring Boot hiển thị và thông báo cổng lắng nghe là **`3000`** (đã được cấu hình trong `application-dev.yml`).

---

### Bước 3: Kiểm tra Backend
Sau khi hệ thống khởi động thành công, bạn có thể truy cập các đường dẫn sau trên trình duyệt để kiểm tra:
- **Swagger UI (Tài liệu API)**: [http://localhost:3000/swagger-ui/index.html](http://localhost:3000/swagger-ui/index.html)
- **API Docs**: [http://localhost:3000/api-docs](http://localhost:3000/api-docs)

---

### Bước 4: Tài khoản Test có sẵn (DataSeeder)
Hệ thống sẽ tự động chèn dữ liệu mẫu (Seeder) khi khởi chạy lần đầu tiên. Bạn có thể sử dụng các tài khoản sau để test:
- **Admin**: `admin@vuavuive.vn` / mật khẩu: `Admin@123`
- **Customer**: `customer@gmail.com` / mật khẩu: `Customer@123`
- **Shipper**: `shipper@gmail.com` / mật khẩu: `Shipper@123`