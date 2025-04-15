package site.dogether.dailytodocertification.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.common.audit.entity.BaseEntity;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.member.entity.Member;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "daily_todo_certification")
@Entity
public class DailyTodoCertification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "daily_todo_id")
    @OneToOne(fetch = FetchType.LAZY)
    private DailyTodo dailyTodo;

    @JoinColumn(name = "reviewer_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member reviewer;

    @Column(name = "content", length = 200, nullable = false)
    private String content;

    @Column(name = "media_url", length = 500, nullable = false)
    private String mediaUrl;

    public static DailyTodoCertification create(
        final String content,
        final DailyTodo dailyTodo,
        final Member member,
        final String mediaUrl
    ) {
        return new DailyTodoCertification(
            null,
            dailyTodo,
            member,
            content,
            mediaUrl
        );
    }

    public DailyTodoCertification(
        final Long id,
        final DailyTodo dailyTodo,
        final Member reviewer,
        final String content,
        final String mediaUrl
    ) {
        this.id = id;
        this.dailyTodo = dailyTodo;
        this.reviewer = reviewer;
        this.content = content;
        this.mediaUrl = mediaUrl;
    }

    // TODO : 검증 조건 및 단위 테스트 추가

    public boolean checkReviewer(final Member target) {
        return reviewer.getId().equals(target.getId());
    }

    public ChallengeGroup getChallengeGroup() {
        return dailyTodo.getChallengeGroup();
    }

    public String getReviewerName() {
        return reviewer.getName();
    }

    public String getDoerName() {
        return dailyTodo.getMember().getName();
    }

    public String getDailyTodoContent() {
        return dailyTodo.getContent();
    }
}
