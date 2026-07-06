package vn.vuavuive.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.UUID;
import vn.vuavuive.backend.modules.category.Category;
import vn.vuavuive.backend.modules.category.CategoryRepository;
import vn.vuavuive.backend.modules.order.Order;
import vn.vuavuive.backend.modules.order.OrderItem;
import vn.vuavuive.backend.modules.order.OrderRepository;
import vn.vuavuive.backend.modules.product.Product;
import vn.vuavuive.backend.modules.product.ProductRepository;
import vn.vuavuive.backend.modules.shipper.Shipper;
import vn.vuavuive.backend.modules.shipper.ShipperRepository;
import vn.vuavuive.backend.modules.user.User;
import vn.vuavuive.backend.modules.user.UserRepository;

import java.util.UUID;

/**
 * DataSeeder - Tự động chèn tài khoản thử nghiệm và dữ liệu mẫu lên Firebase RTDB khi ứng dụng khởi động.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ShipperRepository shipperRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        try {
            seedUsers();
            seedProducts();
            seedShipperOrders();
        } catch (Exception e) {
            log.warn("Lưu ý: Không thể hoàn tất seed dữ liệu (có thể do chưa kết nối được Firebase hoặc thiếu key credentials): {}", e.getMessage());
        }
    }

    private void seedUsers() {
        // 1. Seed tài khoản ADMIN (Force update password)
        User admin = userRepository.findByEmail("admin@vuavuive.vn").orElseGet(() -> {
            User newUser = User.builder()
                    .email("admin@vuavuive.vn")
                    .fullName("Quản Trị Viên")
                    .phone("0999999999")
                    .role(User.Role.ADMIN)
                    .isActive(true)
                    .build();
            return newUser;
        });
        admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
        userRepository.save(admin);
        log.info(">> SEED (FORCE): Cập nhật thành công tài khoản ADMIN: admin@vuavuive.vn / Admin@123 trên Firebase RTDB");


        // 2. Seed tài khoản CUSTOMER
        if (!userRepository.findByEmail("customer@gmail.com").isPresent()) {
            User customer = User.builder()
                    .email("customer@gmail.com")
                    .fullName("Khách Hàng Vui Vẻ")
                    .phone("0912345678")
                    .passwordHash(passwordEncoder.encode("Customer@123"))
                    .role(User.Role.CUSTOMER)
                    .isActive(true)
                    .build();
            userRepository.save(customer);
            log.info(">> SEED: Tạo thành công tài khoản CUSTOMER: customer@gmail.com / Customer@123");
        }

        // 3. Seed tài khoản SHIPPER (User) và đối tượng Shipper
        User shipperUser = userRepository.findByEmail("shipper@gmail.com").orElseGet(() -> {
            User user = User.builder()
                    .email("shipper@gmail.com")
                    .fullName("Tài Xế Vui Vẻ")
                    .phone("0987654321")
                    .passwordHash(passwordEncoder.encode("Shipper@123"))
                    .role(User.Role.SHIPPER)
                    .isActive(true)
                    .build();
            userRepository.save(user);
            log.info(">> SEED: Tạo thành công tài khoản SHIPPER (User): shipper@gmail.com / Shipper@123");
            return user;
        });
        if (shipperUser.getRole() != User.Role.SHIPPER) {
            shipperUser.setRole(User.Role.SHIPPER);
            userRepository.save(shipperUser);
        }

        // Tạo/cập nhật đối tượng Shipper trong bảng shippers tương ứng
        String shipperAssignmentId = resolveSeedShipperAssignmentId(shipperUser);
        Shipper shipper = shipperRepository.findByPhone("0987654321").orElseGet(() -> {
            Shipper created = Shipper.builder()
                    .fullName("Tài Xế Vui Vẻ")
                    .phone("0987654321")
                    .vehicleNumber("29A-123.45")
                    .currentStatus(Shipper.Status.AVAILABLE)
                    .isActive(true)
                    .build();
            log.info(">> SEED: Tạo thành công thực thể Shipper với SĐT: 0987654321");
            return created;
        });
        if (shipper.getUserId() == null || !shipperAssignmentId.equals(shipper.getUserId())) {
            shipper.setUserId(shipperAssignmentId);
            shipperRepository.save(shipper);
        }
    }

    private String resolveSeedShipperAssignmentId(User shipperUser) {
        return userRepository.findAll().stream()
                .filter(u -> shipperUser.getEmail() != null && shipperUser.getEmail().equalsIgnoreCase(u.getEmail()))
                .filter(u -> u.getId() != null && !u.getId().contains("-"))
                .map(User::getId)
                .findFirst()
                .orElse(shipperUser.getId());
    }

    private void seedProducts() {
        if (categoryRepository.findAll().isEmpty()) {
            categoryRepository.save(Category.builder().name("Rau củ hữu cơ").slug("rau-cu-huu-co").imageUrl("https://res.cloudinary.com/ddj1f931a/image/upload/v1716943000/vegetables.jpg").isActive(true).build());
            categoryRepository.save(Category.builder().name("Trái cây tươi").slug("trai-cay-tuoi").imageUrl("https://res.cloudinary.com/ddj1f931a/image/upload/v1716943000/fruits.jpg").isActive(true).build());
            categoryRepository.save(Category.builder().name("Thịt tươi sống").slug("thit-tuoi-song").imageUrl("https://res.cloudinary.com/ddj1f931a/image/upload/v1716943000/meat.jpg").isActive(true).build());
            categoryRepository.save(Category.builder().name("Đồ uống giải khát").slug("do-uong-giai-khat").imageUrl("https://images.unsplash.com/photo-1536825919195-6ae26a4416da?w=400").isActive(true).build());
            categoryRepository.save(Category.builder().name("Đồ khô sạch").slug("do-kho-sach").imageUrl("https://images.unsplash.com/photo-1536304993881-ff6e9eefa2a6?w=400").isActive(true).build());
            categoryRepository.save(Category.builder().name("Gia vị truyền thống").slug("gia-vi-truyen-thong").imageUrl("https://images.unsplash.com/photo-1540148426945-6cf22a6b2383?w=400").isActive(true).build());
            categoryRepository.save(Category.builder().name("Bánh kẹo ngọt").slug("banh-keo-ngot").imageUrl("https://images.unsplash.com/photo-1589367920969-ab8e050bbb04?w=400").isActive(true).build());
            categoryRepository.save(Category.builder().name("Đồ đông lạnh IQF").slug("do-dong-lanh").imageUrl("https://images.unsplash.com/photo-1559628376-f3fe5f782a2e?w=400").isActive(true).build());
            log.info(">> SEED: Đã tạo 8 danh mục sản phẩm mẫu trên Firebase");
        }

        if (productRepository.findAll().isEmpty()) {
            Category cat1 = categoryRepository.findAllRootCategories().stream().findFirst().orElse(null);
            String catId = cat1 != null ? cat1.getId() : UUID.randomUUID().toString();
            
            productRepository.save(Product.builder()
                    .categoryId(catId)
                    .name("Cải thảo tươi Đà Lạt")
                    .description("Cải thảo tươi ngon từ Đà Lạt, giàu vitamin C và chất xơ. Thu hoạch mỗi ngày, đảm bảo độ tươi ngon tối đa.")
                    .originalPrice(BigDecimal.valueOf(20000.0))
                    .sellingPrice(BigDecimal.valueOf(15000.0))
                    .stockQuantity(45)
                    .unit("KG")
                    .imageUrl("https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=400")
                    .isActive(true)
                    .build());

            productRepository.save(Product.builder()
                    .categoryId(catId)
                    .name("Cà chua bi đỏ")
                    .description("Cà chua bi đỏ Đà Lạt, vị ngọt thanh, giàu lycopene và vitamin. Lý tưởng cho salad và nấu ăn.")
                    .originalPrice(BigDecimal.valueOf(32000.0))
                    .sellingPrice(BigDecimal.valueOf(25000.0))
                    .stockQuantity(60)
                    .unit("KG")
                    .imageUrl("https://images.unsplash.com/photo-1546094096-0df4bcaaa337?w=400")
                    .isActive(true)
                    .build());

            log.info(">> SEED: Đã tạo sản phẩm mẫu trên Firebase");
        }
    }

    private void seedShipperOrders() {
        Shipper shipper = shipperRepository.findByPhone("0987654321").orElse(null);
        User customer = userRepository.findByEmail("customer@gmail.com").orElse(null);
        Product product = productRepository.findAll().stream().findFirst().orElse(null);
        if (shipper == null || customer == null || product == null) {
            return;
        }
        String shipperAssignmentId = shipper.getUserId() != null ? shipper.getUserId() : shipper.getId();
        if (orderRepository.findByShipperIdOrderByCreatedAtDesc(shipperAssignmentId, PageRequest.of(0, 3)).getNumberOfElements() >= 3) {
            return;
        }

        seedOrder(customer, shipper, product, Order.OrderStatus.CONFIRMED, Order.PaymentStatus.UNPAID, "Seed: Admin da gan shipper");
        seedOrder(customer, shipper, product, Order.OrderStatus.IN_TRANSIT, Order.PaymentStatus.UNPAID, "Seed: Shipper dang giao");
        seedOrder(customer, shipper, product, Order.OrderStatus.DELIVERED, Order.PaymentStatus.PAID, "Seed: Da giao thanh cong");
        log.info(">> SEED: Da tao 3 don hang mau cho shipper@gmail.com / Shipper@123");
    }

    private void seedOrder(User customer, Shipper shipper, Product product,
                           Order.OrderStatus status, Order.PaymentStatus paymentStatus, String note) {
        BigDecimal unitPrice = product.getSellingPrice();
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(2));
        Order order = Order.builder()
                .userId(customer.getId())
                .userName(customer.getFullName())
                .userPhone(customer.getPhone())
                .shipperId(shipper.getUserId() != null ? shipper.getUserId() : shipper.getId())
                .shipperName(shipper.getFullName())
                .status(status)
                .paymentMethod("COD")
                .paymentStatus(paymentStatus)
                .totalAmount(subtotal)
                .finalAmount(subtotal)
                .deliveryName("Khach Test Flow")
                .deliveryPhone("0912345678")
                .deliveryAddress("123 Duong Test, Quan 1, TP.HCM")
                .note(note)
                .build();
        OrderItem item = OrderItem.builder()
                .productId(product.getId())
                .productName(product.getName())
                .quantity(2)
                .unitPrice(unitPrice)
                .subtotal(subtotal)
                .build();
        order.getOrderItems().add(item);
        orderRepository.save(order);
    }
}
