package site.dogether.s3.url_generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("performance-test")
@Primary
@Component
public class PerformanceTestPresignedUrlGenerator implements PresignedUrlGenerator{

    @Override
    public String generate(final Long dailyTodoId, final String uploadFileType) {
        try {
            Thread.sleep(4); // 실제 Presigned url 생성처럼 대기
            log.info("performance test presigned url 생성!");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 중단 플래그 복구
            log.warn("알림 전송 중 인터럽트 발생!", e);
        }

        return "http://test-presigned-url.site";
    }
}
