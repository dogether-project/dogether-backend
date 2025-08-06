package site.dogether.developer_test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.auth.oauth.JwtHandler;

import java.util.List;
import java.util.Map;

@Profile("!prod")
@Slf4j
@RequiredArgsConstructor
@RestController
public class DeveloperTestApi {

    private final JwtHandler jwtHandler;

    @PostMapping("/api/dev/issue-test-token")
    public List<String> issueTestToken(@RequestBody final Map<String, List<Long>> request) {
        final List<Long> memberIds = request.get("memberIds");
        return memberIds.stream()
            .map(jwtHandler::createToken)
            .toList();
    }
}
