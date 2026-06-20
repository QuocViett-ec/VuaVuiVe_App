# CODEX TASK: Tách Shipper App + Fix OrderStatus + Hoàn thiện Order Flow

## MỤC TIÊU
1. Tạo module `app-shipper` riêng biệt (như `app-admin`, `app-customer`)
2. Fix bug `parseOrderStatus()` trong `OrderService.java`
3. Hoàn thiện flow: Customer đặt → Admin xác nhận → Gán Shipper → Shipper giao → Cập nhật trạng thái
4. Liên kết bảng `Shipper` ↔ `User` trong backend
5. Thêm endpoint `GET /api/shippers/me` và `POST /api/auth/shipper/login`
6. Thêm cookie `vvv.shipper.sid` trong backend auth

## KIẾN TRÚC HIỆN TẠI

```
VuaVuiVe/
├── shared/          (Android library — DTO, API interfaces, Utils)
├── app-customer/    (Android app — namespace: vn.vuavuive.customer, PORTAL_SCOPE: "customer")
├── app-admin/       (Android app — namespace: vn.vuavuive.admin, PORTAL_SCOPE: "admin")
├── app-backend/     (Spring Boot — REST API + PostgreSQL)
└── settings.gradle.kts  (includes: shared, app-customer, app-admin)
```

Shipper code hiện sống trong `app-customer/src/main/java/vn/vuavuive/customer/ui/shipper/`:
- ShipperMainActivity.java, ShipperPagerAdapter.java, ShipperOrderListFragment.java
- ShipperOrderAdapter.java, ShipperOrderDetailActivity.java, ShipperOrderItemAdapter.java

Layout XML: activity_shipper_main.xml, activity_shipper_order_detail.xml, fragment_shipper_order_list.xml, item_shipper_order.xml

---

## PHẦN 1: FIX BUG parseOrderStatus() — QUAN TRỌNG NHẤT

### File: `app-backend/src/main/java/vn/vuavuive/backend/modules/order/OrderService.java`

**BUG HIỆN TẠI** (dòng 387-398):
```java
private Order.OrderStatus parseOrderStatus(String value) {
    if (value == null || value.isBlank() || "all".equalsIgnoreCase(value)) return null;
    String normalized = value.trim().toUpperCase();
    if ("SHIPPED".equals(normalized) || "IN_TRANSIT".equals(normalized)) {
        normalized = "SHIPPING";  // ❌ BUG: IN_TRANSIT là enum hợp lệ, không nên map sang SHIPPING
    }
    if ("PROCESSING".equals(normalized) || "PREPARING".equals(normalized) || "READY_FOR_PICKUP".equals(normalized)) {
        normalized = "CONFIRMED";  // ❌ BUG: PREPARING và READY_FOR_PICKUP là enum hợp lệ
    }
    return Order.OrderStatus.valueOf(normalized);
}
```

**Enum OrderStatus hợp lệ** (trong Order.java):
```java
public enum OrderStatus {
    PENDING, CONFIRMED, SHIPPING, PREPARING, READY_FOR_PICKUP,
    IN_TRANSIT, DELIVERED, FAILED, RETURNED, CANCELLED
}
```

**FIX**: Chỉ map alias bên ngoài, KHÔNG map các enum hợp lệ sang enum khác:
```java
private Order.OrderStatus parseOrderStatus(String value) {
    if (value == null || value.isBlank() || "all".equalsIgnoreCase(value)) return null;
    String normalized = value.trim().toUpperCase();
    // Chỉ map alias legacy — KHÔNG map enum hợp lệ
    if ("SHIPPED".equals(normalized)) normalized = "SHIPPING";
    if ("PROCESSING".equals(normalized)) normalized = "CONFIRMED";
    try {
        return Order.OrderStatus.valueOf(normalized);
    } catch (IllegalArgumentException e) {
        return null;
    }
}
```

---

## PHẦN 2: BACKEND — Liên kết Shipper ↔ User

### 2.1 Sửa Shipper.java — Thêm quan hệ @OneToOne với User

File: `app-backend/src/main/java/vn/vuavuive/backend/modules/shipper/Shipper.java`

Thêm field:
```java
@OneToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", unique = true)
private User user;
```

Hibernate auto DDL sẽ tạo cột `user_id` trong bảng `shippers`.

### 2.2 Sửa ShipperRepository.java — Thêm query

File: `app-backend/src/main/java/vn/vuavuive/backend/modules/shipper/ShipperRepository.java`

Thêm:
```java
Optional<Shipper> findByUserId(UUID userId);
```

### 2.3 Sửa ShipperService.java — createShipper liên kết User

Khi Admin tạo shipper, tự tạo User account với role SHIPPER:

```java
// Inject thêm: UserRepository userRepository, PasswordEncoder passwordEncoder

public ShipperResponse createShipper(ShipperRequest request) {
    if (shipperRepository.existsByPhone(request.phone())) {
        throw AppException.conflict("Số điện thoại shipper đã tồn tại!");
    }
    // Tìm hoặc tạo User account
    User user = userRepository.findByPhone(request.phone()).orElseGet(() -> {
        User newUser = User.builder()
            .phone(request.phone())
            .fullName(request.fullName())
            .role(User.Role.SHIPPER)
            .passwordHash(passwordEncoder.encode("shipper123")) // mật khẩu mặc định
            .isActive(true)
            .build();
        return userRepository.save(newUser);
    });
    // Đảm bảo role là SHIPPER
    if (user.getRole() != User.Role.SHIPPER) {
        user.setRole(User.Role.SHIPPER);
        userRepository.save(user);
    }
    Shipper shipper = Shipper.builder()
        .fullName(request.fullName())
        .phone(request.phone())
        .vehicleNumber(request.vehicleNumber())
        .currentStatus(Shipper.Status.AVAILABLE)
        .isActive(true)
        .user(user)
        .build();
    return toResponse(shipperRepository.save(shipper));
}
```

### 2.4 Thêm endpoint GET /api/shippers/me

Trong ShipperController.java, thêm:
```java
@Operation(summary = "[SHIPPER] Lấy thông tin profile shipper hiện tại")
@GetMapping("/me")
@PreAuthorize("hasRole('SHIPPER')")
public ResponseEntity<ShipperResponse> getMyProfile() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByEmail(email)
        .or(() -> userRepository.findByPhone(email))
        .orElseThrow(() -> AppException.notFound("User"));
    Shipper shipper = shipperRepository.findByUserId(user.getId())
        .or(() -> shipperRepository.findByPhone(user.getPhone()))
        .orElseThrow(() -> AppException.notFound("Shipper profile chưa được thiết lập"));
    return ResponseEntity.ok(toResponse(shipper));
}
```

Inject thêm `UserRepository` vào ShipperController.

### 2.5 Thêm endpoint POST /api/auth/shipper/login

Trong AuthController.java, thêm:
```java
@PostMapping("/shipper/login")
public ResponseEntity<ApiResponse<UserResponse>> shipperLogin(
        @Valid @RequestBody LoginRequest request, HttpServletResponse response) {
    AuthResponse auth = authService.login(request);
    UserResponse user = authService.getUserResponse(request.identifier());
    if (!"shipper".equals(user.role())) {
        throw new AppException(HttpStatus.FORBIDDEN, "Tài khoản không có quyền Shipper");
    }
    setAuthCookie(response, auth.accessToken(), user.role());
    return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
        .success(true).message("Đăng nhập thành công").data(user)
        .accessToken(auth.accessToken()).refreshToken(auth.refreshToken()).build());
}
```

### 2.6 Sửa setAuthCookie trong AuthController — Hỗ trợ shipper

```java
private void setAuthCookie(HttpServletResponse response, String token, String role) {
    String cookieName = "vvv.customer.sid";
    if ("admin".equals(role) || "staff".equals(role) || "audit".equals(role)) {
        cookieName = "vvv.admin.sid";
    } else if ("shipper".equals(role)) {
        cookieName = "vvv.shipper.sid";
    }
    // ... rest unchanged
}
```

### 2.7 Sửa JwtAuthFilter — Đọc cookie shipper

File: `app-backend/src/main/java/vn/vuavuive/backend/security/JwtAuthFilter.java`

Trong `extractToken()`, thêm `vvv.shipper.sid`:
```java
if ("vvv.customer.sid".equals(cookie.getName())
    || "vvv.admin.sid".equals(cookie.getName())
    || "vvv.shipper.sid".equals(cookie.getName())) {
```

### 2.8 Sửa logout trong AuthController — Clear cookie shipper

```java
public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
    clearCookie(response, "vvv.customer.sid");
    clearCookie(response, "vvv.admin.sid");
    clearCookie(response, "vvv.shipper.sid");
    // ...
}
```

### 2.9 Sửa ShipperResponse — Thêm userId

```java
public record ShipperResponse(
    UUID id, String fullName, String phone, String vehicleNumber,
    String currentStatus, Boolean isActive, String userId
) {}
```

Cập nhật toResponse() trong ShipperService:
```java
private ShipperResponse toResponse(Shipper s) {
    return new ShipperResponse(s.getId(), s.getFullName(), s.getPhone(),
        s.getVehicleNumber(), s.getCurrentStatus().name(), s.getIsActive(),
        s.getUser() != null ? s.getUser().getId().toString() : null);
}
```

### 2.10 Sửa OrderService.getShipperOrders — Hỗ trợ lookup qua userId

```java
public PagedResponse<OrderResponse> getShipperOrders(String statusStr, int page, int size) {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByEmail(email)
        .or(() -> userRepository.findByPhone(email))
        .orElseThrow(() -> AppException.notFound("User"));
    Shipper shipper = shipperRepository.findByUserId(user.getId())
        .or(() -> shipperRepository.findByPhone(user.getPhone()))
        .orElseThrow(() -> AppException.notFound("Thông tin tài xế chưa được thiết lập"));
    // ... rest giữ nguyên, dùng shipper.getId()
}
```

---

## PHẦN 3: TẠO MODULE app-shipper

### 3.1 settings.gradle.kts — Thêm include

```kotlin
include(":app-shipper")
```

### 3.2 Tạo app-shipper/build.gradle.kts

Copy từ app-admin, thay đổi:
- `namespace = "vn.vuavuive.shipper"`
- `applicationId = "vn.vuavuive.shipper"`
- `PORTAL_SCOPE = "shipper"`

Dependencies cần thiết (copy từ app-customer):
```kotlin
dependencies {
    implementation(project(":shared"))
    implementation("com.google.dagger:hilt-android:2.56.2")
    annotationProcessor("com.google.dagger:hilt-compiler:2.56.2")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.7")
    implementation("androidx.activity:activity:1.9.3")
    implementation("androidx.fragment:fragment:1.8.3")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    testImplementation("junit:junit:4.13.2")
}
```

### 3.3 Tạo cấu trúc thư mục app-shipper

```
app-shipper/
├── build.gradle.kts
├── proguard-rules.pro   (copy từ app-customer)
└── src/main/
    ├── AndroidManifest.xml
    ├── java/vn/vuavuive/shipper/
    │   ├── VuaVuiVeShipperApp.java
    │   ├── di/
    │   │   └── NetworkModule.java
    │   ├── ui/
    │   │   ├── auth/
    │   │   │   └── ShipperLoginActivity.java
    │   │   ├── main/
    │   │   │   ├── ShipperMainActivity.java
    │   │   │   └── ShipperPagerAdapter.java
    │   │   └── order/
    │   │       ├── ShipperOrderListFragment.java
    │   │       ├── ShipperOrderAdapter.java
    │   │       ├── ShipperOrderDetailActivity.java
    │   │       └── ShipperOrderItemAdapter.java
    │   └── viewmodel/
    │       └── AuthViewModel.java
    └── res/
        ├── layout/   (copy 4 shipper XML từ app-customer + activity_shipper_login.xml)
        ├── values/    (colors.xml, strings.xml, themes.xml)
        ├── drawable/  (copy cần thiết: bg_header_gradient, bg_avatar_circle, bg_badge_status, ic_arrow_back)
        ├── menu/
        └── xml/       (network_security_config.xml)
```

### 3.4 AndroidManifest.xml cho app-shipper

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <application
        android:name=".VuaVuiVeShipperApp"
        android:allowBackup="true"
        android:label="VuaVuiVe Shipper"
        android:networkSecurityConfig="@xml/network_security_config"
        android:supportsRtl="true"
        android:theme="@style/Theme.VuaVuiVe.Shipper"
        android:usesCleartextTraffic="true">
        <activity android:name=".ui.auth.ShipperLoginActivity" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <activity android:name=".ui.main.ShipperMainActivity" android:exported="false" android:launchMode="singleTop" />
        <activity android:name=".ui.order.ShipperOrderDetailActivity" android:exported="false" android:parentActivityName=".ui.main.ShipperMainActivity" />
    </application>
</manifest>
```

### 3.5 VuaVuiVeShipperApp.java

```java
package vn.vuavuive.shipper;
import android.app.Application;
import dagger.hilt.android.HiltAndroidApp;
@HiltAndroidApp
public class VuaVuiVeShipperApp extends Application {}
```

### 3.6 NetworkModule.java cho app-shipper

Chỉ provide các API cần thiết: AuthApi, OrderApi, ShipperOrderApi

```java
package vn.vuavuive.shipper.di;
// Copy structure từ app-customer/di/NetworkModule.java
// PORTAL_SCOPE dùng BuildConfig.PORTAL_SCOPE (= "shipper")
// Chỉ cần provide: SessionManager, AuthApi, OrderApi, ShipperOrderApi
```

### 3.7 ShipperLoginActivity.java

Tạo mới — login đơn giản bằng phone + password, gọi `POST /api/auth/shipper/login`.
Sau login kiểm tra role == "shipper", lưu SessionManager, navigate → ShipperMainActivity.
Layout: tạo `activity_shipper_login.xml` đơn giản với 2 TextInputEditText + nút Login.

### 3.8 Di chuyển Java files

Copy 6 file từ `app-customer/ui/shipper/` sang `app-shipper/`, thay đổi:
- Package name: `vn.vuavuive.customer.ui.shipper` → `vn.vuavuive.shipper.ui.main` hoặc `vn.vuavuive.shipper.ui.order`
- Import R: `vn.vuavuive.customer.R` → `vn.vuavuive.shipper.R`
- Import LoginActivity trong ShipperMainActivity: trỏ về `vn.vuavuive.shipper.ui.auth.ShipperLoginActivity`

### 3.9 Di chuyển Layout files

Copy 4 XML layout files từ `app-customer/res/layout/` sang `app-shipper/res/layout/`.
Giữ nguyên nội dung (không cần thay đổi vì dùng R tự resolve).

### 3.10 Resource files (values/)

**colors.xml** — Copy từ app-customer, giữ các color cần thiết: primary, surface, text_primary, text_secondary, text_hint, divider, error, success, etc.

**strings.xml** — `<string name="app_name">VuaVuiVe Shipper</string>`

**themes.xml** — Copy theme từ app-customer, rename thành `Theme.VuaVuiVe.Shipper`

**drawable/** — Copy: bg_header_gradient.xml, bg_avatar_circle.xml, bg_badge_status.xml, ic_arrow_back.xml

**xml/network_security_config.xml** — Copy từ app-customer

### 3.11 AuthViewModel cho shipper

Tạo simplified AuthViewModel chỉ có login/logout, dùng AuthApi + SessionManager.

---

## PHẦN 4: DỌN DẸP app-customer

### 4.1 Xóa package shipper

Xóa toàn bộ `app-customer/src/main/java/vn/vuavuive/customer/ui/shipper/`

### 4.2 Xóa layout shipper

Xóa: activity_shipper_main.xml, activity_shipper_order_detail.xml, fragment_shipper_order_list.xml, item_shipper_order.xml

### 4.3 Sửa LoginActivity.java

File: `app-customer/src/main/java/vn/vuavuive/customer/ui/auth/LoginActivity.java`

```java
private void goToMain() {
    if (sessionManager.isShipper()) {
        showError("Vui lòng sử dụng ứng dụng VuaVuiVe Shipper");
        sessionManager.clearSession();
        return;
    }
    Intent intent = new Intent(this, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
}
```

Xóa import ShipperMainActivity.

### 4.4 Sửa NetworkModule.java

Xóa `ShipperOrderApi` provider khỏi app-customer/di/NetworkModule.java.
Xóa import `ShipperOrderApi`.

### 4.5 Sửa AndroidManifest.xml

Xóa 2 activity declarations cho ShipperMainActivity và ShipperOrderDetailActivity (dòng 127-137).

---

## PHẦN 5: HOÀN THIỆN ORDER FLOW

### Flow hoàn chỉnh:

```
Customer đặt hàng (PENDING)
    → COD: giữ PENDING, chờ Admin
    → MoMo/VNPay: PENDING → CONFIRMED (auto khi thanh toán thành công)
    
Admin xác nhận (PENDING → CONFIRMED)
Admin chuẩn bị hàng (CONFIRMED → PREPARING)
Admin đóng gói xong (PREPARING → READY_FOR_PICKUP)
Admin gán Shipper (READY_FOR_PICKUP → SHIPPING, set order.shipper = shipper)

Shipper bắt đầu giao (SHIPPING → IN_TRANSIT)
Shipper giao thành công (IN_TRANSIT → DELIVERED)
  HOẶC
Shipper giao thất bại (IN_TRANSIT → FAILED)
```

### 5.1 Sửa ShipperService.assignShipperToOrder

Chỉ cho phép gán shipper khi đơn ở trạng thái CONFIRMED, PREPARING, hoặc READY_FOR_PICKUP (không chỉ kiểm tra paymentStatus):

```java
public void assignShipperToOrder(UUID orderId, UUID shipperId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> AppException.notFound("Đơn hàng"));
    Shipper shipper = shipperRepository.findById(shipperId)
        .orElseThrow(() -> AppException.notFound("Shipper"));
    if (!shipper.getIsActive()) throw AppException.badRequest("Shipper bị khóa");
    
    // Chỉ cho gán khi đơn đã confirmed trở lên, chưa đi giao
    Order.OrderStatus status = order.getStatus();
    if (status != Order.OrderStatus.CONFIRMED
        && status != Order.OrderStatus.PREPARING
        && status != Order.OrderStatus.READY_FOR_PICKUP) {
        throw AppException.badRequest("Chỉ gán shipper cho đơn đã xác nhận (CONFIRMED/PREPARING/READY_FOR_PICKUP)");
    }
    
    order.setShipper(shipper);
    order.setStatus(Order.OrderStatus.SHIPPING);
    orderRepository.save(order);
    appendStatusLog(order, Order.OrderStatus.SHIPPING,
        "Admin gán đơn cho tài xế: " + shipper.getFullName(), "ADMIN", "Hệ thống");
    notifyAdminDashboard(order, "Đã gán tài xế " + shipper.getFullName());
}
```

### 5.2 Sửa ShipperService.updateDeliveryStatus — Validate state transitions

```java
public void updateDeliveryStatus(UUID orderId, UUID shipperId, String newStatusStr, String note) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> AppException.notFound("Đơn hàng"));
    Shipper shipper = shipperRepository.findById(shipperId)
        .orElseThrow(() -> AppException.notFound("Shipper"));
    if (order.getShipper() == null || !order.getShipper().getId().equals(shipperId)) {
        throw AppException.badRequest("Tài xế không được phân công đơn này!");
    }
    Order.OrderStatus newStatus = Order.OrderStatus.valueOf(newStatusStr.toUpperCase());
    
    // Validate transitions
    Order.OrderStatus currentStatus = order.getStatus();
    boolean validTransition = false;
    if (newStatus == Order.OrderStatus.IN_TRANSIT
        && (currentStatus == Order.OrderStatus.SHIPPING
            || currentStatus == Order.OrderStatus.CONFIRMED
            || currentStatus == Order.OrderStatus.PREPARING
            || currentStatus == Order.OrderStatus.READY_FOR_PICKUP)) {
        validTransition = true;
    }
    if ((newStatus == Order.OrderStatus.DELIVERED || newStatus == Order.OrderStatus.FAILED)
        && currentStatus == Order.OrderStatus.IN_TRANSIT) {
        validTransition = true;
    }
    if (!validTransition) {
        throw AppException.badRequest("Không thể chuyển từ " + currentStatus + " sang " + newStatus);
    }
    
    order.setStatus(newStatus);
    if (newStatus == Order.OrderStatus.DELIVERED) {
        order.setPaymentStatus(Order.PaymentStatus.PAID);
        shipper.setCurrentStatus(Shipper.Status.AVAILABLE);
    } else if (newStatus == Order.OrderStatus.FAILED || newStatus == Order.OrderStatus.RETURNED) {
        shipper.setCurrentStatus(Shipper.Status.AVAILABLE);
        for (OrderItem item : order.getOrderItems()) {
            item.getProduct().setStockQuantity(item.getProduct().getStockQuantity() + item.getQuantity());
        }
    } else if (newStatus == Order.OrderStatus.IN_TRANSIT) {
        shipper.setCurrentStatus(Shipper.Status.DELIVERING);
    }
    orderRepository.save(order);
    shipperRepository.save(shipper);
    appendStatusLog(order, newStatus, note, "SHIPPER", shipper.getFullName());
    notifyAdminDashboard(order, "Tài xế " + shipper.getFullName() + " cập nhật: " + newStatusStr);
}
```

### 5.3 Thêm ShipperOrderApi.getMyProfile trong shared module

File: `shared/src/main/java/vn/vuavuive/shared/data/api/ShipperOrderApi.java`

Thêm:
```java
@GET("api/shippers/me")
Call<ApiResponse<Object>> getMyProfile();
```

### 5.4 Thêm POST api/auth/shipper/login trong AuthApi (shared)

```java
@POST("api/auth/shipper/login")
Call<ApiResponse<User>> shipperLogin(@Body LoginRequest body);
```

---

## PHẦN 6: KIỂM TRA VÀ VERIFY

### 6.1 Build all modules

```bash
./gradlew :shared:assembleDebug
./gradlew :app-customer:assembleDebug
./gradlew :app-admin:assembleDebug
./gradlew :app-shipper:assembleDebug
```

### 6.2 Backend build

```bash
cd app-backend
./gradlew bootRun
```

Verify endpoints:
- `POST /api/auth/shipper/login` — returns 200 with shipper role
- `GET /api/shippers/me` — returns shipper profile
- `GET /api/orders/shipper` — returns orders assigned to shipper
- `PUT /api/shippers/{id}/orders/{orderId}/delivery?status=IN_TRANSIT` — updates status

### 6.3 Kiểm tra parseOrderStatus fix

Đảm bảo lọc đơn theo IN_TRANSIT, PREPARING, READY_FOR_PICKUP trả đúng kết quả (không bị map sang status khác).

---

## QUY TẮC QUAN TRỌNG

1. **KHÔNG sửa file nào trong `shared/` module** ngoài việc thêm endpoint mới vào API interface và thêm DTO nếu cần
2. **KHÔNG thay đổi logic của app-admin** — chỉ backend và app-customer/app-shipper
3. **Giữ nguyên tất cả comment/docstring** hiện có trong code
4. **Package name app-shipper**: `vn.vuavuive.shipper`
5. **ApplicationId**: `vn.vuavuive.shipper`
6. **PORTAL_SCOPE**: `"shipper"`
7. **Mọi API call trong app-shipper** phải dùng JWT Bearer token (không dùng cookie trên Android)
8. **Build phải pass** cho cả 3 app modules + backend
9. Sử dụng **cùng version dependencies** như app-customer (xem build.gradle.kts)
10. **Chạy backend và test API** sau khi sửa xong
