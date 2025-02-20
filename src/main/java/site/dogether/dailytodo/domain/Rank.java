package site.dogether.dailytodo.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Rank {

    @Setter
    private int rank;
    private final String name;
    private final int certificationRate;

    public Rank(final int rank,
                final String name,
                final int certificationRate) {
        this.rank = rank;
        this.name = name;
        this.certificationRate = certificationRate;
    }

}
