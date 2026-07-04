# 🤖 Codex Prompt – Wave 3: Product Management Code Review + Fix

> Copy toàn bộ phần trong block bên dưới → paste vào Codex để triển khai.

---

## PROMPT START

```
Bạn là Android developer senior. Nhiệm vụ: review code quản lý Sản phẩm của dự án Vựa Vui Vẻ (Admin + Customer app), tìm và fix tất cả bug logic, đảm bảo build thành công.

Wave 1 (Auth) và Wave 2 (Navigation) đã fix xong. Wave 3 tập trung vào: Product CRUD, validation, role restriction, filter/search, và Customer product browsing.

## Files BẮT BUỘC phải đọc và review

### App Admin
1. `app-admin/src/main/java/vn/vuavuive/admin/ui/products/AdminProductListFragment.java`
2. `app-admin/src/main/java/vn/vuavuive/admin/ui/products/ProductEditActivity.java`
3. `app-admin/src/main/java/vn/vuavuive/admin/ui/products/ProductAdapter.java`
4. `shared/src/main/java/vn/vuavuive/shared/data/api/AdminProductApi.java`
5. `shared/src/main/java/vn/vuavuive/shared/data/dto/Product.java`
6. `shared/src/main/java/vn/vuavuive/shared/data/dto/CategoryResponse.java`

### App Customer
7. `app-customer/src/main/java/vn/vuavuive/customer/ui/product/ProductListFragment.java`
8. `app-customer/src/main/java/vn/vuavuive/customer/ui/product/ProductDetailActivity.java`
9. `app-customer/src/main/java/vn/vuavuive/customer/viewmodel/ProductViewModel.java`
10. `app-customer/src/main/java/vn/vuavuive/customer/data/MockDataProvider.java`

## Thông tin code đã đọc trước (để bạn biết rõ hiện trạng)

### AdminProductListFragment — ĐÃ BIẾT:

**Điểm TỐT (không cần sửa):**
- `isUiReady()` check `isAdded() && binding != null` → dùng ở đầu mọi callback ✓
- `onFailure` có error Toast ✓
- `onResume()` có `loadProducts()` ✓
- `onProductLongClick()` có confirm dialog ✓
- `applyFilters()` có null check cho `allProducts` ✓
- `safeCsv()` xử lý null ✓
- Delete callback có `binding == null || !isAdded()` check ✓

**Điểm CẦN KIỂM TRA:**
- `selectCategory()` (dòng 179-183): gọi `loadProducts()` và `applyFilters()` cùng lúc — `loadProducts()` async sẽ override filter sau. Logic có đúng không?
- Chip chips (all/fruit/veg/dry) chỉ có 4 loại nhưng spinner có 11 loại — chip filter và spinner filter CÓ THỂ CONFLICT: chip set `currentCategoryFilter` và spinner cũng set `currentCategoryFilter` → không đồng bộ UI
- `exportProductsCsv()` gọi `requireContext()` ở dòng 293/295 — nếu fragment detach → crash. Nên dùng `getContext()` và null-check
- `setupFiltersAndFab()`: khi Audit bấm FAB → Toast "Read-only account" (tiếng Anh) — cần Việt hóa: "Tài khoản chỉ đọc, không thể thêm sản phẩm"

### ProductEditActivity — ĐÃ BIẾT:

**Điểm TỐT (không cần sửa):**
- `currentUser == null` → `finish()` ✓
- `isFinishing() || isDestroyed()` check trong mọi callback ✓
- `enforceRolePermissions()` disable tất cả field cho Audit ✓
- Validation chain đầy đủ: name, price, originalPrice >= price, stock >= 0, unit ✓
- Image upload flow: disable button khi uploading, re-enable khi xong ✓
- `pendingImageUri != null && selectedImageUrl == null` → block save ✓

**Bug cần FIX:**
1. **Dòng 71-74: Null check SAI THỨ TỰ** — set role TRƯỚC rồi mới check null:
   ```java
   if (currentUser != null && currentUser.getRole() != null) {
       currentUser.setRole(currentUser.getRole().toLowerCase()); // dòng 72
   }
   if (currentUser == null) { // dòng 74 — check null SAU KHI đã dùng currentUser ở trên!
       finish();
       return;
   }
   ```
   → Cần đổi: check null TRƯỚC, rồi mới set role.

2. **`enforceRolePermissions()` (dòng 277)**: gọi `currentUser.getRole()` KHÔNG null-check:
   ```java
   if (!"audit".equals(currentUser.getRole())) return;
   ```
   → Nếu role null → `!"audit".equals(null)` = true → không block audit. 
   → Đổi thành: `if (currentUser == null || !"audit".equalsIgnoreCase(currentUser.getRole())) return;`

3. **`saveProduct()` (dòng 293)**: `binding.btnSaveProduct.isEnabled()` check — nhưng Audit đã override `setOnClickListener` → Toast thay vì guard → OK. Không cần sửa.

4. **`selectedCategoryId()` (dòng 399-403)**: Nếu `categories` còn trống khi spinner chưa load xong → `position >= categories.size()` → return null → Toast "Chua co danh muc hop le". Đây là UX kém. Thêm Toast rõ hơn: "Danh mục chưa tải xong, vui lòng đợi..."

5. **Toàn bộ Toast strings**: đang dùng tiếng Anh/Latinh không dấu. Cần Việt hóa các Toast phổ biến:
   - "Nhap ten san pham" → "Vui lòng nhập tên sản phẩm"
   - "Gia khong hop le" → "Giá bán không hợp lệ (phải > 0)"
   - "Gia goc phai >= gia ban" → "Giá gốc phải lớn hơn hoặc bằng giá bán"
   - "Ton kho khong hop le" → "Tồn kho không hợp lệ (phải ≥ 0)"
   - "Nhap don vi" → "Vui lòng nhập đơn vị (kg, hộp, bó...)"
   - "Da luu san pham" → "Đã lưu sản phẩm thành công"
   - "Luu that bai" → "Lưu sản phẩm thất bại, vui lòng thử lại"
   - "Khong tim thay san pham" → "Không tìm thấy sản phẩm"
   - "Da tai anh" → "Tải ảnh thành công"
   - "Tai anh that bai" → "Tải ảnh thất bại"

### ProductDetailActivity (Customer) — ĐÃ BIẾT:

**Điểm TỐT (không cần sửa):**
- Mock data load trước → real API load sau (non-blocking) → UX tốt ✓
- `product.getStock() <= 0` → "Hết hàng" + disable FAB ✓
- `quantity < maxStock` check khi tăng số lượng ✓
- `currentProduct.getId().startsWith("mock_")` → block add to cart ✓
- `rvReviews.setNestedScrollingEnabled(false)` ✓
- Rating null → default 0f ✓

**Điểm CẦN KIỂM TRA:**
- `addToCart()` dòng 387: check `currentProduct.getId().startsWith("11111111") || currentProduct.getId().startsWith("mock_")` — nhưng nếu `getId()` null → NullPointerException! Cần null check trước.
- `bindProduct()` dòng 251: `ctl.setTitle(product.getName())` — nếu `product.getName()` null → setTitle("null") → hiện chữ "null". Thêm null check.
- `tryLoadFromApi()` wrap trong try-catch Exception rộng → ẩn hết lỗi. Chỉ nên catch lỗi cụ thể, không catch Exception chung.

## Checklist đầy đủ — Review và FIX

### 3.1 AdminProductListFragment
- [ ] **BUG**: Chip filter + Spinner filter conflict — khi chọn chip "fruit" → `currentCategoryFilter = "fruit"`, nhưng spinner vẫn hiện "Tat ca" → inconsistent UI. Giải pháp: khi chọn chip → reset spinner về vị trí tương ứng (hoặc ít nhất reset về "Tat ca" = index 0). Ngược lại khi chọn spinner → deselect tất cả chip.
- [ ] `exportProductsCsv()` dùng `requireContext()` → đổi sang `getContext()` + null check trước khi gọi
- [ ] Toast Audit FAB block → Việt hóa: "Tài khoản chỉ đọc, không thể thêm sản phẩm"
- [ ] Toast `"San pham khong hop le"` → "Sản phẩm không hợp lệ"

### 3.2 ProductEditActivity
- [ ] **BUG CRITICAL**: Đổi thứ tự null check (dòng 71-77): null check currentUser TRƯỚC, rồi mới setRole
  ```java
  // Đúng:
  currentUser = sessionManager.getUser();
  if (currentUser == null) { finish(); return; }
  if (currentUser.getRole() != null) {
      currentUser.setRole(currentUser.getRole().toLowerCase());
  }
  ```
- [ ] **BUG**: `enforceRolePermissions()` → đổi thành `equalsIgnoreCase` và thêm null guard:
  ```java
  if (currentUser == null || !"audit".equalsIgnoreCase(currentUser.getRole())) return;
  ```
- [ ] `selectedCategoryId()` toast → đổi thành "Danh mục chưa tải xong, vui lòng đợi..."
- [ ] Việt hóa tất cả Toast strings (xem danh sách ở trên)

### 3.3 Customer ProductDetailActivity
- [ ] `addToCart()` dòng 387 → thêm `currentProduct.getId() == null` check TRƯỚC `startsWith()`
  ```java
  String id = currentProduct.getId();
  if (id == null || id.startsWith("11111111") || id.startsWith("mock_")) {
      Toast...
      return;
  }
  ```
- [ ] `bindProduct()` `ctl.setTitle(product.getName())` → null-safe: `ctl.setTitle(product.getName() != null ? product.getName() : "")`
- [ ] Xem `ProductListFragment.java` và kiểm tra: filter, search, load more pagination có đúng không?

### 3.4 ProductListFragment (Customer) — đọc và review toàn bộ
- [ ] API error → hiện empty state hay Toast?
- [ ] Search: debounce hay gọi API mỗi keystroke?
- [ ] Category filter + search kết hợp: logic đúng không?
- [ ] `onResume()` có reload không? (cần thiết sau khi quay từ ProductDetail về)
- [ ] Empty state: không có sản phẩm → hiện gì?
- [ ] Add to cart từ ProductListFragment (nếu có inline add button): stock check?

### 3.5 ProductAdapter (Admin) — đọc và review
- [ ] `getAdapterPosition()` ở onClick/onLongClick → check `!= RecyclerView.NO_POSITION`
- [ ] Item null check trong `onBindViewHolder`
- [ ] Image load null URL → Glide placeholder/error handler đã set?

### 3.6 AdminProductApi (Retrofit interface) — đọc và kiểm tra
- [ ] `getAllProducts(page, limit, search, category)` — params đúng không với backend?
- [ ] `uploadImage(@Part MultipartBody.Part file)` — header Content-Type đã đúng?
- [ ] `deleteProduct(id)` — trả về `Call<Void>` hay `Call<ApiResponse<Void>>`? Callback xử lý đúng không?
- [ ] `updateProduct(id, body)` và `createProduct(body)` — request body type là `Map<String,Object>` hay DTO riêng?

### 3.7 shared Product.java
- [ ] Tất cả getter có thể trả null: `getId()`, `getName()`, `getCategory()`, `getUnit()`, `getImageUrl()`, `getRating()`, `getReviewCount()`, `getSoldCount()`, `getOriginalPrice()`
- [ ] `isActive()` boolean default: false nếu field null → có thể ẩn sản phẩm hợp lệ?
- [ ] `getStock()` default: 0 nếu null → sản phẩm sẽ hiện "Hết hàng" dù chưa có data

## Quy tắc khi fix

1. KHÔNG thay đổi business logic — chỉ fix bug, null safety, UX improvements
2. KHÔNG thêm dependency mới
3. Giữ nguyên code style hiện tại
4. Việt hóa Toast message theo danh sách đã cung cấp ở trên
5. Giữ nguyên tất cả comment/docstring cũ

## Sau khi fix xong

1. Build: `./gradlew assembleDebug` (Windows: `gradlew.bat assembleDebug`)
2. Đảm bảo cả 2 module build OK: app-admin, app-customer
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
git commit -m "fix(codex): wave 3 - product CRUD null safety, role guard, toast i18n, filter conflict"
```
```

---

> ⚠️ **Lưu ý cho user sau khi Codex xong:**
> 1. Xem danh sách thay đổi Codex báo cáo — đặc biệt kiểm tra:
>    - Fix thứ tự null check trong `ProductEditActivity.onCreate()` (critical!)
>    - Fix chip + spinner conflict trong `AdminProductListFragment`
>    - Fix `getId() == null` check trong `addToCart()`
> 2. Build lại: `./gradlew assembleDebug`
> 3. Nếu OK → push GitHub → chuyển sang **Stream 2 Wave 3** (test CRUD sản phẩm trên emulator)
