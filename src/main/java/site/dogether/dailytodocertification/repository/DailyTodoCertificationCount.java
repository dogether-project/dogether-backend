package site.dogether.dailytodocertification.repository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DailyTodoCertificationCount {

    private final Long totalCount;
    private final Long approvedCount;
    private final Long rejectedCount;

    public int getTotalCount() {
        if (totalCount == null) {
            return 0;
        }

        return totalCount.intValue();
    }

    public int getApprovedCount() {
        if (approvedCount == null) {
            return 0;
        }

        return approvedCount.intValue();
    }

    public int getRejectedCount() {
        if (rejectedCount == null) {
            return 0;
        }

        return rejectedCount.intValue();
    }
}
