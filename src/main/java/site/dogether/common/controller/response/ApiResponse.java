package site.dogether.common.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import site.dogether.common.error.ExceptionCode;

@Getter
public class ApiResponse<T> {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String code;

    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;

    public static ApiResponse<Void> success(final String message) {
        return new ApiResponse<>(null, message, null);
    }

    public static <T> ApiResponse<T> successWithData(final String message, final T data) {
        return new ApiResponse<>(null, message, data);
    }

    public static ApiResponse<Void> fail(final ExceptionCode exceptionCode) {
        return new ApiResponse<>(exceptionCode.getValue(), exceptionCode.getMessage(), null);
    }

    private ApiResponse(
        final String code,
        final String message,
        final T data
    ) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
