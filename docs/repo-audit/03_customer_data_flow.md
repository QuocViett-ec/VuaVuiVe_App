# PHẦN 2.1 — LUỒNG DỮ LIỆU APP CUSTOMER (CUSTOMER DATA FLOW AUDIT)

Bảng dưới đây phân tích chi tiết luồng đi của dữ liệu từ giao diện người dùng (UI Screen), qua ViewModel điều phối, đến Repository xử lý và đích đến là các node trên Firebase Realtime Database/Auth cho ứng dụng Khách hàng (Customer App).

---

## Bảng Luồng Dữ Liệu Customer App

| Luồng nghiệp vụ | UI Screen | ViewModel | Repository | Firebase Path | Model/DTO | Ghi chú rủi ro |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Đăng ký tài khoản** | `RegisterActivity` | `AuthViewModel` | `FirebaseUserRepository` | `/users/{uid}` (ghi)<br>Firebase Auth (tạo user) | `RegisterRequest`, `User` | **Medium**: Gửi OTP bằng API Resend. Nếu API key bị lỗi hoặc hết hạn, OTP không gửi được nhưng app sẽ có fallback ghi log để vượt qua. |
| **Đăng nhập** | `LoginActivity` | `AuthViewModel` | `FirebaseUserRepository` | `/users/{uid}` (đọc)<br>Firebase Auth (xác thực) | `LoginRequest`, `User` | **High**: Có cơ chế chuẩn hóa số điện thoại thành `{phone}@vuavuive.local`. Nếu người dùng đăng nhập bằng email thường, cần tránh bị chuẩn hóa nhầm. |
| **Tải danh mục** | `HomeFragment` | `CategoryViewModel` | `CategoryRepositoryFirebase` | `/categories` (đọc) | `CategoryResponse` | **Low**: Dữ liệu danh mục tương đối tĩnh, tải một lần rồi lưu vào danh sách hiển thị. |
| **Tải danh sách sản phẩm** | `ProductListFragment`, `HomeFragment` | `ProductViewModel` | `ProductRepositoryFirebase` | `/products` (đọc) | `Product` | **Medium**: Lọc và sắp xếp (giá, đánh giá, mới nhất) hoàn toàn được xử lý trên client-side sau khi tải toàn bộ sản phẩm. Nếu số lượng sản phẩm lớn sẽ làm giảm hiệu năng. |
| **Lọc sản phẩm theo danh mục** | `ProductListFragment` | `ProductViewModel` | `ProductRepositoryFirebase` | `/products` & `/categories` (đọc) | `Product` | **High**: Category filter khớp danh mục bằng cách ánh xạ từ slug của category sang `category_id` trong sản phẩm. Nếu Admin tạo sản phẩm không gán đúng `category_id` mà gán slug sẽ bị lỗi hiển thị. |
| **Xem chi tiết sản phẩm** | `ProductDetailActivity` | `ProductViewModel` | `ProductRepositoryFirebase` | `/products/{productId}` (đọc)<br> `/reviews` (đọc) | `Product`, `Review` | **Low**: Hiển thị mô tả sản phẩm và danh sách bình luận kèm theo điểm rating. |
| **Thêm vào giỏ hàng** | `ProductDetailActivity`, `CartFragment` | `CartViewModel` | `FirebaseCartRepository` | Room DB (ghi cục bộ)<br> `/carts/{uid}/items` (đồng bộ ghi) | `CartItemEntity` | **High**: Ghi trực tiếp vào Room DB cục bộ trước, sau đó kích hoạt tiến trình background đồng bộ lên Firebase sau 500ms. Có thể bị mất đồng bộ nếu tắt app đột ngột trước khi sync hoàn tất. |
| **Cập nhật số lượng giỏ hàng** | `CartFragment` | `CartViewModel` | `FirebaseCartRepository` | Room DB (ghi cục bộ)<br> `/carts/{uid}/items` (đồng bộ ghi) | `CartItemEntity` | **High**: Nếu số lượng sản phẩm cập nhật về `<= 0`, Room DB sẽ xóa record và Firebase tương ứng cũng bị xóa nút sản phẩm đó. |
| **Tạo đơn hàng (Checkout)** | `CheckoutActivity` | `OrderViewModel` | `FirebaseOrderRepository` | `/orders/{orderUuid}` (ghi)<br> `/products/{productId}/stock_quantity` (ghi giao dịch) | `CreateOrderRequest`, `Order` | **Critical**: Giảm tồn kho sản phẩm bằng Firebase Transaction tuần tự. Nếu có lỗi mạng giữa chừng khi đang giảm kho của danh sách sản phẩm, hệ thống sẽ kích hoạt rollback hoàn lại kho cho các sản phẩm đã trừ. |
| **Xem lịch sử đơn hàng** | `OrderListFragment`, `OrderDetailActivity` | `OrderViewModel` | `FirebaseOrderRepository` | `/orders` (đọc & lọc client theo `user_id`) | `Order` | **Medium**: Việc lọc theo `user_id` và sắp xếp ngày tạo đơn diễn ra trên Client-side. Về lâu dài cần tối ưu bằng indexing của Firebase. |
| **Hủy đơn hàng** | `OrderDetailActivity` | `OrderViewModel` | `FirebaseOrderRepository` | `/orders/{orderUuid}` (ghi)<br> `/products/{productId}/stock_quantity` (hoàn kho) | `Order` | **Critical**: Hủy đơn chỉ được phép khi trạng thái là `PENDING` hoặc `CONFIRMED`. Hệ thống tự động phục hồi lại kho số lượng sản phẩm bằng Transaction và đánh dấu `stock_restored = true`. |
| **Xem/Sửa hồ sơ cá nhân** | `ProfileFragment`, `EditProfileActivity` | `AuthViewModel` | `FirebaseUserRepository` | `/users/{uid}` (ghi) | `User` | **High**: Ghi đè tên vào trường `full_name` tại Firebase. Hãy nhớ rằng Shipper và Admin đang đọc trường `name`. |
