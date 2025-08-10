/**
 * # 최대치의 현재 활동 데이터 생성
 * - 현재 진행중인 그룹에 사용자들이 최대치 소속되어 이론상 진행할 수 있는 최대한의 활동을 진행했다고 가정
 */

// 데이터 생성 공통 옵션
import {getCurrentDateInKst, getDateNDaysAgoInKst} from "../../../util/time-util.js";
import { getLastInsertedIds } from "../../dummy-data/dummy-data-1.js";
// import { getLastInsertedIds } from "../dummy-data/dummy-data-2.js";

const MEMBER_COUNT = 100;   // 전체 회원수 (⭐️ 핵심), dummy-data-2의 MEMBER_COUNT와 일치해야함.
const JOINING_GROUP_COUNT_PER_MEMBER = 5;   // 회원 한명당 참여한 그룹 개수 (최대 5개까지 가능)
const DURATION_PER_GROUP = 28;    // 챌린지 그룹 진행일 (3, 7, 14, 28)
const MEMBER_COUNT_PER_GROUP = 20;   // 챌린지 그룹 하나당 참여중인 인원수 (2 ~ 20 범위 이면서 MEMBER_COUNT의 약수여야함.)
const ONE_DAY_TODO_COUNT_PER_GROUP_MEMBER = 10; // 각 그룹에서 참여 인원이 매일 작성하는 투두 개수 (1 ~ 10 범위 정수)
const TODO_COUNT_PER_MEMBER = (ONE_DAY_TODO_COUNT_PER_GROUP_MEMBER * DURATION_PER_GROUP) * JOINING_GROUP_COUNT_PER_MEMBER;    // 사용자별 총 투두 개수
const CERTIFICATION_COUNT_PER_MEMBER = TODO_COUNT_PER_MEMBER;  // 사용자별 총 투두 인증 개수
const APPROVE_COUNT_PER_MEMBER = Math.floor(CERTIFICATION_COUNT_PER_MEMBER / 2);  // 사용자별 총 인정 받은 투두 인증 개수
const REJECT_COUNT_PER_MEMBER = CERTIFICATION_COUNT_PER_MEMBER - APPROVE_COUNT_PER_MEMBER;   // 사용자별 총 노인정 받은 투두 인증 개수

const lastInsertedIds = getLastInsertedIds();
const FIRST_MEMBER_ID = 1;  // 첫번째 회원의 id
const FIRST_CHALLENGE_GROUP_ID = lastInsertedIds.lastInsertedDummyChallengeGroupId + 1;
const FIRST_CHALLENGE_GROUP_MEMBER_ID = lastInsertedIds.lastInsertedDummyChallengeGroupMemberId + 1;
const FIRST_DAILY_TODO_ID = lastInsertedIds.lastInsertedDummyDailyTodoId + 1;
const FIRST_DAILY_TODO_HISTORY_ID = lastInsertedIds.lastInsertedDummyDailyTodoHistoryId + 1;
const FIRST_DAILY_TODO_CERTIFICATION_ID = lastInsertedIds.lastInsertedDummyDailyTodoCertificationId + 1;
const FIRST_DAILY_TODO_CERTIFICATION_REVIEWER_ID = lastInsertedIds.lastInsertedDummyDailyTodoCertificationReviewerId + 1;
const CURRENT_ROW_INSERTED_AT = getCurrentDateInKst();  // Set up 데이터를 손쉽게 지우기 위해서 스크립트를 실행하는 날짜로 통일

export function createSetUpData() {
    console.log('✏️ [Set up data] - 최대치의 현재 활동 데이터 생성 시작!\n');

    const batch_size = 2000;
    const challenge_group_data = createChallengeGroupData();
    const { challenge_group_member_data, last_selected_challenge_group_record_data } = createChallengeGroupMemberAndLastSelectedChallengeGroupRecordData();
    const { daily_todo_data, daily_todo_history_data } = createDailyTodoAndDailyTodoHistoryData();
    const { daily_todo_certification_data, daily_todo_certification_reviewer_data } = createDailyTodoCertificationAndReviewerData(daily_todo_data);

    console.log(`✅ 데이터 생성 완료!\n`);
    return {
        batch_size,
        challenge_group_data,
        challenge_group_member_data,
        last_selected_challenge_group_record_data,
        daily_todo_data,
        daily_todo_history_data,
        daily_todo_certification_data,
        daily_todo_certification_reviewer_data,
    };
}

const createChallengeGroupData = () => {
    console.log("🗂️ challenge_group 테이블 셋업 데이터 생성중...");

    const challenge_group_data = [];

    const maximumMemberCount = 20;
    const status = 'RUNNING';
    const startAt = getDateNDaysAgoInKst(DURATION_PER_GROUP - 1);
    const endAt = getCurrentDateInKst();
    const createdAt = startAt;
    const rowInsertedAt = CURRENT_ROW_INSERTED_AT;
    const rowUpdatedAt = null;

    const totalGroupCount = Math.ceil(MEMBER_COUNT / MEMBER_COUNT_PER_GROUP) * JOINING_GROUP_COUNT_PER_MEMBER;
    for (let j = 0; j < totalGroupCount; j++) {
        const currentChallengeGroupId = FIRST_CHALLENGE_GROUP_ID + j
        const name = `g-${currentChallengeGroupId}`;
        const joinCode = `jc-${currentChallengeGroupId}`;

        challenge_group_data.push([
            currentChallengeGroupId,
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

    return challenge_group_data;
}

const createChallengeGroupMemberAndLastSelectedChallengeGroupRecordData = () => {
    console.log("🗂️ challenge_group_member & last_selected_challenge_group_record 테이블 셋업 데이터 생성중...");

    const challenge_group_member_data = [];
    const last_selected_challenge_group_record_data = [];

    const blockSize = MEMBER_COUNT_PER_GROUP;
    const blockCount = Math.ceil(MEMBER_COUNT / blockSize);

    // 그룹 id는 이전 insert의 마지막 id 다음부터 시작
    let challengeGroupMemberId = FIRST_CHALLENGE_GROUP_MEMBER_ID;
    let lastSelId = 1;

    const createdAt = getDateNDaysAgoInKst(DURATION_PER_GROUP - 1);
    const rowInsertedAt = CURRENT_ROW_INSERTED_AT;
    const rowUpdatedAt = null;

    for (let b = 0; b < blockCount; b++) {
        const blockStartMemberId = b * blockSize + FIRST_MEMBER_ID;
        const blockEndMemberId = Math.min((b + 1) * blockSize, MEMBER_COUNT);

        for (let k = 0; k < JOINING_GROUP_COUNT_PER_MEMBER; k++) {
            // ⚠️ 베이스 id 반영
            const challengeGroupId = FIRST_CHALLENGE_GROUP_ID + (b * JOINING_GROUP_COUNT_PER_MEMBER) + k;

            for (let memberId = blockStartMemberId; memberId <= blockEndMemberId; memberId++) {
                // 참여 관계
                challenge_group_member_data.push([
                    challengeGroupMemberId++,
                    challengeGroupId,
                    memberId,
                    createdAt,
                    rowInsertedAt,
                    rowUpdatedAt,
                ]);

                // 멤버당 마지막으로 선택한 그룹 기록 (현재 셋업의 마지막 k)
                if (k === JOINING_GROUP_COUNT_PER_MEMBER - 1) {
                    last_selected_challenge_group_record_data.push([
                        lastSelId++,
                        challengeGroupId,
                        memberId,
                        rowInsertedAt,
                        rowUpdatedAt,
                    ]);
                }
            }
        }
    }

    return { challenge_group_member_data, last_selected_challenge_group_record_data };
};

const createDailyTodoAndDailyTodoHistoryData = () => {
    console.log("🗂️ daily_todo & daily_todo_history 테이블 셋업 데이터 생성중...");

    const daily_todo_data = [];
    const daily_todo_history_data = [];

    // 멤버→그룹 매핑을 위해 새로 만든 그룹들의 시작 id
    const blockSize = MEMBER_COUNT_PER_GROUP;

    let dailyTodoId = FIRST_DAILY_TODO_ID;
    let dailyTodoHistoryId = FIRST_DAILY_TODO_HISTORY_ID;

    // 날짜 범위: 오늘이 마지막 날, 시작일은 (DURATION_PER_GROUP - 1)일 전
    for (let memberId = FIRST_MEMBER_ID; memberId <= MEMBER_COUNT; memberId++) {
        const blockIndex = Math.floor((memberId - FIRST_MEMBER_ID) / blockSize);

        // 이 멤버가 속한 이번 사이클의 그룹들
        const groupIds = [];
        for (let k = 0; k < JOINING_GROUP_COUNT_PER_MEMBER; k++) {
            const gid = FIRST_CHALLENGE_GROUP_ID + (blockIndex * JOINING_GROUP_COUNT_PER_MEMBER) + k;
            groupIds.push(gid);
        }

        for (const challengeGroupId of groupIds) {
            for (let d = 0; d < DURATION_PER_GROUP; d++) {
                const writtenAt = getDateNDaysAgoInKst((DURATION_PER_GROUP - 1) - d);
                const rowInsertedAt = CURRENT_ROW_INSERTED_AT;
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
                        rowUpdatedAt
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

    return { daily_todo_data, daily_todo_history_data };
};

const createDailyTodoCertificationAndReviewerData = (daily_todo_data) => {
    console.log("🗂️ daily_todo_certification & daily_todo_certification_reviewer 테이블 셋업 데이터 생성중...");

    const daily_todo_certification_data = [];
    const daily_todo_certification_reviewer_data = [];

    // ====== 그룹 멤버 후보 계산을 위한 베이스/상수 ======
    const blockSize = MEMBER_COUNT_PER_GROUP;
    const blockCount = Math.ceil(MEMBER_COUNT / blockSize);
    const groupsPerCycle = blockCount * JOINING_GROUP_COUNT_PER_MEMBER;

    // 특정 groupId의 소속 멤버 id 리스트 반환 (이번 셋업의 규칙 기반)
    const groupMembersCache = new Map();
    function getMembersOfGroup(groupId) {
        if (groupMembersCache.has(groupId)) return groupMembersCache.get(groupId);
        const offset = groupId - FIRST_CHALLENGE_GROUP_ID;
        const indexInCycle = ((offset % groupsPerCycle) + groupsPerCycle) % groupsPerCycle;
        const b = Math.floor(indexInCycle / JOINING_GROUP_COUNT_PER_MEMBER);
        const startMemberId = b * blockSize + FIRST_MEMBER_ID;
        const endMemberId = Math.min((b + 1) * blockSize, MEMBER_COUNT);
        const members = [];
        for (let m = startMemberId; m <= endMemberId; m++) members.push(m);
        groupMembersCache.set(groupId, members);
        return members;
    }

    // ====== 멤버별 투두 목록 맵 ======
    const memberTodos = new Map(); // memberId -> [{todoId, groupId, writtenAt}]
    for (const row of daily_todo_data) {
        const [todoId, groupId, writerId, , , writtenAt] = row;
        if (!memberTodos.has(writerId)) memberTodos.set(writerId, []);
        memberTodos.get(writerId).push({ todoId, groupId, writtenAt });
    }

    // ====== 본 생성 ======
    let todoCertificationIdBase = FIRST_DAILY_TODO_CERTIFICATION_ID;
    let todoCertificationReviewerId = FIRST_DAILY_TODO_CERTIFICATION_REVIEWER_ID;
    for (let memberId = FIRST_MEMBER_ID; memberId <= MEMBER_COUNT; memberId++) {
        const todos = memberTodos.get(memberId) || [];

        // 승인/거절 목표치
        let approveLeft = APPROVE_COUNT_PER_MEMBER;
        let rejectLeft  = REJECT_COUNT_PER_MEMBER;

        // 안전 보정(혹시 환경 상수가 바뀌어 총량이 다를 경우)
        const totalNeeded = approveLeft + rejectLeft;
        if (totalNeeded !== todos.length) {
            approveLeft = Math.min(approveLeft, todos.length);
            rejectLeft  = Math.min(rejectLeft, Math.max(0, todos.length - approveLeft));
        }

        for (let i = 0; i < todos.length; i++) {
            const currentTodoCertificationId = todoCertificationIdBase++;
            const { todoId, groupId, writtenAt } = todos[i];
            const reviewStatus = (approveLeft > 0) ? 'APPROVE' : 'REJECT';
            if (reviewStatus === 'APPROVE') approveLeft--; else rejectLeft--;

            const createdAt = writtenAt;
            const rowInsertedAt = CURRENT_ROW_INSERTED_AT;
            const rowUpdatedAt = null;

            const content = `tc-${todoId}`;
            const mediaUrl = `http://certification-media.site/m${memberId}/t${todoId}`;
            const reviewFeedback = (reviewStatus === 'APPROVE') ? '인정 ㅎ' : '노인정 빼엑';

            daily_todo_certification_data.push([
                currentTodoCertificationId,
                todoId,
                content,
                mediaUrl,
                reviewStatus,
                reviewFeedback,
                createdAt,
                rowInsertedAt,
                rowUpdatedAt,
            ]);

            // 리뷰어 후보: 같은 그룹 내 본인 제외
            const candidates = getMembersOfGroup(groupId).filter(mid => mid !== memberId);
            if (candidates.length > 0) {
                const reviewerId = candidates[Math.floor(Math.random() * candidates.length)];

                daily_todo_certification_reviewer_data.push([
                    todoCertificationReviewerId++,
                    currentTodoCertificationId,
                    reviewerId,
                    rowInsertedAt,
                    rowUpdatedAt,
                ]);
            }
        }
    }

    return { daily_todo_certification_data, daily_todo_certification_reviewer_data };
};

export const getChallengeGroupIdsPerMember = () => {
    // 멤버 수만큼의 배열을 만들고, 각 멤버의 그룹 ID 배열을 채워서 반환
    const result = Array.from({ length: MEMBER_COUNT }, (_, idx) => {
        const memberId = FIRST_MEMBER_ID + idx;

        // 멤버가 속한 블록 인덱스 (0-based)
        const blockIndex = Math.floor((memberId - FIRST_MEMBER_ID) / MEMBER_COUNT_PER_GROUP);

        // 이 멤버가 속한 그룹들의 ID 계산 (현재 셋업 규칙과 동일)
        const groupIds = [];
        for (let k = 0; k < JOINING_GROUP_COUNT_PER_MEMBER; k++) {
            const gid = FIRST_CHALLENGE_GROUP_ID + (blockIndex * JOINING_GROUP_COUNT_PER_MEMBER) + k;
            groupIds.push(gid);
        }

        return groupIds;
    });

    return result;
};

export const getChallengeGroupMembersPerMember = () => {
    const result = Array.from({ length: MEMBER_COUNT }, () => []);

    // 멤버별 그룹 ID 계산
    const groupIdsPerMember = Array.from({ length: MEMBER_COUNT }, (_, idx) => {
        const memberId = FIRST_MEMBER_ID + idx;
        const blockIndex = Math.floor((memberId - FIRST_MEMBER_ID) / MEMBER_COUNT_PER_GROUP);

        const groupIds = [];
        for (let k = 0; k < JOINING_GROUP_COUNT_PER_MEMBER; k++) {
            const gid = FIRST_CHALLENGE_GROUP_ID + (blockIndex * JOINING_GROUP_COUNT_PER_MEMBER) + k;
            groupIds.push(gid);
        }
        return groupIds;
    });

    // 그룹별 멤버 목록 캐싱
    const groupMembersMap = new Map();
    for (let memberIdx = 0; memberIdx < MEMBER_COUNT; memberIdx++) {
        const memberId = FIRST_MEMBER_ID + memberIdx;
        const blockIndex = Math.floor((memberId - FIRST_MEMBER_ID) / MEMBER_COUNT_PER_GROUP);
        const startId = blockIndex * MEMBER_COUNT_PER_GROUP + FIRST_MEMBER_ID;
        const endId = Math.min((blockIndex + 1) * MEMBER_COUNT_PER_GROUP, MEMBER_COUNT);
        for (const gid of groupIdsPerMember[memberIdx]) {
            if (!groupMembersMap.has(gid)) {
                const members = [];
                for (let m = startId; m <= endId; m++) {
                    members.push(m);
                }
                groupMembersMap.set(gid, members);
            }
        }
    }

    // 멤버별로 같은 그룹원 목록 구성 (자기 자신 제외)
    for (let memberIdx = 0; memberIdx < MEMBER_COUNT; memberIdx++) {
        const memberId = FIRST_MEMBER_ID + memberIdx;
        const groupIds = groupIdsPerMember[memberIdx];
        const memberSet = new Set();

        for (const gid of groupIds) {
            const members = groupMembersMap.get(gid) || [];
            members.forEach(m => {
                if (m !== memberId) { // 자기 자신 제외
                    memberSet.add(m);
                }
            });
        }

        result[memberIdx] = Array.from(memberSet).sort((a, b) => a - b);
    }

    return result;
};
