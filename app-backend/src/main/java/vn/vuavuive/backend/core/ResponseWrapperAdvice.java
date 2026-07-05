package vn.vuavuive.backend.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import vn.vuavuive.backend.exception.GlobalExceptionHandler;
import vn.vuavuive.backend.modules.product.dto.PagedResponse;

@RestControllerAdvice
@RequiredArgsConstructor
public class ResponseWrapperAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        String className = returnType.getDeclaringClass().getName();
        // Exclude Swagger UI and API Docs controllers
        if (className.contains("springdoc") || className.contains("swagger")) {
            return false;
        }
        return true;
    }

    @SneakyThrows
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        String path = request.getURI().getPath();
        if (isPaymentHtmlPage(path)) {
            response.getHeaders().setContentType(MediaType.TEXT_HTML);
            return body;
        }

        // If it's already an ApiResponse or an ErrorResponse, don't wrap it
        if (body instanceof ApiResponse || body instanceof GlobalExceptionHandler.ErrorResponse) {
            return body;
        }

        // If body is null/void, wrap with a success ApiResponse having null data
        if (body == null) {
            return ApiResponse.success(null);
        }

        // Handle PagedResponse separately to convert it to ApiResponse with pagination
        if (body instanceof PagedResponse<?> paged) {
            Pagination pagination = new Pagination(
                    (int) paged.totalElements(),
                    paged.currentPage() + 1,
                    paged.content().size(),
                    paged.totalPages()
            );
            return ApiResponse.builder()
                    .success(true)
                    .message("Thao tác thành công")
                    .data(paged.content())
                    .pagination(pagination)
                    .build();
        }

        // General success wrapping
        ApiResponse<Object> apiResponse = ApiResponse.success(body);

        // String return type needs special handling to avoid ClassCastException in spring
        if (body instanceof String) {
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return objectMapper.writeValueAsString(apiResponse);
        }

        return apiResponse;
    }

    private boolean isPaymentHtmlPage(String path) {
        return path.startsWith("/api/momo/mock")
                || path.startsWith("/api/momo/return")
                || path.startsWith("/api/payments/momo/mock")
                || path.startsWith("/api/payments/momo/return")
                || path.startsWith("/api/payments/zalopay/mock")
                || path.startsWith("/api/payments/zalopay/return");
    }
}
