# Codex Admin Stabilization Prompt

Use Ponytail mode.

Work in:

```text
E:\Nam3\TaiLieuHocKi8\APP\VuaVuiVe
```

You are a senior Android Java developer. Your job is to stabilize the current `app-admin` module because it has many bugs and can easily crash when navigating between admin pages, especially Products.

Before editing anything, read this context file first:

```text
codex-admin-fix-task.md
```

## Main Goal

Fix crashes and unstable behavior in the current admin app with the smallest safe diffs.

Do **not** rewrite the admin app.  
Do **not** introduce big architecture changes.  
Fix screen by screen, flow by flow.

## Project Context

- `app-admin`: Android admin app, Java, XML/ViewBinding, Hilt, Retrofit, Glide.
- `shared`: shared DTOs, Retrofit API interfaces, utilities.
- `app-backend`: Spring Boot backend.
- `app-customer`: customer Android app.

Admin network setup:

```text
app-admin/src/main/java/vn/vuavuive/admin/di/NetworkModule.java
```

Current network assumptions:

- Base URL is `BuildConfig.BASE_URL + "/"`
- Uses `PortalScopeInterceptor(BuildConfig.PORTAL_SCOPE)`
- Uses `CsrfInterceptor`
- Uses `SessionManager`
- Uses Retrofit APIs from `shared`

## Important Current Products Status

The Products page already had some fixes:

1. Product list endpoint was already changed from:

```text
api/admin/products
```

to:

```text
api/products
```

Reason: backend exposes `GET /api/products`; no `/api/admin/products` route was found.

2. Products list crash from `Spinner` calling `applyFilters()` before `ProductAdapter` existed was already fixed.

Expected current behavior:

- `setupRecyclerView()` should run before `setupSpinner()`.
- `applyFilters()` should return early if `adapter == null`.
- `onResume()` should only call `loadProducts()` when view and adapter are ready.

Previous check passed:

```powershell
.\gradlew.bat :app-admin:assembleDebug
```

Your task now is to continue stabilizing admin runtime crashes, especially when navigating through Products and other admin pages.

---

# Global Rules

1. Preserve all existing user changes.
2. Prefer one-file or two-file fixes.
3. Do not add new repositories, new abstraction layers, or large refactors unless an existing API/DTO mismatch absolutely forces it.
4. Match Android Retrofit APIs with actual backend routes before changing UI code.
5. If backend returns raw DTO but Android expects `ApiResponse<T>`, fix the Retrofit return type instead of adding adapter/parsing hacks.
6. If backend returns `204 No Content`, use `Call<Void>` instead of `Call<ApiResponse<Void>>` if parsing causes runtime or response issues.
7. Every Activity or Fragment using `@Inject` must have `@AndroidEntryPoint`.
8. Parent Activity must also be Hilt-enabled if child fragments use injection.
9. Check lifecycle safety:
   - Do not call adapters before they are initialized.
   - Do not call binding after `onDestroyView()`.
   - Guard Retrofit callbacks with `binding != null` or Activity not destroyed checks.
   - Watch out for Spinner/TextWatcher firing during setup.
10. Never let empty or null backend fields crash RecyclerView adapters.
11. Normalize status strings at comparison boundaries, especially order/shipment status values.
12. After Java/XML changes, always run:

```powershell
.\gradlew.bat :app-admin:assembleDebug
```

13. At the end, report:
   - Exact files changed.
   - Exact bugs fixed.
   - Build result.
   - Any skipped risky work.
   - Any remaining runtime checks needed on emulator.

---

# Runtime Debugging Permission

You are allowed to inspect Android Logcat through terminal commands when the emulator/device is running.

Use Logcat to identify the real crash root cause before making broad changes.

Start with:

```powershell
adb devices
```

Then clear old logs:

```powershell
adb logcat -c
```

Then launch/reproduce the admin crash if possible. I will also manually click through the app if needed.

Main reproduction flow:

1. Open admin app.
2. Log in if required.
3. Navigate Products.
4. Open product detail/edit if possible.
5. Go back.
6. Return to Products.
7. Navigate Products -> Orders -> Dashboard -> Products quickly.
8. Try search/filter/refresh on Products.

After reproduction, capture logs:

```powershell
adb logcat -d > admin_crash_logcat.txt
```

Also create a filtered version if useful:

```powershell
adb logcat -d | findstr /i "FATAL EXCEPTION AndroidRuntime vuavuive admin retrofit hilt crash exception" > admin_crash_filtered.txt
```

## Logcat Rules

1. Do not guess if Logcat gives a clear stacktrace.
2. Prioritize the first `FATAL EXCEPTION` related to package `vn.vuavuive.admin`.
3. Identify the exact file, method, and line number from the stacktrace.
4. Fix the smallest root cause first.
5. If crash is from Retrofit parsing, compare Android API return type with backend response.
6. If crash is from lifecycle/binding, guard callbacks with `binding != null`, `isAdded()`, or Activity not destroyed checks where appropriate.
7. If crash is from Hilt, verify `@AndroidEntryPoint` on Activity/Fragment and parent Activity.
8. If crash is from RecyclerView/Adapter, check null fields and initialization order.
9. If crash is from route mismatch, compare with backend controller routes.
10. Do not start broad refactors before fixing the Logcat-confirmed crash.

Create or update this debug note file:

```text
ADMIN_RUNTIME_DEBUG_NOTES.md
```

Include:

- crash screen
- reproduction steps
- Logcat root cause
- files changed
- fix applied
- build result
- remaining suspected issues

---

# First Target

Stabilize admin navigation and Products flow using real Logcat evidence.

Inspect these files first:

```text
app-admin/src/main/java/vn/vuavuive/admin/ui/products/AdminProductListFragment.java
app-admin/src/main/java/vn/vuavuive/admin/ui/products/ProductEditActivity.java
app-admin/src/main/java/vn/vuavuive/admin/ui/products/ProductAdapter.java
shared/src/main/java/vn/vuavuive/shared/data/api/AdminProductApi.java
shared/src/main/java/vn/vuavuive/shared/data/api/ProductApi.java
shared/src/main/java/vn/vuavuive/shared/data/dto/Product.java
app-admin/src/main/java/vn/vuavuive/admin/ui/main/MainActivity.java
app-admin/src/main/java/vn/vuavuive/admin/di/NetworkModule.java
```

Also inspect backend product controller files related to `/api/products`.

---

# A. Products List Screen Checklist

File:

```text
app-admin/src/main/java/vn/vuavuive/admin/ui/products/AdminProductListFragment.java
```

Check and fix:

1. Verify `GET /api/products` route exists in backend.
2. Keep product list endpoint as `api/products` unless backend proves otherwise.
3. Make sure `AdminProductListFragment` cannot crash when:
   - entering Products page
   - leaving Products page quickly
   - returning to Products page
   - pull-to-refresh
   - category spinner auto-selects during setup
   - search text changes before data loads
   - low stock chip changes before adapter/data is ready
4. Ensure `setupRecyclerView()` runs before spinner/filter listeners.
5. Ensure `applyFilters()` safely returns if:
   - `binding == null`
   - `adapter == null`
   - `allProducts == null`
6. Ensure `loadProducts()` handles:
   - response body null
   - response data null
   - HTTP error
   - network failure
   - fragment view already destroyed before callback returns
7. Ensure `SwipeRefreshLayout` loading state always stops in both success and failure.
8. Ensure loading/error/empty UI does not crash if binding is already cleared.
9. Ensure clicking product with null or empty ID does not crash. Show a Toast instead.
10. Ensure long-click delete:
   - audit role cannot delete
   - null or empty ID cannot delete
   - `204 No Content` delete success is handled correctly
   - list reloads after successful delete
   - failed delete shows readable Toast

---

# B. Product Adapter Checklist

File:

```text
app-admin/src/main/java/vn/vuavuive/admin/ui/products/ProductAdapter.java
```

Check and fix:

1. Adapter must not crash on null product object.
2. Adapter must not crash on null product fields:
   - name
   - category
   - subCategory
   - description
   - imageUrl
   - unit
   - active status
3. Image loading must handle null/empty image URL.
4. Price/stock formatting must not crash.
5. Click and long-click listeners must not pass invalid products without guard.
6. Avoid changing layout unless required to prevent crash.

---

# C. Product Add/Edit Screen Checklist

File:

```text
app-admin/src/main/java/vn/vuavuive/admin/ui/products/ProductEditActivity.java
```

Also inspect:

```text
shared/src/main/java/vn/vuavuive/shared/data/api/AdminProductApi.java
shared/src/main/java/vn/vuavuive/shared/data/api/ProductApi.java
```

Also inspect backend product create/update routes.

Check and fix:

1. Verify backend create/update product return shape.
2. If backend returns raw `ProductResponse`, change Retrofit create/update from:

```java
Call<ApiResponse<Product>>
```

to:

```java
Call<Product>
```

only if required by backend.

3. If backend returns wrapped `ApiResponse<Product>`, keep current type.
4. If backend delete returns `204 No Content`, consider changing delete return type to:

```java
Call<Void>
```

only if current `Call<ApiResponse<Void>>` causes issues.

5. Make `saveProduct()` robust:
   - validate name
   - validate price
   - validate original price
   - validate stock
   - validate unit
   - prevent double-click save while request is in progress
   - re-enable save button after success/failure
   - show clear error messages
6. Make `loadExistingProduct()` robust:
   - null ID
   - HTTP error
   - null body
   - failed request
   - activity destroyed before callback returns
7. Ensure audit role disables all editable fields and cannot save.
8. Ensure category mapping still sends backend field:
   - `categoryId`
9. Do not change seeded UUID category mapping unless backend proves it is wrong.
10. Do not rewrite the product editor UI.

---

# D. Admin Main Navigation Stability

File:

```text
app-admin/src/main/java/vn/vuavuive/admin/ui/main/MainActivity.java
```

Check and fix:

1. Ensure `MainActivity` has `@AndroidEntryPoint`.
2. Ensure every injected Fragment has `@AndroidEntryPoint`.
3. Ensure rapid tab switching does not crash due to stale binding callbacks.
4. Ensure navigation does not stack duplicate fragments unnecessarily.
5. Ensure logout clears session and returns to login safely.
6. Do not rewrite the navigation system unless the current implementation is clearly broken.

---

# E. Other Admin Pages Crash-First Audit

After Products and navigation, do a quick compile-time and lifecycle audit of these screens:

- `DashboardFragment`
- `AdminOrderListFragment`
- `AdminOrderDetailActivity`
- `VoucherListFragment`
- `VoucherEditActivity`
- `UserListFragment`
- `ShipmentListFragment`
- `ShipmentDetailActivity`
- `AuditLogFragment`
- `AdminChatFragment`

For each screen, look only for obvious crash causes:

1. Missing `@AndroidEntryPoint` when using injection.
2. Adapter used before initialization.
3. Binding used after `onDestroyView()`.
4. Retrofit callback touching destroyed Fragment/Activity.
5. Null response body/data.
6. Status string casing mismatch causing wrong UI or crash.
7. Spinner/TextWatcher firing before state is ready.
8. MockRepository usage that crashes or blocks real admin flow.

Fix only obvious issues. Do not fully rewrite every page.

---

# F. Backend/API Matching Checklist

For every admin API touched, compare Android interface with backend route and return shape:

- `AdminProductApi`
- `AdminOrderApi`
- `AdminVoucherApi`
- `AdminUserApi`
- `AdminShipmentApi`
- `AuditLogApi`
- `DashboardApi`
- `AdminChatbotApi`

Check:

1. Route path.
2. HTTP method.
3. Request body field names.
4. Response wrapper:
   - raw DTO
   - `ApiResponse<T>`
   - paged response
   - `204 No Content`
5. Android DTO field names.

Only change API signatures when there is a confirmed mismatch.

---

# G. Build Commands

After any Java/XML change, run:

```powershell
.\gradlew.bat :app-admin:assembleDebug
```

If build fails:

1. Fix the smallest compile error first.
2. Do not start new features.
3. Re-run build.

Optional runtime commands if emulator is available:

```powershell
adb devices
adb logcat -c
```

Then reproduce the crash and capture:

```powershell
adb logcat -d > admin_crash_logcat.txt
```

Filtered capture:

```powershell
adb logcat -d | findstr /i "FATAL EXCEPTION AndroidRuntime vuavuive admin retrofit hilt crash exception" > admin_crash_filtered.txt
```

---

# H. Final Report Format

Use this exact final format:

```text
Admin stabilization checkpoint completed.

Build:
- .\gradlew.bat :app-admin:assembleDebug: PASS or FAIL

Runtime Logcat:
- Device detected: YES or NO
- Logcat captured: YES or NO
- First relevant crash: describe root cause or say none found

Files changed:
- file 1
- file 2

Bugs fixed:
- bug 1
- bug 2

Important behavior changes:
- change 1
- change 2

Skipped / not changed:
- item 1 with reason
- item 2 with reason

Need manual emulator test:
- Products list open/leave/reopen
- Products search/filter/refresh
- Add product
- Edit product
- Delete product
- Navigate Products -> Orders -> Dashboard -> Products quickly
- Test with backend running
```

Important:
Do not claim runtime is fully fixed unless Logcat/emulator test confirms it.
If only build passes, say build passes but runtime still needs emulator verification.
