# PHẦN 1 — BẢN ĐỒ REPO (REPO MAP)

Tài liệu này quét toàn bộ cấu trúc mã nguồn của dự án Android E-Commerce "Vựa Vui Vẻ" (VuaVuiVe), bao gồm 4 module Gradle chính: `:shared`, `:app-customer`, `:app-admin`, và `:app-shipper`.

---

## 1. Cấu trúc Module Gradle
Dự án được tổ chức theo mô hình đa module (multi-module) nhằm chia sẻ mã nguồn và DTO:
- **`:shared`**: Thư viện chứa các DTOs (Data Transfer Objects), Room Entities, local DAOs, các định nghĩa API Retrofit, và các tiện ích dùng chung (SessionManager, Interceptors, Constants).
- **`:app-customer`**: Ứng dụng dành cho Khách hàng mua sắm sản phẩm.
- **`:app-admin`**: Ứng dụng quản trị dành cho Quản lý (Admin), Nhân viên (Staff), và Kiểm toán viên (Audit).
- **`:app-shipper`**: Ứng dụng giao hàng dành cho Tài xế giao hàng (Shipper).

---

## 2. Bảng Danh Sách Thành Phần Quan Trọng

| App/Module | Chức năng | File/Package quan trọng | Vai trò | Ghi chú |
| :--- | :--- | :--- | :--- | :--- |
| **`:shared`** | Chứa code dùng chung | `vn.vuavuive.shared.data.dto` | Định nghĩa cấu trúc dữ liệu gửi nhận (User, Product, Order, etc.) | Được import và sử dụng chung bởi cả 3 app. |
| | | `vn.vuavuive.shared.data.local` | Room database (`AppDatabase`), DAOs (`CartDao`, `ProductDao`) và Entities | Phục vụ offline-first cache và giỏ hàng của Customer. |
| | | `vn.vuavuive.shared.data.api` | Giao diện API Retrofit (`AuthApi`, `ProductApi`, `OrderApi`, etc.) | Nguyên bản dùng cho backend Spring Boot cũ. |
| | | `vn.vuavuive.shared.util` | `SessionManager`, `AuthInterceptor`, `Constants` | Xử lý JWT Token và lưu trữ Session nội bộ thiết bị. |
| **`:app-customer`**| Ứng dụng Khách hàng | `vn.vuavuive.customer.VuaVuiVeApp` | Entry Point chính của ứng dụng | Khởi tạo Dagger Hilt. |
| | | `vn.vuavuive.customer.ui.main.MainActivity` | Màn hình chính chứa Bottom Navigation | Điều phối các Fragment Home, Cart, Orders, Profile. |
| | | `vn.vuavuive.customer.data.repository` | `FirebaseUserRepository`, `ProductRepositoryFirebase`, `FirebaseCartRepository`, `FirebaseOrderRepository` | Các Repository chịu trách nhiệm giao tiếp với Firebase RTDB và Auth | Thay thế cho các Repository Retrofit cũ (`AuthRepository`, `ProductRepository`, etc.) vẫn còn tồn tại trong repo. |
| | | `vn.vuavuive.customer.viewmodel` | `AuthViewModel`, `ProductViewModel`, `CartViewModel`, `OrderViewModel` | Quản lý trạng thái UI và kết nối với Firebase Repository | Sử dụng mô hình LiveData bọc trong cấu trúc `Result<T>`. |
| **`:app-admin`** | Ứng dụng Quản trị | `vn.vuavuive.admin.VuaVuiVeAdminApp` | Entry Point chính của ứng dụng | Khởi tạo Dagger Hilt. |
| | | `vn.vuavuive.admin.ui.main.MainActivity` | Điều phối Navigation Drawer chính | Chứa bảng điều khiển Dashboard, quản lý sản phẩm, đơn hàng, shipper. |
| | | `vn.vuavuive.admin.data.repository.MockRepository` | Kho lưu trữ dữ liệu trung tâm của Admin app | **Thành phần đặc biệt**: Đồng bộ real-time dữ liệu từ Firebase RTDB về danh sách RAM cục bộ và cung cấp trực tiếp cho UI. |
| | | `vn.vuavuive.admin.data.firebase` | `FirebaseAdminProductApi`, `FirebaseAdminOrderApi` | Lớp mô phỏng (Mock Retrofit Call) bằng cách đọc/ghi trực tiếp Firebase | Được cấu hình thông qua `di.NetworkModule` để thay thế Retrofit Call cho Product và Order. |
| | | `vn.vuavuive.admin.ui` | `products.ProductEditActivity`, `orders.AdminOrderListFragment` | Màn hình danh sách/chỉnh sửa sản phẩm và quản lý đơn hàng | Kết nối qua Hilt API injection. |
| **`:app-shipper`** | Ứng dụng Giao hàng | `vn.vuavuive.shipper.VuaVuiVeShipperApp` | Entry Point chính của ứng dụng | Khởi tạo Dagger Hilt. |
| | | `vn.vuavuive.shipper.data.repository.FirebaseShipperRepository` | Repository duy nhất xử lý nghiệp vụ Shipper | Đăng nhập Auth, lọc đơn hàng theo `shipperId` và cập nhật trạng thái đơn. |
| | | `vn.vuavuive.shipper.viewmodel` | `ShipperOrderViewModel`, `ShipperAuthViewModel` | Quản lý luồng trạng thái cho tài xế | Giao tiếp trực tiếp với duy nhất `FirebaseShipperRepository`. |
| | | `vn.vuavuive.shipper.ui.order` | `ShipperOrderListFragment`, `ShipperOrderDetailActivity` | Hiển thị các đơn hàng được gán và cập nhật trạng thái giao hàng | Sử dụng giao diện thân thiện với tài xế (nút lớn, chọn nhanh lý do thất bại). |

---

## 3. Các File Cấu Hình Firebase & Gradle Quan Trọng

### 3.1. Firebase Configuration & Rules
- **`database.rules.json`**: Định nghĩa Security Rules của Firebase Realtime Database. Cho phép truy cập mở với danh mục/sản phẩm công khai, nhưng khóa chặt phần `/users`, `/carts`, và `/orders` theo phân quyền UID và Role (chỉ Admin mới được ghi đè toàn quyền).
- **`app-customer/google-services.json`**, **`app-admin/google-services.json`**, **`app-shipper/google-services.json`**: Các tệp chứa định danh dịch vụ và API Key kết nối với Firebase Console. Cả 3 tệp hiện đang cấu hình trỏ về cùng một Realtime Database URL: `https://vua-vui-ve-default-rtdb.firebaseio.com`.

### 3.2. Gradle Dependencies Quan Trọng (`shared/build.gradle.kts`)
- **Dagger Hilt**: `com.google.dagger:hilt-android:2.56.2` (Quản lý Dependency Injection).
- **Firebase Database & Auth**: `com.google.firebase:firebase-database` & `com.google.firebase:firebase-auth` (Truy cập dữ liệu real-time và tài khoản người dùng).
- **Retrofit**: `com.squareup.retrofit2:retrofit:2.11.0` & `converter-gson:2.11.0` (Client gọi HTTP API cũ).
- **Room DB**: `androidx.room:room-runtime:2.6.1` (Cơ sở dữ liệu SQLite cục bộ cho offline cache).
- **Glide**: `com.github.bumptech.glide:glide:4.16.0` (Tải và hiển thị hình ảnh sản phẩm mượt mà).
