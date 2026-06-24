# Vựa Vui Vẻ — Firebase Migration Wrap-up for New Antigravity Session

**Date:** 2026-06-24  
**Purpose:** This file summarizes the current state, decisions, completed checkpoints, pending verification, and next actions for the Vựa Vui Vẻ Android project migration from SQLite/Spring Boot backend to Firebase.

---

## 1. Current High-level Goal

The project is migrating from the old architecture:

```text
Android Apps → Retrofit API → Spring Boot Backend → SQLite/PostgreSQL
```

to the new Firebase-first architecture:

```text
Android Apps → Firebase Authentication + Firebase Realtime Database
```

The target is to make the core app flows run directly on Firebase:

```text
Products / Categories → Firebase Realtime Database
Auth / User Profile   → Firebase Authentication + /users/{firebaseUid}
Cart                  → Firebase /carts/{firebaseUid} with Room only as local cache if needed
Orders                → Firebase /orders/{orderId}
Admin                 → Firebase products/orders/users management
Shipper               → Firebase assigned orders/status updates, can be done later
```

Important: **Do not delete the old backend, SQLite/Room, Retrofit repositories, or API code until Firebase-only verification passes.**  
The user does **not** want intermediate git commits. Complete the migration and verify first, then commit once at the end.

---

## 2. Project Modules

Expected modules:

```text
VuaVuiVe_App/
├── app-customer   # Customer Android app, currently main migration target
├── app-admin      # Admin Android app, still needs Firebase connection verification
├── app-shipper    # Shipper Android app, may be done later
├── app-backend    # Spring Boot backend legacy/backup
├── shared         # DTOs, Retrofit interfaces, Room models, utilities
```

Old project behavior from the task checklist:
- `app-customer` originally used `AuthViewModel → AuthRepository → API/JWT/SessionManager`.
- Cart originally used Room/local behavior and/or API sync.
- Orders originally used API endpoints.
- `app-admin` historically used mock data for many screens.
- `app-backend` had Spring Boot REST API, JWT, PostgreSQL/SQLite, VNPay/MoMo, Gemini, etc.

---

## 3. Firebase Project / App Configuration

Firebase project:

```text
vua-vui-ve
```

Android app package names:

```text
Customer: vn.vuavuive.customer
Admin:    vn.vuavuive.admin
Shipper:  vn.vuavuive.shipper
```

Each module must use a real `google-services.json` from Firebase Console:

```text
app-customer/google-services.json
app-admin/google-services.json
app-shipper/google-services.json
```

Do not rely on fake placeholder `google-services.json` files for runtime. Fake files only allow Gradle compilation but will not connect to the actual Firebase project.

Firebase Authentication provider:
- Email/Password must be enabled.
- The app uses phone input but converts it to a fake email:

```text
0906760495 → 0906760495@vuavuive.local
```

Only enable **Email address/Password**. Do not need to enable passwordless email link.

---

## 4. Firebase Realtime Database Structure

Current expected top-level nodes:

```json
{
  "categories": {},
  "products": {},
  "users": {},
  "carts": {},
  "orders": {},
  "recipes": {},
  "reviews": {},
  "shippers": {},
  "otps": {}
}
```

Known migrated data:
- `/products`: 92 products
- `/categories`: 8 categories
- `/recipes`: roughly 41 recipes
- `/orders`: now being tested through Firebase order flow
- `/users`: should use Firebase Auth UID as key
- `/carts`: should use Firebase Auth UID as key

Important design decision:
- `orders/{orderId}/items` stores product snapshots at purchase time.
- Do not store only `product_id` in order items.
- Snapshot fields should include product name, image, unit, price, quantity, subtotal.

Recommended order item shape:

```json
{
  "product_id": "90000000-0000-0000-0000-000000000100",
  "product_name": "Rau muống Firebase Test",
  "image_url": "https://...",
  "unit": "gói",
  "unit_price": 36000,
  "quantity": 2,
  "subtotal": 72000
}
```

---

## 5. Naming Convention

Firebase data currently mostly follows `snake_case`:

```text
category_id
category_name
is_active
created_at
updated_at
product_id
product_name
image_url
unit_price
stock_quantity
saved_for_later
status_logs
```

Java/Android models may use `camelCase`, but the mapping must be explicit and tested. Avoid mixing snake_case and camelCase across Product, Cart, and Order data unless the DTO mapping is clearly handled.

---

## 6. Security Rules Principle

Never use public global rules:

```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

Also avoid broad authenticated-only rules for sensitive nodes:

```json
"users": { ".read": "auth != null", ".write": "auth != null" },
"carts": { ".read": "auth != null", ".write": "auth != null" },
"orders": { ".read": "auth != null", ".write": "auth != null" }
```

That would allow any logged-in user to access other users' carts, orders, and profiles.

Rules must scope by `auth.uid`:
- Customer can read/write only their own `/users/{uid}` and `/carts/{uid}`.
- Customer can read their own orders where `user_id == auth.uid`.
- Admin can manage products/categories/orders.
- Shipper can read/update only assigned orders where `shipper_id == auth.uid`.
- `otps` should be locked.
- Users must not be able to change their own role to `ADMIN` or `SHIPPER`.

Minimum cart rule:

```json
{
  "rules": {
    "carts": {
      "$uid": {
        ".read": "auth != null && $uid === auth.uid",
        ".write": "auth != null && $uid === auth.uid"
      }
    }
  }
}
```

User role validation idea:

```json
"users": {
  "$uid": {
    ".read": "auth != null && ($uid === auth.uid || root.child('users').child(auth.uid).child('role').val() === 'ADMIN')",
    ".write": "auth != null && $uid === auth.uid",
    "role": {
      ".validate": "(!data.exists() && newData.val() === 'CUSTOMER') || (data.exists() && newData.val() === data.val())"
    }
  }
}
```

---

## 7. Checkpoint Status

### Checkpoint 1 — Products & Categories

**Status: Completed and runtime-tested.**

Completed work:
- Firebase SDK added.
- Customer product/category loading moved to Firebase.
- `ProductRepositoryFirebase` reads `/products`.
- `CategoryRepositoryFirebase` reads `/categories`.
- `CategoryViewModel` uses Firebase category repository.
- `ProductViewModel` uses Firebase product repository.
- Build passed:

```bash
./gradlew :app-customer:assembleDebug
```

Runtime test passed:
- Product ID `90000000-0000-0000-0000-000000000100` was renamed in Firebase from:

```text
Rau muống (500g)
```

to:

```text
Rau muống Firebase Test
```

- App reloaded and displayed the updated name.
- Logcat confirmed `onDataChange` and 92 products loaded from Firebase.

Bug fixed:
- `ProductAdapter.areContentsTheSame` was comparing incorrectly, causing UI not to refresh after product data changed.
- It was fixed to compare content fields such as `name`, `price`, `stock`, `unit`, `imageUrl`.

### Category Filter Hotfix

**Status: Completed and runtime-tested.**

Issue:
- UI category filters use short slugs such as:

```text
veg
fruit
meat
```

- But Firebase product `category_id` uses UUIDs such as:

```text
80000000-0000-0000-0000-000000000001
```

Fix:
- `CategoryResponse.java` was updated with no-arg constructor, setters, and `isActive` support.
- `CategoryRepositoryFirebase.java` created and caches categories.
- `ProductRepositoryFirebase.getProducts()` now loads/caches category slug → UUID mapping.
- Filtering converts slug to UUID before comparing with product `category_id`.
- `"all"` and `"Tất cả"` bypass category filtering and return all active products.

Runtime test passed:
- Home categories render dynamically from `/categories`.
- Clicking `"Trái cây"` maps to UUID `80000000-0000-0000-0000-000000000002`.
- Product list shows only fruit products like `"Táo Mỹ (1kg)"`, `"Chuối Laba (1kg)"`.
- Clicking `"Tất cả"` returns all products.

---

### Checkpoint 2 — Firebase Auth & User Profile

**Status: Implemented/partially tested; verify again during Firebase-only test.**

Target:
- Replace Spring Boot/JWT auth with Firebase Authentication.
- UI still accepts phone number + password.
- Repository converts phone number to fake email `{phone}@vuavuive.local`.

Expected repository:

```text
FirebaseUserRepository.java
```

Expected behavior:
- `register()`:
  1. Normalize phone.
  2. Convert to fake email.
  3. `createUserWithEmailAndPassword`.
  4. Create `/users/{firebaseUid}`.
  5. Default role is `CUSTOMER`.
  6. Save session using `SessionManager`.

- `login()`:
  1. Normalize phone.
  2. Convert to fake email.
  3. `signInWithEmailAndPassword`.
  4. Load `/users/{firebaseUid}`.
  5. If missing, create default profile.
  6. Save session.

`SessionManager` should store:
```text
firebaseUid
phone
fullName
email
role
address
isLoggedIn
```

Must verify:
- Backend off, login still works.
- Firebase Authentication has user `09...@vuavuive.local`.
- RTDB has `/users/{firebaseUid}` with `role: CUSTOMER`.
- User cannot change role to `ADMIN`.

---

### Checkpoint 3 — Cart Integration

**Status: Planned/possibly implemented; verify carefully.**

Design:
- Firebase is remote source.
- Room may remain as local/offline cache.
- Path:

```text
/carts/{firebaseUid}
```

Recommended structure:

```json
{
  "items": {
    "product_id_1": {
      "product_id": "product_id_1",
      "quantity": 2,
      "product_name": "Rau muống sạch",
      "unit_price": 15000,
      "image_url": "https://...",
      "unit": "Bó",
      "stock_quantity": 50,
      "subtotal": 30000,
      "updated_at": "2026-06-24T..."
    }
  },
  "saved_for_later": {},
  "updated_at": "2026-06-24T..."
}
```

Important rules:
- Use `firebaseUid` as cart key.
- User can only read/write `/carts/{auth.uid}`.
- Do not trust stock stored in cart during checkout.
- Checkout must read stock from `/products/{productId}/stock_quantity`.

Merge rule:
```text
mergedQuantity = min(localQuantity + remoteQuantity, latestProductStock)
```

Empty cart rule:
- If `items` empty: delete only `/carts/{uid}/items`.
- Do not delete `/carts/{uid}` if `saved_for_later` still exists.

Must verify:
- Add/update/delete cart writes to Firebase.
- Logout/login retains cart from Firebase.
- Clear cart after checkout.
- If offline cache exists, Firebase remains remote source of truth.

---

### Checkpoint 4 — Order Integration

**Status: Implemented/tested in recent Antigravity run, but needs structured Firebase-only verification.**

Recent Antigravity actions reported:
- Edited `FirebaseOrderRepository.java`.
- Edited `OrderViewModel.java`.
- Added setters to `Order.java`, `OrderItem.java`, `ReturnRequest.java`, and related DTOs.
- Built `:app-customer:assembleDebug`.
- Installed and launched app on emulator.
- Tested checkout/order flow.
- Read/wrote Firebase `/orders`.
- Reduced `stock_quantity` on checkout.
- Restored `stock_quantity` on cancel.
- Manually changed an order status to `DELIVERED` to prepare return request testing.

Order rules:
- Create orders under `/orders/{orderId}`.
- Use `user_id = firebaseUid`.
- Save product snapshots under `items`.
- Save logs under `status_logs`.
- COD is main test path.
- VNPay/MoMo may be mock only, clearly marked.

Recommended order status values should be uppercase:

```text
PENDING
CONFIRMED
SHIPPING
DELIVERED
CANCELLED
RETURN_REQUESTED
FAILED
```

Payment status:

```text
UNPAID
PAID
REFUNDED
PAYMENT_PENDING
```

Order creation flow:
1. Read current cart.
2. Read user profile.
3. Validate cart not empty.
4. For each product, transaction at:

```text
/products/{productId}/stock_quantity
```

5. If any product lacks stock, rollback any stock already reduced.
6. If all OK, write `/orders/{orderId}`.
7. Write initial `status_logs`.
8. Clear Firebase cart and Room cart only after order creation succeeds.

Important:
- Do not run transaction at root database.
- Do not call it full SQL-style multi-table transaction.
- Client-side transaction is acceptable for this course project, but production should use backend/Cloud Functions.

Cancel flow:
- Only allow cancel when status is `PENDING` or `CONFIRMED`.
- Set:

```json
{
  "status": "CANCELLED",
  "stock_restored": true,
  "cancelled_at": "...",
  "cancel_reason": "..."
}
```

- Restore stock once only.
- Prevent double-restock if cancel is clicked twice.

Return flow:
- Only allow return for `DELIVERED`.
- Set status to `RETURN_REQUESTED`.
- Do not restore stock immediately.
- Admin should approve return later.

Must verify:
- COD checkout creates order.
- Stock decreases correctly.
- My Orders reads current user orders.
- Cancel restores stock once.
- Return request changes status without restoring stock.

---

### Checkpoint 5 — Admin App Firebase Integration

**Status: Not fully verified yet; must be tested before cleanup.**

Critical because old `app-admin` may still use `MockRepository`.

Required verification:
- `app-admin` has real `google-services.json`.
- Admin auth works or admin role is recognized from `/users/{uid}/role`.
- Admin reads `/products` and `/orders` from Firebase, not mock data.
- Admin product update modifies Firebase.
- Customer app sees product update.
- Admin sees customer-created order.
- Admin changes order status.
- Customer sees updated order status.
- Admin assigns `shipper_id` / `shipper_name`.

Critical cross-app test:

```text
Customer creates order
→ Admin sees same order from Firebase
→ Admin changes status
→ Customer sees changed status
```

---

### Checkpoint 6 — Shipper App

**Status: Can be deferred.**

Later required verification:
- Shipper account has `role = SHIPPER`.
- `/shippers/{shipperUid}` exists.
- Admin assigns order with `shipper_id = shipperUid`.
- Shipper sees assigned order only.
- Shipper updates status:

```text
SHIPPING → DELIVERED / FAILED
```

- Customer and Admin see updated status.

---

## 8. Current Immediate Goal

The user now wants **Firebase-only verification before cleanup**.

The goal is:

1. Confirm all core data has migrated to Firebase.
2. Confirm `app-customer` connects to Firebase only.
3. Confirm `app-admin` connects to the same Firebase data.
4. `app-shipper` can be postponed but must be clearly marked as pending.
5. Turn off old backend/database and ensure the app still works.
6. Only after passing this, start removing legacy DB/API/backend code.

---

## 9. Required Firebase-only Test Plan

### TEST A — Firebase Data Inventory

Check in Firebase Console:

```text
/categories
/products
/users
/carts
/orders
/recipes
/reviews
/shippers
```

Pass criteria:

```text
[ ] /categories has 8 categories
[ ] /products has 92 products
[ ] /products have name, price, image_url, unit, stock_quantity, category_id
[ ] /users has Firebase UID keys
[ ] /carts/{firebaseUid} appears after adding cart
[ ] /orders/{orderId} appears after checkout
[ ] order items include product snapshot
[ ] order status_logs exists
```

---

### TEST B — Customer Firebase-only

Turn off:
- Spring Boot backend
- PostgreSQL/SQLite server if any
- Anything running old API on port 3000

Then:

```bash
adb shell pm clear vn.vuavuive.customer
adb shell am start -n vn.vuavuive.customer/vn.vuavuive.customer.ui.auth.LoginActivity
```

Pass criteria:

```text
[ ] Register/login using Firebase Auth
[ ] Products/categories load from Firebase
[ ] Search/filter/category chips work
[ ] Product detail opens
[ ] Add product to cart
[ ] /carts/{firebaseUid}/items updates
[ ] Update quantity updates Firebase
[ ] Remove item updates Firebase
[ ] Checkout COD creates /orders/{orderId}
[ ] Product stock_quantity decreases
[ ] Cart clears after successful order
[ ] My Orders shows new order
[ ] Cancel PENDING order sets CANCELLED
[ ] Cancel restores stock
[ ] Cancel again does not restore stock twice
[ ] RETURN_REQUESTED flow works for DELIVERED order
```

---

### TEST C — Admin Firebase Connection

Pass criteria:

```text
[ ] Backend still off
[ ] Admin app launches
[ ] Admin reads products from /products
[ ] Admin reads orders from /orders
[ ] Admin updates product name/price/stock in Firebase
[ ] Customer app refresh shows admin product update
[ ] Customer-created order appears in Admin
[ ] Admin updates order status
[ ] Customer sees updated status
```

If Admin still uses `MockRepository`, do not cleanup yet. First migrate Admin products/orders to Firebase.

---

### TEST D — Cross-app Sync

Required proof:

```text
Customer app creates order
→ Firebase /orders updates
→ Admin app displays same order
→ Admin app updates status
→ Customer app displays new status
```

This is the clearest proof that both apps are using the same Firebase database.

---

### TEST E — Backend Dependency Check

With backend off, check logs/code:

```text
[ ] No runtime call to /api/auth
[ ] No runtime call to /api/products
[ ] No runtime call to /api/orders
[ ] No runtime call to localhost:3000
[ ] FirebaseProductRepository is used
[ ] CategoryRepositoryFirebase is used
[ ] FirebaseUserRepository is used
[ ] FirebaseCartRepository is used
[ ] FirebaseOrderRepository is used
```

Suggested grep:

```bash
grep -R "localhost:3000\|/api/products\|/api/orders\|/api/auth\|Retrofit\|OrderRepository\|ProductRepository\|AuthRepository" app-customer shared app-admin
```

Do not delete matches blindly. Use grep only to identify remaining dependencies.

---

## 10. Cleanup Gate

Cleanup is allowed only after these are true:

| Area | Must pass |
|---|---|
| Product/Category | Customer and Admin read Firebase |
| Auth/User | Login/register works without backend |
| Cart | Add/update/delete writes Firebase |
| Order | Checkout/cancel/return writes Firebase |
| Admin | Admin sees and updates Firebase orders/products |
| Shipper | Can be marked pending if deferred |
| Backend | App core flows work with backend off |
| SQLite/Room | Not used as source of truth |

Safe cleanup order:

```text
1. Stop using Retrofit repositories in runtime.
2. Stop API calls in ViewModels.
3. Keep backend folder but do not run it.
4. Archive app-backend after demo/test passes.
5. Keep Room only if it is deliberately used as offline cache.
6. Remove Room only if Firebase is confirmed as sole source and no offline cart cache is needed.
7. Remove dependencies/code old APIs last.
```

---

## 11. Do Not Do

```text
Do not commit yet. User wants one final commit after all migration is complete.
Do not delete backend/SQLite/Room/Retrofit before verification.
Do not open Firebase rules public.
Do not use auth != null globally for users/carts/orders.
Do not do Cart/Order using backend user UUID instead of Firebase UID.
Do not trust stock stored in cart during checkout.
Do not transaction at database root.
Do not put MoMo/VNPay/Gemini/Telegram secret keys in Android.
Do not mark Shipper done if it has not been verified.
```

---

## 12. Message to Continue Work

Use this as a direct instruction to continue:

```text
Continue from the current Firebase migration state.

The next task is Firebase-only verification before cleanup.

Do not commit yet. Do not delete legacy code yet.

First, turn off Spring Boot/backend and verify app-customer works with Firebase only:
- Firebase Auth login/register
- Products/categories
- Cart at /carts/{firebaseUid}
- COD checkout to /orders
- stock decrement/restoration
- My Orders
- cancel and return request

Then verify app-admin reads/writes the same Firebase /products and /orders:
- admin sees customer-created order
- admin updates product/order status
- customer sees those updates

If app-admin still uses mock data, migrate its product/order repositories to Firebase before cleanup.

Shipper can be deferred, but mark it clearly as pending.

Only after Customer + Admin Firebase-only verification passes should cleanup of old Retrofit/API/backend/SQLite code begin.
```

