# Firebase Runtime Audit – Vựa Vui Vẻ

> **Ngày audit:** 2026-06-24  
> **Trạng thái tổng thể:** ⚠️ PARTIAL – app-customer & app-admin đã Firebase-only về core data; app-shipper vẫn dùng Retrofit thật; Room còn dùng cho giỏ hàng local.

---

## 1. Tóm tắt nhanh

| Module | Auth | Products | Orders | Cart | Notes |
|--------|------|----------|--------|------|-------|
| **app-customer** | ✅ Firebase Auth | ✅ Firebase RTDB | ✅ Firebase RTDB | ✅ Room local cache | Retrofit API còn khai báo nhưng **không inject vào ViewModel** |
| **app-admin** | ✅ Firebase Auth | ✅ Firebase RTDB | ✅ Firebase RTDB | N/A | `MockRepository` còn dùng cho Shipment, AuditLog, Voucher, Users list |
| **app-shipper** | ⛔ Retrofit | ⛔ Retrofit | ⛔ Retrofit | N/A | **Chưa migrate sang Firebase** |
| **shared** | N/A | N/A | N/A | ✅ Room (intentional) | `AppDatabase`, `CartDao`, `CartItemEntity` được dùng chủ đích như local cart |
| **app-backend** | ⚪ N/A | ⚪ N/A | ⚪ N/A | ⚪ N/A | Spring Boot backend – chưa cần xoá cho tới khi toàn bộ app migrate |

---

## 2. Chi tiết từng module

### 2.1 app-customer

#### ✅ Firebase – Runtime paths đã migrate

| ViewModel | Repository được inject | Nguồn dữ liệu |
|-----------|------------------------|---------------|
| `ProductViewModel` | `ProductRepositoryFirebase` | Firebase RTDB `/products` |
| `CategoryViewModel` | `CategoryRepositoryFirebase` | Firebase RTDB `/categories` |
| `CartViewModel` | `FirebaseCartRepository` | Room local + Firebase RTDB `/carts/{uid}` |
| `OrderViewModel` | `FirebaseOrderRepository` | Firebase RTDB `/orders` |
| `AuthViewModel` | `FirebaseUserRepository` | Firebase Auth + RTDB `/users` |

#### ⚠️ Legacy còn tồn tại (không runtime, không gây lỗi ngay)

| File | Vấn đề | Mức độ rủi ro |
|------|--------|---------------|
| `NetworkModule.java` | Khai báo Retrofit và inject tất cả API qua `retrofit.create()` | 🟡 Thấp – không inject vào ViewModel nữa |
| `ProductRepository.java` | Class cũ dùng Retrofit + Room ProductDao | 🟡 Thấp – dead code |
| `CartRepository.java` | Dùng Retrofit CartApi + Room CartDao | 🟡 Thấp – CartViewModel dùng FirebaseCartRepository |
| `AuthRepository.java` | Dùng Retrofit AuthApi | 🟡 Thấp – AuthViewModel dùng FirebaseUserRepository |
| `OrderRepository.java` | Dùng Retrofit OrderApi | 🟡 Thấp – OrderViewModel dùng FirebaseOrderRepository |
| `ShipmentRepository.java` | Dùng Retrofit ShipmentApi | 🟠 **CAO** – ShipmentViewModel vẫn inject cái này |

> **Lưu ý:** `ShipmentViewModel` hiện tại **vẫn inject `ShipmentRepository` (Retrofit-based)**, không phải Firebase. Nếu backend offline, màn Lịch sử vận chuyển sẽ fail.

#### Room – Được dùng chủ đích

- `CartItemEntity` → `CartDao` → `AppDatabase`: dùng trong `FirebaseCartRepository` như **local cache cho giỏ hàng**. Offline-capable cart design.
- `ProductDao` / `ProductEntity`: Vẫn định nghĩa trong `shared` nhưng **không còn được gọi** từ ViewModel nào. Có thể xoá.

---

### 2.2 app-admin

#### ✅ Firebase – Runtime paths đã migrate

| API Interface | Implementation inject thật | Ghi chú |
|---------------|---------------------------|---------|
| `AuthApi` | `FirebaseAuthApi` | Firebase Auth + whitelist role |
| `ProductApi` | `FirebaseAdminProductApi` | Firebase RTDB `/products` |
| `AdminProductApi` | `FirebaseAdminProductApi` | CRUD products trên Firebase |
| `OrderApi` | `FirebaseAdminOrderApi` | Firebase RTDB `/orders` |
| `AdminOrderApi` | `FirebaseAdminOrderApi` | Status update, return review |

#### ⚠️ MockRepository còn được dùng trực tiếp

Các màn sau **vẫn gọi `MockRepository.getInstance()` trực tiếp**, không qua Hilt:

| File | Dữ liệu mock còn dùng |
|------|----------------------|
| `ShipmentListFragment.java` | `getShipments()` |
| `ShipmentDetailActivity.java` | `getShipments()` |
| `AuditLogFragment.java` | `getAuditLogs()`, `getCurrentUser()` |
| `UserListFragment.java` | `getUsers()`, `updateUser()`, `getCurrentUser()` |
| `VoucherListFragment.java` | `getVouchers()`, `getCurrentUser()` |
| `VoucherEditActivity.java` | Voucher CRUD |
| `DashboardFragment.java` | `addAuditLog()`, dashboard stats |
| `AdminLoginActivity.java` | `setCurrentUser()` sau Firebase login |
| `AdminChatFragment.java` | Chat data |

> **Kết luận:** MockRepository vẫn phục vụ dữ liệu cho ~8 màn. Shipments, Users, Vouchers, AuditLog vẫn là dữ liệu giả.

#### Retrofit còn được instantiate

`NetworkModule.java` của admin vẫn build một `Retrofit` instance nhưng các API quan trọng (Auth, Product, Order) đã được override bằng Firebase implementation. Chỉ `ShipmentApi` và `AdminUserApi` vẫn gọi `retrofit.create()` thật.

---

### 2.3 app-shipper

#### ⛔ Chưa migrate – Toàn bộ dùng Retrofit

| File | Vấn đề |
|------|--------|
| `NetworkModule.java` | `AuthApi`, `OrderApi`, `ShipperOrderApi` đều qua `retrofit.create()` → Spring Boot |
| `AuthViewModel.java` | Dùng Retrofit AuthApi |
| `ShipperOrderListFragment.java` | Dùng Retrofit OrderApi |
| `ShipperOrderDetailActivity.java` | Dùng Retrofit ShipperOrderApi |
| `ShipperMainActivity.java` | Dùng Retrofit |

> **Kết luận:** app-shipper **100% Retrofit**, không có Firebase nào được tích hợp. App sẽ fail ngay khi backend offline.

---

### 2.4 shared

| Class | Mô tả | Có cần giữ không? |
|-------|-------|-------------------|
| `AppDatabase.java` | Room database chứa Cart và Product | ✅ Giữ – CartDao đang được dùng |
| `CartDao.java` | Local cart CRUD | ✅ Giữ – FirebaseCartRepository cần |
| `CartItemEntity.java` | Entity cho giỏ hàng local | ✅ Giữ |
| `ProductDao.java` | Local product cache | 🟡 Xem xét xoá sau – không còn inject |
| `ProductEntity.java` | Entity cho product cache | 🟡 Xem xét xoá sau – không còn inject |

---

## 3. Kết luận & Mức độ rủi ro

| Rủi ro | Mô tả | Ưu tiên |
|--------|-------|---------|
| 🔴 **CRITICAL** | app-shipper chưa migrate, sẽ crash khi backend offline | Cao nhất |
| 🟠 **HIGH** | `ShipmentRepository` trong app-customer vẫn dùng Retrofit | Cao |
| 🟡 **MEDIUM** | app-admin còn nhiều màn dùng MockRepository | Trung bình |
| 🟢 **LOW** | Legacy Retrofit repos cũ còn tồn tại nhưng không được inject | Thấp – dead code |
| 🟢 **LOW** | Room ProductDao/ProductEntity không còn inject nhưng vẫn compile | Thấp – cleanup sau |
