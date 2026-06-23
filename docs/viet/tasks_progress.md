# ✅ TASKS_PROGRESS — VuaVuiVe

> File này là bản copy từ `tasks.md` đã thêm tiến độ thực tế ở đầu file.  
> Chỉ tick các mục đã được xác nhận tương đối chắc trong quá trình làm milestone.  
> Các mục chưa manual test đầy đủ vẫn giữ `[ ]` để tránh overclaim.

## Tiến độ nhanh

- [x] M1 Backend startup + public API health
- [x] M2 Backend Auth + JWT + roles
- [x] M3 Android Customer Auth UI
- [x] M4 Customer MainActivity shell
- [x] M5 Home screen + product grid
- [x] Bugfix checkpoint: cart controls/search exit/stock safety
- [ ] M6 ProductDetailActivity manual verification

## Bugfix checkpoint update - 17/06/2026

- [x] Build passed: `.\gradlew.bat :app-customer:assembleDebug`
- [x] Cart minus no longer deletes item at quantity 1
- [x] Cart plus/minus/delete controls are visible and distinct
- [x] ProductDetail blocks add-to-cart when stock <= 0 and clamps quantity after bind
- [x] Home/ProductList/SearchActivity can clear active search with Android back
- [x] `item_product.xml` and `item_recipe.xml` inspected; both are item layouts with no back arrow
- [ ] Manual device retest: cart controls, stock=0 product, search exit

## Remaining milestones

- [ ] M7 Cart full flow
- [ ] M8 Checkout
- [ ] M9 Orders
- [ ] M10 Shipper UI
- [ ] M11 Account/profile extras
- [ ] M12 Admin login/dashboard
- [ ] M13 Remaining admin screens
- [ ] M14 Full integration audit

---

# 🔍 VuaVuiVe — Quét Toàn Bộ Đồ Án & Phân Chia Task

> **Ngày tạo:** 14/06/2026 | **Cập nhật:** 17/06/2026 | **Modules:** `app-admin`, `app-customer` (Customer + Shipper), `app-backend`, `shared`

---

## 📊 TỔNG QUAN KIẾN TRÚC

| Module | Số file Java | Vai trò |
|--------|-------------|---------|
| `app-admin` | 25 files | Android app quản trị (Mock data, không gọi API) |
| `app-customer` | 57 files | Android app khách hàng + Shipper (Hilt DI, Retrofit API) |
| `app-backend` | 65 files | Spring Boot REST API (PostgreSQL, JWT, VNPay/MoMo) |
| `shared` | 53 files | DTO, API interfaces, Room DB, Utils dùng chung |

---

# 🅰️ MODULE: APP-ADMIN (Mock Data)

## 1. AdminLoginActivity
- **Flow:** Chọn role từ Spinner → Auto-fill email/password → `performLogin()` → `MockRepository.adminLogin()` → Nếu thành công → `MainActivity`
- **Roles:** Admin, Staff, Audit, User (bị từ chối)

### ✅ Checklist Test
- [ ] Chọn role Admin → đăng nhập thành công
- [ ] Chọn role Staff → đăng nhập thành công
- [ ] Chọn role Audit → đăng nhập thành công
- [ ] Chọn role User → hiện Toast "từ chối"
- [ ] Để trống email/password → hiện Toast lỗi

## 2. MainActivity (Admin)
- **Flow:** Check `currentUser` → nếu null → logout → Setup BottomNav 5 tabs: Dashboard, Orders, Products, Vouchers, Chatbot
- **Functions:** `replaceFragment()`, `navigateToMenu()`, `logout()`

### ✅ Checklist Test
- [ ] Đăng nhập → hiển thị role badge đúng trên toolbar
- [ ] Chuyển tab Dashboard/Orders/Products/Vouchers/Chatbot OK
- [ ] Nhấn Logout → quay về Login, clear session

## 3. DashboardFragment
- **Flow:** Hiển thị 4 metrics (đơn, doanh thu, users, pending) + RecyclerView pending orders (3) + low stock (3)
- **Shortcuts:** Users (block Staff), Shipments, Audit (block Staff), Reports (export CSV)
- **Functions:** `loadDashboardData()`, `setupListeners()`, `showExportDialog()`, `exportCsv()`

### ✅ Checklist Test
- [ ] Hiển thị đúng 4 metrics
- [ ] Danh sách pending orders hiện tối đa 3
- [ ] Danh sách low stock (stock ≤ 10) hiện tối đa 3
- [ ] Swipe refresh hoạt động
- [ ] "Xem tất cả" → chuyển đúng tab
- [ ] Staff nhấn Users → Toast "không có quyền"
- [ ] Staff nhấn Audit → Toast "không có quyền"
- [ ] Export CSV → lưu file vào Downloads
- [ ] Audit export đơn hàng → bị chặn
- [ ] Audit export sản phẩm → bị chặn
- [ ] Staff export users → bị chặn

## 4. AdminOrderListFragment
- **Flow:** 7 tabs filter (All/Pending/Confirmed/Shipping/Delivered/Cancelled/Returns) + Search realtime + Export CSV + Bulk update
- **Functions:** `setupTabs()`, `applyFilters()`, `showBulkUpdateDialog()`, `exportFilteredOrdersCsv()`

### ✅ Checklist Test
- [ ] Mỗi tab filter đúng status
- [ ] Search theo mã đơn, tên KH, SĐT
- [ ] Long press → multi-select → bulk update status
- [ ] Audit không được bulk update
- [ ] Audit không được export CSV
- [ ] Click đơn → mở `AdminOrderDetailActivity`
- [ ] Swipe refresh load lại data

## 5. AdminOrderDetailActivity
- **Flow:** Load order by ID → Bind header/customer/items/payment/price → Status Spinner → Return request
- **Functions:** `loadOrderDetails()`, `setupStatusSpinner()`, `setupReturnRequest()`

### ✅ Checklist Test
- [ ] Hiển thị đúng mã đơn, ngày tạo
- [ ] Hiển thị đúng tên/SĐT/địa chỉ KH
- [ ] Hiển thị danh sách sản phẩm + giá
- [ ] Hiển thị phương thức thanh toán + trạng thái
- [ ] Hiển thị breakdown: subtotal + ship - discount = total
- [ ] Đổi status qua Spinner → cập nhật thành công
- [ ] Audit: Spinner bị disable (read-only)
- [ ] Nút "Đã thanh toán" hiện khi chưa paid, ẩn khi paid
- [ ] Audit: ẩn nút "Đã thanh toán"
- [ ] Đơn return_requested → hiện panel Approve/Reject
- [ ] Audit: nút approve/reject bị disable

## 6. AdminProductListFragment
- **Flow:** Spinner filter category + Search + Chip low stock + FAB add + Export CSV
- **Functions:** `setupSpinner()`, `applyFilters()`, `exportProductsCsv()`

### ✅ Checklist Test
- [ ] Filter theo từng category hoạt động
- [ ] Search theo tên/subcategory/tags
- [ ] Chip "Low Stock" filter stock ≤ 10
- [ ] Click product → mở ProductEditActivity
- [ ] Long press → xác nhận xóa → xóa thành công
- [ ] Audit: long press → Toast "không có quyền xóa"
- [ ] FAB thêm mới → Audit bị chặn
- [ ] Export CSV → Audit bị chặn

## 7. ProductEditActivity
- **Flow:** Load existing hoặc tạo mới → Validate → Save
- **Functions:** `loadExistingProduct()`, `saveProduct()`, `enforceRolePermissions()`

### ✅ Checklist Test
- [ ] Tạo mới: hiện "THÊM SẢN PHẨM MỚI"
- [ ] Sửa: load đúng data vào form
- [ ] Validate: tên trống, giá ≤ 0, stock âm, unit trống → error
- [ ] Giá gốc < giá bán → error
- [ ] Chọn ảnh mẫu hoạt động
- [ ] Save thành công → quay lại list
- [ ] Audit: tất cả input disabled, nút = "READ ONLY"

## 8. VoucherListFragment + VoucherEditActivity
- **Flow:** List vouchers + FAB add (chỉ Admin) + Click edit

### ✅ Checklist Test
- [ ] Hiển thị danh sách voucher
- [ ] Admin: FAB hiện, Staff/Audit: FAB ẩn
- [ ] Staff/Audit click voucher → Toast "Read-Only" nhưng vẫn mở
- [ ] Tạo mới: validate code, value, dates, duplicate check
- [ ] Percent type: hiện trường Cap
- [ ] Ngày hết hạn trước ngày bắt đầu → error
- [ ] Non-admin: tất cả input disabled

## 9. UserListFragment
- **Flow:** Search + Toggle active/inactive + Click → change role dialog + Export CSV

### ✅ Checklist Test
- [ ] Staff → bị chặn hoàn toàn, popBackStack
- [ ] Search theo tên/email/SĐT
- [ ] Toggle active/inactive → cập nhật thành công
- [ ] Audit toggle → bị chặn, reload undo
- [ ] Click user → dialog đổi role (Admin only)
- [ ] Audit click user → bị chặn
- [ ] Export CSV → Audit bị chặn

## 10. ShipmentListFragment + ShipmentDetailActivity
- **Flow:** 2 spinners (carrier + status) filter → Click → Detail → Spinner status + note → Save

### ✅ Checklist Test
- [ ] Filter theo carrier (internal/external)
- [ ] Filter theo status (pending/shipping/delivered/failed)
- [ ] Click → mở detail đúng data
- [ ] Detail: hiển thị tracking, carrier, orderID, ETA
- [ ] Visual timeline hiển thị đúng
- [ ] Đổi status + nhập note → save thành công
- [ ] Note trống → error
- [ ] Audit: spinner + note disabled, nút = "READ ONLY"

## 11. AuditLogFragment
- **Flow:** Load audit logs từ MockRepository → RecyclerView

### ✅ Checklist Test
- [ ] Staff → bị chặn hoàn toàn
- [ ] Admin/Audit: hiển thị danh sách logs
- [ ] Swipe refresh hoạt động

## 12. AdminChatFragment (AI Chatbot)
- **Flow:** Greeting → Chips gợi ý → Gõ/click query → Rule-based response → Quick replies

### ✅ Checklist Test
- [ ] Greeting hiện tên user
- [ ] Chip "Tổng quan" → báo cáo doanh thu
- [ ] Chip "Sắp hết hàng" → danh sách low stock
- [ ] Chip "Đơn chờ xử lý" → danh sách pending
- [ ] Chip "Đơn giao trễ" → shipments đang giao
- [ ] Chip "Nguy cơ hủy" → phân tích rủi ro
- [ ] Gõ mã đơn (VD: ord-9843a) → chi tiết đơn
- [ ] Gõ câu bất kỳ → fallback message
- [ ] Quick reply buttons hoạt động

---

# 🅱️ MODULE: APP-CUSTOMER

## 1. LoginActivity
- **Flow:** Validate → `authViewModel.login()` → API call → `SessionManager` check role → Shipper → `ShipperMainActivity`, Customer → `MainActivity`

### ✅ Checklist Test
- [ ] Để trống → error "Vui lòng nhập"
- [ ] Đăng nhập sai → hiện error message
- [ ] Đăng nhập đúng customer → vào MainActivity
- [ ] Đăng nhập đúng shipper → vào ShipperMainActivity
- [ ] Đã đăng nhập → skip login
- [ ] Link "Đăng ký" → mở RegisterActivity
- [ ] Link "Quên mật khẩu" → mở ForgotPasswordActivity

## 2. RegisterActivity
- **Flow:** Validate (tên ≥ 2, phone VN pattern, pass ≥ 6, confirm match) → API register → auto login → MainActivity

### ✅ Checklist Test
- [ ] Tên < 2 ký tự → error
- [ ] SĐT không đúng format VN → error
- [ ] Mật khẩu < 6 → error
- [ ] Xác nhận không khớp → error
- [ ] Đăng ký thành công → vào MainActivity
- [ ] Link "Đăng nhập" → quay lại

## 3. MainActivity (Customer)
- **Flow:** Check session → sync cart → Setup Navigation (Home/Cart/Orders/Account) + Cart badge count

### ✅ Checklist Test
- [x] Guest: vào được, user = null
- [ ] Logged in: sync cart từ server
- [ ] Cart badge hiện đúng số lượng
- [x] Chuyển tab OK
- [ ] Deep-link "navigate_to=orders" → chuyển tab Orders

## 4. HomeFragment
- **Flow:** Greeting + Address picker + Search (debounce 400ms) + Shortcuts + Recipes section + Products grid (Mock + API)

### ✅ Checklist Test
- [ ] Greeting hiện tên user hoặc "VỰA VUI VẺ"
- [ ] Đổi địa chỉ giao hàng qua dialog
- [x] Search → filter products + auto scroll
- [x] Shortcuts (Flash Sale, Bánh, Mì, Bia, Sữa) → filter local theo keyword/discount
- [ ] Recipe chips filter theo category
- [ ] Click recipe → RecipeDetailActivity
- [ ] "Mua nguyên liệu" → thêm vào cart
- [x] Product grid hiển thị 2 cột
- [x] Click product → ProductDetailActivity
- [x] Nút "+" thêm vào cart + Toast
- [x] Promo popup hiện 1 lần duy nhất

## 5. ProductDetailActivity
- **Flow:** Load mock → bind → try API → Image/Price/Rating/Stock/Description + Quantity control + Add to cart + Reviews + Similar products

### ✅ Checklist Test
- [ ] Hiển thị ảnh, tên, giá, giá gốc, % giảm
- [ ] Rating bar + số đánh giá
- [ ] Số đã bán + tồn kho
- [ ] Hết hàng → FAB disabled
- [ ] Tăng/giảm số lượng, không vượt stock
- [ ] Add to cart → Toast + recommend event
- [ ] Hiện danh sách reviews
- [ ] Hiện sản phẩm tương tự (cùng category)

## 6. CartFragment
- **Flow:** RecyclerView cart items + Swipe delete + Saved items section + Subtotal + Checkout button

### ✅ Checklist Test
- [ ] Hiển thị danh sách items trong cart
- [ ] Tăng/giảm số lượng
- [ ] Swipe left → xóa item
- [ ] Save for later / Move to cart
- [ ] Tổng tiền cập nhật realtime
- [ ] Cart trống → hiện empty state + "Mua sắm ngay"
- [ ] Checkout → chưa login → redirect Login
- [ ] Checkout → đã login → mở CheckoutActivity

## 7. CheckoutActivity
- **Flow:** Validate delivery info → Build order request → Chọn payment (COD/VNPay/MoMo) → API createOrder → Redirect payment/finish

### ✅ Checklist Test
- [ ] Chưa login → redirect Login + finish
- [ ] Thiếu tên/SĐT/địa chỉ → Toast error
- [ ] Cart trống → Toast error
- [ ] Hiển thị subtotal, ship (30k), discount, total
- [ ] COD → đặt hàng → clear cart → finish
- [ ] VNPay → mở PaymentWebViewActivity
- [ ] MoMo → mở PaymentWebViewActivity
- [ ] Voucher code được gửi lên server

## 8. OrderListFragment
- **Flow:** 5 tabs (All/Pending/Shipping/Delivered/Cancelled) → API getOrders → RecyclerView + Swipe refresh

### ✅ Checklist Test
- [ ] Hiển thị danh sách đơn hàng
- [ ] Filter theo tab đúng
- [ ] Click đơn → OrderDetailActivity
- [ ] Swipe refresh
- [ ] Empty state khi không có đơn

## 9. OrderDetailActivity
- **Flow:** Load order → Bind header/delivery/payment/items → Action buttons (Cancel/Return/Review)

### ✅ Checklist Test
- [ ] Hiển thị mã đơn, status, ngày đặt
- [ ] Hiển thị thông tin giao hàng
- [ ] Hiển thị danh sách sản phẩm
- [ ] Hiển thị phương thức thanh toán + tổng tiền
- [ ] Pending/Confirmed → nút "Hủy đơn" hiện
- [ ] Delivered → nút "Trả hàng" + "Đánh giá" hiện
- [ ] Hủy đơn → confirm dialog → API cancel
- [ ] Trả hàng → nhập lý do → API return
- [ ] Đánh giá → chọn SP → ReviewBottomSheet

## 10. AccountFragment
- **Flow:** Observer user → bind info + Menu items (Edit Profile, Change Pass, Orders, Recipes, Shipments, Reviews, Chat) + Logout

### ✅ Checklist Test
- [ ] Guest: hiện placeholder + "Đăng nhập"
- [ ] Logged in: hiện tên/SĐT/email/avatar
- [ ] Click avatar (guest) → Login
- [ ] Edit Profile → EditProfileActivity
- [ ] Change Password → ChangePasswordActivity  
- [ ] My Orders → OrderListFragment
- [ ] Recipes → RecipeListFragmentActivity
- [ ] Shipments → ShipmentListActivity
- [ ] My Reviews → MyReviewsActivity
- [ ] Chat → ChatActivity
- [ ] Tất cả menu yêu cầu login (trừ Recipes, Chat)
- [ ] Logout → clear session → Login

---

# 🚚 MODULE: SHIPPER (trong app-customer)

## 1. ShipperMainActivity
- **Flow:** Header (tên tài xế) + Toggle Online/Offline + Tabs: "Cần giao" + "Lịch sử" + Logout

### ✅ Checklist Test
- [ ] Hiển thị tên shipper từ SessionManager
- [ ] Toggle Online/Offline → đổi label + màu
- [ ] Tab "Cần giao" + "Lịch sử" hoạt động
- [ ] Logout → clear session → LoginActivity

## 2. ShipperOrderListFragment
- **Flow:** API `getMyShipperOrders()` → Filter active (PREPARING/IN_TRANSIT/READY_FOR_PICKUP) hoặc history (DELIVERED/FAILED/RETURNED)

### ✅ Checklist Test
- [ ] Tab Active: hiện đơn PREPARING/IN_TRANSIT/READY_FOR_PICKUP
- [ ] Tab History: hiện đơn DELIVERED/FAILED/RETURNED
- [ ] Swipe refresh
- [ ] Empty state khi không có đơn
- [ ] Error state khi API lỗi
- [ ] Click đơn → ShipperOrderDetailActivity

## 3. ShipperOrderDetailActivity
- **Flow:** API getOrderDetail → Bind info + Quick Call + Navigate (Google Maps) + Action buttons theo status

### ✅ Checklist Test
- [ ] Hiển thị mã đơn, ngày, status badge
- [ ] Hiển thị tên/SĐT/địa chỉ khách
- [ ] Hiển thị note (nếu có)
- [ ] Quick Call → Intent DIAL
- [ ] Navigate → Google Maps
- [ ] PREPARING → nút "Bắt đầu giao" → IN_TRANSIT
- [ ] IN_TRANSIT → nút "Đã giao" → DELIVERED
- [ ] IN_TRANSIT → nút "Thất bại" → FAILED
- [ ] DELIVERED/FAILED → label hoàn thành, ẩn buttons
- [ ] Hiển thị danh sách sản phẩm
- [ ] Hiển thị tổng tiền + phương thức thanh toán

---

# 🔧 MODULE: APP-BACKEND (Spring Boot)

## Endpoints chính

| Endpoint | Method | Auth | Mô tả |
|----------|--------|------|-------|
| `/api/auth/register` | POST | No | Đăng ký |
| `/api/auth/login` | POST | No | Đăng nhập |
| `/api/auth/refresh` | POST | No | Refresh token |
| `/api/products` | GET | No | Danh sách SP (phân trang, filter) |
| `/api/products/{id}` | GET | No | Chi tiết SP |
| `/api/products/{id}/reviews` | GET | No | Reviews SP |
| `/api/orders` | POST | Yes | Tạo đơn |
| `/api/orders/my` | GET | Yes | Đơn của tôi |
| `/api/orders/{id}` | GET | Yes | Chi tiết đơn |
| `/api/orders/{id}/cancel` | PATCH | Yes | Hủy đơn |
| `/api/orders/shipper` | GET | SHIPPER | Đơn của shipper |
| `/api/orders/{id}/status` | PATCH | ADMIN | Update status |
| `/api/categories` | CRUD | ADMIN | Quản lý danh mục |
| `/api/payments/vnpay` | POST | Yes | Tạo URL VNPay |
| `/api/payments/momo` | POST | Yes | Tạo URL MoMo |
| `/api/shippers` | CRUD | ADMIN | Quản lý shipper |
| `/api/chat` | POST | Yes | AI Chat (Gemini) |
| `/api/recipes` | GET | No | Công thức |

---

# 👥 PHÂN CHIA 4 TASK — SONG SONG, KHÔNG CONFLICT

> **Nguyên tắc:** Mỗi người làm việc trên files hoàn toàn khác nhau → **ZERO conflict**.

---

## 🔴 TASK 1 — NẶNG: App-Admin (Toàn bộ module admin)
**Thành viên:** Người 1

### Scope (files chỉ người này chạm vào)
```
app-admin/src/main/java/vn/vuavuive/admin/**
app-admin/src/main/res/**
```

### Việc cần làm
1. Test toàn bộ **12 màn hình admin** theo checklist ở trên
2. Fix bugs nếu có (Mock data, role permissions, UI)
3. Đảm bảo RBAC đúng: Admin full quyền, Staff hạn chế, Audit read-only
4. Test export CSV hoạt động
5. Test chatbot AI responses

### Files chính cần review/fix
- `AdminLoginActivity.java` — Login flow
- `DashboardFragment.java` — Dashboard metrics + shortcuts
- `AdminOrderListFragment.java` + `AdminOrderDetailActivity.java` — Orders CRUD
- `AdminProductListFragment.java` + `ProductEditActivity.java` — Products CRUD
- `VoucherListFragment.java` + `VoucherEditActivity.java` — Vouchers CRUD
- `UserListFragment.java` — User management
- `ShipmentListFragment.java` + `ShipmentDetailActivity.java` — Shipments
- `AuditLogFragment.java` — Audit logs
- `AdminChatFragment.java` — AI Chatbot
- `MockRepository.java` — Data source

---

## 🔴 TASK 2 — NẶNG: Customer Core (Home + Product + Cart + Checkout + Orders)
**Thành viên:** Người 2

### Scope (files chỉ người này chạm vào)
```
app-customer/src/main/java/.../ui/home/**
app-customer/src/main/java/.../ui/product/**
app-customer/src/main/java/.../ui/cart/**
app-customer/src/main/java/.../ui/checkout/**
app-customer/src/main/java/.../ui/order/**
app-customer/src/main/java/.../ui/search/**
app-customer/src/main/java/.../ui/MainActivity.java
app-customer/src/main/java/.../viewmodel/CartViewModel.java
app-customer/src/main/java/.../viewmodel/OrderViewModel.java
app-customer/src/main/java/.../viewmodel/ProductViewModel.java
app-customer/src/main/java/.../data/MockDataProvider.java
app-customer/src/main/java/.../data/repository/CartRepository.java
app-customer/src/main/java/.../data/repository/OrderRepository.java
app-customer/src/main/java/.../data/repository/ProductRepository.java
```

### Việc cần làm
1. Test **HomeFragment**: greeting, search, shortcuts, recipes, products grid
2. Test **ProductDetailActivity**: hiển thị, quantity, add to cart, reviews, similar
3. Test **CartFragment**: CRUD items, swipe delete, saved items, total
4. Test **CheckoutActivity**: validation, payment methods (COD/VNPay/MoMo), order creation
5. Test **OrderListFragment + OrderDetailActivity**: filter, cancel, return, review
6. Test **SearchActivity**: tìm kiếm sản phẩm

---

## 🟢 TASK 3 — NHẸ: Auth + Account + Shipper UI
**Thành viên:** Người 3

### Scope (files chỉ người này chạm vào)
```
app-customer/src/main/java/.../ui/auth/**
app-customer/src/main/java/.../ui/account/**
app-customer/src/main/java/.../ui/shipper/**
app-customer/src/main/java/.../ui/review/**
app-customer/src/main/java/.../ui/shipment/**
app-customer/src/main/java/.../ui/chat/**
app-customer/src/main/java/.../ui/recipe/**
app-customer/src/main/java/.../viewmodel/AuthViewModel.java
app-customer/src/main/java/.../viewmodel/ChatViewModel.java
app-customer/src/main/java/.../viewmodel/RecipeViewModel.java
app-customer/src/main/java/.../viewmodel/ShipmentViewModel.java
app-customer/src/main/java/.../data/repository/AuthRepository.java
app-customer/src/main/java/.../data/repository/ShipmentRepository.java
```

### Việc cần làm
1. Test **LoginActivity**: validation, API login, role routing (Customer vs Shipper)
2. Test **RegisterActivity**: validation, API register
3. Test **ForgotPasswordActivity**
4. Test **AccountFragment**: guest vs logged in, menu items, logout
5. Test **EditProfileActivity + ChangePasswordActivity**
6. Test **ShipperMainActivity**: header, toggle, tabs
7. Test **ShipperOrderListFragment**: active vs history, API calls
8. Test **ShipperOrderDetailActivity**: call, navigate, status transitions
9. Test **ChatActivity**, **RecipeListFragment**, **ShipmentListActivity**, **MyReviewsActivity**

---

## 🟢 TASK 4 — NHẸ: Backend API + Shared Module + Database
**Thành viên:** Người 4

### Scope (files chỉ người này chạm vào)
```
app-backend/src/main/java/**
app-backend/src/main/resources/**
shared/src/main/java/**
```

### Cấu hình Database (PostgreSQL)
> ⚠️ **Đã migrate từ SQLite sang PostgreSQL** (17/06/2026)

| Thông số | Giá trị |
|---|---|
| **Database** | PostgreSQL 16 |
| **Host** | `localhost:5432` |
| **DB Name** | `vuavuive_app` |
| **Username** | `postgres` |
| **Config file** | `app-backend/src/main/resources/application-dev.yml` |
| **Driver** | `org.postgresql:postgresql` (managed by Spring Boot BOM) |
| **Dialect** | `org.hibernate.dialect.PostgreSQLDialect` |
| **ddl-auto** | `update` (Hibernate tự sync schema khi khởi động) |

### Data đã migrate sang PostgreSQL
| Table | Rows |
|---|---|
| `users` | 3 |
| `categories` | 8 |
| `products` | 92 |
| `shippers` | 1 |
| `recipes` | 41 |
| `orders`, `order_items`, `order_status_logs` | 0 (tạo mới khi test) |

### Việc cần làm
1. **Khởi động PostgreSQL** đảm bảo service đang chạy trên `localhost:5432`
2. **Khởi động backend** bằng lệnh:
   ```bash
   cd app-backend
   .\run_backend.bat
   # hoặc: mvn spring-boot:run
   ```
3. Test API bằng Swagger (`http://localhost:3000/swagger-ui.html`) hoặc Postman:
   - Auth: register → login → nhận `accessToken`
   - Products: CRUD + pagination + search
   - Orders: create → get my → get detail → cancel → update status
   - Categories: CRUD
   - Payments: VNPay/MoMo URL generation
   - Shipper: get assigned orders, update delivery status
   - Chat: AI integration (Gemini)
   - Recipes: list
4. Verify **DataSeeder** tạo đúng test data (xem log lúc khởi động)
5. Review **SecurityConfig**: JWT filter, role-based access
6. Review **shared DTOs**: đảm bảo field mapping khớp giữa frontend ↔ backend
7. Review **shared API interfaces** (Retrofit): đảm bảo endpoint URLs đúng
8. Test **Room Database** (CartDao, ProductDao) trên Android
9. Nếu cần reset database sạch: đổi `ddl-auto: create` → khởi động 1 lần → đổi lại `update`

### ✅ Checklist Test Backend
- [ ] PostgreSQL service đang chạy, kết nối được từ backend
- [ ] Backend khởi động không có lỗi Hibernate/JPA
- [x] Swagger UI mở được tại `http://localhost:3000/swagger-ui.html`
- [x] `POST /api/auth/register` → tạo user thành công
- [x] `POST /api/auth/login` → nhận đúng `accessToken` + `refreshToken`
- [x] `GET /api/products` → trả về 92 sản phẩm
- [x] `GET /api/categories` → trả về 8 danh mục
- [ ] `POST /api/orders` (CUSTOMER token) → tạo đơn thành công
- [ ] `GET /api/orders/my` → đơn của user đó
- [ ] `PATCH /api/orders/{id}/cancel` → hủy đơn
- [x] `GET /api/orders/shipper` (SHIPPER token) → đơn của shipper
- [x] `GET /api/recipes` → 41 công thức
- [ ] `POST /api/chat` → Gemini AI trả lời

---

## 📋 MA TRẬN KHÔNG CONFLICT

| File/Folder | P1 | P2 | P3 | P4 |
|-------------|:--:|:--:|:--:|:--:|
| `app-admin/**` | ✅ | | | |
| `ui/home, product, cart, checkout, order, search` | | ✅ | | |
| `ui/auth, account, shipper, review, shipment, chat, recipe` | | | ✅ | |
| `app-backend/**` | | | | ✅ |
| `shared/**` | | | | ✅ |
| `viewmodel/Cart,Order,Product` | | ✅ | | |
| `viewmodel/Auth,Chat,Recipe,Shipment` | | | ✅ | |
| `data/repository/Cart,Order,Product` | | ✅ | | |
| `data/repository/Auth,Shipment` | | | ✅ | |
| `MockDataProvider.java` | | ✅ | | |

> ⚠️ **Quy tắc:** Mỗi người chỉ chỉnh sửa files trong scope của mình. Nếu cần sửa file ngoài scope → báo nhóm trước.

---

## ⏰ TIMELINE GỢI Ý

| Giai đoạn | Thời gian | Nội dung |
|-----------|-----------|----------|
| Phase 1 | 2-3 giờ | Mỗi người test theo checklist, ghi bug |
| Phase 2 | 2-3 giờ | Fix bugs trong scope |
| Phase 3 | 1 giờ | Cross-review: P1↔P4, P2↔P3 |
| Phase 4 | 1 giờ | Integration test toàn bộ flow end-to-end |
