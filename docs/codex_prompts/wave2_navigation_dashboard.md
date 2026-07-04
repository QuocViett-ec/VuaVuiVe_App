# 🤖 Codex Prompt – Wave 2: Dashboard & Navigation Code Review + Fix

> Copy toàn bộ phần trong block bên dưới → paste vào Codex để triển khai.

---

## PROMPT START

```
Bạn là Android developer senior. Nhiệm vụ: review code Navigation & Dashboard của dự án Vựa Vui Vẻ (3 app: Admin, Customer, Shipper), tìm và fix tất cả bug logic, đảm bảo build thành công.

Đây là kết quả Wave 1 (Auth) đã được fix xong. Wave 2 tập trung vào: Fragment transactions, tab navigation, lifecycle, session re-check, online status.

## Files BẮT BUỘC phải đọc và review

### App Admin
1. `app-admin/src/main/java/vn/vuavuive/admin/ui/main/MainActivity.java`
2. `app-admin/src/main/java/vn/vuavuive/admin/ui/dashboard/DashboardFragment.java`
3. `app-admin/src/main/res/menu/admin_nav_menu.xml`

### App Customer
4. `app-customer/src/main/java/vn/vuavuive/customer/ui/MainActivity.java`
5. `app-customer/src/main/java/vn/vuavuive/customer/ui/home/HomeFragment.java`
6. `app-customer/src/main/java/vn/vuavuive/customer/viewmodel/CartViewModel.java`
7. `app-customer/src/main/res/navigation/nav_graph.xml` (hoặc tên tương tự)

### App Shipper
8. `app-shipper/src/main/java/vn/vuavuive/shipper/ui/main/ShipperMainActivity.java`
9. `app-shipper/src/main/java/vn/vuavuive/shipper/ui/main/ShipperPagerAdapter.java`
10. `app-shipper/src/main/java/vn/vuavuive/shipper/data/repository/FirebaseShipperRepository.java`

## Thông tin code hiện tại (đã đọc trước)

### Admin MainActivity — đã biết:
- `replaceFragment()` tại dòng 79-84 dùng `.commit()` — THIẾU `commitAllowingStateLoss()`
- Bottom nav có 5 items: nav_dashboard, nav_orders, nav_products, nav_vouchers, nav_chatbot
- `binding.bottomNav.setSelectedItemId(R.id.nav_dashboard)` → load DashboardFragment mặc định
- `logout()` dùng `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK` — đúng
- KHÔNG có `onResume()` re-check session

### Admin DashboardFragment — đã biết:
- Quick-action buttons: `btnShortcutUsers`, `btnShortcutShipments`, `btnShortcutAudit`, `btnShortcutReports` — TẤT CẢ đã có setOnClickListener ✓
- Casting `(MainActivity) getActivity()` — KHÔNG có null-check bổ sung trước khi gọi `replaceFragment()`
- `loadDashboardData()` API calls — onFailure để trống `{}` — không có error feedback cho user
- `currentUser` null check ở đầu `onViewCreated()` — nhưng sau đó vẫn dùng `currentUser.getRole()` trong listeners mà không re-check

### Customer MainActivity — đã biết:
- Default tab = `navigation_home` (dòng 80: `bottomNavView.setSelectedItemId(R.id.navigation_home)`)
- NavController setup đầy đủ với 5 destinations
- `observeCartCount()` — có badge count từ CartViewModel ✓
- `navigateToProducts()` public method — được gọi từ CartFragment, OrderListFragment ✓
- `navController` null check đã có trong `navigateToDestination()` ✓

### Shipper ShipperMainActivity — cần kiểm tra:
- Online switch behavior
- Firebase listener cleanup ở onDestroy()

## Checklist review — Fix TẤT CẢ lỗi tìm được

### 2.1 Admin MainActivity
- [ ] `replaceFragment()` — đổi `.commit()` thành `.commitAllowingStateLoss()` để tránh crash khi activity đang paused/saving state
- [ ] Fragment transaction — đang dùng `.replace()` KHÔNG add to backstack — đúng cho bottom nav, không cần sửa
- [ ] Thêm `onResume()` re-check session: nếu `!sessionManager.isLoggedIn()` → gọi `logout()`
- [ ] `binding.tvRoleBadge.setText(role != null ? role.toUpperCase() : "BACKOFFICE")` — đúng, không sửa

### 2.2 Admin DashboardFragment
- [ ] `binding.btnShortcutUsers.setOnClickListener` tại dòng 164-175: khi `getActivity()` trả null (fragment detach) → NullPointerException. Thêm null check: `if (getActivity() instanceof MainActivity)` — ĐÃ CÓ ✓, nhưng `currentUser.getRole()` phía trên chưa null-check → thêm `if (currentUser == null) return;` trước khi dùng `currentUser.getRole()`
- [ ] `onFailure` trong `loadDashboardData()` để trống — nên log lỗi hoặc hiện Toast nhẹ "Không tải được dữ liệu mới nhất"
- [ ] `loadDashboardData()` gọi API khi fragment đang detach → kiểm tra `isAdded()` trước khi update UI trong callback
- [ ] `binding.swipeRefresh.setRefreshing(false)` tại dòng 133 — gọi TRƯỚC khi API callback về → nên chuyển vào trong callback (hoặc dùng điều kiện)

### 2.3 Customer MainActivity
- [ ] `navHostFragment == null` → `return` ở dòng 59 → nhưng `navController` vẫn null → các method sau sẽ crash. Thêm kiểm tra hoặc hiện error và finish
- [ ] `bottomNavView.setSelectedItemId(R.id.navigation_home)` sẽ trigger `setOnItemSelectedListener` → kiểm tra không gây double-navigation
- [ ] `handleNavigateIntent()` dùng `intent.removeExtra("navigate_to")` — đúng, tránh duplicate handle khi `onNewIntent`
- [ ] `observeCartCount()` — `count != null && count > 0` trước khi setBadge — đúng ✓

### 2.4 Shipper ShipperMainActivity
- [ ] Đọc toàn bộ file và kiểm tra:
  - Online switch (`Switch` hoặc `SwitchMaterial`) có setOnCheckedChangeListener? Error handling khi Firebase call thất bại?
  - `onDestroy()` → có set offline? Nếu không, shipper bị force-close sẽ hiện ghost online
  - Firebase RTDB ValueEventListener → có `removeEventListener()` ở `onDestroyView()` hoặc `onStop()`?
  - ViewPager2 + TabLayout đồng bộ: `TabLayoutMediator` hay listener thủ công?

### 2.5 Kiểm tra toàn cục cho Navigation
- [ ] Mọi chỗ cast `(MainActivity) getActivity()` trong các Fragment của Admin → đảm bảo wrap bằng `if (getActivity() instanceof MainActivity && getActivity() != null)`
- [ ] Mọi Fragment trong Customer app: `getActivity()`, `getContext()`, `requireContext()` — dùng đúng context, không gọi sau fragment detach
- [ ] Customer `HomeFragment`, `ProductListFragment`, `CartFragment`, `OrderListFragment`, `AccountFragment` — mỗi cái có `onResume()` reload data không? Nên có để refresh khi quay lại tab

## Quy tắc khi fix

1. **KHÔNG thay đổi business logic** — chỉ fix crash, lifecycle issue, null safety
2. **KHÔNG thêm dependency mới**
3. Giữ nguyên code style hiện tại
4. Comment tiếng Việt ngắn gọn cho từng fix
5. Giữ nguyên tất cả comment/docstring cũ

## Sau khi fix xong

1. Build: `./gradlew assembleDebug` (Windows: `gradlew.bat assembleDebug`)
2. Đảm bảo cả 3 module build thành công: app-admin, app-customer, app-shipper
3. Liệt kê tất cả thay đổi theo format:
   ```
   File: <tên file>
   Dòng: <số dòng>
   Vấn đề: <mô tả lỗi>
   Fix: <code sau khi sửa>
   ```

## Commit message (KHÔNG push — để user review)

```
git add -A
git commit -m "fix(codex): wave 2 - navigation lifecycle, fragment crash, null safety"
```
```

---

> ⚠️ **Lưu ý cho user sau khi Codex xong:**
> 1. Kiểm tra list thay đổi Codex báo cáo — đặc biệt chú ý fix cho `commitAllowingStateLoss()` và `onResume()` re-check session
> 2. Build lại: `./gradlew assembleDebug`
> 3. Nếu OK → push GitHub → chuyển sang **Stream 2 Wave 2** (test tab switching trên emulator)
>
> **Note:** Customer app mặc định mở tab **Home** (không phải Products như ghi trong test plan trước — đã đối chiếu với code thực tế dòng 80 Customer MainActivity).
> Cần cập nhật Stream 2 test checklist nếu cần.
