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

## 1. Cài Đặt PostgreSQL (nếu chưa có)

### Cách 1: Tải từ trang chủ
1. Truy cập [https://www.postgresql.org/download/windows/](https://www.postgresql.org/download/windows/)
2. Tải bản **PostgreSQL 15+** → chạy installer
3. Trong quá trình cài đặt:
   - **Port**: giữ mặc định `5432`
   - **Password cho user `postgres`**: đặt là `Viet0609` (hoặc password tùy ý, nhưng phải sửa lại trong config)
   - Bỏ chọn Stack Builder khi được hỏi

### Cách 2: Cài qua Scoop (nhanh hơn)
```powershell
scoop install postgresql
pg_ctl register -N postgresql -D "C:\ProgramData\PostgreSQL\data"
initdb -D "C:\ProgramData\PostgreSQL\data" -U postgres -E UTF8
pg_ctl start -D "C:\ProgramData\PostgreSQL\data"
```

### Cách 3: Cài qua Chocolatey
```powershell
choco install postgresql --params "/Password:Viet0609"
```

### Kiểm tra PostgreSQL đã chạy
```powershell
psql -U postgres -c "SELECT version();"
```
> Nếu lệnh `psql` không tìm thấy, thêm đường dẫn PostgreSQL `bin` vào biến môi trường `PATH` (thường là `C:\Program Files\PostgreSQL\15\bin`).

---

## 2. Tạo Database & Migrate Dữ Liệu

> Script sẽ tự động tạo database `vuavuive_app`, tạo bảng và migrate dữ liệu từ file SQLite có sẵn.

```powershell
cd app-backend
pip install psycopg2-binary
python migrate_to_postgres.py
```

**Kết quả mong đợi:**
```
[1/4] Creating database 'vuavuive_app'...
[2/4] Creating tables in 'vuavuive_app'...
[3/4] Migrating data from SQLite...
[4/4] Verify row counts in PostgreSQL:
[DONE] Migration complete!
```

> ⚠️ **Nếu password PostgreSQL khác `Viet0609`**, sửa lại trong 2 file:
> - `app-backend/migrate_to_postgres.py` → dòng `PG_PASS`
> - `app-backend/src/main/resources/application-dev.yml` → dòng `password`

---

## 3. Cài Đặt JDK 21 (nếu chưa có)

### Cách 1: Scoop (khuyên dùng)
```powershell
scoop bucket add java
scoop install temurin21-jdk
```

### Cách 2: Tải thủ công
Tải từ [https://adoptium.net/](https://adoptium.net/) → chọn **Temurin 21 LTS**

### Kiểm tra
```powershell
java -version
# Kết quả: openjdk version "21.x.x"
```

---

## 4. Chạy Backend Server

```powershell
cd app-backend
.\run_backend.bat
```

Backend sẽ chạy tại **http://localhost:3000**

> Swagger API docs: [http://localhost:3000/swagger-ui.html](http://localhost:3000/swagger-ui.html)

---

## 5. Build & Chạy Android Modules

1. Mở thư mục gốc `VuaVuiVe` bằng **Android Studio**
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
2. Chạy migrate (lần đầu): python migrate_to_postgres.py
3. Chạy backend: run_backend.bat
4. Chạy app Android từ Android Studio
```
