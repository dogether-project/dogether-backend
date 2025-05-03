package site.dogether.dailytodohistory.entity;

import site.dogether.dailytodo.entity.DailyTodoStatus;

public enum DailyTodoHistoryType {

    WRITE,
    CERTIFY,
    APPROVED,
    REJECTED
    ;

    public static DailyTodoHistoryType convertHistoryTypeFromDailyTodoStatus(final DailyTodoStatus dailyTodoStatus) {
        if (dailyTodoStatus == DailyTodoStatus.CERTIFY_PENDING) {
            return DailyTodoHistoryType.WRITE;
        }

        if (dailyTodoStatus == DailyTodoStatus.REVIEW_PENDING) {
            return DailyTodoHistoryType.CERTIFY;
        }

        if (dailyTodoStatus == DailyTodoStatus.APPROVE) {
            return DailyTodoHistoryType.APPROVED;
        }

        return DailyTodoHistoryType.REJECTED;
    }
}
