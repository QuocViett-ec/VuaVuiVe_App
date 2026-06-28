package vn.vuavuive.customer.data;

import vn.vuavuive.shared.data.dto.Product;
import vn.vuavuive.shared.data.dto.Review;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * MockDataProvider - provides static mock data for frontend display purposes.
 * No API or database connection is used.
 */
public class MockDataProvider {

    // ─── Mock Categories ───────────────────────────────────────────────────────
    public static final String[][] CATEGORIES = {
            {"all",    "🛒 Tất cả"},
            {"veg",    "🥦 Rau củ"},
            {"fruit",  "🍎 Trái cây"},
            {"meat",   "🥩 Thịt"},
            {"drink",  "🥤 Đồ uống"},
            {"dry",    "🌾 Đồ khô"},
            {"spice",  "🌶️ Gia vị"},
            {"sweet",  "🍰 Bánh kẹo"},
            {"frozen", "❄️ Đông lạnh"}
    };

    // ─── Mock Products ─────────────────────────────────────────────────────────
    public static List<Product> getMockProducts() {
        List<Product> products = new ArrayList<>();

        // --- Rau củ (veg) ---
        products.add(createProduct("11111111-1111-1111-1111-111111110001", "Cải thảo tươi Đà Lạt", "veg",
                15000, 20000.0D, "kg", 4.7, 234, 1820, 45,
                "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=400",
                "Cải thảo tươi ngon từ Đà Lạt, giàu vitamin C và chất xơ. Thu hoạch mỗi ngày, đảm bảo độ tươi ngon tối đa.",
                Arrays.asList("rau", "tươi", "đà lạt", "hữu cơ")));

        products.add(createProduct("11111111-1111-1111-1111-111111110002", "Cà chua bi đỏ", "veg",
                25000, 32000.0D, "kg", 4.8, 312, 2100, 60,
                "https://images.unsplash.com/photo-1546094096-0df4bcaaa337?w=400",
                "Cà chua bi đỏ Đà Lạt, vị ngọt thanh, giàu lycopene và vitamin. Lý tưởng cho salad và nấu ăn.",
                Arrays.asList("cà chua", "bi", "đỏ", "đà lạt")));

        products.add(createProduct("11111111-1111-1111-1111-111111110003", "Bắp cải xanh Đà Lạt", "veg",
                12000, null, "kg", 4.5, 156, 980, 80,
                "https://images.unsplash.com/photo-1550828394-9f9b5dcd66e1?w=400",
                "Bắp cải xanh tươi ngon, giòn, thích hợp xào, luộc hoặc làm kim chi.",
                Arrays.asList("bắp cải", "xanh", "tươi")));

        products.add(createProduct("11111111-1111-1111-1111-111111110004", "Khoai tây Đà Lạt", "veg",
                22000, 28000.0D, "kg", 4.6, 445, 3200, 120,
                "https://images.unsplash.com/photo-1518977822534-7049a61ee0c2?w=400",
                "Khoai tây Đà Lạt loại 1, không thuốc bảo quản, phù hợp chiên, nấu canh hoặc nghiền.",
                Arrays.asList("khoai tây", "đà lạt", "tươi")));

        products.add(createProduct("11111111-1111-1111-1111-111111110005", "Cà rốt nhập khẩu Úc", "veg",
                35000, 42000.0D, "kg", 4.9, 287, 1560, 40,
                "https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=400",
                "Cà rốt nhập khẩu Úc, size đều, ngọt, giàu beta-carotene và vitamin A. Rất tốt cho mắt.",
                Arrays.asList("cà rốt", "úc", "nhập khẩu", "hữu cơ")));

        products.add(createProduct("11111111-1111-1111-1111-111111110006", "Ớt chuông 3 màu", "veg",
                55000, 65000.0D, "túi 500g", 4.8, 198, 920, 25,
                "https://images.unsplash.com/photo-1563565375-f3fdfdbefa83?w=400",
                "Ớt chuông đỏ-vàng-xanh tươi ngon, không cay, thích hợp xào hoặc làm salad. Combo tiện lợi.",
                Arrays.asList("ớt chuông", "3 màu", "xào", "salad")));

        // --- Trái cây (fruit) ---
        products.add(createProduct("11111111-1111-1111-1111-111111110007", "Xoài cát Hòa Lộc", "fruit",
                45000, 58000.0D, "kg", 4.9, 521, 4200, 30,
                "https://images.unsplash.com/photo-1601493700631-2b16ec4b4716?w=400",
                "Xoài cát Hòa Lộc thơm ngon, ngọt dịu, hạt nhỏ thịt nhiều. Đặc sản miền Tây chính hiệu.",
                Arrays.asList("xoài", "cát", "hòa lộc", "miền tây")));

        products.add(createProduct("11111111-1111-1111-1111-111111110008", "Thanh long đỏ Bình Thuận", "fruit",
                38000, 48000.0D, "kg", 4.7, 376, 2800, 50,
                "https://images.unsplash.com/photo-1527325678964-54921661f888?w=400",
                "Thanh long đỏ ruột đỏ Bình Thuận, ngọt mát, giàu chất chống oxy hóa. VietGAP.",
                Arrays.asList("thanh long", "đỏ", "bình thuận", "vietgap")));

        products.add(createProduct("11111111-1111-1111-1111-111111110009", "Cam sành Vĩnh Long", "fruit",
                28000, null, "kg", 4.8, 489, 5100, 100,
                "https://images.unsplash.com/photo-1547514701-42782101795e?w=400",
                "Cam sành Vĩnh Long múi vàng óng, nước nhiều, vị ngọt chua thanh tao. Tươi hái mỗi ngày.",
                Arrays.asList("cam", "sành", "vĩnh long")));

        products.add(createProduct("11111111-1111-1111-1111-111111110010", "Dứa mật Ninh Bình", "fruit",
                25000, 32000.0D, "quả", 4.6, 203, 1230, 35,
                "https://images.unsplash.com/photo-1550258987-190a2d41a8ba?w=400",
                "Dứa mật Ninh Bình đặc biệt ngọt, thơm, mắt nông dễ gọt. Dùng ăn tươi hoặc làm sinh tố.",
                Arrays.asList("dứa", "mật", "ninh bình")));

        // --- Thịt (meat) ---
        products.add(createProduct("11111111-1111-1111-1111-111111110011", "Thịt ba chỉ heo Sapa", "meat",
                150000, 175000.0D, "kg", 4.8, 312, 1890, 20,
                "https://images.unsplash.com/photo-1529692236671-f1f6cf9683ba?w=400",
                "Thịt ba chỉ heo đồi Sapa, nạc mỡ cân đối, thịt chắc và thơm. Thích hợp kho, quay hoặc nướng BBQ.",
                Arrays.asList("thịt", "ba chỉ", "heo", "sapa")));

        products.add(createProduct("11111111-1111-1111-1111-111111110012", "Ức gà tươi organic", "meat",
                95000, 115000.0D, "kg", 4.9, 445, 3200, 15,
                "https://images.unsplash.com/photo-1604503468506-a8da13d82791?w=400",
                "Ức gà tươi nuôi thả vườn, không hormon, giàu protein. Phù hợp tập gym, ăn kiêng lành mạnh.",
                Arrays.asList("ức gà", "organic", "tươi", "protein")));

        products.add(createProduct("11111111-1111-1111-1111-111111110013", "Tôm sú tươi Cà Mau", "meat",
                280000, 320000.0D, "kg", 4.9, 267, 980, 8,
                "https://images.unsplash.com/photo-1565680018434-b513d5e5fd47?w=400",
                "Tôm sú Cà Mau size 20-25 con/kg, con to đều, thịt chắc ngọt. Thu mua trực tiếp từ ao nuôi.",
                Arrays.asList("tôm", "sú", "cà mau", "hải sản")));

        products.add(createProduct("11111111-1111-1111-1111-111111110014", "Cá hồi Nauy phi lê", "meat",
                320000, 380000.0D, "kg", 4.8, 198, 756, 12,
                "https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=400",
                "Cá hồi Nauy phi lê tươi, không gai, giàu Omega-3. Thích hợp áp chảo, sashimi hoặc nướng.",
                Arrays.asList("cá hồi", "nauy", "phi lê", "omega-3")));

        // --- Đồ uống (drink) ---
        products.add(createProduct("11111111-1111-1111-1111-111111110015", "Nước dừa tươi Bến Tre", "drink",
                15000, null, "quả", 4.7, 623, 8900, 200,
                "https://images.unsplash.com/photo-1536825919195-6ae26a4416da?w=400",
                "Dừa tươi Bến Tre đặc sản, nước ngọt thanh mát, cơm dày. Mỗi quả giao nguyên trái.",
                Arrays.asList("dừa", "tươi", "bến tre", "giải khát")));

        products.add(createProduct("11111111-1111-1111-1111-111111110016", "Sữa tươi TH True Milk 1L", "drink",
                38000, 42000.0D, "hộp", 4.8, 892, 12000, 500,
                "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=400",
                "Sữa tươi tiệt trùng TH True Milk không đường, nguồn gốc rõ ràng, giàu canxi.",
                Arrays.asList("sữa", "th true milk", "tiệt trùng")));

        // --- Đồ khô (dry) ---
        products.add(createProduct("11111111-1111-1111-1111-111111110017", "Gạo ST25 ngon nhất thế giới", "dry",
                85000, 95000.0D, "kg", 4.9, 1245, 18000, 200,
                "https://images.unsplash.com/photo-1536304993881-ff6e9eefa2a6?w=400",
                "Gạo ST25 đoạt giải gạo ngon nhất thế giới, hạt dài, mềm dẻo, thơm cơm. Từ Sóc Trăng.",
                Arrays.asList("gạo", "st25", "sóc trăng", "ngon nhất")));

        products.add(createProduct("11111111-1111-1111-1111-111111110018", "Nấm đông cô khô Đà Lạt", "dry",
                120000, 145000.0D, "gói 200g", 4.7, 234, 1450, 50,
                "https://images.unsplash.com/photo-1632559004598-4d4ee3aa9b4b?w=400",
                "Nấm đông cô khô nguyên tai Đà Lạt, thơm đậm đà, không phụ gia. Thích hợp nấu cháo, xào.",
                Arrays.asList("nấm", "đông cô", "khô", "đà lạt")));

        // --- Gia vị (spice) ---
        products.add(createProduct("11111111-1111-1111-1111-111111110019", "Tỏi tím Lý Sơn đặc sản", "spice",
                65000, 80000.0D, "kg", 4.9, 567, 4500, 30,
                "https://images.unsplash.com/photo-1540148426945-6cf22a6b2383?w=400",
                "Tỏi tím Lý Sơn Quảng Ngãi, hạt nhỏ đều, vị cay nồng đặc trưng. Sạch, không hóa chất.",
                Arrays.asList("tỏi", "tím", "lý sơn", "đặc sản")));

        products.add(createProduct("11111111-1111-1111-1111-111111110020", "Ớt hiểm xanh tươi", "spice",
                45000, null, "kg", 4.6, 187, 1200, 25,
                "https://images.unsplash.com/photo-1588167056547-c183313da369?w=400",
                "Ớt hiểm xanh cay nồng, thích hợp làm nước mắm, kho thịt hoặc chế biến ăn liền.",
                Arrays.asList("ớt hiểm", "xanh", "cay", "gia vị")));

        // --- Bánh kẹo (sweet) ---
        products.add(createProduct("11111111-1111-1111-1111-111111110021", "Bánh mì sandwich Hà Nội", "sweet",
                25000, 30000.0D, "ổ", 4.5, 312, 2800, 100,
                "https://images.unsplash.com/photo-1589367920969-ab8e050bbb04?w=400",
                "Bánh mì sandwich nướng tươi mỗi ngày, vỏ giòn ruột mềm. Không chất bảo quản.",
                Arrays.asList("bánh mì", "sandwich", "nướng", "tươi")));

        // --- Đông lạnh (frozen) ---
        products.add(createProduct("11111111-1111-1111-1111-111111110022", "Mực ống đông lạnh Phú Quốc", "frozen",
                185000, 220000.0D, "kg", 4.7, 178, 890, 15,
                "https://images.unsplash.com/photo-1559628376-f3fe5f782a2e?w=400",
                "Mực ống Phú Quốc đông lạnh IQF, con đều, thịt trắng ngọt. Cấp đông ngay sau thu hoạch.",
                Arrays.asList("mực", "ống", "đông lạnh", "phú quốc")));

        products.add(createProduct("11111111-1111-1111-1111-111111110023", "Cải ngọt Đà Lạt", "veg",
                18000, null, "bó", 4.6, 143, 750, 60,
                "https://images.unsplash.com/photo-1519996529931-28324d5a630e?w=400",
                "Cải ngọt Đà Lạt non mướt, ngọt tự nhiên, không thuốc trừ sâu. Thích hợp xào tỏi.",
                Arrays.asList("cải ngọt", "đà lạt", "hữu cơ")));

        products.add(createProduct("11111111-1111-1111-1111-111111110024", "Bơ sáp 034 Đắk Lắk", "fruit",
                52000, 65000.0D, "kg", 4.9, 412, 3100, 20,
                "https://images.unsplash.com/photo-1519162808019-7de1683fa2ad?w=400",
                "Bơ sáp 034 Đắk Lắk cơm dày, hạt nhỏ, béo ngậy thơm ngon. Chín đúng độ, không xơ.",
                Arrays.asList("bơ", "sáp", "034", "đắk lắk")));

        products.add(createProduct("11111111-1111-1111-1111-111111110025", "Khổ qua trái Đà Lạt", "veg",
                24000, 30000.0D, "kg", 4.7, 98, 450, 30,
                "https://images.unsplash.com/photo-1589367920969-ab8e050bbb04?w=400",
                "Khổ qua (mướp đắng) hữu cơ trồng tại vườn Đà Lạt, thích hợp làm món canh khổ qua nhồi thịt giải nhiệt.",
                Arrays.asList("khổ qua", "mướp đắng", "đà lạt", "rau")));

        return products;
    }

    // ─── Featured/Recommended products ────────────────────────────────────────
    public static List<Product> getMockFeaturedProducts() {
        List<Product> all = getMockProducts();
        // Return top-rated products
        return all.subList(0, Math.min(8, all.size()));
    }

    // ─── Trending/Sale products ────────────────────────────────────────────────
    public static List<Product> getMockSaleProducts() {
        List<Product> all = getMockProducts();
        List<Product> sales = new ArrayList<>();
        for (Product p : all) {
            if (p.getOriginalPrice() != null && p.getOriginalPrice() > p.getPrice()) {
                sales.add(p);
            }
        }
        return sales;
    }

    // ─── Products by category ──────────────────────────────────────────────────
    public static List<Product> getMockProductsByCategory(String category) {
        if (category == null || "all".equals(category)) {
            return getMockProducts();
        }
        List<Product> result = new ArrayList<>();
        for (Product p : getMockProducts()) {
            if (category.equals(p.getCategory())) {
                result.add(p);
            }
        }
        return result;
    }

    // ─── Search products ───────────────────────────────────────────────────────
    public static List<Product> searchMockProducts(String query) {
        if (query == null || query.isEmpty()) return getMockProducts();
        String q = query.toLowerCase().trim();
        List<Product> result = new ArrayList<>();
        for (Product p : getMockProducts()) {
            if (p.getName().toLowerCase().contains(q)
                    || (p.getDescription() != null && p.getDescription().toLowerCase().contains(q))
                    || (p.getTags() != null && p.getTags().stream().anyMatch(t -> t.toLowerCase().contains(q)))) {
                result.add(p);
            }
        }
        return result;
    }

    // ─── Mock Reviews ──────────────────────────────────────────────────────────
    public static List<Review> getMockReviews(String productId) {
        List<Review> reviews = new ArrayList<>();

        String[][] reviewData = {
                {"user001", "Nguyễn Minh Tuấn", "5", "Sản phẩm tươi ngon tuyệt vời! Giao hàng nhanh, đóng gói cẩn thận. Sẽ mua lại lần sau. ⭐⭐⭐⭐⭐", "2026-05-20"},
                {"user002", "Trần Thị Lan", "4", "Chất lượng tốt, tươi ngon. Giá hơi cao nhưng xứng đáng. Shipper thân thiện.", "2026-05-18"},
                {"user003", "Lê Văn Bình", "4", "Nhìn chung ổn, sẽ mua lại. Đóng gói sạch sẽ, hàng đúng mô tả.", "2026-05-15"},
                {"user004", "Phạm Thu Hương", "5", "Ngon quá!! Mua về gia đình ai cũng khen. Hàng tươi, sạch, giao đúng hẹn 👍", "2026-05-12"},
                {"user005", "Võ Quang Khải", "5", "Sản phẩm chất lượng cao. Giao hàng nhanh trong ngày. Recommended!", "2026-05-10"}
        };

        for (String[] r : reviewData) {
            Review review = new Review();
            review.setUserId(r[0]);
            review.setUserName(r[1]);
            review.setRating(Integer.parseInt(r[2]));
            review.setComment(r[3]);
            review.setProductId(productId);
            review.setCreatedAt(r[4]);
            reviews.add(review);
        }
        return reviews;
    }

    public static Product getMockProductById(String productId) {
        for (Product p : getMockProducts()) {
            if (productId != null && productId.equals(p.getId())) return p;
        }
        return null;
    }

    // ─── Helper: Create Product ────────────────────────────────────────────────
    private static Product createProduct(String id, String name, String category,
                                          double price, Double originalPrice,
                                          String unit, double rating, int reviewCount,
                                          int soldCount, int stock,
                                          String imageUrl, String description,
                                          List<String> tags) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setCategory(category);
        p.setPrice(price);
        p.setOriginalPrice(originalPrice);
        p.setUnit(unit);
        p.setRating(rating);
        p.setReviewCount(reviewCount);
        p.setSoldCount(soldCount);
        p.setStock(stock);
        p.setImageUrl(imageUrl);
        p.setDescription(description);
        p.setTags(tags);
        p.setActive(true);
        return p;
    }

    public static List<java.util.Map<String, Object>> getMockRecipes() {
        List<java.util.Map<String, Object>> recipes = new ArrayList<>();

        // 1. Canh khổ qua nhồi thịt
        java.util.Map<String, Object> r1 = new java.util.HashMap<>();
        r1.put("_id", "recipe_001");
        r1.put("name", "Canh khổ qua nhồi thịt");
        r1.put("description", "Món canh khổ qua thanh nhiệt, bổ dưỡng, kết hợp giữa thịt heo xay, mộc nhĩ nấm đông cô nhồi vào mướp đắng.");
        r1.put("image", "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=500");
        r1.put("category", "Món canh");
        r1.put("ingredients", Arrays.asList(
                java.util.Map.of("name", "Khổ qua trái Đà Lạt"),
                java.util.Map.of("name", "Thịt ba chỉ heo Sapa"),
                java.util.Map.of("name", "Nấm đông cô khô Đà Lạt")
        ));
        recipes.add(r1);

        // 2. Canh bắp cải thịt bằm
        java.util.Map<String, Object> r2 = new java.util.HashMap<>();
        r2.put("_id", "recipe_002");
        r2.put("name", "Canh bắp cải thịt bằm");
        r2.put("description", "Canh bắp cải nấu thịt băm là món canh quen thuộc, ngọt mát và cực dễ làm cho bữa cơm gia đình hàng ngày.");
        r2.put("image", "https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r2.put("category", "Món canh");
        r2.put("ingredients", Arrays.asList(
                java.util.Map.of("name", "Bắp cải xanh Đà Lạt"),
                java.util.Map.of("name", "Thịt ba chỉ heo Sapa"),
                java.util.Map.of("name", "Tỏi tím Lý Sơn đặc sản")
        ));
        recipes.add(r2);

        // 3. Canh cải ngọt nấu tôm
        java.util.Map<String, Object> r3 = new java.util.HashMap<>();
        r3.put("_id", "recipe_003");
        r3.put("name", "Canh cải ngọt nấu tôm");
        r3.put("description", "Vị ngọt thanh từ tôm sú tươi kết hợp với cải ngọt giòn mát tạo nên một món canh thơm ngon, dễ ăn.");
        r3.put("image", "https://images.unsplash.com/photo-1547928507-6c9b3c374f88?w=500");
        r3.put("category", "Món canh");
        r3.put("ingredients", Arrays.asList(
                java.util.Map.of("name", "Cải ngọt Đà Lạt"),
                java.util.Map.of("name", "Tôm sú tươi Cà Mau")
        ));
        recipes.add(r3);

        // 4. Cải ngọt xào tỏi
        java.util.Map<String, Object> r4 = new java.util.HashMap<>();
        r4.put("_id", "recipe_004");
        r4.put("name", "Cải ngọt xào tỏi");
        r4.put("description", "Món xào đơn giản, giữ trọn vị giòn ngọt của cải xanh kết hợp với tỏi phi thơm lừng.");
        r4.put("image", "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=500");
        r4.put("category", "Xào, luộc");
        r4.put("ingredients", Arrays.asList(
                java.util.Map.of("name", "Cải ngọt Đà Lạt"),
                java.util.Map.of("name", "Tỏi tím Lý Sơn đặc sản"),
                java.util.Map.of("name", "Ớt hiểm xanh tươi")
        ));
        recipes.add(r4);

        // 5. Ớt chuông xào ba chỉ
        java.util.Map<String, Object> r5 = new java.util.HashMap<>();
        r5.put("_id", "recipe_005");
        r5.put("name", "Ớt chuông xào ba chỉ");
        r5.put("description", "Ớt chuông giòn ngọt giàu Vitamin C xào cùng thịt ba chỉ heo ngậy thơm, rất bắt cơm.");
        r5.put("image", "https://images.unsplash.com/photo-1563565375-f3fdfdbefa83?w=500");
        r5.put("category", "Xào, luộc");
        r5.put("ingredients", Arrays.asList(
                java.util.Map.of("name", "Ớt chuông 3 màu"),
                java.util.Map.of("name", "Thịt ba chỉ heo Sapa"),
                java.util.Map.of("name", "Tỏi tím Lý Sơn đặc sản")
        ));
        recipes.add(r5);

        // 6. Tôm sú rim thịt ba chỉ
        java.util.Map<String, Object> r6 = new java.util.HashMap<>();
        r6.put("_id", "recipe_006");
        r6.put("name", "Tôm sú rim thịt ba chỉ");
        r6.put("description", "Món ăn mặn đậm đà, thịt ba chỉ cháy cạnh béo ngậy quấn quyện cùng tôm sú giòn ngọt.");
        r6.put("image", "https://images.unsplash.com/photo-1565680018434-b513d5e5fd47?w=500");
        r6.put("category", "Món mặn");
        r6.put("ingredients", Arrays.asList(
                java.util.Map.of("name", "Tôm sú tươi Cà Mau"),
                java.util.Map.of("name", "Thịt ba chỉ heo Sapa"),
                java.util.Map.of("name", "Tỏi tím Lý Sơn đặc sản")
        ));
        recipes.add(r6);

        // 7. Cá hồi áp chảo sốt bơ
        java.util.Map<String, Object> r7 = new java.util.HashMap<>();
        r7.put("_id", "recipe_007");
        r7.put("name", "Cá hồi áp chảo sốt bơ");
        r7.put("description", "Cá hồi áp chảo chín tới, thơm lừng sốt bơ béo ngậy kết hợp ăn kèm quả bơ sáp tươi mát.");
        r7.put("image", "https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=500");
        r7.put("category", "Món mặn");
        r7.put("ingredients", Arrays.asList(
                java.util.Map.of("name", "Cá hồi Nauy phi lê"),
                java.util.Map.of("name", "Bơ sáp 034 Đắk Lắk")
        ));
        recipes.add(r7);

        return recipes;
    }
}
