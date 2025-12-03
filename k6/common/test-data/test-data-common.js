import {getDateNDaysAgo} from "../util/time-util.js";

// =========== 상수 ===========
// 회원 관련 데이터 옵션
export const MEMBER_COUNT = 400;   // 총 회원 수
export const MEMBER_PER_GROUP_COUNT = 20;  // 그룹당 참여 회원 수
export const DAY_TODO_PER_MEMBER_COUNT = 10;  // 회원이 하루에 작성하는 투두 개수
export const DAY_TODO_CERTIFICATION_PER_MEMBER_COUNT = DAY_TODO_PER_MEMBER_COUNT;  // 회원이 하루 인증하는 투두 인증 개수

// 과거 & 현재 활동 데이터 옵션
export const PAST_GROUP_PER_MEMBER_COUNT = 5;  // 회원당 과거 활동 참여 그룹 수
export const CURRENT_FOR_READ_GROUP_PER_MEMBER_COUNT = 5;  // 회원당 현재 활동 참여 그룹 수
export const CURRENT_FOR_WRITE_GROUP_PER_MEMBER_COUNT = 3;  // 회원당 현재 활동 참여 그룹 수

export const PAST_GROUP_RUNNING_DAY = 3;  // 과거 그룹 진행일 수
export const CURRENT_GROUP_RUNNING_DAY = 28;  // 현재 그룹 진행일 수

export const PAST_TOTAL_ACTIVITY_DAY = 365;  // 총 과거 진행일 수
export const PAST_TOTAL_ACTIVITY_CYCLE = Math.ceil(PAST_TOTAL_ACTIVITY_DAY / PAST_GROUP_RUNNING_DAY);
export const PAST_ONE_CYCLE_PER_GROUP_COUNT = MEMBER_COUNT / MEMBER_PER_GROUP_COUNT * PAST_GROUP_PER_MEMBER_COUNT;   // 한 사이클에 존재하는 그룹 개수

export const PAST_GROUP_ACTIVITY_START_AT = getDateNDaysAgo(PAST_TOTAL_ACTIVITY_CYCLE * PAST_GROUP_RUNNING_DAY + CURRENT_GROUP_RUNNING_DAY);
export const CURRENT_GROUP_ACTIVITY_START_AT = getDateNDaysAgo(CURRENT_GROUP_RUNNING_DAY - 1);

// 과거 활동 데이터 최종 개수
export const PAST_TOTAL_CHALLENGE_GROUP_COUNT = PAST_TOTAL_ACTIVITY_CYCLE * PAST_ONE_CYCLE_PER_GROUP_COUNT;
export const PAST_TOTAL_CHALLENGE_GROUP_MEMBER_COUNT = PAST_TOTAL_CHALLENGE_GROUP_COUNT * MEMBER_PER_GROUP_COUNT;
export const PAST_TOTAL_DAILY_TODO_COUNT = MEMBER_COUNT * DAY_TODO_PER_MEMBER_COUNT * PAST_GROUP_RUNNING_DAY * PAST_GROUP_PER_MEMBER_COUNT * PAST_TOTAL_ACTIVITY_CYCLE;
export const PAST_TOTAL_DAILY_TODO_CERTIFICATION_COUNT = MEMBER_COUNT * DAY_TODO_CERTIFICATION_PER_MEMBER_COUNT * PAST_GROUP_RUNNING_DAY * PAST_GROUP_PER_MEMBER_COUNT * PAST_TOTAL_ACTIVITY_CYCLE;

// CSV 파일 & DB 옵션
export const CSV_SAVED_BASE_PATH = './csv'
export const DB_BATCH_INSERT_SIZE = 100000;


// =========== 테스트 데이터 생성 유틸 함수 ===========
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


// ================== K6 쓰기 API 성능 테스트 헬퍼 함수 (for Write) ==================
export function getJoinCodesPerMember() {
    const totalChallengeGroupCount = MEMBER_COUNT / MEMBER_PER_GROUP_COUNT * CURRENT_FOR_WRITE_GROUP_PER_MEMBER_COUNT;
    const groupJoinCodeByMember = [];
    const joinCodes = [];
    let challengeGroupId = getLastIdsOfPastActivityData().lastChallengeGroupId + 1;

    for (let i = 0; i < MEMBER_COUNT / MEMBER_PER_GROUP_COUNT; i++) {
        const currentChallengeGroupId = challengeGroupId + totalChallengeGroupCount + i;
        joinCodes.push(`jc-${currentChallengeGroupId}`);
    }

    for (let m = 0; m < MEMBER_COUNT; m++) {
        groupJoinCodeByMember.push(joinCodes[Math.floor(m / MEMBER_PER_GROUP_COUNT)]);
    }

    return groupJoinCodeByMember;
}

export function getTodoTargetGroupIdsPerMember() {
    const groupIdsForCreateDailyTodo = [];
    let joiningGroupId = getLastIdsOfPastActivityData().lastChallengeGroupId + 1;

    for (let j = 0; j < MEMBER_COUNT / MEMBER_PER_GROUP_COUNT; j++) {
        for (let k = 0; k < MEMBER_PER_GROUP_COUNT; k++) {
            groupIdsForCreateDailyTodo.push(joiningGroupId);
        }
        joiningGroupId++;
    }

    return groupIdsForCreateDailyTodo;
}

export function getOneCertifiableTodoIdPerMember() {
    const todoIds = [];
    let dailyTodoId = getLastIdsOfPastActivityData().lastDailyTodoId + 1;

    for (let day = 0; day < CURRENT_GROUP_RUNNING_DAY; day++) {
        for (let memberId = 1; memberId <= MEMBER_COUNT; memberId++) {
            for (let i = 0; i < CURRENT_FOR_WRITE_GROUP_PER_MEMBER_COUNT; i++) {
                if (day === CURRENT_GROUP_RUNNING_DAY - 1 && i === 0) {
                    continue;
                }

                for (let j = 0; j < DAY_TODO_PER_MEMBER_COUNT; j++) {
                    const currentTodoId = dailyTodoId++;
                    if (day === CURRENT_GROUP_RUNNING_DAY - 1 && i === 1 && j === 0) {
                        todoIds.push(currentTodoId);
                    }
                }
            }
        }
    }

    return todoIds;
}

export function getPendingCertificationIdsPerReviewer() {
    const pendingCertificationIds = Array(MEMBER_COUNT).fill(0);
    let dailyTodoCertificationId = getLastIdsOfPastActivityData().lastDailyTodoCertificationId + 1;

    for (let day = 0; day < CURRENT_GROUP_RUNNING_DAY; day++) {
        for (let memberId = 1; memberId <= MEMBER_COUNT; memberId++) {
            const reviewerId = getReviewerId(memberId);
            for (let i = 0; i < CURRENT_FOR_WRITE_GROUP_PER_MEMBER_COUNT; i++) {
                if (day === CURRENT_GROUP_RUNNING_DAY - 1 && i === 0) {
                    continue;
                }

                for (let j = 0; j < DAY_TODO_PER_MEMBER_COUNT; j++) {
                    if (day === CURRENT_GROUP_RUNNING_DAY - 1 && i === 1) {
                        continue;
                    }

                    const currentTodoCertificationId = dailyTodoCertificationId++;
                    if (day === CURRENT_GROUP_RUNNING_DAY - 1 && i === 2 && j === 0) {
                        pendingCertificationIds[reviewerId - 1] = currentTodoCertificationId;
                    }
                }
            }
        }
    }

    return pendingCertificationIds;
}
