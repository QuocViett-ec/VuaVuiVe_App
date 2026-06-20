package vn.vuavuive.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
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

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DataSeeder - Tự động chèn tài khoản thử nghiệm và dữ liệu mẫu khi ứng dụng khởi động.
 * Giúp lập trình viên có sẵn tài khoản Admin, Customer, Shipper và các sản phẩm để test.
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
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        seedUsers();
        seedProducts();
        seedShipperOrders();
    }

    private void seedUsers() {
        // 1. Seed tài khoản ADMIN
        if (!userRepository.findByEmail("admin@vuavuive.vn").isPresent()) {
            User admin = User.builder()
                    .email("admin@vuavuive.vn")
                    .fullName("Quản Trị Viên")
                    .phone("0999999999")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .role(User.Role.ADMIN)
                    .isActive(true)
                    .build();
            userRepository.save(admin);
            log.info(">> SEED: Tạo thành công tài khoản ADMIN: admin@vuavuive.vn / Admin@123");
        }

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
        if (shipper.getUser() == null) {
            shipper.setUser(shipperUser);
            shipperRepository.save(shipper);
        }
    }

    private void seedProducts() {
        if (categoryRepository.count() == 0) {
            jdbcTemplate.update(
                "INSERT INTO categories (id, name, slug, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                "22222222-2222-2222-2222-222222220001", "Rau củ hữu cơ", "rau-cu-huu-co", "https://res.cloudinary.com/ddj1f931a/image/upload/v1716943000/vegetables.jpg", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO categories (id, name, slug, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                "22222222-2222-2222-2222-222222220002", "Trái cây tươi", "trai-cay-tuoi", "https://res.cloudinary.com/ddj1f931a/image/upload/v1716943000/fruits.jpg", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO categories (id, name, slug, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                "22222222-2222-2222-2222-222222220003", "Thịt tươi sống", "thit-tuoi-song", "https://res.cloudinary.com/ddj1f931a/image/upload/v1716943000/meat.jpg", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO categories (id, name, slug, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                "22222222-2222-2222-2222-222222220004", "Đồ uống giải khát", "do-uong-giai-khat", "https://images.unsplash.com/photo-1536825919195-6ae26a4416da?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO categories (id, name, slug, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                "22222222-2222-2222-2222-222222220005", "Đồ khô sạch", "do-kho-sach", "https://images.unsplash.com/photo-1536304993881-ff6e9eefa2a6?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO categories (id, name, slug, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                "22222222-2222-2222-2222-222222220006", "Gia vị truyền thống", "gia-vi-truyen-thong", "https://images.unsplash.com/photo-1540148426945-6cf22a6b2383?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO categories (id, name, slug, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                "22222222-2222-2222-2222-222222220007", "Bánh kẹo ngọt", "banh-keo-ngot", "https://images.unsplash.com/photo-1589367920969-ab8e050bbb04?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO categories (id, name, slug, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                "22222222-2222-2222-2222-222222220008", "Đồ đông lạnh IQF", "do-dong-lanh", "https://images.unsplash.com/photo-1559628376-f3fe5f782a2e?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            log.info(">> SEED: Đã tạo 8 danh mục sản phẩm mẫu bằng SQL");
        }

        if (productRepository.count() == 0) {
            jdbcTemplate.update(
                "INSERT INTO products (id, category_id, name, description, original_price, selling_price, stock_quantity, unit, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "11111111-1111-1111-1111-111111110001", "22222222-2222-2222-2222-222222220001", "Cải thảo tươi Đà Lạt", "Cải thảo tươi ngon từ Đà Lạt, giàu vitamin C và chất xơ. Thu hoạch mỗi ngày, đảm bảo độ tươi ngon tối đa.", new java.math.BigDecimal("20000"), new java.math.BigDecimal("15000.0"), 45, "KG", "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO products (id, category_id, name, description, original_price, selling_price, stock_quantity, unit, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "11111111-1111-1111-1111-111111110002", "22222222-2222-2222-2222-222222220001", "Cà chua bi đỏ", "Cà chua bi đỏ Đà Lạt, vị ngọt thanh, giàu lycopene và vitamin. Lý tưởng cho salad và nấu ăn.", new java.math.BigDecimal("32000"), new java.math.BigDecimal("25000.0"), 60, "KG", "https://images.unsplash.com/photo-1546094096-0df4bcaaa337?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO products (id, category_id, name, description, original_price, selling_price, stock_quantity, unit, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "11111111-1111-1111-1111-111111110003", "22222222-2222-2222-2222-222222220001", "Bắp cải xanh Đà Lạt", "Bắp cải xanh tươi ngon, giòn, thích hợp xào, luộc hoặc làm kim chi.", new java.math.BigDecimal("12000.0"), new java.math.BigDecimal("12000.0"), 80, "KG", "https://images.unsplash.com/photo-1550828394-9f9b5dcd66e1?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO products (id, category_id, name, description, original_price, selling_price, stock_quantity, unit, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "11111111-1111-1111-1111-111111110004", "22222222-2222-2222-2222-222222220001", "Khoai tây Đà Lạt", "Khoai tây Đà Lạt loại 1, không thuốc bảo quản, phù hợp chiên, nấu canh hoặc nghiền.", new java.math.BigDecimal("28000"), new java.math.BigDecimal("22000.0"), 120, "KG", "https://images.unsplash.com/photo-1518977822534-7049a61ee0c2?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO products (id, category_id, name, description, original_price, selling_price, stock_quantity, unit, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "11111111-1111-1111-1111-111111110005", "22222222-2222-2222-2222-222222220001", "Cà rốt nhập khẩu Úc", "Cà rốt nhập khẩu Úc, size đều, ngọt, giàu beta-carotene và vitamin A. Rất tốt cho mắt.", new java.math.BigDecimal("42000"), new java.math.BigDecimal("35000.0"), 40, "KG", "https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO products (id, category_id, name, description, original_price, selling_price, stock_quantity, unit, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "11111111-1111-1111-1111-111111110006", "22222222-2222-2222-2222-222222220001", "Ớt chuông 3 màu", "Ớt chuông đỏ-vàng-xanh tươi ngon, không cay, thích hợp xào hoặc làm salad. Combo tiện lợi.", new java.math.BigDecimal("65000"), new java.math.BigDecimal("55000.0"), 25, "TÚI 500G", "https://images.unsplash.com/photo-1563565375-f3fdfdbefa83?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO products (id, category_id, name, description, original_price, selling_price, stock_quantity, unit, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "11111111-1111-1111-1111-111111110007", "22222222-2222-2222-2222-222222220002", "Xoài cát Hòa Lộc", "Xoài cát Hòa Lộc thơm ngon, ngọt dịu, hạt nhỏ thịt nhiều. Đặc sản miền Tây chính hiệu.", new java.math.BigDecimal("58000"), new java.math.BigDecimal("45000.0"), 30, "KG", "https://images.unsplash.com/photo-1601493700631-2b16ec4b4716?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO products (id, category_id, name, description, original_price, selling_price, stock_quantity, unit, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "11111111-1111-1111-1111-111111110008", "22222222-2222-2222-2222-222222220002", "Thanh long đỏ Bình Thuận", "Thanh long đỏ ruột đỏ Bình Thuận, ngọt mát, giàu chất chống oxy hóa. VietGAP.", new java.math.BigDecimal("48000"), new java.math.BigDecimal("38000.0"), 50, "KG", "https://images.unsplash.com/photo-1527325678964-54921661f888?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO products (id, category_id, name, description, original_price, selling_price, stock_quantity, unit, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "11111111-1111-1111-1111-111111110009", "22222222-2222-2222-2222-222222220002", "Cam sành Vĩnh Long", "Cam sành Vĩnh Long múi vàng óng, nước nhiều, vị ngọt chua thanh tao. Tươi hái mỗi ngày.", new java.math.BigDecimal("28000.0"), new java.math.BigDecimal("28000.0"), 100, "KG", "https://images.unsplash.com/photo-1547514701-42782101795e?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO products (id, category_id, name, description, original_price, selling_price, stock_quantity, unit, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "11111111-1111-1111-1111-111111110010", "22222222-2222-2222-2222-222222220002", "Dứa mật Ninh Bình", "Dứa mật Ninh Bình đặc biệt ngọt, thơm, mắt nông dễ gọt. Dùng ăn tươi hoặc làm sinh tố.", new java.math.BigDecimal("32000"), new java.math.BigDecimal("25000.0"), 35, "QUẢ", "https://images.unsplash.com/photo-1550258987-190a2d41a8ba?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO products (id, category_id, name, description, original_price, selling_price, stock_quantity, unit, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "11111111-1111-1111-1111-111111110011", "22222222-2222-2222-2222-222222220003", "Thịt ba chỉ heo Sapa", "Thịt ba chỉ heo đồi Sapa, nạc mỡ cân đối, thịt chắc và thơm. Thích hợp kho, quay hoặc nướng BBQ.", new java.math.BigDecimal("175000"), new java.math.BigDecimal("150000.0"), 20, "KG", "https://images.unsplash.com/photo-1529692236671-f1f6cf9683ba?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO products (id, category_id, name, description, original_price, selling_price, stock_quantity, unit, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "11111111-1111-1111-1111-111111110012", "22222222-2222-2222-2222-222222220003", "Ức gà tươi organic", "Ức gà tươi nuôi thả vườn, không hormon, giàu protein. Phù hợp tập gym, ăn kiêng lành mạnh.", new java.math.BigDecimal("115000"), new java.math.BigDecimal("95000.0"), 15, "KG", "https://images.unsplash.com/photo-1604503468506-a8da13d82791?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO products (id, category_id, name, description, original_price, selling_price, stock_quantity, unit, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "11111111-1111-1111-1111-111111110013", "22222222-2222-2222-2222-222222220003", "Tôm sú tươi Cà Mau", "Tôm sú Cà Mau size 20-25 con/kg, con to đều, thịt chắc ngọt. Thu mua trực tiếp từ ao nuôi.", new java.math.BigDecimal("320000"), new java.math.BigDecimal("280000.0"), 8, "KG", "https://images.unsplash.com/photo-1565680018434-b513d5e5fd47?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO products (id, category_id, name, description, original_price, selling_price, stock_quantity, unit, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "11111111-1111-1111-1111-111111110014", "22222222-2222-2222-2222-222222220003", "Cá hồi Nauy phi lê", "Cá hồi Nauy phi lê tươi, không gai, giàu Omega-3. Thích hợp áp chảo, sashimi hoặc nướng.", new java.math.BigDecimal("380000"), new java.math.BigDecimal("320000.0"), 12, "KG", "https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO products (id, category_id, name, description, original_price, selling_price, stock_quantity, unit, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "11111111-1111-1111-1111-111111110015", "22222222-2222-2222-2222-222222220004", "Nước dừa tươi Bến Tre", "Dừa tươi Bến Tre đặc sản, nước ngọt thanh mát, cơm dày. Mỗi quả giao nguyên trái.", new java.math.BigDecimal("15000.0"), new java.math.BigDecimal("15000.0"), 200, "QUẢ", "https://images.unsplash.com/photo-1536825919195-6ae26a4416da?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO products (id, category_id, name, description, original_price, selling_price, stock_quantity, unit, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "11111111-1111-1111-1111-111111110016", "22222222-2222-2222-2222-222222220004", "Sữa tươi TH True Milk 1L", "Sữa tươi tiệt trùng TH True Milk không đường, nguồn gốc rõ ràng, giàu canxi.", new java.math.BigDecimal("42000"), new java.math.BigDecimal("38000.0"), 500, "HỘP", "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO products (id, category_id, name, description, original_price, selling_price, stock_quantity, unit, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "11111111-1111-1111-1111-111111110017", "22222222-2222-2222-2222-222222220005", "Gạo ST25 ngon nhất thế giới", "Gạo ST25 đoạt giải gạo ngon nhất thế giới, hạt dài, mềm dẻo, thơm cơm. Từ Sóc Trăng.", new java.math.BigDecimal("95000"), new java.math.BigDecimal("85000.0"), 200, "KG", "https://images.unsplash.com/photo-1536304993881-ff6e9eefa2a6?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO products (id, category_id, name, description, original_price, selling_price, stock_quantity, unit, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "11111111-1111-1111-1111-111111110018", "22222222-2222-2222-2222-222222220005", "Nấm đông cô khô Đà Lạt", "Nấm đông cô khô nguyên tai Đà Lạt, thơm đậm đà, không phụ gia. Thích hợp nấu cháo, xào.", new java.math.BigDecimal("145000"), new java.math.BigDecimal("120000.0"), 50, "GÓI 200G", "https://images.unsplash.com/photo-1632559004598-4d4ee3aa9b4b?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO products (id, category_id, name, description, original_price, selling_price, stock_quantity, unit, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "11111111-1111-1111-1111-111111110019", "22222222-2222-2222-2222-222222220006", "Tỏi tím Lý Sơn đặc sản", "Tỏi tím Lý Sơn Quảng Ngãi, hạt nhỏ đều, vị cay nồng đặc trưng. Sạch, không hóa chất.", new java.math.BigDecimal("80000"), new java.math.BigDecimal("65000.0"), 30, "KG", "https://images.unsplash.com/photo-1540148426945-6cf22a6b2383?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO products (id, category_id, name, description, original_price, selling_price, stock_quantity, unit, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "11111111-1111-1111-1111-111111110020", "22222222-2222-2222-2222-222222220006", "Ớt hiểm xanh tươi", "Ớt hiểm xanh cay nồng, thích hợp làm nước mắm, kho thịt hoặc chế biến ăn liền.", new java.math.BigDecimal("45000.0"), new java.math.BigDecimal("45000.0"), 25, "KG", "https://images.unsplash.com/photo-1588167056547-c183313da369?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO products (id, category_id, name, description, original_price, selling_price, stock_quantity, unit, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "11111111-1111-1111-1111-111111110021", "22222222-2222-2222-2222-222222220007", "Bánh mì sandwich Hà Nội", "Bánh mì sandwich nướng tươi mỗi ngày, vỏ giòn ruột mềm. Không chất bảo quản.", new java.math.BigDecimal("30000"), new java.math.BigDecimal("25000.0"), 100, "Ổ", "https://images.unsplash.com/photo-1589367920969-ab8e050bbb04?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO products (id, category_id, name, description, original_price, selling_price, stock_quantity, unit, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "11111111-1111-1111-1111-111111110022", "22222222-2222-2222-2222-222222220008", "Mực ống đông lạnh Phú Quốc", "Mực ống Phú Quốc đông lạnh IQF, con đều, thịt trắng ngọt. Cấp đông ngay sau thu hoạch.", new java.math.BigDecimal("220000"), new java.math.BigDecimal("185000.0"), 15, "KG", "https://images.unsplash.com/photo-1559628376-f3fe5f782a2e?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO products (id, category_id, name, description, original_price, selling_price, stock_quantity, unit, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "11111111-1111-1111-1111-111111110023", "22222222-2222-2222-2222-222222220001", "Cải ngọt Đà Lạt", "Cải ngọt Đà Lạt non mướt, ngọt tự nhiên, không thuốc trừ sâu. Thích hợp xào tỏi.", new java.math.BigDecimal("18000.0"), new java.math.BigDecimal("18000.0"), 60, "BÓ", "https://images.unsplash.com/photo-1519996529931-28324d5a630e?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO products (id, category_id, name, description, original_price, selling_price, stock_quantity, unit, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "11111111-1111-1111-1111-111111110024", "22222222-2222-2222-2222-222222220002", "Bơ sáp 034 Đắk Lắk", "Bơ sáp 034 Đắk Lắk cơm dày, hạt nhỏ, béo ngậy thơm ngon. Chín đúng độ, không xơ.", new java.math.BigDecimal("65000"), new java.math.BigDecimal("52000.0"), 20, "KG", "https://images.unsplash.com/photo-1519162808019-7de1683fa2ad?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            jdbcTemplate.update(
                "INSERT INTO products (id, category_id, name, description, original_price, selling_price, stock_quantity, unit, image_url, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "11111111-1111-1111-1111-111111110025", "22222222-2222-2222-2222-222222220001", "Khổ qua trái Đà Lạt", "Khổ qua (mướp đắng) hữu cơ trồng tại vườn Đà Lạt, thích hợp làm món canh khổ qua nhồi thịt giải nhiệt.", new java.math.BigDecimal("30000"), new java.math.BigDecimal("24000.0"), 30, "KG", "https://images.unsplash.com/photo-1589367920969-ab8e050bbb04?w=400", true, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)), java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS))
            );

            log.info(">> SEED: Đã tạo 25 sản phẩm tương ứng khớp với MockDataProvider bằng SQL");
        }
    }

    private void seedShipperOrders() {
        Shipper shipper = shipperRepository.findByPhone("0987654321").orElse(null);
        User customer = userRepository.findByEmail("customer@gmail.com").orElse(null);
        Product product = productRepository.findById(UUID.fromString("11111111-1111-1111-1111-111111110001"))
                .orElseGet(() -> productRepository.findAll().stream().findFirst().orElse(null));
        if (shipper == null || customer == null || product == null) {
            return;
        }
        if (orderRepository.findByShipperIdOrderByCreatedAtDesc(shipper.getId(), PageRequest.of(0, 3)).getNumberOfElements() >= 3) {
            return;
        }

        seedOrder(customer, shipper, product, Order.OrderStatus.SHIPPING, Order.PaymentStatus.UNPAID, "Seed: Admin da gan shipper");
        seedOrder(customer, shipper, product, Order.OrderStatus.IN_TRANSIT, Order.PaymentStatus.UNPAID, "Seed: Shipper dang giao");
        seedOrder(customer, shipper, product, Order.OrderStatus.DELIVERED, Order.PaymentStatus.PAID, "Seed: Da giao thanh cong");
        log.info(">> SEED: Da tao 3 don hang mau cho shipper@gmail.com / Shipper@123");
    }

    private void seedOrder(User customer, Shipper shipper, Product product,
                           Order.OrderStatus status, Order.PaymentStatus paymentStatus, String note) {
        BigDecimal unitPrice = product.getSellingPrice();
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(2));
        Order order = Order.builder()
                .user(customer)
                .shipper(shipper)
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
                .order(order)
                .product(product)
                .quantity(2)
                .unitPrice(unitPrice)
                .subtotal(subtotal)
                .build();
        order.getOrderItems().add(item);
        orderRepository.save(order);
    }
}
