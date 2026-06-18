package vn.vuavuive.backend.modules.recommend;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    @PostMapping
    public List<Object> recommendations(@RequestBody(required = false) Map<String, Object> body) {
        return List.of();
    }

    @PostMapping("/event")
    public Void event(@RequestBody(required = false) Map<String, Object> body) {
        return null;
    }

    @GetMapping("/similar/{id}")
    public List<Object> similar(@PathVariable String id) {
        return List.of();
    }

    @PostMapping("/similar-ml")
    public List<Object> similarMl(@RequestBody(required = false) Map<String, String> body) {
        return List.of();
    }

    @GetMapping("/history")
    public List<Map<String, Object>> history() {
        return List.of();
    }
}
