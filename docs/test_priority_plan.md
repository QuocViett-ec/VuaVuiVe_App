# Kế hoạch Test & Fix theo thứ tự ưu tiên – Vựa Vui Vẻ

> ✅ Đã cross-check toàn bộ codebase (40+ files Java, 3 app modules + shared)
> 
> Mỗi wave test xong → fix → **commit + push GitHub** ngay, tránh conflict khi sửa các flow sau.

---

## 📂 Chia 2 luồng test song song

| Luồng | File | Ai làm? | Mô tả |
|-------|------|---------|--------|
| **Stream 1** | [`test_stream1_code_review.md`](test_stream1_code_review.md) | 🤖 **Codex/AI** | Đọc code → phát hiện bug logic → fix → build thành công |
| **Stream 2** | [`test_stream2_manual_testing.md`](test_stream2_manual_testing.md) | 👤 **User** | Chạy app trên emulator → test UI/UX → tick ✅/❌ |

### Quy trình:
```
Stream 1 (Codex fix code)  ──→  build OK  ──→  Stream 2 (User test emulator)
         │                                              │
         └── commit + push ◄───── OK? ◄────────────────┘
                                   │
                              Nếu lỗi → quay lại Stream 1
```

> **Mỗi wave:** Codex review & fix trước (Stream 1) → User test sau (Stream 2) → Push GitHub.

---

## Tại sao phải theo thứ tự này?

Các flow có **quan hệ phụ thuộc (dependency)** rõ ràng:

```
[Auth/Session] → [Sản phẩm] → [Giỏ hàng] → [Checkout/Đặt hàng]
                                                      ↓
                                          [Admin xác nhận đơn]
                                                      ↓
                                          [Shipper giao hàng]
                                                      ↓
                                     [Customer review / return]
```

---

## ⚠️ Phát hiện quan trọng từ cross-check codebase

> [!WARNING]
> Những điểm này **khác với test flow docs** — cần lưu ý khi test!

| # | Phát hiện | Ảnh hưởng |
|---|-----------|-----------|
| 1 | **Admin: User Mgmt & Shipment truy cập qua DashboardFragment** (nút quick-action), **KHÔNG có trong bottom nav** | Test flow F, G phải vào Dashboard trước, bấm nút quick-action |
| 2 | **Admin: Voucher + User dùng `MockRepository`** (dữ liệu local, KHÔNG gọi API backend) | Dữ liệu sẽ **mất khi kill app** — đây là hạn chế hiện tại |
| 3 | **Admin: Shipment cũng dùng `MockRepository`** | Tương tự #2, dữ liệu local |
| 4 | **Customer: App mặc định mở tab `Products`** (line 81 MainActivity) | Test flow B nói kiểm tra "app mở mặc định vào tab Products" — đúng |
| 5 | **Shipper: Dùng Firebase RTDB trực tiếp** (FirebaseShipperRepository), không qua REST API backend | Dữ liệu đơn phải đồng bộ giữa backend ↔ Firebase |
| 6 | **Admin login: Có spinner chọn role, auto-fill email/password** | Không cần nhập tay, chỉ cần chọn role và bấm login |
| 7 | **Customer login: Auto-fill `customer@gmail.com` / `Customer@123`** | Tiện test nhưng cần verify xoá prefill khi production |
| 8 | **Shipper login: Có check `isLoggedIn()` → auto-navigate** | Kill app → mở lại: **nếu token còn hạn sẽ skip Login** — đúng flow |
| 9 | **Admin Order Detail: Status spinner có 8 status codes** | `pending, confirmed, preparing, ready_for_pickup, shipping, in_transit, delivered, cancelled` |
| 10 | **Admin Order Detail: Có tính năng GÁN SHIPPER** (spinnerShipper + btnAssignShipper) | Test flow D2 chưa đề cập — nên test luôn |

---

## 🔴 Wave 1 – Auth & Session (Làm TRƯỚC TIÊN)

> Nền tảng cho mọi thứ. Không login được = không test được gì.

| App | Flow | File chính | Mô tả |
|-----|------|-----------|--------|
| **Admin** | A | `AdminLoginActivity.java` | Spinner chọn role → auto-fill → login API → `isBackoffice()` check |
| **Customer** | A | `LoginActivity.java` | email/pass validation → `AuthViewModel.login()` → `isShipper()` block |
| **Shipper** | A | `ShipperLoginActivity.java` | `isLoggedIn()` auto-skip → login → `isShipper()` role check |

**Verify cụ thể:**
- Admin: 4 tài khoản đúng spinner → login thành công/thất bại
- Admin: `currentUser.isBackoffice()` reject Customer role ở `MainActivity`
- Customer: Shipper login bị reject tại `goToMain()` (dòng 151)
- Shipper: Customer login bị reject (dòng 88 `repository.logout()`)
- Session persist sau kill app (kiểm tra `hasValidAccessToken()` JWT decode)
- Logout xoá session (`clearSession()` + FLAG_ACTIVITY_CLEAR_TASK)

**Commit:** `fix: auth flow - login, role guard, session`

---

## 🟠 Wave 2 – Dashboard & Navigation cơ bản

| App | Flow | File chính | Mô tả |
|-----|------|-----------|--------|
| **Admin** | B | `MainActivity.java` → 5 bottom tabs | Dashboard, Đơn hàng, Sản phẩm, Voucher, Chat |
| **Customer** | B | `MainActivity.java` → NavController | Home, Products, Cart, Orders, Account — default tab: **Products** |
| **Shipper** | B | `ShipperMainActivity.java` → ViewPager2 | Cần giao, Lịch sử, Thống kê, Cá nhân + Online switch |

**Verify cụ thể:**
- Admin: `replaceFragment()` chuyển 5 tab không crash
- Customer: NavController + bottom nav đồng bộ tab highlight
- Shipper: Online switch → `repository.updateOnlineStatus(isChecked)`
- Quay app về background → mở lại: session không mất

**Commit:** `fix: navigation & dashboard - tab switching, crash fixes`

---

## 🟡 Wave 3 – Quản lý Sản phẩm (Admin) + Duyệt sản phẩm (Customer)

| App | Flow | File chính | Mô tả |
|-----|------|-----------|--------|
| **Admin** | C1 | `AdminProductListFragment` | Spinner 11 category + chip (all/fruit/veg/dry) + search + low stock |
| **Admin** | C2 | `ProductEditActivity` | Validation: tên rỗng, giá ≤ 0, originalPrice < price, stock < 0, unit rỗng |
| **Admin** | C3 | `ProductEditActivity` | `enforceRolePermissions()` → Audit disabled mọi field + "READ ONLY" |
| **Customer** | C | `ProductListFragment` + `ProductDetailActivity` | Search, category filter, quantity ≤ stock, Add to cart |

**Verify cụ thể:**
- Admin: `loadCategories()` từ API → bind spinner dynamically
- Admin: Upload image → `adminProductApi.uploadImage()` → lưu URL
- Admin: Audit role → `enforceRolePermissions()` disable toàn bộ form
- Admin: Long press xoá → `adminProductApi.deleteProduct()`
- Customer: ProductDetail hiện giá, stock, rating, add to cart

**Commit:** `fix: product management - CRUD, validation, role restriction`

---

## 🟢 Wave 4 – Giỏ hàng & Checkout COD (Customer)

| App | Flow | File chính | Mô tả |
|-----|------|-----------|--------|
| **Customer** | D | `CartFragment` | Swipe-to-delete, saved items, empty state, shop now button |
| **Customer** | E | `CheckoutActivity` | `DeliveryInfo` validation, voucher codes, COD order creation |

**Verify cụ thể:**
- Cart: `ItemTouchHelper.LEFT` swipe → `removeItem()`
- Cart: `savedItems` section expand/collapse
- Checkout: Voucher `VUAVUIVE` → 15%, `FREESHIP`/`FREESHIP24` → 30.000đ
- Checkout: `cartLoaded` guard chống race condition
- Checkout: Map picker → `MapPickerActivity` → address fill
- COD: `cartViewModel.clearCart()` + finish after success

**Commit:** `fix: cart & COD checkout - quantity, voucher, order creation`

---

## 🔵 Wave 5 – Quản lý đơn hàng (Admin)

| App | Flow | File chính | Mô tả |
|-----|------|-----------|--------|
| **Admin** | D1 | `AdminOrderListFragment` | 7 tab status + search (orderId/name/phone) |
| **Admin** | D2 | `AdminOrderDetailActivity` | Status spinner (8 codes) + Mark Paid + **Gán Shipper** |
| **Admin** | D3 | `AdminOrderListFragment` | Multi-select → `showBulkUpdateDialog()` → 4 bulk options |

**Verify cụ thể:**
- Tab "Đang giao" filter: `shipping` OR `shipped` OR `in_transit`
- Tab "Trả hàng" filter: `status.startsWith("return")`
- Mark Paid: chỉ hiện khi `!isPaid && !MOMO && !audit`
- **Gán Shipper**: `adminUserApi.getUsers(role=SHIPPER)` → `adminOrderApi.assignShipper()`
- Export CSV: Audit bị chặn, Admin/Staff OK
- Bulk update: Audit bị chặn

**Commit:** `fix: admin order management - status update, bulk, assign shipper`

---

## 🟣 Wave 6 – Shipper nhận & giao đơn

| App | Flow | File chính | Mô tả |
|-----|------|-----------|--------|
| **Shipper** | C | `ShipperOrderListFragment(isHistory=false)` | Firebase real-time, chip filter (All/Pending/Shipping) |
| **Shipper** | D | `ShipperOrderDetailActivity` | Call (ACTION_DIAL), Navigate (Google Maps + fallback), Payment info |
| **Shipper** | E1 | `ShipperOrderDetailActivity` | CONFIRMED/PREPARING/READY_FOR_PICKUP/SHIPPING → **IN_TRANSIT** |
| **Shipper** | E2 | `ShipperOrderDetailActivity` | IN_TRANSIT → **DELIVERED** |
| **Shipper** | E3 | `ShipperOrderDetailActivity` | IN_TRANSIT → **FAILED** + 4 lý do có sẵn + "Lý do khác" custom |

**Verify cụ thể:**
- Tab Cần giao: filter 5 status active (CONFIRMED, SHIPPING, PREPARING, IN_TRANSIT, READY_FOR_PICKUP)
- Search: orderId + recipientName + phone + **address** (test flow có address!)
- Call: `Intent.ACTION_DIAL` (không ACTION_CALL, nên an toàn)
- Navigate: Google Maps `google.navigation:q=` → fallback `https://maps.google.com/...`
- Fail reasons: 4 preset + 1 custom (dialog chain: chọn "Lý do khác" → dialog nhập tay)
- Payment text: MoMo paid → "Do not collect cash", COD → "Amount to collect: X đ"

**Commit:** `fix: shipper delivery flow - status update, call, navigate`

---

## ⚫ Wave 7 – Hậu giao hàng

| App | Flow | File chính | Mô tả |
|-----|------|-----------|--------|
| **Customer** | G | `OrderListFragment` + `OrderDetailActivity` | Cancel (pending/confirmed), Return (delivered), Review (delivered) |
| **Shipper** | F | `ShipperOrderListFragment(isHistory=true)` | Filter: DELIVERED / FAILED / RETURNED |
| **Shipper** | G | `ShipperStatsFragment` | Revenue = DELIVERED only, COD vs Online, failed count |
| **Admin** | D4 | `AdminOrderDetailActivity.setupReturnRequest()` | Approve → `returned`, Reject → `delivered` |

**Verify cụ thể:**
- Customer cancel: chỉ hiện khi `pending/pending_payment/pending_approval/confirmed`
- Customer return: bắt buộc lý do, lý do rỗng → Toast warning
- Customer review: `ReviewBottomSheetDialogFragment` — chọn sản phẩm nếu nhiều item
- Shipper stats: `onlineRevenue` chỉ tính MoMo paid, `codRevenue` = phần còn lại
- Shipper profile: `rate = success / (success + failed) * 100`
- Admin return: `layoutReturnReview` chỉ hiện khi status = `return_requested`

**Commit:** `fix: post-delivery - review, return, stats, history`

---

## ⬛ Wave 8 – Các tính năng bổ sung

| App | Flow | File chính | Mô tả |
|-----|------|-----------|--------|
| **Customer** | F | `CheckoutActivity` + `PaymentResultActivity` | MoMo/ZaloPay: tạo đơn → tạo payment URL → deeplink/WebView |
| **Customer** | H | `AccountFragment`, `EditProfileActivity`, `ChangePasswordActivity` | Profile, change password, recipes, chat, shipments, reviews |
| **Admin** | E | `VoucherListFragment` ⚠️ **dùng MockRepository** | Admin add/edit, Staff/Audit chỉ xem (FAB ẩn) |
| **Admin** | F | `UserListFragment` ⚠️ **dùng MockRepository, vào từ Dashboard** | Staff bị block, Admin đổi role, Audit chỉ xem |
| **Admin** | G | `ShipmentListFragment` ⚠️ **dùng MockRepository, vào từ Dashboard** | Carrier + Status filter, ShipmentDetailActivity |
| **Admin** | — | `AdminChatFragment` | Chat tab cuối cùng |
| **Shipper** | H | `ShipperProfileFragment` | Name/email/phone + tỷ lệ thành công + logout |

> [!IMPORTANT]
> **Admin Voucher, User, Shipment** đều dùng `MockRepository` (dữ liệu in-memory).
> Dữ liệu sẽ **reset khi kill app**. Đây là behavior hiện tại, KHÔNG phải bug.
> User Mgmt & Shipment truy cập qua **DashboardFragment** quick-action buttons,
> KHÔNG có trong bottom navigation.

**Commit:** `fix: supplementary features - payment, voucher, user mgmt, chat`

---

## ⭐ Wave 9 – Test liên thông (End-to-End)

| Flow | Mô tả |
|------|--------|
| **Customer → Admin → Shipper → Customer** | Đặt COD → Admin xác nhận + gán shipper → Shipper giao → Customer review |
| **Customer tạo đơn MoMo** | Mock payment → Admin thấy → Shipper giao |
| **Customer yêu cầu return** | Admin approve/reject return |

> [!IMPORTANT]
> E2E cần cả 3 app chạy đồng thời.
> Shipper dùng Firebase RTDB — đơn cần sync từ backend → Firebase.
> Admin gán shipper qua `adminOrderApi.assignShipper()`.

**Commit:** `test: e2e cross-app flow verified`

---

## Tóm tắt thứ tự commit

| # | Wave | Commit message | Ước lượng |
|---|------|----------------|-----------|
| 1 | Auth | `fix: auth flow - login, role guard, session` | 30 phút |
| 2 | Navigation | `fix: navigation & dashboard - tab switching, crash fixes` | 20 phút |
| 3 | Sản phẩm | `fix: product management - CRUD, validation, role restriction` | 45 phút |
| 4 | Cart & COD | `fix: cart & COD checkout - quantity, voucher, order creation` | 45 phút |
| 5 | Admin order | `fix: admin order management - status update, bulk, assign shipper` | 40 phút |
| 6 | Shipper giao | `fix: shipper delivery flow - status update, call, navigate` | 40 phút |
| 7 | Hậu giao | `fix: post-delivery - review, return, stats, history` | 30 phút |
| 8 | Bổ sung | `fix: supplementary features - payment, voucher, user mgmt, chat` | 45 phút |
| 9 | E2E | `test: e2e cross-app flow verified` | 30 phút |

**Tổng ước lượng: ~5h test thủ công + fix**
