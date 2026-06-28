# PHẦN 2.3 — LUỒNG DỮ LIỆU APP SHIPPER (SHIPPER DATA FLOW AUDIT)

Ứng dụng Giao hàng (Shipper App) được xây dựng tinh gọn, tương tác trực tiếp với Firebase Realtime Database thông qua `FirebaseShipperRepository` và không có lớp API Retrofit trung gian nào hoạt động.

---

## Bảng Luồng Dữ Liệu Shipper App

| Luồng nghiệp vụ | UI Screen | ViewModel | Repository | Firebase Path | Model/DTO | Ghi chú rủi ro |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Đăng nhập Shipper** | `ShipperLoginActivity` | `ShipperAuthViewModel` | `FirebaseShipperRepository` | Firebase Auth (xác thực)<br> `/users/{uid}` (đọc profile) | `User` | **High**: Đọc vai trò người dùng từ `/users/{uid}/role`. Chỉ cho phép vào ứng dụng nếu role là `SHIPPER`. Nếu không phải, tự động logout. |
| **Bật/Tắt online nhận đơn** | `ShipperMainActivity` | `ShipperOrderViewModel` | `FirebaseShipperRepository` | `/users/{uid}/onlineStatus` (ghi) | Không có | **Medium**: Ghi trạng thái `AVAILABLE` (sẵn sàng giao) hoặc `OFFLINE` trực tiếp lên node thông tin user của shipper. |
| **Tải danh sách đơn giao** | `ShipperOrderListFragment` | `ShipperOrderViewModel` | `FirebaseShipperRepository` | `/orders` (đọc real-time & lắng nghe sự thay đổi) | `Order` | **Critical**: Tải toàn bộ đơn hàng từ `/orders` về và thực hiện lọc phía Client: `uid.equals(order.getShipperId())`. Vì Admin không thể gán `shipperId`, danh sách này sẽ luôn rỗng. |
| **Xem chi tiết đơn giao** | `ShipperOrderDetailActivity` | `ShipperOrderViewModel` | `FirebaseShipperRepository` | `/orders/{orderId}` (đọc) | `Order` | **Critical**: Khi phân tích thông tin khách hàng, app đọc các trường `recipient_name`, `recipient_phone`, và `recipient_address`. Các trường này không tồn tại trong DB (do Customer ghi `delivery_name`...), dẫn tới hiển thị trống. |
| **Nhận đơn (Bắt đầu giao)** | `ShipperOrderDetailActivity` | `ShipperOrderViewModel` | `FirebaseShipperRepository` | `/orders/{orderId}/status` (ghi)<br> `/orders/{orderId}/statusLogs` (ghi) | `Order` | **High**: Cập nhật trạng thái đơn thành `IN_TRANSIT`. Đồng thời tạo bản ghi nhật ký sự kiện vào `/orders/{orderId}/statusLogs` với vai trò là `SHIPPER`. |
| **Hoàn tất giao hàng** | `ShipperOrderDetailActivity` | `ShipperOrderViewModel` | `FirebaseShipperRepository` | `/orders/{orderId}/status` (ghi)<br> `/orders/{orderId}/statusLogs` (ghi)<br> `/orders/{orderId}/updatedAt` (ghi) | `Order` | **High**: Cập nhật trạng thái đơn thành `DELIVERED` và cập nhật mốc thời gian hoàn thành. |
| **Báo cáo giao thất bại** | `ShipperOrderDetailActivity` | `ShipperOrderViewModel` | `FirebaseShipperRepository` | `/orders/{orderId}/status` (ghi)<br> `/orders/{orderId}/failReason` (ghi)<br> `/orders/{orderId}/statusLogs` (ghi) | `Order` | **High**: Cập nhật trạng thái đơn thành `FAILED`, ghi rõ lý do khách quan/chủ quan (ví dụ: Khách không nghe máy) vào trường `failReason`. |
| **Hồ sơ Shipper** | `ShipperProfileFragment` | `ShipperAuthViewModel` | `FirebaseShipperRepository` | `/users/{currentUid}` (đọc) | `User` | **Low**: Hiển thị thông tin cá nhân cơ bản và nút đăng xuất tài khoản. |
