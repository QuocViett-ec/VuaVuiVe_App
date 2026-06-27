# PHẦN 8 — PHÂN CHIA CÔNG VIỆC NHÓM (TEAM TASK BREAKDOWN)

Để triển khai dự án hiệu quả, công việc được phân chia cụ thể cho 5 thành viên (Member 1 đến Member 5) dựa trên các cấu trúc module độc lập trong Gradle và phân tích lỗi hiện trạng.

---

## Bảng Phân Chia Công Việc Nhóm 5 Người

| Thành viên | Phụ trách | File/Module liên quan | Công việc cụ thể | Output cần nộp | Mức ưu tiên |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Member 1** *(Leader / Firebase)* | Hạ tầng Firebase, bảo mật & Gradle | Root project, `database.rules.json`, Gradle files | - Sửa rules Firebase khớp với `shipperId` thay vì `shipper_id`.<br>- Gom quản lý phiên bản dependencies Gradle về file root.<br>- Dọn dẹp mã nguồn Retrofit cũ của Customer App. | - File rules bảo mật mới.<br>- Gradle build hoàn chỉnh không cảnh báo xung đột.<br>- Code sạch không file thừa. | **High** |
| **Member 2** *(Android Customer)* | Phát triển & tối ưu ứng dụng Khách hàng | `:app-customer` | - Tối ưu hóa bộ lọc và phân trang sản phẩm trên Client.<br>- Giám sát và kiểm tra cơ chế rollback kho khi checkout lỗi mạng.<br>- Sửa lỗi lệch trường `full_name` để đồng bộ thêm trường `name`. | - Màn hình Home, Cart, Checkout hoạt động mượt mà.<br>- Khớp trường thông tin user name với Admin/Shipper. | **High** |
| **Member 3** *(Android Admin)* | Phát triển ứng dụng Quản trị & Vá lỗi API | `:app-admin` | - Sửa `MockRepository` đảm bảo thêm/sửa/xóa sản phẩm phải ghi đè lên Firebase `/products`.<br>- **Bổ sung UI gán Shipper cho Đơn hàng** (ghi đè `shipperId` vào Order).<br>- Tách màn hình Voucher, User, Shipment khỏi MockRepository và tiêm Firebase API tương ứng. | - Giao diện gán Shipper hoạt động thực tế trên app Admin.<br>- Admin ghi đè dữ liệu sản phẩm lên Firebase thành công.<br>- Giao diện quản lý danh mục mới. | **Critical** |
| **Member 4** *(Android Shipper)* | Phát triển ứng dụng Giao hàng & Đồng bộ | `:app-shipper` | - **Sửa lỗi lệch trường thông tin người nhận** trong `FirebaseShipperRepository` (`delivery_address` $\rightarrow$ `recipientAddress`...).<br>- Tối ưu hóa UI danh sách đơn hàng cho Shipper, kiểm tra tính năng lọc đơn theo ID tài xế. | - Ứng dụng Shipper nhận được đơn hàng được gán từ Admin.<br>- Hiển thị đầy đủ tên, địa chỉ, sđt người nhận. | **Critical** |
| **Member 5** *(Tester / QA / Writer)* | Kiểm thử liên thông & Viết tài liệu | Toàn bộ dự án & folder `/docs` | - Thiết lập Firebase local emulator để test nội bộ.<br>- Thực hiện kiểm thử tích hợp liên thông 3 app (End-to-End).<br>- Soạn thảo báo cáo đồ án tốt nghiệp theo đề xuất Outline. | - Bản log kiểm thử lỗi liên thông.<br>- Bản thảo báo cáo đồ án hoàn chỉnh file Word/PDF. | **Medium** |
