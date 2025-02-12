package site.dogether.dailytodo.domain;

import java.util.List;

public class MyTodoSummary {

    private final List<DailyTodo> myTodos;

    public MyTodoSummary(List<DailyTodo> myTodos) {
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
}
