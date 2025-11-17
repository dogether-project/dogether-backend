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
}
