Đúng hướng rồi. Bây giờ bạn **không nên test kiểu từng màn hình rời rạc nữa**, mà nên test theo mục tiêu lớn:

> **Tắt backend/SQLite cũ → app vẫn chạy được bằng Firebase → dữ liệu Customer/Admin/Shipper cùng nhìn một nguồn Firebase.**

Theo log bạn gửi, Antigravity đã có thao tác build, cài app, test order, cập nhật `/orders`, giảm/tăng `stock_quantity` trong `/products`, và sửa trạng thái order trực tiếp trên Firebase để test return flow. 
Vậy bây giờ nên test theo checklist dưới đây.

---

# 1. Test “Firebase là database chính” trước

Mục tiêu: chứng minh app **không còn cần Spring Boot/SQLite/PostgreSQL để chạy luồng chính**.

Làm theo thứ tự:

```text
1. Tắt Spring Boot backend.
2. Tắt PostgreSQL nếu đang chạy.
3. Không mở API server ở port 3000.
4. Xóa app khỏi emulator hoặc clear data app.
5. Cài lại app-customer.
6. Đăng nhập bằng Firebase Auth.
7. Test toàn bộ flow từ Product → Cart → Checkout → Order.
```

Lệnh clear app:

```bash
adb shell pm clear vn.vuavuive.customer
```

Sau đó mở lại app:

```bash
adb shell am start -n vn.vuavuive.customer/vn.vuavuive.customer.ui.auth.LoginActivity
```

Nếu **backend đã tắt mà app vẫn login, load product, add cart, checkout, xem order được**, nghĩa là Customer app đã thật sự chạy bằng Firebase.

---

# 2. Test dữ liệu Firebase đã migrate đủ chưa

Vào Firebase Console → Realtime Database, kiểm tra các node chính:

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

Checklist tối thiểu:

```text
[ ] /categories có 8 danh mục
[ ] /products có 92 sản phẩm
[ ] Mỗi product có category_id đúng UUID
[ ] Mỗi product có category_name hoặc map được sang /categories
[ ] Mỗi product có name, price, image_url, unit, stock_quantity
[ ] /users có user đăng nhập bằng Firebase UID
[ ] /carts/{firebaseUid} xuất hiện khi add cart
[ ] /orders/{orderId} xuất hiện khi checkout
[ ] /orders/{orderId}/items có snapshot sản phẩm
[ ] /orders/{orderId}/status_logs có lịch sử trạng thái
[ ] /recipes load được nếu app còn dùng Recipe
[ ] /reviews load được nếu Product Detail còn hiển thị review
```

Điểm quan trọng: **đừng chỉ nhìn có node là xong**. Phải test app đọc/ghi node đó được.

---

# 3. Test Customer App theo luồng end-to-end

Đây là luồng quan trọng nhất.

## A. Auth/User

Test khi backend đã tắt:

```text
[ ] Register user mới bằng SĐT + password
[ ] Firebase Authentication có user dạng 09...@vuavuive.local
[ ] /users/{firebaseUid} được tạo
[ ] role = CUSTOMER
[ ] Logout
[ ] Login lại bằng SĐT + password
[ ] Account hiển thị đúng tên/SĐT
[ ] Sai password báo lỗi
```

Nếu bước này còn cần backend thì chưa đạt.

---

## B. Product/Category

```text
[ ] Home hiện category từ Firebase
[ ] Product list hiện đủ 92 sản phẩm
[ ] Ảnh load được
[ ] Giá đúng
[ ] Search chạy
[ ] Filter "Tất cả" hiện đủ sản phẩm
[ ] Filter "Rau củ" hiện rau củ
[ ] Filter "Trái cây" hiện trái cây
[ ] Product detail mở được
```

Test chắc nhất: đổi tên 1 sản phẩm trên Firebase Console, refresh app, app phải hiện tên mới.

---

## C. Cart

```text
[ ] Add product vào cart
[ ] Firebase xuất hiện /carts/{firebaseUid}/items/{productId}
[ ] Tăng số lượng → Firebase quantity đổi
[ ] Giảm số lượng → Firebase quantity đổi
[ ] Xóa item → Firebase item bị xóa
[ ] Save for later → item chuyển sang saved_for_later
[ ] Logout/login lại → cart vẫn còn
```

Nếu mục tiêu là **chỉ Firebase, không Room**, thì sau này phải bỏ Room khỏi Cart. Còn hiện tại nếu đang dùng **Room cache + Firebase remote**, vẫn chấp nhận được, nhưng phải hiểu: Firebase là nguồn chính, Room chỉ là cache/offline.

---

## D. Checkout / Order COD

Test COD trước, chưa test VNPay/MoMo vội.

```text
[ ] Add 1 sản phẩm vào cart
[ ] Ghi lại stock_quantity hiện tại trên Firebase
[ ] Checkout bằng COD
[ ] /orders/{orderId} được tạo
[ ] status = PENDING
[ ] payment_method = COD
[ ] payment_status = UNPAID
[ ] items trong order có product_name, unit_price, quantity, image_url
[ ] /products/{productId}/stock_quantity bị trừ đúng
[ ] Cart được clear sau khi order tạo thành công
[ ] Order xuất hiện trong "Đơn hàng của tôi"
```

Ví dụ stock ban đầu là 50, mua 2 thì sau checkout phải còn 48.

---

## E. Cancel Order

Chỉ test với đơn `PENDING`.

```text
[ ] Bấm hủy đơn
[ ] status chuyển thành CANCELLED
[ ] stock_restored = true
[ ] stock_quantity được cộng trả lại đúng
[ ] Bấm hủy lần 2 không cộng stock lần nữa
[ ] status_logs có log CANCELLED
```

Đây là test rất quan trọng vì nếu không có `stock_restored`, app có thể cộng kho 2 lần.

---

## F. Return Request

Chỉ test với đơn `DELIVERED`.

```text
[ ] Chuyển order sang DELIVERED để test
[ ] Bấm Trả hàng
[ ] Nhập lý do
[ ] status chuyển thành RETURN_REQUESTED
[ ] Không cộng stock ngay
[ ] status_logs có log RETURN_REQUESTED
```

Return chỉ là yêu cầu trả hàng. Không nên cộng kho ngay, vì cần admin duyệt.

---

# 4. Test Admin App kết nối Firebase

Phần này rất quan trọng vì app-admin cũ có thể vẫn dùng **MockRepository**. Trước khi gỡ database cũ, phải kiểm tra admin đã thật sự đọc Firebase chưa.

Checklist Admin:

```text
[ ] app-admin có google-services.json thật
[ ] app-admin login bằng Firebase Auth hoặc có cơ chế role ADMIN từ /users
[ ] Admin dashboard đọc số liệu từ Firebase, không phải mock
[ ] Admin Product List đọc /products
[ ] Admin sửa tên/giá/stock sản phẩm → Firebase thay đổi
[ ] Customer app refresh thấy thay đổi đó
[ ] Admin Order List đọc /orders
[ ] Đơn vừa tạo từ Customer xuất hiện trong Admin
[ ] Admin cập nhật status PENDING → CONFIRMED
[ ] Customer app thấy order đổi status
[ ] Admin phân công shipper_id / shipper_name
```

Test chéo quan trọng nhất:

```text
Customer tạo order
→ Admin thấy order đó
→ Admin đổi status
→ Customer thấy status mới
```

Nếu làm được, nghĩa là Customer và Admin đã dùng chung Firebase database.

---

# 5. Shipper để sau, nhưng cần test tối thiểu sau này

Shipper có thể để sau, nhưng sau này phải test:

```text
[ ] Tài khoản shipper có role = SHIPPER
[ ] /shippers/{shipperUid} tồn tại
[ ] Admin gán order.shipper_id = shipperUid
[ ] Shipper app chỉ thấy đơn được gán cho mình
[ ] Shipper bấm Bắt đầu giao → status = SHIPPING
[ ] Customer/Admin thấy status SHIPPING
[ ] Shipper bấm Đã giao → status = DELIVERED
[ ] Customer/Admin thấy DELIVERED
```

Luồng test chéo:

```text
Admin gán đơn cho shipper
→ Shipper thấy đơn
→ Shipper cập nhật trạng thái
→ Admin và Customer thấy thay đổi
```

---

# 6. Test “không còn gọi backend cũ”

Đây là bước để biết có thể bắt đầu gỡ bớt database/backend cũ hay chưa.

Nhờ Antigravity kiểm tra code/log theo các dấu hiệu sau:

```text
[ ] Tắt Spring Boot, app vẫn chạy flow chính
[ ] Logcat không còn gọi http://localhost:3000
[ ] Logcat không còn gọi /api/products
[ ] Logcat không còn gọi /api/orders
[ ] Logcat không còn gọi /api/auth/login
[ ] ProductRepositoryFirebase đang được inject
[ ] CategoryRepositoryFirebase đang được inject
[ ] FirebaseUserRepository đang được inject
[ ] FirebaseCartRepository đang được inject
[ ] FirebaseOrderRepository đang được inject
```

Có thể grep trong code:

```bash
grep -R "localhost:3000\|/api/products\|/api/orders\|/api/auth\|Retrofit\|OrderRepository\|ProductRepository\|AuthRepository" app-customer shared app-admin
```

Không nhất thiết xóa ngay, nhưng phải biết file nào còn được dùng runtime.

---

# 7. Khi nào mới được gỡ database cũ?

Chỉ gỡ sau khi pass bảng này:

| Khu vực          | Điều kiện pass                               |
| ---------------- | -------------------------------------------- |
| Product/Category | Customer + Admin đều đọc Firebase            |
| Auth/User        | Login/register không cần backend             |
| Cart             | Add/update/delete cart ghi Firebase          |
| Order            | Checkout/cancel/return ghi Firebase          |
| Admin            | Admin thấy order/product thật từ Firebase    |
| Shipper          | Có thể để sau, nhưng phải biết chưa hoàn tất |
| Backend          | Tắt backend app vẫn chạy flow chính          |
| SQLite/Room      | Không còn dùng làm source chính              |

Gỡ theo thứ tự an toàn:

```text
1. Ngừng dùng Retrofit repositories trong runtime.
2. Ngừng gọi Spring Boot API trong ViewModel.
3. Giữ backend folder nhưng không chạy.
4. Sau demo/test ổn, archive app-backend.
5. Chỉ xóa Room nếu bạn thật sự không cần offline cache.
6. Cuối cùng mới remove dependencies/code cũ.
```

Mình không khuyên xóa Room ngay nếu Cart đang dùng offline-first. Nhưng để test “chỉ Firebase”, bạn có thể tạm thời yêu cầu:

```text
Cart không đọc dữ liệu từ Room khi app khởi động.
Cart phải sync trực tiếp từ /carts/{firebaseUid}.
Room chỉ ghi cache sau khi Firebase trả dữ liệu.
```

---

# 8. Bộ test cuối cùng nên chạy theo thứ tự này

Bạn cứ làm đúng thứ tự này:

```text
TEST 1 — Firebase Data Inventory
Kiểm tra /products, /categories, /users, /carts, /orders.

TEST 2 — Customer Firebase-only
Tắt backend → login → product → cart → checkout COD → order list → cancel → return.

TEST 3 — Admin Firebase connection
Admin đọc products/orders từ Firebase → sửa product/order → Customer thấy thay đổi.

TEST 4 — Cross-app sync
Customer tạo order → Admin thấy → Admin đổi status → Customer thấy.

TEST 5 — Shipper later
Admin assign shipper → Shipper thấy đơn → Shipper update status → Admin/Customer thấy.

TEST 6 — Old backend dependency check
Tắt backend, grep/logcat đảm bảo không còn API cũ trong flow chính.

TEST 7 — Cleanup
Chỉ gỡ từng phần sau khi pass.
```

---

# Tin nhắn bạn có thể gửi Antigravity

```text
Bây giờ tôi muốn chuyển sang giai đoạn Firebase-only verification trước khi cleanup code cũ.

Mục tiêu:
1. Xác nhận toàn bộ dữ liệu chính đã migrate sang Firebase.
2. Xác nhận app-customer và app-admin đều đọc/ghi trực tiếp Firebase.
3. app-shipper có thể để sau, nhưng cần giữ note rõ phần nào chưa test.
4. Tắt Spring Boot backend và PostgreSQL/SQLite cũ, app vẫn chạy được các flow chính.
5. Sau khi pass mới bắt đầu gỡ bớt Retrofit/API/backend/SQLite code cũ.

Hãy test theo thứ tự:

A. Customer Firebase-only:
- Tắt backend.
- Clear app data.
- Login/register bằng Firebase Auth.
- Load products/categories từ Firebase.
- Add/update/delete cart tại /carts/{firebaseUid}.
- Checkout COD tạo /orders/{orderId}.
- Stock trong /products bị trừ đúng.
- Cart clear sau checkout.
- My Orders đọc /orders theo user_id.
- Cancel order cập nhật CANCELLED và restore stock đúng.
- Return delivered order cập nhật RETURN_REQUESTED, không restore stock ngay.

B. Admin Firebase:
- Xác nhận app-admin không còn dùng MockRepository cho products/orders.
- Admin đọc /products và /orders từ Firebase.
- Admin sửa product, customer thấy thay đổi.
- Admin thấy order customer vừa tạo.
- Admin đổi status order, customer thấy status mới.

C. Backend dependency check:
- Backend vẫn tắt.
- Logcat không có request /api/auth, /api/products, /api/orders.
- Kiểm tra các ViewModel đang inject FirebaseRepository thay vì Repository cũ.

D. Cleanup chỉ thực hiện sau khi A+B+C pass.
Không commit giữa chừng. Không xóa code cũ trước khi test pass.
```

Chốt lại: **test Customer Firebase-only trước, rồi test Admin đọc cùng Firebase, rồi mới cleanup**. Shipper để sau được, nhưng phải ghi rõ là chưa hoàn tất để tránh tưởng app đã migrate 100%.
