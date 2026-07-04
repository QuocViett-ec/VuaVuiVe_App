# 🤖 Stream 1 – Code Logic Review (Codex tự review & fix)

> **Mục đích:** AI đọc code → phát hiện bug logic → fix trước khi user test thủ công.
> 
> **Quy tắc:** Mỗi wave review xong → fix → build thành công → commit + push GitHub.
> 
> **KHÔNG cần chạy app.** Chỉ đọc code và sửa lỗi logic.

---

## Quy trình cho mỗi Wave

```
1. Đọc tất cả file liên quan trong wave
2. Kiểm tra từng checklist item bên dưới
3. Nếu phát hiện lỗi → sửa ngay
4. Chạy `./gradlew assembleDebug` để đảm bảo build thành công
5. Commit: `fix(codex): wave X - <mô tả ngắn>`
6. Push GitHub
7. Báo cho user biết đã fix những gì → user bắt đầu Stream 2
```

---

## 🔴 Wave 1 – Auth & Session

### Files cần review

| Module | File | Path |
|--------|------|------|
| shared | `SessionManager.java` | `shared/.../util/SessionManager.java` |
| shared | `User.java` | `shared/.../data/dto/User.java` |
| admin | `AdminLoginActivity.java` | `app-admin/.../ui/auth/AdminLoginActivity.java` |
| admin | `MainActivity.java` | `app-admin/.../ui/main/MainActivity.java` |
| customer | `LoginActivity.java` | `app-customer/.../ui/auth/LoginActivity.java` |
| customer | `RegisterActivity.java` | `app-customer/.../ui/auth/RegisterActivity.java` (nếu có) |
| customer | `AuthViewModel.java` | `app-customer/.../viewmodel/AuthViewModel.java` |
| customer | `AuthRepository.java` | `app-customer/.../data/repository/AuthRepository.java` |
| shipper | `ShipperLoginActivity.java` | `app-shipper/.../ui/auth/ShipperLoginActivity.java` |
| shipper | `ShipperAuthRepository.java` | `app-shipper/.../data/repository/ShipperAuthRepository.java` (nếu có) |

### Checklist review

#### 1.1 SessionManager (shared)
- [ ] `saveSession()` — kiểm tra null cho tất cả tham số trước khi put vào SharedPreferences
- [ ] `getUser()` — trả về null nếu không có data, caller phải handle null
- [ ] `hasValidAccessToken()` — JWT decode có try-catch cho token malformed/expired không?
- [ ] `clearSession()` — có xoá HẾT tất cả keys (access_token, refresh_token, user data) không?
- [ ] `isAdmin()`, `isBackoffice()`, `isShipper()` — logic so sánh role có case-insensitive không?
- [ ] Thread safety: SharedPreferences có dùng `apply()` thay `commit()` cho write operations?

#### 1.2 AdminLoginActivity
- [ ] Spinner chọn role → auto-fill credentials: có đúng 4 options (Admin, Staff, Audit, Customer)?
- [ ] `onLoginClick()` → validate email/password không rỗng trước khi gọi API
- [ ] API response callback: check `result.data != null` trước khi truy cập `result.data.getUser()`
- [ ] Login thành công → `sessionManager.saveSession()` → navigate to `MainActivity`
- [ ] Login thất bại → hiện error message, KHÔNG navigate
- [ ] Error handling: network error, server error (500), invalid credentials (401/403) đều được xử lý

#### 1.3 Admin MainActivity (route guard)
- [ ] `onCreate()` → check `sessionManager.isBackoffice()` → nếu false thì `finish()` + redirect login
- [ ] Không có path nào bypass được `isBackoffice()` check
- [ ] `onBackPressed()` behavior: double-back-to-exit hay về login?

#### 1.4 Customer LoginActivity
- [ ] `goToMain()` dòng ~151 → kiểm tra `user.isShipper()` → nếu true thì block + Toast
- [ ] Validate email format (regex hoặc Patterns.EMAIL_ADDRESS)
- [ ] Validate password không rỗng
- [ ] Loading state: disable button khi đang gọi API, re-enable khi xong
- [ ] Error response từ API: hiện message cụ thể (sai email? sai password? account locked?)

#### 1.5 Shipper LoginActivity
- [ ] `onCreate()` → check `isLoggedIn()` → auto-navigate nếu token còn hạn
- [ ] Login response → check `user.isShipper()` → nếu false → `repository.logout()` + Toast
- [ ] `isLoggedIn()` phải check cả token expiry, không chỉ check token tồn tại
- [ ] Token refresh logic: nếu access_token expired nhưng refresh_token còn hạn → refresh

#### 1.6 Cross-module consistency
- [ ] Role string constants: "admin", "staff", "audit", "shipper", "user"/"customer" — có thống nhất giữa 3 app?
- [ ] `User.isBackoffice()` trả true cho: admin, staff, audit — kiểm tra logic
- [ ] `User.isShipper()` chỉ trả true cho "shipper" — kiểm tra logic
- [ ] Tất cả role comparison dùng `equalsIgnoreCase()` — tránh case mismatch

---

## 🟠 Wave 2 – Dashboard & Navigation

### Files cần review

| Module | File |
|--------|------|
| admin | `MainActivity.java` (fragment switching) |
| admin | `DashboardFragment.java` |
| admin | `admin_nav_menu.xml` |
| customer | `MainActivity.java` (NavController) |
| customer | `HomeFragment.java` |
| shipper | `ShipperMainActivity.java` |
| shipper | `ShipperPagerAdapter.java` |

### Checklist review

#### 2.1 Admin MainActivity
- [ ] `replaceFragment()` — có `commitAllowingStateLoss()` cho trường hợp activity đang paused?
- [ ] Bottom nav `setOnItemSelectedListener` → 5 tab mapping đúng fragment?
- [ ] Fragment transaction có add to backstack không? (không nên cho bottom nav)
- [ ] Lifecycle: `onResume()` có re-check session không?

#### 2.2 Admin DashboardFragment
- [ ] Quick-action buttons (`btn_nav_users`, `btn_nav_shipments`, `btn_nav_audit`, `btn_nav_reports`) — có setOnClickListener?
- [ ] `replaceFragment(new UserListFragment())` — casting `(MainActivity) getActivity()` có null-check?
- [ ] Stats data load — API call có error handling?

#### 2.3 Customer MainActivity
- [ ] NavController setup — navigation graph có đúng 5 destinations?
- [ ] Default tab = Products (line ~81) — `navController.navigate(R.id.nav_products)` có đúng?
- [ ] `navigateToProducts()` public method — được gọi từ CartFragment, OrderListFragment
- [ ] Badge count cho Cart tab — có observe LiveData từ CartViewModel?

#### 2.4 Shipper ShipperMainActivity
- [ ] ViewPager2 + TabLayout — `ShipperPagerAdapter` trả đúng 4 fragments?
- [ ] Online switch — `repository.updateOnlineStatus(isChecked)` có error handling?
- [ ] `onDestroy()` → set offline status? hay keep online?
- [ ] Firebase RTDB listener cho online status — có remove listener ở `onDestroy()`?

---

## 🟡 Wave 3 – Sản phẩm

### Files cần review

| Module | File |
|--------|------|
| admin | `AdminProductListFragment.java` |
| admin | `ProductEditActivity.java` |
| admin | `AdminProductAdapter.java` |
| admin | `AdminProductApi.java` (Retrofit interface) |
| customer | `ProductListFragment.java` |
| customer | `ProductDetailActivity.java` |
| customer | `ProductViewModel.java` |
| shared | `Product.java` |

### Checklist review

#### 3.1 AdminProductListFragment
- [ ] `loadCategories()` — API error → spinner trống → app crash khi chọn?
- [ ] `loadProducts()` — null response → `NullPointerException` khi iterate?
- [ ] Chip filter (all/fruit/veg/dry) + spinner — hai bộ lọc có conflict nhau không?
- [ ] Search `TextWatcher` — có debounce không? (gọi API mỗi ký tự sẽ spam request)
- [ ] Low stock chip — threshold bao nhiêu? Logic filter đúng?
- [ ] Long-press delete → `adminProductApi.deleteProduct()` — có confirm dialog?
- [ ] Audit role check: `isAudit()` block đúng FAB add + long-press delete?
- [ ] Export CSV: `exportProductsCsv()` — dữ liệu null trong Product gây crash?

#### 3.2 ProductEditActivity
- [ ] Validation chain: `name.isEmpty()`, `price <= 0`, `originalPrice < price`, `stock < 0`, `unit.isEmpty()`
- [ ] `originalPrice < price` — logic đúng chưa? (originalPrice là giá gốc, phải >= price)
- [ ] Image upload: `adminProductApi.uploadImage()` — MultipartBody build đúng?
- [ ] Image upload error → imageUrl null → create product vẫn thành công nhưng không có ảnh?
- [ ] `enforceRolePermissions()` (Audit) — disable ALL fields: EditText, Spinner, Button, ImageView
- [ ] Edit mode vs Create mode: `productId != null` → load existing data
- [ ] API response cho update vs create: cùng endpoint hay khác?

#### 3.3 Customer ProductDetailActivity
- [ ] Quantity selector: `quantity <= product.getStock()` — max check
- [ ] Quantity = 0 → nút Add to Cart disabled?
- [ ] `product.getStock() == 0` → hiện "Hết hàng", disable Add to Cart
- [ ] Price display: `product.getPrice()` vs `product.getOriginalPrice()` — sale badge logic
- [ ] Rating display: null rating → default 0 hay ẩn section?
- [ ] Add to Cart: `cartViewModel.addItem()` — duplicate product handling (tăng qty hay thêm mới)?

---

## 🟢 Wave 4 – Giỏ hàng & Checkout COD

### Files cần review

| Module | File |
|--------|------|
| customer | `CartFragment.java` |
| customer | `CartAdapter.java` |
| customer | `CartViewModel.java` |
| customer | `CheckoutActivity.java` |
| customer | `MapPickerActivity.java` |
| shared | `CartItemEntity.java` |
| shared | `CurrencyFormatter.java` |

### Checklist review

#### 4.1 CartFragment
- [ ] `ItemTouchHelper.LEFT` swipe — `getAdapterPosition()` có return `NO_POSITION` (-1)?
- [ ] `cartAdapter.getItemAt(position)` — null check trước `removeItem()`
- [ ] Empty state: cart trống + saved trống → `layoutEmptyCart` visible
- [ ] `savedItems` expand/collapse — `savedExpanded` boolean toggle đúng?
- [ ] `updateTotal()` — items null check (`items != null ? items.size() : 0`)
- [ ] `btnCheckout` → check `authViewModel.isLoggedIn()` trước khi navigate

#### 4.2 CartViewModel / CartItemEntity
- [ ] `addItem()` — nếu product đã có trong cart → tăng quantity thay vì duplicate
- [ ] `removeItem(productId)` — productId null check
- [ ] `getLineTotal()` = `price * quantity` — kiểm tra kiểu dữ liệu (double overflow?)
- [ ] `clearCart()` — xoá hết items + saved items?
- [ ] `getCartItemsSync()` — chạy trên background thread? (Room query)

#### 4.3 CheckoutActivity
- [ ] `cartLoaded` guard — nếu LiveData chưa emit → button disabled → race condition phòng ngừa OK?
- [ ] `doPlaceOrder()` — empty cartItems check ĐÃ CÓ ở `placeOrder()` wrapper
- [ ] Voucher hardcode: `VUAVUIVE` → 15%, `FREESHIP`/`FREESHIP24` → 30.000đ — logic đúng
- [ ] `appliedDiscount` — nếu subtotal thay đổi sau khi apply voucher → discount cũ vẫn đúng?
- [ ] `totalAmount = max(subtotal + shipping - discount, 0)` — không cho âm, OK
- [ ] `CreateOrderRequest` build — tất cả fields set đầy đủ? (`items`, `delivery`, `payment`, `subtotal`, `shippingFee`, `totalAmount`)
- [ ] COD success: `cartViewModel.clearCart()` → `finish()` — đúng sequence
- [ ] MoMo/ZaloPay: `result.data.getId()` vs `result.data.getOrderId()` — null fallback đúng?
- [ ] `PaymentResultActivity` intent extras: `payment_url`, `deeplink`, `order_id`, `order_total`, `provider` — tất cả non-null?
- [ ] Map picker result: `address` null/empty check trước `setText()`

---

## 🔵 Wave 5 – Quản lý đơn hàng (Admin)

### Files cần review

| Module | File |
|--------|------|
| admin | `AdminOrderListFragment.java` |
| admin | `AdminOrderDetailActivity.java` |
| admin | `AdminOrderAdapter.java` |
| admin | `AdminOrderApi.java` |
| admin | `AdminUserApi.java` |
| shared | `Order.java` |
| shared | `OrderItem.java` |
| shared | `PaymentDetail.java` |

### Checklist review

#### 5.1 AdminOrderListFragment
- [ ] 7 tab filter — status mapping chính xác cho từng tab?
- [ ] Tab "Đang giao": `shipping` OR `shipped` OR `in_transit` — code có handle cả 3?
- [ ] Tab "Trả hàng": `status.startsWith("return")` — catch cả `return_requested` + `returned`?
- [ ] Search: tìm theo `orderId`, `recipientName`, `recipientPhone` — null-safe cho mỗi field
- [ ] Multi-select (long press): `selectedOrders` list management — add/remove toggle
- [ ] `showBulkUpdateDialog()` — 4 options: confirm, shipping, delivered, cancelled
- [ ] Bulk update — Audit role blocked? check `isAudit()` trước khi show dialog
- [ ] Export CSV — field separator đúng (`,`), escape cho giá trị chứa dấu phẩy?
- [ ] Pagination: `page` param khi gọi API — có load more khi scroll?

#### 5.2 AdminOrderDetailActivity
- [ ] Status spinner — 8 values đúng: `pending, confirmed, preparing, ready_for_pickup, shipping, in_transit, delivered, cancelled`
- [ ] Status update API call — `adminOrderApi.updateOrderStatus()` error handling
- [ ] Mark Paid button: hiện chỉ khi `!isPaid && paymentMethod != MOMO && !isAudit`
- [ ] Gán Shipper: `adminUserApi.getUsers("shipper")` → populate spinner
- [ ] Gán Shipper: `adminOrderApi.assignShipper(orderId, shipperId)` → success/error handling
- [ ] Shipper spinner empty (no shippers available) → disable assign button?
- [ ] Return request section: `layoutReturnReview` visible chỉ khi `status == "return_requested"`
- [ ] Approve return: set status → `returned`, Reject: set status → `delivered`
- [ ] Order items RecyclerView — `setNestedScrollingEnabled(false)` cho scroll trong ScrollView?
- [ ] Audit role: disable status spinner + mark paid + assign shipper + return actions

---

## 🟣 Wave 6 – Shipper giao đơn

### Files cần review

| Module | File |
|--------|------|
| shipper | `ShipperOrderListFragment.java` |
| shipper | `ShipperOrderDetailActivity.java` |
| shipper | `ShipperOrderAdapter.java` |
| shipper | `ShipperOrderItemAdapter.java` |
| shipper | `FirebaseShipperRepository.java` |
| shared | `Order.java` (getRecipientName/Phone/Address) |

### Checklist review

#### 6.1 ShipperOrderListFragment
- [ ] `filterByTab()` — Active: 5 status (CONFIRMED, SHIPPING, PREPARING, IN_TRANSIT, READY_FOR_PICKUP)
- [ ] `filterByTab()` — History: 3 status (DELIVERED, FAILED, RETURNED)
- [ ] Search: `orderId` + `recipientName` + `recipientPhone` + `recipientAddress` — tất cả null-safe `.toLowerCase()`
- [ ] Chip filter: "ALL" vs "PENDING" vs "SHIPPING" (active) / "SUCCESS" vs "FAILED" (history)
- [ ] Chip "PENDING" maps to: CONFIRMED, PREPARING, READY_FOR_PICKUP — đúng
- [ ] Chip "SHIPPING" maps to: IN_TRANSIT, SHIPPING — đúng
- [ ] Firebase LiveData observer — `getViewLifecycleOwner()` tránh leak
- [ ] Empty state: `layoutEmpty` visible/gone toggle

#### 6.2 ShipperOrderDetailActivity
- [ ] `orderId = getIntent().getStringExtra("order_id")` — null → `finish()` ✓
- [ ] `bindOrder()` — tất cả fields null-safe: recipientName, phone, address, note
- [ ] Phone null/empty → `btnCall.setEnabled(false)` ✓
- [ ] Address null/empty → `btnNavigate.setEnabled(false)` ✓
- [ ] `ACTION_DIAL` (không phải ACTION_CALL) — không cần CALL_PHONE permission ✓
- [ ] Google Maps fallback: `resolveActivity()` null → open browser ✓
- [ ] `setupActionButtons()` state machine:
  - CONFIRMED/PREPARING/READY_FOR_PICKUP/SHIPPING → show btnStartDelivery
  - IN_TRANSIT → show btnDelivered + btnFailed
  - DELIVERED/FAILED/RETURNED → show tvDoneLabel
- [ ] `confirm()` dialog → `updateStatus(newStatus)` — one-shot observer (`removeObservers`)
- [ ] `showFailReasonDialog()` — 4 preset reasons + "Lý do khác" → `showCustomFailReasonDialog()`
- [ ] Custom fail reason empty → default "Lý do khác" — OK
- [ ] `updateStatus()` with failReason — `repository.updateOrderStatus(orderId, newStatus, failReason)`
- [ ] Payment text logic: MoMo paid → "Do not collect cash", COD → "Amount to collect: X đ"
- [ ] ZaloPay handling in `paymentText()` — MISSING? chỉ check `momo`, không check `zalopay`

---

## ⚫ Wave 7 – Hậu giao hàng

### Files cần review

| Module | File |
|--------|------|
| customer | `OrderListFragment.java` |
| customer | `OrderDetailActivity.java` |
| customer | `OrderAdapter.java` |
| customer | `ReviewBottomSheetDialogFragment.java` |
| customer | `OrderViewModel.java` |
| shipper | `ShipperStatsFragment.java` |
| shipper | `ShipperProfileFragment.java` |

### Checklist review

#### 7.1 Customer OrderListFragment
- [ ] 5 tab filter: All, Pending Group, Shipping Group, Delivered, Cancelled
- [ ] `matchesCurrentTab()` — PENDING group: `pending`, `pending_payment`, `pending_approval`, `confirmed`, `preparing`, `ready_for_pickup`
- [ ] SHIPPING group: `shipping`, `in_transit`
- [ ] `onResume()` → `loadOrders()` — refresh mỗi lần quay lại
- [ ] Empty state toggle đúng

#### 7.2 Customer OrderDetailActivity
- [ ] `updateActionButtons()`:
  - Cancel visible khi: `pending`, `pending_payment`, `pending_approval`, `confirmed`
  - Return visible khi: `delivered`
  - Review visible khi: `delivered`
- [ ] Cancel dialog → `orderViewModel.cancelOrder(orderId)` → success: Toast + finish
- [ ] Return dialog → validate reason không rỗng → `orderViewModel.returnOrder(orderId, reason)`
- [ ] Review dialog → nhiều items → `AlertDialog.setItems()` chọn sản phẩm
- [ ] Review 1 item → trực tiếp mở `ReviewBottomSheetDialogFragment`
- [ ] `getStatusLabel()` — switch đủ tất cả status codes?
- [ ] `getStatusColor()` — có color resource cho mỗi status?
- [ ] `order.getId()` substring(0,8) — nếu ID < 8 chars → `StringIndexOutOfBoundsException`!

#### 7.3 ShipperStatsFragment
- [ ] Revenue chỉ tính DELIVERED orders — đúng
- [ ] MoMo paid → `onlineRevenue`, còn lại → `codRevenue` — ZaloPay bị coi là COD?
- [ ] `(long) order.getFinalAmount()` — truncation cho số thập phân?
- [ ] Division by zero: `total == 0` → rate = 100% (default) — hợp lý?

#### 7.4 ShipperProfileFragment
- [ ] `sessionManager.getUser()` null → crash? cần null-check
- [ ] `rate = (success * 100) / total` — integer division → mất precision
- [ ] Logout: `repository.logout()` → `goToLogin()` với CLEAR_TASK flags

---

## ⬛ Wave 8 – Tính năng bổ sung

### Files cần review

| Module | File |
|--------|------|
| customer | `PaymentResultActivity.java` |
| customer | `AccountFragment.java` |
| customer | `EditProfileActivity.java` |
| customer | `ChangePasswordActivity.java` |
| admin | `VoucherListFragment.java` |
| admin | `VoucherEditActivity.java` |
| admin | `UserListFragment.java` |
| admin | `UserAdapter.java` |
| admin | `ShipmentListFragment.java` |
| admin | `ShipmentDetailActivity.java` |
| admin | `MockRepository.java` |

### Checklist review

#### 8.1 Customer PaymentResultActivity
- [ ] Intent extras: `payment_url`, `deeplink`, `order_id`, `order_total`, `provider` — null handling
- [ ] WebView load `payment_url` — null URL → crash?
- [ ] Deeplink handling: MoMo vs ZaloPay deeplink format khác nhau
- [ ] Payment callback/polling: kiểm tra trạng thái thanh toán sau khi quay về app
- [ ] Success → `cartViewModel.clearCart()` — có được gọi?
- [ ] Failure → giữ cart, cho phép thử lại?

#### 8.2 Admin VoucherListFragment (MockRepository)
- [ ] `MockRepository.getInstance().getCurrentUser()` null → `return` — OK
- [ ] FAB visibility: chỉ `"admin"` → visible, staff/audit → GONE
- [ ] `onVoucherClick()` — staff/audit vẫn navigate tới VoucherEditActivity (read-only?)
- [ ] VoucherEditActivity — có `enforceRolePermissions()` cho staff/audit không?

#### 8.3 Admin UserListFragment (MockRepository)
- [ ] Staff role → `popBackStack()` block access — nhưng nếu fragment đã load thì sao?
- [ ] Audit role: `onUserStatusChanged()` → Toast + reload (undo toggle) — đúng
- [ ] `exportUsersCsv()` — `u.getName().replace(",", " -")` — getName() null → NPE!
- [ ] `u.getRole().toUpperCase()` — getRole() null → NPE!
- [ ] Change role dialog — Admin tự đổi role mình → mất quyền admin?
- [ ] Tab filter: tab 0 = Khách hàng (`user`/`customer`), tab 1 = Shipper, tab 2 = Nhân viên

#### 8.4 Admin ShipmentListFragment (MockRepository)
- [ ] Carrier filter + Status filter — two-dimensional filter logic
- [ ] `ShipmentDetailActivity` — `getStringExtra("SHIPMENT_ID")` null → crash?

---

## ⭐ Wave 9 – E2E (chỉ review data flow)

### Checklist review

- [ ] Order status flow consistency: `pending → confirmed → preparing → ready_for_pickup → shipping/in_transit → delivered`
- [ ] Tất cả 3 app dùng cùng status string constants?
- [ ] Firebase RTDB path cho shipper orders — backend write + shipper read — path match?
- [ ] Admin assign shipper → Firebase node update → Shipper app pick up — data flow liền mạch?
- [ ] Payment status: `pending → paid` — cả COD và online payment?
- [ ] Return flow: Customer request → Admin review → status update → tất cả app reflect?

---

## ⚡ Danh sách lỗi phổ biến cần quét toàn codebase

> Sau khi review từng wave, chạy thêm các kiểm tra toàn cục sau:

| # | Pattern cần tìm | Vì sao nguy hiểm |
|---|-----------------|-------------------|
| 1 | `.substring(0, 8)` không có length check | `StringIndexOutOfBoundsException` |
| 2 | `.getRole().toUpperCase()` không null-check | `NullPointerException` |
| 3 | `.getName().replace(...)` không null-check | `NullPointerException` |
| 4 | `getActivity()` casting không null-check | Fragment detached crash |
| 5 | LiveData `.observe(this, ...)` trong Activity `onResume()` | Duplicate observers |
| 6 | `getAdapterPosition()` không check `NO_POSITION` | Array index -1 crash |
| 7 | Firebase listener không remove ở `onDestroy()` | Memory leak |
| 8 | `Integer.parseInt()` không try-catch | `NumberFormatException` |
| 9 | RecyclerView trong ScrollView thiếu `setNestedScrollingEnabled(false)` | Scroll conflict |
| 10 | Retrofit callback trên main thread access room DB | `IllegalStateException` |
