# 🤖 Codex Prompt – Wave 5: Admin Order Management Code Review + Fix

> Copy toàn bộ phần trong block bên dưới → paste vào Codex để triển khai.

---

## PROMPT START

```
Bạn là Android developer senior. Nhiệm vụ: review code Quản lý Đơn hàng của Admin app (dự án Vựa Vui Vẻ), tìm và fix tất cả bug logic, đảm bảo build thành công.

Wave 1–4 đã fix xong. Wave 5 tập trung vào: Danh sách đơn hàng Admin, Chi tiết đơn, Đổi trạng thái đơn, Bulk update, Gán Shipper.

## Files BẮT BUỘC phải đọc và review

1. `app-admin/src/main/java/vn/vuavuive/admin/ui/orders/AdminOrderListFragment.java`
2. `app-admin/src/main/java/vn/vuavuive/admin/ui/orders/AdminOrderDetailActivity.java`
3. `app-admin/src/main/java/vn/vuavuive/admin/ui/orders/OrderAdapter.java`
4. `shared/src/main/java/vn/vuavuive/shared/data/api/AdminOrderApi.java`
5. `shared/src/main/java/vn/vuavuive/shared/data/dto/Order.java`
6. `shared/src/main/java/vn/vuavuive/shared/data/dto/OrderItem.java`

## Thông tin code đã đọc trước (hiện trạng chính xác)

### AdminOrderListFragment.java — ĐÃ BIẾT:

**Điểm TỐT (không cần sửa):**
- `isUiReady()` = `isAdded() && binding != null` → dùng ở đầu mọi callback ✓
- Tab filter với multi-status mapping: "shipping" → match cả "shipping"/"shipped"/"in_transit" ✓
- "pending" → match cả "pending"/"pending_payment"/"pending_approval" ✓
- `onResume()` có `loadOrders()` để refresh khi quay từ Detail về ✓
- Bulk action: reset multiSelectMode sau khi thực hiện ✓
- `onOrderSelectionChanged()` ẩn/hiện bulk action bar đúng ✓
- `exportFilteredOrdersCsv()` có audit guard, try-catch đầy đủ ✓

**Bug cần FIX:**

1. **Dòng 68-67: Null check sai thứ tự giống Wave 3**:
   ```java
   currentUser = sessionManager.getUser();
   if (currentUser == null) return;
   if (currentUser.getRole() != null) currentUser.setRole(currentUser.getRole().toLowerCase(Locale.getDefault())); // dùng sau null check → OK
   ```
   → Lần này thứ tự ĐÚNG (check null trước, set role sau). Không cần sửa. ✓

2. **Dòng 87: `tab.getTag()` cast sang String không null-check**:
   ```java
   currentStatusFilter = (String) tab.getTag();
   ```
   → Nếu tab nào bị tạo nhầm không có tag → `(String) null` = null → `applyFilters()` sẽ dùng `currentStatusFilter = null` → `null.equals(status)` → crash! Thêm null-safe:
   ```java
   Object tag = tab.getTag();
   currentStatusFilter = tag != null ? (String) tag : "all";
   ```

3. **Dòng 260-271: Bulk update — `selectedIds` là Set → loop gọi API cho từng ID, nhưng Toast "thành công" hiện NGAY TRƯỚC KHI bất kỳ API call nào trả về**:
   → Toast "Đã cập nhật hàng loạt X đơn hàng thành công" hiện tức thì, nhưng thực ra API chưa xong. Nếu API fail → user không biết. Cần: thêm counter, chỉ hiện Toast sau khi tất cả API đã response (hoặc ít nhất hiện Toast khác nhau cho thành công/thất bại).

4. **Dòng 264-268: Bulk update API callbacks đều rỗng `{}`**:
   ```java
   @Override public void onResponse(...) {}
   @Override public void onFailure(...) {}
   ```
   → Không có error handling khi bulk update fail. Cần đếm success/fail và báo cáo.

5. **Dòng 291-292: `exportFilteredOrdersCsv()` gọi `applyFilters()` rồi lại loop `allOrders` thủ công** — logic filter bị duplicate (dễ lệch nhau khi logic filter thay đổi). Nên lấy trực tiếp list hiện tại từ adapter thay vì re-filter `allOrders`.

6. **Dòng 320-322: `getContext().getContentResolver()`** — nếu fragment detach → NPE. Dùng `requireContext()` bên trong try-catch đã có → OK, nhưng tốt hơn nên check `getContext() != null` trước.

### AdminOrderDetailActivity.java — ĐÃ BIẾT:

**Điểm TỐT:**
- `isInitialSpinnerLoad` flag chống trigger API ngay khi spinner load lần đầu ✓
- `isAssignableStatus()` check đúng các status cho phép gán shipper ✓
- `setupReturnRequest()` ẩn/hiện layout đúng theo `return_requested` status ✓
- Error body extraction trong `updateOrderStatus` callback ✓
- `markPaid` callback update `order` object local → re-render ✓
- Dòng 68-74: Vẫn sai thứ tự null check giống `ProductEditActivity` (dùng `currentUser` trước, null check sau) ← CẦN FIX

**Bug cần FIX:**

1. **Dòng 68-74: Null check sai thứ tự** (BUG giống Wave 3):
   ```java
   currentUser = sessionManager.getUser();
   if (currentUser != null && currentUser.getRole() != null) { // OK
       currentUser.setRole(currentUser.getRole().toLowerCase()); // OK — dùng sau check null
   }
   if (currentUser == null) { // CHECK NULL SAU KHI ĐÃ DÙNG currentUser Ở TRÊN → về logic là OK vì if đầu check null trước
       finish();
       return;
   }
   ```
   → Thực ra logic này an toàn vì `if (currentUser != null && ...)` check null trước. Khác với Wave 3. Không cần sửa. ✓

2. **Dòng 89-113: `loadOrderDetails()` có `if (orderId != null) { ... return; }` rồi ngay sau có `renderOrderDetails(orderId)` bên ngoài if** — nếu orderId null (đã handled ở onCreate nên không thể null ở đây) thì `renderOrderDetails(null)` sẽ crash vì `order` chưa được set. Thêm null guard cho `order` trong `renderOrderDetails()`.

3. **Dòng 97: `renderOrderDetails(orderId)` trong `loadOrderDetails()` callback** — nhưng `orderId` là local parameter của `loadOrderDetails()` → đây là closure capture. Nếu activity bị destroy trước khi callback về → `isFinishing() || isDestroyed()` không được check. Thêm:
   ```java
   if (isFinishing() || isDestroyed()) return;
   ```
   ở đầu cả `onResponse` và `onFailure` của `loadOrderDetails`.

4. **Dòng 115-116: `renderOrderDetails()` sử dụng `order` object mà không null-check trước**:
   ```java
   binding.tvOrderIdTitle.setText("Mã đơn: " + (order.getOrderId() != null ? ... : order.getId()));
   ```
   → Nếu `order = null` → NullPointerException ngay dòng 117. Thêm null-check `if (order == null) { finish(); return; }` ở đầu `renderOrderDetails()`.

5. **Dòng 191: Tính subtotal**:
   ```java
   double subtotal = order.getSubtotal() > 0 ? order.getSubtotal() : (order.getTotalAmount() - order.getShippingFee() + order.getDiscount());
   ```
   → Nếu `getDiscount()` trả về số dương (đây là số tiền giảm) → phép tính `totalAmount - shippingFee + discount` sai! Phải là `totalAmount - shippingFee - discount`. **Hoặc** backend gửi discount là số âm? Cần kiểm tra `Order.getDiscount()` convention. Nếu discount là số dương thì đổi `+` thành `-`.

6. **Dòng 198: `"audit".equals(currentUser.getRole())`** — nếu role chưa được toLowerCase() → sẽ không match "AUDIT". Đảm bảo đổi sang `equalsIgnoreCase`.

7. **Dòng 209: `renderOrderDetails(orderId)` trong `markPaid` callback** — `orderId` là variable từ closure trong `loadOrderDetails()` → có thể đã null hoặc đây là field của activity?. Kiểm tra scoping. Nên dùng `order.getId()` thay vì `orderId` parameter.

8. **Dòng 304-306: `binding.tvCurrentShipper.setText()` dùng chuỗi tiếng Anh không dấu**:
   → Việt hóa: "Shipper hiện tại: ..." và "Chưa gán shipper"

9. **Dòng 335: `labels.add("Khong co shipper active")`** → Việt hóa: "Không có shipper đang hoạt động"

10. **Dòng 360-386: `setupShipperAssignment()` toast strings** → Việt hóa:
    - "Khong tai duoc danh sach shipper" → "Không tải được danh sách shipper"
    - "Vui long chon shipper" → "Vui lòng chọn shipper"
    - "Da gan shipper" → "Đã gán shipper thành công"
    - "Khong gan duoc shipper" → "Không gán được shipper"
    - "Loi gan shipper" → "Lỗi gán shipper: "
    - "Khong cap nhat duoc tra hang" → "Không cập nhật được trạng thái trả hàng"
    - "Loi ket noi" → "Lỗi kết nối"

## Checklist đầy đủ — Review và FIX

### 5.1 AdminOrderListFragment
- [ ] Dòng 87: null-safe `tab.getTag()` → `currentStatusFilter = tag != null ? (String) tag : "all"`
- [ ] Bulk update callbacks rỗng → thêm success/fail counter, hiện Toast sau khi hoàn thành:
  ```java
  // Pattern gợi ý:
  AtomicInteger successCount = new AtomicInteger(0);
  AtomicInteger failCount = new AtomicInteger(0);
  int total = selectedIds.size();
  for (String id : selectedIds) {
      adminOrderApi.updateOrderStatus(id, body).enqueue(new Callback<...>() {
          @Override public void onResponse(...) {
              if (response.isSuccessful() && ...) successCount.incrementAndGet();
              else failCount.incrementAndGet();
              if (successCount.get() + failCount.get() == total) {
                  runOnUiThread không dùng được trong Fragment → dùng requireActivity().runOnUiThread()
                  -> Toast "Cập nhật: X thành công, Y thất bại"
              }
          }
          @Override public void onFailure(...) {
              failCount.incrementAndGet();
              // tương tự
          }
      });
  }
  ```
  *Lưu ý*: `AtomicInteger` cần import `java.util.concurrent.atomic.AtomicInteger`
- [ ] Dòng 320-322: `getContext()` null check trước `getContentResolver()`

### 5.2 AdminOrderDetailActivity
- [ ] Thêm `if (isFinishing() || isDestroyed()) return;` vào `onResponse()` và `onFailure()` của `loadOrderDetails()` (dòng 93-108)
- [ ] Thêm `if (order == null) { finish(); return; }` ở đầu `renderOrderDetails()` (dòng 115)
- [ ] Kiểm tra và fix phép tính subtotal (dòng 191) — `+ order.getDiscount()` hay `- order.getDiscount()`? Đọc `Order.java` để xác định convention
- [ ] Dòng 198 và 249 và 407: đổi `"audit".equals(...)` thành `"audit".equalsIgnoreCase(...)` để tránh case mismatch
- [ ] Việt hóa toàn bộ Toast strings trong `setupShipperAssignment()` và `updateReturnStatus()` (xem danh sách ở trên)
- [ ] Dòng 209: kiểm tra `orderId` scoping trong `renderOrderDetails()` callback — dùng `order.getId()` để an toàn hơn

### 5.3 OrderAdapter
Đọc toàn bộ `OrderAdapter.java` và kiểm tra:
- [ ] Multi-select mode: `getSelectedOrderIds()` trả `Set<String>` — ID lấy từ đâu? `order.getId()` hay `order.getOrderId()`? Phải nhất quán với `updateOrderStatus(id, body)` gọi
- [ ] `onBindViewHolder()`: bind status color theo status code — null-safe?
- [ ] Long-press để enter multi-select mode — có confirm dialog không? Hay enable ngay?
- [ ] `updateData(List)`: `notifyDataSetChanged()` hay DiffUtil?
- [ ] Item null check trong `getItemAt(position)` — bound check đủ không?

### 5.4 AdminOrderApi (Retrofit interface)
Đọc và kiểm tra:
- [ ] `getOrders(status, page, limit, search, date)` — params đúng với backend?
- [ ] `updateOrderStatus(orderId, body)` — method là PUT hay PATCH? Body type là `Map<String,String>` hay DTO?
- [ ] `assignShipper(orderId, shipperId)` — trả `Call<Map<String,String>>` — body format gì?
- [ ] `markPaid(orderId)` — method là POST hay PUT?

### 5.5 Order.java (shared DTO)
Đọc và kiểm tra:
- [ ] `getDiscount()` — trả số dương (tiền giảm) hay số âm?
- [ ] `getSubtotal()` vs `getTotalAmount()` vs `getFinalAmount()` — khác nhau thế nào? Verify convention
- [ ] `getOrderId()` vs `getId()` — khi nào dùng cái nào?
- [ ] `getShipperId()` — null khi chưa gán shipper?
- [ ] `getReturnRequest()` — nullable, caller phải null-check
- [ ] `getItems()` — nullable, caller phải null-check trước `for` loop
- [ ] `getPayment()` — nullable, caller phải null-check

## Quy tắc khi fix

1. KHÔNG thay đổi business logic (bulk update logic, shipper assignment, status state machine)
2. KHÔNG thêm dependency mới (nếu cần AtomicInteger thì đã có trong Java standard library)
3. Giữ nguyên code style hiện tại
4. Comment tiếng Việt cho fix quan trọng
5. Giữ nguyên tất cả comment/docstring cũ

## Sau khi fix xong

1. Build: `./gradlew assembleDebug` (Windows: `gradlew.bat assembleDebug`)
2. Module cần build OK: `app-admin` (và `shared` nếu sửa Order.java)
3. Liệt kê thay đổi theo format:
   ```
   File: <tên file>
   Dòng: <số dòng>
   Vấn đề: <lỗi gì>
   Fix: <code sau khi sửa>
   ```

## Commit message (KHÔNG push — để user review)

```
git add -A
git commit -m "fix(codex): wave 5 - order mgmt null safety, bulk update feedback, role equalsIgnoreCase, i18n"
```
```

---

> ⚠️ **Lưu ý cho user sau khi Codex xong:**
> 1. **Quan trọng nhất**: Kiểm tra fix subtotal tính sai (cộng hay trừ discount) — dựa vào convention của `Order.getDiscount()`
> 2. Kiểm tra bulk update có hiện feedback thành công/thất bại từng lệnh không
> 3. Kiểm tra `isFinishing()` guard trong `loadOrderDetails()` callback
> 4. Build: `./gradlew assembleDebug`
> 5. OK → push GitHub → chuyển sang **Stream 2 Wave 5** (test Admin order list, bulk update, gán Shipper trên emulator)
