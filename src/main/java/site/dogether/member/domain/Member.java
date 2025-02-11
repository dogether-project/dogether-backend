package site.dogether.member.domain;

import lombok.Getter;
import site.dogether.member.domain.exception.InvalidMemberException;

@Getter
public class Member {

    private final Long id;
    private final String providerId;
    private final String name;

    public Member(final String providerId, final String name) {
        this(null, providerId, name);
        validateName(name);
        validateProviderId(providerId);
    }

    public Member(final Long id, final String providerId, final String name) {
        this.id = id;
        this.providerId = providerId;
        this.name = name;
    }

    private void validateName(final String name) {
        if (name == null || name.isEmpty()) {
            throw new InvalidMemberException("이름은 필수 입력값입니다.");
        }
    }

    private void validateProviderId(final String providerId) {
        if (providerId == null || providerId.isEmpty()) {
            throw new InvalidMemberException("Provider ID는 필수 입력값입니다.");
        }
    }
}
