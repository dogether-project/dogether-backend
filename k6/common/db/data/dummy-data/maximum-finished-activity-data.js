/**
 * # 최대치의 종료된 활동 데이터 생성
 * - 사용자 ${MEMBER_COUNT}명이 과거 ${USAGE_DAYS_BEFORE_CURRENT_GROUP}일간 이론상 가능한 최대치의 활동을 진행했다고 가정
 */
import { getDateNDaysAgoInKst, getEndDateFromStartAgoAndDuration } from "../../util/time-util.js";

// ===== 공통 옵션 =====
const MEMBER_COUNT = 100;                     // 전체 회원수 (⭐️ 핵심)
const DURATION_PER_CURRENT_GROUP = 28;        // set-up 스크립트의 현재 그룹 총 활동일(예: 3/7/14/28) (⭐️ 핵심)
const JOINING_GROUP_COUNT_PER_MEMBER = 5;     // 회원 1명이 동시 참여하는 그룹 개수
const MEMBER_COUNT_PER_GROUP = 20;            // 그룹 당 인원수 (MEMBER_COUNT의 약수 권장)
const ONE_DAY_TODO_COUNT_PER_GROUP_MEMBER = 10;// 1일 1인 당 투두 개수 (1~10)
const FIRST_MEMBER_ID = 1;

// ===== 과거 생성 기간 설정 =====
// 원시 사용일수: "현재 그룹 시작일 이전" 총 사용일
const USAGE_DAYS_BEFORE_CURRENT_GROUP = 365 + (DURATION_PER_CURRENT_GROUP - 1);

// 과거 그룹 길이(예: 3일, 7일, 14일, 28일)
const DURATION_PER_PAST_GROUP = 3;

// === 핵심 보정 ===
// 마지막 과거 그룹의 종료일(endAt)이 정확히 T일 전이 되도록 USAGE를 보정
// endAgo_last = U' - C*Dp + 1 = T  를 만족하도록 (U' - (T-1)) % Dp == 0 로 조정
const TARGET_LAST_END_AGO = DURATION_PER_CURRENT_GROUP; // set-up의 현재 그룹 총 활동일과 동일하게 맞춤
const RAW = USAGE_DAYS_BEFORE_CURRENT_GROUP - (TARGET_LAST_END_AGO - 1);
const DELTA =
    (DURATION_PER_PAST_GROUP - (RAW % DURATION_PER_PAST_GROUP) + DURATION_PER_PAST_GROUP) % DURATION_PER_PAST_GROUP;

// 보정된 사용일수
const ADJUSTED_USAGE_DAYS_BEFORE_CURRENT_GROUP = USAGE_DAYS_BEFORE_CURRENT_GROUP + DELTA;

// 사이클 수(정확히 나누어떨어짐이 보장됨)
const PAST_GROUP_CYCLE_COUNT =
    (ADJUSTED_USAGE_DAYS_BEFORE_CURRENT_GROUP - (TARGET_LAST_END_AGO - 1)) / DURATION_PER_PAST_GROUP;

// 파생 통계(멤버별 과거 todo/인증 수 등)
const PAST_TODO_COUNT_PER_MEMBER =
    (ONE_DAY_TODO_COUNT_PER_GROUP_MEMBER * DURATION_PER_PAST_GROUP) *
    JOINING_GROUP_COUNT_PER_MEMBER * PAST_GROUP_CYCLE_COUNT;
const PAST_CERTIFICATION_COUNT_PER_MEMBER = PAST_TODO_COUNT_PER_MEMBER;
const PAST_APPROVE_COUNT_PER_MEMBER = Math.floor(PAST_CERTIFICATION_COUNT_PER_MEMBER / 2);
const PAST_REJECT_COUNT_PER_MEMBER = PAST_CERTIFICATION_COUNT_PER_MEMBER - PAST_APPROVE_COUNT_PER_MEMBER;

export const getLastInsertedIds = () => {
    const lastInsertedDummyChallengeGroupId = (MEMBER_COUNT / MEMBER_COUNT_PER_GROUP) * JOINING_GROUP_COUNT_PER_MEMBER * PAST_GROUP_CYCLE_COUNT;
    const lastInsertedDummyChallengeGroupMemberId = MEMBER_COUNT * JOINING_GROUP_COUNT_PER_MEMBER * PAST_GROUP_CYCLE_COUNT;
    const lastInsertedDummyDailyTodoId = PAST_TODO_COUNT_PER_MEMBER * MEMBER_COUNT;
    const lastInsertedDummyDailyTodoHistoryId = PAST_TODO_COUNT_PER_MEMBER * MEMBER_COUNT;
    const lastInsertedDummyDailyTodoCertificationId = PAST_CERTIFICATION_COUNT_PER_MEMBER * MEMBER_COUNT;
    const lastInsertedDummyDailyTodoCertificationReviewerId = PAST_CERTIFICATION_COUNT_PER_MEMBER * MEMBER_COUNT;

    return {
        lastInsertedDummyChallengeGroupId,
        lastInsertedDummyChallengeGroupMemberId,
        lastInsertedDummyDailyTodoId,
        lastInsertedDummyDailyTodoHistoryId,
        lastInsertedDummyDailyTodoCertificationId,
        lastInsertedDummyDailyTodoCertificationReviewerId
    };
};

export function createDummyData() {
    console.log('👷 최대치의 종료된 활동 더미 데이터 생성 시작.\n');

    const batch_size = 1000;
    const member_data = createMemberData();
    const notification_token_data = createNotificationTokenData();
    const daily_todo_stats_data = createDailyTodoStatsData();
    const challenge_group_data = createChallengeGroupData();
    const challenge_group_member_data = createChallengeGroupMemberData();
    const { daily_todo_data, daily_todo_history_data } = createDailyTodoAndDailyTodoHistoryData();
    const { daily_todo_certification_data, daily_todo_certification_reviewer_data } =
        createDailyTodoCertificationAndReviewerData(daily_todo_data);

    console.log(`✅ 더미 데이터 생성 완료!\n`);
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
    };
}

const createMemberData = () => {
    console.log("🗂️ member 테이블 더미 데이터 생성중...");

    const member_data = [];
    const profileImageUrl = `http://profile-image.site`;

    // 가장 오래된 시점으로 통일
    const createdAt = getDateNDaysAgoInKst(ADJUSTED_USAGE_DAYS_BEFORE_CURRENT_GROUP);
    const rowInsertedAt = createdAt;
    const rowUpdatedAt = null;

    for (let i = 0; i < MEMBER_COUNT; i++) {
        const id = FIRST_MEMBER_ID + i;
        const providerId = `pid-${id}`;
        const name = `m-${id}`;

        member_data.push([
            id, providerId, name, profileImageUrl, createdAt, rowInsertedAt, rowUpdatedAt
        ]);
    }
    return member_data;
};

const createNotificationTokenData = () => {
    console.log("🗂️ notification_token 테이블 더미 데이터 생성중...");

    const notification_token_data = [];
    const rowInsertedAt = getDateNDaysAgoInKst(ADJUSTED_USAGE_DAYS_BEFORE_CURRENT_GROUP);
    const rowUpdatedAt = null;

    for (let i = 0; i < MEMBER_COUNT; i++) {
        const id = FIRST_MEMBER_ID + i;
        const memberId = FIRST_MEMBER_ID + i;
        const tokenValue = `t-${id}`;

        notification_token_data.push([ id, memberId, tokenValue, rowInsertedAt, rowUpdatedAt ]);
    }
    return notification_token_data;
};

const createDailyTodoStatsData = () => {
    console.log("🗂️ daily_todo_stats 테이블 더미 데이터 생성중...");

    const daily_todo_stats_data = [];
    const certificatedCount = PAST_CERTIFICATION_COUNT_PER_MEMBER;
    const approvedCount = PAST_APPROVE_COUNT_PER_MEMBER;
    const rejectedCount = PAST_REJECT_COUNT_PER_MEMBER;
    const rowInsertedAt = getDateNDaysAgoInKst(ADJUSTED_USAGE_DAYS_BEFORE_CURRENT_GROUP);
    const rowUpdatedAt = null;

    for (let i = 0; i < MEMBER_COUNT; i++) {
        const id = FIRST_MEMBER_ID + i;
        const memberId = FIRST_MEMBER_ID + i;
        daily_todo_stats_data.push([
            id, memberId, certificatedCount, approvedCount, rejectedCount, rowInsertedAt, rowUpdatedAt
        ]);
    }
    return daily_todo_stats_data;
};

const createChallengeGroupData = () => {
    console.log("🗂️ challenge_group 테이블 더미 데이터 생성중...");

    const challenge_group_data = [];
    const maximumMemberCount = MEMBER_COUNT_PER_GROUP;
    const status = 'FINISHED';

    // i = 0 → 가장 오래된 그룹, i 증가할수록 최근으로
    for (let i = 0; i < PAST_GROUP_CYCLE_COUNT; i++) {
        const startAgo = ADJUSTED_USAGE_DAYS_BEFORE_CURRENT_GROUP - (i * DURATION_PER_PAST_GROUP);
        const startAt = getDateNDaysAgoInKst(startAgo);
        const endAt = getEndDateFromStartAgoAndDuration(startAgo, DURATION_PER_PAST_GROUP);

        const createdAt = startAt;
        const rowInsertedAt = startAt;
        const rowUpdatedAt = null;

        const blockCount = Math.ceil(MEMBER_COUNT / MEMBER_COUNT_PER_GROUP);
        const groupCountPerCycle = blockCount * JOINING_GROUP_COUNT_PER_MEMBER;

        for (let j = 0; j < groupCountPerCycle; j++) {
            const id = (i * groupCountPerCycle + j) + 1;
            const name = `g-${id}`;
            const joinCode = `jc-${id}`;

            challenge_group_data.push([
                id,              // id
                name,            // name
                maximumMemberCount,
                joinCode,
                status,
                startAt,
                endAt,
                createdAt,
                rowInsertedAt,
                rowUpdatedAt,
            ]);
        }
    }
    return challenge_group_data;
};

const createChallengeGroupMemberData = () => {
    console.log("🗂️ challenge_group_member 테이블 더미 데이터 생성중...");

    const challenge_group_member_data = [];

    const cycles = PAST_GROUP_CYCLE_COUNT;
    const blockSize = MEMBER_COUNT_PER_GROUP;
    const blockCount = Math.ceil(MEMBER_COUNT / blockSize);
    const groupCountPerCycle = blockCount * JOINING_GROUP_COUNT_PER_MEMBER;

    let challengeGroupMemberId = 1;

    for (let ci = 0; ci < cycles; ci++) {
        const startAgo = ADJUSTED_USAGE_DAYS_BEFORE_CURRENT_GROUP - (ci * DURATION_PER_PAST_GROUP);
        const createdAt = getDateNDaysAgoInKst(startAgo);
        const rowInsertedAt = createdAt;
        const rowUpdatedAt = null;

        const cycleGroupIdBase = ci * groupCountPerCycle;

        for (let b = 0; b < blockCount; b++) {
            const blockStartMemberId = b * blockSize + FIRST_MEMBER_ID;
            const blockEndMemberId = Math.min((b + 1) * blockSize, MEMBER_COUNT);

            for (let k = 0; k < JOINING_GROUP_COUNT_PER_MEMBER; k++) {
                const challengeGroupId = cycleGroupIdBase + (b * JOINING_GROUP_COUNT_PER_MEMBER) + (k + 1);

                for (let memberId = blockStartMemberId; memberId <= blockEndMemberId; memberId++) {
                    challenge_group_member_data.push([
                        challengeGroupMemberId++,
                        challengeGroupId,
                        memberId,
                        createdAt,
                        rowInsertedAt,
                        rowUpdatedAt,
                    ]);
                }
            }
        }
    }
    return challenge_group_member_data;
};

const createDailyTodoAndDailyTodoHistoryData = () => {
    console.log("🗂️ daily_todo & daily_todo_history 테이블 더미 데이터 생성중...");

    const daily_todo_data = [];
    const daily_todo_history_data = [];

    let dailyTodoId = 1;
    let dailyTodoHistoryId = 1;

    for (let ci = 0; ci < PAST_GROUP_CYCLE_COUNT; ci++) {
        const startAgo = ADJUSTED_USAGE_DAYS_BEFORE_CURRENT_GROUP - (ci * DURATION_PER_PAST_GROUP);

        for (let memberId = FIRST_MEMBER_ID; memberId <= MEMBER_COUNT; memberId++) {
            const groupIds = getGroupIdsForMemberInCycle(memberId, ci);

            for (const challengeGroupId of groupIds) {
                for (let d = 0; d < DURATION_PER_PAST_GROUP; d++) {
                    const writtenAt = getDateNDaysAgoInKst(startAgo - d);
                    const rowInsertedAt = writtenAt;
                    const rowUpdatedAt = null;

                    for (let n = 0; n < ONE_DAY_TODO_COUNT_PER_GROUP_MEMBER; n++) {
                        const currentTodoId = dailyTodoId++;
                        const content = `td=${currentTodoId}`;
                        const status = 'CERTIFY_COMPLETED';

                        daily_todo_data.push([
                            currentTodoId,
                            challengeGroupId,
                            memberId,
                            content,
                            status,
                            writtenAt,
                            rowInsertedAt,
                            rowUpdatedAt,
                        ]);

                        daily_todo_history_data.push([
                            dailyTodoHistoryId++,
                            currentTodoId,
                            writtenAt,
                            rowInsertedAt,
                            rowUpdatedAt
                        ]);
                    }
                }
            }
        }
    }
    return { daily_todo_data, daily_todo_history_data };
};

function getGroupIdsForMemberInCycle(memberId, ci) {
    const blockIndex = Math.floor((memberId - FIRST_MEMBER_ID) / MEMBER_COUNT_PER_GROUP);
    const blockCount = Math.ceil(MEMBER_COUNT / MEMBER_COUNT_PER_GROUP);
    const groupsPerCycle = blockCount * JOINING_GROUP_COUNT_PER_MEMBER;

    const cycleGroupIdBase = ci * groupsPerCycle;
    const groupIds = [];

    for (let k = 0; k < JOINING_GROUP_COUNT_PER_MEMBER; k++) {
        const gid = cycleGroupIdBase + (blockIndex * JOINING_GROUP_COUNT_PER_MEMBER) + (k + 1);
        groupIds.push(gid);
    }
    return groupIds;
}

const createDailyTodoCertificationAndReviewerData = (daily_todo_data) => {
    console.log("🗂️ daily_todo_certification & daily_todo_certification_reviewer 테이블 더미 데이터 생성중...");

    const daily_todo_certification_data = [];
    const daily_todo_certification_reviewer_data = [];

    // ====== 그룹 멤버 캐시 ======
    const blockSize = MEMBER_COUNT_PER_GROUP;
    const blockCount = Math.ceil(MEMBER_COUNT / blockSize);
    const groupsPerCycle = blockCount * JOINING_GROUP_COUNT_PER_MEMBER;

    const groupMembersCache = new Map(); // groupId -> memberId[]
    function getMembersOfGroup(groupId) {
        if (groupMembersCache.has(groupId)) return groupMembersCache.get(groupId);
        const idx = groupId - 1;
        const indexInCycle = idx % groupsPerCycle;

        const b = Math.floor(indexInCycle / JOINING_GROUP_COUNT_PER_MEMBER);
        const startMemberId = b * blockSize + FIRST_MEMBER_ID;
        const endMemberId = Math.min((b + 1) * blockSize, MEMBER_COUNT);

        const members = [];
        for (let m = startMemberId; m <= endMemberId; m++) members.push(m);

        groupMembersCache.set(groupId, members);
        return members;
    }

    // ====== 멤버별 todo 목록 ======
    const memberTodos = new Map(); // memberId -> [{todoId, groupId, writtenAt}]
    for (const row of daily_todo_data) {
        const [todoId, groupId, writerId, , , writtenAt] = row;
        if (!memberTodos.has(writerId)) memberTodos.set(writerId, []);
        memberTodos.get(writerId).push({ todoId, groupId, writtenAt });
    }

    // ====== 본 생성 ======
    let certificationId = 1;
    let reviewerIdSeq = 1;
    const mediaUrlBase = 'http://certification-media.site';

    for (let memberId = FIRST_MEMBER_ID; memberId <= MEMBER_COUNT; memberId++) {
        const todos = memberTodos.get(memberId) || [];

        let approveLeft = PAST_APPROVE_COUNT_PER_MEMBER;
        let rejectLeft = PAST_REJECT_COUNT_PER_MEMBER;

        const totalNeeded = approveLeft + rejectLeft;
        if (totalNeeded !== todos.length) {
            approveLeft = Math.min(approveLeft, todos.length);
            rejectLeft = Math.min(rejectLeft, Math.max(0, todos.length - approveLeft));
        }

        for (let i = 0; i < todos.length; i++) {
            const { todoId, groupId, writtenAt } = todos[i];
            const reviewStatus = (approveLeft > 0) ? 'APPROVE' : 'REJECT';
            if (reviewStatus === 'APPROVE') approveLeft--; else rejectLeft--;

            const createdAt = writtenAt;
            const rowInsertedAt = createdAt;
            const rowUpdatedAt = null;

            const currentCertificationId = certificationId++;
            const content = `tc-${currentCertificationId}`;
            const mediaUrl = `${mediaUrlBase}/m${memberId}/t${todoId}`;
            const reviewFeedback = (reviewStatus === 'APPROVE') ? '인정 ㅎ' : '노인정 빼엑';

            // (주의) 이 파일의 certification 배열 구조는 기존 프로젝트의 과거 DDL 순서에 맞춤
            daily_todo_certification_data.push([
                currentCertificationId,
                todoId,
                content,
                mediaUrl,
                reviewStatus,
                reviewFeedback,
                createdAt,
                rowInsertedAt,
                rowUpdatedAt,
            ]);

            // 리뷰어: 같은 그룹 내 본인 제외 랜덤. 후보 없으면 생성하지 않음
            const candidates = getMembersOfGroup(groupId).filter(mid => mid !== memberId);
            if (candidates.length > 0) {
                const reviewerMemberId = candidates[Math.floor(Math.random() * candidates.length)];
                daily_todo_certification_reviewer_data.push([
                    reviewerIdSeq++,
                    currentCertificationId,
                    reviewerMemberId,
                    rowInsertedAt,
                    rowUpdatedAt,
                ]);
            }
        }
    }

    return { daily_todo_certification_data, daily_todo_certification_reviewer_data };
};
