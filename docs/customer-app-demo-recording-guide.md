# Checklist quay demo full system Vua Vui Ve

Tai lieu nay dung de quay man hinh demo **3 app trong he thong Vua Vui Ve**:

- `app-customer`: khach hang mua san pham, dat hang, theo doi don.
- `app-admin`: quan tri don hang, san pham, voucher, user, shipment, bao cao.
- `app-shipper`: tai xe nhan don, giao hang, cap nhat trang thai.

Yeu cau quay: **khong can thuyet minh, khong can loi thoai, chi quay thao tac va man hinh app**.

Phan Customer auth ban da quay roi:

- Dang nhap.
- Dang ky.
- Quen mat khau.
- Doi mat khau.

Vi vay phan Customer trong file nay bat dau tu trang thai **da dang nhap thanh cong**.

---

## 1. Cach quay de video de xem

### 1.1. Nguyen tac chung

- Quay doc man hinh dien thoai/emulator.
- Moi man hinh dung lai 2-4 giay de thay ro du lieu.
- Khi bam nut quan trong, dung lai 1-2 giay truoc va sau khi bam.
- Khi co loading, doi den khi du lieu hien ra roi moi chuyen man hinh.
- Khong can bat mic.
- Khong can mo log/backend trong video chinh.
- Khong de lo token, file cau hinh, API key, console loi.
- Neu can quay 3 app, nen tach thanh 3 video hoac 4 chuong:
  - Customer.
  - Admin.
  - Shipper.
  - Lien thong 3 app.

### 1.2. Thu tu quay dep nhat

1. Quay Customer tu Home den dat don.
2. Quay Admin nhan don moi, xac nhan don, gan shipper.
3. Quay Shipper nhan don, bat dau giao, giao thanh cong/that bai.
4. Quay Customer refresh lai don hang de thay trang thai dong bo.
5. Quay Admin cac man hinh quan tri con lai.
6. Quay Shipper thong ke, lich su, profile.

### 1.3. Thoi luong goi y

Ban day du:

- Customer: 18-25 phut.
- Admin: 20-30 phut.
- Shipper: 10-15 phut.
- Lien thong 3 app: 8-12 phut.

Ban rut gon:

- Customer: 10-15 phut.
- Admin: 12-18 phut.
- Shipper: 7-10 phut.
- Lien thong 3 app: 5-8 phut.

---

## 2. Chuan bi truoc khi quay

### 2.1. Backend va app

Kiem tra truoc khi quay:

- Backend dang chay.
- App Customer, Admin, Shipper da cai ban moi nhat.
- Emulator/dien thoai co internet.
- Firebase/Realtime Database co du lieu mau.
- Cac app ket noi duoc backend/Firebase.
- Neu vua sua code, restart backend va cai lai app truoc khi quay.

### 2.2. Tai khoan quay

Customer:

- Email: `customer@gmail.com`
- Mat khau: `Customer@123`
- Trang thai: da dang nhap san neu khong muon quay lai auth.

Admin:

- Email: `admin@vuavuive.vn`
- Mat khau: `Admin@123`
- Quyen: toan quyen.

Staff:

- Email: `staff@vuavuive.vn`
- Mat khau: `Staff@123`
- Quyen: van hanh, bi chan mot so muc cao.

Audit:

- Email: `audit@vuavuive.vn`
- Mat khau: `Audit@123`
- Quyen: chi xem/read-only.

Shipper:

- Email: `shipper@gmail.com`
- Mat khau: `Shipper@123`
- Quyen: giao hang.

### 2.3. Du lieu can co san

San pham:

- It nhat 8-12 san pham active.
- It nhat 1 san pham ton kho thap, stock <= 10.
- It nhat 1 san pham het hang, stock = 0.
- San pham co anh dai dien.
- San pham co gia ban, gia goc, don vi, mo ta.
- Nhieu danh muc: rau cu, trai cay, thit/hai san, do kho, gia vi.

Voucher:

- It nhat 2 voucher hien thi tren Customer Home.
- It nhat 1 voucher giam gia don hang.
- It nhat 1 voucher free ship.

Don hang:

- It nhat 1 don `pending`.
- It nhat 1 don `confirmed`.
- It nhat 1 don `in_transit`.
- It nhat 1 don `delivered`.
- It nhat 1 don `cancelled` hoac `failed`.
- It nhat 1 don co yeu cau tra hang neu muon quay return flow.

Shipment:

- It nhat 1 shipment noi bo.
- It nhat 1 shipment external/GHN neu co.
- Shipment co tracking number, carrier, status, ETA, timeline.

Recipe:

- It nhat 2 cong thuc nau an.
- Moi cong thuc co anh, nguyen lieu, cac buoc nau.
- Nguyen lieu co lien ket voi san pham de them vao gio.

---

## 3. Flow quay Customer app

### 3.1. Bat dau Customer

Muc tieu can thay:

- App da vao man hinh chinh sau dang nhap.
- Bottom navigation co cac tab: Home, Products, Cart, Orders, Account.
- Ten/loi chao cua khach hang hien tren Home neu co.

Thao tac quay:

1. Mo app Customer.
2. Neu app dang o Login, dang nhap nhanh bang tai khoan customer.
3. Dung tai man hinh Home 3 giay.
4. Chuyen nhanh qua 5 tab bottom navigation de thay cau truc app.
5. Quay lai tab Home.

Khong can quay lai:

- Dang ky.
- Quen mat khau.
- Doi mat khau.

### 3.2. Home

Muc tieu can thay:

- Logo/thuong hieu Vua Vui Ve.
- Loi chao hoac ten khach hang.
- The thanh vien/diem tich luy/barcode neu co.
- Dia chi giao hang.
- Thanh tim kiem.
- Nut tim bang hinh anh.
- Nut chat/menu.
- Banner slider.
- Voucher.
- Cong thuc noi bat.
- Danh muc san pham.
- Flash Sale neu dang trong khung gio.
- Grid san pham noi bat.

Thao tac quay:

1. O tab Home, keo tu dau trang xuong cuoi trang.
2. Dung o khu vuc the thanh vien/diem.
3. Bam vao dia chi giao hang neu co nut edit, mo dialog/man hinh edit, sau do quay lai.
4. Bam vao mot voucher de copy ma neu app co toast copy.
5. Keo den khu vuc cong thuc noi bat.
6. Keo den khu vuc danh muc san pham.
7. Bam mot danh muc de mo danh sach san pham theo danh muc.
8. Quay lai Home.
9. Bam Flash Sale neu co.

Can quay ro:

- Home lay du lieu dong, khong phai man hinh tinh.
- Voucher co tuong tac.
- Danh muc chuyen duoc sang danh sach san pham.

### 3.3. Tim kiem bang chu

Muc tieu can thay:

- Man hinh Search.
- O nhap tu khoa.
- Lich su tim kiem neu co.
- Ket qua san pham.
- Empty state khi khong co ket qua neu can.

Thao tac quay:

1. Tu Home, bam thanh tim kiem.
2. Nhap tu khoa san pham, vi du `rau`, `tao`, `ca chua`.
3. Doi ket qua hien ra.
4. Bam mot san pham trong ket qua.
5. Quay lai man hinh Search.
6. Xoa tu khoa va nhap tu khoa khong ton tai de quay empty state neu muon.
7. Quay lai Home.

Can quay ro:

- Ket qua search co anh, ten, gia.
- Bam ket qua mo duoc Product Detail.

### 3.4. Tim kiem bang hinh anh

Muc tieu can thay:

- Nut camera/gallery.
- Chon anh tu gallery hoac chup anh.
- App nhan dien anh thanh tu khoa.
- Danh sach san pham goi y theo anh.

Thao tac quay:

1. Mo Search hoac Home.
2. Bam nut tim kiem bang hinh anh.
3. Chon Gallery.
4. Chon anh rau/cu/qua da chuan bi.
5. Doi app xu ly anh.
6. Quay ket qua nhan dien va danh sach san pham.
7. Bam mot san pham trong ket qua.

Neu API nhan dien anh chua san sang:

- Quay man hinh bao loi/khong nhan dien duoc.
- Quay app khong crash va co the quay lai tim bang chu.

### 3.5. Products tab

Muc tieu can thay:

- Danh sach san pham dang grid/list.
- Search trong tab Products.
- Filter danh muc.
- Add to cart nhanh.
- Keo load them/pagination neu co.

Thao tac quay:

1. Bam tab Products.
2. Dung 2 giay o dau danh sach.
3. Bam/chon mot danh muc.
4. Nhap tu khoa search.
5. Xoa search.
6. Keo xuong cuoi danh sach de thay load them.
7. Bam nut them gio hang nhanh tren mot san pham.
8. Mo mot san pham bat ky.

Can quay ro:

- San pham hien anh, ten, gia, don vi.
- Filter/search thay doi danh sach.
- Add to cart co phan hoi/toast/cap nhat gio.

### 3.6. Flash Sale

Muc tieu can thay:

- Man hinh Flash Sale.
- Countdown neu trong gio sale.
- Gia goc, gia giam, badge giam gia.
- Nut them gio hang.

Thao tac quay:

1. Tu Home hoac danh muc, mo Flash Sale.
2. Quay header/countdown.
3. Keo danh sach sale.
4. Bam mot san pham sale.
5. Quay Product Detail co gia sale neu co.
6. Quay lai Flash Sale.
7. Bam add to cart tren san pham sale.

Luu y:

- Flash Sale trong code thuong nam khung gio 06:00-08:00 va 14:00-16:00.
- Neu ngoai khung gio, quay man hinh het/khong co sale neu app hien dung.

### 3.7. Product Detail

Muc tieu can thay:

- Anh san pham/slider anh.
- Ten san pham.
- Gia ban, gia goc, giam gia.
- Rating/review count.
- So luong da ban neu co.
- Ton kho.
- Mo ta san pham.
- Nut tang/giam so luong.
- Nut Add to Cart.
- Nut Buy Now.
- Review.
- San pham tuong tu.

Thao tac quay:

1. Mo chi tiet mot san pham con hang.
2. Vuot anh san pham neu co nhieu anh.
3. Bam anh de xem full screen neu co.
4. Quay lai chi tiet.
5. Keo xuong xem mo ta.
6. Keo den review/rating.
7. Tang so luong.
8. Giam so luong.
9. Thu tang qua ton kho neu muon quay validation.
10. Bam Add to Cart.
11. Bam Buy Now voi san pham khac hoac cung san pham neu muon vao checkout nhanh.
12. Keo den san pham tuong tu va bam mot san pham.

Can quay ro:

- App chan so luong lon hon ton kho.
- San pham het hang khong cho mua neu co.
- Add to Cart thanh cong.

### 3.8. Gio hang

Muc tieu can thay:

- Danh sach san pham trong gio.
- Tang/giam so luong.
- Xoa san pham.
- Tong tien.
- Saved for later neu co.
- Empty cart state neu co.
- Nut checkout.

Thao tac quay:

1. Bam tab Cart.
2. Quay danh sach san pham da them.
3. Tang so luong mot san pham.
4. Giam so luong mot san pham.
5. Xoa mot san pham bang nut xoa hoac swipe trai neu app ho tro.
6. Neu co saved-for-later, chuyen mot san pham sang saved.
7. Bam checkout.

Can quay ro:

- Tong tien thay doi khi tang/giam.
- Gio hang cap nhat ngay sau khi xoa.

### 3.9. Checkout COD

Muc tieu can thay:

- Thong tin nguoi nhan.
- So dien thoai.
- Dia chi giao hang.
- Ghi chu.
- Danh sach san pham.
- Tam tinh, phi ship, giam gia, tong thanh toan.
- O voucher.
- Phuong thuc thanh toan COD.
- Tao don thanh cong.

Thao tac quay:

1. Tu Cart, bam checkout.
2. Kiem tra form thong tin giao hang.
3. Sua ten/so dien thoai/dia chi neu can.
4. Bam icon map/dia chi neu co Map Picker.
5. Nhap ghi chu giao hang.
6. Nhap voucher, vi du `VUAVUIVE`, `FREESHIP`, `FREESHIP24` neu co.
7. Quay phan tong tien thay doi sau voucher.
8. Chon COD.
9. Bam dat hang.
10. Quay PaymentResult/Order Success.
11. Bam xem don hang neu co.

Can quay ro:

- Validation form neu thieu thong tin.
- Voucher ap dung thanh cong hoac hien loi hop le.
- Don hang tao thanh cong va co ma don.

### 3.10. Checkout MoMo/ZaloPay

Muc tieu can thay:

- Chon phuong thuc thanh toan online.
- Mo webview/payment screen neu co.
- Ket qua thanh toan.
- PaymentResult hien success/pending/failed.

Thao tac quay:

1. Tao gio hang nho de test nhanh.
2. Vao Checkout.
3. Chon MoMo.
4. Bam dat hang/thanh toan.
5. Quay PaymentWebView neu mo ra.
6. Neu debug co mock success, bam mock success.
7. Quay PaymentResult.
8. Lap lai nhanh voi ZaloPay neu app co.

Can quay ro:

- COD va online payment la 2 flow khac nhau.
- Don online co payment status rieng.

### 3.11. Orders tab

Muc tieu can thay:

- Tab Orders.
- Cac tab trang thai: Tat ca, Cho duyet, Da xac nhan, Dang giao, Da giao, Da huy, Tra hang.
- Don moi tao xuat hien.
- Keo refresh danh sach.
- Mo chi tiet don.

Thao tac quay:

1. Bam tab Orders.
2. Quay don moi tao o danh sach.
3. Chuyen tung tab status.
4. Keo refresh.
5. Bam don moi tao de mo Order Detail.

Can quay ro:

- Don tu Checkout dong bo sang Orders.
- Filter status hoat dong.

### 3.12. Order Detail Customer

Muc tieu can thay:

- Ma don.
- Ngay tao.
- Trang thai don.
- Thong tin nguoi nhan.
- San pham trong don.
- Thanh toan.
- Tong tien.
- Timeline/log neu co.
- Nut huy don neu don con pending.
- Nut yeu cau tra hang neu don da giao.
- Nut danh gia neu don da giao.

Thao tac quay:

1. Mo mot don pending.
2. Quay day du thong tin don.
3. Bam huy don neu muon quay cancel flow.
4. Quay lai Orders de thay don chuyen sang huy.
5. Mo mot don delivered.
6. Bam danh gia san pham.
7. Gui rating/comment/hinh anh neu co.
8. Mo lai don delivered.
9. Bam yeu cau tra hang.
10. Nhap ly do tra hang.
11. Gui request.
12. Quay lai tab Tra hang.

Can quay ro:

- Action thay doi theo status don.
- Pending co huy.
- Delivered co review/return.

### 3.13. Reviews Customer

Muc tieu can thay:

- Man hinh danh sach review cua toi hoac don da giao can danh gia.
- Review bottom sheet/dialog.
- Rating sao.
- Noi dung danh gia.
- Gui danh gia thanh cong.

Thao tac quay:

1. Tu Account hoac Order Detail, mo My Reviews.
2. Chon don da giao.
3. Mo form danh gia.
4. Chon 5 sao.
5. Nhap noi dung ngan.
6. Gui danh gia.
7. Quay lai Product Detail de thay review neu can.

### 3.14. Recipes

Muc tieu can thay:

- Danh sach cong thuc.
- Tim kiem cong thuc neu co.
- Chi tiet cong thuc.
- Nguyen lieu.
- Cac buoc nau.
- Them 1 nguyen lieu vao gio.
- Them tat ca nguyen lieu vao gio.

Thao tac quay:

1. Tu Home hoac Account, mo Recipes.
2. Keo danh sach cong thuc.
3. Bam mot cong thuc.
4. Quay anh/title/thoi gian/khau phan neu co.
5. Keo phan nguyen lieu.
6. Bam them mot nguyen lieu vao gio.
7. Bam them tat ca nguyen lieu vao gio neu co.
8. Mo Cart de thay nguyen lieu da duoc them.
9. Quay lai Recipe Detail.
10. Keo phan cac buoc nau.

### 3.15. Shipment Customer

Muc tieu can thay:

- Danh sach van don.
- Tracking number.
- Carrier.
- Trang thai giao hang.
- ETA.
- Chi tiet shipment.
- Timeline trang thai.
- Link ve order.

Thao tac quay:

1. Tu Account, mo Shipments.
2. Quay danh sach shipment.
3. Bam mot shipment.
4. Quay tracking number, carrier, status, ETA.
5. Keo timeline trang thai.
6. Bam xem don hang neu co.

### 3.16. Account Customer

Muc tieu can thay:

- Thong tin tai khoan.
- Diem/thanh vien neu co.
- Edit Profile.
- My Orders.
- Shipments.
- My Reviews.
- Recipes.
- Chat support.
- Logout.

Thao tac quay:

1. Bam tab Account.
2. Quay header profile.
3. Mo Edit Profile.
4. Sua ten/phone/dia chi nho neu can.
5. Luu va quay lai Account.
6. Mo My Orders.
7. Quay lai Account.
8. Mo Shipments.
9. Quay lai Account.
10. Mo My Reviews.
11. Quay lai Account.
12. Mo Recipes.
13. Quay lai Account.
14. Mo Chat.

Khong can quay:

- Change Password vi da quay roi.

### 3.17. Chat Customer

Muc tieu can thay:

- Man hinh chat ho tro.
- Tin nhan chao.
- Suggestion chips.
- Gui tin nhan.
- Bot/assistant tra loi.

Thao tac quay:

1. Mo Chat tu Account hoac Home.
2. Bam mot suggestion chip.
3. Doi phan hoi.
4. Nhap cau hoi ngan, vi du `Don hang cua toi dang o dau?`.
5. Gui tin nhan.
6. Quay danh sach chat sau khi co phan hoi.

---

## 4. Flow quay Admin app

### 4.1. Dang nhap Admin va role gate

Muc tieu can thay:

- Man hinh login Admin.
- Dang nhap bang admin.
- Vao MainActivity.
- Role badge hien ADMIN/STAFF/AUDIT.
- Bottom navigation: Dashboard, Don hang, San pham, Voucher, Chat.
- Logout icon.

Thao tac quay:

1. Mo app Admin.
2. Dang nhap bang `admin@vuavuive.vn`.
3. Doi app vao Dashboard.
4. Quay role badge tren header.
5. Bam nhanh 5 tab bottom navigation.
6. Quay lai Dashboard.

Co the quay them phan phan quyen neu can:

1. Logout Admin.
2. Login Staff.
3. Vao Dashboard.
4. Thu bam Users/Audit shortcut de thay bi chan.
5. Logout Staff.
6. Login Audit.
7. Thu sua/xuat du lieu de thay read-only.

### 4.2. Dashboard Admin

Muc tieu can thay:

- Loi chao admin.
- Tong don.
- Doanh thu.
- Tong users.
- Don pending.
- Danh sach don cho xu ly.
- San pham sap het hang.
- Swipe refresh.
- Shortcut: Users, Shipments, Audit, Reports.
- Nut xem tat ca don.
- Nut xem tat ca san pham.

Thao tac quay:

1. O Dashboard, dung lai dau trang 3 giay.
2. Keo xem cac metric.
3. Quay danh sach pending orders.
4. Quay danh sach low stock products.
5. Keo refresh Dashboard.
6. Bam `Xem tat ca don` de sang tab Orders.
7. Quay lai Dashboard.
8. Bam `Xem tat ca san pham` de sang Products.
9. Quay lai Dashboard.
10. Bam shortcut Reports, chon mot loai report CSV.
11. Quay toast/save result neu co.

Can quay ro:

- Dashboard gom so lieu tong quan va shortcut dieu hanh.
- Du lieu don/san pham co lay dong.

### 4.3. Admin Orders list

Muc tieu can thay:

- Danh sach don.
- Search theo ma don/ten/so dien thoai.
- Tab status: Tat ca, Cho duyet, Da xac nhan, Dang giao, Da giao, Da huy, Tra hang.
- Export CSV.
- Bulk update.
- Real-time refresh khi don thay doi.

Thao tac quay:

1. Bam tab Don hang.
2. Quay danh sach Tat ca.
3. Chuyen qua tung tab status.
4. Nhap ma don hoac ten khach vao search.
5. Xoa search.
6. Bam export CSV.
7. Long press/chon nhieu don neu adapter ho tro multi-select.
8. Bam bulk status.
9. Chon xac nhan don hoac huy don.
10. Refresh danh sach.
11. Bam mot don pending de mo chi tiet.

Can quay ro:

- Search va filter thay doi danh sach.
- Export CSV co phan hoi.
- Bulk action chi hien khi chon don.

### 4.4. Admin Order Detail

Muc tieu can thay:

- Ma don.
- Ngay tao.
- Badge trang thai.
- Khach hang.
- Phone.
- Dia chi.
- Danh sach item co anh.
- Payment method/status/transaction id neu co.
- Tam tinh, ship, discount, tong.
- Xac nhan don.
- Huy don.
- Gan shipper.
- Mark paid cho COD delivered.
- Duyet/tu choi return request.
- Refund neu don returned + paid.

Thao tac quay voi don pending:

1. Mo don pending.
2. Quay header va thong tin khach.
3. Keo danh sach san pham.
4. Quay payment/breakdown.
5. Bam `XAC NHAN DON`.
6. Doi toast/trang thai cap nhat.
7. Neu man hinh hien gan shipper, chon shipper tu spinner.
8. Bam `GAN SHIPPER`.
9. Quay shipper hien tai sau khi gan.

Thao tac quay voi don khac:

1. Mo don pending khac.
2. Bam huy don de quay cancel.
3. Mo don delivered COD chua paid neu co.
4. Bam Mark Paid.
5. Mo don co return request.
6. Bam approve return.
7. Lap lai voi don return khac neu muon quay reject.
8. Mo don returned paid neu co.
9. Bam refund neu nut hien.

Can quay ro:

- Admin xu ly don tu pending sang confirmed.
- Admin gan shipper cho don confirmed.
- Return/refund chi hien khi dung dieu kien.

### 4.5. Admin Products list

Muc tieu can thay:

- Danh sach san pham.
- Search ten/sub-category.
- Spinner danh muc.
- Chip danh muc nhanh: All, Fruit, Veg, Dry.
- Low stock filter.
- Product count.
- Export CSV.
- Add product FAB.
- Mo edit product.
- Long press delete.

Thao tac quay:

1. Bam tab San pham.
2. Quay danh sach san pham.
3. Nhap search ten san pham.
4. Xoa search.
5. Doi spinner category.
6. Bam chip Fruit/Veg/Dry.
7. Bat Low Stock.
8. Tat Low Stock.
9. Bam export products CSV.
10. Bam FAB them san pham.
11. Quay lai danh sach.
12. Bam mot san pham de edit.
13. Quay lai danh sach.
14. Long press mot san pham test de hien dialog xoa.
15. Bam huy neu khong muon xoa that.

Can quay ro:

- Low stock chi hien san pham ton kho thap.
- Export co tao file/toast.
- Admin co quyen them/sua/xoa.

### 4.6. Admin Add/Edit Product

Muc tieu can thay:

- Form them/sua san pham.
- Anh preview.
- Chon anh tu Gallery.
- Nhap URL anh.
- Ten san pham.
- Gia ban.
- Gia goc.
- Ton kho.
- Don vi.
- Mo ta.
- Tags.
- Category spinner.
- Switch active.
- Validation.
- Luu san pham.

Thao tac quay them san pham:

1. Tu Products, bam FAB add.
2. Bam Save khi form rong de quay validation.
3. Nhap ten test, vi du `Rau muong demo`.
4. Nhap gia ban.
5. Nhap gia goc >= gia ban.
6. Nhap stock.
7. Nhap unit, vi du `bo`, `kg`, `hop`.
8. Chon category.
9. Nhap description.
10. Nhap tags.
11. Chon anh Gallery hoac nhap URL.
12. Bat/tat active.
13. Bam Save.
14. Quay lai danh sach va search san pham vua tao.

Thao tac quay sua san pham:

1. Mo san pham co san.
2. Doi stock hoac gia.
3. Bam Save.
4. Quay lai Products.
5. Mo Customer Product Detail de thay gia/stock moi neu muon quay lien thong.

### 4.7. Admin Vouchers

Muc tieu can thay:

- Danh sach voucher.
- Add voucher FAB voi admin.
- Form voucher.
- Type: ship, percent, fixed.
- Value.
- Cap voi percent.
- Min order.
- Max uses.
- Start date.
- Expire date.
- Note.
- Active switch.
- Validation.
- Read-only voi staff/audit.

Thao tac quay:

1. Bam tab Voucher.
2. Quay danh sach voucher.
3. Bam FAB them voucher.
4. Nhap code test, vi du `DEMO10`.
5. Chon type percent.
6. Nhap value `10`.
7. Nhap cap.
8. Nhap min order.
9. Nhap max uses.
10. Chon ngay bat dau/ngay het han.
11. Nhap note.
12. Bam Save.
13. Quay lai danh sach thay voucher moi.
14. Mo voucher vua tao.
15. Sua active/value nho.
16. Save.

Quay phan read-only neu can:

1. Logout Admin.
2. Login Staff hoac Audit.
3. Vao Voucher.
4. Mo voucher.
5. Quay form bi khoa hoac nut save thanh read-only.

### 4.8. Admin Chatbot

Muc tieu can thay:

- Tab Chat.
- Bot greeting.
- Quick chips: tong quan, don cho xu ly, don giao tre, san pham sap het hang, nguy co huy don.
- Gui cau hoi tu ban phim.
- Ket qua tra loi dua tren du lieu mock/repository.

Thao tac quay:

1. Bam tab Chat.
2. Dung lai o greeting.
3. Bam chip `Tong quan hom nay`.
4. Doi bot tra loi.
5. Bam chip `San pham sap het hang`.
6. Doi bot tra loi.
7. Nhap ma don neu co, vi du `ORD-9843A`.
8. Gui tin nhan.
9. Quay ket qua tra cuu.

Can quay ro:

- Chatbot admin tra loi theo ngu canh quan tri.
- Co quick replies sau moi cau tra loi.

### 4.9. Admin Users shortcut

Muc tieu can thay:

- Mo Users tu Dashboard shortcut.
- Search user.
- Tab role: Khach hang, Shipper, Nhan vien.
- Active/inactive switch.
- Dialog detail user.
- Change role.
- Export users CSV.
- Staff bi chan vao Users.
- Audit chi xem/khong export.

Thao tac quay:

1. O Dashboard, bam shortcut Users.
2. Quay danh sach users.
3. Search theo ten/email/phone.
4. Chuyen tab Khach hang.
5. Chuyen tab Shipper.
6. Chuyen tab Nhan vien.
7. Bam mot user de mo dialog detail.
8. Quay thong tin name/email/phone/address/provider/points/status.
9. Bam Change Role neu dang login Admin.
10. Chon role, sau do huy hoac doi tren user test.
11. Bat/tat active tren user test.
12. Bam export CSV.

Can quay ro:

- Quan ly user khong nam tren bottom nav, mo bang shortcut Dashboard.
- Phan quyen admin moi duoc doi role.

### 4.10. Admin Shipments shortcut

Muc tieu can thay:

- Mo Shipments tu Dashboard shortcut.
- Loc carrier: tat ca, noi bo, GHN.
- Loc status: pending, shipping, delivered, failed.
- Danh sach shipment.
- Chi tiet shipment.
- Tracking number.
- Carrier.
- Order id.
- ETA.
- Timeline.
- Cap nhat status voi note.

Thao tac quay:

1. O Dashboard, bam shortcut Shipments.
2. Quay danh sach shipment.
3. Doi carrier filter.
4. Doi status filter.
5. Bam mot shipment.
6. Quay tracking/carrier/order/ETA.
7. Keo timeline.
8. Chon status moi.
9. Bam Save khi note rong de quay validation.
10. Nhap note.
11. Bam Save.
12. Quay timeline cap nhat.

Can quay ro:

- Admin co man hinh quan ly shipment rieng.
- Cap nhat shipment yeu cau note.

### 4.11. Admin Audit log shortcut

Muc tieu can thay:

- Mo Audit log tu Dashboard shortcut.
- Danh sach lich su thao tac.
- Swipe refresh.
- Staff bi chan neu test role.

Thao tac quay:

1. O Dashboard, bam shortcut Audit.
2. Quay danh sach audit logs.
3. Keo refresh.
4. Neu vua export/sua user/sua shipment, quay log moi xuat hien neu co.

### 4.12. Admin Reports shortcut

Muc tieu can thay:

- Dialog chon loai bao cao CSV.
- Bao cao Orders.
- Bao cao Products.
- Bao cao Users.
- Toast luu file vao Downloads.
- Audit/staff restriction neu co.

Thao tac quay:

1. O Dashboard, bam shortcut Reports.
2. Chon bao cao Orders.
3. Quay toast thanh cong.
4. Bam lai Reports.
5. Chon Products.
6. Bam lai Reports.
7. Chon Users neu dang Admin.

### 4.13. Admin permission mini-flow

Muc tieu can thay:

- Admin co toan quyen.
- Staff bi chan Users/Audit hoac voucher edit.
- Audit read-only, khong duoc sua/export/cap nhat status.

Thao tac quay ngan:

1. Logout Admin.
2. Login Staff.
3. Bam shortcut Users.
4. Quay toast bi chan.
5. Bam shortcut Audit.
6. Quay toast bi chan.
7. Logout Staff.
8. Login Audit.
9. Vao Products.
10. Mo product edit.
11. Quay form bi disable/read-only.
12. Vao Orders.
13. Thu export CSV hoac bulk update.
14. Quay toast bi chan.
15. Logout Audit.
16. Login lai Admin neu can tiep tuc.

Neu thoi gian video ngan:

- Chi quay Admin role.
- Bo qua Staff/Audit.

---

## 5. Flow quay Shipper app

### 5.1. Dang nhap Shipper

Muc tieu can thay:

- Man hinh login Shipper.
- Dang nhap shipper thanh cong.
- Customer account bi tu choi neu test role.
- Vao ShipperMainActivity.

Thao tac quay:

1. Mo app Shipper.
2. Nhap `shipper@gmail.com`.
3. Nhap `Shipper@123`.
4. Bam dang nhap.
5. Dung o man hinh chinh.

Neu quay role gate:

1. Logout.
2. Thu login bang `customer@gmail.com`.
3. Quay app tu choi do sai role.
4. Login lai bang shipper.

### 5.2. Header va Online status

Muc tieu can thay:

- Ten shipper tren header.
- Switch Online/Offline.
- Label doi Online/Offline.
- Trang thai duoc cap nhat len Firebase.
- Nut logout.

Thao tac quay:

1. O man hinh Shipper chinh, quay header.
2. Bat switch Online.
3. Dung 1 giay.
4. Tat switch Offline.
5. Bat lai Online de tiep tuc quay giao hang.

### 5.3. Tab Can giao

Muc tieu can thay:

- Tab Can giao.
- Search don.
- Chip filter: Tat ca, Cho lay hang, Dang giao.
- Danh sach don active.
- Empty state neu khong co don.
- Mo chi tiet don.

Thao tac quay:

1. Bam tab Can giao.
2. Keo refresh.
3. Quay danh sach don.
4. Bam chip Cho lay hang.
5. Bam chip Dang giao.
6. Bam chip Tat ca.
7. Nhap ma don/ten/phone/dia chi vao search.
8. Xoa search.
9. Bam mot don confirmed.

Can quay ro:

- Tab Can giao chi hien don active: confirmed/in_transit va cac bien the dang chuan bi.
- Don da delivered/failed khong con nam o Can giao.

### 5.4. Shipper Order Detail

Muc tieu can thay:

- Ma don rut gon.
- Ngay tao.
- Badge status.
- Ten khach.
- So dien thoai.
- Dia chi.
- Ghi chu giao hang.
- Tong tien.
- Payment instruction.
- Danh sach san pham.
- Nut Call.
- Nut Navigate.
- Nut bat dau giao/giao thanh cong/giao that bai/hoan hang tuy status.

Thao tac quay:

1. Mo chi tiet don.
2. Quay header ma don/status.
3. Quay thong tin khach hang.
4. Quay so dien thoai va dia chi.
5. Bam Call de mo dialer.
6. Quay lai app.
7. Bam Navigate de mo Google Maps/browser.
8. Quay lai app.
9. Keo danh sach san pham.
10. Quay tong tien va payment instruction.

Can quay ro:

- COD hien so tien can thu.
- MoMo paid hien khong can thu tien mat.
- MoMo pending hien can xac nhan thanh toan.

### 5.5. Shipper bat dau giao

Muc tieu can thay:

- Don confirmed co nut bat dau giao.
- Dialog xac nhan.
- Trang thai chuyen sang IN_TRANSIT.
- Nut action doi thanh giao thanh cong/giao that bai.

Thao tac quay:

1. Mo don status confirmed.
2. Bam `Bat dau giao hang`.
3. Xac nhan dialog.
4. Doi toast cap nhat thanh cong.
5. Quay status moi.
6. Quay lai tab Can giao.
7. Bam chip Dang giao de thay don trong nhom dang giao.

### 5.6. Shipper giao thanh cong

Muc tieu can thay:

- Don IN_TRANSIT co nut giao thanh cong.
- Dialog xac nhan.
- Trang thai chuyen DELIVERED.
- Don bien mat khoi Can giao.
- Don xuat hien o Lich su/Thanh cong.
- Thong ke cap nhat.

Thao tac quay:

1. Mo don IN_TRANSIT.
2. Bam `Giao thanh cong`.
3. Xac nhan dialog.
4. Doi cap nhat thanh cong.
5. Quay status delivered.
6. Quay lai Can giao de thay don khong con trong active.
7. Bam tab Lich su.
8. Bam chip Thanh cong.
9. Quay don delivered.
10. Bam tab Thong ke de thay so don/doanh thu cap nhat.

### 5.7. Shipper giao that bai

Muc tieu can thay:

- Don IN_TRANSIT co nut giao that bai.
- Dialog chon ly do.
- Ly do co san.
- Ly do khac nhap tay.
- Trang thai chuyen FAILED.
- Don vao Lich su/That bai.

Thao tac quay voi don test khac:

1. Mo don IN_TRANSIT.
2. Bam `Giao that bai`.
3. Chon ly do co san, vi du khach khong nghe may.
4. Xac nhan.
5. Quay status FAILED.
6. Quay tab Lich su.
7. Bam chip That bai.
8. Mo don failed de thay label ket thuc va ly do.

Neu muon quay custom reason:

1. Dung mot don IN_TRANSIT khac.
2. Bam `Giao that bai`.
3. Chon `Ly do khac`.
4. Nhap ly do.
5. Gui.

### 5.8. Shipper hoan hang/return

Muc tieu can thay:

- Don FAILED co nut xac nhan hoan hang ve kho.
- Don delivered co return request approved co nut xac nhan nhan hang khach tra.
- Trang thai chuyen RETURNED.

Thao tac quay neu co du lieu:

1. Mo don FAILED.
2. Bam nut hoan hang ve kho.
3. Xac nhan dialog.
4. Quay status RETURNED.
5. Mo Lich su.
6. Bam filter That bai de thay returned/failed.

### 5.9. Tab Lich su Shipper

Muc tieu can thay:

- Tab Lich su.
- Search.
- Chip Tat ca, Thanh cong, That bai.
- Delivered, Failed, Returned.
- Mo chi tiet don lich su.
- Don terminal khong con nut giao active.

Thao tac quay:

1. Bam tab Lich su.
2. Quay danh sach.
3. Bam chip Thanh cong.
4. Bam chip That bai.
5. Search theo ma don/ten/phone/dia chi.
6. Mo mot don delivered.
7. Quay label ket thuc.
8. Quay lai Lich su.

### 5.10. Tab Thong ke Shipper

Muc tieu can thay:

- Tong doanh thu delivered.
- Tong don hoan thanh.
- COD amount.
- Online amount.
- Success count.
- Failed count.

Thao tac quay:

1. Bam tab Thong ke.
2. Dung lai dau trang 3 giay.
3. Quay tong doanh thu.
4. Quay so don hoan thanh.
5. Quay COD amount.
6. Quay Online amount.
7. Quay success/failed count.
8. Neu vua giao thanh cong/that bai, quay so lieu da thay doi.

### 5.11. Tab Ca nhan Shipper

Muc tieu can thay:

- Ten shipper.
- Email.
- Phone.
- Ty le giao thanh cong.
- Progress success rate.
- Logout.

Thao tac quay:

1. Bam tab Ca nhan.
2. Quay profile.
3. Quay ty le thanh cong.
4. Bam logout neu ket thuc video Shipper.

---

## 6. Flow quay lien thong 3 app

Day la phan quan trong nhat de thay he thong dong bo Customer - Admin - Shipper.

### 6.1. Customer tao don moi

Thao tac quay:

1. Mo Customer.
2. Them 1-2 san pham vao Cart.
3. Checkout COD.
4. Dat hang thanh cong.
5. Mo Orders.
6. Quay don moi status pending/cho duyet.
7. Ghi lai ma don tren man hinh neu can doi chieu.

Can thay:

- Don tao tu Customer xuat hien trong Orders Customer.

### 6.2. Admin nhan don moi

Thao tac quay:

1. Mo Admin.
2. Vao Orders.
3. Keo refresh neu can.
4. Search ma don vua tao.
5. Mo chi tiet don.
6. Quay thong tin customer/item/tong tien.
7. Bam xac nhan don.
8. Chon shipper.
9. Bam gan shipper.
10. Quay don thanh confirmed va co shipper.

Can thay:

- Don Customer tao dong bo sang Admin.
- Admin co quyen xac nhan va gan shipper.

### 6.3. Shipper nhan don

Thao tac quay:

1. Mo Shipper.
2. Bat Online.
3. Vao tab Can giao.
4. Refresh/tim ma don.
5. Mo don.
6. Quay thong tin giao hang.
7. Bam bat dau giao.
8. Quay don chuyen dang giao.

Can thay:

- Don Admin gan xuat hien trong app Shipper.
- Shipper cap nhat status duoc.

### 6.4. Customer thay don dang giao

Thao tac quay:

1. Quay lai Customer.
2. Vao Orders.
3. Refresh.
4. Mo don vua tao.
5. Quay status dang giao/in_transit.

Can thay:

- Trang thai Shipper update dong bo ve Customer.

### 6.5. Shipper giao thanh cong

Thao tac quay:

1. Quay lai Shipper.
2. Mo don dang giao.
3. Bam giao thanh cong.
4. Xac nhan.
5. Quay status delivered.
6. Mo Lich su/Thanh cong.

Can thay:

- Don chuyen delivered.
- Don ra khoi Can giao.

### 6.6. Customer thay don da giao va danh gia

Thao tac quay:

1. Quay lai Customer.
2. Vao Orders.
3. Refresh.
4. Mo don.
5. Quay status delivered.
6. Bam danh gia.
7. Gui review.

Can thay:

- Delivered cho phep Customer review.

### 6.7. Return flow lien thong neu can

Thao tac quay:

1. Customer mo don delivered.
2. Gui yeu cau tra hang.
3. Admin mo Orders tab Tra hang.
4. Admin mo don.
5. Admin approve return.
6. Shipper mo don neu co action hoan hang/nhan hang tra.
7. Shipper xac nhan returned.
8. Customer/Admin refresh lai don.

Can thay:

- Return request di tu Customer sang Admin.
- Admin duyet/tro tu.
- Trang thai returned dong bo.

---

## 7. Checklist man hinh bat buoc

### 7.1. Customer bat buoc co

- Home.
- Search chu.
- Search hinh anh.
- Products tab.
- Product Detail.
- Add to Cart.
- Cart.
- Checkout COD.
- Checkout online neu co.
- PaymentResult.
- Orders list.
- Order Detail.
- Cancel order.
- Review delivered order.
- Return request.
- Recipes.
- Shipments.
- Account.
- Edit Profile.
- Chat Customer.

Khong bat buoc quay lai:

- Customer login.
- Customer register.
- Customer forgot password.
- Customer change password.

### 7.2. Admin bat buoc co

- Admin Login.
- Dashboard.
- Metrics.
- Pending orders.
- Low stock products.
- Reports export.
- Orders list.
- Search/filter Orders.
- Order Detail.
- Confirm order.
- Assign shipper.
- Cancel order.
- Return approve/reject neu co du lieu.
- Products list.
- Search/filter/low stock Products.
- Add Product.
- Edit Product.
- Delete Product dialog.
- Vouchers list.
- Add/Edit Voucher.
- Chatbot Admin.
- Users shortcut.
- Shipments shortcut.
- Audit log shortcut.
- Role permission Staff/Audit neu co thoi gian.

### 7.3. Shipper bat buoc co

- Shipper Login.
- Header ten shipper.
- Online/Offline switch.
- Tab Can giao.
- Search/filter don.
- Order Detail.
- Call.
- Navigate.
- Payment instruction.
- Bat dau giao.
- Giao thanh cong.
- Giao that bai.
- Lich su.
- Thong ke.
- Ca nhan.
- Logout.

### 7.4. Lien thong bat buoc co

- Customer tao don.
- Admin thay don moi.
- Admin xac nhan don.
- Admin gan shipper.
- Shipper thay don.
- Shipper bat dau giao.
- Customer thay don dang giao.
- Shipper giao thanh cong.
- Customer thay don da giao.
- Customer danh gia.

---

## 8. Checklist du lieu test nhanh

### 8.1. San pham demo nen co

- `Rau muong demo`
- `Ca chua bi`
- `Tao Envy`
- `Thit ba roi`
- `Bi do`
- `Xa lach`
- `Gao huu co`
- `Nuoc ep trai cay`

### 8.2. Voucher demo nen co

- `VUAVUIVE`
- `FREESHIP`
- `FREESHIP24`
- `DEMO10`

### 8.3. Dia chi demo

- Ten nguoi nhan: `Nguyen Van Test`
- Phone: `0901234567`
- Dia chi: `123 Duong Demo, Quan 1, TP HCM`
- Ghi chu: `Giao trong gio hanh chinh`

### 8.4. Review demo

- Rating: 5 sao.
- Noi dung: `San pham tuoi, dong goi can than.`

### 8.5. Return demo

- Ly do: `San pham bi dap khi nhan hang`

### 8.6. Shipper fail reason demo

- Ly do co san: `Khach hang khong nghe may`
- Ly do nhap tay: `Khach hen giao lai ngay mai`

---

## 9. Cac loi khi quay va cach xu ly tren man hinh

### 9.1. Customer khong thay san pham

Xu ly:

1. Quay empty state neu app co.
2. Refresh Products.
3. Kiem tra backend/Firebase.
4. Doi sang san pham/danh muc co du lieu.

### 9.2. Customer khong ap dung duoc voucher

Xu ly:

1. Kiem tra ma voucher dung.
2. Kiem tra gia tri don hang dat dieu kien min order.
3. Quay validation message neu app hien ro.
4. Dung voucher khac.

### 9.3. Customer khong co don delivered de review

Xu ly:

1. Dung Admin/Shipper cap nhat mot don test sang delivered.
2. Refresh Orders Customer.
3. Mo don delivered de quay review.

### 9.4. Admin khong thay don moi

Xu ly:

1. Refresh Orders.
2. Search ma don.
3. Kiem tra Customer da dat hang thanh cong.
4. Kiem tra backend/Firebase.

### 9.5. Admin khong gan duoc shipper

Xu ly:

1. Kiem tra don da o status confirmed.
2. Kiem tra co user role shipper active.
3. Refresh Order Detail.

### 9.6. Shipper khong thay don

Xu ly:

1. Kiem tra Admin da gan shipper dung tai khoan.
2. Bat Online.
3. Refresh tab Can giao.
4. Kiem tra don dang confirmed/in_transit.
5. Doi sang search ma don.

### 9.7. Map/Call khong mo duoc

Xu ly:

1. Quay nut bi disable neu thieu phone/address.
2. Cai Google Maps hoac dung fallback browser.
3. Kiem tra emulator co app Dialer/Maps.

### 9.8. Thanh toan online khong thanh cong

Xu ly:

1. Quay PaymentWebView/mock result neu app dang debug.
2. Dung COD cho flow lien thong chinh.
3. Quay online payment thanh flow rieng.

---

## 10. Ban quay rut gon 25-35 phut

Neu chi du thoi gian quay mot video gon:

Customer:

1. Home.
2. Products.
3. Product Detail.
4. Cart.
5. Checkout COD.
6. Orders.
7. Review delivered order.
8. Account + Chat.

Admin:

1. Dashboard.
2. Orders.
3. Order Detail confirm + assign shipper.
4. Products add/edit.
5. Voucher.
6. Users/Shipments shortcut.
7. Chatbot.

Shipper:

1. Online switch.
2. Can giao.
3. Order Detail.
4. Bat dau giao.
5. Giao thanh cong.
6. Lich su.
7. Thong ke.
8. Profile.

Lien thong:

1. Customer dat don.
2. Admin xac nhan + gan shipper.
3. Shipper giao.
4. Customer thay delivered + review.

---

## 11. Ban quay day du 60-90 phut

Neu can show toan bo app day du:

1. Customer Home.
2. Customer Search chu.
3. Customer Search hinh anh.
4. Customer Products filter.
5. Customer Flash Sale.
6. Customer Product Detail.
7. Customer Cart.
8. Customer Checkout COD.
9. Customer Checkout MoMo/ZaloPay.
10. Customer Orders all status.
11. Customer Cancel.
12. Customer Review.
13. Customer Return.
14. Customer Recipes.
15. Customer Shipments.
16. Customer Account/Edit Profile.
17. Customer Chat.
18. Admin Login.
19. Admin Dashboard.
20. Admin Reports.
21. Admin Orders search/filter/export/bulk.
22. Admin Order Detail confirm/cancel/assign shipper/return/refund.
23. Admin Products search/filter/low stock/export.
24. Admin Add Product.
25. Admin Edit Product.
26. Admin Delete dialog.
27. Admin Vouchers add/edit.
28. Admin Chatbot.
29. Admin Users.
30. Admin Shipments.
31. Admin Audit Logs.
32. Admin Staff/Audit permission.
33. Shipper Login.
34. Shipper Online switch.
35. Shipper Can giao search/filter.
36. Shipper Order Detail call/navigate/payment/items.
37. Shipper Start Delivery.
38. Shipper Delivered.
39. Shipper Failed.
40. Shipper Returned.
41. Shipper History.
42. Shipper Stats.
43. Shipper Profile.
44. End-to-end Customer -> Admin -> Shipper -> Customer.

---

## 12. Checklist cuoi truoc khi nop video

- Video Customer khong lap lai auth da quay.
- Video co du 3 app Customer/Admin/Shipper.
- Co it nhat 1 flow dat don thanh cong.
- Co it nhat 1 flow Admin xac nhan don.
- Co it nhat 1 flow Admin gan shipper.
- Co it nhat 1 flow Shipper bat dau giao.
- Co it nhat 1 flow Shipper giao thanh cong.
- Co Customer refresh thay status thay doi.
- Co review san pham sau delivered.
- Co Admin quan ly san pham.
- Co Admin quan ly voucher.
- Co Admin users/shipments/audit/report.
- Co Shipper thong ke/profile.
- Khong lo thong tin nhay cam.
- Cac man hinh dung du lau de xem ro.
- Khong can giong noi, khong can caption dai trong video.
