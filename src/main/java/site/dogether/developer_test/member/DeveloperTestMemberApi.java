package site.dogether.developer_test.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.auth.service.AuthService;
import site.dogether.member.entity.Member;
import site.dogether.member.service.MemberService;
import site.dogether.notification.entity.NotificationToken;
import site.dogether.notification.repository.NotificationTokenRepository;

import java.util.Map;
import java.util.UUID;

@Profile("!prod")
@Slf4j
@RequiredArgsConstructor
@RestController
public class DeveloperTestMemberApi {

    private final MemberService memberService;
    private final AuthService authService;
    private final NotificationTokenRepository notificationTokenRepository;

    @PostMapping("/api/dev/save-member")
    public String saveMember(@RequestBody final Map<String, String> request) {
        final Member saved = memberService.save(request.get("providerId"), request.get("name"));
        final String token = UUID.randomUUID().toString();
        notificationTokenRepository.save(new NotificationToken(saved, token));
        log.info("develop test member save : {}, {}", saved, token);
        return authService.issueTestUserJwt(saved.getId());
    }

    @DeleteMapping("/api/dev/delete-member/{memberId}")
    public String deleteMember(@PathVariable final Long memberId) {
        memberService.delete(memberId);
        log.info("member delete : {}", memberId);
        return "탈퇴 완료!! - " + memberId;
    }
}
