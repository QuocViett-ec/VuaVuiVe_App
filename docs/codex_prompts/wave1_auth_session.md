# 🤖 Codex Prompt – Wave 1: Auth & Session Code Review + Fix

> Copy toàn bộ nội dung bên dưới → paste vào Codex để triển khai.

---

## PROMPT START

```
Bạn là một Android developer senior. Nhiệm vụ của bạn là review toàn bộ code liên quan đến Auth & Session trong dự án Vựa Vui Vẻ (3 app Android: Admin, Customer, Shipper + shared module), tìm và fix tất cả bug logic, rồi đảm bảo build thành công.

## Cấu trúc dự án

Đây là multi-module Android project (Gradle):
- `shared/` — Module dùng chung (SessionManager, User DTO, API models)
- `app-admin/` — App dành cho Admin/Staff/Audit
- `app-customer/` — App dành cho khách hàng
- `app-shipper/` — App dành cho shipper giao hàng

## Files BẮT BUỘC phải đọc và review

1. `shared/src/main/java/vn/vuavuive/shared/util/SessionManager.java`
2. `shared/src/main/java/vn/vuavuive/shared/data/dto/User.java`
3. `app-admin/src/main/java/vn/vuavuive/admin/ui/auth/AdminLoginActivity.java`
4. `app-admin/src/main/java/vn/vuavuive/admin/ui/main/MainActivity.java`
5. `app-customer/src/main/java/vn/vuavuive/customer/ui/auth/LoginActivity.java`
6. `app-customer/src/main/java/vn/vuavuive/customer/viewmodel/AuthViewModel.java`
7. `app-customer/src/main/java/vn/vuavuive/customer/data/repository/AuthRepository.java`
8. `app-shipper/src/main/java/vn/vuavuive/shipper/ui/auth/ShipperLoginActivity.java`
9. `app-shipper/src/main/java/vn/vuavuive/shipper/ui/main/ShipperMainActivity.java`

Nếu có thêm file auth liên quan (RegisterActivity, ForgotPasswordActivity, ShipperAuthRepository...), hãy tìm và review luôn.

## Checklist review — Fix TẤT CẢ lỗi tìm được

### 1. SessionManager (shared)
- [ ] `saveSession()` — null check tất cả tham số trước khi put vào SharedPreferences
- [ ] `getUser()` — trả về null an toàn nếu không có data, KHÔNG crash
- [ ] `hasValidAccessToken()` — JWT decode phải có try-catch cho token malformed/expired
- [ ] `clearSession()` — phải xoá HẾT: access_token, refresh_token, user data, mọi key
- [ ] `isAdmin()`, `isBackoffice()`, `isShipper()` — dùng `equalsIgnoreCase()` cho role comparison
- [ ] Thread safety: dùng `apply()` thay `commit()` cho SharedPreferences write

### 2. AdminLoginActivity
- [ ] Spinner có đúng 4 options: Admin, Staff, Audit, Customer
- [ ] Chọn spinner → auto-fill email/password → validate không rỗng trước khi gọi API
- [ ] API callback: check `result != null && result.data != null` trước khi access nested fields
- [ ] Login thành công → `sessionManager.saveSession()` → navigate to MainActivity
- [ ] Login thất bại → hiện error Toast/message, KHÔNG navigate, KHÔNG crash
- [ ] Network error, server 500, invalid credentials 401/403 — tất cả phải có error handling
- [ ] Loading state: disable login button khi đang gọi API, re-enable khi callback

### 3. Admin MainActivity (route guard)
- [ ] `onCreate()` phải check `sessionManager.isBackoffice()` → nếu false → finish() + redirect login
- [ ] Không có code path nào bypass được isBackoffice() check
- [ ] Session null/expired → redirect về AdminLoginActivity

### 4. Customer LoginActivity
- [ ] Validate email format trước khi gọi API (Patterns.EMAIL_ADDRESS hoặc regex)
- [ ] Validate password không rỗng
- [ ] Login success → kiểm tra `user.isShipper()` → nếu true → block + Toast warning + KHÔNG navigate
- [ ] API response null check: `result.data`, `result.data.getUser()`, `result.data.getAccessToken()`
- [ ] Loading state: disable button khi loading, re-enable khi xong
- [ ] Error messages cụ thể cho từng loại lỗi

### 5. Shipper LoginActivity
- [ ] `onCreate()` → check `sessionManager.isLoggedIn()` → auto-navigate nếu token còn hạn
- [ ] Login response → check role: nếu KHÔNG phải shipper → `logout()` + Toast + quay về login
- [ ] `isLoggedIn()` nên check cả token expiry, không chỉ check token tồn tại
- [ ] Null safety cho tất cả API response fields

### 6. Shipper ShipperMainActivity
- [ ] `onCreate()` → check session/login → nếu chưa login → redirect ShipperLoginActivity
- [ ] Firebase online status: `repository.updateOnlineStatus()` error handling
- [ ] `onDestroy()` → có set offline không? (nên có để tránh ghost online)

### 7. Cross-module consistency
- [ ] Role string constants thống nhất giữa 3 app: "admin", "staff", "audit", "shipper", "user"/"customer"
- [ ] `User.isBackoffice()` trả true cho: admin, staff, audit — verify logic
- [ ] `User.isShipper()` chỉ trả true cho "shipper" — verify logic
- [ ] Tất cả role comparison dùng `equalsIgnoreCase()` — tránh case mismatch

### 8. Global patterns cần quét
- [ ] Mọi chỗ dùng `.getRole().toUpperCase()` hoặc `.getRole().toLowerCase()` → thêm null check cho getRole()
- [ ] Mọi chỗ dùng `.getName()`, `.getEmail()` → null check trước khi setText() hoặc compare
- [ ] Intent extras null check: `getIntent().getStringExtra()` có thể trả null
- [ ] `getActivity()` casting trong Fragment → null check tránh crash khi Fragment detached

## Quy tắc khi fix

1. **KHÔNG thay đổi business logic** — chỉ fix bug, thêm null check, sửa logic sai
2. **KHÔNG thêm dependency mới** — chỉ dùng những gì đã có trong project
3. **Giữ nguyên code style** hiện tại (indentation, naming convention)
4. **Comment tiếng Việt** cho các fix quan trọng để team hiểu
5. **Giữ nguyên tất cả comment/docstring hiện có** — không xoá

## Sau khi fix xong

1. Chạy: `./gradlew assembleDebug` (hoặc `gradlew.bat assembleDebug` trên Windows)
2. Đảm bảo build THÀNH CÔNG cho cả 3 module: app-admin, app-customer, app-shipper
3. Nếu build fail → fix lỗi compile → build lại
4. Liệt kê TẤT CẢ thay đổi đã thực hiện dưới dạng:
   - File: <tên file>
   - Dòng: <số dòng>
   - Trước: <code cũ>
   - Sau: <code mới>
   - Lý do: <giải thích>

## Commit message

Sau khi mọi thứ build OK:
```
git add -A
git commit -m "fix(codex): wave 1 - auth & session review, null safety, role guards"
```

KHÔNG push — để user review trước.
```

---

> ⚠️ **Lưu ý cho user:** Sau khi Codex chạy xong prompt này, hãy:
> 1. Kiểm tra danh sách thay đổi Codex báo cáo
> 2. Build thử: `./gradlew assembleDebug`
> 3. Nếu OK → push GitHub → chuyển sang Stream 2 (test thủ công Wave 1)
