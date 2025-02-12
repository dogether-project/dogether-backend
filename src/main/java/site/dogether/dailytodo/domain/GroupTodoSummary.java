package site.dogether.dailytodo.domain;

import java.util.List;
import java.util.stream.IntStream;

public class GroupTodoSummary {

    private final List<MyTodoSummary> myTodoSummaries;

    public GroupTodoSummary(final List<MyTodoSummary> myTodoSummaries) {
        this.myTodoSummaries = myTodoSummaries;
    }

    public int calculateTotalTodoCount() {
        return myTodoSummaries.stream()
                .mapToInt(MyTodoSummary::calculateTotalTodoCount)
                .sum();
    }

    public int calculateTotalCertificatedCount() {
        return myTodoSummaries.stream()
                .mapToInt(MyTodoSummary::calculateTotalCertificatedCount)
                .sum();
    }

    public int calculateTotalApprovedCount() {
        return myTodoSummaries.stream()
                .mapToInt(MyTodoSummary::calculateTotalApprovedCount)
                .sum();
    }

    public List<Rank> getRanksOfTop3() {
        final List<Rank> allRanking = myTodoSummaries.stream()
                .map(myTodoSummary -> new Rank(
                        0,
                        myTodoSummary.getMyTodos().get(0).getMember().getName(),
                        myTodoSummary.calculateCertificationRate(),
                        myTodoSummary.calculateApprovalRate()
                ))
                .sorted((o1, o2) -> (int) (o2.getCertificationRate() - o1.getCertificationRate()))
                .toList();

        IntStream.range(0, allRanking.size())
                .forEach(i -> allRanking.get(i).setRank(i + 1));

        return List.of(
                allRanking.get(0),
                allRanking.size() > 1 ? allRanking.get(1) : new Rank(2, null, 0, 0),
                allRanking.size() > 2 ? allRanking.get(2) : new Rank(3, null, 0, 0)
        );
    }

}
