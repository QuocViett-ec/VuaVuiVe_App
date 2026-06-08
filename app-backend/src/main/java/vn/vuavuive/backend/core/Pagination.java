package vn.vuavuive.backend.core;

public record Pagination(
    int total,
    int page,
    int limit,
    int totalPages
) {}
