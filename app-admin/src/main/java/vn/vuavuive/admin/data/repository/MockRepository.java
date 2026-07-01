package vn.vuavuive.admin.data.repository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import vn.vuavuive.shared.data.dto.DashboardStats;
import vn.vuavuive.shared.data.dto.Order;
import vn.vuavuive.shared.data.dto.OrderItem;
import vn.vuavuive.shared.data.dto.PaymentDetail;
import vn.vuavuive.shared.data.dto.Product;
import vn.vuavuive.shared.data.dto.Shipment;
import vn.vuavuive.shared.data.dto.User;
import vn.vuavuive.shared.data.dto.Voucher;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MockRepository {
    private static MockRepository instance;

    private List<Product> products = new ArrayList<>();
    private List<Order> orders = new ArrayList<>();
    private List<User> users = new ArrayList<>();
    private List<Voucher> vouchers = new ArrayList<>();
    private List<Shipment> shipments = new ArrayList<>();
    private List<AuditLog> auditLogs = new ArrayList<>();

    private User currentUser;

    public static synchronized MockRepository getInstance() {
        if (instance == null) {
            instance = new MockRepository();
        }
        return instance;
    }

    private MockRepository() {
        initMockData();
        startFirebaseSync();
    }

    private void startFirebaseSync() {
        try {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            
            // Listen to Users
            dbRef.child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<User> list = new ArrayList<>();
                    for (DataSnapshot s : snapshot.getChildren()) {
                        User u = new User();
                        u.setId(s.getKey());
                        u.setEmail(s.child("email").getValue(String.class));
                        u.setName(s.child("name").getValue(String.class));
                        u.setPhone(s.child("phone").getValue(String.class));
                        u.setAddress(s.child("address").getValue(String.class));
                        
                        String role = s.child("role").getValue(String.class);
                        u.setRole(role != null ? role.toUpperCase() : "CUSTOMER");
                        
                        Boolean active = s.child("isActive").getValue(Boolean.class);
                        if (active == null) active = s.child("is_active").getValue(Boolean.class);
                        u.setActive(active != null ? active : true);
                        
                        u.setCreatedAt(s.child("created_at").getValue(String.class));
                        u.setProvider(s.child("provider").getValue(String.class));
                        list.add(u);
                    }
                    if (!list.isEmpty()) {
                        users = list;
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });

            // Listen to Products
            dbRef.child("products").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Product> list = new ArrayList<>();
                    for (DataSnapshot s : snapshot.getChildren()) {
                        Product p = new Product();
                        p.setId(s.child("id").getValue(String.class) != null ? s.child("id").getValue(String.class) : s.getKey());
                        p.setName(s.child("name").getValue(String.class));
                        p.setSlug(s.child("slug").getValue(String.class));
                        
                        Double sellingPrice = s.child("selling_price").getValue(Double.class);
                        p.setPrice(sellingPrice != null ? sellingPrice : 0.0);
                        
                        p.setOriginalPrice(s.child("original_price").getValue(Double.class));
                        p.setCategory(s.child("category_id").getValue(String.class));
                        p.setSubCategory(s.child("sub_category").getValue(String.class));
                        p.setDescription(s.child("description").getValue(String.class));
                        
                        String imgUrl = s.child("image_url").getValue(String.class);
                        if (imgUrl == null) imgUrl = s.child("imageUrl").getValue(String.class);
                        p.setImageUrl(imgUrl);
                        
                        Integer stock = s.child("stock_quantity").getValue(Integer.class);
                        if (stock == null) stock = s.child("stock").getValue(Integer.class);
                        p.setStock(stock != null ? stock : 0);
                        
                        p.setUnit(s.child("unit").getValue(String.class));
                        
                        Boolean active = s.child("is_active").getValue(Boolean.class);
                        if (active == null) active = s.child("isActive").getValue(Boolean.class);
                        p.setActive(active != null ? active : true);
                        
                        p.setRating(s.child("rating").getValue(Double.class));
                        p.setReviewCount(s.child("review_count").getValue(Integer.class));
                        p.setSoldCount(s.child("sold_count").getValue(Integer.class));
                        p.setCreatedAt(s.child("created_at").getValue(String.class));
                        list.add(p);
                    }
                    if (!list.isEmpty()) {
                        products = list;
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });

            // Listen to Orders
            dbRef.child("orders").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Order> list = new ArrayList<>();
                    for (DataSnapshot s : snapshot.getChildren()) {
                        Order o = new Order();
                        o.setId(s.child("id").getValue(String.class) != null ? s.child("id").getValue(String.class) : s.getKey());
                        
                        String orderId = s.child("order_id").getValue(String.class);
                        if (orderId == null) orderId = s.child("orderId").getValue(String.class);
                        if (orderId == null) orderId = s.getKey();
                        o.setOrderId(orderId);
                        
                        String userId = s.child("user_id").getValue(String.class);
                        if (userId == null) userId = s.child("userId").getValue(String.class);
                        o.setUserId(userId);
                        
                        String status = s.child("status").getValue(String.class);
                        o.setStatus(status != null ? status.toUpperCase() : null);
                        
                        o.setNote(s.child("note").getValue(String.class));
                        
                        Double subtotal = s.child("subtotal_amount").getValue(Double.class);
                        if (subtotal == null) subtotal = s.child("subtotalAmount").getValue(Double.class);
                        o.setSubtotal(subtotal != null ? subtotal : 0.0);
                        
                        Double shipping = s.child("shipping_fee").getValue(Double.class);
                        if (shipping == null) shipping = s.child("shippingFee").getValue(Double.class);
                        o.setShippingFee(shipping != null ? shipping : 0.0);
                        
                        Double discount = s.child("discount_amount").getValue(Double.class);
                        if (discount == null) discount = s.child("discountAmount").getValue(Double.class);
                        o.setDiscount(discount != null ? discount : 0.0);
                        
                        Double finalAmount = s.child("final_amount").getValue(Double.class);
                        if (finalAmount == null) finalAmount = s.child("finalAmount").getValue(Double.class);
                        if (finalAmount == null) finalAmount = s.child("totalAmount").getValue(Double.class);
                        if (finalAmount == null) finalAmount = s.child("total_amount").getValue(Double.class);
                        o.setFinalAmount(finalAmount != null ? finalAmount : 0.0);
                        o.setTotalAmount(finalAmount != null ? finalAmount : 0.0);

                        Boolean restored = s.child("stock_restored").getValue(Boolean.class);
                        if (restored == null) restored = s.child("stockRestored").getValue(Boolean.class);
                        o.setStockRestored(restored != null ? restored : false);
                        
                        String createdAt = s.child("created_at").getValue(String.class);
                        if (createdAt == null) createdAt = s.child("createdAt").getValue(String.class);
                        o.setCreatedAt(createdAt);
                        
                        String updatedAt = s.child("updated_at").getValue(String.class);
                        if (updatedAt == null) updatedAt = s.child("updatedAt").getValue(String.class);
                        o.setUpdatedAt(updatedAt);
                        
                        String deliveredAt = s.child("delivered_at").getValue(String.class);
                        if (deliveredAt == null) deliveredAt = s.child("deliveredAt").getValue(String.class);
                        o.setDeliveredAt(deliveredAt);

                        // Delivery
                        vn.vuavuive.shared.data.dto.DeliveryInfo delivery = new vn.vuavuive.shared.data.dto.DeliveryInfo();
                        String delName = s.child("delivery_name").getValue(String.class);
                        if (delName == null) delName = s.child("deliveryName").getValue(String.class);
                        if (delName == null) delName = s.child("recipientName").getValue(String.class);
                        delivery.setName(delName);

                        String delPhone = s.child("delivery_phone").getValue(String.class);
                        if (delPhone == null) delPhone = s.child("deliveryPhone").getValue(String.class);
                        if (delPhone == null) delPhone = s.child("recipientPhone").getValue(String.class);
                        delivery.setPhone(delPhone);

                        String delAddr = s.child("delivery_address").getValue(String.class);
                        if (delAddr == null) delAddr = s.child("deliveryAddress").getValue(String.class);
                        if (delAddr == null) delAddr = s.child("recipientAddress").getValue(String.class);
                        delivery.setAddress(delAddr);
                        o.setDelivery(delivery);

                        // Payment Detail
                        vn.vuavuive.shared.data.dto.PaymentDetail payment = new vn.vuavuive.shared.data.dto.PaymentDetail();
                        String payMethod = s.child("payment_method").getValue(String.class);
                        if (payMethod == null) payMethod = s.child("paymentMethod").getValue(String.class);
                        payment.setMethod(payMethod);

                        String payStatus = s.child("payment_status").getValue(String.class);
                        if (payStatus == null) payStatus = s.child("paymentStatus").getValue(String.class);
                        if (payStatus == null) payStatus = s.child("payment").child("status").getValue(String.class);
                        payment.setStatus(payStatus);
                        payment.setAmount(o.getFinalAmount());
                        o.setPayment(payment);

                        // Items
                        List<vn.vuavuive.shared.data.dto.OrderItem> items = new ArrayList<>();
                        DataSnapshot itemsSnap = s.child("items");
                        if (itemsSnap.exists()) {
                            for (DataSnapshot itemSnap : itemsSnap.getChildren()) {
                                vn.vuavuive.shared.data.dto.OrderItem item = new vn.vuavuive.shared.data.dto.OrderItem();
                                String prodId = itemSnap.child("product_id").getValue(String.class);
                                if (prodId == null) prodId = itemSnap.child("productId").getValue(String.class);
                                item.setProductId(prodId);
                                
                                String prodName = itemSnap.child("product_name").getValue(String.class);
                                if (prodName == null) prodName = itemSnap.child("productName").getValue(String.class);
                                item.setName(prodName);
                                
                                String imgUrl = itemSnap.child("image_url").getValue(String.class);
                                if (imgUrl == null) imgUrl = itemSnap.child("imageUrl").getValue(String.class);
                                item.setImageUrl(imgUrl);
                                
                                item.setUnit(itemSnap.child("unit").getValue(String.class));
                                
                                Double price = itemSnap.child("unit_price").getValue(Double.class);
                                if (price == null) price = itemSnap.child("productPrice").getValue(Double.class);
                                item.setPrice(price != null ? price : 0.0);
                                
                                Integer qty = itemSnap.child("quantity").getValue(Integer.class);
                                item.setQuantity(qty != null ? qty : 1);
                                items.add(item);
                            }
                        }
                        o.setItems(items);

                        list.add(o);
                    }
                    if (!list.isEmpty()) {
                        orders = list;
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });

            // Listen to Vouchers
            dbRef.child("vouchers").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Voucher> list = new ArrayList<>();
                    for (DataSnapshot s : snapshot.getChildren()) {
                        Voucher v = new Voucher();
                        v.setId(s.child("id").getValue(String.class) != null ? s.child("id").getValue(String.class) : s.getKey());
                        v.setCode(s.child("code").getValue(String.class) != null ? s.child("code").getValue(String.class) : s.getKey());
                        v.setType(s.child("type").getValue(String.class));
                        
                        Double val = s.child("value").getValue(Double.class);
                        v.setValue(val != null ? val : 0.0);
                        
                        Double cap = s.child("cap").getValue(Double.class);
                        v.setCap(cap != null ? cap : 0.0);
                        
                        Double minOrder = s.child("minOrderValue").getValue(Double.class);
                        if (minOrder == null) minOrder = s.child("min_order_value").getValue(Double.class);
                        v.setMinOrderValue(minOrder != null ? minOrder : 0.0);
                        
                        Integer maxU = s.child("maxUses").getValue(Integer.class);
                        if (maxU == null) maxU = s.child("max_uses").getValue(Integer.class);
                        v.setMaxUses(maxU != null ? maxU : 0);
                        
                        v.setStartsAt(s.child("startsAt").getValue(String.class) != null ? s.child("startsAt").getValue(String.class) : s.child("starts_at").getValue(String.class));
                        v.setExpiresAt(s.child("expiresAt").getValue(String.class) != null ? s.child("expiresAt").getValue(String.class) : s.child("expires_at").getValue(String.class));
                        
                        Boolean act = s.child("isActive").getValue(Boolean.class);
                        if (act == null) act = s.child("is_active").getValue(Boolean.class);
                        v.setActive(act != null ? act : true);
                        
                        v.setNote(s.child("note").getValue(String.class));
                        list.add(v);
                    }
                    if (!list.isEmpty()) {
                        vouchers = list;
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });

            // Listen to Shipments
            dbRef.child("shipments").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Shipment> list = new ArrayList<>();
                    for (DataSnapshot s : snapshot.getChildren()) {
                        java.util.Map<String, Object> map = new java.util.HashMap<>();
                        for (DataSnapshot child : s.getChildren()) {
                            map.put(child.getKey(), child.getValue());
                        }
                        if (!map.containsKey("_id")) {
                            map.put("_id", s.getKey());
                        }
                        String json = new com.google.gson.Gson().toJson(map);
                        Shipment sh = createDtoFromJson(json, Shipment.class);
                        list.add(sh);
                    }
                    if (!list.isEmpty()) {
                        shipments = list;
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });

            // Listen to Audit Logs
            dbRef.child("audit_logs").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<AuditLog> list = new ArrayList<>();
                    for (DataSnapshot s : snapshot.getChildren()) {
                        AuditLog log = new AuditLog(
                            s.child("operatorName").getValue(String.class),
                            s.child("role").getValue(String.class),
                            s.child("action").getValue(String.class),
                            s.child("target").getValue(String.class),
                            s.child("details").getValue(String.class)
                        );
                        log.id = s.child("id").getValue(String.class) != null ? s.child("id").getValue(String.class) : s.getKey();
                        log.timestamp = s.child("timestamp").getValue(String.class);
                        list.add(log);
                    }
                    java.util.Collections.sort(list, (l1, l2) -> {
                        if (l1.timestamp == null || l2.timestamp == null) return 0;
                        return l2.timestamp.compareTo(l1.timestamp);
                    });
                    if (!list.isEmpty()) {
                        auditLogs = list;
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initMockData() {
        // 1. Initial Users
        User u1 = new User();
        u1.setId("usr-1");
        u1.setName("Lê Hoàng Admin");
        u1.setEmail("admin@vuavuive.vn");
        u1.setPhone("0901234567");
        u1.setRole("admin");
        u1.setActive(true);
        u1.setCreatedAt("2026-01-10T08:00:00Z");
        u1.setProvider("local");
        u1.setAddress("123 Đường 3/2, Quận 10, TP. HCM");

        User u2 = new User();
        u2.setId("usr-2");
        u2.setName("Trần Thị Nhân Viên");
        u2.setEmail("staff@vuavuive.vn");
        u2.setPhone("0912345678");
        u2.setRole("staff");
        u2.setActive(true);
        u2.setCreatedAt("2026-02-15T09:00:00Z");
        u2.setProvider("local");
        u2.setAddress("456 Lê Lợi, Quận 1, TP. HCM");

        User u3 = new User();
        u3.setId("usr-3");
        u3.setName("Nguyễn Văn Kiểm Toán");
        u3.setEmail("audit@vuavuive.vn");
        u3.setPhone("0923456789");
        u3.setRole("audit");
        u3.setActive(true);
        u3.setCreatedAt("2026-03-20T10:00:00Z");
        u3.setProvider("local");
        u3.setAddress("789 Nguyễn Huệ, Quận 1, TP. HCM");

        User u4 = new User();
        u4.setId("usr-4");
        u4.setName("Phạm Minh Huy");
        u4.setEmail("huy.pham@gmail.com");
        u4.setPhone("0934567890");
        u4.setRole("user");
        u4.setActive(true);
        u4.setCreatedAt("2026-04-01T14:30:00Z");
        u4.setProvider("google");
        u4.setAddress("12 Song Hành, TP. Thủ Đức, TP. HCM");

        User u5 = new User();
        u5.setId("usr-5");
        u5.setName("Đặng Minh Anh");
        u5.setEmail("minhanh.dang@gmail.com");
        u5.setPhone("0945678901");
        u5.setRole("user");
        u5.setActive(false); // Inactive user
        u5.setCreatedAt("2026-04-10T11:15:00Z");
        u5.setProvider("local");
        u5.setAddress("56 Nguyễn Chí Thanh, Quận 5, TP. HCM");

        users.addAll(Arrays.asList(u1, u2, u3, u4, u5));
        currentUser = u1; // Default logged in as admin

        // 2. Initial Products
        Product p1 = new Product();
        p1.setId("prod-1");
        p1.setName("Cà chua bi hữu cơ Đà Lạt");
        p1.setSlug("ca-chua-bi-huu-co");
        p1.setPrice(35000);
        p1.setOriginalPrice(42000.0);
        p1.setCategory("veg");
        p1.setSubCategory("Rau ăn quả");
        p1.setDescription("Cà chua bi hữu cơ thơm ngon ngọt mát gieo trồng tại trang trại Đà Lạt chuẩn VietGAP.");
        p1.setImageUrl("https://images.unsplash.com/photo-1595855759920-86582396756a?auto=format&fit=crop&w=300&q=80");
        p1.setStock(4); // Low stock!
        p1.setUnit("Hộp 500g");
        p1.setTags(Arrays.asList("organic", "dalat", "fresh"));
        p1.setActive(true);
        p1.setRating(4.8);
        p1.setReviewCount(12);
        p1.setSoldCount(150);
        p1.setCreatedAt("2026-04-01T08:00:00Z");

        Product p2 = new Product();
        p2.setId("prod-2");
        p2.setName("Thịt ba rọi heo thảo mộc");
        p2.setSlug("thit-ba-roi-heo-thao-moc");
        p2.setPrice(145000);
        p2.setOriginalPrice(160000.0);
        p2.setCategory("meat");
        p2.setSubCategory("Thịt heo");
        p2.setDescription("Thịt ba chỉ thơm ngon, mỡ nạc hài hòa từ heo nuôi dưỡng bằng thảo mộc tự nhiên.");
        p2.setImageUrl("https://images.unsplash.com/photo-1602470520998-f4a52199a3d6?auto=format&fit=crop&w=300&q=80");
        p2.setStock(8); // Low stock!
        p2.setUnit("Khay 500g");
        p2.setTags(Arrays.asList("meat", "fresh", "premium"));
        p2.setActive(true);
        p2.setRating(4.9);
        p2.setReviewCount(35);
        p2.setSoldCount(240);
        p2.setCreatedAt("2026-04-02T08:00:00Z");

        Product p3 = new Product();
        p3.setId("prod-3");
        p3.setName("Táo Envy Mỹ nhập khẩu");
        p3.setSlug("tao-envy-my-nhap-khau");
        p3.setPrice(89000);
        p3.setOriginalPrice(99000.0);
        p3.setCategory("fruit");
        p3.setSubCategory("Trái cây nhập khẩu");
        p3.setDescription("Táo Envy Mỹ giòn ngọt vượt trội, hương thơm dịu nhẹ, sắc đỏ bắt mắt giàu dinh dưỡng.");
        p3.setImageUrl("https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?auto=format&fit=crop&w=300&q=80");
        p3.setStock(45);
        p3.setUnit("Kg");
        p3.setTags(Arrays.asList("apple", "import", "sweet"));
        p3.setActive(true);
        p3.setRating(4.7);
        p3.setReviewCount(48);
        p3.setSoldCount(410);
        p3.setCreatedAt("2026-04-03T08:00:00Z");

        Product p4 = new Product();
        p4.setId("prod-4");
        p4.setName("Sữa tươi tiệt trùng ít đường");
        p4.setSlug("sua-tuoi-tiet-trung-it-duong");
        p4.setPrice(32000);
        p4.setOriginalPrice(32000.0);
        p4.setCategory("drink");
        p4.setSubCategory("Sữa & Sản phẩm từ sữa");
        p4.setDescription("Sữa tươi tiệt trùng thơm ngậy mát lành bổ sung canxi và các vitamin cần thiết.");
        p4.setImageUrl("https://images.unsplash.com/photo-1550583724-b2692b85b150?auto=format&fit=crop&w=300&q=80");
        p4.setStock(120);
        p4.setUnit("Lốc 4 hộp 180ml");
        p4.setTags(Arrays.asList("milk", "healthy", "daily"));
        p4.setActive(true);
        p4.setRating(4.5);
        p4.setReviewCount(9);
        p4.setSoldCount(95);
        p4.setCreatedAt("2026-04-04T08:00:00Z");

        Product p5 = new Product();
        p5.setId("prod-5");
        p5.setName("Mì gói Hảo Hảo Tôm Chua Cay");
        p5.setSlug("mi-hao-hao-tom-chua-cay");
        p5.setPrice(4500);
        p5.setOriginalPrice(5000.0);
        p5.setCategory("dry");
        p5.setSubCategory("Đồ ăn liền");
        p5.setDescription("Mì gói quốc dân Hảo Hảo tôm chua cay thơm ngon đậm đà khó cưỡng.");
        p5.setImageUrl("https://images.unsplash.com/photo-1612927601601-6638404737ce?auto=format&fit=crop&w=300&q=80");
        p5.setStock(350);
        p5.setUnit("Gói 75g");
        p5.setTags(Arrays.asList("instant", "popular"));
        p5.setActive(true);
        p5.setRating(4.6);
        p5.setReviewCount(120);
        p5.setSoldCount(1850);
        p5.setCreatedAt("2026-04-05T08:00:00Z");

        Product p6 = new Product();
        p6.setId("prod-6");
        p6.setName("Hạt tiêu đen xay nguyên chất");
        p6.setSlug("hat-tieu-den-xay-nguyen-chat");
        p6.setPrice(22000);
        p6.setOriginalPrice(25000.0);
        p6.setCategory("spice");
        p6.setSubCategory("Gia vị khô");
        p6.setDescription("Tiêu đen thơm nồng nguyên chất xay sẵn cực tiện lợi cho mọi món ăn.");
        p6.setImageUrl("https://images.unsplash.com/photo-1508747703725-719777637510?auto=format&fit=crop&w=300&q=80");
        p6.setStock(0); // Out of stock
        p6.setUnit("Hũ 50g");
        p6.setTags(Arrays.asList("pepper", "spice", "essential"));
        p6.setActive(false); // Inactive product too
        p6.setRating(4.2);
        p6.setReviewCount(5);
        p6.setSoldCount(62);
        p6.setCreatedAt("2026-04-06T08:00:00Z");

        products.addAll(Arrays.asList(p1, p2, p3, p4, p5, p6));

        // 3. Initial Vouchers
        Voucher v1 = new Voucher();
        v1.setCode("FREESHIP");
        v1.setType("ship");
        v1.setValue(15000);
        v1.setCap(0);
        v1.setMinOrderValue(100000);
        v1.setMaxUses(500);
        v1.setActive(true);
        v1.setStartsAt("2026-05-01T00:00:00Z");
        v1.setExpiresAt("2026-06-30T23:59:59Z");
        v1.setNote("Miễn phí vận chuyển cho đơn từ 100k");

        Voucher v2 = new Voucher();
        v2.setCode("HE2026");
        v2.setType("percent");
        v2.setValue(10); // 10%
        v2.setCap(50000); // max 50k
        v2.setMinOrderValue(200000);
        v2.setMaxUses(100);
        v2.setActive(true);
        v2.setStartsAt("2026-05-15T00:00:00Z");
        v2.setExpiresAt("2026-08-31T23:59:59Z");
        v2.setNote("Giảm 10% tối đa 50k chào hè rực rỡ");

        Voucher v3 = new Voucher();
        v3.setCode("PRO50K");
        v3.setType("fixed");
        v3.setValue(50000); // Giảm 50k
        v3.setCap(0);
        v3.setMinOrderValue(500000);
        v3.setMaxUses(50);
        v3.setActive(false); // Expired or inactive
        v3.setStartsAt("2026-01-01T00:00:00Z");
        v3.setExpiresAt("2026-02-28T23:59:59Z");
        v3.setNote("Giảm ngay 50k cho đơn khủng trên 500k");

        vouchers.addAll(Arrays.asList(v1, v2, v3));

        // 4. Initial Orders & Items
        String o1Json = "{\n" +
                "  \"_id\": \"ORD-9843A\",\n" +
                "  \"orderId\": \"ORD-9843A\",\n" +
                "  \"userId\": \"usr-4\",\n" +
                "  \"delivery\": {\n" +
                "    \"name\": \"Phạm Minh Huy\",\n" +
                "    \"phone\": \"0934567890\",\n" +
                "    \"address\": \"12 Song Hành, TP. Thủ Đức, TP. HCM\",\n" +
                "    \"note\": \"\"\n" +
                "  },\n" +
                "  \"payment\": {\n" +
                "    \"method\": \"cod\",\n" +
                "    \"status\": \"pending\",\n" +
                "    \"gateway\": \"COD\",\n" +
                "    \"amount\": 220000\n" +
                "  },\n" +
                "  \"voucherCode\": \"FREESHIP\",\n" +
                "  \"shippingFee\": 20000,\n" +
                "  \"discount\": 15000,\n" +
                "  \"subtotal\": 215000,\n" +
                "  \"totalAmount\": 220000,\n" +
                "  \"status\": \"pending\",\n" +
                "  \"createdAt\": \"2026-05-22T09:30:00Z\",\n" +
                "  \"items\": [\n" +
                "    {\n" +
                "      \"productId\": \"prod-1\",\n" +
                "      \"name\": \"Cà chua bi hữu cơ Đà Lạt\",\n" +
                "      \"price\": 35000,\n" +
                "      \"quantity\": 2,\n" +
                "      \"unit\": \"Hộp 500g\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"productId\": \"prod-2\",\n" +
                "      \"name\": \"Thịt ba rọi heo thảo mộc\",\n" +
                "      \"price\": 145000,\n" +
                "      \"quantity\": 1,\n" +
                "      \"unit\": \"Khay 500g\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        Order o1 = createDtoFromJson(o1Json, Order.class);

        String o2Json = "{\n" +
                "  \"_id\": \"ORD-1092F\",\n" +
                "  \"orderId\": \"ORD-1092F\",\n" +
                "  \"userId\": \"usr-4\",\n" +
                "  \"delivery\": {\n" +
                "    \"name\": \"Phạm Minh Huy\",\n" +
                "    \"phone\": \"0934567890\",\n" +
                "    \"address\": \"12 Song Hành, TP. Thủ Đức, TP. HCM\",\n" +
                "    \"note\": \"\"\n" +
                "  },\n" +
                "  \"payment\": {\n" +
                "    \"method\": \"momo\",\n" +
                "    \"status\": \"paid\",\n" +
                "    \"gateway\": \"momo\",\n" +
                "    \"transactionId\": \"MOMO12948194\",\n" +
                "    \"amount\": 104000,\n" +
                "    \"transactionTime\": \"2026-05-21T15:21:30Z\"\n" +
                "  },\n" +
                "  \"shippingFee\": 15000,\n" +
                "  \"discount\": 0,\n" +
                "  \"subtotal\": 89000,\n" +
                "  \"totalAmount\": 104000,\n" +
                "  \"status\": \"confirmed\",\n" +
                "  \"createdAt\": \"2026-05-21T15:20:00Z\",\n" +
                "  \"items\": [\n" +
                "    {\n" +
                "      \"productId\": \"prod-3\",\n" +
                "      \"name\": \"Táo Envy Mỹ nhập khẩu\",\n" +
                "      \"price\": 89000,\n" +
                "      \"quantity\": 1,\n" +
                "      \"unit\": \"Kg\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        Order o2 = createDtoFromJson(o2Json, Order.class);

        String o3Json = "{\n" +
                "  \"_id\": \"ORD-5743D\",\n" +
                "  \"orderId\": \"ORD-5743D\",\n" +
                "  \"userId\": \"usr-5\",\n" +
                "  \"delivery\": {\n" +
                "    \"name\": \"Đặng Minh Anh\",\n" +
                "    \"phone\": \"0945678901\",\n" +
                "    \"address\": \"56 Nguyễn Chí Thanh, Quận 5, TP. HCM\",\n" +
                "    \"note\": \"\"\n" +
                "  },\n" +
                "  \"payment\": {\n" +
                "    \"method\": \"cod\",\n" +
                "    \"status\": \"paid\",\n" +
                "    \"gateway\": \"COD\",\n" +
                "    \"amount\": 143000\n" +
                "  },\n" +
                "  \"shippingFee\": 15000,\n" +
                "  \"discount\": 0,\n" +
                "  \"subtotal\": 128000,\n" +
                "  \"totalAmount\": 143000,\n" +
                "  \"status\": \"delivered\",\n" +
                "  \"createdAt\": \"2026-05-20T10:00:00Z\",\n" +
                "  \"items\": [\n" +
                "    {\n" +
                "      \"productId\": \"prod-4\",\n" +
                "      \"name\": \"Sữa tươi tiệt trùng ít đường\",\n" +
                "      \"price\": 32000,\n" +
                "      \"quantity\": 4,\n" +
                "      \"unit\": \"Lốc 4 hộp 180ml\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        Order o3 = createDtoFromJson(o3Json, Order.class);

        String momoPendingJson = "{\n" +
                "  \"_id\": \"ORD-MOMO-P\",\n" +
                "  \"orderId\": \"ORD-MOMO-P\",\n" +
                "  \"userId\": \"usr-4\",\n" +
                "  \"delivery\": {\"name\": \"Phạm Minh Huy\", \"phone\": \"0934567890\", \"address\": \"12 Song Hành, TP. Thủ Đức\"},\n" +
                "  \"payment\": {\"method\": \"momo\", \"status\": \"pending\", \"gateway\": \"momo\", \"transactionId\": \"REQ-MOMO-P\", \"amount\": 157000},\n" +
                "  \"shippingFee\": 15000,\n" +
                "  \"subtotal\": 142000,\n" +
                "  \"totalAmount\": 157000,\n" +
                "  \"status\": \"pending\",\n" +
                "  \"createdAt\": \"2026-05-23T09:15:00Z\",\n" +
                "  \"items\": [{\"productId\": \"prod-1\", \"name\": \"Cà chua bi hữu cơ Đà Lạt\", \"price\": 35000, \"quantity\": 2, \"unit\": \"Hộp 500g\"}]\n" +
                "}";
        String momoPaidJson = "{\n" +
                "  \"_id\": \"ORD-MOMO-S\",\n" +
                "  \"orderId\": \"ORD-MOMO-S\",\n" +
                "  \"userId\": \"usr-4\",\n" +
                "  \"delivery\": {\"name\": \"Phạm Minh Huy\", \"phone\": \"0934567890\", \"address\": \"12 Song Hành, TP. Thủ Đức\"},\n" +
                "  \"payment\": {\"method\": \"momo\", \"status\": \"paid\", \"gateway\": \"momo\", \"transactionId\": \"MOMO12948194\", \"amount\": 104000},\n" +
                "  \"shippingFee\": 15000,\n" +
                "  \"subtotal\": 89000,\n" +
                "  \"totalAmount\": 104000,\n" +
                "  \"status\": \"confirmed\",\n" +
                "  \"createdAt\": \"2026-05-23T10:20:00Z\",\n" +
                "  \"items\": [{\"productId\": \"prod-3\", \"name\": \"Táo Envy Mỹ nhập khẩu\", \"price\": 89000, \"quantity\": 1, \"unit\": \"Kg\"}]\n" +
                "}";
        String momoFailedJson = "{\n" +
                "  \"_id\": \"ORD-MOMO-F\",\n" +
                "  \"orderId\": \"ORD-MOMO-F\",\n" +
                "  \"userId\": \"usr-5\",\n" +
                "  \"delivery\": {\"name\": \"Đặng Minh Anh\", \"phone\": \"0945678901\", \"address\": \"56 Nguyễn Chí Thanh, Quận 5\"},\n" +
                "  \"payment\": {\"method\": \"momo\", \"status\": \"failed\", \"gateway\": \"momo\", \"transactionId\": \"REQ-MOMO-F\", \"amount\": 143000},\n" +
                "  \"shippingFee\": 15000,\n" +
                "  \"subtotal\": 128000,\n" +
                "  \"totalAmount\": 143000,\n" +
                "  \"status\": \"pending\",\n" +
                "  \"createdAt\": \"2026-05-23T11:30:00Z\",\n" +
                "  \"items\": [{\"productId\": \"prod-4\", \"name\": \"Sữa tươi tiệt trùng ít đường\", \"price\": 32000, \"quantity\": 4, \"unit\": \"Lốc 4 hộp 180ml\"}]\n" +
                "}";
        Order momoPending = createDtoFromJson(momoPendingJson, Order.class);
        Order momoPaid = createDtoFromJson(momoPaidJson, Order.class);
        Order momoFailed = createDtoFromJson(momoFailedJson, Order.class);

        orders.addAll(Arrays.asList(o1, o2, o3, momoPending, momoPaid, momoFailed));

        // 5. Initial Shipments - Handled lazily via getShipments()

        // Since there are no setters in Shipment DTO, we can't edit fields directly unless they are there,
        // Wait, did we see setters in Shipment DTO? Let's check Shipment.java again:
        // Ah, Shipment.java has only getters and no setters! Let's check if we can write a subclass,
        // or we can use reflection, or we can use Gson serialize/deserialize to set fields if needed!
        // Wait! In Java, we can serialize a customized JSON string and parse it with Gson into a Shipment object!
        // That is an incredibly clever and robust way to instantiate classes without setters!
        // Let's create an easy helper to instantiate mock shipments and other objects using Gson!
    }

    // Helper using Gson to instantiate/clone mock DTOs without setters
    private <T> T createDtoFromJson(String json, Class<T> clazz) {
        return new com.google.gson.Gson().fromJson(json, clazz);
    }

    private void writeToFirebase(String path, Object obj) {
        try {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            String json = new com.google.gson.Gson().toJson(obj);
            java.util.Map<String, Object> map = new com.google.gson.Gson().fromJson(
                json, new com.google.gson.reflect.TypeToken<java.util.Map<String, Object>>(){}.getType()
            );
            dbRef.child(path).setValue(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Custom class for AuditLog
    public static class AuditLog {
        public String id;
        public String timestamp;
        public String operatorName;
        public String role;
        public String action;
        public String target;
        public String details;

        public AuditLog(String operatorName, String role, String action, String target, String details) {
            this.id = "AUD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            this.operatorName = operatorName;
            this.role = role;
            this.action = action;
            this.target = target;
            this.details = details;
        }
    }

    public List<AuditLog> getAuditLogs() {
        return auditLogs;
    }

    public void addAuditLog(String action, String target, String details) {
        String name = currentUser != null ? currentUser.getName() : "Hệ thống";
        String role = currentUser != null ? currentUser.getRole() : "system";
        AuditLog log = new AuditLog(name, role, action, target, details);
        auditLogs.add(0, log);
        writeToFirebase("audit_logs/" + log.id, log);
    }

    // Users
    public List<User> getUsers() {
        return users;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public boolean adminLogin(String email, String password) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email) && "123456".equals(password)) { // Dummy check
                if (u.isBackoffice() && u.isActive()) {
                    currentUser = u;
                    addAuditLog("Đăng nhập", "Hệ thống", "Đăng nhập thành công vào trang quản trị");
                    return true;
                }
            }
        }
        return false;
    }

    public void updateUser(User user) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(user.getId())) {
                users.set(i, user);
                addAuditLog("Cập nhật thành viên", user.getName(), "Cập nhật quyền thành " + user.getRole() + ", trạng thái hoạt động: " + user.isActive());
                try {
                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                    dbRef.child("users").child(user.getId()).child("role").setValue(user.getRole() != null ? user.getRole().toUpperCase() : "CUSTOMER");
                    dbRef.child("users").child(user.getId()).child("isActive").setValue(user.isActive());
                    dbRef.child("users").child(user.getId()).child("is_active").setValue(user.isActive());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
        }
    }

    // Products CRUD
    public List<Product> getProducts() {
        return products;
    }

    public void addProduct(Product p) {
        p.setId("prod-" + (products.size() + 1));
        p.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date()));
        products.add(p);
        addAuditLog("Thêm sản phẩm", p.getName(), "Tạo sản phẩm mới với giá " + p.getPrice() + "đ");
    }

    public void updateProduct(Product p) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equals(p.getId())) {
                products.set(i, p);
                addAuditLog("Sửa sản phẩm", p.getName(), "Cập nhật thông tin chi tiết sản phẩm");
                return;
            }
        }
    }

    public void deleteProduct(String id) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equals(id)) {
                Product p = products.remove(i);
                addAuditLog("Xóa sản phẩm", p.getName(), "Xóa sản phẩm khỏi hệ thống (soft delete)");
                return;
            }
        }
    }

    // Orders
    public List<Order> getOrders() {
        return orders;
    }

    public void updateOrderStatus(String orderId, String status) {
        for (Order o : orders) {
            if (o.getId().equals(orderId)) {
                String oldStatus = o.getStatus();
                o.setStatus(status);
                addAuditLog("Cập nhật đơn hàng", o.getId(), "Thay đổi trạng thái từ '" + oldStatus + "' sang '" + status + "'");
                
                try {
                    FirebaseDatabase.getInstance().getReference().child("orders").child(orderId).child("status").setValue(status.toUpperCase());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Trigger auto-creation of shipment if status is confirmed/shipping
                if ("confirmed".equalsIgnoreCase(status)) {
                    createShipmentForOrder(o);
                }
                return;
            }
        }
    }

    public void approveReturn(String orderId, boolean approved, String note) {
        for (Order o : orders) {
            if (o.getId().equals(orderId)) {
                String action = approved ? "Duyệt trả hàng" : "Từ chối trả hàng";
                o.setStatus(approved ? "return_approved" : "return_rejected");
                addAuditLog(action, o.getId(), "Ghi chú: " + note);
                
                try {
                    FirebaseDatabase.getInstance().getReference().child("orders").child(orderId).child("status").setValue(o.getStatus().toUpperCase());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
        }
    }

    public void markPaid(String orderId) {
        for (Order o : orders) {
            if (o.getId().equals(orderId)) {
                o.setPaymentStatus("paid");
                addAuditLog("Đánh dấu thanh toán", o.getId(), "Đánh dấu đã thanh toán thành công");
                
                try {
                    FirebaseDatabase.getInstance().getReference().child("orders").child(orderId).child("payment_status").setValue("PAID");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
        }
    }

    // Vouchers CRUD
    public List<Voucher> getVouchers() {
        return vouchers;
    }

    public void addVoucher(Voucher v) {
        v.setCode(v.getCode().toUpperCase().trim());
        if (v.getId() == null) {
            v.setId("vouch-" + UUID.randomUUID().toString().substring(0, 8));
        }
        vouchers.add(v);
        writeToFirebase("vouchers/" + v.getCode(), v);
        addAuditLog("Tạo khuyến mãi", v.getCode(), "Mức giảm: " + v.getValue() + ", Kiểu: " + v.getType());
    }

    public void updateVoucher(String oldCode, Voucher v) {
        for (int i = 0; i < vouchers.size(); i++) {
            if (vouchers.get(i).getCode().equalsIgnoreCase(oldCode)) {
                v.setCode(v.getCode().toUpperCase().trim());
                if (!oldCode.equalsIgnoreCase(v.getCode())) {
                    try {
                        FirebaseDatabase.getInstance().getReference().child("vouchers").child(oldCode).removeValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                vouchers.set(i, v);
                writeToFirebase("vouchers/" + v.getCode(), v);
                addAuditLog("Cập nhật khuyến mãi", v.getCode(), "Chỉnh sửa thông số voucher");
                return;
            }
        }
    }

    public void deleteVoucher(String code) {
        for (int i = 0; i < vouchers.size(); i++) {
            if (vouchers.get(i).getCode().equalsIgnoreCase(code)) {
                Voucher v = vouchers.remove(i);
                try {
                    FirebaseDatabase.getInstance().getReference().child("vouchers").child(code).removeValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                addAuditLog("Xóa khuyến mãi", v.getCode(), "Xóa voucher khỏi hệ thống");
                return;
            }
        }
    }

    // Shipments
    public List<Shipment> getShipments() {
        if (shipments.isEmpty()) {
            // Lazy load a couple of shipments
            for (Order o : orders) {
                if ("confirmed".equalsIgnoreCase(o.getStatus()) || "delivered".equalsIgnoreCase(o.getStatus())) {
                    createShipmentForOrder(o);
                }
            }
        }
        return shipments;
    }

    private void createShipmentForOrder(Order o) {
        for (Shipment s : shipments) {
            if (s.getOrderId().equals(o.getId())) return; // Already exists
        }

        String tracking = "TRACK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String carrier = "internal";
        String status = "delivered".equalsIgnoreCase(o.getStatus()) ? "delivered" : "pending";
        String dateStr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date());

        String json = "{\n" +
                "  \"_id\": \"ship-" + UUID.randomUUID().toString().substring(0, 8) + "\",\n" +
                "  \"orderId\": \"" + o.getId() + "\",\n" +
                "  \"customerId\": \"" + o.getCustomerId() + "\",\n" +
                "  \"carrier\": \"" + carrier + "\",\n" +
                "  \"trackingNumber\": \"" + tracking + "\",\n" +
                "  \"shippingFee\": " + o.getShippingFee() + ",\n" +
                "  \"eta\": \"2026-05-25T17:00:00Z\",\n" +
                "  \"currentStatus\": \"" + status + "\",\n" +
                "  \"createdAt\": \"" + dateStr + "\",\n" +
                "  \"statusHistory\": [\n" +
                "    {\n" +
                "      \"status\": \"pending\",\n" +
                "      \"note\": \"Đã tạo yêu cầu vận chuyển\",\n" +
                "      \"timestamp\": \"" + dateStr + "\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Shipment s = createDtoFromJson(json, Shipment.class);
        shipments.add(0, s);
        writeToFirebase("shipments/" + s.getId(), s);
    }

    public void updateShipmentStatus(String shipmentId, String status, String note) {
        for (int i = 0; i < shipments.size(); i++) {
            Shipment s = shipments.get(i);
            if (s.getId().equals(shipmentId)) {
                String dateStr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date());
                // Build new status history list
                List<String> historyStrings = new ArrayList<>();
                if (s.getStatusHistory() != null) {
                    for (vn.vuavuive.shared.data.dto.StatusEvent event : s.getStatusHistory()) {
                        historyStrings.add("{\n" +
                                "      \"status\": \"" + event.getStatus() + "\",\n" +
                                "      \"note\": \"" + event.getNote() + "\",\n" +
                                "      \"timestamp\": \"" + event.getTimestamp() + "\"\n" +
                                "    }");
                    }
                }
                historyStrings.add("{\n" +
                        "      \"status\": \"" + status + "\",\n" +
                        "      \"note\": \"" + note + "\",\n" +
                        "      \"timestamp\": \"" + dateStr + "\"\n" +
                        "    }");

                String historyJson = "[" + String.join(",", historyStrings) + "]";

                String updatedJson = "{\n" +
                        "  \"_id\": \"" + s.getId() + "\",\n" +
                        "  \"orderId\": \"" + s.getOrderId() + "\",\n" +
                        "  \"customerId\": \"" + s.getCustomerId() + "\",\n" +
                        "  \"carrier\": \"" + s.getCarrier() + "\",\n" +
                        "  \"trackingNumber\": \"" + s.getTrackingNumber() + "\",\n" +
                        "  \"shippingFee\": " + s.getShippingFee() + ",\n" +
                        "  \"eta\": \"" + s.getEta() + "\",\n" +
                        "  \"currentStatus\": \"" + status + "\",\n" +
                        "  \"createdAt\": \"" + s.getCreatedAt() + "\",\n" +
                        "  \"statusHistory\": " + historyJson + "\n" +
                        "}";

                Shipment updatedShipment = createDtoFromJson(updatedJson, Shipment.class);
                shipments.set(i, updatedShipment);
                writeToFirebase("shipments/" + s.getId(), updatedShipment);
                addAuditLog("Cập nhật vận chuyển", s.getTrackingNumber(), "Cập nhật trạng thái '" + status + "', Ghi chú: " + note);
                
                // If shipment delivered, mark order as delivered!
                if ("delivered".equals(status)) {
                    updateOrderStatus(s.getOrderId(), "delivered");
                }
                return;
            }
        }
    }

    public DashboardStats getDashboardStats() {
        int todayOrders = 0;
        int monthOrders = 0;
        int pending = 0;
        int shipping = 0;
        long revenue = 0;

        for (Order o : orders) {
            todayOrders++;
            monthOrders++;
            if ("pending".equalsIgnoreCase(o.getStatus())) pending++;
            if ("shipping".equalsIgnoreCase(o.getStatus()) || "in_transit".equalsIgnoreCase(o.getStatus())) shipping++;
            if ("delivered".equalsIgnoreCase(o.getStatus())) {
                revenue += o.getTotalAmount();
            }
        }

        String json = "{\n" +
                "  \"todayOrders\": " + todayOrders + ",\n" +
                "  \"monthOrders\": " + monthOrders + ",\n" +
                "  \"totalOrders\": " + orders.size() + ",\n" +
                "  \"pendingCount\": " + pending + ",\n" +
                "  \"shippingCount\": " + shipping + ",\n" +
                "  \"totalRevenue\": " + revenue + ",\n" +
                "  \"totalUsers\": " + users.size() + "\n" +
                "}";

        return createDtoFromJson(json, DashboardStats.class);
    }
}
