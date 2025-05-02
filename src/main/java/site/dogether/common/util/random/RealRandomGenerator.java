package site.dogether.common.util.random;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class RealRandomGenerator implements RandomGenerator {

    @Override
    public int generateNumberInRange(final int start, final int end) {
        if (start > end) {
            throw new IllegalArgumentException(String.format("start는 end보다 작거나 같아야 합니다. (start : %d) (end : %d)", start, end));
        }

        return ThreadLocalRandom.current().nextInt(start, end + 1);
    }
}
