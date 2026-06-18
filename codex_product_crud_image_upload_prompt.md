# Codex Prompt: Admin Products CRUD + Gallery Image Upload + Customer Sync

Use Ponytail mode.

Work in:

```text
E:\Nam3\TaiLieuHocKi8\APP\VuaVuiVe
```

Read these context files first if they exist:

```text
codex-admin-fix-task.md
codex_admin_stabilization_prompt.md
ADMIN_RUNTIME_DEBUG_NOTES.md
ADMIN_PRODUCT_CRUD_DEBUG_NOTES.md
```

## Goal

Fix and complete the Admin Products CRUD flow.

Current bugs:

1. In Admin app, the Add Product screen allows entering all fields but saving fails.
2. The "Chọn ảnh từ Gallery" button does not properly let admin select an image.
3. Admin must be able to select an image from Gallery, upload that image through an API, receive an image URL, and save that image URL into product data.
4. When admin adds, edits, or deletes products, the Customer app must show the updated data after reload because both apps should use the same backend source of truth.

Do not rewrite the app.
Do not create a new architecture.
Use the smallest safe diffs.
Fix this exact flow first.

---

# Must Support Final Flow

## Admin Add Product

1. Admin opens Add Product screen.
2. Admin fills:
   - name
   - selling price
   - original price
   - stock
   - unit
   - category
   - description
   - tags
   - active status
3. Admin taps "Chọn ảnh từ Gallery".
4. Android Gallery/system picker opens.
5. Admin chooses an image.
6. App previews the selected image.
7. App uploads selected image through backend upload API.
8. Backend returns public image URL.
9. App stores returned URL in `imageUrl`.
10. Admin taps "LƯU THÔNG TIN".
11. Product is created successfully.
12. Admin Products list reloads and shows new product.
13. Customer app reloads product list/home/search and sees the new product.

## Admin Edit Product

1. Admin opens existing product.
2. Existing product data loads.
3. Admin may keep old image or select a new image from Gallery.
4. If new image is selected, upload it through upload API and use returned URL.
5. Admin taps save.
6. Product updates successfully.
7. Admin Products list reloads.
8. Customer app reloads and sees updated product data.

## Admin Delete Product

1. Admin long-presses or deletes product from Products list.
2. Product is deleted through backend API.
3. Admin Products list reloads or removes the product locally.
4. Customer app reloads and no longer shows deleted product.

---

# Main Files To Inspect

Admin product flow:

```text
app-admin/src/main/java/vn/vuavuive/admin/ui/products/ProductEditActivity.java
app-admin/src/main/java/vn/vuavuive/admin/ui/products/AdminProductListFragment.java
app-admin/src/main/java/vn/vuavuive/admin/ui/products/ProductAdapter.java
```

Shared product APIs and DTOs:

```text
shared/src/main/java/vn/vuavuive/shared/data/api/AdminProductApi.java
shared/src/main/java/vn/vuavuive/shared/data/api/ProductApi.java
shared/src/main/java/vn/vuavuive/shared/data/dto/Product.java
```

Network setup:

```text
app-admin/src/main/java/vn/vuavuive/admin/di/NetworkModule.java
```

Customer product screens:

```text
app-customer/src/main/java/vn/vuavuive/customer/ui/product/ProductListFragment.java
app-customer/src/main/java/vn/vuavuive/customer/ui/home/HomeFragment.java
app-customer/src/main/java/vn/vuavuive/customer/ui/search/SearchActivity.java
```

Backend product and upload files:

Search backend for:

```text
ProductController
ProductService
ProductRequest
ProductResponse
UploadController
ImageUpload
MultipartFile
/api/products
/api/upload
/api/uploads
imageUrl
```

---

# A. Fix Product Save Failure

Inspect `ProductEditActivity.saveProduct()`.

Confirm backend expected request fields from backend controller/request DTO.

Possible expected product fields:

```text
name
description
originalPrice
sellingPrice
stockQuantity
unit
imageUrl
categoryId
isActive
```

Do not assume. Confirm from backend.

Fix:

1. Validate required fields safely:
   - name is not empty
   - selling price is valid and > 0
   - original price is valid and >= selling price if backend requires it
   - stock is valid and >= 0
   - unit is not empty
   - category is valid
2. Build request body using exact backend field names.
3. Send `imageUrl` as the uploaded image URL.
4. Prevent double-click:
   - disable save button while request is running
   - re-enable on failure
5. On success:
   - show success Toast
   - finish activity or navigate back
   - Admin Products list should reload in `onResume()`
6. On failure:
   - show readable Toast
   - log HTTP code
   - log backend error body if available
   - log Retrofit exception message
7. Do not log tokens, passwords, cookies, or secrets.

Check Retrofit return type:

- If backend create/update returns raw `ProductResponse`, use:

```java
Call<Product>
```

or the exact matching DTO.

- If backend create/update returns wrapper, keep:

```java
Call<ApiResponse<Product>>
```

Only change this after confirming backend response shape.

---

# B. Fix Product Update

For existing product edit:

1. Read `PRODUCT_ID` safely.
2. If ID is null/empty:
   - show Toast
   - finish safely
3. Load product by correct endpoint:

```text
GET /api/products/{id}
```

4. Bind product fields safely.
5. Preserve old `imageUrl` if admin does not pick a new image.
6. If admin picks a new image:
   - upload first
   - replace `selectedImageUrl` with returned public URL
7. Save update through correct endpoint:

```text
PUT /api/products/{id}
```

8. Confirm response type matches backend.
9. Show clear success/failure messages.

---

# C. Fix Product Delete

In `AdminProductListFragment.java`, inspect delete logic.

Verify backend endpoint:

```text
DELETE /api/products/{id}
```

If backend returns `204 No Content`, use:

```java
Call<Void>
```

instead of:

```java
Call<ApiResponse<Void>>
```

if current response type causes parsing/null-body issue.

Delete rules:

1. Audit role cannot delete.
2. Null/empty product ID cannot delete.
3. Confirm before delete.
4. On success:
   - reload product list with `loadProducts()`
   - or remove item from local list and call `applyFilters()`
5. On failure:
   - show readable Toast
   - log HTTP code/error body.

---

# D. Implement Gallery Picker + Upload API

In `ProductEditActivity.java`, fix "Chọn ảnh từ Gallery".

Preferred Android approach:

Use Activity Result API:

```java
ActivityResultLauncher<String> imagePickerLauncher;
```

with:

```java
ActivityResultContracts.GetContent()
```

MIME type:

```text
image/*
```

Behavior:

1. Button click launches image picker.
2. If user cancels, do nothing and do not crash.
3. If user selects image:
   - save selected `Uri`
   - preview with Glide
   - upload selected image through backend API
   - show upload loading state if possible
   - on upload success, store returned public URL in `selectedImageUrl`
   - on upload failure, show Toast and keep preview but prevent product save unless valid image URL already exists

Permissions:

- Prefer `ActivityResultContracts.GetContent()` or system picker so broad storage permission is not needed.
- Do not request unnecessary permissions.
- If current min/target SDK requires permission for the chosen approach, add the minimum correct permission only.

---

# E. Implement / Use Image Upload API

Search backend for an existing upload endpoint.

Look for controllers/services using:

```text
MultipartFile
upload
image
file
```

If existing upload endpoint exists:

1. Add or fix shared Retrofit upload API.
2. Use `MultipartBody.Part` for image file.
3. Use `RequestBody` as needed.
4. Parse returned image URL correctly.
5. Use returned public URL as product `imageUrl`.

Expected Retrofit shape may look like:

```java
@Multipart
@POST("api/uploads/images")
Call<ApiResponse<UploadResponse>> uploadImage(@Part MultipartBody.Part file);
```

But do not assume route or response. Confirm from backend.

If no upload endpoint exists:

1. Add a minimal backend upload endpoint if consistent with existing backend style.
2. Save uploaded image under a backend-served static directory.
3. Return a public URL/path that both Admin and Customer can load.
4. Ensure backend exposes uploaded files statically.
5. Keep the change minimal.
6. Do not add cloud storage unless already used in project.

Minimum backend behavior:

- Accept multipart image file.
- Validate file is not empty.
- Validate content type starts with `image/`.
- Save file with safe unique filename.
- Return JSON containing public URL, for example:

```json
{
  "url": "/uploads/products/filename.jpg"
}
```

or wrapped in existing `ApiResponse` style if backend uses that.

Important:

The returned URL must be usable by Customer app Glide/image loading.

If backend returns relative URL like:

```text
/uploads/products/file.jpg
```

Android should convert it to full URL using the existing backend base URL if current image loader requires full URL.

---

# F. Convert Uri To Multipart Correctly

When uploading selected Gallery image from Android:

1. Open InputStream from ContentResolver.
2. Read bytes safely.
3. Determine file extension/content type from ContentResolver when possible.
4. Create `RequestBody` with correct media type.
5. Create `MultipartBody.Part` with field name expected by backend, likely:

```text
file
```

but confirm from backend.
6. Upload using Retrofit.

Avoid direct file path assumptions from Gallery URI.
Do not use deprecated `getRealPathFromURI` style unless already safely implemented and necessary.
Do not crash on `content://` URIs.

---

# G. Customer Sync Requirement

Admin and Customer must use the same backend source of truth.

Check customer screens:

```text
app-customer/src/main/java/vn/vuavuive/customer/ui/product/ProductListFragment.java
app-customer/src/main/java/vn/vuavuive/customer/ui/home/HomeFragment.java
app-customer/src/main/java/vn/vuavuive/customer/ui/search/SearchActivity.java
```

Verify:

1. Customer product list loads from backend `GET /api/products`.
2. Customer product detail loads from backend `GET /api/products/{id}`.
3. Customer home/search do not rely on stale mock data if backend is available.
4. After admin add/edit/delete, customer reload shows updated data.
5. Customer image loading can display uploaded product image URL.

Do not implement realtime sync.
Just ensure both apps use backend APIs and refresh/reload correctly.

---

# H. Image URL Normalization

Make image loading robust.

If product `imageUrl` is:

1. Full URL:
   - use as is.
2. Relative path starting with `/uploads/...`:
   - convert to full URL with backend base URL.
3. Empty/null:
   - show placeholder and do not crash.

Apply only where needed, preferably with existing utility if project already has one.

Do not duplicate URL normalization in many places if a shared helper already exists.

---

# I. Logcat Debugging

You are allowed to use adb Logcat.

Start:

```powershell
adb devices
adb logcat -c
```

Reproduce:

1. Open admin app.
2. Go to Products.
3. Tap Add Product.
4. Fill all fields.
5. Tap "Chọn ảnh từ Gallery".
6. Select image.
7. Tap "LƯU THÔNG TIN".
8. Try edit product with new image.
9. Try delete product.
10. Open customer app and reload product list/home/search.

Capture:

```powershell
adb logcat -d > admin_product_crud_image_upload_logcat.txt
```

Filtered:

```powershell
adb logcat -d | findstr /i "FATAL EXCEPTION AndroidRuntime vuavuive admin customer retrofit product gallery permission image uri upload multipart save create update delete" > admin_product_crud_image_upload_filtered.txt
```

Use Logcat to identify root cause before broad changes.

---

# J. Build Commands

After Java/XML changes:

```powershell
.\gradlew.bat :app-admin:assembleDebug
.\gradlew.bat :app-customer:assembleDebug
```

If backend code changes:

- Run the project’s backend build/test command if available.
- If no obvious command exists, at least ensure backend Java code compiles through the existing project build command if present.

Fix compile errors with the smallest changes.

---

# K. Debug Notes

Create or update:

```text
ADMIN_PRODUCT_CRUD_IMAGE_UPLOAD_NOTES.md
```

Include:

```text
Product CRUD + Image Upload checkpoint

Root causes:
- save failure:
- gallery picker failure:
- image upload issue:
- customer sync issue:

Backend upload API:
- route:
- request field name:
- response shape:
- public URL format:

Files changed:
-

Build:
- app-admin assembleDebug:
- app-customer assembleDebug:
- backend build/check:

Manual test:
- Admin add product:
- Admin pick Gallery image:
- Admin upload image:
- Admin save product:
- Admin edit product:
- Admin delete product:
- Customer reload product list:
- Customer reload home/search:
- Customer image display:

Remaining limitations:
-
```

---

# L. Final Report Format

Use this exact final response:

```text
Product CRUD + image upload checkpoint completed.

Build:
- .\gradlew.bat :app-admin:assembleDebug: PASS or FAIL
- .\gradlew.bat :app-customer:assembleDebug: PASS or FAIL
- backend build/check: PASS or FAIL or NOT RUN

Runtime Logcat:
- Device detected: YES or NO
- Logcat captured: YES or NO
- First relevant failure: describe root cause or say none found

Files changed:
- file 1
- file 2

Bugs fixed:
- Admin save product:
- Admin edit product:
- Admin delete product:
- Gallery picker:
- Image upload:
- Customer sync:

Backend/API findings:
- product create route:
- product update route:
- product delete route:
- upload route:
- upload response shape:

Image handling:
- Gallery picker fixed: YES or NO
- Upload API used/implemented: YES or NO
- Product imageUrl stores public URL: YES or NO
- Customer can display uploaded image: YES or NO

Need manual test:
- Add product with Gallery image
- Edit product with new Gallery image
- Delete product
- Reload customer product list
- Reload customer home/search
```

Important:
Do not claim runtime is fully fixed unless Logcat/emulator test confirms it.
If only build passes, say build passes but runtime still needs emulator verification.
