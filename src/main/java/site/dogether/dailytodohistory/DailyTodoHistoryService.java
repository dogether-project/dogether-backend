package site.dogether.dailytodohistory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodohistory.entity.DailyTodoHistory;
import site.dogether.dailytodohistory.repository.DailyTodoHistoryRepository;

import java.util.List;

import static site.dogether.dailytodohistory.entity.DailyTodoHistoryType.WRITE;
import static site.dogether.dailytodohistory.entity.DailyTodoHistoryType.convertHistoryTypeFromDailyTodoStatus;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DailyTodoHistoryService {

    private final DailyTodoHistoryRepository dailyTodoHistoryRepository;

    @Transactional
    public void saveDailyTodoWriteHistory(final List<DailyTodo> dailyTodos) {
        final List<DailyTodoHistory> dailyTodoHistories = dailyTodos.stream()
            .map(dailyTodo -> new DailyTodoHistory(dailyTodo.getChallengeGroup(), dailyTodo.getMember(), dailyTodo, WRITE))
            .toList();
        dailyTodoHistoryRepository.saveAll(dailyTodoHistories);
    }

    @Transactional
    public void saveDailyTodoHistory(final DailyTodo dailyTodo) {
        final DailyTodoHistory dailyTodoHistory = new DailyTodoHistory(
            dailyTodo.getChallengeGroup(),
            dailyTodo.getMember(),
            dailyTodo,
            convertHistoryTypeFromDailyTodoStatus(dailyTodo.getStatus())
        );
        dailyTodoHistoryRepository.save(dailyTodoHistory);
    }
}
