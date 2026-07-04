# 🤖 Codex Prompt – Wave 4: Cart & Checkout COD Code Review + Fix

> Copy toàn bộ phần trong block bên dưới → paste vào Codex để triển khai.

---

## PROMPT START

```
Bạn là Android developer senior. Nhiệm vụ: review code Giỏ hàng & Checkout của dự án Vựa Vui Vẻ (Customer app), tìm và fix tất cả bug logic, đảm bảo build thành công.

Wave 1–3 đã fix xong. Wave 4 tập trung vào: CartFragment, CartAdapter, CartViewModel, FirebaseCartRepository, CheckoutActivity, MapPickerActivity.

## Files BẮT BUỘC phải đọc và review

1. `app-customer/src/main/java/vn/vuavuive/customer/ui/cart/CartFragment.java`
2. `app-customer/src/main/java/vn/vuavuive/customer/ui/cart/CartAdapter.java`
3. `app-customer/src/main/java/vn/vuavuive/customer/viewmodel/CartViewModel.java`
4. `app-customer/src/main/java/vn/vuavuive/customer/data/repository/FirebaseCartRepository.java`
5. `app-customer/src/main/java/vn/vuavuive/customer/ui/checkout/CheckoutActivity.java`
6. `app-customer/src/main/java/vn/vuavuive/customer/ui/checkout/MapPickerActivity.java`
7. `app-customer/src/main/java/vn/vuavuive/customer/viewmodel/OrderViewModel.java`
8. `shared/src/main/java/vn/vuavuive/shared/data/local/CartItemEntity.java`
9. `shared/src/main/java/vn/vuavuive/shared/data/dto/request/CreateOrderRequest.java`
10. `shared/src/main/java/vn/vuavuive/shared/data/dto/DeliveryInfo.java`

## Thông tin code đã đọc trước (hiện trạng chính xác)

### CartFragment.java — ĐÃ BIẾT:

**Điểm TỐT:**
- `getViewLifecycleOwner()` cho LiveData observe — đúng, tránh leak ✓
- `items != null ? items : new ArrayList<>()` null guard ✓
- `authViewModel.isLoggedIn()` check trước khi navigate Checkout ✓
- `getItemAt(position)` null check trước `removeItem()` ✓
- `layoutEmptyCart` và `layoutCartContent` visibility toggle đúng ✓

**Điểm CẦN KIỂM TRA / CÓ THỂ CÓ BUG:**
- Dòng 139: `viewHolder.getAdapterPosition()` — trong RecyclerView mới nên dùng `viewHolder.getBindingAdapterPosition()`. `getAdapterPosition()` bị deprecated API 29+ và có thể trả `NO_POSITION (-1)` khi animate. Dù đã có `cartAdapter.getItemAt(position)` null-check, nhưng cần thêm `if (position == RecyclerView.NO_POSITION) { cartAdapter.notifyItemChanged(position); return; }` trước khi getItemAt.
- `updateTotal()` dòng 180-188: gọi `item.getLineTotal()` — cần kiểm tra `CartItemEntity.getLineTotal()` có null-safe không (productPrice có thể 0.0 mặc định).
- `layoutCartContent` dòng 199: dùng trực tiếp không null-check → nếu `findViewById` trả null → NullPointerException. Thêm null-check.
- `updateSavedSection()` dòng 219: `tvSavedToggle.setText(savedExpanded ? "^" : "v")` — nên dùng icon/drawable thay vì ký tự "^" và "v".

### CheckoutActivity.java — ĐÃ BIẾT:

**Điểm TỐT:**
- `cartLoaded` guard flag chống race condition ✓
- `btnPlaceOrder.setEnabled(false)` cho đến khi LiveData fire ✓
- `Math.max(subtotal + shipping - discount, 0)` tránh tổng âm ✓
- `authViewModel.isLoggedIn()` check ở `onCreate()` ✓
- `setLoading(true/false)` disable button khi đang gọi API ✓
- Map picker result null-check ✓
- COD: `cartViewModel.clearCart()` → `finish()` ✓
- MoMo/ZaloPay: intent extras đầy đủ ✓

**Bug cần FIX:**
1. **Dòng 231: `rgPaymentMethod.getCheckedRadioButtonId()`** — nếu không có RadioButton nào được check (layout chưa load xong) → trả `-1` → `method` = COD (default). Cần verify `rgPaymentMethod != null` trước.

2. **Dòng 279: `orderViewModel.createOrder(request).observe(this, result -> {...})`** — nếu `createOrder()` trả về LiveData và được observe nhiều lần (vd: orientation change giữa chừng) → **duplicate orders**! Cần dùng `SingleLiveEvent` hoặc sau khi observe xong thì `removeObservers()`. Kiểm tra `OrderViewModel.createOrder()` — nếu trả LiveData thường thì cần fix.

3. **Dòng 282: `result.data.getId() != null ? result.data.getId() : result.data.getOrderId()`** — nếu cả hai đều null → `orderId = null` → `trackPurchaseEvents(null)` → `createMomoPayment(null, ...)` → crash hoặc API error. Thêm guard: nếu `orderId == null || orderId.isEmpty()` → Toast lỗi + finish.

4. **Dòng 293: `result.data.getFinalAmount()`** — `getFinalAmount()` có thể trả 0.0 nếu field null → MoMo/ZaloPay nhận amount = 0. Fallback: dùng `total` từ tính toán local nếu `getFinalAmount()` = 0.

5. **Dòng 315: `"Thanh toan don hang Vua Vui Ve: " + orderId`** — ZaloPay description không dấu (đúng vì ZaloPay API thường yêu cầu ASCII). OK không cần sửa.

6. **Thread safety (dòng 203-218)**: `new Thread(() -> { ... cartViewModel.getCartItemsSync() })` — `getCartItemsSync()` comment rõ "MUST be called from background thread" → đúng. Nhưng sau đó gọi `runOnUiThread()` để update UI — OK. Tuy nhiên, nếu activity bị destroy trước khi thread xong → `runOnUiThread()` crash nếu activity null. Thêm `if (isFinishing() || isDestroyed()) return;` trong `runOnUiThread`.

### CartViewModel.java — ĐÃ BIẾT:
- Wrapper đơn giản gọi `FirebaseCartRepository` ✓
- `getCartItemsSync()` comment rõ cần background thread ✓
- `removeLegacyMockItems()` — được gọi ở đâu? Kiểm tra lifecycle.

## Checklist đầy đủ — Review và FIX

### 4.1 CartFragment
- [ ] Dòng 139: Thêm `NO_POSITION` guard trước `getItemAt(position)`:
  ```java
  int position = viewHolder.getBindingAdapterPosition(); // đổi sang BindingAdapterPosition
  if (position == RecyclerView.NO_POSITION) {
      cartAdapter.notifyItemChanged(viewHolder.getBindingAdapterPosition());
      return;
  }
  ```
- [ ] Dòng 199: Thêm null-check cho `layoutCartContent` trước khi gọi `setVisibility()`
- [ ] `updateTotal()`: kiểm tra `item.getLineTotal()` — nếu `productPrice` default là 0.0 thì result đúng, không crash. Đọc `CartItemEntity.getLineTotal()` để confirm.
- [ ] `tvSavedToggle` ký tự "^"/"v" — đổi thành ▲/▼ (Unicode) hoặc rotation animation cho UX tốt hơn

### 4.2 CartAdapter
Đọc toàn bộ `CartAdapter.java` và kiểm tra:
- [ ] `getItemAt(position)` — bound check: `position >= 0 && position < items.size()`
- [ ] `onBindViewHolder()` — tất cả `item.getProductName()`, `item.getProductPrice()`, `item.getProductImageUrl()` có null-safe setText không?
- [ ] Quantity +/- buttons — kiểm tra `quantity >= 1` (không cho về 0) và `quantity <= productStock`
- [ ] "Lưu lại" / "Move to cart" button callback đúng không?
- [ ] Image load (Glide/Picasso): null URL → placeholder
- [ ] `setItems()`: `notifyDataSetChanged()` hay `DiffUtil`? Nếu dùng `notifyDataSetChanged()` → OK nhưng kém hiệu quả

### 4.3 FirebaseCartRepository
Đọc toàn bộ và kiểm tra:
- [ ] Firebase listener: có `removeEventListener()` ở đúng lifecycle không?
- [ ] `addItem()`: nếu product đã có trong cart → update quantity (tăng thêm), KHÔNG tạo duplicate entry
- [ ] `removeItem()`: `productId` null check
- [ ] `clearCart()`: xoá hết cả cart + saved items? Hay chỉ cart?
- [ ] `getCartItemsSync()`: đây là Room query hay Firebase? Nếu Firebase thì synchronous call có chạy được không?
- [ ] `onUserLoggedIn()` / `onUserLoggedOut()`: lifecycle đúng? Listener được attach/detach đúng chỗ?
- [ ] Error handling: Firebase operation fail → silent fail hay có callback?

### 4.4 CheckoutActivity
- [ ] **BUG**: Thêm guard `rgPaymentMethod != null` trước `.getCheckedRadioButtonId()`
- [ ] **BUG DUPLICATE ORDER**: Kiểm tra `OrderViewModel.createOrder()` — nếu trả LiveData thông thường, phải gọi `removeObservers()` sau khi handle response, HOẶC đổi sang `SingleLiveEvent`
- [ ] **BUG**: `orderId == null || orderId.isEmpty()` → Toast lỗi + `finish()` — KHÔNG tiếp tục gọi createMomoPayment/ZaloPay
- [ ] **BUG**: Thread `isFinishing() || isDestroyed()` check trong `runOnUiThread()` callback
- [ ] `getFinalAmount()` fallback: nếu = 0.0, dùng `total` từ `Math.max(subtotal + 30_000 - appliedDiscount, 0)`
- [ ] Voucher `VUAVUIVE` — discount tính theo subtotal TẠI THỜI ĐIỂM apply. Nếu sau đó user thay đổi cart (quay lại) → discount cũ sẽ sai. Thêm note/comment giải thích limitation này.
- [ ] Phone validation: kiểm tra SĐT có đúng 10 số và bắt đầu bằng 0 không? Hiện tại chỉ check `isEmpty()`.
- [ ] Address validation: tối thiểu phải có phường/quận/tỉnh (> 10 ký tự). Hiện tại chỉ check `isEmpty()`.

### 4.5 OrderViewModel
Đọc toàn bộ `OrderViewModel.java` và kiểm tra:
- [ ] `createOrder()` trả loại LiveData gì? `MutableLiveData` thông thường hay `SingleLiveEvent`?
- [ ] `createMomoPayment()` và `createZaloPayPayment()` — tương tự
- [ ] Error propagation: API error → `Result.error(message)` → CheckoutActivity hiện Toast
- [ ] Nếu dùng `MutableLiveData`, phải đảm bảo sau mỗi `observe()` phải `removeObservers()` hoặc dùng pattern observer một lần

### 4.6 CartItemEntity (shared)
Đọc `CartItemEntity.java` và kiểm tra:
- [ ] `getLineTotal()` = `productPrice * quantity` — nếu `productPrice` null (0.0 mặc định) → lineTotal = 0, không crash. OK?
- [ ] Fields: `productId`, `productName`, `productPrice`, `productImageUrl`, `productUnit`, `productStock`, `quantity`, `savedForLater`, `addedAt` — tất cả đã có getter/setter?
- [ ] `@Entity` annotation nếu dùng Room: `tableName`, `primaryKey` đúng chưa?
- [ ] Hoặc nếu là POJO: serializable/parcelable nếu pass qua Intent?

### 4.7 CreateOrderRequest (shared)
Đọc `CreateOrderRequest.java` và kiểm tra:
- [ ] `OrderItemRequest` inner class: `productId`, `productName`, `quantity`, `price`, `subtotal`, `unit`, `imageUrl` — đủ fields?
- [ ] JSON field names có match với backend API? Dùng `@SerializedName` nếu cần

### 4.8 MapPickerActivity
Đọc `MapPickerActivity.java` và kiểm tra:
- [ ] Nếu dùng Google Maps API: API key đã khai báo trong `AndroidManifest.xml`?
- [ ] Kết quả trả về: `Intent.putExtra(EXTRA_ADDRESS, addressString)` → `setResult(RESULT_OK, intent)`
- [ ] Null/empty address → không setResult hoặc setResult với empty string → kiểm tra caller handle đúng không

## Quy tắc khi fix

1. KHÔNG thay đổi business logic (voucher codes, price calculation, payment methods)
2. KHÔNG thêm dependency mới
3. Giữ nguyên code style hiện tại
4. Comment tiếng Việt cho fix quan trọng
5. Giữ nguyên tất cả comment/docstring cũ

## Sau khi fix xong

1. Build: `./gradlew assembleDebug` (Windows: `gradlew.bat assembleDebug`)
2. Module cần build OK: app-customer (và shared nếu sửa CartItemEntity/CreateOrderRequest)
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
git commit -m "fix(codex): wave 4 - cart swipe guard, checkout duplicate order, null safety, validation"
```
```

---

> ⚠️ **Lưu ý cho user sau khi Codex xong:**
> 1. **Quan trọng nhất**: Kiểm tra fix Duplicate Order bug — `createOrder().observe()` phải `removeObservers()` sau khi handle
> 2. Kiểm tra fix `orderId == null` guard trước khi gọi MoMo/ZaloPay
> 3. Kiểm tra `isFinishing()` guard trong background thread callback
> 4. Build: `./gradlew assembleDebug`
> 5. OK → push GitHub → chuyển sang **Stream 2 Wave 4** (test giỏ hàng + checkout COD trên emulator)
