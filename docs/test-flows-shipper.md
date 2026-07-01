# Shipper App Test Flows

Tai lieu nay dung de test thu cong app `app-shipper` theo cac man hinh va logic hien co trong codebase.

## Dieu kien chuan bi

- Backend/Firebase dang chay va app Shipper tro duoc du lieu don hang.
- User shipper da co role `shipper`.
- Co it nhat mot don da duoc Admin xac nhan/giao cho shipper.
- Emulator thuc te can internet de test Call/Google Maps fallback.

## Tai khoan test

| Role | Email | Mat khau | Ky vong |
| --- | --- | --- | --- |
| Shipper | `shipper@gmail.com` | `Shipper@123` | Dang nhap duoc app Shipper |
| Customer | `customer@gmail.com` | `Customer@123` | Bi tu choi do sai role |

## A. Dang nhap va session

1. Mo app Shipper.
2. Bam login khi identifier/password rong.
3. Nhap `shipper@gmail.com` / `Shipper@123` va dang nhap.
4. Kill app roi mo lai.
5. Logout tu header/Profile.
6. Thu login bang Customer.

Ket qua mong doi:

- Form chan truong rong.
- Shipper vao ShipperMainActivity.
- Session con hop le sau khi mo lai app.
- Logout xoa Firebase/session va quay ve Login.
- Tai khoan khong phai shipper bi tu choi.

## B. Man hinh chinh, online status va tab

1. Sau login, kiem tra ten shipper tren header.
2. Bat/tat online switch.
3. Chuyen cac tab: Can giao, Lich su, Thong ke, Ca nhan.
4. Quay app ve background roi mo lai.

Ket qua mong doi:

- Online switch ghi len `/users/{uid}/onlineStatus`.
- Label online/offline doi dung theo switch.
- Cac tab load du lieu khong crash.

## C. Tab Can giao

1. Mo tab Can giao.
2. Swipe refresh.
3. Tim theo ma don.
4. Tim theo ten nguoi nhan, so dien thoai, dia chi.
5. Chuyen chip filter:
   - All
   - Pending: `CONFIRMED`, `PREPARING`, `READY_FOR_PICKUP`
   - Shipping: `IN_TRANSIT`, `SHIPPING`
6. Mo chi tiet mot don.

Ket qua mong doi:

- Tab Can giao chi hien don active: `CONFIRMED`, `PREPARING`, `READY_FOR_PICKUP`, `SHIPPING`, `IN_TRANSIT`.
- Search va chip filter loc dung.
- Empty state hien khi khong co don.

## D. Chi tiet don va tac vu nhanh

1. Mo chi tiet don tu tab Can giao.
2. Kiem tra ma don, status, recipient, phone, address, note, danh sach item, tong tien.
3. Bam Call.
4. Bam Navigate.
5. Kiem tra payment section:
   - MoMo paid: khong thu tien mat.
   - MoMo pending: hien can xac nhan/thanh toan chua xong.
   - COD: hien so tien can thu.

Ket qua mong doi:

- Call mo dialer voi so dien thoai nguoi nhan.
- Navigate mo Google Maps navigation, neu loi thi fallback sang geo URI.
- Payment instruction dung theo method/status.

## E. Cap nhat trang thai giao hang

### E1. Bat dau giao

1. Mo don status `CONFIRMED`, `PREPARING`, `READY_FOR_PICKUP` hoac `SHIPPING`.
2. Bam bat dau giao.
3. Xac nhan dialog.
4. Quay lai danh sach Can giao.

Ket qua mong doi:

- Don chuyen sang `IN_TRANSIT`.
- Nut action doi sang giao thanh cong/giao that bai.

### E2. Giao thanh cong

1. Mo don `IN_TRANSIT`.
2. Bam giao thanh cong.
3. Xac nhan dialog.
4. Mo tab Lich su va filter Success.

Ket qua mong doi:

- Don chuyen sang `DELIVERED`.
- Don bien mat khoi Can giao va xuat hien trong Lich su/Success.
- Thong ke so don thanh cong/tong doanh thu cap nhat.

### E3. Giao that bai

1. Mo don `IN_TRANSIT`.
2. Bam giao that bai.
3. Chon mot ly do co san:
   - Khach khong nghe may.
   - Sai dia chi/khong tim thay.
   - Khach tu choi nhan.
   - Khach hen giao lai.
4. Lap lai voi lua chon Ly do khac va nhap ly do custom.
5. Mo tab Lich su va filter Failed.

Ket qua mong doi:

- Don chuyen sang `FAILED`.
- Ly do that bai duoc gui kem update.
- Don xuat hien trong Lich su/Failed.

## F. Tab Lich su

1. Mo tab Lich su.
2. Swipe refresh.
3. Search ma don/ten/phone/dia chi.
4. Chuyen chip:
   - All
   - Success: `DELIVERED`
   - Failed: `FAILED`, `RETURNED`
5. Mo chi tiet don delivered/failed.

Ket qua mong doi:

- Tab Lich su chi hien don terminal: `DELIVERED`, `FAILED`, `RETURNED`.
- Don terminal khong con hien action giao hang.

## G. Thong ke

1. Mo tab Thong ke.
2. Kiem tra tong doanh thu, tong don hoan thanh, COD amount, online amount, success count, failed count.
3. Tao them mot don delivered COD va refresh.
4. Tao them mot don delivered MoMo paid va refresh.
5. Tao them mot don failed/returned va refresh.

Ket qua mong doi:

- Doanh thu chi tinh don `DELIVERED`.
- COD amount tinh don delivered khong phai MoMo paid.
- Online amount tinh don delivered MoMo paid.
- Failed count tinh `FAILED` va `RETURNED`.

## H. Ca nhan

1. Mo tab Ca nhan.
2. Kiem tra name/email/phone.
3. Kiem tra ty le giao thanh cong neu co lich su don.
4. Bam logout.

Ket qua mong doi:

- Thong tin profile lay dung tu session/user.
- Ty le thanh cong = delivered / (delivered + failed + returned).
- Logout quay ve ShipperLoginActivity.

## I. Test lien thong voi Customer/Admin

1. Customer tao don COD.
2. Admin xac nhan don sang `confirmed` va gan/cho shipper nhan don.
3. Shipper refresh Can giao.
4. Shipper bat dau giao sang `IN_TRANSIT`.
5. Customer refresh Orders, kiem tra don dang giao.
6. Shipper giao thanh cong hoac that bai.
7. Admin va Customer refresh lai don.

Ket qua mong doi:

- Trang thai don dong bo tren ca 3 app.
- Delivered cho phep Customer review/return.
- Failed/Returned nam trong lich su shipper va tab Da huy/tra hang tuy app.

