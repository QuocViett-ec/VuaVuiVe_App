# PROJECT_PROGRESS.md — VuaVuiVe Progress Handoff

> Cập nhật bởi ChatGPT: 17/06/2026  
> Quy tắc làm tiếp: dùng Codex CLI + `@ponytail full`, đi từng milestone nhỏ, Codex inspect trước rồi mới approve sửa code.

---

## 0. Cách làm việc hiện tại

- User làm solo, không chia 4 người như kế hoạch gốc trong `tasks.md`.
- Mỗi milestone phải:
  1. cho Codex inspect trước,
  2. giới hạn allowed files,
  3. approve sau khi xem plan,
  4. build bằng `./gradlew :app-customer:assembleDebug` hoặc `.` tương ứng trên Windows,
  5. manual test,
  6. commit + push.
- Không cho Codex sửa lan sang backend/shared/admin nếu milestone không cần.
- Nếu Codex muốn sửa file ngoài scope, dừng lại và hỏi trước.

---

## 1. Milestone status

| Milestone | Trạng thái | Ghi chú |
|---|---:|---|
| M1 Backend startup + public API health | ✅ Done | Backend chạy port 3000, Swagger mở được, public APIs sản phẩm/công thức test OK. |
| M2 Backend Auth + JWT + roles | ✅ Done + pushed | Register/login/refresh/token/role tests OK. Đã fix backend register nhận thêm `name` bằng `@JsonAlias("name")`. |
| M3 Android Customer Auth UI | ✅ Done | Android parse `accessToken`/`refreshToken`, save session token, register validate email. Build pass. |
| M4 Customer MainActivity shell | ✅ Done | Guest/logged-in flow, bottom nav, cart badge ổn. Đã fix literal `@string/action_save_later` trong cart item. |
| M5 Home screen + product grid | ✅ Done | Home grid 2 cột, search debounce, shortcut filters, product click, plus add cart, promo SharedPreferences. Build pass. |
| Bugfix checkpoint before/inside M6 | 🟡 In progress | Đang fix cart quantity/icon, stock=0 display state, search exit UX, ProductDetail stock safety. |
| M6 Product detail screen | 🟡 Partially inspected | ProductDetail flow đã có sẵn nhiều chức năng; cần bugfix nhỏ và test lại. |
| M7 Cart | ⏳ Not started fully | Có bug cần fix trước: quantity=1 bấm minus bị xóa item; icon +/-/delete bị ẩn/khó thấy. |
| M8 Checkout | ⏳ Not started | Chưa làm. |
| M9 Customer orders | ⏳ Not started | Chưa làm. |
| M10 Shipper flow | ⏳ Not started | Backend shipper API đã test, UI shipper chưa làm milestone riêng. |
| M11 Account/profile extras | ⏳ Not started | Chưa làm. |
| M12 Admin login/dashboard | ⏳ Not started | Chưa làm. |
| M13 Remaining admin screens | ⏳ Not started | Chưa làm. |
| M14 Full integration audit | ⏳ Not started | Chưa làm. |

---

## 2. Done details

### M1 — Backend startup + public API health

Confirmed:
- Backend chạy được ở port `3000`.
- Swagger mở được ở `http://localhost:3000/swagger-ui.html`.
- Public endpoints đã test OK:
  - `GET /api/products`
  - `GET /api/products/{id}`
  - `GET /api/products/search`
  - `GET /api/products/categories`
  - `GET /api/recipes`

Known risk:
- Backend startup từng có Hibernate warning/error liên quan alter UUID ở `order_status_logs`, nhưng app vẫn start. Chưa xử lý vì chưa blocker.

### M2 — Backend Auth + JWT + roles

Confirmed:
- CUSTOMER login OK.
- ADMIN login OK.
- SHIPPER login OK.
- CUSTOMER token gọi `GET /api/orders/my` OK.
- ADMIN token gọi `POST /api/products` OK khi dùng categoryId hợp lệ.
- SHIPPER token gọi `GET /api/orders/shipper` OK, trả `data: []` vì chưa có đơn assigned.
- Register với `fullName` OK.
- Register với `name` ban đầu fail, sau đó đã fix backend bằng `@JsonAlias("name")` cho `fullName` trong `RegisterRequest.java`.
- Refresh token OK.
- Protected API không token bị 403 đúng.
- CUSTOMER/SHIPPER token gọi admin product POST bị 403 đúng.

Seeded accounts:
- Admin: `admin@vuavuive.vn` / `Admin@123`
- Customer: `customer@gmail.com` / `Customer@123`
- Shipper: `shipper@gmail.com` / `Shipper@123`

### M3 — Android Customer Auth UI

Files changed:
- `shared/src/main/java/vn/vuavuive/shared/data/dto/ApiResponse.java`
- `shared/src/main/java/vn/vuavuive/shared/util/SessionManager.java`
- `app-customer/src/main/java/vn/vuavuive/customer/data/repository/AuthRepository.java`
- `app-customer/src/main/java/vn/vuavuive/customer/ui/auth/RegisterActivity.java`

Behavior:
- `ApiResponse` parse thêm `accessToken`, `refreshToken`.
- `SessionManager` lưu/get/clear token.
- `AuthRepository` save user + tokens khi login/register.
- `RegisterActivity` yêu cầu email không trống và email hợp lệ.
- Build `:app-customer:assembleDebug` pass.

### M4 — Customer MainActivity shell

Confirmed:
- App mở không crash.
- Guest flow vào được.
- Logged-in customer vào được.
- Bottom nav Home/Cart/Orders/Account hoạt động.
- Cart badge ổn.
- Cart tab không còn hiện literal `@string/action_save_later`.

Files changed:
- `app-customer/src/main/res/layout/item_cart.xml`
- `app-customer/src/main/res/values/strings.xml`

### M5 — Home screen + product grid

File changed:
- `app-customer/src/main/java/vn/vuavuive/customer/ui/home/HomeFragment.java`

Confirmed:
- Home opens without crash.
- Product grid 2 columns.
- Search debounce/filter works.
- Shortcut filters local:
  - Flash Sale: `discountPercent > 0` hoặc `originalPrice > price`
  - Bánh: `bánh`, `banh`, `cake`, `bread`, `sweet`
  - Mì: `mì`, `mi`, `noodle`, `dry`
  - Bia: `bia`, `beer`, `drink`
  - Sữa: `sữa`, `sua`, `milk`, `drink`
- Product click opens `ProductDetailActivity`.
- Plus button add cart + Toast.
- Promo popup uses SharedPreferences and does not repeat unless app data is cleared.
- Build pass.

Known risk:
- Android DTO hiện dùng `price`, chưa expose `sellingPrice`.
- Android DTO không expose `categoryName`, nên filter dùng field hiện có như `category`, `subCategory`, `tags`, `name`.
- Bia/Sữa có keyword `drink` nên có thể filter hơi rộng.

---

## 3. Current bugfix checkpoint

Manual bugs reported:

1. Cart quantity bug
- Khi quantity = 1, bấm minus thì item bị mất khỏi cart.
- Expected: minus ở 1 không xóa item; item chỉ xóa bằng explicit delete/swipe delete.

2. Cart icons hidden/confusing
- Plus/minus/delete trong cart item không hiện hoặc bị ẩn/khó thấy.
- Inspect phát hiện `item_cart.xml` dùng `ic_delete` cho minus nên dễ nhìn nhầm.
- Expected: visible plus/minus/delete. Có thể dùng text fallback `+`, `−`, `X`.

3. Stock = 0 visibility
- User muốn product stock = 0 vẫn hiển thị, nhưng có trạng thái `Hết hàng`/out-of-stock.
- Add-to-cart phải disabled khi stock = 0.
- ProductDetail cũng phải disable add-to-cart khi stock = 0.

4. Back arrow
- Một số mũi tên quay lại không hoạt động.
- Codex inspect chưa thấy broken cụ thể trong allowed scope; không sửa navigation chung chung.

5. Search bar stuck focus
- Search bar click vào có lúc không thoát/focus được.
- Cần Android back clear text/focus/hide keyboard/reset list nếu đang search.

Codex inspect proposed files:
- `app-customer/src/main/java/vn/vuavuive/customer/ui/cart/CartAdapter.java`
- `app-customer/src/main/res/layout/item_cart.xml`
- `app-customer/src/main/java/vn/vuavuive/customer/ui/product/ProductDetailActivity.java`
- `app-customer/src/main/java/vn/vuavuive/customer/ui/home/HomeFragment.java`
- `app-customer/src/main/java/vn/vuavuive/customer/ui/product/ProductListFragment.java`
- `app-customer/src/main/java/vn/vuavuive/customer/ui/search/SearchActivity.java`

Do not modify:
- `app-backend/**`
- `app-admin/**`
- unrelated `shared/**`
- repositories/viewmodels unless absolutely needed
- Checkout/orders/auth/payment flow
- database seed data
- large UI redesigns

---

## 4. Approved bugfix prompt for Codex

Use this in Codex:

```text
@ponytail full

Approved for this bugfix checkpoint, but keep the changes minimal and controlled.

Allowed files:
- app-customer/src/main/java/vn/vuavuive/customer/ui/cart/CartAdapter.java
- app-customer/src/main/res/layout/item_cart.xml
- app-customer/src/main/java/vn/vuavuive/customer/ui/product/ProductDetailActivity.java
- app-customer/src/main/java/vn/vuavuive/customer/ui/home/HomeFragment.java
- app-customer/src/main/java/vn/vuavuive/customer/ui/product/ProductListFragment.java
- app-customer/src/main/java/vn/vuavuive/customer/ui/search/SearchActivity.java

Do not modify:
- app-backend
- app-admin
- shared files
- repositories
- viewmodels
- Checkout
- Orders logic
- Auth flow
- payment flow
- database seed data
- large UI redesigns

Implement only these fixes:

1. Cart minus behavior:
- In CartAdapter, tapping minus when quantity is 1 must not remove the item.
- Quantity must never go below 1.
- At quantity 1, either do nothing or show a small Toast.
- Item removal must only happen through explicit delete/remove action or existing swipe-delete behavior.
- Do not change CartRepository unless absolutely required.

2. Cart icons:
- In item_cart.xml, make plus, minus, and delete/remove controls visible and tappable.
- Use simple text fallback if needed:
  - minus: "−" or "-"
  - plus: "+"
  - delete: existing ic_close or visible delete label/icon
- Do not redesign the cart item layout.
- Keep the save-later behavior unchanged.

3. ProductDetail stock safety:
- Keep stock=0 product detail visible.
- If stock <= 0, show out-of-stock state and disable add-to-cart.
- Clamp selected quantity after product bind if stock is lower than current quantity.
- Add defensive stock check before addToCart().
- Do not change product loading architecture.

4. Back arrows:
- Do not make broad navigation changes.
- Only fix a back arrow if you find a specific broken click handler in one of the allowed files.
- If no specific broken back arrow is found, leave navigation unchanged and report that no safe fix was applied.

5. Search exit behavior:
- Add minimal Android-back handling for active search mode in HomeFragment, ProductListFragment, and SearchActivity.
- If search text is non-empty or search has focus:
  - clear text or exit search mode,
  - clear focus,
  - hide keyboard,
  - reset list to normal if applicable.
- If search is not active, Android back should keep the existing behavior.
- Do not create a new search architecture.

After editing:
- run .\gradlew.bat :app-customer:assembleDebug
- summarize files changed
- summarize behavior changed
- explain root cause for each bug fixed
- provide manual test checklist:
  - Cart item quantity 1, tap minus, item remains
  - Cart plus/minus/delete controls are visible and tappable
  - Delete removes item only through explicit delete/remove
  - Stock=0 product detail shows out-of-stock and cannot be added
  - Home search can be exited with Android back
  - Product list search can be exited with Android back
  - SearchActivity exits/clears search correctly
  - Build passes
```

---

## 5. Commit/push notes

Before moving to next milestone:

```powershell
git status
git add app-customer/src/main/java/vn/vuavuive/customer/ui/cart/CartAdapter.java
git add app-customer/src/main/res/layout/item_cart.xml
git add app-customer/src/main/java/vn/vuavuive/customer/ui/product/ProductDetailActivity.java
git add app-customer/src/main/java/vn/vuavuive/customer/ui/home/HomeFragment.java
git add app-customer/src/main/java/vn/vuavuive/customer/ui/product/ProductListFragment.java
git add app-customer/src/main/java/vn/vuavuive/customer/ui/search/SearchActivity.java
git commit -m "Fix customer cart controls and search exit behavior"
git push
```

Only add files that actually changed.

---

## 6. Next milestone after bugfix checkpoint

After bugfix build + manual test + commit/push:

### Continue M6 ProductDetailActivity

Need verify:
- Product detail opens from Home.
- Correct product shown, no wrong mock fallback.
- Price/original price/discount OK.
- Rating/reviews/similar products do not crash.
- Quantity controls cannot go below 1 or exceed stock.
- Stock=0 disables add-to-cart and shows out-of-stock.
- Add to cart works and Toast appears.
- Back arrow works for ProductDetail.

Then move to:
- M7 CartFragment full flow
- M8 Checkout
- M9 Orders
