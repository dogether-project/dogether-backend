/**
 * # 현재 활동 + 쓰기 API 통합 데이터 (보장형/캐싱)
 *
 * 사용자 400명, 20명 단위 블록.
 *
 * 그룹 구성 (총 100개):
 *  A. ACTIVITY(20): 현실적인 오늘 활동(세그먼트 기반 수량), 인증/리뷰 일부 생성
 *  B. JOIN-EMPTY(20): 참여 API 테스트용 (빈 그룹)
 *  C. TODO-CREATE(20): 오늘 투두 0 → 모든 멤버가 오늘 10개 작성 가능
 *  D. CERTIFY-OWN(20): 각 멤버 '오늘' 반드시 1개씩 CERTIFY_PENDING 투두 생성  ✅
 *  E. REVIEW(20): 각 멤버 '오늘' 반드시 1개씩 CERTIFY_COMPLETED + 인증 생성, 검사자는 다음 멤버  ✅
 *
 * last_selected_challenge_group_record 는 **각 멤버당 1건만** 생성 (ACTIVITY 그룹에만 기록) → 총 400건
 * 데이터 재현성(랜덤X), 세그먼트(HIGH 100 / MID 200 / LOW 100)
 */

import { getCurrentDateInKst, getDateNDaysAgoInKst } from "../../util/time-util.js";
import { getLastInsertedIds } from "../past-activity/variable-past-activity-data.js";

// ========= 상수 =========
const MEMBER_COUNT = 400;
const MEMBERS_PER_GROUP = 20;
const BLOCKS = MEMBER_COUNT / MEMBERS_PER_GROUP; // 20
const GROUP_DURATION = 28;

const ACTIVITY_GROUP_COUNT     = BLOCKS; // 20
const JOIN_EMPTY_GROUP_COUNT   = BLOCKS; // 20
const TODO_CREATE_GROUP_COUNT  = BLOCKS; // 20
const CERTIFY_OWN_GROUP_COUNT  = BLOCKS; // 20
const REVIEW_GROUP_COUNT       = BLOCKS; // 20

// ========= 과거 데이터 마지막 ID =========
const DummyLast = getLastInsertedIds();

const FIRST_CHALLENGE_GROUP_ID = DummyLast.lastInsertedDummyChallengeGroupId + 1;
const FIRST_CHALLENGE_GROUP_MEMBER_ID = DummyLast.lastInsertedDummyChallengeGroupMemberId + 1;
const FIRST_LAST_SELECTED_ID = DummyLast.lastInsertedDummyLastSelectedChallengeGroupRecordId + 1;
const FIRST_DAILY_TODO_ID = DummyLast.lastInsertedDummyDailyTodoId + 1;
const FIRST_DAILY_TODO_HISTORY_ID = DummyLast.lastInsertedDummyDailyTodoHistoryId + 1;
const FIRST_DAILY_TODO_CERTIFICATION_ID = DummyLast.lastInsertedDummyDailyTodoCertificationId + 1;
const FIRST_DAILY_TODO_CERTIFICATION_REVIEWER_ID = DummyLast.lastInsertedDummyDailyTodoCertificationReviewerId + 1;

// ========= 유틸 =========
function membersOfBlock(blockIdx) {
    const start = blockIdx * MEMBERS_PER_GROUP + 1;
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

// 활동용(현실감)에서는 일부 PENDING이 나와도 괜찮지만,
// “보장형” 세트는 아래처럼 명시적으로 상태를 강제한다.
function todoStatusOf(todoId, memberId) {
    return ((todoId + memberId) % 5 === 0) ? "CERTIFY_PENDING" : "CERTIFY_COMPLETED";
}

function pickReviewer(groupMembers, writerId) {
    const idx = groupMembers.indexOf(writerId);
    if (idx === -1) return groupMembers[0];
    return groupMembers[(idx + 1) % groupMembers.length];
}

// ========= 캐시 (k6에서 헬퍼가 안전하게 재사용하도록) =========
let cachedCurrentData = null;
let cachePendingTodoIdByMember = null;         // memberId(1..N) -> today CERTIFY_PENDING todoId (보장형)
let cacheReviewCertIdsByReviewer = null;       // reviewerId(1..N) -> [certId,...] (보장형)

// ========= 메인 생성 =========
export function createCurrentActivityData() {
    console.log("👷 [Current for write api] 쓰기 API 테스트를 위한 현재 활동 데이터 생성 시작.");

    if (cachedCurrentData) {
        console.log("♻️ 캐시된 데이터 반환");
        return cachedCurrentData;
    }

    const batch_size = 2000;
    const challenge_group_data = [];
    const challenge_group_member_data = [];
    const last_selected_challenge_group_record_data = [];
    const daily_todo_data = [];
    const daily_todo_history_data = [];
    const daily_todo_certification_data = [];
    const daily_todo_certification_reviewer_data = [];

    cachePendingTodoIdByMember = Array(MEMBER_COUNT + 1).fill(null);
    cacheReviewCertIdsByReviewer = Array.from({ length: MEMBER_COUNT + 1 }, () => []);

    let groupIdSeq = FIRST_CHALLENGE_GROUP_ID;
    let cgMemberIdSeq = FIRST_CHALLENGE_GROUP_MEMBER_ID;
    let lastSelectedIdSeq = FIRST_LAST_SELECTED_ID;
    let todoIdSeq = FIRST_DAILY_TODO_ID;
    let todoHistIdSeq = FIRST_DAILY_TODO_HISTORY_ID;
    let certIdSeq = FIRST_DAILY_TODO_CERTIFICATION_ID;
    let certReviewerIdSeq = FIRST_DAILY_TODO_CERTIFICATION_REVIEWER_ID;

    const startAt = getDateNDaysAgoInKst(GROUP_DURATION - 1); // 28일 전
    const endAt   = getCurrentDateInKst();                    // 오늘
    const createdAt = startAt;
    const rowInsertedAt = getCurrentDateInKst();

    // --- 그룹 ID 구간 ---
    const ACTIVITY_START_GID = groupIdSeq;
    const ACTIVITY_END_GID_EXCL = ACTIVITY_START_GID + ACTIVITY_GROUP_COUNT;

    const JOIN_EMPTY_START_GID = ACTIVITY_END_GID_EXCL;
    const JOIN_EMPTY_END_GID_EXCL = JOIN_EMPTY_START_GID + JOIN_EMPTY_GROUP_COUNT;

    const TODO_CREATE_START_GID = JOIN_EMPTY_END_GID_EXCL;
    const TODO_CREATE_END_GID_EXCL = TODO_CREATE_START_GID + TODO_CREATE_GROUP_COUNT;

    const CERTIFY_OWN_START_GID = TODO_CREATE_END_GID_EXCL;
    const CERTIFY_OWN_END_GID_EXCL = CERTIFY_OWN_START_GID + CERTIFY_OWN_GROUP_COUNT;

    const REVIEW_START_GID = CERTIFY_OWN_END_GID_EXCL;
    const REVIEW_END_GID_EXCL = REVIEW_START_GID + REVIEW_GROUP_COUNT;

    // 1) ACTIVITY: 현실적인 오늘 활동 + 일부 인증/리뷰 생성
    for (let b = 0; b < ACTIVITY_GROUP_COUNT; b++) {
        const gid = groupIdSeq++;
        challenge_group_data.push([
            gid, `active-g-${gid}`, MEMBERS_PER_GROUP, `jc-${gid}`, "RUNNING",
            startAt, endAt, createdAt, rowInsertedAt, null
        ]);

        const members = membersOfBlock(b);
        for (const memberId of members) {
            challenge_group_member_data.push([
                cgMemberIdSeq++, gid, memberId, createdAt, rowInsertedAt, null
            ]);

            // last_selected는 ACTIVITY 그룹에만 1건 생성 (총 400건)
            last_selected_challenge_group_record_data.push([
                lastSelectedIdSeq++, gid, memberId, rowInsertedAt, null
            ]);

            // 현실형 오늘 투두
            const count = todosToday(memberId);
            for (let k = 0; k < count; k++) {
                const todoId = todoIdSeq++;
                const status = todoStatusOf(todoId, memberId);

                daily_todo_data.push([
                    todoId, gid, memberId, `td=${todoId}`, status, endAt, rowInsertedAt, null
                ]);
                daily_todo_history_data.push([
                    todoHistIdSeq++, todoId, endAt, rowInsertedAt, null
                ]);

                if (status === "CERTIFY_COMPLETED") {
                    const certId = certIdSeq++;
                    const reviewerId = pickReviewer(members, memberId);
                    daily_todo_certification_data.push([
                        certId, todoId, `tc-${todoId}`,
                        `http://certification-media.site/m${memberId}/t${todoId}`,
                        "REVIEW_PENDING", null, endAt, rowInsertedAt, null
                    ]);
                    daily_todo_certification_reviewer_data.push([
                        certReviewerIdSeq++, certId, reviewerId, rowInsertedAt, null
                    ]);

                    // 현실형에서 생성된 리뷰도 reviewer 캐시에 추가(보너스)
                    cacheReviewCertIdsByReviewer[reviewerId].push(certId);
                }
            }
        }
    }

    // 2) JOIN-EMPTY: 참여 API 테스트 전용 (멤버 없음)
    for (let i = 0; i < JOIN_EMPTY_GROUP_COUNT; i++) {
        const gid = groupIdSeq++;
        challenge_group_data.push([
            gid, `extra-g-${gid}`, MEMBERS_PER_GROUP, `extra-jc-${gid}`, "RUNNING",
            startAt, endAt, createdAt, rowInsertedAt, null
        ]);
    }

    // 3) TODO-CREATE: 오늘 투두 0 (각 멤버가 오늘 10개 작성 가능)
    for (let b = 0; b < TODO_CREATE_GROUP_COUNT; b++) {
        const gid = groupIdSeq++;
        challenge_group_data.push([
            gid, `todo-create-g-${gid}`, MEMBERS_PER_GROUP, `tcg-${gid}`, "RUNNING",
            startAt, endAt, createdAt, rowInsertedAt, null
        ]);

        const members = membersOfBlock(b);
        for (const memberId of members) {
            challenge_group_member_data.push([
                cgMemberIdSeq++, gid, memberId, createdAt, rowInsertedAt, null
            ]);
            // 오늘 투두 생성 없음
        }
    }

    // 4) CERTIFY-OWN(보장형): 각 멤버가 오늘 CERTIFY_PENDING 투두를 정확히 1개 갖도록
    for (let b = 0; b < CERTIFY_OWN_GROUP_COUNT; b++) {
        const gid = groupIdSeq++;
        challenge_group_data.push([
            gid, `certify-own-g-${gid}`, MEMBERS_PER_GROUP, `cown-jc-${gid}`, "RUNNING",
            startAt, endAt, createdAt, rowInsertedAt, null
        ]);

        const members = membersOfBlock(b);
        for (const memberId of members) {
            challenge_group_member_data.push([
                cgMemberIdSeq++, gid, memberId, createdAt, rowInsertedAt, null
            ]);

            // 보장형: 무조건 CERTIFY_PENDING 1개 생성 (인증 레코드 생성X)
            const todoId = todoIdSeq++;
            const status = "CERTIFY_PENDING";
            daily_todo_data.push([
                todoId, gid, memberId, `own-pending-td=${todoId}`, status, endAt, rowInsertedAt, null
            ]);
            daily_todo_history_data.push([
                todoHistIdSeq++, todoId, endAt, rowInsertedAt, null
            ]);

            cachePendingTodoIdByMember[memberId] = todoId; // ✅ 보장 캐시
        }
    }

    // 5) REVIEW(보장형): 각 멤버가 '검사자'로서 오늘 최소 1개 이상의 REVIEW_PENDING 인증을 갖도록
    for (let b = 0; b < REVIEW_GROUP_COUNT; b++) {
        const gid = groupIdSeq++;
        challenge_group_data.push([
            gid, `review-g-${gid}`, MEMBERS_PER_GROUP, `rvw-jc-${gid}`, "RUNNING",
            startAt, endAt, createdAt, rowInsertedAt, null
        ]);

        const members = membersOfBlock(b);
        for (const memberId of members) {
            challenge_group_member_data.push([
                cgMemberIdSeq++, gid, memberId, createdAt, rowInsertedAt, null
            ]);
        }
        // 작성자=각 멤버, 상태= CERTIFY_COMPLETED → 인증 생성, 검사자=다음 멤버
        for (const writerId of members) {
            const todoId = todoIdSeq++;
            // 강제로 완료 상태로 (인증 생성 대상)
            const status = "CERTIFY_COMPLETED";
            daily_todo_data.push([
                todoId, gid, writerId, `review-writer-td=${todoId}`, status, endAt, rowInsertedAt, null
            ]);
            daily_todo_history_data.push([
                todoHistIdSeq++, todoId, endAt, rowInsertedAt, null
            ]);

            const certId = certIdSeq++;
            const reviewerId = pickReviewer(members, writerId);

            daily_todo_certification_data.push([
                certId, todoId, `tc-${todoId}`,
                `http://certification-media.site/m${writerId}/t${todoId}`,
                "REVIEW_PENDING", null, endAt, rowInsertedAt, null
            ]);
            daily_todo_certification_reviewer_data.push([
                certReviewerIdSeq++, certId, reviewerId, rowInsertedAt, null
            ]);

            cacheReviewCertIdsByReviewer[reviewerId].push(certId); // ✅ 보장 캐시
        }
    }

    console.log("✅ 생성 완료!");
    console.log(`   - 그룹 수: ${REVIEW_END_GID_EXCL - ACTIVITY_START_GID}개 (ACT:${ACTIVITY_GROUP_COUNT}, JOIN-EMPTY:${JOIN_EMPTY_GROUP_COUNT}, TODO-CREATE:${TODO_CREATE_GROUP_COUNT}, CERTIFY-OWN:${CERTIFY_OWN_GROUP_COUNT}, REVIEW:${REVIEW_GROUP_COUNT})`);
    console.log(`   - last_selected: ${last_selected_challenge_group_record_data.length}건 (각 멤버 1건)`);

    cachedCurrentData = {
        batch_size,
        challenge_group_data,
        challenge_group_member_data,
        last_selected_challenge_group_record_data,
        daily_todo_data,
        daily_todo_history_data,
        daily_todo_certification_data,
        daily_todo_certification_reviewer_data,
    };
    return cachedCurrentData;
}

// ========= Helpers (k6에서 그대로 사용) =========

// 1) 챌린지 그룹 참여 테스트용 joinCode (회원 수만큼 순환 배정)
export function getJoinCodesPerMember() {
    const startGid = FIRST_CHALLENGE_GROUP_ID + ACTIVITY_GROUP_COUNT; // JOIN-EMPTY 시작
    const result = new Array(MEMBER_COUNT);
    for (let i = 0; i < MEMBER_COUNT; i++) {
        const block = i % JOIN_EMPTY_GROUP_COUNT; // 0..19
        const gid = startGid + block;
        result[i] = `extra-jc-${gid}`;
    }
    return result;
}

// 2) 오늘 투두 작성용 그룹 id (각 회원 1개, TODO-CREATE 그룹)  ← **숫자 배열** 반환
export function getTodoTargetGroupIdsPerMember() {
    const startGid = FIRST_CHALLENGE_GROUP_ID + ACTIVITY_GROUP_COUNT + JOIN_EMPTY_GROUP_COUNT; // TODO-CREATE 시작
    const result = new Array(MEMBER_COUNT);
    for (let i = 0; i < MEMBER_COUNT; i++) {
        const block = Math.floor(i / MEMBERS_PER_GROUP); // 0..19
        const gid = startGid + block;
        result[i] = gid; // 숫자 단일 값
    }
    return result;
}

// 3) (본인) 오늘 인증할 수 있는 DailyTodoId 1개씩 (모두 CERTIFY_PENDING, 멤버 수와 1:1)
export function getOneCertifiableTodoIdPerMember() {
    if (!cachedCurrentData) createCurrentActivityData();
    const result = new Array(MEMBER_COUNT);
    for (let memberId = 1; memberId <= MEMBER_COUNT; memberId++) {
        const todoId = cachePendingTodoIdByMember?.[memberId] ?? null;
        // 절대 null/undefined가 되지 않도록 보장형으로 만들어 둠
        result[memberId - 1] = todoId;
    }
    return result;
}

// 4) (검사자) 본인이 검사할 REVIEW_PENDING 인증 ID 목록  (length>=1 보장)
export function getPendingCertificationIdsPerReviewer() {
    if (!cachedCurrentData) createCurrentActivityData();
    const result = new Array(MEMBER_COUNT);
    for (let memberId = 1; memberId <= MEMBER_COUNT; memberId++) {
        result[memberId - 1] = cacheReviewCertIdsByReviewer?.[memberId] ?? [];
    }
    return result;
}

// 5) (검사자) 본인이 검사할 REVIEW_PENDING 인증 ID 한 건씩 (편의용)
export function getOnePendingCertificationIdPerReviewer() {
    const all = getPendingCertificationIdsPerReviewer();
    return all.map(list => list[0]);
}

// 6) (읽기 보조) 활동 그룹 id (회원별 소속 1개)
export function getChallengeGroupIdsPerMember() {
    const result = new Array(MEMBER_COUNT);
    for (let i = 0; i < MEMBER_COUNT; i++) {
        const block = Math.floor(i / MEMBERS_PER_GROUP);
        const gid = FIRST_CHALLENGE_GROUP_ID + block; // ACTIVITY 시작
        result[i] = [gid];
    }
    return result;
}

// 7) (읽기 보조) 같은 활동 그룹의 다른 멤버 1명
export function getChallengeGroupMembersPerMember() {
    const result = new Array(MEMBER_COUNT);
    for (let i = 0; i < MEMBER_COUNT; i++) {
        const block = Math.floor(i / MEMBERS_PER_GROUP);
        const members = membersOfBlock(block);
        const me = members[i % MEMBERS_PER_GROUP];
        const other = members[(i % MEMBERS_PER_GROUP + 1) % MEMBERS_PER_GROUP];
        result[me - 1] = [other];
    }
    return result;
}
