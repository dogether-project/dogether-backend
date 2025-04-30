package site.dogether.dailytodo.entity;

import lombok.Getter;
import site.dogether.dailytodo.exception.InvalidDailyTodoException;

import java.util.List;

@Getter
public class DailyTodos {

    public static final int MAXIMUM_ALLOWED_VALUE_COUNT = 10;

    private final List<DailyTodo> values;

    public DailyTodos(final List<DailyTodo> values) {
        validateValues(values);
        this.values = values;
    }

    private void validateValues(final List<DailyTodo> values) {
        if (values == null || values.isEmpty()) {
            throw new InvalidDailyTodoException(String.format("데일리 투두로 null 혹은 빈 리스트를 입력할 수 없습니다. (%s)", values));
        }

        if (values.size() > MAXIMUM_ALLOWED_VALUE_COUNT) {
            throw new InvalidDailyTodoException(String.format("데일리 투두는 %d개 이하만 입력할 수 있습니다. (%d)", MAXIMUM_ALLOWED_VALUE_COUNT, values.size()));
        }
    }
}
