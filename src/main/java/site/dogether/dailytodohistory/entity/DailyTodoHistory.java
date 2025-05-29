package site.dogether.dailytodohistory.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import site.dogether.common.audit.entity.BaseEntity;
import site.dogether.dailytodo.entity.DailyTodo;

import java.time.LocalDateTime;
import java.util.List;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "daily_todo_history")
@Entity
public class DailyTodoHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "daily_todo_id", nullable = false)
    @OneToOne(fetch = FetchType.LAZY)
    private DailyTodo dailyTodo;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @ToString.Exclude
    @OneToMany(mappedBy = "dailyTodoHistory", cascade = CascadeType.REMOVE)
    private List<DailyTodoHistoryRead> dailyTodoHistoryReads;

    public DailyTodoHistory(final DailyTodo dailyTodo) {
        this(null, dailyTodo, LocalDateTime.now());
    }

    public DailyTodoHistory(
        final Long id,
        final DailyTodo dailyTodo,
        final LocalDateTime eventTime
    ) {
        this.id = id;
        this.dailyTodo = dailyTodo;
        this.eventTime = eventTime;
    }

    public void updateEventTime() {
        this.eventTime = LocalDateTime.now();
    }
}
