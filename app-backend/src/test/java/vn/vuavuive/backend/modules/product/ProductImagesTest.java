package vn.vuavuive.backend.modules.product;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductImagesTest {
    @Test
    void addsThreeFallbackImagesWhenProductHasOnlyMainImage() {
        Product product = Product.builder()
                .name("Cà rốt Đà Lạt")
                .imageUrl("https://example.com/main.jpg")
                .images(List.of("https://example.com/main.jpg"))
                .build();

        List<String> images = ProductImages.withFallback(product);

        assertEquals(4, images.size());
        assertEquals("https://example.com/main.jpg", images.get(0));
        assertEquals("https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=800", images.get(1));
        assertEquals(4, images.stream().distinct().count());
    }
}
