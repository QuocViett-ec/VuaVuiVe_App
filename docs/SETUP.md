# 🚀 Hướng Dẫn Cài Đặt Dự Án VuaVuiVe

## Yêu Cầu Hệ Thống

| Công cụ | Phiên bản | Ghi chú |
|---------|-----------|---------|
| **JDK** | 21 | Khuyên dùng Temurin 21 |
| **Android Studio** | Mới nhất | Build Android modules |
| **PostgreSQL** | 15+ | Database cho backend |
| **Python** | 3.8+ | Chạy script migrate dữ liệu |
| **Maven** | 3.9+ | Đã có sẵn trong `app-backend/apache-maven-3.9.6` |

---

## 1. Cài Đặt PostgreSQL

### 🍎 macOS

```bash
# Cài qua Homebrew (khuyên dùng)
brew install postgresql@15
brew services start postgresql@15

# Thêm vào PATH (thêm vào ~/.zshrc hoặc ~/.bash_profile)
echo 'export PATH="/opt/homebrew/opt/postgresql@15/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# Tạo user postgres với password
psql postgres -c "CREATE USER postgres WITH SUPERUSER PASSWORD 'Viet0609';"

# Kiểm tra
psql -U postgres -c "SELECT version();"
```

### 🪟 Windows

**Cách 1: Tải từ trang chủ**
1. Truy cập [https://www.postgresql.org/download/windows/](https://www.postgresql.org/download/windows/)
2. Tải bản **PostgreSQL 15+** → chạy installer
3. Trong quá trình cài đặt:
   - **Port**: giữ mặc định `5432`
   - **Password cho user `postgres`**: đặt là `Viet0609`
   - Bỏ chọn Stack Builder khi được hỏi

**Cách 2: Cài qua Scoop**
```powershell
scoop install postgresql
```

**Cách 3: Cài qua Chocolatey**
```powershell
choco install postgresql --params "/Password:Viet0609"
```

**Kiểm tra:**
```powershell
psql -U postgres -c "SELECT version();"
```

> Nếu lệnh `psql` không tìm thấy, thêm đường dẫn PostgreSQL `bin` vào biến môi trường `PATH` (thường là `C:\Program Files\PostgreSQL\15\bin`).

---

## 2. Cài Đặt JDK 21

### 🍎 macOS
```bash
# Cài qua Homebrew
brew install --cask temurin@21

# Kiểm tra
java -version
# Kết quả: openjdk version "21.x.x"
```

### 🪟 Windows

**Cách 1: Scoop (khuyên dùng)**
```powershell
scoop bucket add java
scoop install temurin21-jdk
```

**Cách 2: Tải thủ công**
Tải từ [https://adoptium.net/](https://adoptium.net/) → chọn **Temurin 21 LTS**

```powershell
java -version
# Kết quả: openjdk version "21.x.x"
```

---

## 3. Migrate Dữ Liệu SQLite → PostgreSQL

> 📦 File `app-backend/vuavuive_v2.db` đã có sẵn trong repo (đã được push lên). Chỉ cần chạy script migrate một lần để chuyển vào PostgreSQL.

```bash
cd app-backend
python migrate_to_postgres.py
```

**Kết quả mong đợi:**
```
[1/4] Creating database 'vuavuive_app'...
[2/4] Creating tables in 'vuavuive_app'...
[3/4] Migrating data from SQLite...
  [OK] users: 3 rows migrated
  [OK] categories: 8 rows migrated
  [OK] products: 92 rows migrated
  ...
[4/4] Verify row counts in PostgreSQL:
[DONE] Migration complete!
```

> ⚠️ **Nếu password PostgreSQL khác `Viet0609`**, sửa lại trong 2 file:
> - `app-backend/migrate_to_postgres.py` → dòng `PG_PASS`
> - `app-backend/src/main/resources/application-dev.yml` → dòng `password`

---

## 4. Chạy Backend Server

### 🍎 macOS / Linux
```bash
cd app-backend
chmod +x mvn
./mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 🪟 Windows
```powershell
cd app-backend
.\run_backend.bat
```

Backend sẽ chạy tại **http://localhost:3000**

> Swagger API docs: [http://localhost:3000/swagger-ui.html](http://localhost:3000/swagger-ui.html)

---

## 5. Build & Chạy Android Modules

1. Mở thư mục gốc `VuaVuiVe_App` bằng **Android Studio**
2. Đợi Gradle sync hoàn tất
3. Chọn module cần chạy:
   - `app-customer` — Ứng dụng khách hàng
   - `app-admin` — Ứng dụng quản trị
4. Chọn thiết bị/emulator → **Run ▶**

> ⚠️ Đảm bảo backend đang chạy trước khi mở app Android.

---

## Tóm Tắt Thứ Tự Khởi Động

```
1. Khởi động PostgreSQL service
2. Chạy migrate (lần đầu tiên): python migrate_to_postgres.py
3. Chạy backend server
4. Chạy app Android từ Android Studio
```

---

## Cấu Trúc Dự Án

```
VuaVuiVe_App/
├── app-backend/          # Spring Boot backend (Java)
│   ├── src/              # Source code
│   ├── vuavuive_v2.db    # SQLite database (dùng để migrate)
│   ├── migrate_to_postgres.py  # Script migrate sang PostgreSQL
│   └── run_backend.bat   # Script chạy backend (Windows)
├── app-customer/         # Android app cho khách hàng
├── app-admin/            # Android app cho admin
└── shared/               # Shared code (models, API interfaces)
```

---

## Thông Tin Tài Khoản Mặc Định

Sau khi migrate dữ liệu, các tài khoản mặc định trong database:

| Role | Thông tin đăng nhập |
|------|---------------------|
| Admin | Xem trong `app-backend/vuavuive_v2.db` → bảng `users` |

> Mật khẩu đã được hash bằng BCrypt, kiểm tra tài khoản admin trong database.

---

## Tích Hợp Bên Ngoài (cần cấu hình trong `application-dev.yml`)

| Dịch vụ | Mô tả |
|---------|-------|
| **Cloudinary** | Lưu trữ ảnh sản phẩm |
| **Telegram Bot** | Đăng nhập/đăng ký qua Telegram |
| **VNPay** | Thanh toán online (sandbox) |
| **Google Gemini AI** | Chatbot hỏi đáp |
