package vn.vuavuive.backend.modules.upload;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import vn.vuavuive.backend.core.ApiResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadController {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<Map<String, String>> uploadImage(@RequestPart("file") MultipartFile file,
                                                        HttpServletRequest request) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        String extension = extension(file.getOriginalFilename(), contentType);
        Path dir = Paths.get("uploads", "products").toAbsolutePath().normalize();
        Files.createDirectories(dir);

        String filename = UUID.randomUUID() + "." + extension;
        Files.copy(file.getInputStream(), dir.resolve(filename));

        String url = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath("/uploads/products/" + filename)
                .replaceQuery(null)
                .toUriString();
        return ApiResponse.success(Map.of("url", url));
    }

    private String extension(String originalName, String contentType) {
        if (originalName != null) {
            int dot = originalName.lastIndexOf('.');
            if (dot >= 0 && dot < originalName.length() - 1) {
                String ext = originalName.substring(dot + 1).toLowerCase(Locale.ROOT);
                if (ALLOWED_EXTENSIONS.contains(ext)) return ext;
            }
        }
        String subtype = contentType.substring(contentType.indexOf('/') + 1).toLowerCase(Locale.ROOT);
        return ALLOWED_EXTENSIONS.contains(subtype) ? subtype : "jpg";
    }
}
