package site.dogether.dailytodo.domain;

import java.util.List;
import lombok.Getter;

@Getter
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

    public double calculateCertificationRate() {
        int totalTodoCount = calculateTotalTodoCount();
        int totalCertificatedCount = calculateTotalCertificatedCount();
        return (double) totalCertificatedCount / totalTodoCount;
    }

    public double calculateApprovalRate() {
        int totalTodoCount = calculateTotalTodoCount();
        int totalApprovedCount = calculateTotalApprovedCount();
        return (double) totalApprovedCount / totalTodoCount;
    }
}
