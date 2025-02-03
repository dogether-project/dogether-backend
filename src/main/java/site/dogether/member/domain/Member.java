package site.dogether.member.domain;

import lombok.Getter;

@Getter
public class Member {

    private final Long id;
    private final String providerId;
    private final String name;

    public Member(final String providerId, final String name) {
        this(null, providerId, name);
    }

    public Member(final Long id, final String providerId, final String name) {
        this.id = id;
        this.providerId = providerId;
        this.name = name;
    }
}
