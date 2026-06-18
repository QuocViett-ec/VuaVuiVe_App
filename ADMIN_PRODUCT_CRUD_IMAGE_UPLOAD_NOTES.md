# Product CRUD + Image Upload checkpoint

## Root causes

- save failure: Android create/update expected `ApiResponse<Product>`, but backend returns raw `ProductResponse`; delete expected `ApiResponse<Void>`, but backend returns `204 No Content`.
- gallery picker failure: product editor button picked random sample URLs instead of opening Android Gallery/system picker.
- image upload issue: backend had no upload endpoint for product images.
- customer sync issue: no realtime sync needed; Customer already reloads from `GET /api/products` through `ProductViewModel`/`ProductRepository` when API is available.
- latest Logcat save failure: backend returned `404 "Danh mục không tồn tại"` because Admin ProductEditActivity used hardcoded category UUIDs that do not match the current database.
- follow-up category spinner failure: Android expected a raw JSON array, but runtime backend returned an `ApiResponse` object; Admin now reads categories from `data`.

## Backend upload API

- route: `POST /api/uploads/images`
- request field name: multipart `file`
- response shape: `ApiResponse<{ "url": "http://host/uploads/products/<uuid>.<ext>" }>`
- public URL format: absolute URL built from request host; files served at `GET /uploads/products/<filename>`

## Files changed

- `app-admin/src/main/java/vn/vuavuive/admin/ui/products/ProductEditActivity.java`
- `shared/src/main/java/vn/vuavuive/shared/data/api/AdminProductApi.java`
- `shared/src/main/java/vn/vuavuive/shared/data/dto/CategoryResponse.java`
- `shared/src/main/java/vn/vuavuive/shared/data/dto/UploadResponse.java`
- `app-backend/src/main/java/vn/vuavuive/backend/modules/upload/UploadController.java`
- `app-backend/src/main/java/vn/vuavuive/backend/config/StaticResourceConfig.java`
- `app-backend/src/main/java/vn/vuavuive/backend/config/SecurityConfig.java`

## Build

- app-admin assembleDebug: PASS
- app-customer assembleDebug: PASS
- backend build/check: PASS (`mvn test`)
- app-admin debug APK installed on emulator: PASS
- launch smoke Logcat after install: no `FATAL EXCEPTION`

## Manual test

- Admin add product: not manually completed
- Admin pick Gallery image: not manually completed
- Admin upload image: not manually completed
- Admin save product: not manually completed
- Admin edit product: not manually completed
- Admin delete product: not manually completed
- Customer reload product list: not manually completed
- Customer reload home/search: not manually completed
- Customer image display: not manually completed

## Remaining limitations

- Backend `ProductRequest` does not include `tags` or `isActive`, so the admin form still cannot persist those fields through create/update.
- Runtime Logcat was captured after launcher/basic ProductEditActivity state only; full gallery/save flow needs manual emulator test with backend running.
- Product category spinner now loads real categories from `GET /api/categories`; backend must be running before opening/saving the Admin product form.
- Runtime category response is handled as `ApiResponse<List<CategoryResponse>>`.
