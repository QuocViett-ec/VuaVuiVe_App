# Tài liệu Luồng Thanh toán MoMo - Dự án VuaVuiVe

Tài liệu này tổng hợp cấu trúc thư mục, danh sách các file liên quan và chi tiết luồng thanh toán MoMo hiện tại của hệ thống **VuaVuiVe** (bao gồm ứng dụng Android Client và Spring Boot Backend).

---

## 1. Cấu trúc thư mục liên quan đến MoMo

Dưới đây là cây thư mục rút gọn biểu diễn các file xử lý luồng thanh toán MoMo:

```text
VuaVuiVe/
├── app-backend/                                    # Spring Boot Backend
│   └── src/main/
│       ├── java/vn/vuavuive/backend/
│       │   ├── config/
│       │   │   ├── AppConfig.java                  # Cấu hình chung cho RestTemplate
│       │   │   └── FirebaseConfig.java              # Cấu hình kết nối Firebase Realtime DB
│       │   └── modules/
│       │       ├── order/
│       │       │   ├── Order.java                  # Model Order (được đồng bộ với Firebase)
│       │       │   ├── OrderRepository.java        # Ghi/Đọc dữ liệu Order qua Firebase REST API
│       │       │   └── OrderService.java           # Xử lý logic nghiệp vụ của Order
│       │       └── payment/
│       │           ├── MomoController.java         # REST Controller định nghĩa API MoMo (Sandbox & Mock)
│       │           ├── PaymentController.java      # REST Controller tổng hợp các cổng thanh toán
│       │           ├── MoMoService.java            # Logic xử lý gọi API MoMo & IPN Callback
│       │           ├── PaymentTransaction.java     # Model lưu lịch sử giao dịch thanh toán
│       │           ├── PaymentTransactionRepository.java # Lưu trữ giao dịch vào Firebase
│       │           └── dto/                        # Các DTO truyền nhận dữ liệu MoMo
│       │               ├── CreateMomoPaymentRequest.java
│       │               ├── CreateMomoPaymentResponse.java
│       │               ├── MomoCreateRequest.java
│       │               ├── MomoCreateResponse.java
│       │               └── MomoIpnRequest.java
│       └── resources/
│           └── application-dev.yml                 # File cấu hình môi trường Dev/Sandbox MoMo
│
├── app-customer/                                   # Android Client App
│   └── src/main/java/vn/vuavuive/customer/
│       ├── data/repository/
│       │   ├── FirebaseOrderRepository.java        # Repository trực tiếp viết/đọc Firebase (Luồng Mock hiện tại)
│       │   └── OrderRepository.java                # Repository gọi API qua Spring Boot REST (Retrofit)
│       ├── ui/checkout/
│       │   ├── CheckoutActivity.java               # Màn hình thanh toán & chọn phương thức
│       │   ├── PaymentResultActivity.java          # Màn hình chờ kết quả & mở Deep Link / Web
│       │   └── PaymentWebViewActivity.java         # Webview nhúng trang thanh toán
│       └── viewmodel/
│           └── OrderViewModel.java                 # ViewModel kết nối View và Repository
│
└── shared/                                         # Module dùng chung cho Client
    └── src/main/java/vn/vuavuive/shared/
        └── data/
            ├── api/
            │   └── PaymentApi.java                 # Định nghĩa các Route gọi API thanh toán của Retrofit
            └── dto/                                # DTO dùng chung trên Mobile
                ├── CreateMomoPaymentRequest.java
                ├── CreateMomoPaymentResponse.java
                └── PaymentStatusResponse.java
```

---

## 2. Chi tiết các file liên quan chính

### A. Phía Backend (`app-backend`)

1. **`application-dev.yml`**:
   - Chứa thông tin cấu hình Sandbox của MoMo (`partner-code`, `access-key`, `secret-key`, các URLs nhận kết quả: `redirect-url`, `ipn-url`).
   - Có flag `mock-mode` để chạy thử nghiệm giả lập (khi không cấu hình sandbox key thật).

2. **`MoMoService.java`**:
   - `createMomoPayment(...)`: Tạo phiên thanh toán. Nếu `mock-mode = true`, trả về URL trỏ tới mock view của backend. Nếu `false`, thực hiện ký mã SHA256 và gọi API MoMo Sandbox thật.
   - `handleMomoIpn(...)`: Nhận webhook thông báo kết quả giao dịch từ MoMo (IPN), kiểm tra chữ ký (`signature`) hợp lệ và cập nhật trạng thái đơn hàng trên Firebase thành `PAID` và `CONFIRMED`.
   - `handleMockResult(...)`: Dùng khi chạy giả lập mock để cập nhật trực tiếp trạng thái đơn hàng mà không cần API MoMo thật.

3. **`MomoController.java` & `PaymentController.java`**:
   - Cung cấp các endpoint:
     - `/api/payments/momo` hoặc `/api/momo/create-payment` (POST): Yêu cầu thanh toán.
     - `/api/payments/momo/ipn` (POST): Nhận callback từ MoMo.
     - `/api/payments/{orderId}/status` (GET): Lấy trạng thái thanh toán hiện tại của đơn hàng.

---

### B. Phía Client App (`app-customer`)

1. **`CheckoutActivity.java`**:
   - Nơi người dùng bấm đặt hàng và chọn phương thức thanh toán `momo`.
   - Khi tạo đơn hàng thành công, gọi `orderViewModel.createMomoPayment(...)` để lấy thông tin liên kết thanh toán (`payUrl`, `deeplink`).

2. **`PaymentResultActivity.java`**:
   - Nhận `payment_url` và `deeplink` từ màn hình checkout.
   - Mở ứng dụng MoMo bằng Deep Link (`momo://...`) hoặc mở trình duyệt ngoài qua `payUrl`.
   - Khi quay lại ứng dụng (`onResume`), gọi `refreshOrder()` kiểm tra trạng thái đơn hàng trên Firebase để thông báo thành công/thất bại và xóa giỏ hàng.

3. **`PaymentWebViewActivity.java`**:
   - Màn hình Webview dùng để tải trực tiếp trang thanh toán nếu không muốn mở trình duyệt ngoài. Nhận diện URL trả về từ cổng để tự động đóng Webview và kiểm tra kết quả.

---

## 3. Phân tích Luồng Hoạt động (Flow) Hiện tại

Hiện tại trong mã nguồn đang tồn tại **hai luồng hoạt động độc lập**:

### Luồng A: Luồng Mock trực tiếp trên Firebase (Luồng đang active trên App)
*Do `OrderViewModel` đang được Inject bằng `FirebaseOrderRepository` thay vì Retrofit `OrderRepository`:*

1. Người dùng bấm **Đặt hàng** bằng MoMo tại `CheckoutActivity`.
2. App gọi `FirebaseOrderRepository.createMomoPayment(...)`.
3. Repository này cập nhật trạng thái đơn hàng thẳng lên Firebase Realtime DB thành `PAID` và `CONFIRMED`, đồng thời trả về một link giả lập (`https://example.com/api/payments/momo/return?status=PAID&orderId=...`).
4. `PaymentResultActivity` được kích hoạt và mở link giả lập.
5. Khi người dùng quay lại app, `refreshOrder()` đọc trạng thái `payment_status` từ Firebase (đã đổi thành `PAID` ở bước 3) và hiển thị "Thanh toán thành công" lập tức mà **không cần qua Backend xử lý giao dịch hay API MoMo**.

---

### Luồng B: Luồng Sandbox thông qua Spring Boot Backend
*Đây là luồng chuẩn cần được tích hợp đầy đủ:*

1. Người dùng bấm **Đặt hàng** bằng MoMo tại `CheckoutActivity`.
2. App gọi API REST của Backend thông qua Retrofit `OrderRepository.createMomoPayment(...)`.
3. Backend nhận yêu cầu tại `PaymentController` / `MomoController`, gửi thông tin giao dịch sang cổng thanh toán MoMo Sandbox (hoặc giao diện mock web của backend).
4. MoMo trả về liên kết thanh toán và Deep Link thật.
5. Client App nhận thông tin và mở ứng dụng MoMo (hoặc WebView) để người dùng thực hiện quét mã / nhập mật khẩu thanh toán.
6. Sau khi thanh toán xong:
   - MoMo điều hướng người dùng quay lại app (`redirect-url` / Return URL).
   - MoMo gửi một HTTP POST (IPN webhook) đến Backend `/api/payments/momo/ipn`.
7. Backend kiểm tra chữ ký IPN, xác thực số tiền và cập nhật trạng thái đơn hàng trên Firebase.
8. Client App lắng nghe sự kiện thay đổi trạng thái realtime từ Firebase để cập nhật giao diện thành công cho người dùng.

---

## 4. Đề xuất Hướng Điều chỉnh Flow

Để hoàn thiện luồng thanh toán MoMo chạy qua Backend và MoMo Sandbox thật:

1. **Điều chỉnh trên App Customer**:
   - Chuyển đổi `OrderViewModel` sử dụng Retrofit `OrderRepository` thay vì `FirebaseOrderRepository` để gọi API Backend tạo link MoMo thật.
   - Cấu hình ứng dụng xử lý đúng kết quả trả về từ `PaymentWebViewActivity` hoặc `PaymentResultActivity` thông qua kiểm tra trạng thái từ API Backend (`/api/payments/{orderId}/status`).

2. **Cấu hình Ngrok cho IPN**:
   - MoMo Sandbox cần gọi webhook qua HTTPS công khai. Do đó, cần cấu hình địa chỉ IPN trong `application-dev.yml` trỏ tới Ngrok của bạn (ví dụ: `https://<subdomain>.ngrok-free.app/api/payments/momo/ipn`).
