package site.dogether.dailytodohistory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.common.audit.entity.BaseEntity;
import site.dogether.dailytodo.entity.DailyTodoStatus;
import site.dogether.member.entity.Member;

import java.time.LocalDateTime;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "daily_todo_history")
@Entity
public class DailyTodoHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "challenge_group_id", nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private ChallengeGroup challengeGroup;

    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(name = "todo_content", length = 30, nullable = false, updatable = false)
    private String todoContent;

    @Column(name = "todo_status", length = 20, nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private DailyTodoStatus todoStatus;

    @Column(name = "todo_certification_content", length = 200, updatable = false)
    private String todoCertificationContent;

    @Column(name = "todo_certification_media_url", length = 500, updatable = false)
    private String todoCertificationMediaUrl;

    @Column(name = "event_at", nullable = false, updatable = false)
    private LocalDateTime eventAt;

    public DailyTodoHistory(
        final ChallengeGroup challengeGroup,
        final Member member,
        final String todoContent,
        final DailyTodoStatus todoStatus,
        final String todoCertificationContent,
        final String todoCertificationMediaUrl
    ) {
        this(
            null,
            challengeGroup,
            member,
            todoContent,
            todoStatus,
            todoCertificationContent,
            todoCertificationMediaUrl,
            LocalDateTime.now()
        );
    }

    public DailyTodoHistory(
        final Long id,
        final ChallengeGroup challengeGroup,
        final Member member,
        final String todoContent,
        final DailyTodoStatus todoStatus,
        final String todoCertificationContent,
        final String todoCertificationMediaUrl,
        final LocalDateTime eventAt
    ) {
        this.id = id;
        this.challengeGroup = challengeGroup;
        this.member = member;
        this.todoContent = todoContent;
        this.todoStatus = todoStatus;
        this.todoCertificationContent = todoCertificationContent;
        this.todoCertificationMediaUrl = todoCertificationMediaUrl;
        this.eventAt = eventAt;
    }
}
