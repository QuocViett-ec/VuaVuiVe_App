# Danh Sách Kịch Bản Kiểm Thử (Test Checklist)

Dưới đây là danh sách các kịch bản kiểm thử chi tiết cho các tính năng vừa cập nhật. Bạn có thể sử dụng danh sách này để chạy thử ứng dụng và kiểm tra tính đúng đắn.

---

## 1. Luồng Gán Shipper (Admin App)
- [ ] **Kịch bản 1: Hiển thị trạng thái Shipper mặc định**
  - **Mô tả**: Mở chi tiết một đơn hàng mới tạo (Trạng thái `Pending`).
  - **Kết quả kỳ vọng**: Dưới phần thông tin giao hàng, hiển thị dòng chữ `"Chưa gán shipper"`. Nút **Gán** được kích hoạt (trừ tài khoản có vai trò `audit`).
- [ ] **Kịch bản 2: Hiển thị Dialog chọn Shipper**
  - **Mô tả**: Nhấn vào nút **Gán** shipper.
  - **Kết quả kỳ vọng**: Một Dialog hiển thị danh sách các shipper hiện có trong hệ thống (lọc từ Firebase RTDB node `/users` có `role` là `SHIPPER`). Thông tin hiển thị gồm: Tên Shipper + Email.
- [ ] **Kịch bản 3: Thực hiện gán Shipper thành công**
  - **Mô tả**: Chọn một shipper bất kỳ từ danh sách (ví dụ: `shipper@gmail.com`).
  - **Kết quả kỳ vọng**:
    - Hiển thị Toast thông báo thành công.
    - Trạng thái đơn hàng trên màn hình tự động chuyển sang `Đã xác nhận (Confirmed)`.
    - Thông tin shipper được cập nhật trực tiếp hiển thị tên shipper vừa chọn (thay vì `"Chưa gán shipper"`).
    - Trên Firebase Console node `/orders/{orderId}`, kiểm tra xem các trường `shipperId` và `shipperName` có được cập nhật chính xác hay chưa.

---

## 2. Luồng Nhận & Giao Hàng (Shipper App)
- [ ] **Kịch bản 4: Tải danh sách đơn hàng được gán**
  - **Mô tả**: Đăng nhập vào ứng dụng Shipper bằng tài khoản shipper vừa được gán (ví dụ: `shipper@gmail.com` / `Shipper@123`).
  - **Kết quả kỳ vọng**: Đơn hàng vừa được gán ở bước trước xuất hiện trong danh sách "Cần giao" của shipper.
- [ ] **Kịch bản 5: Chuyển trạng thái giao hàng**
  - **Mô tả**: Shipper nhấn bắt đầu giao đơn hàng.
  - **Kết quả kỳ vọng**: Trạng thái đơn hàng chuyển sang `IN_TRANSIT` (Đang giao hàng). Cả Admin App và Customer App đều cập nhật trạng thái này đồng bộ theo thời gian thực.
- [ ] **Kịch bản 6: Xác nhận giao hàng thành công & Thu tiền COD**
  - **Mô tả**: Đơn hàng thanh toán bằng hình thức **COD**. Shipper nhấn "Xác nhận giao hàng thành công" (DELIVERED).
  - **Kết quả kỳ vọng**:
    - Trạng thái đơn hàng chuyển sang `DELIVERED`.
    - Trạng thái thanh toán của đơn hàng tự động chuyển sang `PAID` (Đã thanh toán) trên Firebase RTDB (`paymentStatus = PAID`).
    - Cả Admin và Khách hàng đều thấy đơn hàng đã hoàn thành và đã thanh toán.

---

## 3. Thống Kê Dashboard Realtime (Admin App)
- [ ] **Kịch bản 7: Tính toán số liệu thống kê doanh thu và đơn hàng**
  - **Mô tả**: Truy cập tab Dashboard trên Admin App.
  - **Kết quả kỳ vọng**:
    - Tổng số đơn hàng (`Total Orders`), Đơn hàng chờ xử lý (`Pending`), Đơn hàng đang giao (`Shipping`) hiển thị đúng số lượng thực tế từ Firebase.
    - Tổng doanh thu (`Total Revenue`) được tính đúng bằng tổng tiền (`finalAmount`) của các đơn hàng có trạng thái `DELIVERED` hoặc `paymentStatus == PAID`.
    - Tổng số người dùng (`Total Users`) hiển thị đúng số lượng tài khoản đăng ký trên Firebase.

---

## 4. Quản Lý Dữ Liệu Firebase (Admin App)
- [ ] **Kịch bản 8: Quản lý người dùng (Users)**
  - **Mô tả**: Xem danh sách người dùng, tìm kiếm theo tên/email, lọc theo role, và cập nhật trạng thái/vai trò của một user.
  - **Kết quả kỳ vọng**: Dữ liệu tải từ Firebase, thay đổi lập tức được đồng bộ lên Firebase `/users`.
- [ ] **Kịch bản 9: Quản lý mã giảm giá (Vouchers)**
  - **Mô tả**: Xem danh sách, tạo mới một voucher, sửa thông tin, hoặc xóa (soft-delete / chuyển `isActive = false`).
  - **Kết quả kỳ vọng**: Đồng bộ hoàn toàn trên Firebase node `/vouchers`.
- [ ] **Kịch bản 10: Xem Lịch Sử Hoạt Động (Audit Logs)**
  - **Mô tả**: Vào phần Audit Logs của Admin để kiểm tra lịch sử thao tác.
  - **Kết quả kỳ vọng**: Các hoạt động thay đổi trạng thái đơn hàng, gán shipper đều được tự động lưu vết vào node `/auditLogs` trên Firebase và hiển thị đầy đủ trên màn hình Admin.
