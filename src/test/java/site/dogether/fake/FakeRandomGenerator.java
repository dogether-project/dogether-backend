package site.dogether.fake;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import site.dogether.common.util.random.RandomGenerator;

@Slf4j
@Profile("test")
@Primary
@Component
public class FakeRandomGenerator implements RandomGenerator {

    public int result;

    @Override
    public int generateNumberInRange(final int start, final int end) {
        log.info("가짜 객체가 랜덤한 척 임의의 정수를 반환! -> {}", result);
        return result;
    }
}
