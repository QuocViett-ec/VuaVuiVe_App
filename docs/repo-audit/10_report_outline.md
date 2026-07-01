# PHẦN 7 — ĐỀ XUẤT CẤU TRÚC BÁO CÁO ĐỒ ÁN (REPORT OUTLINE)

Dưới đây là khung sườn (outline) chi tiết để bạn viết báo cáo đồ án tốt nghiệp hoặc báo cáo môn học "Vựa Vui Vẻ". Khung sườn này được tối ưu hóa dựa trên thực tế cấu trúc mã nguồn đã được audit.

---

## Bố Cục Báo Cáo Đề Xuất

### CHƯƠNG 1: GIỚI THIỆU ĐỀ TÀI & HỆ THỐNG
* **1.1. Lý do chọn đề tài**: Xu hướng thương mại điện tử m-commerce, nhu cầu mua sắm thực phẩm sạch, nông sản trực tiếp từ vựa.
* **1.2. Giới thiệu hệ thống "Vựa Vui Vẻ"**: Hệ sinh thái di động gồm 3 ứng dụng chuyên biệt hỗ trợ khép kín chuỗi cung ứng từ nông trại đến khách hàng tiêu dùng.
* **1.3. Phạm vi đề tài**: Nghiên cứu phát triển ứng dụng di động trên nền tảng hệ điều hành Android sử dụng Java và công nghệ Firebase.

### CHƯƠNG 2: MỤC TIÊU & PHÂN QUYỀN HỆ THỐNG
* **2.1. Mục tiêu ứng dụng**:
  - Tối ưu hóa thời gian đặt hàng và vận chuyển.
  - Quản lý tồn kho real-time tránh hiện tượng lệch kho (out of stock).
  - Tích hợp chatbot hỗ trợ khách hàng và xuất báo cáo kiểm toán hoạt động.
* **2.2. Phân quyền người dùng (Role-based Access)**:
  - **Khách hàng (Customer)**: Đăng ký/đăng nhập (chuẩn hóa điện thoại), xem sản phẩm, quản lý giỏ hàng offline, đặt hàng, thanh toán online (MoMo, ZaloPay), phản hồi đánh giá.
  - **Quản trị viên (Admin/Staff/Audit)**: Dashboard thống kê doanh thu, quản lý danh mục, sản phẩm, duyệt đơn hàng và yêu cầu trả hàng, phân quyền shipper.
  - **Tài xế giao hàng (Shipper)**: Cập nhật trạng thái online, nhận đơn được gán, định vị và cập nhật trạng thái giao hàng (Thành công/Thất bại kèm lý do).

### CHƯƠNG 3: THIẾT KẾ KIẾN TRÚC & CƠ SỞ DỮ LIỆU
* **3.1. Kiến trúc tổng thể hệ thống**: Trình bày mô hình multi-module của Android Gradle. Giải thích vai trò của module `:shared` chứa các DTOs dùng chung và Room local database.
* **3.2. Thiết kế Cơ sở dữ liệu Firebase Realtime Database**:
  - Trình bày sơ đồ cấu trúc cây JSON của database (vẽ các node chính: `/users`, `/products`, `/categories`, `/carts`, `/orders`, `/shipments`).
  - Giải thích thiết kế cây dữ liệu phẳng (flat tree) phục vụ tối ưu hóa truy vấn real-time của Firebase.
* **3.3. Quy tắc bảo mật dữ liệu (Firebase Security Rules)**: Phân tích file `database.rules.json`. Làm rõ cơ chế chặn đọc/ghi trái phép tại các node `/orders` và `/carts` dựa trên ID của tài khoản đăng nhập.

### CHƯƠNG 4: LUỒNG NGHIỆP VỤ & PHÁT TRIỂN ỨNG DỤNG
* **4.1. Mô tả chi tiết ứng dụng app-customer**: Luồng đồng bộ giỏ hàng Room-Firebase, giải thuật Transaction trừ kho tuần tự và cơ chế Rollback an toàn khi checkout lỗi.
* **4.2. Mô tả chi tiết ứng dụng app-admin**: Cách thức tích hợp Firebase Mock API giả lập Retrofit Call qua DI (Dagger Hilt) và cơ chế RAM sync của `MockRepository`.
* **4.3. Mô tả chi tiết ứng dụng app-shipper**: Cơ chế Firebase-Only, tối ưu hóa giao diện nút ấn cho tài xế ngoài đường, cập nhật trạng thái đơn kèm statusLogs lịch trình.

### CHƯƠNG 5: KIỂM THỬ LUỒNG DỮ LIỆU VÀ ĐÁNH GIÁ
* **5.1. Kịch bản kiểm thử luồng chính (End-to-End Testing)**: Quy trình kiểm thử liên hoàn: Customer tạo đơn $\rightarrow$ Admin duyệt đơn và tạo vận đơn $\rightarrow$ Shipper nhận đơn và giao thành công.
* **5.2. Kết quả kiểm toán tính nhất quán dữ liệu (Cross-App Consistency)**:
  - Nêu rõ các phát hiện lỗi kỹ thuật lệch trường dữ liệu (`full_name` vs `name`, `delivery_name` vs `recipientName`).
  - Nêu rõ vấn đề thiếu chức năng gán Shipper từ Admin dẫn tới lỗi gãy luồng.
  - Trình bày phương án vá lỗi đã đề xuất.

### CHƯƠNG 6: TỔ CHỨC NHÓM & PHÂN CÔNG CÔNG VIỆC
* Trình bày bảng phân chia công việc cho 5 thành viên trong nhóm dựa trên các module Gradle và luồng chức năng thực tế của repo.

### CHƯƠNG 7: KẾT LUẬN & HƯỚNG PHÁT TRIỂN
* **7.1. Hạn chế hiện tại**: Tồn tại mã nguồn cũ chưa dọn dẹp, xử lý tìm kiếm và lọc dữ liệu còn làm trên client-side (chưa tận dụng index database tối đa).
* **7.2. Hướng phát triển**: Tích hợp Firebase Cloud Functions để tự động hóa gán shipper, tích hợp Google Maps API dẫn đường cho Shipper, chuyển hoàn toàn sang kiến trúc Clean Architecture Kotlin.
