package site.dogether.dailytodocertification.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import site.dogether.common.audit.entity.BaseTimeEntity;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "daily_todo_certification_media_url")
@Entity
public class DailyTodoCertificationMediaUrl extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "daily_todo_certification_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private DailyTodoCertification dailyTodoCertification;

    @Column(name = "value", length = 500, nullable = false)
    private String value;

    public static DailyTodoCertificationMediaUrl create(final String mediaUrlValue, DailyTodoCertification dailyTodoCertification) {
        return new DailyTodoCertificationMediaUrl(null, dailyTodoCertification, mediaUrlValue);
    }

    public DailyTodoCertificationMediaUrl(
        final Long id,
        final DailyTodoCertification dailyTodoCertification,
        final String value
    ) {
        this.id = id;
        this.dailyTodoCertification = dailyTodoCertification;
        this.value = value;
    }

    // TODO : 필드 검증 조건 & 단위 테스트 추가
}
