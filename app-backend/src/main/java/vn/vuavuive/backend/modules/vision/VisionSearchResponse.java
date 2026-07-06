package vn.vuavuive.backend.modules.vision;

import java.util.List;

public record VisionSearchResponse(
        String keyword,
        List<String> keywords,
        String category,
        double confidence
) {}
