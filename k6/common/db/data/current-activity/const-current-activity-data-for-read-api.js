import {getDateNDaysAgoInKst, calculateEndAt, getCurrentDateInKstWithoutTime} from "../../util/time-util.js";
import { getLastInsertedIds } from "../past-activity/const-past-activity-data.js";

// =========== 상수 ===========
const MEMBER_COUNT = 400;   // 총 회원 수
const MEMBER_PER_GROUP_COUNT = 20;  // 그룹당 참여 회원 수
const GROUP_PER_MEMBER_COUNT = 3;  // 회원당 참여 그룹 수
const CURRENT_GROUP_RUNNING_DAY = 28;  // 현재 그룹 진행일 수
const CURRENT_GROUP_ACTIVITY_START_AT = getDateNDaysAgoInKst(CURRENT_GROUP_RUNNING_DAY - 1);
const DAY_TODO_PER_MEMBER_COUNT = 8;  // 회원이 하루에 작성하는 투두 개수
const DAY_TODO_CERTIFICATION_PER_MEMBER_COUNT = DAY_TODO_PER_MEMBER_COUNT;  // 회원이 하루 인증하는 투두 인증 개수

// =========== 최종 데이터 개수 ===========
const TOTAL_MEMBER_COUNT = MEMBER_COUNT;
const TOTAL_NOTIFICATION_TOKEN_COUNT = MEMBER_COUNT;
const TOTAL_DAILY_TODO_STATS_COUNT = MEMBER_COUNT;
const TOTAL_CHALLENGE_GROUP_COUNT = MEMBER_COUNT / MEMBER_PER_GROUP_COUNT * GROUP_PER_MEMBER_COUNT;
const TOTAL_CHALLENGE_GROUP_MEMBER_COUNT = MEMBER_COUNT * GROUP_PER_MEMBER_COUNT;
const TOTAL_DAILY_TODO_COUNT = MEMBER_COUNT * CURRENT_GROUP_RUNNING_DAY * DAY_TODO_PER_MEMBER_COUNT * GROUP_PER_MEMBER_COUNT;
const TOTAL_DAILY_TODO_HISTORY_COUNT = TOTAL_DAILY_TODO_COUNT;
const TOTAL_DAILY_TODO_CERTIFICATION_COUNT = MEMBER_COUNT * CURRENT_GROUP_RUNNING_DAY * DAY_TODO_CERTIFICATION_PER_MEMBER_COUNT * GROUP_PER_MEMBER_COUNT;
const TOTAL_DAILY_TODO_CERTIFICATION_REVIEWER_COUNT = TOTAL_DAILY_TODO_CERTIFICATION_COUNT;

// =========== 데이터 PK ===========
const lastIds = getLastInsertedIds();
let challengeGroupId = lastIds.lastInsertedDummyChallengeGroupId + 1;
let challengeGroupMemberId = lastIds.lastInsertedDummyChallengeGroupMemberId + 1;
let dailyTodoId = lastIds.lastInsertedDummyDailyTodoId + 1;  // daily_todo & daily_todo_history
let dailyTodoCertificationId = lastIds.lastInsertedDummyDailyTodoCertificationId + 1;

// =========== 메인 로직 ===========
export function createCurrentActivityData() {
    const batch_size = 100000;
    const member_data = [];
    const notification_token_data = [];
    const daily_todo_stats_data = [];
    const challenge_group_data = [];
    const challenge_group_member_data = [];
    const last_selected_challenge_group_record_data = [];
    const daily_todo_data = [];
    const daily_todo_history_data = [];
    const daily_todo_certification_data = [];
    const daily_todo_certification_reviewer_data = [];

    // 캐싱
    const groupIdsByMember = Array.from({ length: MEMBER_COUNT }, () => []);
    const todoIdsByMember = Array.from({ length: MEMBER_COUNT }, () => []);

    const todayDate = getCurrentDateInKstWithoutTime();

    console.log(`👷[Const Current Activity Data] 현재 진행중인 활동 데이터 생성중...`);

    // 1. challenge_group & challenge_group_member 데이터 생성
    const currentGroupStartAt = new Date(CURRENT_GROUP_ACTIVITY_START_AT);
    currentGroupStartAt.setHours(7, 0, 0, 0);
    const currentGroupEndAt = calculateEndAt(currentGroupStartAt, CURRENT_GROUP_RUNNING_DAY);
    let joiningGroupId = challengeGroupId;

    for (let i = 0; i < TOTAL_CHALLENGE_GROUP_COUNT; i++) {
        const currentChallengeGroupId = challengeGroupId++;
        challenge_group_data.push([currentChallengeGroupId, `g-${currentChallengeGroupId}`, MEMBER_PER_GROUP_COUNT, `jc-${currentChallengeGroupId}`, "RUNNING", currentGroupStartAt, currentGroupEndAt, currentGroupStartAt, todayDate, null]);
    }

    for (let i = 0; i < GROUP_PER_MEMBER_COUNT; i++) {
        for (let j = 0; j < MEMBER_COUNT / MEMBER_PER_GROUP_COUNT; j++) {
            let memberId = 1 + j * MEMBER_PER_GROUP_COUNT;
            for (let k = 0; k < MEMBER_PER_GROUP_COUNT; k++) {
                let currentMemberId = memberId++;
                challenge_group_member_data.push([challengeGroupMemberId++, joiningGroupId, currentMemberId, currentGroupStartAt, todayDate, null]);
                groupIdsByMember[currentMemberId - 1].push(joiningGroupId);

                if (i === GROUP_PER_MEMBER_COUNT - 1) {
                    last_selected_challenge_group_record_data.push([currentMemberId, joiningGroupId, currentMemberId, todayDate, null]);
                }
            }
            joiningGroupId++;
        }
    }

    for (let day = 0; day < CURRENT_GROUP_RUNNING_DAY; day++) {
        const currentTodoWrittenAt = new Date(currentGroupStartAt);
        currentTodoWrittenAt.setDate(currentTodoWrittenAt.getDate() + day);
        currentTodoWrittenAt.setHours(8, 0, 0, 0);

        const currentTodoCertifyAt = new Date(currentTodoWrittenAt);
        currentTodoCertifyAt.setHours(17, 0, 0, 0);

        for (let memberId = 1; memberId <= MEMBER_COUNT; memberId++) {
            const reviewerId = getReviewerId(memberId);
            for (let i = 0; i < GROUP_PER_MEMBER_COUNT; i++) {
                let reviewStatusToggle = true;
                for (let j = 0; j < DAY_TODO_PER_MEMBER_COUNT; j++) {
                    // 2. daily_todo & daily_todo_history 데이터 생성
                    const currentTodoId = dailyTodoId++;
                    daily_todo_data.push([currentTodoId, groupIdsByMember[memberId - 1][i], memberId, `td=${currentTodoId}`, "CERTIFY_COMPLETED", currentTodoWrittenAt, todayDate, null]);
                    daily_todo_history_data.push([currentTodoId, currentTodoId, currentTodoWrittenAt, todayDate, null]);
                    todoIdsByMember[memberId - 1].push(currentTodoId);

                    // 3. daily_todo_certification & daily_todo_certification_reviewer 데이터 생성
                    // 현재는 투두 개수 == 인증 개수로 고정했지만 인증 개수만 다르게 하고 싶다면 아래 로직을 별도 루프로 분리해야함.
                    const currentTodoCertificationId = dailyTodoCertificationId++;
                    const reviewStatus = reviewStatusToggle ? "APPROVE" : "REJECT";
                    const reviewFeedBack = reviewStatusToggle ? `와 미쳤다 ㄷㄷ - ${currentTodoCertificationId}` : `그게 최선인가? ㅎ - ${currentTodoCertificationId}`;
                    reviewStatusToggle = !reviewStatusToggle;

                    daily_todo_certification_data.push([currentTodoCertificationId, currentTodoId, `tc-${currentTodoId}`, `http://certification-media.site/m${memberId}/t${currentTodoId}`, reviewStatus, reviewFeedBack, currentTodoCertifyAt, todayDate, null]);
                    daily_todo_certification_reviewer_data.push([currentTodoCertificationId, currentTodoCertificationId, reviewerId, todayDate, null]);
                }
            }
        }
    }

    console.log(`✅ 데이터 생성 완료!\n`);

    return {
        batch_size,
        member_data,
        notification_token_data,
        daily_todo_stats_data,
        challenge_group_data,
        challenge_group_member_data,
        daily_todo_data,
        daily_todo_history_data,
        daily_todo_certification_data,
        daily_todo_certification_reviewer_data,
        last_selected_challenge_group_record_data
    };
}

// ================== 헬퍼 함수 ==================
export function getChallengeGroupIdsPerMember() {
    const groupIdsByMember = Array.from({ length: MEMBER_COUNT }, () => []);

    let joiningGroupId = challengeGroupId;
    for (let i = 0; i < GROUP_PER_MEMBER_COUNT; i++) {
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

function getReviewerId(memberId) {
    // 그룹의 시작과 끝 ID를 구함
    const groupIndex = Math.floor((memberId - 1) / MEMBER_PER_GROUP_COUNT);
    const startId = groupIndex * MEMBER_PER_GROUP_COUNT + 1;         // 예: 1, 21, 41, ...

    // 그룹 내 offset (0~19)
    const offset = memberId - startId;

    // 매칭 규칙: 0 <-> 19, 1 <-> 18, ...
    const reviewerOffset = MEMBER_PER_GROUP_COUNT - 1 - offset;

    return startId + reviewerOffset;
}
