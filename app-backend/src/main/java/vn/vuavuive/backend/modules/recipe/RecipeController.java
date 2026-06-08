package vn.vuavuive.backend.modules.recipe;

import org.springframework.web.bind.annotation.*;
import vn.vuavuive.backend.core.ApiResponse;

import java.util.*;

@RestController
@RequestMapping("/api/recipes")
@CrossOrigin(origins = "*")
public class RecipeController {

    private final List<Map<String, Object>> recipes = new ArrayList<>();

    public RecipeController() {
        initRecipes();
    }

    private void initRecipes() {
        // 1. Canh khổ qua nhồi thịt
        Map<String, Object> r1 = new HashMap<>();
        r1.put("_id", "recipe_001");
        r1.put("name", "Canh khổ qua nhồi thịt");
        r1.put("description", "Món canh khổ qua thanh nhiệt, bổ dưỡng, kết hợp giữa thịt heo xay, mộc nhĩ nấm đông cô nhồi vào mướp đắng.");
        r1.put("image", "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=500");
        r1.put("category", "Món canh");
        r1.put("ingredients", Arrays.asList(
                Map.of("name", "Khổ qua trái Đà Lạt"),
                Map.of("name", "Thịt ba chỉ heo Sapa"),
                Map.of("name", "Nấm đông cô khô Đà Lạt")
        ));
        recipes.add(r1);

        // 2. Canh bắp cải thịt bằm
        Map<String, Object> r2 = new HashMap<>();
        r2.put("_id", "recipe_002");
        r2.put("name", "Canh bắp cải thịt bằm");
        r2.put("description", "Canh bắp cải nấu thịt băm là món canh quen thuộc, ngọt mát và cực dễ làm cho bữa cơm gia đình hàng ngày.");
        r2.put("image", "https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r2.put("category", "Món canh");
        r2.put("ingredients", Arrays.asList(
                Map.of("name", "Bắp cải xanh Đà Lạt"),
                Map.of("name", "Thịt ba chỉ heo Sapa"),
                Map.of("name", "Tỏi tím Lý Sơn đặc sản")
        ));
        recipes.add(r2);

        // 3. Canh cải ngọt nấu tôm
        Map<String, Object> r3 = new HashMap<>();
        r3.put("_id", "recipe_003");
        r3.put("name", "Canh cải ngọt nấu tôm");
        r3.put("description", "Vị ngọt thanh từ tôm sú tươi kết hợp với cải ngọt giòn mát tạo nên một món canh thơm ngon, dễ ăn.");
        r3.put("image", "https://images.unsplash.com/photo-1547928507-6c9b3c374f88?w=500");
        r3.put("category", "Món canh");
        r3.put("ingredients", Arrays.asList(
                Map.of("name", "Cải ngọt Đà Lạt"),
                Map.of("name", "Tôm sú tươi Cà Mau")
        ));
        recipes.add(r3);

        // 4. Cải ngọt xào tỏi
        Map<String, Object> r4 = new HashMap<>();
        r4.put("_id", "recipe_004");
        r4.put("name", "Cải ngọt xào tỏi");
        r4.put("description", "Món xào đơn giản, giữ trọn vị giòn ngọt của cải xanh kết hợp với tỏi phi thơm lừng.");
        r4.put("image", "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=500");
        r4.put("category", "Xào, luộc");
        r4.put("ingredients", Arrays.asList(
                Map.of("name", "Cải ngọt Đà Lạt"),
                Map.of("name", "Tỏi tím Lý Sơn đặc sản"),
                Map.of("name", "Ớt hiểm xanh tươi")
        ));
        recipes.add(r4);

        // 5. Ớt chuông xào ba chỉ
        Map<String, Object> r5 = new HashMap<>();
        r5.put("_id", "recipe_005");
        r5.put("name", "Ớt chuông xào ba chỉ");
        r5.put("description", "Ớt chuông giòn ngọt giàu Vitamin C xào cùng thịt ba chỉ heo ngậy thơm, rất bắt cơm.");
        r5.put("image", "https://images.unsplash.com/photo-1563565375-f3fdfdbefa83?w=500");
        r5.put("category", "Xào, luộc");
        r5.put("ingredients", Arrays.asList(
                Map.of("name", "Ớt chuông 3 màu"),
                Map.of("name", "Thịt ba chỉ heo Sapa"),
                Map.of("name", "Tỏi tím Lý Sơn đặc sản")
        ));
        recipes.add(r5);

        // 6. Tôm sú rim thịt ba chỉ
        Map<String, Object> r6 = new HashMap<>();
        r6.put("_id", "recipe_006");
        r6.put("name", "Tôm sú rim thịt ba chỉ");
        r6.put("description", "Món ăn mặn đậm đà, thịt ba chỉ cháy cạnh béo ngậy quấn quyện cùng tôm sú giòn ngọt.");
        r6.put("image", "https://images.unsplash.com/photo-1565680018434-b513d5e5fd47?w=500");
        r6.put("category", "Món mặn");
        r6.put("ingredients", Arrays.asList(
                Map.of("name", "Tôm sú tươi Cà Mau"),
                Map.of("name", "Thịt ba chỉ heo Sapa"),
                Map.of("name", "Tỏi tím Lý Sơn đặc sản")
        ));
        recipes.add(r6);

        // 7. Cá hồi áp chảo sốt bơ
        Map<String, Object> r7 = new HashMap<>();
        r7.put("_id", "recipe_007");
        r7.put("name", "Cá hồi áp chảo sốt bơ");
        r7.put("description", "Cá hồi áp chảo chín tới, thơm lừng sốt bơ béo ngậy kết hợp ăn kèm quả bơ sáp tươi mát.");
        r7.put("image", "https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=500");
        r7.put("category", "Món mặn");
        r7.put("ingredients", Arrays.asList(
                Map.of("name", "Cá hồi Nauy phi lê"),
                Map.of("name", "Bơ sáp 034 Đắk Lắk")
        ));
        recipes.add(r7);
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getRecipes() {
        return ApiResponse.success(recipes);
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getRecipe(@PathVariable String id) {
        for (Map<String, Object> r : recipes) {
            if (id.equals(r.get("_id"))) {
                return ApiResponse.success(r);
            }
        }
        return ApiResponse.error(404, "Không tìm thấy công thức");
    }
}
