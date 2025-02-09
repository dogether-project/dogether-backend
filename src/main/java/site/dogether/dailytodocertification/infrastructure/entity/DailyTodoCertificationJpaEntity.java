package site.dogether.dailytodocertification.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.dogether.common.audit.entity.BaseTimeEntity;
import site.dogether.dailytodo.infrastructure.entity.DailyTodoJpaEntity;
import site.dogether.dailytodocertification.domain.DailyTodoCertification;
import site.dogether.member.infrastructure.entity.MemberJpaEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "daily_todo_certification")
@Entity
public class DailyTodoCertificationJpaEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "daily_todo_id")
    @OneToOne(fetch = FetchType.LAZY)
    private DailyTodoJpaEntity dailyTodo;

    @JoinColumn(name = "reviewer_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private MemberJpaEntity reviewer;

    @Column(name = "content", length = 200, nullable = false)
    private String content;

    public DailyTodoCertificationJpaEntity(
        final DailyTodoCertification dailyTodoCertification,
        final DailyTodoJpaEntity dailyTodo,
        final MemberJpaEntity member
    ) {
        this(
            null,
            dailyTodo,
            member,
            dailyTodoCertification.getContent()
        );
    }

    public DailyTodoCertificationJpaEntity(
        final DailyTodoJpaEntity dailyTodo,
        final MemberJpaEntity reviewer,
        final String content
    ) {
        this(null, dailyTodo, reviewer, content);
    }

    public DailyTodoCertificationJpaEntity(
        final Long id,
        final DailyTodoJpaEntity dailyTodo,
        final MemberJpaEntity reviewer,
        final String content
    ) {
        this.id = id;
        this.dailyTodo = dailyTodo;
        this.reviewer = reviewer;
        this.content = content;
    }
}
