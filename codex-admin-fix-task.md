# Codex Admin Fix Task Digest

Purpose: Give ChatGPT enough context to write a clean prompt for Codex to continue fixing the VuaVuiVe admin app.

Rule for next Codex run: use Ponytail mode. Smallest diff first. Do not add new abstractions unless an existing API/DTO mismatch forces it. Always run `.\gradlew.bat :app-admin:assembleDebug` after Java changes.

## Project Map

Root: `E:\Nam3\TaiLieuHocKi8\APP\VuaVuiVe`

- `app-admin`: Android admin app, Java, ViewBinding, Hilt, Retrofit, Glide.
- `shared`: shared DTOs, Retrofit API interfaces, utilities.
- `app-backend`: Spring Boot backend.
- `app-customer`: customer Android app.

Admin network setup:

- `app-admin/src/main/java/vn/vuavuive/admin/di/NetworkModule.java`
- Provides `Retrofit`, `OkHttpClient`, `SessionManager`, cookie jar, APIs.
- Base URL is `BuildConfig.BASE_URL + "/"`.
- Adds `PortalScopeInterceptor(BuildConfig.PORTAL_SCOPE)` and `CsrfInterceptor`.

## Current Working Changes Already Made

Products page had two issues.

1. `shared/src/main/java/vn/vuavuive/shared/data/api/AdminProductApi.java`
   - Changed product list endpoint from `api/admin/products` to `api/products`.
   - Reason: backend `ProductController` exposes `GET /api/products`; there is no `/api/admin/products` route found.

2. `app-admin/src/main/java/vn/vuavuive/admin/ui/products/AdminProductListFragment.java`
   - Fixed crash caused by `Spinner` calling `applyFilters()` before `ProductAdapter` existed.
   - `setupRecyclerView()` now runs before `setupSpinner()`.
   - `applyFilters()` returns early if `adapter == null`.
   - `onResume()` only calls `loadProducts()` when `binding != null && adapter != null`.

Check passed:

```powershell
.\gradlew.bat :app-admin:assembleDebug
```

## Product Page Files

### `ui/products/AdminProductListFragment.java`

Role: product list screen.

Key fields:

- `@Inject AdminProductApi adminProductApi`
- `@Inject SessionManager sessionManager`
- `FragmentAdminProductListBinding binding`
- `ProductAdapter adapter`
- `List<Product> allProducts`
- filter state: `currentCategoryFilter`, `currentLowStockFilter`, `currentSearchQuery`
- `User currentUser`

Functions:

- `onCreateView(...)`: inflate `FragmentAdminProductListBinding`.
- `onViewCreated(...)`: load user from `SessionManager`, setup recycler, spinner, filters/FAB, then load products.
- `setupSpinner()`: category spinner; item selection updates `currentCategoryFilter` and calls `applyFilters()`.
- `setupRecyclerView()`: creates `ProductAdapter`, sets `SwipeRefreshLayout` refresh to `loadProducts()`.
- `setupFiltersAndFab()`: search watcher, low stock chip, add product FAB, CSV export.
- `loadProducts()`: calls `adminProductApi.getAllProducts(1, 100, null, null)`, updates `allProducts`, calls `applyFilters()`.
- `applyFilters()`: local in-memory filter by category, low stock, search text.
- `onProductClick(Product)`: opens `ProductEditActivity` with `PRODUCT_ID`.
- `onProductLongClick(Product)`: audit role cannot delete; otherwise confirm and call `adminProductApi.deleteProduct(id)`.
- `onResume()`: reloads products if view and adapter are ready.
- `isAudit()`: true when current role is `audit`.
- `exportProductsCsv()`: writes current `allProducts` to Downloads via `MediaStore`.
- `safeCsv(String)`: simple comma replacement.
- `onDestroyView()`: clears binding.

Known risks:

- `createProduct/updateProduct/deleteProduct` API response types may not match backend because backend create/update currently return raw `ProductResponse`, not `ApiResponse<Product>`.
- `deleteProduct` endpoint is `DELETE /api/products/{id}` and backend returns `204 No Content`, while Android expects `ApiResponse<Void>`. Retrofit may treat this as successful with null body; current delete code only checks callback success by showing toast inside `onResponse`, so it may be okay.

### `ui/products/ProductEditActivity.java`

Role: add/edit product form.

Key fields:

- `@Inject AdminProductApi adminProductApi`
- `@Inject ProductApi productApi`
- `@Inject SessionManager sessionManager`
- `ActivityProductEditBinding binding`
- `String productId`
- `Product existingProduct`
- `String selectedImageUrl`

Functions:

- `onCreate(...)`: setup binding/user, read `PRODUCT_ID`, setup spinner/listeners, load existing product or set defaults, enforce role permissions.
- `setupSpinner()`: category display names and keys.
- `setupListeners()`: back, choose sample image, save.
- `loadExistingProduct()`: calls `productApi.getProduct(productId)` and then `bindProduct()`.
- `bindProduct(Product)`: fills form fields and image preview.
- `loadImagePreview(String)`: Glide image load.
- `enforceRolePermissions()`: audit role disables inputs and save.
- `saveProduct()`: validates name, price, original price, stock, unit; builds `Map<String,Object>` with backend fields; calls create or update.
- `categoryIdFor(String)`: maps category slug to seeded UUID.

Important backend field mapping in `saveProduct()`:

- `name`
- `description`
- `originalPrice`
- `sellingPrice`
- `stockQuantity`
- `unit`
- `imageUrl`
- `categoryId`

### `ui/products/ProductAdapter.java`

Role: RecyclerView adapter for product cards.

Functions:

- `ProductAdapter(List<Product>, OnProductClickListener)`
- `updateData(List<Product>)`
- `onCreateViewHolder(...)`
- `onBindViewHolder(...)`
- `getItemCount()`
- `ProductViewHolder.bind(Product)`: binds name, category/subCategory, price, original price, stock badge, sold count, active status, image, click and long-click.

Risk:

- If backend sends missing/null fields, adapter mostly handles it. `CurrencyFormatter.formatVnd(product.getPrice())` is safe because price is primitive `double`.

## Shared Product APIs And DTOs

### `shared/data/api/AdminProductApi.java`

Current shape:

```java
@GET("api/products")
Call<ApiResponse<List<Product>>> getAllProducts(int page, int limit, String search, String category);

@POST("api/products")
Call<ApiResponse<Product>> createProduct(@Body Map<String, Object> body);

@PUT("api/products/{id}")
Call<ApiResponse<Product>> updateProduct(@Path("id") String id, @Body Map<String, Object> body);

@DELETE("api/products/{id}")
Call<ApiResponse<Void>> deleteProduct(@Path("id") String id);
```

Possible next minimal fix:

- If save crashes or always fails parsing, change create/update return type to `Call<Product>` because backend `ProductController.createProduct()` and `updateProduct()` return `ProductResponse` directly.
- If delete parsing fails, change delete return type to `Call<Void>`.

### `shared/data/api/ProductApi.java`

Used by product detail load:

- `GET api/products`
- `GET api/products/categories`
- `GET api/products/{id}`
- `GET api/products/{id}/reviews`

### `shared/data/dto/Product.java`

Fields used by admin:

- `_id` -> `id`
- `name`, `slug`, `price`, `originalPrice`
- `category`, `subCategory`, `description`, `imageUrl`
- `stock`, `unit`, `tags`, `isActive`
- `externalId`, `rating`, `reviewCount`, `soldCount`
- `createdAt`, `updatedAt`

Helpers:

- `isOnSale()`
- `getDiscountPercent()`
- `isInStock()`

Backend `ProductResponse` provides legacy JSON:

- `_id`
- `price`
- `stock`
- `category` is category slug
- `isActive`

This should parse into Android `Product`.

## Other Admin Screens

### `ui/auth/AdminLoginActivity.java`

Role: admin login.

Functions:

- `onCreate(...)`: bind view, setup role spinner, login button.
- `setupRoleSpinner()`: role selection.
- `performLogin()`: validates form, calls `AuthApi`, saves user/tokens in `SessionManager`, starts `MainActivity`.

Need verify:

- Has `@AndroidEntryPoint`.
- Uses backend login, not `MockRepository`.
- Saves cookie/session/tokens consistently.

### `ui/main/MainActivity.java`

Role: admin shell/navigation.

Functions:

- `onCreate(...)`: checks session user and sets UI.
- `setupUI()`: bottom navigation/drawer item routing.
- `replaceFragment(Fragment)`: swaps content fragment.
- `navigateToMenu(int)`: programmatic navigation.
- `logout()`: clears session and returns to login.

Need verify:

- Has `@AndroidEntryPoint`.
- Parent activity must be Hilt-enabled because child fragments use injection.

### `ui/dashboard/DashboardFragment.java`

Role: dashboard stats, pending orders, low stock, shortcuts, export.

Functions:

- `onCreateView(...)`
- `onViewCreated(...)`
- `setupRecyclerViews()`
- `loadDashboardData()`
- `setupListeners()`
- `showExportDialog()`
- `exportCsv(String, String)`
- `onDestroyView()`

Likely still uses `MockRepository`. Minimal next step: either inject `DashboardApi` if backend endpoint matches, or compute dashboard from existing admin order/product APIs. Do not build a large dashboard repository unless necessary.

### `ui/orders/AdminOrderListFragment.java`

Role: order list, filters, bulk update, CSV export.

Functions:

- `onCreateView(...)`
- `onViewCreated(...)`
- `setupTabs()`
- `setupRecyclerView()`
- `setupSearchAndActions()`
- `loadOrders()`
- `applyFilters()`
- `onOrderClick(Order)`
- `onOrderSelectionChanged(int)`
- `onResume()`
- `showBulkUpdateDialog()`
- `exportFilteredOrdersCsv()`
- `onDestroyView()`

API:

- `AdminOrderApi.getOrders(status, page, limit, from, to)`
- `AdminOrderApi.bulkUpdateStatus(body)`

Risk:

- Backend and Android wrapper must match. `AdminOrderApi` expects `ApiResponse<List<Order>>`.

### `ui/orders/AdminOrderDetailActivity.java`

Role: view order detail, update status, mark paid/refund, return review.

Functions:

- `onCreate(...)`
- `loadOrderDetails(String)`
- `renderOrderDetails(String)`
- `setupStatusSpinner()`
- `setupReturnRequest()`
- `updateReturnStatus(String)`

API:

- `updateOrderStatus(id, body)`
- `reviewReturnRequest(id, body)`
- `markPaid(id)`
- `markRefunded(id)`

Risk:

- Status casing may differ: backend may return uppercase enum, UI often compares lowercase. Normalize at comparison boundaries.

### `ui/orders/OrderAdapter.java`

Role: order RecyclerView adapter and multi-select.

Functions:

- `updateData(List<Order>)`
- `setMultiSelectMode(boolean)`
- `isMultiSelectMode()`
- `getSelectedOrderIds()`
- `toggleSelection(String)`
- `selectAll(boolean)`
- `onCreateViewHolder(...)`
- `onBindViewHolder(...)`
- `getItemCount()`
- `OrderViewHolder.bind(Order)`
- `setupStatusBadge(String)`

### `ui/vouchers/VoucherListFragment.java`

Role: voucher list.

Functions:

- `onCreateView(...)`
- `onViewCreated(...)`
- `setupRecyclerView()`
- `setupFab()`
- `loadVouchers()`
- `onVoucherClick(Voucher)`
- `onResume()`
- `onDestroyView()`

Likely still mock-heavy. API exists in `AdminVoucherApi`.

### `ui/vouchers/VoucherEditActivity.java`

Role: create/edit voucher form.

Functions:

- `onCreate(...)`
- `setupSpinner()`
- `setupDatePickers()`
- `loadExistingVoucher()`
- `setupListeners()`
- `enforceRolePermissions()`
- `saveVoucher()`

API exists:

- `GET api/admin/vouchers`
- `POST api/admin/vouchers`
- `PUT api/admin/vouchers/{code}`
- `DELETE api/admin/vouchers/{code}`

### `ui/users/UserListFragment.java`

Role: user list, filter/export/status toggle.

Functions:

- `onCreateView(...)`
- `onViewCreated(...)`
- `setupRecyclerView()`
- `setupFiltersAndExport()`
- `loadUsers()`
- `applyFilters()`
- `onUserStatusChanged(User, boolean)`
- `onUserClick(User)`
- `exportUsersCsv()`
- `onDestroyView()`

API exists in `AdminUserApi`. Need verify response mapping.

### `ui/shipments/ShipmentListFragment.java`

Role: shipment list/filter.

Functions:

- `onCreateView(...)`
- `onViewCreated(...)`
- `setupSpinners()`
- `setupRecyclerView()`
- `loadShipments()`
- `applyFilters()`
- `onShipmentClick(Shipment)`
- `onResume()`
- `onDestroyView()`

API exists in `AdminShipmentApi`.

### `ui/shipments/ShipmentDetailActivity.java`

Role: shipment detail and status update.

Functions:

- `onCreate(...)`
- `setupSpinner()`
- `loadShipmentDetails()`
- `renderTimeline()`
- `dpToPx(int)`
- `spToPx(int)`
- `setupListeners()`
- `enforceRolePermissions()`
- `saveShipmentStatus()`

### `ui/audit/AuditLogFragment.java`

Role: audit log list.

Functions:

- `onCreateView(...)`
- `onViewCreated(...)`
- `setupRecyclerView()`
- `loadAuditLogs()`
- `onDestroyView()`

API exists in `AuditLogApi`.

### `ui/audit/AuditLogAdapter.java`

Functions:

- `updateData(List<AuditLog>)`
- `bind(AuditLog)`
- `setupActionBadgeColor(String)`

### `ui/chatbot/AdminChatFragment.java`

Role: admin chatbot UI.

Functions:

- `onCreateView(...)`
- `onViewCreated(...)`
- `setupRecyclerView()`
- `addBotGreeting()`
- `getCurrentTimestamp()`
- `setupInputAndSend()`
- `setupChips()`
- `triggerQuery(String)`
- `sendMessage()`
- `addUserMessage(String)`
- `processAIResponse(String)`
- `onQuickReplyClick(String)`
- `onDestroyView()`

Likely local/mock response logic. API exists in `AdminChatbotApi`.

### `ui/chatbot/ChatAdapter.java`

Role: chat message adapter.

Functions:

- `ChatMessage(...)`
- `addMessage(ChatMessage)`
- `getItemViewType(int)`
- `onCreateViewHolder(...)`
- `onBindViewHolder(...)`
- `getItemCount()`

## Shared Admin API Interfaces

- `AdminProductApi`: product list/create/update/delete/export.
- `AdminOrderApi`: admin orders, bulk status, export, status update, return review, paid/refund.
- `AdminVoucherApi`: voucher CRUD.
- `AdminUserApi`: user admin actions.
- `AdminShipmentApi`: shipment admin actions.
- `AdminChatbotApi`: chatbot.
- `AuditLogApi`: audit logs.
- `DashboardApi`: dashboard stats/analytics, currently `api/users/dashboard/...`; verify if this is admin-safe.

## Recommended Prompt For Next Codex

Use this prompt:

```text
Use Ponytail mode. Work in E:\Nam3\TaiLieuHocKi8\APP\VuaVuiVe.

Goal: continue stabilizing app-admin without big rewrites. Read codex-admin-fix-task.md first. Preserve existing user changes. Fix only the next broken admin flow I name.

Rules:
- Prefer one-file or two-file fixes.
- Match existing backend routes before changing Android APIs.
- If Retrofit response wrapper mismatches backend, fix the return type instead of adding adapter layers.
- After Java changes run: .\gradlew.bat :app-admin:assembleDebug
- Report exact files changed and skipped work.

First target: <paste the broken screen/flow here, plus logcat stacktrace if available>.
```

## Fast Debug Checklist

When a screen crashes:

1. Ask for or inspect Logcat stacktrace if the crash is not obvious.
2. Check Hilt: Activity and Fragment using injection need `@AndroidEntryPoint`, and parent Activity must also have it.
3. Check lifecycle order: spinners/text watchers may call filters before adapter exists.
4. Check Retrofit route exists in backend.
5. Check Retrofit response type matches backend exact JSON.
6. Build admin module.

## Current Product Fix Status

- Product list endpoint fixed.
- Product list early spinner crash fixed.
- `:app-admin:assembleDebug` passes.
- Runtime still needs emulator/manual check against backend.

Skipped: full admin rewrite. Add it only screen by screen when a real crash/API mismatch is confirmed.
