package site.dogether.dailytodo.entity;

import lombok.Getter;

import java.util.List;

@Getter
public class MyTodoSummary {

    private final List<DailyTodo> myTodos;
    private final String memberName;

    public MyTodoSummary(final List<DailyTodo> myTodos, final String memberName) {
        this.myTodos = myTodos;
        this.memberName = memberName;
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

    public int calculateCertificationRate() {
        final int totalTodoCount = calculateTotalTodoCount();

        if (totalTodoCount == 0) {
            return 0;
        }
        final int totalCertificatedCount = calculateTotalCertificatedCount();
        double certificationRate = (double) totalCertificatedCount / totalTodoCount;
        return (int) Math.floor(certificationRate * 100);
    }

    public double calculateApprovalRate() {
        final int totalTodoCount = calculateTotalTodoCount();
        if (totalTodoCount == 0) {
            return 0;
        }
        final int totalApprovedCount = calculateTotalApprovedCount();
        return (double) totalApprovedCount / totalTodoCount;
    }

    public int calculateTotalRejectedCount() {
        return (int) myTodos.stream()
            .filter(DailyTodo::isRejected)
            .count();
    }
}
