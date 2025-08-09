/**
 * [ 데이터 셋 특정 ]
 * 사용자 ${MEMBER_COUNT}명이 과거 ${USAGE_DAYS_BEFORE_CURRENT_GROUP}일간 이론상 가능한 최대치의 활동을 진행했다고 가정
 */

import { getDateNDaysAgoInKst, getEndDateFromStartAgoAndDuration } from "../../util/db-util.js";

// 데이터 생성 공통 옵션
const MEMBER_COUNT = 100;   // 전체 회원수 (⭐️ 핵심)
const JOINING_GROUP_COUNT_PER_MEMBER = 5;   // 회원 한명당 참여한 그룹 개수 (최대 5개까지 가능)
const DURATION_PER_GROUP = 3;    // 챌린지 그룹 진행일 (3, 7, 14, 28)
const MEMBER_COUNT_PER_GROUP = 20;   // 챌린지 그룹 하나당 참여중인 인원수 (2 ~ 20 범위 이면서 MEMBER_COUNT의 약수여야함.)
const ONE_DAY_TODO_COUNT_PER_GROUP_MEMBER = 10; // 각 그룹에서 참여 인원이 매일 작성하는 투두 개수 (1 ~ 10 범위 정수)

const FIRST_MEMBER_ID = 1;  // 첫번째 회원의 id

/**
 * [ 현재 참여중인 챌린지 그룹 시작일 이전 과거 데이터 생성 옵션 ]
 */
const USAGE_DAYS_BEFORE_CURRENT_GROUP = 365;    // 현재 참여 그룹 시작일 이전 앱 사용일 (⭐️ 핵심)
const PAST_GROUP_CYCLE_COUNT = Math.floor(USAGE_DAYS_BEFORE_CURRENT_GROUP / DURATION_PER_GROUP);    // 현재 참여 그룹 시작일 이전 진행한 그룹 사이클
const PAST_TODO_COUNT_PER_MEMBER = (ONE_DAY_TODO_COUNT_PER_GROUP_MEMBER * DURATION_PER_GROUP) * JOINING_GROUP_COUNT_PER_MEMBER * PAST_GROUP_CYCLE_COUNT;    // 현재 참여 그룹 시작일 이전 사용자별 총 투두 개수
const PAST_CERTIFICATION_COUNT_PER_MEMBER = PAST_TODO_COUNT_PER_MEMBER;  // 현재 참여 그룹 시작일 이전 사용자별 총 투두 인증 개수
const PAST_APPROVE_COUNT_PER_MEMBER = Math.floor(PAST_CERTIFICATION_COUNT_PER_MEMBER / 2);  // 현재 참여 그룹 시작일 이전 사용자별 총 인정 받은 투두 인증 개수
const PAST_REJECT_COUNT_PER_MEMBER = PAST_CERTIFICATION_COUNT_PER_MEMBER - PAST_APPROVE_COUNT_PER_MEMBER;   // 현재 참여 그룹 시작일 이전 사용자별 총 노인정 받은 투두 인증 개수

export function createDummyData() {
    console.log('👷 더미 데이터 MK.2 생성 시작!\n');

    const batch_size = 1000;
    const member_data = createMemberData();
    const notification_token_data = createNotificationTokenData();
    const daily_todo_stats_data = createDailyTodoStatsData();
    const challenge_group_data = createChallengeGroupData();
    const { challenge_group_member_data, last_selected_challenge_group_record_data } = createChallengeGroupMemberAndLastSelectedChallengeGroupRecordData();
    const { daily_todo_data, daily_todo_history_data } = createDailyTodoAndDailyTodoHistoryData();
    const { daily_todo_certification_data, daily_todo_certification_reviewer_data } = createDailyTodoCertificationAndReviewerData(daily_todo_data);

    console.log(`✅ 더미 데이터 생성 완료!\n`);
    return {
        batch_size,
        member_data,
        notification_token_data,
        daily_todo_stats_data,
        challenge_group_data,
        challenge_group_member_data,
        last_selected_challenge_group_record_data,
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
    const createdAt = getDateNDaysAgoInKst(USAGE_DAYS_BEFORE_CURRENT_GROUP);
    const rowInsertedAt = getDateNDaysAgoInKst(USAGE_DAYS_BEFORE_CURRENT_GROUP);
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
}

const createNotificationTokenData = () => {
    console.log("🗂️ notification_token 테이블 더미 데이터 생성중...");

    const notification_token_data = [];

    const rowInsertedAt = getDateNDaysAgoInKst(USAGE_DAYS_BEFORE_CURRENT_GROUP);
    const rowUpdatedAt = null;
    for (let i = 0; i < MEMBER_COUNT; i++) {
        const id = FIRST_MEMBER_ID + i;
        const memberId = FIRST_MEMBER_ID + i;
        const tokenValue = `t-${id}`;

        notification_token_data.push([
            id, memberId, tokenValue, rowInsertedAt, rowUpdatedAt
        ]);
    }

    return notification_token_data;
}

const createDailyTodoStatsData = () => {
    console.log("🗂️ daily_todo_stats 테이블 더미 데이터 생성중...");

    const daily_todo_stats_data = [];

    const certificatedCount = PAST_CERTIFICATION_COUNT_PER_MEMBER;
    const approvedCount = PAST_APPROVE_COUNT_PER_MEMBER;
    const rejectedCount = PAST_REJECT_COUNT_PER_MEMBER;
    const rowInsertedAt = getDateNDaysAgoInKst(USAGE_DAYS_BEFORE_CURRENT_GROUP);
    const rowUpdatedAt = null;
    for (let i = 0; i < MEMBER_COUNT; i++) {
        const id = FIRST_MEMBER_ID + i;
        const memberId = FIRST_MEMBER_ID + i;

        daily_todo_stats_data.push([
            id, memberId, certificatedCount, approvedCount, rejectedCount, rowInsertedAt, rowUpdatedAt
        ]);
    }

    return daily_todo_stats_data;
}

const createChallengeGroupData = () => {
    console.log("🗂️ challenge_group 테이블 더미 데이터 생성중...");

    const challenge_group_data = [];

    const maximumMemberCount = 20;
    const status = 'FINISHED';
    for (let i = 0; i < PAST_GROUP_CYCLE_COUNT; i++) {
        const startAt = getDateNDaysAgoInKst(USAGE_DAYS_BEFORE_CURRENT_GROUP - (i * DURATION_PER_GROUP));
        const endAt = getEndDateFromStartAgoAndDuration(USAGE_DAYS_BEFORE_CURRENT_GROUP - (i * DURATION_PER_GROUP), DURATION_PER_GROUP);
        const createdAt = getDateNDaysAgoInKst(USAGE_DAYS_BEFORE_CURRENT_GROUP - (i * DURATION_PER_GROUP));
        const rowInsertedAt = getDateNDaysAgoInKst(USAGE_DAYS_BEFORE_CURRENT_GROUP - (i * DURATION_PER_GROUP));
        const rowUpdatedAt = null;

        const groupCountPerCycle = Math.ceil(MEMBER_COUNT / MEMBER_COUNT_PER_GROUP) * JOINING_GROUP_COUNT_PER_MEMBER;
        for (let j = 0; j < groupCountPerCycle; j++) {
            const id = (i * groupCountPerCycle + j) + 1;
            const name = `g-${id}`;
            const joinCode = `jc-${id}`;

            challenge_group_data.push([
                id,
                name,
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
}

const createChallengeGroupMemberAndLastSelectedChallengeGroupRecordData = () => {
    console.log("🗂️ challenge_group_member & last_selected_challenge_group_record 테이블 더미 데이터 생성중...");

    const challenge_group_member_data = [];
    const last_selected_challenge_group_record_data = [];

    const cycles = PAST_GROUP_CYCLE_COUNT;
    const blockSize = MEMBER_COUNT_PER_GROUP;
    const blockCount = Math.ceil(MEMBER_COUNT / blockSize);
    const groupCountPerCycle = blockCount * JOINING_GROUP_COUNT_PER_MEMBER;

    let challengeGroupMemberId = 1;
    let lastSelectedChallengeGroupRecordId = 1;

    for (let ci = 0; ci < cycles; ci++) {
        const startAgo = USAGE_DAYS_BEFORE_CURRENT_GROUP - (ci * DURATION_PER_GROUP);
        const createdAt = getDateNDaysAgoInKst(startAgo);
        const rowInsertedAt = createdAt;
        const rowUpdatedAt = null;

        const cycleGroupIdBase = ci * groupCountPerCycle;

        for (let b = 0; b < blockCount; b++) {
            const blockStartMemberId = b * blockSize + FIRST_MEMBER_ID;
            const blockEndMemberId = Math.min((b + 1) * blockSize, MEMBER_COUNT);

            // 이 블록이 속한 그룹 묶음 (JOINING_GROUP_COUNT_PER_MEMBER개)
            // 규칙: 같은 블록의 멤버들은 해당 사이클에서 연속된 k개 그룹에 모두 속한다.
            // 그룹 id = cycleGroupIdBase + (b * JOINING_GROUP_COUNT_PER_MEMBER) + (k + 1)
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

                    // 사용자들이 마지막으로 참여한 그룹 정보 저장
                    if (ci === cycles - 1 && k === JOINING_GROUP_COUNT_PER_MEMBER - 1) {
                        last_selected_challenge_group_record_data.push([
                            lastSelectedChallengeGroupRecordId++,
                            challengeGroupId,
                            memberId,
                            rowInsertedAt,
                            rowUpdatedAt,
                        ]);
                    }
                }
            }
        }
    }

    return { challenge_group_member_data, last_selected_challenge_group_record_data };
}

const createDailyTodoAndDailyTodoHistoryData = () => {
    console.log("🗂️ daily_todo & daily_todo_history 테이블 더미 데이터 생성중...");

    const daily_todo_data = [];
    const daily_todo_history_data = [];

    let dailyTodoId = 1;
    let dailyTodoHistoryId = 1;
    for (let ci = 0; ci < PAST_GROUP_CYCLE_COUNT; ci++) {
        const startAgo = USAGE_DAYS_BEFORE_CURRENT_GROUP - (ci * DURATION_PER_GROUP);

        for (let memberId = FIRST_MEMBER_ID; memberId <= MEMBER_COUNT; memberId++) {
            const groupIds = getGroupIdsForMemberInCycle(memberId, ci);

            for (const challengeGroupId of groupIds) {
                for (let d = 0; d < DURATION_PER_GROUP; d++) {
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
}

function getGroupIdsForMemberInCycle(memberId, ci) {
    const blockIndex = Math.floor((memberId - FIRST_MEMBER_ID) / MEMBER_COUNT_PER_GROUP);
    const blockCount = Math.ceil(MEMBER_COUNT / MEMBER_COUNT_PER_GROUP);
    const groupsPerCycle = blockCount * JOINING_GROUP_COUNT_PER_MEMBER;

    const cycleGroupIdBase = ci * groupsPerCycle; // +1은 아래서 보정
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

    // ====== 준비: 그룹 멤버 캐시 ======
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

    // ====== 준비: 멤버별 todo 목록 ======
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

            // 인증 레코드 생성
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

            // 리뷰어: 같은 그룹 내 본인 제외 랜덤. 후보 없으면 "생성하지 않음".
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
