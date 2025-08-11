/**
 * # 챌린지 그룹 참여 V1 API set up data
 * - MEMBER_COUNT: VU 수에 맞춰 유동 생성
 * - row_inserted_at: 스크립트 실행일로 고정
 * - k6에서 사용할 joinCode 배열 헬퍼 export
 */

import {getCurrentDateInKst, getDateNDaysLaterInKst} from "../../../util/time-util.js";

import {getLastInsertedIds} from "../../dummy-data/only-member-info-data.js";
// import {getLastInsertedIds} from "../../dummy-data/maximum-finished-activity-data.js";

// ========= 테스트 파라미터 =========
export const MEMBER_COUNT = 100;            // k6 vus와 맞추면 좋음
export const GROUP_CAPACITY = 20;           // 그룹 최대 인원(조절 가능: 2~20)
export const DURATION_PER_GROUP = 28;       // 응답에서 duration 확인용

// ========= ID & 타임 =========
const DummyDataLastInsertedIds = getLastInsertedIds();

const FIRST_CHALLENGE_GROUP_ID = DummyDataLastInsertedIds.lastInsertedDummyChallengeGroupId + 1;
const CURRENT_ROW_INSERTED_AT   = getCurrentDateInKst();

// ========= 내부 유틸 =========
function calcGroupCount(memberCount = MEMBER_COUNT, capacity = GROUP_CAPACITY) {
    const cap = Math.max(2, Math.min(20, capacity));
    return Math.ceil(memberCount / cap);
}

function buildGroups({ memberCount = MEMBER_COUNT, capacity = GROUP_CAPACITY }) {
    const groupCount = calcGroupCount(memberCount, capacity);

    const startAt     = getCurrentDateInKst();                         // 오늘 시작
    const endAt       = getDateNDaysLaterInKst(DURATION_PER_GROUP-1);  // 오늘 포함 28일
    const createdAt   = startAt;
    const rowInserted = CURRENT_ROW_INSERTED_AT;
    const rowUpdated  = null;

    const status = "RUNNING"; // 가입 테스트용으로 모집중 상태
    const challenge_group_data = [];
    const joinCodes = [];

    for (let i = 0; i < groupCount; i++) {
        const id       = FIRST_CHALLENGE_GROUP_ID + i;
        const name     = `g-${id}`;
        const joinCode = `jc-${id}`;

        challenge_group_data.push([
            id,                    // id
            name,                  // name
            capacity,              // maximumMemberCount
            joinCode,              // joinCode
            status,                // status
            startAt,               // startAt
            endAt,                 // endAt
            createdAt,             // createdAt
            rowInserted,           // row_inserted_at
            rowUpdated,            // row_updated_at
        ]);

        joinCodes.push(joinCode);
    }

    return { challenge_group_data, joinCodes };
}

// ========= 메인: 셋업 데이터 생성 =========
export function createSetUpData({
                                    memberCount = MEMBER_COUNT,
                                    capacity = GROUP_CAPACITY,
                                } = {}) {
    console.log("✏️ 챌린지 그룹 참여 V1 API set up 데이터 생성 시작.");

    const { challenge_group_data } = buildGroups({ memberCount, capacity });

    console.log(`✅ 그룹 ${challenge_group_data.length}개 생성(용량=${capacity}, 대상 회원수=${memberCount}\n`);
    return {
        batch_size: 2000,
        challenge_group_data,
    };
}

export function getJoinCodesPerMember() {
    const { joinCodes } = buildGroups({
        memberCount: MEMBER_COUNT,
        capacity: GROUP_CAPACITY,
    });
    const groupCount = joinCodes.length;

    const vuJoinCodes = new Array(MEMBER_COUNT);
    for (let i = 0; i < MEMBER_COUNT; i++) {
        vuJoinCodes[i] = joinCodes[i % groupCount];
    }
    return vuJoinCodes;
}
