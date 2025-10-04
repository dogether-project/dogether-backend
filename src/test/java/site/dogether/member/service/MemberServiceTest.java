package site.dogether.member.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import site.dogether.member.entity.Member;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class MemberServiceTest {

    @Autowired private MemberService memberService;

    @Test
    void 신규_회원을_저장한다() {
        // given
        String providerId = "providerId";
        String name = "폰트";

        // when
        Member saved = memberService.save(providerId, name);

        // then
        assertThat(saved.getProviderId()).isEqualTo(providerId);
    }

    @Test
    void 이미_가입된_회원이면_저장하지_않고_조회한다() {
        // given
        String providerId = "providerId";
        String name = "폰트";
        Member saved = memberService.save(providerId, name);

        // when
        Member found = memberService.save(providerId, name);

        // then
        assertThat(found.getId()).isEqualTo(saved.getId());
    }
}
