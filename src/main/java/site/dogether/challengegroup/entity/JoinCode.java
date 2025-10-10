package site.dogether.challengegroup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.security.SecureRandom;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class JoinCode {

    private static final int JOIN_CODE_LENGTH = 8;
    private static final String ALL_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String EXCLUDED_AMBIGUOUS_CHARS = "0Oo1Iil";
    private static final String AVAILABLE_CHARS = buildAvailableChars();

    @Column(name = "join_code", length = 20, nullable = false, unique = true)
    private String value;

    private static String buildAvailableChars() {
        final StringBuilder builder = new StringBuilder();
        for (char c : ALL_CHARS.toCharArray()) {
            if (EXCLUDED_AMBIGUOUS_CHARS.indexOf(c) == -1) {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    public static JoinCode generate() {
        final SecureRandom random = new SecureRandom();
        final StringBuilder codeBuilder = new StringBuilder(JOIN_CODE_LENGTH);

        for (int i = 0; i < JOIN_CODE_LENGTH; i++) {
            final int index = random.nextInt(AVAILABLE_CHARS.length());
            codeBuilder.append(AVAILABLE_CHARS.charAt(index));
        }

        return new JoinCode(codeBuilder.toString());
    }

    private JoinCode(final String value) {
        validateJoinCode(value);
        this.value = value;
    }

    private void validateJoinCode(final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("참가 코드는 null 또는 공백일 수 없습니다.");
        }
        if (value.length() != JOIN_CODE_LENGTH) {
            throw new IllegalArgumentException(
                String.format("참가 코드는 %d자여야 합니다. (입력된 길이: %d)", JOIN_CODE_LENGTH, value.length())
            );
        }
    }

    @Override
    public boolean equals(final Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        final JoinCode joinCode = (JoinCode) object;
        return Objects.equals(value, joinCode.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
