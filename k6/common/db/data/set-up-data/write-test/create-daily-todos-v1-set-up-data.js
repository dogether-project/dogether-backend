/**
 * # 데일리 투두 작성 V1 API set up data
 * - 각 사용자(VU)는 정확히 1개의 그룹에만 참여
 * - 그룹/멤버 배치 규칙: 멤버를 GROUP_CAPACITY씩 블록으로 나눠, 블록별 1그룹 생성
 * - row_inserted_at = 스크립트 실행일(삭제/정리 편의)
 * - k6 헬퍼: 멤버별 투두 대상 그룹 id 배열 반환 (각 원소는 길이 1)
 */

import {getCurrentDateInKst, getDateNDaysLaterInKst} from "../../../util/time-util.js";

import {getLastInsertedIds} from "../../dummy-data/dummy-data-1.js";
// import {getLastInsertedIds} from "../../dummy-data/dummy-data-2.js";


// ===== 파라미터 (자유 조절) =====
const MEMBER_COUNT = 100;   // 총 사용자 수 (k6 vus와 맞추면 편함)
const GROUP_CAPACITY = 20;  // 그룹 정원(2~20)
const DURATION_PER_GROUP = 28;

// ===== 고정값/시간 =====
const FIRST_MEMBER_ID = 1;
const CURRENT_ROW_INSERTED_AT = getCurrentDateInKst();

const DummyDataLastInsertedIds = getLastInsertedIds();
const FIRST_CHALLENGE_GROUP_ID = DummyDataLastInsertedIds.lastInsertedDummyChallengeGroupId + 1;
const FIRST_CHALLENGE_GROUP_MEMBER_ID = DummyDataLastInsertedIds.lastInsertedDummyChallengeGroupMemberId + 1;

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
    const endAt = getDateNDaysLaterInKst(DURATION_PER_GROUP-1);  // 오늘 포함 28일
    const createdAt = startAt;
    const rowInsertedAt = CURRENT_ROW_INSERTED_AT;
    const rowUpdatedAt = null;

    const rows = [];
    for (let i = 0; i < totalGroupCount; i++) {
        const id = FIRST_CHALLENGE_GROUP_ID + i;
        rows.push([
            id,
            `g-${id}`,          // name
            GROUP_CAPACITY,     // maximumMemberCount
            `jc-${id}`,         // joinCode
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

// ===== 메인: Set-up 데이터 =====
export function createSetUpData() {
    console.log("✏️ 데일리 투두 작성 V1 API set up 데이터 생성 시작.");

    const batch_size = 2000;
    const challenge_group_data = createChallengeGroupData();
    const { challenge_group_member_data, last_selected_challenge_group_record_data } =
        createChallengeGroupMemberAndLastSelected();

    console.log("✅ 데이터 생성 완료!\n");

    return {
        batch_size,
        challenge_group_data,
        challenge_group_member_data,
        last_selected_challenge_group_record_data,
    };
}

// ===== k6 헬퍼 =====
// 각 멤버가 투두를 작성할 그룹 id 배열 반환 (각 원소는 길이 1의 배열)
export function getTodoTargetGroupIdsPerMember() {
    const result = new Array(MEMBER_COUNT);
    for (let i = 0; i < MEMBER_COUNT; i++) {
        const memberId = FIRST_MEMBER_ID + i;
        result[i] = groupIdOfMember(memberId); // 바로 groupId 값만 넣음
    }
    return result;
}
