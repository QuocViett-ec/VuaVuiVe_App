# PHẦN 2 — TÁI DỰNG SCHEMA FIREBASE THỰC TẾ (FIREBASE SCHEMA RECONSTRUCTION)

Tài liệu này tổng hợp cấu trúc dữ liệu thực tế đang được sử dụng trong các đoạn mã nguồn Java của cả 3 ứng dụng. Nhiều trường dữ liệu đang bị lệch chuẩn đặt tên (snake_case vs camelCase) hoặc lệch hẳn từ khóa, tạo ra rủi ro mất mát dữ liệu khi chuyển tiếp giữa các app.

---

## Bảng Schema Thực Tế & Phân Tích Rủi Ro Lệch Trường

| Node gốc | Field | Type suy luận | App dùng | Đọc/Ghi | Bắt buộc? | Rủi ro/lệch schema |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **`/users/{uid}`** | `id` | String | Cả 3 App | Đọc/Ghi | Có | Không |
| | `full_name` | String | Customer | Đọc/Ghi | Có | **Critical**: Customer ghi `full_name` nhưng Shipper/Admin (`MockRepository`) đọc `name`. Sẽ bị hiển thị tên trống/null trên màn hình quản lý hoặc shipper. |
| | `name` | String | Admin, Shipper | Đọc/Ghi | Không | (Xem rủi ro của `full_name` ở trên). |
| | `phone` | String | Cả 3 App | Đọc/Ghi | Có | Không |
| | `email` | String | Cả 3 App | Đọc/Ghi | Có | Không |
| | `role` | String | Cả 3 App | Đọc/Ghi | Có | Cần viết in hoa (`CUSTOMER`, `SHIPPER`, `ADMIN`). |
| | `is_active` | Boolean | Customer, Admin | Đọc/Ghi | Có | **High**: Customer/Admin ghi song song cả `is_active` và `isActive`. |
| | `isActive` | Boolean | Admin | Đọc/Ghi | Không | Tránh nhầm lẫn, cần thống nhất dùng một field duy nhất. |
| | `onlineStatus`| String | Shipper | Đọc/Ghi | Không | Trạng thái hoạt động tài xế (`AVAILABLE` / `OFFLINE`). |
| **`/products/{productId}`** | `id` | String | Cả 3 App | Đọc/Ghi | Có | Thường được gán trùng với khóa `{productId}`. |
| | `name` | String | Cả 3 App | Đọc/Ghi | Có | Không |
| | `slug` | String | Customer, Admin | Đọc/Ghi | Có | Dùng để tạo URL thân thiện và lọc danh mục. |
| | `selling_price`| Double | Cả 3 App | Đọc/Ghi | Có | Được ánh xạ về thuộc tính `price` trong DTO `Product`. |
| | `original_price`| Double | Customer, Admin | Đọc/Ghi | Không | Giá vốn/giá gốc chưa giảm. |
| | `category_id` | String | Customer, Admin | Đọc/Ghi | Có | Ánh xạ về thuộc tính `category` trong DTO `Product`. |
| | `stock_quantity`| Integer | Cả 3 App | Đọc/Ghi | Có | **High**: Admin `MockRepository` đọc dự phòng cả `stock` và `stock_quantity`. |
| | `image_url` | String | Cả 3 App | Đọc/Ghi | Không | **High**: Admin `MockRepository` đọc dự phòng cả `image_url` và `imageUrl`. |
| | `is_active` | Boolean | Cả 3 App | Đọc/Ghi | Có | **High**: Đọc dự phòng cả `is_active` và `isActive`. |
| **`/carts/{uid}`** | `updated_at` | String (ISO) | Customer | Đọc/Ghi | Có | Thời điểm cập nhật giỏ hàng lần cuối. |
| | `items/{productId}` | Object (Map) | Customer | Đọc/Ghi | Không | Chứa danh sách sản phẩm trong giỏ: `product_id`, `product_name`, `unit_price`, `quantity`, `subtotal`, `unit`, `image_url`. |
| | `saved_for_later/{productId}` | Object (Map) | Customer | Đọc/Ghi | Không | Cấu trúc giống hệt `items` nhưng để lưu mua sau. |
| **`/orders/{orderUuid}`** | `id` | String | Cả 3 App | Đọc/Ghi | Có | Trùng với `{orderUuid}`. |
| | `order_id` | String | Cả 3 App | Đọc/Ghi | Có | Mã hiển thị có tiền tố `ORD-` (Ví dụ: `ORD-1719468900000`). |
| | `user_id` | String | Cả 3 App | Đọc/Ghi | Có | Mã định danh khách hàng tạo đơn. |
| | `status` | String | Cả 3 App | Đọc/Ghi | Có | Trạng thái đơn. **High**: Customer ghi in hoa (`PENDING`), Shipper cập nhật (`IN_TRANSIT`, `DELIVERED`, `FAILED`), Admin (`MockRepository`) chuyển đổi `.toUpperCase()`. |
| | `delivery_name`| String | Customer, Admin | Đọc/Ghi | Có | **Critical**: Customer ghi `delivery_name`, Admin đọc `delivery_name` / `deliveryName`, nhưng Shipper đọc `recipient_name` / `recipientName`. Sẽ làm màn hình Shipper trống tên người nhận. |
| | `delivery_phone`| String | Customer, Admin | Đọc/Ghi | Có | **Critical**: Customer ghi `delivery_phone`, Admin đọc `delivery_phone` / `deliveryPhone`, nhưng Shipper đọc `recipient_phone` / `recipientPhone`. Trống số điện thoại bên Shipper. |
| | `delivery_address`| String | Customer, Admin | Đọc/Ghi | Có | **Critical**: Customer ghi `delivery_address`, Admin đọc `delivery_address` / `deliveryAddress`, nhưng Shipper đọc `recipient_address` / `recipientAddress` / `deliveryAddress`. Trống địa chỉ giao hàng bên Shipper. |
| | `shipperId` | String | Shipper | Đọc | Không | **Critical**: Shipper app lọc theo `shipperId` hoặc `shipper_id` để lấy đơn hàng của mình. Nhưng Admin app không có chức năng ghi field này vào Order, dẫn tới Shipper không bao giờ nhận được đơn. |
| | `subtotal_amount`| Double | Customer, Admin | Đọc/Ghi | Có | Admin đọc dự phòng cả `subtotal_amount` và `subtotalAmount`. |
| | `shipping_fee` | Double | Customer, Admin | Đọc/Ghi | Có | Admin đọc dự phòng cả `shipping_fee` và `shippingFee`. |
| | `discount_amount`| Double | Customer, Admin | Đọc/Ghi | Có | Admin đọc dự phòng cả `discount_amount` và `discountAmount`. |
| | `final_amount` | Double | Cả 3 App | Đọc/Ghi | Có | **High**: Lệch tên field. Customer/Admin ghi `final_amount`, Shipper đọc dự phòng cả `final_amount`, `finalAmount`, `totalAmount`, `total_amount`. |
| | `payment_method`| String | Cả 3 App | Đọc/Ghi | Có | Gồm `COD` hoặc `MOMO`. Shipper đọc dự phòng `paymentMethod`. |
| | `payment_status`| String | Cả 3 App | Đọc/Ghi | Có | Gồm `UNPAID`, `PAID`, `REFUNDED`. |
| | `items/{productId}` | Map | Cả 3 App | Đọc/Ghi | Có | Chứa các mặt hàng: `product_id` (hoặc `productId`), `product_name` (hoặc `productName`), `unit_price`, `quantity`, `subtotal`, `unit`, `image_url`. |
| | `status_logs/{logUuid}` | Map | Cả 3 App | Đọc/Ghi | Không | **High**: Lịch sử đơn hàng. Customer/Admin ghi `updated_by` và `created_at`, Shipper ghi `changedBy` và `changedAt`. |
| | `return_request`| Object | Customer, Admin | Đọc/Ghi | Không | Chứa `reason`, `status` (PENDING, APPROVED, REJECTED), `admin_note`, `requested_at`. |
| **`/vouchers/{code}`** | `id` | String | Customer, Admin | Đọc/Ghi | Có | Khóa định danh của voucher. |
| | `code` | String | Customer, Admin | Đọc/Ghi | Có | Mã giảm giá (Ví dụ: `VUIVE10`). Trùng với node `{code}`. |
| | `value` | Double | Customer, Admin | Đọc/Ghi | Có | Số tiền hoặc phần trăm giảm. |
| | `type` | String | Customer, Admin | Đọc/Ghi | Có | Kiểu giảm (`fixed` hoặc `percentage`). |
| | `isActive` | Boolean | Customer, Admin | Đọc/Ghi | Có | Trạng thái hoạt động của voucher. |
| **`/shipments/{shipmentId}`**| `id` | String | Admin | Đọc/Ghi | Có | Trùng với `{shipmentId}`. |
| | `orderId` | String | Admin | Đọc/Ghi | Có | **High**: Đọc/ghi dự phòng cả `orderId` và `order_id`. |
| | `customerId` | String | Admin | Đọc/Ghi | Có | **High**: Đọc/ghi dự phòng cả `customerId` và `customer_id`. |
| | `trackingNumber`| String | Admin | Đọc/Ghi | Có | Mã vận đơn. Đọc dự phòng `tracking_number`. |
| | `shippingFee` | Double | Admin | Đọc/Ghi | Có | Phí giao hàng. Đọc dự phòng `shipping_fee`. |
| | `currentStatus`| String | Admin | Đọc/Ghi | Có | Trạng thái vận chuyển (`pending`, `processing`, `shipping`, etc.). |
| **`/audit_logs/{logId}`** | `id` | String | Admin | Ghi | Có | Mã log hoạt động của admin. |
| | `timestamp` | String | Admin | Ghi | Có | Thời gian diễn ra hoạt động. |
| | `operatorName` | String | Admin | Ghi | Có | Tên người thực hiện hoạt động. |
| | `action` | String | Admin | Ghi | Có | Tên hoạt động (Ví dụ: "Xóa sản phẩm"). |
| | `details` | String | Admin | Ghi | Có | Chi tiết hoạt động. |
