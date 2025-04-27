package site.dogether.dailytodo.controller.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.dogether.common.controller.response.SuccessCode;

@Getter
@RequiredArgsConstructor
public enum DailyTodoSuccessCode implements SuccessCode {

    CREATE_DAILY_TODOS("DTS-0001", "데일리 투두가 작성되었습니다."),
    CERTIFY_DAILY_TODO("DTS-0002", "데일리 투두 수행 인증이 완료되었습니다."),
    GET_YESTERDAY_DAILY_TODOS("DTS-0003", "어제 작성된 데일리 투두 내용들이 조회되었습니다."),
    GET_MY_DAILY_TODOS("DTS-0004", "내 데일리 투두들이 조회되었습니다."),
    GET_CHALLENGE_GROUP_MEMBER_TODAY_TODO_HISTORY("DTS-0005", "챌린지 그룹원의 당일 투두가 조회되었습니다."),
    MARK_TODO_HISTORY_AS_READ("DTS-0006", "성공적으로 읽음 처리 되었습니다.")
    ;

    private final String value;
    private final String message;
}
