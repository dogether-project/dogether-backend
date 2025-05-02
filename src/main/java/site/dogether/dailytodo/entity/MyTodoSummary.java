package site.dogether.dailytodo.entity;

import lombok.Getter;

import java.util.List;

@Getter
public class MyTodoSummary {

    private final List<DailyTodo> myTodos;

    public MyTodoSummary(final List<DailyTodo> myTodos) {
        this.myTodos = myTodos;
    }

    public int calculateTotalTodoCount() {
        return myTodos.size();
    }

    public int calculateTotalCertificatedCount() {
        int totalTodoCount = calculateTotalTodoCount();
        int certifyPendingCount = (int) myTodos.stream()
            .filter(DailyTodo::isCertifyPending)
            .count();

        return totalTodoCount - certifyPendingCount;
    }

    public int calculateTotalApprovedCount() {
        return (int) myTodos.stream()
            .filter(DailyTodo::isApproved)
            .count();
    }

    public int calculateTotalRejectedCount() {
        return (int) myTodos.stream()
            .filter(DailyTodo::isRejected)
            .count();
    }
}
