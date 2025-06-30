package site.dogether.appinfo.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.controller.response.SuccessCode;

@Getter
@RequiredArgsConstructor
public enum AppInfoSuccessCode implements SuccessCode {

    FORCE_UPDATE_CHECK("AIS-0001", "앱 강제 업데이트 필요 여부가 조회되었습니다.");

    private final String value;
    private final String message;
}
