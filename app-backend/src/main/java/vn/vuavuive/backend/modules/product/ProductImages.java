package vn.vuavuive.backend.modules.product;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ProductImages {
    private ProductImages() {}

    public static boolean needsBackfill(Product product) {
        return product != null && clean(product.getImages()).size() <= 1;
    }

    public static List<String> withFallback(Product product) {
        List<String> images = clean(product != null ? product.getImages() : null);
        String main = product != null ? product.getImageUrl() : null;
        if (notBlank(main) && !images.contains(main)) {
            images.add(0, main);
        }
        if (product == null || images.size() >= 4) {
            return images;
        }

        for (String url : candidates(product)) {
            if (!images.contains(url)) {
                images.add(url);
            }
            if (images.size() >= 4) {
                break;
            }
        }
        return images;
    }

    private static List<String> clean(List<String> raw) {
        List<String> result = new ArrayList<>();
        if (raw == null) {
            return result;
        }
        for (String url : raw) {
            if (notBlank(url) && !result.contains(url)) {
                result.add(url);
            }
        }
        return result;
    }

    private static List<String> candidates(Product product) {
        String text = normalize((product.getName() == null ? "" : product.getName()) + " "
                + (product.getSubCategory() == null ? "" : product.getSubCategory()) + " "
                + (product.getTags() == null ? "" : product.getTags().toString()));

        if (text.contains("tomato") || text.contains("chua")) {
            return List.of(
                    "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=800",
                    "https://images.unsplash.com/photo-1561136594-7f68413baa99?w=800",
                    "https://images.unsplash.com/photo-1571680322279-a226e6a4cc2a?w=800"
            );
        }
        if (text.contains("khoai") || text.contains("potato")) {
            return List.of(
                    "https://images.unsplash.com/photo-1518977822534-7049a61ee0c2?w=800",
                    "https://images.unsplash.com/photo-1590165482129-1b8b27698780?w=800",
                    "https://images.unsplash.com/photo-1603048719539-9ecb4aa395e3?w=800"
            );
        }
        if (text.contains("carrot") || text.contains("rot")) {
            return List.of(
                    "https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=800",
                    "https://images.unsplash.com/photo-1445282768818-728615cc910a?w=800",
                    "https://images.unsplash.com/photo-1582515073490-39981397c445?w=800"
            );
        }
        if (text.contains("mango") || text.contains("xoai")) {
            return List.of(
                    "https://images.unsplash.com/photo-1601493700631-2b16ec4b4716?w=800",
                    "https://images.unsplash.com/photo-1553279768-865429fa0078?w=800",
                    "https://images.unsplash.com/photo-1591073113125-e46713c829ed?w=800"
            );
        }
        if (text.contains("orange") || text.contains("cam")) {
            return List.of(
                    "https://images.unsplash.com/photo-1547514701-42782101795e?w=800",
                    "https://images.unsplash.com/photo-1582979512210-99b6a53386f9?w=800",
                    "https://images.unsplash.com/photo-1611080626919-7cf5a9dbab12?w=800"
            );
        }
        if (text.contains("chicken")) {
            return List.of(
                    "https://images.unsplash.com/photo-1604503468506-a8da13d82791?w=800",
                    "https://images.unsplash.com/photo-1587593810167-a84920ea0781?w=800",
                    "https://images.unsplash.com/photo-1610057099431-d73a1c9d2f2f?w=800"
            );
        }
        if (text.contains("meat") || text.contains("heo") || text.contains("thit")) {
            return List.of(
                    "https://images.unsplash.com/photo-1529692236671-f1f6cf9683ba?w=800",
                    "https://images.unsplash.com/photo-1607623814075-e51df1bdc82f?w=800",
                    "https://images.unsplash.com/photo-1603048297172-c92544798d5a?w=800"
            );
        }
        if (text.contains("shrimp") || text.contains("fish") || text.contains("salmon") || text.contains("tom") || text.contains("muc")) {
            return List.of(
                    "https://images.unsplash.com/photo-1565680018434-b513d5e5fd47?w=800",
                    "https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=800",
                    "https://images.unsplash.com/photo-1559737558-2f5a35f4523b?w=800"
            );
        }
        if (text.contains("rice") || text.contains("gao")) {
            return List.of(
                    "https://images.unsplash.com/photo-1536304993881-ff6e9eefa2a6?w=800",
                    "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=800",
                    "https://images.unsplash.com/photo-1604329760661-e71dc83f8f26?w=800"
            );
        }
        if (text.contains("bread") || text.contains("banh")) {
            return List.of(
                    "https://images.unsplash.com/photo-1589367920969-ab8e050bbb04?w=800",
                    "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=800",
                    "https://images.unsplash.com/photo-1549931319-a545dcf3bc73?w=800"
            );
        }
        return List.of(
                "https://images.unsplash.com/photo-1542838132-92c53300491e?w=800",
                "https://images.unsplash.com/photo-1518843875459-f738682238a6?w=800",
                "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=800"
        );
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT);
    }
}
