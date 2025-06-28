package site.dogether.appinfo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppInfoServiceTest {

    @DisplayName("현재 권장 앱 버전과 같은 버전이 요청되면 false를 반환한다.")
    @Test
    void forceUpdateCheckReturnTrue() {
        // Given
        final AppInfoService appInfoService = new AppInfoService();
        final String request = "1.0.3";

        // When
        final boolean result = appInfoService.forceUpdateCheck(request);

        // Then
        assertThat(result).isFalse();
    }
}
