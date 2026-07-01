# Admin App Test Flows

Tai lieu nay dung de test thu cong app `app-admin` theo cac man hinh va logic hien co trong codebase.

## Dieu kien chuan bi

- Backend dang chay va app Admin tro duoc API backend.
- Firebase/project config hop le, du lieu mau da co san product, order, user.
- Neu test tren Android emulator, da cau hinh network/ADB reverse theo cach du an dang dung.
- Nen test tung app mot luc neu may yeu RAM, sau do test lien thong voi Customer/Shipper.

## Tai khoan test

| Role | Email | Mat khau | Ky vong |
| --- | --- | --- | --- |
| Admin | `admin@vuavuive.vn` | `Admin@123` | Toan quyen quan tri |
| Staff | `staff@vuavuive.vn` | `Staff@123` | Van hanh don/san pham, bi chan mot so quyen cao |
| Audit | `audit@vuavuive.vn` | `Audit@123` | Chi xem, khong duoc sua/xuat du lieu |
| Customer blocked | `customer@gmail.com` | `Customer@123` | Khong duoc vao app Admin |

## A. Dang nhap va phan quyen

1. Mo app Admin.
2. Chon tung role trong spinner va bam dang nhap.
3. Voi Admin/Staff/Audit: app vao man hinh chinh, hien dashboard/bottom navigation.
4. Voi Customer blocked: app khong cho vao trang quan tri.
5. Bam logout.

Ket qua mong doi:

- Session dung role duoc luu sau khi login thanh cong.
- User khong phai backoffice bi tu choi.
- Logout xoa session va quay lai Login.

## B. Dashboard va navigation

1. Dang nhap bang Admin.
2. Lan luot chuyen cac tab bottom navigation: Dashboard, Don hang, San pham, Voucher, Chat.
3. Keo refresh neu man hinh co swipe refresh.
4. Xoay man hinh hoac quay lai app tu background.

Ket qua mong doi:

- Khong crash, moi tab load du lieu dung.
- Session khong bi mat khi quay lai app.

## C. Quan ly san pham

### C1. Danh sach, tim kiem, loc

1. Vao tab San pham.
2. Tim theo ten san pham.
3. Doi category: all, veg, fruit, meat, drink, dry, spice, household, sweet, frozen, other.
4. Bat loc low stock.
5. Swipe refresh danh sach.

Ket qua mong doi:

- Danh sach loc dung theo ten/category.
- Low stock chi hien san pham active co ton kho thap (`stock <= 10`).

### C2. Them san pham moi

1. Bam FAB them san pham.
2. Thu bam Save khi thieu ten/gia/unit/category.
3. Nhap day du: ten, gia, original price, stock, unit, description, tags, category, active.
4. Chon/upload anh neu can.
5. Save va quay lai danh sach.

Ket qua mong doi:

- Form chan cac gia tri khong hop le: ten rong, gia <= 0, original price < price, stock < 0, unit rong, category sai.
- San pham moi xuat hien trong danh sach sau khi luu.

### C3. Sua/xoa san pham va read-only

1. Mo chi tiet mot san pham bang Admin.
2. Doi gia hoac stock, save.
3. Mo app Customer va kiem tra chi tiet san pham da cap nhat.
4. Quay lai Admin, long press mot san pham de xoa.
5. Dang nhap Audit, thu them/sua/xoa san pham.

Ket qua mong doi:

- Admin sua/xoa duoc.
- Customer nhin thay du lieu moi sau khi refresh.
- Audit chi xem, cac action sua/xoa bi chan.

## D. Quan ly don hang

### D1. Danh sach don hang

1. Vao tab Don hang.
2. Chuyen cac tab status: Tat ca, Cho duyet, Da xac nhan, Dang giao, Da giao, Da huy, Tra hang.
3. Tim theo ma don, ten nguoi nhan, so dien thoai.
4. Thu export CSV bang Admin/Staff.
5. Dang nhap Audit va thu export CSV.

Ket qua mong doi:

- Loc status va search dung.
- CSV duoc luu vao Downloads voi Admin/Staff.
- Audit bi chan export.

### D2. Chi tiet va cap nhat trang thai

1. Mo mot don trong danh sach.
2. Kiem tra thong tin khach hang, dia chi, item, tong tien, payment.
3. Doi trang thai theo luong: `pending` -> `confirmed` -> `in_transit` -> `delivered`.
4. Voi don COD chua thanh toan, bam Mark paid neu nut hien.
5. Quay ve danh sach va refresh.

Ket qua mong doi:

- Trang thai luu thanh cong va danh sach cap nhat.
- Mark paid chi hien khi don khong phai MoMo va chua paid.
- Audit khong duoc doi status/mark paid.

### D3. Bulk update

1. Long press/chon nhieu don trong danh sach.
2. Bam bulk status.
3. Chon confirmed, shipping/in_transit, delivered hoac cancelled.
4. Refresh danh sach.

Ket qua mong doi:

- Cac don da chon doi status.
- Bulk mode tat sau khi update.
- Audit bi chan bulk update.

### D4. Tra hang

1. Tao hoac tim don co status `return_requested`.
2. Mo chi tiet don.
3. Thu approve return.
4. Tao lai case khac va thu reject return.

Ket qua mong doi:

- Approve dua don sang `returned`.
- Reject dua don ve `delivered`.
- Audit khong duoc approve/reject.

## E. Voucher

1. Vao tab Voucher.
2. Dang nhap Admin, bam FAB them voucher.
3. Nhap thong tin voucher va save.
4. Mo lai voucher de sua.
5. Dang nhap Staff/Audit, mo danh sach voucher va thu them/sua.

Ket qua mong doi:

- Admin them/sua duoc voucher.
- Staff/Audit chi xem, FAB bi an hoac action sua bi xem nhu read-only.

## F. Quan ly user

1. Mo man hinh User Management neu co entry trong app.
2. Tim theo ten/email/phone.
3. Loc theo Customer, Shipper, Nhan vien.
4. Admin doi active/inactive cua mot user.
5. Admin mo detail user va doi role: admin, staff, audit, shipper, user.
6. Dang nhap Staff va thu vao User Management.
7. Dang nhap Audit va thu doi active/export CSV.

Ket qua mong doi:

- Staff bi chan khoi User Management.
- Admin doi active va role duoc.
- Audit chi xem, khong doi active va khong export CSV.

## G. Shipment va Chat

1. Mo danh sach shipment neu co entry trong app.
2. Mo chi tiet shipment.
3. Doi status: pending, processing, shipping, delivered, failed.
4. Thu save khi chua nhap note.
5. Dang nhap Audit va thu update shipment.
6. Mo Chat, gui mot tin nhan ho tro noi bo/khach hang neu backend san sang.

Ket qua mong doi:

- Shipment yeu cau note truoc khi luu status.
- Audit khong duoc update shipment.
- Chat khong crash, tin nhan gui/nhan dung neu dich vu chat dang chay.

