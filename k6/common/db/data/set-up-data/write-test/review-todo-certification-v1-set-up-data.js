/**
 * # 데일리 투두 인증 검사 V1 API - Set up 데이터
 * - 각 사용자(VU)는 정확히 1개의 그룹에만 참여
 * - 회원당 10개의 투두 작성 + 전부 인증 생성 (review_status='REVIEW_PENDING', review_feedback=null)
 * - 리뷰어: 같은 그룹 내 '다음' 멤버(원형), 항상 1명 → 모든 회원이 최소 1건 이상 검사자로 배정됨
 * - row_inserted_at = 스크립트 실행일(정리 편의)
 *
 * k6 헬퍼:
 *  - getPendingCertificationIdsPerReviewer(): number[][]  (result[memberId-1] => 그 회원이 검사자인 인증ID 배열)
 *  - getOnePendingCertificationIdPerReviewer(): number[]  (result[memberId-1] => 그 회원이 검사자인 인증ID 한 건)
 */

import { getCurrentDateInKst } from "../../../util/time-util.js";

import { getLastInsertedIds } from "../../dummy-data/dummy-data-1.js";
// import { getLastInsertedIds } from "../../dummy-data/dummy-data-2.js";

// ===== 파라미터 =====
const MEMBER_COUNT = 100;   // k6 vus와 맞춰 사용
const GROUP_CAPACITY = 20;  // 2~20
const TODOS_PER_MEMBER = 10;

// ===== 고정값/시간 =====
const FIRST_MEMBER_ID = 1;
const CURRENT_ROW_INSERTED_AT = getCurrentDateInKst();

const DummyDataLastInsertedIds = getLastInsertedIds();
const FIRST_CHALLENGE_GROUP_ID = DummyDataLastInsertedIds.lastInsertedDummyChallengeGroupId + 1;
const FIRST_CHALLENGE_GROUP_MEMBER_ID = DummyDataLastInsertedIds.lastInsertedDummyChallengeGroupMemberId + 1;
const FIRST_DAILY_TODO_ID = DummyDataLastInsertedIds.lastInsertedDummyDailyTodoId + 1;
const FIRST_DAILY_TODO_HISTORY_ID = DummyDataLastInsertedIds.lastInsertedDummyDailyTodoHistoryId + 1;
const FIRST_DAILY_TODO_CERTIFICATION_ID = DummyDataLastInsertedIds.lastInsertedDummyDailyTodoCertificationId + 1;
const FIRST_DAILY_TODO_CERTIFICATION_REVIEWER_ID = DummyDataLastInsertedIds.lastInsertedDummyDailyTodoCertificationReviewerId + 1;

// ===== 내부 규칙(결정적) =====
const blockCount = Math.ceil(MEMBER_COUNT / GROUP_CAPACITY);
const totalGroupCount = blockCount;

// 멤버 → 단일 소속 그룹 ID
function groupIdOfMember(memberId) {
    const blockIndex = Math.floor((memberId - FIRST_MEMBER_ID) / GROUP_CAPACITY);
    return FIRST_CHALLENGE_GROUP_ID + blockIndex;
}

// 그룹 → 멤버 목록
function membersOfGroup(groupId) {
    const offset = groupId - FIRST_CHALLENGE_GROUP_ID;
    const blockIndex = offset;
    const start = FIRST_MEMBER_ID + blockIndex * GROUP_CAPACITY;
    const end = Math.min(start + GROUP_CAPACITY - 1, FIRST_MEMBER_ID + MEMBER_COUNT - 1);
    const members = [];
    for (let m = start; m <= end; m++) members.push(m);
    return members;
}

// 같은 그룹 내 '다음' 멤버(원형) - reviewer 선정
function pickNextMember(groupMembers, writerId) {
    const idx = groupMembers.indexOf(writerId);
    if (idx === -1) {
        // 예외적으로 목록에 없으면 첫 번째 멤버(자기 자신이면 다음)
        const first = groupMembers[0];
        return first === writerId ? groupMembers[1] : first;
    }
    const nextIdx = (idx + 1) % groupMembers.length;
    return (groupMembers[nextIdx] === writerId)
        ? groupMembers[(nextIdx + 1) % groupMembers.length]
        : groupMembers[nextIdx];
}

// ===== 1) challenge_group =====
function createChallengeGroupData() {
    const status = "RUNNING";
    const startAt = CURRENT_ROW_INSERTED_AT; // 오늘 시작
    const endAt = CURRENT_ROW_INSERTED_AT;   // (기간 무관 테스트) 필요시 확장
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
    const createdAt = CURRENT_ROW_INSERTED_AT;
    const rowInsertedAt = CURRENT_ROW_INSERTED_AT;
    const rowUpdatedAt = null;

    const challenge_group_member_data = [];
    const last_selected_challenge_group_record_data = [];

    let cgMemberId = FIRST_CHALLENGE_GROUP_MEMBER_ID;
    let lastSelId = 1;

    for (let gIdx = 0; gIdx < totalGroupCount; gIdx++) {
        const groupId = FIRST_CHALLENGE_GROUP_ID + gIdx;
        const members = membersOfGroup(groupId);
        for (const memberId of members) {
            challenge_group_member_data.push([
                cgMemberId++,
                groupId,
                memberId,
                createdAt,
                rowInsertedAt,
                rowUpdatedAt,
            ]);
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

// ===== 3) daily_todo / history / certification / reviewer =====
let daily_todo_certification_data_cache = null;
let daily_todo_certification_reviewer_data_cache = null;

function createTodoAndCertificationAndReviewerData() {
    const daily_todo_data = [];
    const daily_todo_history_data = [];
    const daily_todo_certification_data = [];
    const daily_todo_certification_reviewer_data = [];

    const rowInsertedAt = CURRENT_ROW_INSERTED_AT;
    const rowUpdatedAt = null;
    const writtenAt = CURRENT_ROW_INSERTED_AT;

    let todoId = FIRST_DAILY_TODO_ID;
    let todoHistId = FIRST_DAILY_TODO_HISTORY_ID;
    let certId = FIRST_DAILY_TODO_CERTIFICATION_ID;
    let certReviewerId = FIRST_DAILY_TODO_CERTIFICATION_REVIEWER_ID;

    // 그룹별 멤버 목록 캐시
    const groupIdToMembers = new Map();
    for (let gIdx = 0; gIdx < totalGroupCount; gIdx++) {
        const gid = FIRST_CHALLENGE_GROUP_ID + gIdx;
        groupIdToMembers.set(gid, membersOfGroup(gid));
    }

    // 각 멤버가 TODOS_PER_MEMBER 만큼 투두 작성 → 전부 인증 생성(REVIEW_PENDING)
    for (let i = 0; i < MEMBER_COUNT; i++) {
        const memberId = FIRST_MEMBER_ID + i;
        const gid = groupIdOfMember(memberId);
        const sameGroupMembers = groupIdToMembers.get(gid);

        for (let n = 0; n < TODOS_PER_MEMBER; n++) {
            const currentTodoId = todoId++;
            const content = `td=${currentTodoId}`;

            // daily_todo: CERTIFY_COMPLETED (인증 생성 대상)
            daily_todo_data.push([
                currentTodoId,
                gid,
                memberId,
                content,
                'CERTIFY_COMPLETED',
                writtenAt,
                rowInsertedAt,
                rowUpdatedAt,
            ]);

            daily_todo_history_data.push([
                todoHistId++,
                currentTodoId,
                writtenAt,
                rowInsertedAt,
                rowUpdatedAt,
            ]);

            // certification: REVIEW_PENDING
            const currentCertId = certId++;
            daily_todo_certification_data.push([
                currentCertId,
                currentTodoId,
                `tc-${currentTodoId}`,
                `http://certification-media.site/m${memberId}/t${currentTodoId}`,
                'REVIEW_PENDING',   // 초기 상태
                null,               // review_feedback
                writtenAt,
                rowInsertedAt,
                rowUpdatedAt,
            ]);

            // reviewer: 같은 그룹의 '다음' 멤버(원형)
            const reviewerId = pickNextMember(sameGroupMembers, memberId);
            daily_todo_certification_reviewer_data.push([
                certReviewerId++,
                currentCertId,
                reviewerId,
                rowInsertedAt,
                rowUpdatedAt,
            ]);
        }
    }

    // 헬퍼에서 재사용할 수 있게 캐시
    daily_todo_certification_data_cache = daily_todo_certification_data;
    daily_todo_certification_reviewer_data_cache = daily_todo_certification_reviewer_data;

    return {
        daily_todo_data,
        daily_todo_history_data,
        daily_todo_certification_data,
        daily_todo_certification_reviewer_data,
    };
}

// ===== 메인: Set-up 데이터 =====
export function createSetUpData() {
    console.log("🧩 데일리 투두 인증 검사 V1 API set up 데이터 생성 시작.");

    const batch_size = 2000;
    const challenge_group_data = createChallengeGroupData();
    const {
        challenge_group_member_data,
        last_selected_challenge_group_record_data,
    } = createChallengeGroupMemberAndLastSelected();

    const {
        daily_todo_data,
        daily_todo_history_data,
        daily_todo_certification_data,
        daily_todo_certification_reviewer_data,
    } = createTodoAndCertificationAndReviewerData();

    console.log("✅ 데이터 생성 완료!\n");

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

// ===== k6 헬퍼 =====

// 모든 회원에 대해, 검사자에게 배정된 "대기 중" 인증 ID 배열(2차원)
// result[memberId-1] => number[]  (그 회원이 검사자인 REVIEW_PENDING 인증 ID 목록)
export function getPendingCertificationIdsPerReviewer() {
    // 캐시 없을 경우 한 번 생성(직접 호출상황 대비)
    if (!daily_todo_certification_reviewer_data_cache || !daily_todo_certification_data_cache) {
        createTodoAndCertificationAndReviewerData();
    }

    // REVIEW_PENDING 필터링을 위해 certification 테이블을 맵으로
    const pendingSet = new Set();
    for (const row of daily_todo_certification_data_cache) {
        // [certId, todoId, content, mediaUrl, reviewStatus, reviewFeedback, createdAt, rowInsertedAt, rowUpdatedAt]
        const certId = row[0];
        const reviewStatus = row[4];
        if (reviewStatus === 'REVIEW_PENDING') {
            pendingSet.add(certId);
        }
    }

    // reviewer 별 모음
    const result = Array.from({ length: MEMBER_COUNT }, () => []);
    for (const row of daily_todo_certification_reviewer_data_cache) {
        // [certReviewerId, certId, reviewerId, rowInsertedAt, rowUpdatedAt]
        const certId = row[1];
        const reviewerId = row[2];
        if (pendingSet.has(certId)) {
            result[reviewerId - FIRST_MEMBER_ID].push(certId);
        }
    }
    return result;
}

// 모든 회원에 대해, 검사자에게 배정된 "대기 중" 인증 ID 한 건씩(1차원)
// result[memberId-1] => number | null
export function getOnePendingCertificationIdPerReviewer() {
    const all = getPendingCertificationIdsPerReviewer();
    const one = new Array(MEMBER_COUNT).fill(null);
    for (let i = 0; i < MEMBER_COUNT; i++) {
        one[i] = all[i]?.[0] ?? null;
    }
    return one;
}
