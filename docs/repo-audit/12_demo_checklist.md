# PHẦN 9 — DANH SÁCH KIỂM TRA CHUẨN BỊ DEMO (DEMO CHECKLIST)

Tài liệu này cung cấp checklist 10 bước kiểm tra kỹ thuật và nghiệp vụ liên thông giữa 3 ứng dụng trước khi nộp đồ án hoặc thực hiện buổi demo trước hội đồng.

---

## Bảng Checklist Kiểm Tra Trước Khi Demo

| STT | Bước kiểm tra | Nội dung kiểm tra chi tiết | Trạng thái mong muốn | App liên quan | Xác nhận |
| :--- | :--- | :--- | :--- | :--- | :---: |
| **1** | **Xác thực Firebase Config** | Kiểm tra toàn bộ tệp `google-services.json` ở cả 3 app có trỏ chung về một Firebase Project. | Cả 3 ứng dụng đọc/ghi dữ liệu trên cùng một Realtime Database URL. | Cả 3 App | `[ ]` |
| **2** | **Kiểm tra tài khoản Test** | Tạo sẵn ít nhất 3 tài khoản trong Firebase: 1 Customer, 1 Admin, 1 Shipper với vai trò (role) chính xác ghi tại node `/users/{uid}/role`. | - Đăng nhập phân quyền chính xác.<br>- Tài khoản sai role bị từ chối đăng nhập. | Cả 3 App | `[ ]` |
| **3** | **Quản lý danh mục & sản phẩm** | Admin chỉnh sửa một sản phẩm (ví dụ đổi giá từ 35k thành 30k) hoặc tạo sản phẩm mới. | - Khách hàng mở app thấy giá sản phẩm cập nhật tức thì.<br>- Lọc danh mục hiện đúng sản phẩm. | Customer, Admin | `[ ]` |
| **4** | **Kiểm tra giỏ hàng offline** | Khách hàng thêm sản phẩm vào giỏ hàng, tắt mạng (chế độ máy bay) $\rightarrow$ tắt app $\rightarrow$ mở lại app $\rightarrow$ bật mạng. | - Giỏ hàng vẫn giữ nguyên (đọc từ Room DB).<br>- Đồng bộ lên Firebase `/carts/{uid}` thành công khi có mạng. | Customer | `[ ]` |
| **5** | **Khấu trừ tồn kho (Checkout)** | Khách hàng chọn mua sản phẩm với số lượng 2. Xem số lượng tồn kho của sản phẩm trước và sau khi đặt hàng. | - Số lượng tồn kho giảm đúng bằng 2.<br>- Node đơn hàng mới được tạo dưới `/orders` chứa danh sách sản phẩm. | Customer | `[ ]` |
| **6** | **Hủy đơn & hoàn kho** | Khách hàng bấm "Hủy đơn" tại màn hình chi tiết đơn hàng (chỉ thực hiện khi đơn là PENDING/CONFIRMED). | - Trạng thái đơn chuyển thành `CANCELLED`.<br>- Số lượng tồn kho của sản phẩm tự động tăng trả lại như ban đầu. | Customer | `[ ]` |
| **7** | **Admin duyệt đơn & gán Shipper** | Admin mở danh sách đơn, chọn đơn hàng mới, duyệt đơn (CONFIRMED) và **chọn gán Shipper**. | - Trạng thái đơn chuyển CONFIRMED.<br>- Node đơn hàng xuất hiện trường `shipperId` ghi UID của Shipper được chọn.<br>- Node `/shipments` tự động sinh mã vận đơn. | Admin | `[ ]` |
| **8** | **Shipper nhận đơn giao** | Shipper đăng nhập app, chuyển trạng thái sang Online. | - Đơn hàng vừa được gán xuất hiện trong danh sách đơn của Shipper.<br>- Hiển thị đầy đủ, chính xác địa chỉ và sđt người nhận. | Shipper | `[ ]` |
| **9** | **Shipper giao hàng thành công** | Shipper bấm "Bắt đầu giao" (IN_TRANSIT) $\rightarrow$ bấm "Giao thành công" (DELIVERED). | - Trạng thái đơn tại Firebase và Customer App cập nhật real-time sang `DELIVERED`.<br>- Nhật ký sự kiện ghi đúng log hoạt động của Shipper. | Customer, Shipper | `[ ]` |
| **10** | **Shipper giao thất bại (Fallback)** | Shipper giao đơn hàng khác, chọn "Giao thất bại" và chọn lý do (ví dụ: Khách không nghe máy). | - Trạng thái đơn chuyển sang `FAILED`.<br>- Màn hình Admin và Customer hiển thị rõ lý do thất bại. | Cả 3 App | `[ ]` |
