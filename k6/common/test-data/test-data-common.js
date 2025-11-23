// =========== 상수 ===========
// 회원 관련 데이터 옵션
export const MEMBER_COUNT = 400;   // 총 회원 수
export const MEMBER_PER_GROUP_COUNT = 20;  // 그룹당 참여 회원 수
export const DAY_TODO_PER_MEMBER_COUNT = 10;  // 회원이 하루에 작성하는 투두 개수
export const DAY_TODO_CERTIFICATION_PER_MEMBER_COUNT = DAY_TODO_PER_MEMBER_COUNT;  // 회원이 하루 인증하는 투두 인증 개수

// 과거 & 현재 활동 데이터 옵션
export const PAST_GROUP_PER_MEMBER_COUNT = 5;  // 회원당 과거 활동 참여 그룹 수
export const CURRENT_FOR_READ_GROUP_PER_MEMBER_COUNT = 5;  // 회원당 현재 활동 참여 그룹 수 (사실상 고정 값...)

export const PAST_GROUP_RUNNING_DAY = 3;  // 과거 그룹 진행일 수
export const CURRENT_GROUP_RUNNING_DAY = 28;  // 현재 그룹 진행일 수

export const PAST_TOTAL_ACTIVITY_DAY = 365;  // 총 과거 진행일 수
export const PAST_TOTAL_ACTIVITY_CYCLE = Math.ceil(PAST_TOTAL_ACTIVITY_DAY / PAST_GROUP_RUNNING_DAY);
export const PAST_ONE_CYCLE_PER_GROUP_COUNT = MEMBER_COUNT / MEMBER_PER_GROUP_COUNT * PAST_GROUP_PER_MEMBER_COUNT;   // 한 사이클에 존재하는 그룹 개수
export const PAST_GROUP_ACTIVITY_START_AT = getDateNDaysAgo(PAST_TOTAL_ACTIVITY_CYCLE * PAST_GROUP_RUNNING_DAY + CURRENT_GROUP_RUNNING_DAY);

// 과거 활동 데이터 최종 개수
export const PAST_TOTAL_CHALLENGE_GROUP_COUNT = PAST_TOTAL_ACTIVITY_CYCLE * PAST_ONE_CYCLE_PER_GROUP_COUNT;
export const PAST_TOTAL_CHALLENGE_GROUP_MEMBER_COUNT = PAST_TOTAL_CHALLENGE_GROUP_COUNT * MEMBER_PER_GROUP_COUNT;
export const PAST_TOTAL_DAILY_TODO_COUNT = MEMBER_COUNT * DAY_TODO_PER_MEMBER_COUNT * PAST_GROUP_RUNNING_DAY * PAST_GROUP_PER_MEMBER_COUNT * PAST_TOTAL_ACTIVITY_CYCLE;
export const PAST_TOTAL_DAILY_TODO_CERTIFICATION_COUNT = MEMBER_COUNT * DAY_TODO_CERTIFICATION_PER_MEMBER_COUNT * PAST_GROUP_RUNNING_DAY * PAST_GROUP_PER_MEMBER_COUNT * PAST_TOTAL_ACTIVITY_CYCLE;

// CSV 파일 옵션
export const CSV_SAVED_BASE_PATH = './csv'


// =========== 테스트 데이터 생성 유틸 함수 ===========
/**
 * 현재 날짜 반환
 */
export function getCurrentDate() {
    const now = new Date();
    return convertDateTimeFormatString(now);
}

/**
 * n일전 날짜 계산
 */
export function getDateNDaysAgo(n) {
    const now = new Date();
    now.setDate(now.getDate() - n);
    return convertDateTimeFormatString(now);
}

/**
 * Date 객체를 문자열로 변환
 */
export function convertDateTimeFormatString(date = new Date()) {
    // MySQL DATETIME 형식으로 포맷 (YYYY-MM-DD HH:mm:ss)
    const pad = (n) => n.toString().padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
}

/**
 * 날짜 문자열에서 년/일/월 문자열만 파싱
 */
export function toDateOnly(dateTimeString) {
    if (typeof dateTimeString !== 'string') return dateTimeString;

    // 공백 기준으로 앞부분(날짜)만 사용
    return dateTimeString.split(' ')[0];
}

/**
 * startDate에서 cycle * duration일 이후 날짜를 구하는 계산 함수
 */
export function calculateNextDate(startDate, cycle, duration) {
    const d = new Date(startDate);
    d.setDate(d.getDate() + (cycle * duration));
    return convertDateTimeFormatString(d);
}

/**
 * startAt 날짜에 시작해 duration일 만큼 진행하는 활동의 마지막 활동 일을 구하는 계산 함수
 */
export function calculateEndAt(startAt, duration) {
    const d = new Date(startAt);
    d.setDate(d.getDate() + (duration - 1));
    return convertDateTimeFormatString(d);
}

/**
 * 리뷰어 ID 계산
 */
export function getReviewerId(memberId) {
    // 그룹의 시작과 끝 ID를 구함
    const groupIndex = Math.floor((memberId - 1) / MEMBER_PER_GROUP_COUNT);
    const startId = groupIndex * MEMBER_PER_GROUP_COUNT + 1;         // 예: 1, 21, 41, ...

    // 그룹 내 offset (0~19)
    const offset = memberId - startId;

    // 매칭 규칙: 0 <-> 19, 1 <-> 18, ...
    const reviewerOffset = MEMBER_PER_GROUP_COUNT - 1 - offset;

    return startId + reviewerOffset;
}

/**
 * 과거 활동 데이터 중 테이블별 마지막 데이터들의 PK를 계산해서 반환
 */
export function getLastIdsOfPastActivityData() {
    return {
        lastChallengeGroupId: PAST_TOTAL_CHALLENGE_GROUP_COUNT,
        lastChallengeGroupMemberId: PAST_TOTAL_CHALLENGE_GROUP_MEMBER_COUNT,
        lastDailyTodoId: PAST_TOTAL_DAILY_TODO_COUNT,
        lastDailyTodoCertificationId: PAST_TOTAL_DAILY_TODO_CERTIFICATION_COUNT
    };
}


// ================== K6 조회 API 성능 테스트 헬퍼 함수 (for Read) ==================
export function getChallengeGroupIdsPerMember() {
    let challengeGroupId = getLastIdsOfPastActivityData().lastChallengeGroupId + 1;
    const groupIdsByMember = Array.from({ length: MEMBER_COUNT }, () => []);

    let joiningGroupId = challengeGroupId;
    for (let i = 0; i < CURRENT_FOR_READ_GROUP_PER_MEMBER_COUNT; i++) {
        for (let j = 0; j < MEMBER_COUNT / MEMBER_PER_GROUP_COUNT; j++) {
            let memberId = 1 + j * MEMBER_PER_GROUP_COUNT;
            for (let k = 0; k < MEMBER_PER_GROUP_COUNT; k++) {
                let currentMemberId = memberId++;
                groupIdsByMember[currentMemberId - 1].push(joiningGroupId);
            }
            joiningGroupId++;
        }
    }

    return groupIdsByMember;
}

export function getChallengeGroupMembersPerMember() {
    const groupMemberIdsByMember = Array.from({ length: MEMBER_COUNT }, () => []);
    for (let i = 1; i <= MEMBER_COUNT; i++) {
        groupMemberIdsByMember[i - 1].push(getReviewerId(i));
    }

    return groupMemberIdsByMember;
}
