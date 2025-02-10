package site.dogether.s3.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.controller.response.SuccessCode;

@Getter
@RequiredArgsConstructor
public enum S3SuccessCode implements SuccessCode {

    ISSUED_S3_PRESIGNED_URLS("s3s-0001", "presigned url이 생성되었습니다."),
    ;

    private final String value;
    private final String message;
}
