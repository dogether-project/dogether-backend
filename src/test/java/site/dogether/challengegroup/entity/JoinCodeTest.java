package site.dogether.challengegroup.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;

class JoinCodeTest {

    @Test
    void 참가_코드를_생성한다() {
        final JoinCode joinCode = JoinCode.generate();

        assertThat(joinCode).isNotNull();
        assertThat(joinCode.getValue()).hasSize(8);
    }

    @Test
    void 생성된_참가_코드는_영문자와_숫자로_구성된다() {
        final JoinCode joinCode = JoinCode.generate();

        assertThat(joinCode.getValue()).matches("^[a-zA-Z0-9]{8}$");
    }

    @Test
    void 참가_코드_생성_시_매번_다른_값이_생성된다() {
        final JoinCode joinCode1 = JoinCode.generate();
        final JoinCode joinCode2 = JoinCode.generate();

        assertThat(joinCode1.getValue()).isNotEqualTo(joinCode2.getValue());
    }

    @Test
    void 같은_값을_가진_참가_코드는_동일하다() throws Exception {
        final Constructor<JoinCode> constructor = JoinCode.class.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);

        final JoinCode joinCode1 = constructor.newInstance("abcd1234");
        final JoinCode joinCode2 = constructor.newInstance("abcd1234");

        assertThat(joinCode1).isEqualTo(joinCode2);
        assertThat(joinCode1.hashCode()).isEqualTo(joinCode2.hashCode());
    }

    @Test
    void 다른_값을_가진_참가_코드는_다르다() throws Exception {
        final Constructor<JoinCode> constructor = JoinCode.class.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);

        final JoinCode joinCode1 = constructor.newInstance("abcd1234");
        final JoinCode joinCode2 = constructor.newInstance("efgh5678");

        assertThat(joinCode1).isNotEqualTo(joinCode2);
    }
}
