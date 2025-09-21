import {getDateNDaysAgoInKst, calculateNextDate, calculateEndAt} from "../../util/time-util.js";

// =========== 상수 ===========
const MEMBER_COUNT = 400;   // 총 회원 수
const MEMBER_PER_GROUP_COUNT = 20;  // 그룹당 참여 회원 수
const GROUP_PER_MEMBER_COUNT = 3;  // 회원당 참여 그룹 수
const PAST_GROUP_RUNNING_DAY = 28;  // 과거 그룹 진행일 수
const CURRENT_GROUP_RUNNING_DAY = 28;  // 현재 그룹 진행일 수
const TOTAL_PAST_ACTIVITY_DAY = 365 * 2;  // 총 과거 진행일 수
const TOTAL_ACTIVITY_CYCLE = Math.ceil(TOTAL_PAST_ACTIVITY_DAY / PAST_GROUP_RUNNING_DAY);
const PAST_GROUP_ACTIVITY_START_AT = getDateNDaysAgoInKst(TOTAL_ACTIVITY_CYCLE * PAST_GROUP_RUNNING_DAY + CURRENT_GROUP_RUNNING_DAY);
const ONE_CYCLE_PER_GROUP_COUNT = MEMBER_COUNT / MEMBER_PER_GROUP_COUNT * GROUP_PER_MEMBER_COUNT;   // 한 사이클에 존재하는 그룹 개수
const DAY_TODO_PER_MEMBER_COUNT = 8;  // 회원이 하루에 작성하는 투두 개수
const DAY_TODO_CERTIFICATION_PER_MEMBER_COUNT = DAY_TODO_PER_MEMBER_COUNT;  // 회원이 하루 인증하는 투두 인증 개수

// =========== 최종 데이터 개수 ===========
const TOTAL_MEMBER_COUNT = MEMBER_COUNT;
const TOTAL_NOTIFICATION_TOKEN_COUNT = MEMBER_COUNT;
const TOTAL_DAILY_TODO_STATS_COUNT = MEMBER_COUNT;
const TOTAL_CHALLENGE_GROUP_COUNT = TOTAL_ACTIVITY_CYCLE * ONE_CYCLE_PER_GROUP_COUNT;
const TOTAL_CHALLENGE_GROUP_MEMBER_COUNT = MEMBER_COUNT * GROUP_PER_MEMBER_COUNT * TOTAL_ACTIVITY_CYCLE;
const TOTAL_DAILY_TODO_COUNT = MEMBER_COUNT * DAY_TODO_PER_MEMBER_COUNT * PAST_GROUP_RUNNING_DAY * GROUP_PER_MEMBER_COUNT * TOTAL_ACTIVITY_CYCLE;
const TOTAL_DAILY_TODO_HISTORY_COUNT = TOTAL_DAILY_TODO_COUNT;
const TOTAL_DAILY_TODO_CERTIFICATION_COUNT = MEMBER_COUNT * DAY_TODO_CERTIFICATION_PER_MEMBER_COUNT * PAST_GROUP_RUNNING_DAY * GROUP_PER_MEMBER_COUNT * TOTAL_ACTIVITY_CYCLE;
const TOTAL_DAILY_TODO_CERTIFICATION_REVIEWER_COUNT = TOTAL_DAILY_TODO_CERTIFICATION_COUNT;

// =========== 메인 로직 ===========
export function createPastActivityData() {
    const batch_size = 100000;
    const member_data = [];
    const notification_token_data = [];
    const daily_todo_stats_data = [];
    const challenge_group_data = [];
    const challenge_group_member_data = [];
    const daily_todo_data = [];
    const daily_todo_history_data = [];
    const daily_todo_certification_data = [];
    const daily_todo_certification_reviewer_data = [];

    let memberId = 1;  // member & notification_token & daily_todo_stats
    let challengeGroupId = 1;
    let challengeGroupMemberId = 1;
    let dailyTodoId = 1;  // daily_todo & daily_todo_history
    let dailyTodoCertificationId = 1;

    // 캐싱
    const groupIdsByMember = Array.from({ length: MEMBER_COUNT }, () => []);
    const todoIdsByMember = Array.from({ length: MEMBER_COUNT }, () => []);

    console.log(`👷[Const Past Activity Data] 과거 활동 데이터 생성중... (총 과거 진행일 수 : ${TOTAL_ACTIVITY_CYCLE * PAST_GROUP_RUNNING_DAY}일)`);

    // 1. member & notification_token & daily_todo_stats 데이터 생성
    const memberCreatedAt = new Date(PAST_GROUP_ACTIVITY_START_AT);
    memberCreatedAt.setHours(6, 0, 0, 0);
    const dailyTodoCertificationCountPerMember = TOTAL_DAILY_TODO_CERTIFICATION_COUNT / TOTAL_MEMBER_COUNT;
    const approvedDailyTodoCertificationCountPerMember = Math.ceil(dailyTodoCertificationCountPerMember / 2);
    const rejectedDailyTodoCertificationCountPerMember = dailyTodoCertificationCountPerMember - approvedDailyTodoCertificationCountPerMember;
    for (let i = 0; i < MEMBER_COUNT; i++) {
        const currentMemberId = memberId++;
        member_data.push([currentMemberId, `pid-${currentMemberId}`, `m-${currentMemberId}`, `http://profile-image.site/${currentMemberId}`, memberCreatedAt, memberCreatedAt, null]);
        notification_token_data.push([currentMemberId, currentMemberId, `t-${currentMemberId}`, memberCreatedAt, null]);
        daily_todo_stats_data.push([currentMemberId, currentMemberId, dailyTodoCertificationCountPerMember, approvedDailyTodoCertificationCountPerMember, rejectedDailyTodoCertificationCountPerMember, memberCreatedAt, null]);
    }

    for (let cycle = 0; cycle < TOTAL_ACTIVITY_CYCLE; cycle++) {
        const groupStartAtInCycle = calculateNextDate(PAST_GROUP_ACTIVITY_START_AT, cycle, PAST_GROUP_RUNNING_DAY);
        groupStartAtInCycle.setHours(7, 0, 0, 0);
        const groupEndAtInCycle = calculateEndAt(groupStartAtInCycle, PAST_GROUP_RUNNING_DAY);
        const firstGroupIdInCycle = 1 + cycle * ONE_CYCLE_PER_GROUP_COUNT;

        // 2. challenge_group & challenge_group_member 데이터 생성
        for (let i = 0; i < ONE_CYCLE_PER_GROUP_COUNT; i++) {
            const currentChallengeGroupId = challengeGroupId++;
            challenge_group_data.push([currentChallengeGroupId, `g-${currentChallengeGroupId}`, MEMBER_PER_GROUP_COUNT, `jc-${currentChallengeGroupId}`, "FINISHED", groupStartAtInCycle, groupEndAtInCycle, groupStartAtInCycle, groupStartAtInCycle, null]);
        }

        let groupId = firstGroupIdInCycle;
        for (let i = 0; i < GROUP_PER_MEMBER_COUNT; i++) {
            for (let j = 0; j < MEMBER_COUNT / MEMBER_PER_GROUP_COUNT; j++) {
                let memberId = 1 + j * MEMBER_PER_GROUP_COUNT;
                for (let k = 0; k < MEMBER_PER_GROUP_COUNT; k++) {
                    let currentMemberId = memberId++;
                    challenge_group_member_data.push([challengeGroupMemberId++, groupId, currentMemberId, groupStartAtInCycle, groupStartAtInCycle, null]);
                    groupIdsByMember[currentMemberId - 1].push(groupId);
                }
                groupId++;
            }
        }

        for (let day = 0; day < PAST_GROUP_RUNNING_DAY; day++) {
            const currentTodoWrittenAt = new Date(groupStartAtInCycle);
            currentTodoWrittenAt.setDate(currentTodoWrittenAt.getDate() + day);
            currentTodoWrittenAt.setHours(8, 0, 0, 0);

            const currentTodoCertifyAt = new Date(currentTodoWrittenAt);
            currentTodoCertifyAt.setHours(17, 0, 0, 0);

            for (let memberId = 1; memberId <= MEMBER_COUNT; memberId++) {
                const reviewerId = getReviewerId(memberId);
                for (let i = 0; i < GROUP_PER_MEMBER_COUNT; i++) {
                    let reviewStatusToggle = true;
                    for (let j = 0; j < DAY_TODO_PER_MEMBER_COUNT; j++) {
                        // 3. daily_todo & daily_todo_history 데이터 생성
                        const currentTodoId = dailyTodoId++;
                        daily_todo_data.push([currentTodoId, groupIdsByMember[memberId - 1][i], memberId, `td=${currentTodoId}`, "CERTIFY_COMPLETED", currentTodoWrittenAt, currentTodoWrittenAt, null]);
                        daily_todo_history_data.push([currentTodoId, currentTodoId, currentTodoWrittenAt, currentTodoWrittenAt, null]);
                        todoIdsByMember[memberId - 1].push(currentTodoId);

                        // 4. daily_todo_certification & daily_todo_certification_reviewer 데이터 생성
                        // 현재는 투두 개수 == 인증 개수로 고정했지만 인증 개수만 다르게 하고 싶다면 아래 로직을 별도 루프로 분리해야함.
                        const currentTodoCertificationId = dailyTodoCertificationId++;
                        const reviewStatus = reviewStatusToggle ? "APPROVE" : "REJECT";
                        const reviewFeedBack = reviewStatusToggle ? `와 미쳤다 ㄷㄷ - ${currentTodoCertificationId}` : `그게 최선인가? ㅎ - ${currentTodoCertificationId}`;
                        reviewStatusToggle = !reviewStatusToggle;

                        daily_todo_certification_data.push([currentTodoCertificationId, currentTodoId, `tc-${currentTodoId}`, `http://certification-media.site/m${memberId}/t${currentTodoId}`, reviewStatus, reviewFeedBack, currentTodoCertifyAt, currentTodoCertifyAt, null]);
                        daily_todo_certification_reviewer_data.push([currentTodoCertificationId, currentTodoCertificationId, reviewerId, currentTodoCertifyAt, null]);
                    }
                }
            }
        }
    }

    function getReviewerId(memberId) {
        // 그룹의 시작과 끝 ID를 구함
        const groupIndex = Math.floor((memberId - 1) / MEMBER_PER_GROUP_COUNT);
        const startId = groupIndex * MEMBER_PER_GROUP_COUNT + 1;         // 예: 1, 21, 41, ...
        const endId = startId + MEMBER_PER_GROUP_COUNT - 1;              // 예: 20, 40, 60, ...

        // 그룹 내 offset (0~19)
        const offset = memberId - startId;

        // 매칭 규칙: 0 <-> 19, 1 <-> 18, ...
        const reviewerOffset = MEMBER_PER_GROUP_COUNT - 1 - offset;

        return startId + reviewerOffset;
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
        last_selected_challenge_group_record_data: []
    };
}

// ================ 헬퍼 함수 ================
export function getLastInsertedIds() {
    return {
        lastInsertedDummyMemberId: TOTAL_MEMBER_COUNT,
        lastInsertedDummyNotificationTokenId: TOTAL_NOTIFICATION_TOKEN_COUNT,
        lastInsertedDummyDailyTodoStatsId: TOTAL_DAILY_TODO_STATS_COUNT,

        lastInsertedDummyChallengeGroupId: TOTAL_CHALLENGE_GROUP_COUNT,
        lastInsertedDummyChallengeGroupMemberId: TOTAL_CHALLENGE_GROUP_MEMBER_COUNT,
        lastInsertedDummyLastSelectedChallengeGroupRecordId: 0, // Past는 기록 안함

        lastInsertedDummyDailyTodoId: TOTAL_DAILY_TODO_COUNT,
        lastInsertedDummyDailyTodoHistoryId: TOTAL_DAILY_TODO_HISTORY_COUNT,
        lastInsertedDummyDailyTodoCertificationId: TOTAL_DAILY_TODO_CERTIFICATION_COUNT,
        lastInsertedDummyDailyTodoCertificationReviewerId: TOTAL_DAILY_TODO_CERTIFICATION_REVIEWER_COUNT,
    };
}
