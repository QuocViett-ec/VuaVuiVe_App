# PHẦN 1.1 — TỔNG QUAN KIẾN TRÚC (ARCHITECTURE OVERVIEW)

Tài liệu này trình bày sơ đồ tổ chức, cơ chế giao tiếp và cách chia sẻ dữ liệu giữa 3 ứng dụng Mobile (Customer, Admin, Shipper) thông qua hạ tầng Firebase Auth và Realtime Database.

---

## 1. Kiến trúc Tổng quan (High-Level Architecture)

Hệ thống được thiết kế theo cấu trúc **Client-Server trực tiếp thông qua Firebase (BaaS - Backend-as-a-Service)**, đánh dấu giai đoạn đang chuyển dịch từ hệ thống REST API Spring Boot cũ sang cơ chế Realtime.

```
          ┌────────────────────────────────────────────────────────┐
          │                      Firebase                          │
          │  ┌───────────────────────┐   ┌──────────────────────┐  │
          │  │     Firebase Auth     │   │  Realtime Database   │  │
          │  │  (Email/Pass, Google) │   │     (RTDB Nodes)     │  │
          │  └───────────▲───────────┘   └──────────▲───────────┘  │
          └──────────────┼──────────────────────────┼──────────────┘
                         │                          │
         ┌───────────────┼──────────────────────────┼───────────────┐
         │               │                          │               │
  ┌──────┴──────┐ ┌──────┴──────┐            ┌──────┴──────┐        │
  │  Customer   │ │    Admin    │            │   Shipper   │  ◀─────┼─── Môi trường Client
  │    App      │ │     App     │            │     App     │        │    (Android Apps)
  │ (Hilt,Room) │ │ (Hilt,Sync) │            │ (Hilt,Only) │        │
  └─────────────┘ └─────────────┘            └─────────────┘        │
         │               │                          │               │
         └───────────────┴─────────────┬────────────┴───────────────┘
                                       │
                                       ▼
                       ┌──────────────────────────────┐
                       │           :shared            │
                       │    (DTOs, Utils, Session)    │
                       └──────────────────────────────┘
```

---

## 2. Vai trò của Module `:shared`
Module `:shared` đóng vai trò là xương sống biểu diễn dữ liệu thống nhất cho toàn bộ hệ thống:
1. **Chia sẻ Models (DTOs)**: Định nghĩa một bộ cấu trúc dữ liệu duy nhất (`User.java`, `Product.java`, `Order.java`, `OrderItem.java`, `Voucher.java`, `Shipment.java`) đảm bảo cả 3 ứng dụng đều hiểu cấu trúc đối tượng như nhau khi tương tác với Firebase.
2. **Offline Local Cache**: Chứa cấu trúc Room Database (`AppDatabase.java`) để Customer app lưu trữ dữ liệu giỏ hàng cục bộ.
3. **Session & Utility**: Cung cấp `SessionManager` để quản lý phiên đăng nhập và các `Interceptor` bảo mật (dù hiện tại các interceptor này chủ yếu phục vụ lớp gọi Retrofit cũ).

---

## 3. Kiến trúc Luồng Dữ liệu Đặc trưng của từng App

### 3.1. Customer App — Cơ chế Hybrid Offline-First (Room DB + Firebase Sync)
Ứng dụng Customer áp dụng cơ chế đồng bộ giỏ hàng thông minh:
- **Write**: Khi khách hàng thêm/sửa/xóa sản phẩm trong giỏ hàng, ứng dụng sẽ ghi trực tiếp vào Room DB trước để đảm bảo phản hồi tức thì trên UI (Offline-First).
- **Debounced Sync**: Ngay sau đó, một tiến trình background được kích hoạt sau 500ms (debounced) thông qua `FirebaseCartRepository` để đồng bộ toàn bộ giỏ hàng từ Room lên Firebase node `/carts/{uid}`.
- **Read**: Khi khởi động ứng dụng, nếu đã đăng nhập, app sẽ gọi `syncFromServer()` để tải giỏ hàng từ Firebase lưu vào Room DB.

### 3.2. Admin App — Cơ chế Firebase Adapter & Hybrid Sync Listener
Admin App áp dụng hai cách tiếp cận song song để tích hợp Firebase:
- **API Interfaces Wrapper (Adapter Pattern)**: Các màn hình danh sách sản phẩm và đơn hàng sử dụng cấu trúc Dagger Hilt truyền thống. Thay vì tiêm các HTTP API Client, Hilt cấu hình để tiêm các lớp Firebase Adapter (`FirebaseAdminProductApi`, `FirebaseAdminOrderApi`). Các lớp này thực thi interface Retrofit nhưng thực tế thực hiện truy vấn trực tiếp vào Firebase RTDB và bọc kết quả trả về trong đối tượng `retrofit2.Call` giả lập.
- **Background RAM Sync (MockRepository)**: Đối với các chức năng khác (như Vouchers, Shipments, Users, Audit Logs, Dashboard), ứng dụng sử dụng lớp `MockRepository`. Khi khởi tạo, repository này đăng ký các `ValueEventListener` lắng nghe real-time toàn bộ thay đổi dữ liệu trên Firebase RTDB, tải về lưu trữ trong các mảng `List<T>` trong RAM cục bộ. UI sẽ đọc trực tiếp từ các mảng RAM này để đạt tốc độ hiển thị cực nhanh.

### 3.3. Shipper App — Cơ chế Firebase-Only thuần túy
Shipper App có kiến trúc tinh gọn nhất:
- Không sử dụng Retrofit hay Room DB.
- Chỉ giao tiếp duy nhất qua `FirebaseShipperRepository`.
- Đăng ký lắng nghe trực tiếp sự thay đổi tại `/orders` và lọc dữ liệu đơn hàng ngay tại máy khách theo `shipperId` của tài xế đang đăng nhập.

---

## 4. Cơ chế Xác thực (Authentication Flow)
Hệ thống sử dụng **Firebase Authentication** làm giải pháp xác thực duy nhất:
- **Mã hóa cuộc gọi**: Sau khi đăng nhập thành công vào Firebase Auth bằng Email/Mật khẩu, ID Token của Firebase sẽ được lấy ra và lưu vào `SessionManager` để làm mã xác thực dùng chung.
- **Áp dụng email nội bộ cho số điện thoại**: Khách hàng (Customer) có thể đăng nhập bằng Số điện thoại. Ứng dụng sẽ tự động chuẩn hóa số điện thoại đó thành email dạng `{phone}@vuavuive.local` trước khi gửi lên Firebase Auth.
- **Phân quyền vai trò (Role-based access)**: Khi đăng nhập, ứng dụng sẽ đọc dữ liệu profile tương ứng tại node Realtime Database `/users/{uid}` để kiểm tra quyền `role` (CUSTOMER, ADMIN, SHIPPER, STAFF, AUDIT). Nếu vai trò không khớp với quyền lợi của ứng dụng (ví dụ: Tài khoản SHIPPER cố tình đăng nhập vào Admin App), ứng dụng sẽ tự động thực hiện lệnh `FirebaseAuth.signOut()` và đá người dùng ra ngoài.
