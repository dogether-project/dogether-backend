/**
 * # 현재 활동 데이터 (Current Activity)
 * - Read API 부하 테스트 전용
 * - 사용자 400명, 20명씩 20개 그룹 = 총 20 그룹
 * - 그룹 기간: 28일
 * - 오늘 기준으로 "마지막 날"에 도달했다고 가정
 * - inserted_at/created_at = 오늘 날짜 (정리 편의)
 * - 데이터는 재현성 보장 (랜덤 없음)
 */

import { getCurrentDateInKst, getDateNDaysAgoInKst } from "../../util/time-util.js";
import { getLastInsertedIds } from "../past-activity/variable-past-activity-data.js";

// ========= 상수 =========
const MEMBER_COUNT = 400;
const MEMBERS_PER_GROUP = 20;
const GROUP_COUNT = MEMBER_COUNT / MEMBERS_PER_GROUP; // 20
const GROUP_DURATION = 28;

// ========= 과거 데이터 마지막 ID 불러오기 =========
const DummyLast = getLastInsertedIds();

const FIRST_CHALLENGE_GROUP_ID = DummyLast.lastInsertedDummyChallengeGroupId + 1;
const FIRST_CHALLENGE_GROUP_MEMBER_ID = DummyLast.lastInsertedDummyChallengeGroupMemberId + 1;
const FIRST_LAST_SELECTED_ID = DummyLast.lastInsertedDummyLastSelectedChallengeGroupRecordId + 1;
const FIRST_DAILY_TODO_ID = DummyLast.lastInsertedDummyDailyTodoId + 1;
const FIRST_DAILY_TODO_HISTORY_ID = DummyLast.lastInsertedDummyDailyTodoHistoryId + 1;
const FIRST_DAILY_TODO_CERTIFICATION_ID = DummyLast.lastInsertedDummyDailyTodoCertificationId + 1;
const FIRST_DAILY_TODO_CERTIFICATION_REVIEWER_ID = DummyLast.lastInsertedDummyDailyTodoCertificationReviewerId + 1;

// ========= 유틸 =========
function membersOfGroupIndex(gIdx) {
    const start = gIdx * MEMBERS_PER_GROUP + 1;
    return Array.from({ length: MEMBERS_PER_GROUP }, (_, i) => start + i);
}

function balancedSegmentOf(memberId) {
    if (memberId <= 100) return "HIGH";
    if (memberId <= 300) return "MID";
    return "LOW";
}

function todosToday(memberId) {
    const seg = balancedSegmentOf(memberId);
    const today = new Date(getCurrentDateInKst());
    const day = today.getDay();
    const weekend = (day === 0 || day === 6);

    if (seg === "HIGH") return weekend ? 3 : 6;
    if (seg === "MID") return weekend ? 1 : 3;
    if (weekend) return 0;
    return (memberId % 2 === 0) ? 1 : 0;
}

function todoStatusOf(todoId, memberId) {
    return ((todoId + memberId) % 5 === 0) ? "CERTIFY_PENDING" : "CERTIFY_COMPLETED";
}

function pickReviewer(groupMembers, writerId) {
    const idx = groupMembers.indexOf(writerId);
    if (idx === -1) return groupMembers[0];
    return groupMembers[(idx + 1) % groupMembers.length];
}

// ========= 메인 데이터 생성 =========
export function createCurrentActivityData() {
    console.log("👷 [Current for read api] 읽기 API 테스트를 위한 현재 활동 데이터 생성 시작.");

    const batch_size = 2000;
    const challenge_group_data = [];
    const challenge_group_member_data = [];
    const last_selected_challenge_group_record_data = [];
    const daily_todo_data = [];
    const daily_todo_history_data = [];
    const daily_todo_certification_data = [];
    const daily_todo_certification_reviewer_data = [];

    let groupIdSeq = FIRST_CHALLENGE_GROUP_ID;
    let groupMemberIdSeq = FIRST_CHALLENGE_GROUP_MEMBER_ID;
    let lastSelectedIdSeq = FIRST_LAST_SELECTED_ID;
    let todoIdSeq = FIRST_DAILY_TODO_ID;
    let todoHistIdSeq = FIRST_DAILY_TODO_HISTORY_ID;
    let certIdSeq = FIRST_DAILY_TODO_CERTIFICATION_ID;
    let certReviewerIdSeq = FIRST_DAILY_TODO_CERTIFICATION_REVIEWER_ID;

    const startAt = getDateNDaysAgoInKst(GROUP_DURATION - 1); // 28일 전
    const endAt   = getCurrentDateInKst(); // 오늘
    const createdAt = startAt;
    const insertedAt = getCurrentDateInKst();

    for (let gIdx = 0; gIdx < GROUP_COUNT; gIdx++) {
        const gid = groupIdSeq++;
        challenge_group_data.push([
            gid, `g-${gid}`, MEMBERS_PER_GROUP, `jc-${gid}`, "RUNNING",
            startAt, endAt, createdAt, insertedAt, null
        ]);

        const members = membersOfGroupIndex(gIdx);
        for (const memberId of members) {
            challenge_group_member_data.push([
                groupMemberIdSeq++, gid, memberId, createdAt, insertedAt, null
            ]);

            // 마지막 선택 그룹 기록 (Current에서만 생성)
            last_selected_challenge_group_record_data.push([
                lastSelectedIdSeq++, gid, memberId, insertedAt, null
            ]);

            // 오늘 하루치 투두
            const todoCount = todosToday(memberId);
            for (let n = 0; n < todoCount; n++) {
                const todoId = todoIdSeq++;
                const status = todoStatusOf(todoId, memberId);

                daily_todo_data.push([todoId, gid, memberId, `td=${todoId}`, status, endAt, insertedAt, null]);
                daily_todo_history_data.push([todoHistIdSeq++, todoId, endAt, insertedAt, null]);

                if (status === "CERTIFY_COMPLETED") {
                    const certId = certIdSeq++;
                    const reviewerId = pickReviewer(members, memberId);

                    daily_todo_certification_data.push([
                        certId, todoId, `tc-${todoId}`, `http://certification-media.site/m${memberId}/t${todoId}`,
                        "REVIEW_PENDING", null, endAt, insertedAt, null
                    ]);
                    daily_todo_certification_reviewer_data.push([
                        certReviewerIdSeq++, certId, reviewerId, insertedAt, null
                    ]);
                }
            }
        }
    }

    console.log("✅ [Current] 데이터 생성 완료!");
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

// ========= Helper Functions for K6 =========

// 각 멤버가 속한 그룹 ID 배열 반환 (예: [[21],[21],...,[40]])
export function getChallengeGroupIdsPerMember() {
    const result = new Array(MEMBER_COUNT);
    for (let gIdx = 0; gIdx < GROUP_COUNT; gIdx++) {
        const gid = FIRST_CHALLENGE_GROUP_ID + gIdx;
        const members = membersOfGroupIndex(gIdx);
        for (const memberId of members) {
            result[memberId - 1] = [gid];
        }
    }
    return result;
}

// 각 멤버가 같은 그룹의 다른 멤버 ID를 하나 갖도록 (히스토리 조회 API용)
export function getChallengeGroupMembersPerMember() {
    const result = new Array(MEMBER_COUNT);
    for (let gIdx = 0; gIdx < GROUP_COUNT; gIdx++) {
        const members = membersOfGroupIndex(gIdx);
        for (let i = 0; i < members.length; i++) {
            const memberId = members[i];
            const otherId = members[(i + 1) % members.length];
            result[memberId - 1] = [otherId];
        }
    }
    return result;
}
