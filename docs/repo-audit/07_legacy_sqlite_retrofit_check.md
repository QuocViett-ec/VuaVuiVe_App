# PHẦN 4 — KIỂM TRA DẤU VẾT HỆ THỐNG CŨ (LEGACY SYSTEMS & MOCK DATA CHECK)

Trong quá trình chuyển dịch (migrate) từ hệ thống SQLite/Room DB nội bộ và Spring Boot REST API sang BaaS Firebase, dự án vẫn còn sót lại nhiều tệp mã nguồn cũ, cơ sở dữ liệu mẫu và các tệp cung cấp dữ liệu giả (Mock Data) gây phình to dự án và làm tăng độ nhiễu thông tin.

---

## Bảng Phân Tích Thành Phần Cũ và Hướng Xử Lý

| File / Thư mục | Dấu vết cũ | App/Module | Còn được gọi không? | Hướng xử lý | Lý do |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **`vuavuive_v2.db`** (Thư mục gốc) | File cơ sở dữ liệu SQLite vật lý | Gốc repo | Không | **Xóa sau** | Đây là tệp DB SQLite cũ của dự án khi chạy offline. Hiện tại dữ liệu đã chuyển lên Firebase RTDB nên tệp này chiếm dung lượng không cần thiết. |
| **`vn.vuavuive.customer.data.repository`** (`ProductRepository`, `OrderRepository`, `CartRepository`, `CategoryRepository`, `AuthRepository`) | Các Repository gọi Retrofit API | `:app-customer` | Không | **Xóa ngay** | Các ViewModel tương ứng đã chuyển hẳn sang gọi các Repository Firebase mới (`ProductRepositoryFirebase`, `FirebaseOrderRepository`, etc.). Các file này hoàn toàn dư thừa. |
| **`vn.vuavuive.customer.data.MockDataProvider`** | Chứa danh sách dữ liệu giả dạng cứng (hardcoded) | `:app-customer` | Không | **Xóa ngay** | File này chứa hàng trăm dòng khai báo sản phẩm và danh mục giả bằng tiếng Việt để test offline thuở ban đầu. |
| **`vn.vuavuive.shared.data.api`** (Toàn bộ package với 19 interfaces) | Định nghĩa các Retrofit Endpoints | `:shared` | **Có** | **Tạm giữ** | Dù Customer và Shipper không gọi, Admin App vẫn đang tham chiếu tới các Interface này trong `NetworkModule` và các lớp Firebase Mock API (`FirebaseAuthApi`, `FirebaseProductApi`). |
| **`vn.vuavuive.shared.data.local`** (`ProductDao.java`, `ProductEntity.java`) | Room DAO và Entity cho Sản phẩm | `:shared` | Không | **Xóa ngay** | Ứng dụng Customer không còn cache sản phẩm vào Room DB nữa mà lấy trực tiếp thời gian thực từ Firebase. |
| **`vn.vuavuive.shared.data.local`** (`CartDao.java`, `CartItemEntity.java`, `AppDatabase.java`) | Room DAO và Entity cho Giỏ hàng | `:shared` | **Có** | **Giữ lại** | Được sử dụng trong Customer App (`FirebaseCartRepository`) để làm bộ đệm giỏ hàng offline-first. Đây là tính năng cần giữ. |
| **`app-admin/NetworkModule.java`** (Cấu hình Retrofit providers cho 6 APIs) | Cấu hình tiêm Retrofit client cho User, Voucher, Shipment, Dashboard, Audit Log | `:app-admin` | **Có** (bởi Dagger Hilt) | **Sửa đổi** | Hilt vẫn khởi tạo các Retrofit Client này dù giao diện UI đã chuyển hướng gọi sang `MockRepository`. Cần viết lại DI cung cấp các lớp Firebase Mock tương ứng để ngắt kết nối Retrofit hoàn toàn. |
| **`app-admin/data/repository/MockRepository.java`** (`initMockData()`) | Dữ liệu khởi tạo voucher, sản phẩm, đơn hàng giả | `:app-admin` | **Có** | **Sửa đổi** | Hàm này nạp cứng các voucher và sản phẩm giả vào RAM khi khởi tạo app. Cần xóa dữ liệu nạp cứng này, chỉ dựa hoàn toàn vào dữ liệu đồng bộ từ Firebase. |
