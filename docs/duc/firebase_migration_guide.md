# Firebase Migration Guide – Vựa Vui Vẻ

> **Mục tiêu:** Hướng dẫn từng bước migrate các module còn lại sang Firebase-only architecture.  
> **Ngày cập nhật:** 2026-06-24

---

## Kiến trúc mục tiêu

```
Android Apps
    ↓
Firebase Authentication  (đăng nhập, phân quyền)
    +
Firebase Realtime Database  (dữ liệu sản phẩm, đơn hàng, giỏ hàng)
    +
Room Database  (local cart cache – chỉ app-customer)
```

Không còn Retrofit, Spring Boot, PostgreSQL trong runtime path.

---

## Trạng thái hiện tại

| Module | Tiến độ |
|--------|---------|
| app-customer – Auth, Product, Cart, Order | ✅ Done |
| app-admin – Auth, Product, Order | ✅ Done |
| app-customer – Shipment | ⛔ Cần migrate |
| app-admin – Shipment, Users, Voucher, AuditLog | ⛔ Cần migrate (MockRepository) |
| app-shipper – Toàn bộ | ⛔ Cần migrate |

---

## Phase 1: Migrate app-customer Shipment

### Mục tiêu
Thay `ShipmentRepository` (Retrofit) bằng `FirebaseShipmentRepository`.

### Cấu trúc Firebase RTDB đề xuất

```
/shipments
  /{shipmentId}
    orderId: string
    customerId: string
    shipperId: string
    status: string  ("ASSIGNED" | "PICKED_UP" | "IN_TRANSIT" | "DELIVERED" | "FAILED")
    createdAt: long
    updatedAt: long
    trackingNotes: string
```

### Bước thực hiện

1. **Tạo `FirebaseShipmentRepository.java`** trong `app-customer/data/repository/`:
   ```java
   @Singleton
   public class FirebaseShipmentRepository {
       private final DatabaseReference ref = FirebaseDatabase.getInstance()
           .getReference("shipments");
       private final SessionManager sessionManager;

       @Inject
       public FirebaseShipmentRepository(SessionManager sessionManager) {
           this.sessionManager = sessionManager;
       }

       public LiveData<AuthRepository.Result<List<Shipment>>> getMyShipments() {
           // Query shipments WHERE customerId == currentUser.uid
       }
   }
   ```

2. **Cập nhật `ShipmentViewModel`** để inject `FirebaseShipmentRepository`.

3. **Cập nhật `DatabaseModule.java`** để provide `FirebaseShipmentRepository`.

4. **Xoá** `ShipmentRepository.java` (legacy Retrofit).

---

## Phase 2: Migrate app-admin MockRepository → Firebase

### 2.1 Users (Quản lý thành viên)

**Nguồn Firebase:** `/users/{uid}` đã có sẵn.

1. Tạo `FirebaseUserAdminRepository` query tất cả `/users`.
2. Cập nhật `UserListFragment` inject repository thay `MockRepository.getInstance().getUsers()`.

### 2.2 Vouchers

**Cấu trúc Firebase RTDB đề xuất:**

```
/vouchers
  /{voucherId}
    code: string
    discountType: string  ("PERCENT" | "FIXED")
    discountValue: double
    minOrderValue: double
    maxUses: int
    usedCount: int
    startDate: long
    endDate: long
    isActive: boolean
```

Tạo `FirebaseVoucherRepository` thực hiện CRUD trên node này.

### 2.3 AuditLog

**Cấu trúc Firebase RTDB đề xuất:**

```
/audit_logs
  /{logId}
    action: string
    target: string
    description: string
    performedBy: string  (uid)
    timestamp: long
```

Tạo `FirebaseAuditLogRepository`.

### 2.4 Shipments (Admin)

Query `/shipments` và filter theo status hoặc shipperId.

### 2.5 DashboardFragment

Thay các stat từ MockRepository bằng queries tổng hợp Firebase:
- Tổng đơn: `COUNT /orders`
- Doanh thu: `SUM /orders[status==DELIVERED].totalAmount`
- Tổng sản phẩm: `COUNT /products[isActive==true]`

---

## Phase 3: Migrate app-shipper

### Bước thực hiện

1. **Thêm Firebase dependency vào `app-shipper/build.gradle.kts`:**
   ```kotlin
   implementation(platform("com.google.firebase:firebase-bom:33.x.x"))
   implementation("com.google.firebase:firebase-auth")
   implementation("com.google.firebase:firebase-database")
   ```

2. **Tạo `FirebaseShipperAuthRepository`:**
   ```java
   // Đăng nhập Firebase Auth
   // Đọc role từ /users/{uid}/role
   // Chỉ cho phép role == "SHIPPER"
   ```

3. **Tạo `FirebaseShipperOrderRepository`:**
   ```java
   // Query /orders WHERE shipperId == currentUser.uid
   // Cập nhật status
   ```

4. **Thay `NetworkModule.java`** – xoá Retrofit, inject Firebase repositories qua Hilt.

5. **Cập nhật tất cả Fragment/Activity** inject Firebase repositories.

6. **Xoá** `NetworkModule.java` (Retrofit version).

### Firebase RTDB Rules cho Shipper

```json
"orders": {
  "$orderId": {
    ".read": "auth != null && (
      root.child('users').child(auth.uid).child('role').val() == 'ADMIN' ||
      root.child('users').child(auth.uid).child('role').val() == 'STAFF' ||
      (root.child('users').child(auth.uid).child('role').val() == 'SHIPPER' &&
       data.child('shipperId').val() == auth.uid)
    )",
    ".write": "auth != null && (
      root.child('users').child(auth.uid).child('role').val() == 'SHIPPER' &&
      data.child('shipperId').val() == auth.uid
    )"
  }
}
```

---

## Phase 4: Cleanup legacy code

> **Chỉ thực hiện sau khi verify xong toàn bộ Phase 1–3.**

### app-customer

- [ ] Xoá `ProductRepository.java` (Retrofit)
- [ ] Xoá `CartRepository.java` (Retrofit)
- [ ] Xoá `AuthRepository.java` (Retrofit) – chú ý class này còn dùng như type holder cho `Result<T>`
- [ ] Xoá `OrderRepository.java` (Retrofit)
- [ ] Xoá `ShipmentRepository.java` (sau khi Phase 1 done)
- [ ] Xoá `CategoryRepository.java` (Retrofit)
- [ ] Dọn `NetworkModule.java` – xoá Retrofit, giữ SessionManager
- [ ] Xoá `ProductDao.java`, `ProductEntity.java` trong shared (sau khi confirm không cần)

### app-admin

- [ ] Thoát MockRepository khỏi UI layers sau khi có Firebase repos
- [ ] Xoá các phần `MockRepository` không còn cần

### shared

- [ ] Giữ `CartDao`, `CartItemEntity`, `AppDatabase` (cần cho FirebaseCartRepository)
- [ ] Xoá `ProductDao`, `ProductEntity` sau Phase 4

### app-backend

- [ ] Đóng băng backend, không deploy mới
- [ ] Sau khi toàn bộ app verify Firebase-only → archive/delete

---

## Checklist tổng thể

- [x] app-customer – Auth
- [x] app-customer – Product
- [x] app-customer – Cart (Room local cache)
- [x] app-customer – Order
- [x] app-customer – Category
- [ ] app-customer – Shipment
- [x] app-admin – Auth (Firebase Auth + whitelist)
- [x] app-admin – Product CRUD
- [x] app-admin – Order management
- [ ] app-admin – User management
- [ ] app-admin – Voucher management
- [ ] app-admin – AuditLog
- [ ] app-admin – Shipment management
- [ ] app-shipper – Auth
- [ ] app-shipper – Order list & update
