# Firebase Cleanup Plan – Vựa Vui Vẻ

> **Mục tiêu:** Xoá an toàn tất cả code legacy (Retrofit, Spring Boot, Room ProductDao) sau khi verify Firebase-only hoàn tất.  
> **Nguyên tắc:** Không xoá gì nếu chưa verify runtime. Xoá theo thứ tự ưu tiên.

---

## Nguyên tắc xoá an toàn

1. **Verify trước, xoá sau.** Mỗi module phải compile và chạy thành công với Firebase trước khi xoá legacy.
2. **Xoá theo module.** Không xoá cross-module cùng lúc.
3. **Giữ Room CartDao.** `CartItemEntity`, `CartDao`, `AppDatabase` là thiết kế có chủ đích cho offline cart.
4. **Không xoá app-backend** cho tới khi toàn bộ 3 app đã verify Firebase-only.
5. **Commit sau mỗi phase**, không commit giữa chừng.

---

## Danh sách file cần xoá (theo priority)

### Nhóm 1 – Dead code, xoá ngay sau Phase 1-2 (app-customer Shipment done)

> Các file này đã không còn được inject vào ViewModel, không ảnh hưởng runtime.

| File | Đường dẫn | Lý do xoá |
|------|-----------|-----------|
| `ProductRepository.java` | `app-customer/data/repository/` | Thay bằng `ProductRepositoryFirebase` |
| `CartRepository.java` | `app-customer/data/repository/` | Thay bằng `FirebaseCartRepository` |
| `OrderRepository.java` | `app-customer/data/repository/` | Thay bằng `FirebaseOrderRepository` |
| `CategoryRepository.java` | `app-customer/data/repository/` | Thay bằng `CategoryRepositoryFirebase` |

> ⚠️ **Chú ý `AuthRepository.java`:** Class này vẫn được dùng như **type holder** (`AuthRepository.Result<T>`) khắp codebase. Nếu xoá, phải replace tất cả `AuthRepository.Result` bằng wrapper khác. Khuyến nghị: **giữ lại, chỉ xoá logic Retrofit bên trong**.

### Nhóm 2 – Sau khi Phase 2 (app-customer Shipment) done

| File | Đường dẫn | Lý do xoá |
|------|-----------|-----------|
| `ShipmentRepository.java` | `app-customer/data/repository/` | Thay bằng `FirebaseShipmentRepository` |

### Nhóm 3 – Sau khi Phase 3 (app-admin MockRepository) done

| File / Section | Đường dẫn | Lý do xoá |
|----------------|-----------|-----------|
| Gọi `MockRepository.getShipments()` | `ShipmentListFragment`, `ShipmentDetailActivity` | Thay bằng Firebase |
| Gọi `MockRepository.getUsers()` | `UserListFragment` | Thay bằng Firebase `/users` |
| Gọi `MockRepository.getVouchers()` | `VoucherListFragment`, `VoucherEditActivity` | Thay bằng Firebase `/vouchers` |
| Gọi `MockRepository.getAuditLogs()` | `AuditLogFragment` | Thay bằng Firebase `/audit_logs` |
| `MockRepository.getInstance()` toàn bộ | app-admin | Xoá sau khi tất cả màn migrate xong |

### Nhóm 4 – Sau khi Phase 4 (app-shipper) done

| File | Đường dẫn | Lý do xoá |
|------|-----------|-----------|
| `NetworkModule.java` (Retrofit version) | `app-shipper/di/` | Thay bằng Firebase DI |
| `AuthViewModel.java` (Retrofit) | `app-shipper/viewmodel/` | Thay bằng Firebase Auth |
| Tất cả `retrofit2.Call` imports | `app-shipper/ui/` và `viewmodel/` | Không còn cần |

### Nhóm 5 – Cleanup NetworkModule (tất cả app)

| Module | Hành động |
|--------|-----------|
| `app-customer/NetworkModule.java` | Xoá tất cả `@Provides` cho Retrofit APIs đã không dùng. Giữ `SessionManager`, xoá `Retrofit`, `OkHttpClient`, các API providers. |
| `app-admin/NetworkModule.java` | Tương tự – chỉ giữ Firebase providers |
| `app-shipper/NetworkModule.java` | Xoá toàn bộ, tạo mới với Firebase providers |

### Nhóm 6 – shared Room cleanup (cuối cùng)

| File | Hành động | Khi nào |
|------|-----------|---------|
| `ProductDao.java` | Xoá | Sau khi confirm không còn DAO nào inject |
| `ProductEntity.java` | Xoá | Sau khi xoá ProductDao |
| Cập nhật `AppDatabase.java` | Xoá `productDao()` method và `ProductEntity` khỏi `@Database` entities | Cùng lúc xoá ProductDao |
| **GIỮ** `CartDao.java` | Không xoá | FirebaseCartRepository dùng |
| **GIỮ** `CartItemEntity.java` | Không xoá | FirebaseCartRepository dùng |

---

## Cleanup app-backend

> **Điều kiện:** Toàn bộ 3 app (customer, admin, shipper) phải đã verify Firebase-only thành công.

### Bước 1 – Đóng băng
- Không thêm tính năng mới vào `app-backend`
- Không deploy lên server mới

### Bước 2 – Verify independence
Chạy toàn bộ 3 app trong chế độ airplane mode (không có backend) và confirm:
- Login vẫn work (Firebase Auth)
- Product list vẫn load (Firebase RTDB)
- Order vẫn tạo được (Firebase RTDB)
- Cart vẫn hoạt động (Room local)

### Bước 3 – Archive
```bash
# Tạo archive backup
cd /Users/lynhuhieu/Documents/VuaVuiVe_App
git tag legacy-backend-final
git push origin legacy-backend-final

# Có thể xoá app-backend khỏi settings.gradle.kts
# include(":app-customer", ":app-admin", ":app-shipper", ":shared")
# Không còn include ":app-backend"
```

---

## Dependency cleanup – build.gradle.kts

Sau khi xoá tất cả Retrofit usage, có thể xoá các dependency sau khỏi `shared/build.gradle.kts`:

```kotlin
// CÓ THỂ XOÁ sau khi verify
implementation("com.squareup.retrofit2:retrofit:2.x.x")
implementation("com.squareup.retrofit2:converter-gson:2.x.x")
implementation("com.squareup.okhttp3:okhttp:4.x.x")
implementation("com.squareup.okhttp3:logging-interceptor:4.x.x")
```

**GIỮ lại:**

```kotlin
// Firebase
implementation(platform("com.google.firebase:firebase-bom:33.x.x"))
implementation("com.google.firebase:firebase-auth")
implementation("com.google.firebase:firebase-database")

// Room – cho local cart cache
implementation("androidx.room:room-runtime:2.x.x")
annotationProcessor("androidx.room:room-compiler:2.x.x")
```

---

## Verification commands

```bash
# Sau mỗi cleanup phase, chạy:
./gradlew :app-customer:assembleDebug
./gradlew :app-admin:assembleDebug
./gradlew :app-shipper:assembleDebug

# Kiểm tra không còn Retrofit import trong runtime path
grep -rn "retrofit2" app-customer/src/main/java --include="*.java" | grep -v "legacy\|old\|backup"
grep -rn "retrofit2" app-admin/src/main/java --include="*.java"
grep -rn "retrofit2" app-shipper/src/main/java --include="*.java"

# Kiểm tra không còn MockRepository trong runtime
grep -rn "MockRepository" app-admin/src/main/java --include="*.java"
```

---

## Timeline đề xuất

| Phase | Công việc | Ưu tiên |
|-------|-----------|---------|
| **Phase A** | Migrate app-customer Shipment → Firebase | Cao |
| **Phase B** | Migrate app-admin Users, Vouchers, AuditLog, Shipment → Firebase | Trung bình |
| **Phase C** | Migrate app-shipper toàn bộ → Firebase | Cao |
| **Phase D** | Cleanup dead code (Nhóm 1-4) | Sau A+B+C |
| **Phase E** | Cleanup NetworkModule, shared Room | Sau D |
| **Phase F** | Archive app-backend | Sau E + verify |
