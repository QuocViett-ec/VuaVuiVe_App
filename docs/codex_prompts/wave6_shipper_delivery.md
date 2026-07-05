# 🤖 Codex Prompt – Wave 6: Shipper Delivery Code Review + Fix

> Copy toàn bộ phần trong block bên dưới → paste vào Codex để triển khai.

---

## PROMPT START

```
Bạn là Android developer senior. Nhiệm vụ: review code luồng Giao hàng của Shipper app (dự án Vựa Vui Vẻ), tìm và fix tất cả bug logic, đảm bảo build thành công.

Wave 1–5 đã fix xong. Wave 6 tập trung vào: ShipperMainActivity, ShipperOrderListFragment, ShipperOrderDetailActivity, FirebaseShipperRepository (Firebase RTDB real-time sync).

## Files BẮT BUỘC phải đọc và review

1. `app-shipper/src/main/java/vn/vuavuive/shipper/ui/main/ShipperMainActivity.java`
2. `app-shipper/src/main/java/vn/vuavuive/shipper/ui/main/ShipperPagerAdapter.java`
3. `app-shipper/src/main/java/vn/vuavuive/shipper/ui/order/ShipperOrderListFragment.java`
4. `app-shipper/src/main/java/vn/vuavuive/shipper/ui/order/ShipperOrderDetailActivity.java`
5. `app-shipper/src/main/java/vn/vuavuive/shipper/ui/order/ShipperOrderAdapter.java`
6. `app-shipper/src/main/java/vn/vuavuive/shipper/data/repository/FirebaseShipperRepository.java`
7. `app-shipper/src/main/java/vn/vuavuive/shipper/ui/auth/ShipperLoginActivity.java`

## Thông tin code đã đọc trước (hiện trạng chính xác)

### ShipperMainActivity.java — ĐÃ BIẾT:

**Điểm TỐT:**
- Session check: `!repository.isLoggedIn() || !sessionManager.isShipper()` → `goToLogin()` ✓
- `goToLogin()` dùng `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK` ✓
- `onDestroy()` → `repository.updateOnlineStatus(false)` → set offline khi bị kill ✓
- `TabLayoutMediator` đồng bộ tabs ✓
- Online toggle cập nhật Firebase + label/color tức thì ✓
- 4 tabs: "Cần giao", "Lịch sử", "Thống kê", "Cá nhân" ✓

**Điểm CẦN KIỂM TRA:**
- Dòng 40: `setContentView()` được gọi SAU `if (!repository.isLoggedIn() ...)` — nếu check fail thì gọi `goToLogin()` nhưng KHÔNG return → tiếp tục chạy `setContentView()`. Nhưng đã có `return` dòng 42. OK ✓
- `setupHeader()`: `tvName.setText(user.getName())` — nếu `tvName = null` (layout chưa set) → NPE vì `setContentView()` chưa gọi khi vào `setupHeader()`. Nhưng `setupHeader()` được gọi SAU `setContentView()` (dòng 46). OK ✓
- `onDestroy()` → `repository.updateOnlineStatus(false)` — không có error handling. Nếu Firebase call fail khi app kill → không hiển thị được error (đây là expected behavior). OK.
- **Vấn đề**: Không có `onStop()` set offline — nếu user chỉ nhấn Home (app vào background) → shipper vẫn hiện Online. Có thể cần set offline ở `onStop()` và online lại ở `onResume()`. Nhưng đây là product decision, không phải bug. Cần hỏi user.

### ShipperOrderListFragment.java — ĐÃ BIẾT:

**Điểm TỐT:**
- `ordersLiveData` variable giữ tham chiếu LiveData → có thể `removeObservers()` khi cần ✓
- `getViewLifecycleOwner()` cho LiveData observe ✓
- `applyFilterAndSearch()` null-safe cho tất cả fields ✓
- `filterByTab()` logic mapping status đúng ✓
- Chip filter mapping logic đúng: "PENDING" chip → match CONFIRMED/PREPARING/READY_FOR_PICKUP ✓
- Empty state toggle đúng ✓
- `result.data != null ? result.data : new ArrayList<>()` ✓

**Điểm CẦN KIỂM TRA:**
- Dòng 59-60: `ordersLiveData` được declare ở class level, nhưng **không có removeObserver trong `onDestroyView()`**. `onDestroyView()` (dòng 158-160) để rỗng! → Nếu Fragment được recreate (configuration change), observer cũ vẫn active → memory leak + duplicate updates:
  ```java
  @Override
  public void onDestroyView() {
      super.onDestroyView();
      // THIẾU: if (ordersLiveData != null) ordersLiveData.removeObservers(this);
  }
  ```
  → Thêm `removeObservers` ở `onDestroyView()`.

- Dòng 141: `Chip chip = new Chip(requireContext())` — nếu fragment detached → crash. Nhưng `setupFilterChips()` được gọi trong `onViewCreated()` → fragment attached. OK ✓

- Dòng 148-152: `chip.setOnCheckedChangeListener` — khi `chipGroupFilter` ở chế độ `SELECTION_MODE_SINGLE` (nếu có), uncheck một chip sẽ fire listener với `isChecked=false`. Logic `if (isChecked)` đã xử lý đúng. ✓

- **Vấn đề tiềm ẩn**: `statusFilter` default là "ALL" nhưng chip "Tất cả" được tạo với `checked=true` (addChip thứ nhất). Khi user switch tab → fragment recreate → `statusFilter` reset về "ALL" đúng. OK ✓

### ShipperOrderDetailActivity.java — ĐÃ BIẾT:

**Điểm TỐT:**
- `orderId == null` check và `finish()` ngay đầu `onCreate()` ✓
- `onResume()` → `fetchAndBind()` → refresh data khi quay lại ✓
- `liveData.removeObservers(this)` sau khi `updateStatus()` trả về kết quả — one-shot pattern đúng ✓
- Confirm dialog trước khi đổi status ✓
- `showFailReasonDialog()` → nếu chọn "Lý do khác" → `showCustomFailReasonDialog()` ✓
- Maps/Dial: check `resolveActivity()` trước khi start, có fallback URL ✓
- `order.getFailReason()` null-safe ✓
- `bindStatusBadge()` null-safe ✓

**Bug cần FIX:**

1. **Dòng 99: `fetchAndBind()` → `repository.getOrderDetail(orderId).observe(this, ...)`** — `observe(this, ...)` với `this` là Activity lifecycle. Nếu `fetchAndBind()` được gọi NHIỀU LẦN (vì `onResume()` gọi mỗi lần quay về), sẽ có NHIỀU observers cùng active → mỗi Firebase event sẽ callback nhiều lần → UI bị render nhiều lần. Cần `removeObservers()` trước khi `observe()` lại:
   ```java
   private void fetchAndBind() {
       repository.getOrderDetail(orderId).removeObservers(this); // thêm dòng này
       repository.getOrderDetail(orderId).observe(this, result -> { ... });
   }
   ```
   *Hoặc*: Lưu LiveData vào field và chỉ observe một lần trong `onCreate()`.

2. **Dòng 340-344: `paymentText()` — strings tiếng Anh**:
   ```java
   "Payment: Paid by MoMo. Do not collect cash."
   "Payment: MoMo X. Online payment has not been completed."
   "Payment: COD. Amount to collect: X đ"
   ```
   → Việt hóa hoàn toàn vì Shipper là người Việt:
   - `"Đã thanh toán qua MoMo. Không thu tiền mặt."`
   - `"MoMo - Chưa thanh toán. Cần liên hệ khách xác nhận."`
   - `"COD - Thu tiền mặt: " + fmt.format((long) total) + " đ"`

3. **Dòng 174: `(long) order.getFinalAmount()`** — nếu `getFinalAmount()` trả `double` và có decimal → cast sang `long` sẽ cắt phần thập phân. Ổn nếu đây là tiền VND (không có xu). Nhưng nên dùng `Math.round()` để an toàn hơn.

4. **Dòng 217-218: `order.getFailReason()`** — Trường hợp `status = FAILED` nhưng `getFailReason()` null → hiện "❌ Đơn hàng giao thất bại" không có lý do. Đây là hành vi mong đợi, không phải bug. ✓

5. **Dòng 66: `onBackPressed()`** — deprecated trong API 33+. Nên đổi thành `getOnBackPressedDispatcher().onBackPressed()` hoặc đơn giản là `finish()`.

### FirebaseShipperRepository.java — CHƯA ĐỌC:

**Cần đọc toàn bộ và kiểm tra:**
- Firebase RTDB listener lifecycle: attach ở đâu, detach ở đâu?
- `getMyOrders()` — lắng nghe node nào? `/orders/` hay `/shippers/{uid}/orders/`?
- `getOrderDetail(orderId)` — one-shot hay real-time listener?
- `updateOrderStatus(orderId, status, failReason)` — write vào RTDB hay gọi Backend REST API?
- `updateOnlineStatus(isOnline)` — write vào `/users/{uid}/onlineStatus`?
- `logout()` — Firebase signOut + clearSession + removeListeners?
- `isLoggedIn()` — check `FirebaseAuth.getCurrentUser() != null`?
- Error handling: Firebase DatabaseException → propagate đúng không?
- Memory leak: listeners không được detach → quan trọng nhất cần kiểm tra

### ShipperOrderAdapter.java — CHƯA ĐỌC:

Đọc và kiểm tra:
- `submitList()` (DiffUtil) hay `notifyDataSetChanged()`?
- `onBindViewHolder()`: status display text + color mapping đúng không?
- Click listener → mở `ShipperOrderDetailActivity` với `"order_id"` extra — consistent với activity nhận

## Checklist đầy đủ — Review và FIX

### 6.1 ShipperOrderListFragment
- [ ] **BUG MEMORY LEAK**: Thêm `removeObservers` vào `onDestroyView()`:
  ```java
  @Override
  public void onDestroyView() {
      super.onDestroyView();
      if (ordersLiveData != null) {
          ordersLiveData.removeObservers(getViewLifecycleOwner());
      }
  }
  ```
- [ ] Xem xét: đặt `masterList` cleanup trong `onDestroyView()` để tránh hold reference sau fragment detach

### 6.2 ShipperOrderDetailActivity
- [ ] **BUG DUPLICATE OBSERVER**: Sửa `fetchAndBind()` — removeObservers trước khi observe lại:
  ```java
  private void fetchAndBind() {
      LiveData<...> liveData = repository.getOrderDetail(orderId);
      liveData.removeObservers(this);
      liveData.observe(this, result -> {
          ...
      });
  }
  ```
- [ ] Việt hóa `paymentText()` — 3 strings tiếng Anh
- [ ] Dòng 66: đổi `onBackPressed()` → `finish()`
- [ ] `(long) order.getFinalAmount()` → `Math.round(order.getFinalAmount())`

### 6.3 FirebaseShipperRepository — ĐỌC VÀ FIX toàn bộ
- [ ] Kiểm tra mọi `ValueEventListener` / `ChildEventListener`: có được `removeEventListener()` ở đúng lifecycle không?
- [ ] `getMyOrders()`: listener attach ở đâu? Nếu không detach → memory leak ngay cả khi Fragment đã destroy
- [ ] `updateOrderStatus()`: nếu gọi cả RTDB lẫn REST API → đảm bảo cả 2 đều handle error
- [ ] `logout()`: kiểm tra có remove tất cả listeners trước khi signOut không
- [ ] Null safety: Firebase `DataSnapshot.getValue(Order.class)` có thể trả null khi node bị xoá
- [ ] Network error: `DatabaseError` → propagate như `Result.error(message)` không?

### 6.4 ShipperOrderAdapter
- [ ] Click item → `startActivity(intent với "order_id")` — key phải là `"order_id"` (lowercase, underscore) — nhất quán với `ShipperOrderDetailActivity.getIntent().getStringExtra("order_id")`
- [ ] `getItemAt(position)` hay `getItem(position)` — bound check
- [ ] Status display và color: "CONFIRMED" → "Chờ lấy hàng" → màu cam; "IN_TRANSIT" → "Đang giao" → màu xanh cam; "DELIVERED" → "Đã giao" → màu xanh lá

### 6.5 ShipperPagerAdapter
Đọc và kiểm tra:
- [ ] `createFragment(position)` trả đúng 4 Fragment cho 4 tab: 0=OrderList(active), 1=OrderList(history), 2=Stats, 3=Profile
- [ ] Fragment instances có bị recreate không cần thiết không?

### 6.6 ShipperLoginActivity — review lại (đã bỏ qua Wave 1)
Đọc toàn bộ và kiểm tra:
- [ ] Login success → kiểm tra role → nếu KHÔNG phải shipper → logout + Toast + stay on login
- [ ] `onCreate()` check session → nếu đã login → `startActivity(ShipperMainActivity)` + `finish()`
- [ ] Firebase auth: `signInWithEmailAndPassword()` error handling đầy đủ?
- [ ] Network error vs wrong credentials vs account disabled — phân biệt error messages?

## Quy tắc khi fix

1. KHÔNG thay đổi business logic (status state machine, Firebase node structure)
2. KHÔNG thêm dependency mới
3. Giữ nguyên code style hiện tại
4. Comment tiếng Việt cho fix quan trọng
5. Giữ nguyên tất cả comment/docstring cũ

## Sau khi fix xong

1. Build: `./gradlew assembleDebug` (Windows: `gradlew.bat assembleDebug`)
2. Module cần build OK: `app-shipper` (và `shared` nếu cần)
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
git commit -m "fix(codex): wave 6 - shipper memory leak, duplicate observer, firebase lifecycle, i18n"
```
```

---

> ⚠️ **Lưu ý cho user sau khi Codex xong:**
> 1. **Quan trọng nhất**: Kiểm tra `FirebaseShipperRepository` — listener lifecycle và memory leak là rủi ro lớn nhất trong Wave 6
> 2. Kiểm tra fix Duplicate Observer trong `fetchAndBind()` — tránh UI render nhiều lần
> 3. Kiểm tra `onDestroyView()` đã có `removeObservers()` chưa
> 4. Build: `./gradlew assembleDebug`
> 5. OK → push GitHub → chuyển sang **Stream 2 Wave 6** (test Shipper nhận đơn, bắt đầu giao, cập nhật trạng thái trên emulator)
