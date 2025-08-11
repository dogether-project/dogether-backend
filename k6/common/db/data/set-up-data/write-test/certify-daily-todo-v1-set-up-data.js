/**
 * # 투두 인증 V1 API set up data
 * - 각 사용자(VU)는 정확히 1개의 그룹에만 참여
 * - 각 사용자당 daily_todo 10개 생성(모두 CERTIFY_PENDING)
 * - daily_todo_history 동시 생성
 * - row_inserted_at = 스크립트 실행일
 * - k6 헬퍼:
 *   - getCertifiableTodoIdsPerMember(): 멤버별 인증 대상 todoId 배열(각 10개)
 *   - getOneCertifiableTodoIdPerMember(): 멤버별 인증 대상 todoId 한 개(편의)
 */

import { getCurrentDateInKst, getDateNDaysLaterInKst } from "../../../util/time-util.js";

import { getLastInsertedIds } from "../../dummy-data/only-member-info-data.js";
// import { getLastInsertedIds } from "../../dummy-data/maximum-finished-activity-data.js";

// ===== 파라미터 (자유 조절) =====
const MEMBER_COUNT = 100;   // 총 사용자 수 (k6 vus와 맞추면 편함)
const GROUP_CAPACITY = 20;  // 그룹 정원(2~20)
const DURATION_PER_GROUP = 28;
const TODOS_PER_MEMBER = 10; // 멤버당 생성할 투두 수 (인증 대상)

// ===== 고정값/시간 =====
const FIRST_MEMBER_ID = 1;
const CURRENT_ROW_INSERTED_AT = getCurrentDateInKst();

const STATUS_CERTIFY_PENDING = "CERTIFY_PENDING";

const DummyDataLastInsertedIds = getLastInsertedIds();
const FIRST_CHALLENGE_GROUP_ID = DummyDataLastInsertedIds.lastInsertedDummyChallengeGroupId + 1;
const FIRST_CHALLENGE_GROUP_MEMBER_ID = DummyDataLastInsertedIds.lastInsertedDummyChallengeGroupMemberId + 1;
const FIRST_DAILY_TODO_ID = DummyDataLastInsertedIds.lastInsertedDummyDailyTodoId + 1;
const FIRST_DAILY_TODO_HISTORY_ID = DummyDataLastInsertedIds.lastInsertedDummyDailyTodoHistoryId + 1;

// ===== 내부 규칙(결정적) =====
// 멤버를 정원 단위로 잘라 각 블록당 그룹 1개 생성
const blockCount = Math.ceil(MEMBER_COUNT / GROUP_CAPACITY);
const totalGroupCount = blockCount;

// 멤버 → 소속 그룹 ID (결정적, 1인 1그룹)
function groupIdOfMember(memberId) {
    const blockIndex = Math.floor((memberId - FIRST_MEMBER_ID) / GROUP_CAPACITY);
    return FIRST_CHALLENGE_GROUP_ID + blockIndex;
}

// 해당 그룹의 멤버 목록 (결정적)
function membersOfGroup(groupId) {
    const offset = groupId - FIRST_CHALLENGE_GROUP_ID;
    const blockIndex = offset; // 블록 = 그룹 인덱스
    const start = FIRST_MEMBER_ID + blockIndex * GROUP_CAPACITY;
    const end = Math.min(start + GROUP_CAPACITY - 1, FIRST_MEMBER_ID + MEMBER_COUNT - 1);
    const members = [];
    for (let m = start; m <= end; m++) members.push(m);
    return members;
}

// ===== 1) challenge_group =====
function createChallengeGroupData() {
    const status = "RUNNING";
    const startAt = getCurrentDateInKst();                         // 오늘 시작
    const endAt = getDateNDaysLaterInKst(DURATION_PER_GROUP - 1);  // 오늘 포함 28일
    const createdAt = startAt;
    const rowInsertedAt = CURRENT_ROW_INSERTED_AT;
    const rowUpdatedAt = null;

    const rows = [];
    for (let i = 0; i < totalGroupCount; i++) {
        const id = FIRST_CHALLENGE_GROUP_ID + i;
        rows.push([
            id,
            `g-${id}`,      // name
            GROUP_CAPACITY, // maximumMemberCount
            `jc-${id}`,     // joinCode
            status,
            startAt,
            endAt,
            createdAt,
            rowInsertedAt,
            rowUpdatedAt,
        ]);
    }
    return rows;
}

// ===== 2) challenge_group_member & last_selected_challenge_group_record =====
function createChallengeGroupMemberAndLastSelected() {
    const createdAt = getCurrentDateInKst();
    const rowInsertedAt = CURRENT_ROW_INSERTED_AT;
    const rowUpdatedAt = null;

    const challenge_group_member_data = [];
    const last_selected_challenge_group_record_data = [];

    let challengeGroupMemberId = FIRST_CHALLENGE_GROUP_MEMBER_ID;
    let lastSelId = 1;

    for (let gIdx = 0; gIdx < totalGroupCount; gIdx++) {
        const groupId = FIRST_CHALLENGE_GROUP_ID + gIdx;
        const members = membersOfGroup(groupId);
        for (const memberId of members) {
            // 참여 관계 (1인 1그룹)
            challenge_group_member_data.push([
                challengeGroupMemberId++,
                groupId,
                memberId,
                createdAt,
                rowInsertedAt,
                rowUpdatedAt,
            ]);

            // “마지막 선택 그룹” 기록(여기선 유일 그룹)
            last_selected_challenge_group_record_data.push([
                lastSelId++,
                groupId,
                memberId,
                rowInsertedAt,
                rowUpdatedAt,
            ]);
        }
    }

    return { challenge_group_member_data, last_selected_challenge_group_record_data };
}

// ===== (헬퍼에 사용할) 멤버별 본인 투두 id 계산 규칙 =====
// memberId 기준으로, 해당 멤버의 투두는 연속된 TODOS_PER_MEMBER개 id를 차지
function calcTodoIdsOfMember(memberId) {
    const index = memberId - FIRST_MEMBER_ID; // 0-based
    const startId = FIRST_DAILY_TODO_ID + index * TODOS_PER_MEMBER;
    const ids = [];
    for (let k = 0; k < TODOS_PER_MEMBER; k++) ids.push(startId + k);
    return ids;
}

// ===== 3) daily_todo & daily_todo_history =====
function createDailyTodoAndHistoryData() {
    const daily_todo_data = [];
    const daily_todo_history_data = [];

    let dailyTodoHistoryId = FIRST_DAILY_TODO_HISTORY_ID;

    const writtenAt = getCurrentDateInKst(); // 오늘 작성(단순화)
    const rowInsertedAt = CURRENT_ROW_INSERTED_AT;
    const rowUpdatedAt = null;

    for (let i = 0; i < MEMBER_COUNT; i++) {
        const memberId = FIRST_MEMBER_ID + i;
        const challengeGroupId = groupIdOfMember(memberId);
        const todoIds = calcTodoIdsOfMember(memberId);

        for (const todoId of todoIds) {
            const content = `td=${todoId}`;
            const status = STATUS_CERTIFY_PENDING;

            daily_todo_data.push([
                todoId,
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
                todoId,
                writtenAt,
                rowInsertedAt,
                rowUpdatedAt,
            ]);
        }
    }

    return { daily_todo_data, daily_todo_history_data };
}

// ===== 메인: Set-up 데이터 =====
export function createSetUpData() {
    console.log("✏️ 투두 인증 V1 API set up 데이터 생성 시작.");

    const batch_size = 2000;

    const challenge_group_data = createChallengeGroupData();
    const { challenge_group_member_data, last_selected_challenge_group_record_data } =
        createChallengeGroupMemberAndLastSelected();

    const { daily_todo_data, daily_todo_history_data } = createDailyTodoAndHistoryData();

    console.log("✅ 데이터 생성 완료!\n");

    return {
        batch_size,
        challenge_group_data,
        challenge_group_member_data,
        last_selected_challenge_group_record_data,
        daily_todo_data,
        daily_todo_history_data,
    };
}

// ===== k6 헬퍼 =====
// 1) 멤버별 인증 가능한 todoId 배열 (각 10개씩)
export function getCertifiableTodoIdsPerMember() {
    const result = new Array(MEMBER_COUNT);
    for (let i = 0; i < MEMBER_COUNT; i++) {
        const memberId = FIRST_MEMBER_ID + i;
        result[i] = calcTodoIdsOfMember(memberId);
    }
    return result;
}

// 2) 멤버별 인증 가능한 todoId 한 개(편의용; 첫 번째 것)
export function getOneCertifiableTodoIdPerMember() {
    const result = new Array(MEMBER_COUNT);
    for (let i = 0; i < MEMBER_COUNT; i++) {
        const memberId = FIRST_MEMBER_ID + i;
        const ids = calcTodoIdsOfMember(memberId);
        result[i] = ids[0];
    }
    return result;
}
