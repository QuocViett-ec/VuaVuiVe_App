package vn.vuavuive.backend.modules.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/export")
public class ExportController {

    public record ExportRequest(
            String filename,
            String content
    ) {}

    @PostMapping
    public ResponseEntity<Map<String, Object>> exportCsv(@RequestBody ExportRequest request) {
        log.info("Yêu cầu ghi file báo cáo: {}", request.filename());
        Map<String, Object> response = new HashMap<>();
        try {
            // Thư mục exports nằm ở root của project VuaVuiVe_App
            // Backend chạy từ app-backend/, do đó "../exports" sẽ chỉ đến thư mục exports ở root
            File exportsDir = new File("../exports");
            if (!exportsDir.exists()) {
                exportsDir.mkdirs();
            }

            File file = new File(exportsDir, request.filename());
            try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                writer.write(request.content());
            }

            log.info("Đã ghi file báo cáo thành công vào: {}", file.getAbsolutePath());
            response.put("success", true);
            response.put("path", file.getAbsolutePath());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi ghi file báo cáo", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
