package site.dogether.dailytodo.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Rank {

    @Setter
    private int rank;
    private final String name;
    private final double certificationRate;
    private final double approvalRate;

    public Rank(int rank, String name, double certificationRate, double approvalRate) {
        this.rank = rank;
        this.name = name;
        this.certificationRate = certificationRate;
        this.approvalRate = approvalRate;
    }

}
