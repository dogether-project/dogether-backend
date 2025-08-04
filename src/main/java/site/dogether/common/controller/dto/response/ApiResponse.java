package site.dogether.common.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import site.dogether.common.exception.handler.ErrorCode;

@Getter
public class ApiResponse<T> {

    private final String code;
    private final String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;

    public static ApiResponse<Void> success() {
        return new ApiResponse<>("success", "API 요청이 정상적으로 수행되었습니다.", null);
    }

    public static <T> ApiResponse<T> success(final T data) {
        return new ApiResponse<>("success", "API 요청이 정상적으로 수행되었습니다.", data);
    }

    public static ApiResponse<Void> fail(final ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getValue(), errorCode.getMessage(), null);
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
