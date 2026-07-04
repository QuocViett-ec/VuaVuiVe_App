# 📱 Stream 2 – Test thủ công trên máy ảo (User)

> **Mục đích:** Sau khi Codex đã review & fix code (Stream 1) → User chạy app trên emulator/thiết bị thật để kiểm tra UI/UX và tích hợp thực tế.
>
> **Quy tắc:** Codex fix xong wave nào → User test wave đó → OK thì push GitHub → tiếp wave sau.
>
> **Cần:** Android Studio + Emulator hoặc thiết bị thật + Backend server chạy sẵn.

---

## Quy trình cho mỗi Wave

```
1. Codex báo đã fix xong Stream 1 wave X
2. Pull code mới nhất (nếu cần)
3. Build & install app lên emulator
4. Thực hiện từng bước test bên dưới
5. Ghi nhận lỗi (nếu có) → báo Codex fix tiếp
6. OK → commit + push GitHub
```

---

`## 🔴 Wave 1 – Auth & Session
`
### Test Admin App

| # | Bước | Kết quả mong đợi | ✅/❌ |
|---|------|-------------------|-------|
| 1 | Mở app Admin | Hiện màn hình Login với spinner chọn role | |
| 2 | Chọn "Admin" trong spinner | Email/password tự động điền | |
| 3 | Bấm "Đăng nhập" | Vào được Dashboard, hiện 5 tab dưới | |
| 4 | Quay lại, chọn "Staff" → login | Vào Dashboard, một số chức năng bị hạn chế | |
| 5 | Quay lại, chọn "Audit" → login | Vào Dashboard, các nút thêm/sửa/xoá bị ẩn hoặc disabled | |
| 6 | Quay lại, chọn "Customer" → login | **BỊ CHẶN** — Toast báo không có quyền, quay về Login | |
| 7 | Kill app → mở lại | **Vẫn ở Dashboard** (session persist), không cần login lại | |
| 8 | Bấm "Đăng xuất" | Quay về Login, xoá session | |

### Test Customer App

| # | Bước | Kết quả mong đợi | ✅/❌ |
|---|------|-------------------|-------|
| 1 | Mở app Customer | Hiện Login, email/pass đã prefill `customer@gmail.com` | |
| 2 | Bấm "Đăng nhập" | Vào app, hiện tab Products (mặc định) | |
| 3 | Thử login bằng tài khoản Shipper | **BỊ CHẶN** — Toast "Tài khoản shipper không được phép" | |
| 4 | Login bằng email/pass sai | Hiện thông báo lỗi, không crash | |
| 5 | Kill app → mở lại | Session còn, vào thẳng app | |
| 6 | Đăng xuất | Quay về Login | |

### Test Shipper App

| # | Bước | Kết quả mong đợi | ✅/❌ |
|---|------|-------------------|-------|
| 1 | Mở app Shipper | Nếu chưa login → hiện Login. Nếu đã login → vào thẳng | |
| 2 | Login bằng tài khoản Shipper | Vào màn hình chính 4 tab | |
| 3 | Login bằng tài khoản Customer | **BỊ CHẶN** — Toast + logout + quay về Login | |
| 4 | Kill app → mở lại | Session còn, skip Login | |

---

## 🟠 Wave 2 – Dashboard & Navigation

### Test Admin App

| # | Bước | Kết quả mong đợi | ✅/❌ |
|---|------|-------------------|-------|
| 1 | Bấm lần lượt 5 tab dưới | Dashboard → Đơn hàng → Sản phẩm → Voucher → Chat, không crash | |
| 2 | Ở Dashboard, kéo xuống xem stats | Dữ liệu hiện, hoặc "đang tải" nếu chưa có | |
| 3 | Ở Dashboard, bấm nút "Quản lý thành viên" | Chuyển sang UserListFragment | |
| 4 | Ở Dashboard, bấm nút "Vận chuyển" | Chuyển sang ShipmentListFragment | |
| 5 | Bấm Home (recent apps) → quay lại | App không crash, session giữ nguyên | |

### Test Customer App

| # | Bước | Kết quả mong đợi | ✅/❌ |
|---|------|-------------------|-------|
| 1 | App mở → tab Products được chọn mặc định | ✓ Có sản phẩm hiển thị | |
| 2 | Bấm tab: Home → Products → Cart → Orders → Account | Chuyển tab mượt, không crash | |
| 3 | Tab Cart: giỏ trống → hiện empty state | "Giỏ hàng trống" + nút "Mua sắm ngay" | |
| 4 | Tab Orders: chưa có đơn → hiện empty state | "Chưa có đơn hàng" | |

### Test Shipper App

| # | Bước | Kết quả mong đợi | ✅/❌ |
|---|------|-------------------|-------|
| 1 | 4 tab: Cần giao → Lịch sử → Thống kê → Cá nhân | Chuyển mượt, không crash | |
| 2 | Toggle switch "Online" | Bật/tắt, cập nhật Firebase | |
| 3 | Tab "Cá nhân" | Hiện tên, email, phone, tỉ lệ thành công | |

---

## 🟡 Wave 3 – Sản phẩm

### Test Admin App

| # | Bước | Kết quả mong đợi | ✅/❌ |
|---|------|-------------------|-------|
| 1 | Tab Sản phẩm → danh sách hiện | Có sản phẩm, hiện ảnh/tên/giá/stock | |
| 2 | Gõ tên trong ô tìm kiếm | Filter realtime, danh sách thu hẹp | |
| 3 | Chọn category từ spinner | Chỉ hiện sản phẩm đúng category | |
| 4 | Bấm chip "Sắp hết hàng" | Chỉ hiện sản phẩm stock thấp | |
| 5 | Bấm FAB "+" → mở form thêm SP | Form trống, đủ fields: tên, giá, ảnh, stock, unit, category | |
| 6 | Bỏ trống tên → bấm Lưu | Validation error: "Tên không được trống" | |
| 7 | Nhập giá ≤ 0 → bấm Lưu | Validation error | |
| 8 | Điền đầy đủ → chọn ảnh → Lưu | Thêm thành công, quay về danh sách, SP mới xuất hiện | |
| 9 | Bấm vào SP → sửa → Lưu | Cập nhật thành công | |
| 10 | Long-press SP → xoá | Confirm dialog → xoá → SP biến mất | |
| 11 | Login Audit → tab SP | FAB "+" ẩn, long-press không xoá được | |

### Test Customer App

| # | Bước | Kết quả mong đợi | ✅/❌ |
|---|------|-------------------|-------|
| 1 | Tab Products → danh sách SP | Hiện ảnh, tên, giá, rating | |
| 2 | Bấm vào 1 SP → chi tiết | Hiện đầy đủ: ảnh lớn, giá, mô tả, rating, nút thêm | |
| 3 | Tăng số lượng vượt stock | Không cho tăng quá stock | |
| 4 | Bấm "Thêm vào giỏ" | Toast thành công, badge cart tăng | |

---

## 🟢 Wave 4 – Giỏ hàng & Checkout COD

### Test Customer App

| # | Bước | Kết quả mong đợi | ✅/❌ |
|---|------|-------------------|-------|
| 1 | Tab Cart → có SP vừa thêm | Hiện đúng tên, giá, số lượng, tổng tiền | |
| 2 | Vuốt trái 1 item | Item bị xoá khỏi giỏ | |
| 3 | Xoá hết → empty state | "Giỏ hàng trống" hiện lên | |
| 4 | Thêm lại SP → bấm "Thanh toán" | Mở CheckoutActivity | |
| 5 | Bỏ trống họ tên/SĐT/địa chỉ → bấm Đặt | Báo lỗi validation | |
| 6 | Điền đủ thông tin | Tổng tiền = subtotal + 30.000đ ship | |
| 7 | Nhập mã `VUAVUIVE` → bấm Áp dụng | Giảm 15%, tổng cập nhật | |
| 8 | Nhập mã `FREESHIP` → bấm Áp dụng | Miễn phí ship 30.000đ | |
| 9 | Nhập mã sai → Áp dụng | Toast "Mã không hợp lệ" | |
| 10 | Chọn COD → bấm Đặt hàng | Toast "Đặt hàng thành công", quay về, giỏ hàng trống | |
| 11 | Bấm icon bản đồ cạnh địa chỉ | Mở MapPickerActivity, chọn → address fill | |

---

## 🔵 Wave 5 – Quản lý đơn hàng (Admin)

### Test Admin App

| # | Bước | Kết quả mong đợi | ✅/❌ |
|---|------|-------------------|-------|
| 1 | Tab Đơn hàng → danh sách đơn | Hiện đơn vừa đặt từ Customer (status: pending) | |
| 2 | Bấm các tab filter | Lọc đúng status cho từng tab | |
| 3 | Gõ tìm kiếm theo tên khách | Tìm đúng đơn | |
| 4 | Bấm vào 1 đơn → chi tiết | Hiện thông tin đầy đủ: items, địa chỉ, thanh toán, status | |
| 5 | Đổi status: pending → confirmed | Cập nhật thành công, badge đổi màu | |
| 6 | Đổi status: confirmed → shipping | Cập nhật thành công | |
| 7 | Bấm "Gán Shipper" → chọn shipper từ dropdown | Gán thành công | |
| 8 | Bấm "Mark Paid" (nếu COD chưa paid) | Đánh dấu đã thanh toán | |
| 9 | Long-press chọn nhiều đơn → Bulk update | Cập nhật hàng loạt thành công | |
| 10 | Login Audit → thử đổi status | Bị chặn, Toast read-only | |

---

## 🟣 Wave 6 – Shipper giao đơn

### Test Shipper App

| # | Bước | Kết quả mong đợi | ✅/❌ |
|---|------|-------------------|-------|
| 1 | Tab "Cần giao" → đơn assigned hiện | Đơn có status CONFIRMED/SHIPPING | |
| 2 | Bấm chip "Chờ lấy hàng" | Chỉ hiện CONFIRMED/PREPARING/READY_FOR_PICKUP | |
| 3 | Bấm chip "Đang giao" | Chỉ hiện IN_TRANSIT/SHIPPING | |
| 4 | Gõ tìm kiếm theo tên/SĐT/địa chỉ | Tìm đúng đơn | |
| 5 | Bấm vào 1 đơn → chi tiết | Thông tin khách, danh sách SP, tổng tiền, phương thức TT | |
| 6 | Bấm nút "Gọi điện" | Mở app Phone với SĐT khách | |
| 7 | Bấm "Chỉ đường" | Mở Google Maps với địa chỉ giao | |
| 8 | Bấm "Bắt đầu giao hàng" → Xác nhận | Status → IN_TRANSIT, nút đổi thành Delivered/Failed | |
| 9 | Bấm "Đã giao thành công" → Xác nhận | Status → DELIVERED, label xanh "Đã giao" | |
| 10 | (Test case khác) Bấm "Giao thất bại" | Dialog chọn lý do → chọn 1 → xác nhận → FAILED | |
| 11 | Chọn "Lý do khác (Nhập tay)" | Dialog nhập text → gõ lý do → gửi → FAILED | |
| 12 | COD order → payment text | Hiện "Amount to collect: X đ" | |

---

## ⚫ Wave 7 – Hậu giao hàng

### Test Customer App

| # | Bước | Kết quả mong đợi | ✅/❌ |
|---|------|-------------------|-------|
| 1 | Tab Orders → đơn status "Đang chờ" | Bấm vào → nút "Huỷ đơn" hiện | |
| 2 | Bấm "Huỷ đơn" → xác nhận | Đơn chuyển sang "Đã huỷ" | |
| 3 | Đơn "Đã giao" → bấm vào | Nút "Trả hàng" + "Đánh giá" hiện | |
| 4 | Bấm "Trả hàng" → bỏ trống lý do → Gửi | Báo lỗi "Vui lòng nhập lý do" | |
| 5 | Nhập lý do → Gửi | Toast "Đã gửi yêu cầu trả hàng" | |
| 6 | Bấm "Đánh giá" | Chọn sản phẩm (nếu nhiều) → form đánh giá hiện | |

### Test Shipper App

| # | Bước | Kết quả mong đợi | ✅/❌ |
|---|------|-------------------|-------|
| 7 | Tab "Lịch sử" | Hiện đơn DELIVERED/FAILED | |
| 8 | Tab "Thống kê" | Hiện tổng doanh thu, COD/Online, tỉ lệ | |

### Test Admin App

| # | Bước | Kết quả mong đợi | ✅/❌ |
|---|------|-------------------|-------|
| 9 | Đơn return_requested → mở chi tiết | Section "Yêu cầu trả hàng" hiện | |
| 10 | Bấm "Chấp nhận trả" | Status → returned | |
| 11 | (Test khác) Bấm "Từ chối" | Status → delivered (giữ nguyên) | |

---

## ⬛ Wave 8 – Tính năng bổ sung

### Test Customer App

| # | Bước | Kết quả mong đợi | ✅/❌ |
|---|------|-------------------|-------|
| 1 | Checkout → chọn MoMo → Đặt hàng | Mở WebView/deeplink MoMo (mock) | |
| 2 | Checkout → chọn ZaloPay → Đặt hàng | Mở WebView/deeplink ZaloPay (mock) | |
| 3 | Tab Account → xem profile | Hiện tên, email, phone | |
| 4 | Bấm "Sửa hồ sơ" → đổi tên → Lưu | Cập nhật thành công | |
| 5 | Bấm "Đổi mật khẩu" → nhập cũ/mới | Đổi thành công (hoặc lỗi nếu sai MK cũ) | |

### Test Admin App

| # | Bước | Kết quả mong đợi | ✅/❌ |
|---|------|-------------------|-------|
| 6 | Tab Voucher → danh sách voucher | Hiện data (MockRepository) | |
| 7 | Bấm FAB "+" → thêm voucher | Form thêm mới (chỉ Admin) | |
| 8 | Login Staff → Tab Voucher | FAB ẩn, chỉ xem được | |
| 9 | Dashboard → nút "Quản lý thành viên" | Hiện danh sách users (MockRepository) | |
| 10 | Admin đổi role 1 user | Chọn role mới → thành công | |
| 11 | Audit thử đổi status user | Bị chặn Toast read-only | |
| 12 | Dashboard → nút "Vận chuyển" | Hiện danh sách shipments | |

---

## ⭐ Wave 9 – Test liên thông (E2E)

> ⚠️ Cần chạy cả 3 app đồng thời (Admin + Customer + Shipper)

### Kịch bản 1: Đặt hàng COD → Giao thành công → Review

| # | App | Bước | Kết quả | ✅/❌ |
|---|-----|------|---------|-------|
| 1 | Customer | Thêm SP vào giỏ → Checkout COD → Đặt hàng | Đơn tạo thành công | |
| 2 | Admin | Tab Đơn hàng → thấy đơn mới (pending) | Đơn hiện đúng | |
| 3 | Admin | Đổi status: confirmed → Gán shipper | Shipper được assign | |
| 4 | Shipper | Tab Cần giao → đơn mới hiện | Đơn hiện đúng thông tin | |
| 5 | Shipper | Bắt đầu giao → Giao thành công | Status → DELIVERED | |
| 6 | Admin | Đơn chuyển sang "Đã giao" | Status đồng bộ | |
| 7 | Customer | Tab Orders → đơn "Đã giao" → Đánh giá | Review thành công | |

### Kịch bản 2: Trả hàng

| # | App | Bước | Kết quả | ✅/❌ |
|---|-----|------|---------|-------|
| 1 | Customer | Đơn delivered → Yêu cầu trả hàng | Request gửi | |
| 2 | Admin | Đơn return_requested → Chấp nhận/Từ chối | Status cập nhật | |

### Kịch bản 3: Giao thất bại

| # | App | Bước | Kết quả | ✅/❌ |
|---|-----|------|---------|-------|
| 1 | Shipper | Đơn IN_TRANSIT → Giao thất bại + lý do | Status → FAILED | |
| 2 | Admin | Đơn hiện "Thất bại" | Status đồng bộ | |

---

## 📋 Bảng tóm tắt: Stream 1 vs Stream 2

| Wave | Stream 1 (Codex review code) | Stream 2 (User test emulator) | Commit |
|------|------------------------------|-------------------------------|--------|
| 1 | Auth logic, null checks, role guards | Login 3 app, session persist | `fix: auth flow` |
| 2 | Fragment transactions, lifecycle | Tab switching, crash test | `fix: navigation` |
| 3 | Validation, role permissions, API error | CRUD sản phẩm, Audit readonly | `fix: products` |
| 4 | Cart logic, race condition, voucher | Giỏ hàng, voucher codes, COD | `fix: cart & checkout` |
| 5 | Status mapping, bulk update, CSV | Admin quản lý đơn, gán shipper | `fix: admin orders` |
| 6 | Firebase data flow, state machine | Shipper giao hàng, call/navigate | `fix: shipper` |
| 7 | Cancel/return logic, stats calc | Review, trả hàng, thống kê | `fix: post-delivery` |
| 8 | MockRepository, payment flow | MoMo/ZaloPay, voucher, user mgmt | `fix: supplementary` |
| 9 | Data consistency cross-module | 3 app đồng thời, E2E | `test: e2e verified` |
