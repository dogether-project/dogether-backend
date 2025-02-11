package site.dogether.dailytodocertification.domain;

import lombok.Getter;
import site.dogether.dailytodocertification.domain.exception.InvalidDailyTodoCertificationException;

import java.util.List;

@Getter
public class DailyTodoCertificationMediaUrls {

    private final List<String> values;

    public DailyTodoCertificationMediaUrls(final List<String> values) {
        validateValues(values);

        this.values = values;
    }

    private void validateValues(final List<String> values) {
        if (values == null || values.isEmpty()) {
            throw new InvalidDailyTodoCertificationException("데일리 투두 수행 인증에 미디어 정보를 누락할 수 없습니다.");
        }
    }
}
