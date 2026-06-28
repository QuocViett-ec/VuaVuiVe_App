# PHẦN 6 — SƠ ĐỒ HỆ THỐNG MERMAID (SYSTEM DIAGRAMS)

Tài liệu này chứa 5 sơ đồ luồng dữ liệu và kiến trúc được thiết kế bằng ngôn ngữ Mermaid Markdown để chèn trực tiếp vào báo cáo đồ án của bạn.

---

## 1. Sơ đồ Kiến trúc Tổng thể (Overall Architecture Diagram)

Sơ đồ mô tả tương tác giữa 3 ứng dụng khách và dịch vụ đám mây Firebase Auth / Realtime Database, thông qua việc kế thừa module `:shared`.

```mermaid
flowchart TD
    subgraph Firebase["Đám mây Firebase (BaaS)"]
        Auth["Firebase Auth\n(Xác thực tài khoản)"]
        RTDB["Realtime Database\n(RTDB Node: /users, /products, /orders, /carts)"]
    end

    subgraph Clients["Thiết bị di động Android"]
        CustomerApp["app-customer\n(Hilt, Room Local DB)"]
        AdminApp["app-admin\n(Hilt, MockRepository Cache)"]
        ShipperApp["app-shipper\n(Hilt, Firebase-Only)"]
    end

    subgraph Codebase[":shared Module"]
        SharedDTOs["Shared DTOs & Models\n(User, Product, Order, etc.)"]
    end

    %% Các mối quan hệ tương tác
    CustomerApp -->|1. Xác thực điện thoại/email| Auth
    CustomerApp -->|2. Đồng bộ giỏ hàng / Tạo đơn| RTDB
    CustomerApp -.->|Kế thừa cấu trúc| SharedDTOs

    AdminApp -->|1. Xác thực tài khoản Admin| Auth
    AdminApp -->|2. Quản lý sản phẩm & đơn hàng| RTDB
    AdminApp -.->|Kế thừa cấu trúc| SharedDTOs

    ShipperApp -->|1. Xác thực tài khoản Shipper| Auth
    ShipperApp -->|2. Lắng nghe đơn & Cập nhật trạng thái| RTDB
    ShipperApp -.->|Kế thừa cấu trúc| SharedDTOs
```

---

## 2. Luồng Dữ liệu Sản phẩm của Khách hàng (Customer Product Data Flow)

Sơ đồ mô tả cách Customer App hiển thị danh mục, tải sản phẩm, lọc danh mục và xem chi tiết sản phẩm.

```mermaid
flowchart LR
    RTDB_Cat["Firebase /categories"] -->|1. Tải danh mục| CatRepo["CategoryRepositoryFirebase"]
    CatRepo -->|2. Trả về LiveData| CatVM["CategoryViewModel"]
    CatVM -->|3. Hiển thị UI| HomeFrag["HomeFragment (UI)"]

    RTDB_Prod["Firebase /products"] -->|4. Tải danh sách| ProdRepo["ProductRepositoryFirebase"]
    ProdRepo -->|5. Trả về LiveData| ProdVM["ProductViewModel"]
    ProdVM -->|6. Lọc & Sắp xếp trên RAM| ProductFrag["ProductListFragment (UI)"]
    
    ProductFrag -->|7. Chọn sản phẩm| DetailAct["ProductDetailActivity (UI)"]
```

---

## 3. Luồng Tạo đơn hàng & Thanh toán (Customer Checkout / Order Creation Flow)

Sơ đồ mô tả quy trình checkout phức tạp của Khách hàng, bao gồm kiểm tra tồn kho bằng Transaction và cơ chế Rollback khi xảy ra lỗi.

```mermaid
flowchart TD
    Start["1. Khách bấm 'Đặt hàng' tại CheckoutActivity"] --> CheckStock{"2. Trừ kho tuần tự qua Transaction\ntại /products/{id}/stock_quantity"}
    
    CheckStock -- SUCCESS --> WriteOrder["3. Tạo node đơn hàng mới\ntại /orders/{orderUuid}"]
    CheckStock -- FAIL / Hết hàng --> Rollback["3. Rollback: Phục hồi lại số lượng kho\ncủa các sản phẩm đã trừ trước đó"]
    
    Rollback --> ShowError["4. Hiển thị lỗi hết hàng lên màn hình"]
    
    WriteOrder --> ClearCart["4. Xóa giỏ hàng cục bộ (Room DB)"]
    ClearCart --> SyncCart["5. Đồng bộ giỏ hàng rỗng lên /carts/{uid}"]
    SyncCart --> End["6. Chuyển sang màn hình kết quả đặt hàng"]
```

---

## 4. Luồng Quản lý Đơn hàng của Admin (Admin Order Management Flow)

Sơ đồ mô tả luồng kiểm duyệt đơn hàng, xuất báo cáo CSV và tự động tạo vận đơn (Shipment) của Quản trị viên.

```mermaid
flowchart TD
    Start["Admin mở AdminOrderListFragment"] --> LoadOrders["1. Tải toàn bộ đơn hàng từ /orders"]
    LoadOrders --> Filter["2. Lọc trạng thái / Xuất file báo cáo CSV"]
    
    Filter --> UpdateStatus["3. Admin duyệt đơn hàng (Chọn CONFIRMED)"]
    UpdateStatus --> WriteDB["4. Cập nhật trạng thái /orders/{orderId}/status"]
    
    WriteDB --> CreateShipment["5. Hệ thống tự động tạo Vận đơn mới\ntại /shipments/{shipId}"]
    CreateShipment --> End["6. Đơn sẵn sàng cho Shipper lấy hàng"]
```

---

## 5. Luồng Cập nhật Trạng thái Giao hàng của Shipper (Shipper Delivery Status Flow)

Sơ đồ mô tả luồng cập nhật đơn hàng của tài xế khi giao hàng, bao gồm cập nhật trạng thái đơn và ghi nhật ký statusLogs.

```mermaid
flowchart TD
    Start["Shipper đăng nhập, bật Online nhận đơn"] --> LoadOrders["1. Tải danh sách đơn hàng được gán\n(shipperId == currentUid)"]
    
    LoadOrders --> PickOrder["2. Shipper bấm 'Bắt đầu giao' (IN_TRANSIT)"]
    PickOrder --> WriteTransit["3. Ghi /orders/{id}/status = IN_TRANSIT\nvà ghi log vào /statusLogs"]
    
    WriteTransit --> DeliveryCheck{"4. Giao hàng cho khách hàng"}
    
    DeliveryCheck -- THÀNH CÔNG --> Delivered["5. Ghi /orders/{id}/status = DELIVERED\nvà ghi log thành công"]
    DeliveryCheck -- THẤT BẠI --> Failed["5. Chọn lý do thất bại\nGhi status = FAILED & failReason\nvà ghi log thất bại"]
```
