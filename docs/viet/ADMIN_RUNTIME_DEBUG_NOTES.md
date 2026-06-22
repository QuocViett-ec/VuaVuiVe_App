# Admin Runtime Debug Notes

## Crash screen

- Target: Products/admin navigation stabilization.
- Runtime crash was not reproduced from terminal launch.

## Reproduction steps tried

1. `adb logcat -c`
2. `adb shell monkey -p vn.vuavuive.admin 1`
3. Captured `admin_crash_logcat.txt`
4. Checked app process with `adb shell pidof vn.vuavuive.admin`

## Logcat root cause

- Device detected: yes, `emulator-5554`.
- `adb` was not on PATH, used `C:\Users\ADMIN\AppData\Local\Android\Sdk\platform-tools\adb.exe`.
- No relevant `FATAL EXCEPTION` for `vn.vuavuive.admin` was found after basic launch.
- Backend/API inspection found confirmed Product API mismatches:
  - `POST /api/products` returns raw `ProductResponse`, not `ApiResponse<Product>`.
  - `PUT /api/products/{id}` returns raw `ProductResponse`, not `ApiResponse<Product>`.
  - `DELETE /api/products/{id}` returns `204 No Content`, not `ApiResponse<Void>`.
- Later save attempt from ProductEditActivity returned `404 "Danh mục không tồn tại"`; confirmed cause was hardcoded Admin category UUIDs not matching the current backend database.
- Follow-up category spinner issue showed `Expected BEGIN_ARRAY but was BEGIN_OBJECT`; fixed Retrofit category response type to `ApiResponse<List<CategoryResponse>>`.

## Files changed

- `shared/src/main/java/vn/vuavuive/shared/data/api/AdminProductApi.java`
- `app-admin/src/main/java/vn/vuavuive/admin/ui/products/AdminProductListFragment.java`
- `app-admin/src/main/java/vn/vuavuive/admin/ui/products/ProductEditActivity.java`
- `app-admin/src/main/java/vn/vuavuive/admin/ui/products/ProductAdapter.java`

## Fix applied

- Matched Product create/update Retrofit calls to raw `Call<Product>`.
- Matched Product delete Retrofit call to `Call<Void>` for backend `204`.
- Guarded Products list callbacks when fragment view is already destroyed.
- Stopped Products refresh spinner on safe success/failure paths.
- Guarded product click/delete when product or product ID is invalid.
- Guarded product filtering when binding, adapter, or data is not ready.
- Made Product adapter tolerate null lists/items and fallback product names.
- Guarded Product editor callbacks when Activity is finishing/destroyed.
- Blocked Product editor save when editing an invalid product ID.
- Loaded Product editor categories from backend `GET /api/categories` and save with the selected category's real ID.
- Parsed runtime category response from the `data` field.

## Build result

- `.\gradlew.bat :app-admin:assembleDebug`: PASS
- `.\gradlew.bat :app-customer:assembleDebug`: PASS
- Installed `app-admin-debug.apk` on `emulator-5554`: PASS
- Launch smoke Logcat after install: no `FATAL EXCEPTION`

## Remaining suspected issues

- Products list/add/edit/delete still need manual emulator test with backend running.
- Current Logcat only confirms no startup/login crash after basic launch, not full Products navigation.
- Other admin pages were only quick-audited for obvious Hilt injection risk; no broad rewrite was done.
