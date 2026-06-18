package vn.vuavuive.backend.modules.cart;

import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart/me")
public class CartController {

    @GetMapping
    public Map<String, Object> getCart() {
        return emptyCart();
    }

    @PutMapping
    public Map<String, Object> syncCart(@RequestBody Map<String, Object> body) {
        return cartFrom(body);
    }

    @PostMapping("/merge")
    public Map<String, Object> mergeCart(@RequestBody Map<String, Object> body) {
        return cartFrom(body);
    }

    @DeleteMapping
    public Void clearCart() {
        return null;
    }

    private Map<String, Object> cartFrom(Map<String, Object> body) {
        return Map.of(
                "items", body.getOrDefault("items", List.of()),
                "savedForLater", body.getOrDefault("savedForLater", List.of()),
                "updatedAt", Instant.now().toString()
        );
    }

    private Map<String, Object> emptyCart() {
        return Map.of(
                "items", List.of(),
                "savedForLater", List.of(),
                "updatedAt", Instant.now().toString()
        );
    }
}
