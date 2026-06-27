# Codex Task: Implement MoMo Sandbox PayUrl Flow for VuaVuiVe

## 1. Project Context

Project: **VuaVuiVe**

System components:

- `app-backend`: Spring Boot backend, currently integrated with Firebase Realtime Database and MoMo-related payment modules.
- `app-customer`: Android customer app using Java/XML.
- `shared`: shared DTO/API module for Android Retrofit models.

The current MoMo payment implementation has two separate flows:

| Flow | Current status | Problem |
|---|---|---|
| Firebase mock flow | Active in Android app through `FirebaseOrderRepository` | It marks MoMo orders as `PAID` and `CONFIRMED` immediately, without backend, IPN, or real sandbox payment. |
| Backend sandbox flow | Partially implemented in Spring Boot | It should create a real MoMo sandbox payment and return `payUrl`/deeplink, but Android is not consistently using it. |

The user cannot reliably log in to **MoMo UAT/Test App**, so the required implementation should prioritize opening MoMo sandbox `payUrl` in browser/WebView instead of requiring the MoMo app login.

## 2. Main Goal

Implement a stable MoMo sandbox payment flow where:

1. Android creates an order with payment method `MOMO`.
2. Backend creates a MoMo sandbox payment request.
3. Backend returns `payUrl` to Android.
4. Android opens `payUrl` using browser/Custom Tab/WebView.
5. Backend receives MoMo IPN callback and updates payment/order status.
6. Android checks payment status after the user returns.
7. A development-only mock success fallback is available for demo.

Do **not** remove the COD flow.

## 3. Important Business Flow

The correct order lifecycle must be:

| Payment method | Payment result | `paymentStatus` | `orderStatus` |
|---|---|---|---|
| COD | Order placed | `UNPAID` or `COD_PENDING` | `PENDING_APPROVAL` / `CHỜ DUYỆT` |
| MoMo | Payment created, not paid yet | `PENDING` / `PENDING_PAYMENT` | `PENDING_PAYMENT` |
| MoMo | Payment success | `PAID` | `PENDING_APPROVAL` / `CHỜ DUYỆT` |
| MoMo | Payment failed/cancelled | `FAILED` or `UNPAID` | stays `PENDING_PAYMENT` or becomes `CANCELLED`, depending on existing project convention |
| Admin approves | Approved order | unchanged | `CONFIRMED` / `ĐÃ XÁC NHẬN` |
| Shipper | Can see orders | unchanged | only `CONFIRMED` orders |

Critical fix:

- Backend IPN/mock result must **not** update MoMo-paid orders directly to `CONFIRMED`.
- After successful MoMo payment, set order to `PENDING_APPROVAL` so admin still has to approve before shipper sees it.

## 4. Relevant Files From Current Context

Backend:

- `app-backend/src/main/resources/application-dev.yml`
- `app-backend/src/main/java/vn/vuavuive/backend/config/AppConfig.java`
- `app-backend/src/main/java/vn/vuavuive/backend/modules/order/Order.java`
- `app-backend/src/main/java/vn/vuavuive/backend/modules/order/OrderRepository.java`
- `app-backend/src/main/java/vn/vuavuive/backend/modules/order/OrderService.java`
- `app-backend/src/main/java/vn/vuavuive/backend/modules/payment/MomoController.java`
- `app-backend/src/main/java/vn/vuavuive/backend/modules/payment/PaymentController.java`
- `app-backend/src/main/java/vn/vuavuive/backend/modules/payment/MoMoService.java`
- `app-backend/src/main/java/vn/vuavuive/backend/modules/payment/PaymentTransaction.java`
- `app-backend/src/main/java/vn/vuavuive/backend/modules/payment/PaymentTransactionRepository.java`
- `app-backend/src/main/java/vn/vuavuive/backend/modules/payment/dto/CreateMomoPaymentRequest.java`
- `app-backend/src/main/java/vn/vuavuive/backend/modules/payment/dto/CreateMomoPaymentResponse.java`
- `app-backend/src/main/java/vn/vuavuive/backend/modules/payment/dto/MomoCreateRequest.java`
- `app-backend/src/main/java/vn/vuavuive/backend/modules/payment/dto/MomoCreateResponse.java`
- `app-backend/src/main/java/vn/vuavuive/backend/modules/payment/dto/MomoIpnRequest.java`

Android customer app:

- `app-customer/src/main/java/vn/vuavuive/customer/data/repository/FirebaseOrderRepository.java`
- `app-customer/src/main/java/vn/vuavuive/customer/data/repository/OrderRepository.java`
- `app-customer/src/main/java/vn/vuavuive/customer/ui/checkout/CheckoutActivity.java`
- `app-customer/src/main/java/vn/vuavuive/customer/ui/checkout/PaymentResultActivity.java`
- `app-customer/src/main/java/vn/vuavuive/customer/ui/checkout/PaymentWebViewActivity.java`
- `app-customer/src/main/java/vn/vuavuive/customer/viewmodel/OrderViewModel.java`

Shared Android module:

- `shared/src/main/java/vn/vuavuive/shared/data/api/PaymentApi.java`
- `shared/src/main/java/vn/vuavuive/shared/data/dto/CreateMomoPaymentRequest.java`
- `shared/src/main/java/vn/vuavuive/shared/data/dto/CreateMomoPaymentResponse.java`
- `shared/src/main/java/vn/vuavuive/shared/data/dto/PaymentStatusResponse.java`

## 5. Backend Implementation Requirements

### 5.1 MoMo configuration

Inspect `application-dev.yml` and make sure the backend supports:

- `partner-code`
- `access-key`
- `secret-key`
- sandbox endpoint, usually `https://test-payment.momo.vn`
- `redirect-url`
- `ipn-url`
- `mock-mode`

If credentials are missing, keep the app runnable by using `mock-mode = true`, but make the real sandbox path ready when credentials are provided.

### 5.2 Create payment endpoint

Ensure there is one clean endpoint for Android to call, preferably:

```text
POST /api/payments/momo
```

Request should include at least:

```json
{
  "orderId": "string",
  "amount": 100000,
  "orderInfo": "Pay order ..."
}
```

Response should include at least:

```json
{
  "success": true,
  "orderId": "string",
  "paymentStatus": "PENDING",
  "payUrl": "https://test-payment.momo.vn/...",
  "deeplink": "momo://...",
  "qrCodeUrl": "...",
  "message": "Created MoMo payment"
}
```

Android must be able to proceed with `payUrl` even if `deeplink` is unavailable.

### 5.3 Payment transaction persistence

When creating a payment, save/update a payment transaction record:

- `orderId`
- `requestId`
- `amount`
- `paymentMethod = MOMO`
- `paymentStatus = PENDING`
- `payUrl`
- `createdAt`
- raw MoMo response if already supported

### 5.4 IPN callback

Ensure backend has:

```text
POST /api/payments/momo/ipn
```

Required behavior:

1. Parse MoMo IPN.
2. Verify signature.
3. Verify `orderId`, `amount`, and transaction information.
4. If success result:
   - update transaction `paymentStatus = PAID`
   - update order `paymentStatus = PAID`
   - update order `orderStatus = PENDING_APPROVAL` / `CHỜ DUYỆT`
5. If failed/cancelled result:
   - update transaction `paymentStatus = FAILED`
   - keep order out of admin-approved/shipper-visible states
6. Return the response format expected by MoMo.

Do not set successful MoMo orders to `CONFIRMED` in IPN.

### 5.5 Status endpoint

Ensure Android can check payment result:

```text
GET /api/payments/{orderId}/status
```

Expected response:

```json
{
  "orderId": "string",
  "paymentStatus": "PAID",
  "orderStatus": "PENDING_APPROVAL",
  "message": "Payment completed"
}
```

### 5.6 Development mock fallback

Add or keep a development-only endpoint:

```text
POST /api/payments/momo/mock-success/{orderId}
```

Behavior:

- Only enabled in dev/mock mode.
- Updates payment transaction to `PAID`.
- Updates order `paymentStatus = PAID`.
- Updates order `orderStatus = PENDING_APPROVAL`.
- Does not mark order as `CONFIRMED`.

Also support mock failure if simple to add:

```text
POST /api/payments/momo/mock-fail/{orderId}
```

## 6. Android Implementation Requirements

### 6.1 Use backend Retrofit repository for MoMo

Inspect `OrderViewModel` and repository injection/creation.

Current issue:

- `OrderViewModel` is using `FirebaseOrderRepository`.
- That causes MoMo payments to be marked successful immediately.

Required fix:

- For MoMo payment creation, use Retrofit/backend `OrderRepository` or a dedicated `PaymentRepository`.
- Do not call `FirebaseOrderRepository.createMomoPayment(...)` for real MoMo flow.
- Keep Firebase realtime reads if the app relies on Firebase for UI updates, but payment creation should go through backend.

### 6.2 Checkout flow

In `CheckoutActivity.java`:

1. User chooses MoMo.
2. Create order with initial status:
   - `paymentStatus = PENDING`
   - `orderStatus = PENDING_PAYMENT`
3. Call backend create MoMo payment endpoint.
4. Receive `payUrl`.
5. Open `PaymentResultActivity` or `PaymentWebViewActivity` with:
   - `orderId`
   - `payUrl`
   - optional `deeplink`

### 6.3 Prefer payUrl over MoMo UAT app deeplink

Because MoMo UAT login is unreliable for this user, Android should:

1. Prefer opening `payUrl` in browser, Custom Tab, or WebView.
2. Treat deeplink as optional.
3. Never block payment only because the MoMo app is missing or cannot be opened.

Simple acceptable implementation:

```java
Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(payUrl));
startActivity(intent);
```

If the project already has `PaymentWebViewActivity`, it is acceptable to use it instead, as long as it handles return URLs correctly.

### 6.4 Payment result screen

In `PaymentResultActivity.java`:

- Show payment pending state after opening `payUrl`.
- Provide button: `I have completed payment` / `Kiểm tra thanh toán`.
- On click, call backend status endpoint.
- On `paymentStatus = PAID`:
  - show success
  - clear cart
  - navigate to Orders screen
- On `PENDING`:
  - show "Payment is still pending"
- On `FAILED`:
  - show failure and allow retry

Also refresh status in `onResume()`, but do not assume success just because the user returned from browser.

### 6.5 Debug/mock success button

For demo safety, add a debug-only/mock-mode action:

- Button text: `Mock payment success` or `Demo: mark paid`
- Calls:

```text
POST /api/payments/momo/mock-success/{orderId}
```

- After success, refresh payment status and navigate normally.

This must not appear in production builds if the project has build flavor/config support. If production hiding is too expensive, guard it with a dev config flag.

## 7. Firebase/Order Status Synchronization Rules

Use the existing project status naming where possible. If constants/enums exist, reuse them.

Required semantic mapping:

| Meaning | Recommended code value |
|---|---|
| Waiting for MoMo payment | `PENDING_PAYMENT` |
| Payment paid | `PAID` |
| Waiting for admin approval | `PENDING_APPROVAL` |
| Admin confirmed | `CONFIRMED` |
| Payment failed | `FAILED` |

If the existing app uses Vietnamese strings such as `chờ duyệt` or `đã xác nhận`, do not introduce inconsistent duplicates. Map to the existing values carefully.

## 8. Testing Checklist

### Backend tests/manual checks

- [ ] Backend starts successfully with dev profile.
- [ ] `POST /api/payments/momo` returns `payUrl` in mock mode.
- [ ] `POST /api/payments/momo` returns MoMo sandbox `payUrl` when sandbox credentials are configured.
- [ ] Payment transaction is saved as `PENDING` after create payment.
- [ ] `POST /api/payments/momo/ipn` verifies signature before updating status.
- [ ] Successful IPN sets order to `paymentStatus = PAID` and `orderStatus = PENDING_APPROVAL`.
- [ ] Successful IPN does not set order to `CONFIRMED`.
- [ ] Failed IPN sets payment failure state and does not expose order to shipper.
- [ ] `GET /api/payments/{orderId}/status` returns current payment/order status.
- [ ] `POST /api/payments/momo/mock-success/{orderId}` works only in dev/mock mode.

### Android tests/manual checks

- [ ] COD order still works and goes to `PENDING_APPROVAL`.
- [ ] MoMo order starts as `PENDING_PAYMENT`.
- [ ] Android calls backend MoMo create payment endpoint, not Firebase mock payment creation.
- [ ] Android receives and opens `payUrl`.
- [ ] Returning from browser does not automatically mark payment successful.
- [ ] `I have completed payment` checks backend status.
- [ ] Paid MoMo order clears cart and appears in user's orders.
- [ ] Paid MoMo order appears for admin as waiting approval.
- [ ] Paid MoMo order does not appear for shipper before admin approval.
- [ ] After admin approval, order becomes `CONFIRMED`.
- [ ] Shipper sees only `CONFIRMED` orders.
- [ ] Debug mock success can complete the demo when sandbox/UAT has issues.

### Build commands

Run the appropriate commands for this repository. Common candidates:

```bash
./mvnw test
./mvnw spring-boot:run
./gradlew :app-customer:assembleDebug
```

If the repo layout differs, inspect available Gradle/Maven files and use the correct commands.

## 9. Constraints

- Do not remove COD payment.
- Do not remove existing Firebase order read/sync if the app depends on it.
- Do not mark MoMo-paid orders as `CONFIRMED` automatically.
- Do not require MoMo UAT app login for the main demo path.
- Keep changes scoped to payment/order flow.
- Reuse existing DTOs, Retrofit APIs, repositories, enums/constants, and naming patterns.
- Add logs with enough context: `orderId`, `requestId`, `paymentStatus`, `orderStatus`, but do not log secrets.
- Do not commit real MoMo secrets.

## 10. Suggested Codex Execution Plan

1. Inspect current backend payment/order code and Android checkout/payment repositories.
2. Identify where Android currently chooses `FirebaseOrderRepository` for MoMo.
3. Fix backend status transitions:
   - MoMo success -> `PAID` + `PENDING_APPROVAL`
   - Admin approval -> `CONFIRMED`
4. Ensure create payment returns `payUrl`.
5. Ensure Android opens `payUrl` through browser/WebView.
6. Add/check payment status polling/refresh from backend.
7. Add dev-only mock success fallback.
8. Run backend and Android builds/tests.
9. Document tested flow and any required environment values.

## 11. Short Prompt To Run Codex

```text
Read CODEX_MOMO_SANDBOX_PAYURL_IMPLEMENTATION.md and implement the MoMo payment flow exactly as specified.

Focus on replacing the current Android Firebase mock MoMo flow with the backend-created MoMo sandbox payUrl flow. Do not require MoMo UAT app login. Keep COD working. After successful MoMo payment, update paymentStatus to PAID and orderStatus to PENDING_APPROVAL / CHỜ DUYỆT, not CONFIRMED. CONFIRMED must only happen after admin approval so shipper sees only admin-approved orders.

Add or keep a dev-only mock-success fallback endpoint for demo. Build/test backend and app-customer after changes, then summarize changed files and manual test steps.
```
