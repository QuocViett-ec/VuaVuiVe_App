package vn.vuavuive.backend.modules.vision;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.vuavuive.backend.core.ApiResponse;
import vn.vuavuive.backend.exception.AppException;
import vn.vuavuive.backend.modules.ai.GeminiService;

import java.util.Base64;

@RestController
@RequestMapping("/api/vision")
@RequiredArgsConstructor
public class VisionController {

    private final GeminiService geminiService;

    @PostMapping(value = "/product-search", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<VisionSearchResponse>> productSearch(
            @RequestPart("image") MultipartFile image) throws java.io.IOException {
        if (image == null || image.isEmpty()) {
            throw AppException.badRequest("Vui long chon anh san pham");
        }
        String base64 = Base64.getEncoder().encodeToString(image.getBytes());
        VisionSearchResponse result = geminiService.analyzeProductImage(base64, image.getContentType());
        return ResponseEntity.ok(ApiResponse.success("Nhan dien hinh anh thanh cong", result));
    }
}
