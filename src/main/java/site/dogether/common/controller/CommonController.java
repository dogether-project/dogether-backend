package site.dogether.common.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class CommonController {

    @GetMapping("/api/health-check")
    public String healthCheck() {
        log.info("health check API 호출!");
        return "Dogether Backend Service is OK 👍🔥\n";
    }

    @GetMapping("/api/infra-test")
    public String chicken() {
        return "이제 그만할래... 인프라 그만...\n";
    }
}
