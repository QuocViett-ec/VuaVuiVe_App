# Customer App Test Flows

Tai lieu nay dung de test thu cong app `app-customer` theo cac man hinh va logic hien co trong codebase.

## Dieu kien chuan bi

- Backend dang chay va app Customer tro duoc API backend.
- Firebase/project config hop le.
- Co san san pham active con hang de them gio.
- Nen co it nhat mot don delivered de test review/return.

## Tai khoan test

| Role | Email | Mat khau | Ky vong |
| --- | --- | --- | --- |
| Customer | `customer@gmail.com` | `Customer@123` | Dang nhap duoc app Customer |
| Shipper | `shipper@gmail.com` | `Shipper@123` | Bi tu choi, yeu cau dung app Shipper |

## A. Dang nhap, dang ky, quen mat khau

1. Mo app Customer.
2. Bam login voi email/password rong.
3. Nhap `customer@gmail.com` / `Customer@123` va dang nhap.
4. Logout tu tab Account.
5. Thu login bang tai khoan shipper.
6. Mo link Register va Forgot password.

Ket qua mong doi:

- Form chan email/password rong.
- Customer login vao MainActivity.
- Tai khoan shipper bi tu choi trong app Customer.
- Register/Forgot password mo dung man hinh, khong crash.

## B. Main navigation va Home

1. Sau login, kiem tra bottom navigation: Home, Products, Cart, Orders, Account.
2. Kiem tra app mo mac dinh vao tab Products.
3. Sang Home, kiem tra greeting, diem thanh vien, dia chi, banner, recipe, product, voucher.
4. Bam search header.
5. Bam category/product/recipe neu co.
6. Dua app ve background roi mo lai.

Ket qua mong doi:

- Tab chuyen dung, khong mat session.
- SearchActivity mo dung tu header.
- Banner/recipe/product/voucher load du lieu hoac hien empty state hop ly.

## C. Duyet san pham va chi tiet san pham

1. Vao Products.
2. Tim kiem san pham.
3. Loc theo category.
4. Mo chi tiet mot san pham active con hang.
5. Kiem tra anh, gia, original price/discount, rating, so luong da ban, stock, mo ta, review, san pham tuong tu.
6. Tang/giam quantity.
7. Tang qua stock.
8. Bam Add to cart.
9. Mo san pham het hang neu co.

Ket qua mong doi:

- Quantity khong nho hon 1 va khong vuot stock.
- San pham het hang disable Add to cart.
- San pham mock/test khong cho mua.
- Add to cart hien thong bao va badge gio hang tang.

## D. Gio hang

1. Vao Cart sau khi da them san pham.
2. Tang/giam so luong trong gio.
3. Swipe left mot item de xoa.
4. Kiem tra subtotal/total va so luong item.
5. Thu Saved for later neu UI item co nut tuong ung.
6. Expand/collapse Saved section.
7. Logout roi mo Cart.
8. Bam checkout khi chua login.

Ket qua mong doi:

- Tong tien cap nhat theo quantity.
- Xoa item cap nhat list va badge.
- Khi gio rong hien empty state va nut shop now.
- Checkout khi chua login dieu huong ve Login.

## E. Checkout COD

1. Dang nhap Customer.
2. Them it nhat mot san pham con hang vao gio.
3. Vao Checkout.
4. Bam Dat hang khi thieu ten/phone/dia chi.
5. Nhap day du ten nguoi nhan, so dien thoai, dia chi, ghi chu.
6. Ap ma voucher rong, sai, `VUAVUIVE`, `FREESHIP24` hoac `FREESHIP`.
7. Chon COD.
8. Bam Dat hang.
9. Vao Orders.

Ket qua mong doi:

- Form chan thong tin giao hang thieu.
- `VUAVUIVE` giam 15%; `FREESHIP24`/`FREESHIP` giam phi ship 30.000.
- Don COD tao thanh cong, gio hang duoc clear.
- Don moi xuat hien trong danh sach Orders.

## F. Checkout MoMo/ZaloPay

1. Them san pham vao gio va vao Checkout.
2. Chon MoMo.
3. Dat hang.
4. Kiem tra PaymentResultActivity mo cong thanh toan/deeplink.
5. Bam Kiem tra thanh toan.
6. Neu build debug co nut mock success, bam mock success.
7. Lap lai voi ZaloPay.

Ket qua mong doi:

- App tao don truoc, sau do tao payment URL/deeplink.
- Khi payment PAID, gio hang duoc clear va don chuyen sang trang thai cho admin duyet.
- Khi FAILED/CANCELLED/PENDING, man hinh hien dung trang thai va khong crash.

## G. Don hang

1. Vao Orders.
2. Chuyen cac tab: Tat ca, Cho xac nhan, Dang giao, Da giao, Da huy.
3. Swipe refresh.
4. Mo chi tiet don.
5. Voi don `pending`, `pending_payment`, `pending_approval` hoac `confirmed`, bam Huy don.
6. Voi don `delivered`, bam Yeu cau tra hang, thu gui ly do rong va ly do hop le.
7. Voi don `delivered`, bam Review va danh gia san pham.

Ket qua mong doi:

- Loc tab dung theo status.
- Chi tiet don hien recipient, address, payment, items, total.
- Cancel chi hien cho don con duoc huy.
- Return bat buoc co ly do va tao `return_requested`.
- Review chi hien khi don delivered va co san pham.

## H. Account va tien ich

1. Vao Account khi da login.
2. Kiem tra avatar/name/phone/email.
3. Mo Edit profile va luu thay doi.
4. Mo Change password va test mat khau sai/dung.
5. Mo My orders tu Account.
6. Mo Recipes, Shipments, My reviews, Chat support.
7. Logout.
8. Vao Account khi guest va bam cac menu can login.

Ket qua mong doi:

- Thong tin user hien dung.
- Cac menu can login dieu huong ve Login khi guest.
- Logout clear task va quay ve Login.
- Chat/Recipes/Shipments/My reviews mo dung man hinh va khong crash.

## I. Test lien thong voi Admin/Shipper

1. Customer tao don COD moi.
2. Admin login va xac nhan don sang `confirmed`.
3. Shipper login, kiem tra don xuat hien trong tab Can giao.
4. Shipper giao thanh cong sang `delivered`.
5. Customer refresh Orders.

Ket qua mong doi:

- Trang thai don dong bo qua 3 app.
- Customer thay `delivered` va co the review/return.

