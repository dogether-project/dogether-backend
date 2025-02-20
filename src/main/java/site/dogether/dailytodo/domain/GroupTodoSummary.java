package site.dogether.dailytodo.domain;

import java.util.List;
import java.util.stream.IntStream;

public class GroupTodoSummary {

    private final List<MyTodoSummary> myTodoSummaries;

    public GroupTodoSummary(final List<MyTodoSummary> myTodoSummaries) {
        this.myTodoSummaries = myTodoSummaries;
    }

    public List<Rank> getRanks() {
        final List<Rank> allRanking = myTodoSummaries.stream()
                .map(myTodoSummary -> new Rank(
                        0,
                        myTodoSummary.getMemberName(),
                        myTodoSummary.calculateCertificationRate()
                ))
                .sorted((o1, o2) -> (o2.getCertificationRate() - o1.getCertificationRate()))
                .toList();

        IntStream.range(0, allRanking.size())
                .forEach(i -> allRanking.get(i).setRank(i + 1));

        return allRanking;
    }

}
