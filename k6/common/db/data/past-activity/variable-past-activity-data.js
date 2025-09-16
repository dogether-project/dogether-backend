/**
 * # 현실적인 과거 활동 더미 데이터
 * - 사용자 400명, 세그먼트: HIGH(100)/MID(200)/LOW(100)
 * - 그룹 정원 20명 → 항상 20개 그룹이 병행
 * - 그룹 기간: 3/7/14/28일만 사용, 순환 배치(결정적)
 * - 총 PAST_TOTAL_DAYS(예: 3년=1095일)을 정확히 채우고,
 *   마지막 past 그룹은 "현재 28일 그룹 시작 전날"에 종료
 * - 투두/인증/리뷰는 전부 결정적 규칙 (랜덤 X)
 * - inserted_at/created_at: 과거 데이터는 실제 과거 날짜
 * - getLastInsertedIds(): 실행과 무관하게 계산식으로 최종 ID 반환(결정적)
 */

import { getDateNDaysAgoInKst } from "../../util/time-util.js";

// ========= 상수/파라미터 =========
const MEMBER_COUNT = 400;
const MEMBERS_PER_GROUP = 20;
const GROUPS_IN_PARALLEL = MEMBER_COUNT / MEMBERS_PER_GROUP; // 20
export const PAST_TOTAL_DAYS = 365 * 3; // 3년 = 1095일
const CURRENT_GROUP_DURATION = 28;      // 현재 그룹은 28일로 고정

// allowed durations (결정적 순환 배치)
const ALLOWED_DURS = [3, 7, 14, 28];

// 상태 상수
const STATUS_CERTIFY_COMPLETED = "CERTIFY_COMPLETED";
const STATUS_CERTIFY_PENDING   = "CERTIFY_PENDING";

// ========= 로그 출력을 위한 기간 포맷터 =========
function formatDaysToPeriod(days) {
    const years = Math.floor(days / 365);
    const months = Math.floor((days % 365) / 30);
    const remainingDays = days % 30;

    const parts = [];
    if (years > 0) parts.push(`${years}년`);
    if (months > 0) parts.push(`${months}개월`);
    if (remainingDays > 0) parts.push(`${remainingDays}일`);

    return parts.join(" ");
}

// ========= 세그먼트 규칙 =========
function balancedSegmentOf(memberId) {
    if (memberId <= 100) return "HIGH";
    if (memberId <= 300) return "MID";
    return "LOW";
}

// ========= 날짜/요일 =========
function isWeekendByAgo(dAgo) {
    const dt = new Date(getDateNDaysAgoInKst(dAgo));
    const day = dt.getDay();
    return day === 0 || day === 6;
}

// ========= 투두 개수 규칙 =========
function todosToday(memberId, dAgo) {
    const seg = balancedSegmentOf(memberId);
    const weekend = isWeekendByAgo(dAgo);

    if (seg === "HIGH") return weekend ? 3 : 6;
    if (seg === "MID") return weekend ? 1 : 3;
    if (weekend) return 0;
    return ((memberId + dAgo) % 2 === 0) ? 1 : 0;
}

// ========= 투두/인증/리뷰 상태 =========
function todoStatusOf(todoId, memberId) {
    return ((todoId + memberId) % 5 === 0)
        ? STATUS_CERTIFY_PENDING
        : STATUS_CERTIFY_COMPLETED;
}

function reviewStateOf(certId, groupId) {
    if ((certId + groupId) % 7 === 0) return { status: "REVIEW_PENDING", feedback: null };
    if (certId % 5 === 0) return { status: "REJECT", feedback: "기준 미충족" };
    return { status: "APPROVE", feedback: "괜찮네요" };
}

function pickReviewer(groupMembers, writerId) {
    const idx = groupMembers.indexOf(writerId);
    if (idx === -1) return groupMembers[0];
    return groupMembers[(idx + 1) % groupMembers.length];
}

// ========= 타임라인 =========
const LAST_PAST_END_AGO = CURRENT_GROUP_DURATION;
const PAST_START_AGO    = LAST_PAST_END_AGO + (PAST_TOTAL_DAYS - 1);

function buildPastBlocks() {
    const blocks = [];
    let remaining = PAST_TOTAL_DAYS;
    let cursorEndAgo = LAST_PAST_END_AGO;
    let durIdx = 0;

    while (remaining > 0) {
        let dur = ALLOWED_DURS[durIdx % ALLOWED_DURS.length];
        durIdx++;
        if (dur > remaining) {
            for (let k = ALLOWED_DURS.length - 1; k >= 0; k--) {
                if (ALLOWED_DURS[k] <= remaining) {
                    dur = ALLOWED_DURS[k];
                    break;
                }
            }
        }
        blocks.push({
            endAgo: cursorEndAgo,
            startAgo: cursorEndAgo + (dur - 1),
            duration: dur
        });
        cursorEndAgo += dur;
        remaining -= dur;
    }
    return blocks;
}

// ========= 멤버 배치 =========
function membersOfGroupIndex(gIdx) {
    const start = gIdx * MEMBERS_PER_GROUP + 1;
    return Array.from({ length: MEMBERS_PER_GROUP }, (_, i) => start + i);
}

// ========= 데이터 생성 =========
export function createPastActivityData() {
    console.log(`👷 [Dummy] 과거 활동 데이터 생성 시작 (기간=${formatDaysToPeriod(PAST_TOTAL_DAYS)}).\n`);

    const batch_size = 2000;

    const member_data = [];
    const notification_token_data = [];
    const daily_todo_stats_data = [];
    const challenge_group_data = [];
    const challenge_group_member_data = [];
    const daily_todo_data = [];
    const daily_todo_history_data = [];
    const daily_todo_certification_data = [];
    const daily_todo_certification_reviewer_data = [];

    let challengeGroupIdSeq = 1;
    let challengeGroupMemberIdSeq = 1;
    let dailyTodoIdSeq = 1;
    let dailyTodoHistoryIdSeq = 1;
    let dailyTodoCertIdSeq = 1;
    let dailyTodoCertReviewerIdSeq = 1;

    const oldestDate = getDateNDaysAgoInKst(PAST_START_AGO);

    // 멤버/토큰/통계
    for (let memberId = 1; memberId <= MEMBER_COUNT; memberId++) {
        member_data.push([memberId, `pid-${memberId}`, `m-${memberId}`, `http://profile-image.site/${memberId}`, oldestDate, oldestDate, null]);
        notification_token_data.push([memberId, memberId, `t-${memberId}`, oldestDate, null]);
        daily_todo_stats_data.push([memberId, memberId, 0, 0, 0, oldestDate, null]);
    }

    const blocks = buildPastBlocks();
    const groupIdxMembers = Array.from({ length: GROUPS_IN_PARALLEL }, (_, gIdx) => membersOfGroupIndex(gIdx));

    for (const { startAgo, endAgo, duration } of blocks) {
        const startAt = getDateNDaysAgoInKst(startAgo);
        const endAt   = getDateNDaysAgoInKst(endAgo);
        const createdAt = startAt;

        for (let gIdx = 0; gIdx < GROUPS_IN_PARALLEL; gIdx++) {
            const gid = challengeGroupIdSeq++;
            challenge_group_data.push([gid, `g-${gid}`, MEMBERS_PER_GROUP, `jc-${gid}`, "FINISHED", startAt, endAt, createdAt, createdAt, null]);

            const members = groupIdxMembers[gIdx];
            for (const memberId of members) {
                challenge_group_member_data.push([challengeGroupMemberIdSeq++, gid, memberId, createdAt, createdAt, null]);
            }

            for (let d = 0; d < duration; d++) {
                const dAgo = startAgo - d;
                const writtenAt = getDateNDaysAgoInKst(dAgo);

                for (const memberId of members) {
                    const todoCount = todosToday(memberId, dAgo);
                    if (todoCount <= 0) continue;

                    for (let n = 0; n < todoCount; n++) {
                        const todoId = dailyTodoIdSeq++;
                        const status = todoStatusOf(todoId, memberId);

                        daily_todo_data.push([todoId, gid, memberId, `td=${todoId}`, status, writtenAt, writtenAt, null]);
                        daily_todo_history_data.push([dailyTodoHistoryIdSeq++, todoId, writtenAt, writtenAt, null]);

                        if (status === STATUS_CERTIFY_COMPLETED) {
                            const certId = dailyTodoCertIdSeq++;
                            const reviewerId = pickReviewer(members, memberId);
                            const { status: reviewStatus, feedback } = reviewStateOf(certId, gid);

                            daily_todo_certification_data.push([certId, todoId, `tc-${todoId}`, `http://certification-media.site/m${memberId}/t${todoId}`, reviewStatus, feedback, writtenAt, writtenAt, null]);
                            daily_todo_certification_reviewer_data.push([dailyTodoCertReviewerIdSeq++, certId, reviewerId, writtenAt, null]);
                        }
                    }
                }
            }
        }
    }

    console.log("✅ [Dummy] 데이터 생성 완료!\n");

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
        last_selected_challenge_group_record_data: [], // Past에서는 비워둠
    };
}

// ========= "계산식"으로 마지막 ID 구하기 =========
function computeLastIds() {
    const blocks = buildPastBlocks();

    let todoCount = 0;
    let historyCount = 0;
    let certCount = 0;
    let certReviewerCount = 0;

    let runningTodoId = 1;
    let runningCertId = 1;

    for (const { startAgo, duration } of blocks) {
        for (let d = 0; d < duration; d++) {
            const dAgo = startAgo - d;
            for (let memberId = 1; memberId <= MEMBER_COUNT; memberId++) {
                const count = todosToday(memberId, dAgo);
                if (count <= 0) continue;

                for (let n = 0; n < count; n++) {
                    const todoId = runningTodoId++;
                    todoCount++;
                    historyCount++;

                    const status = todoStatusOf(todoId, memberId);
                    if (status === STATUS_CERTIFY_COMPLETED) {
                        const certId = runningCertId++;
                        certCount++;
                        certReviewerCount++;
                    }
                }
            }
        }
    }

    return {
        lastInsertedDummyMemberId: MEMBER_COUNT,
        lastInsertedDummyNotificationTokenId: MEMBER_COUNT,
        lastInsertedDummyDailyTodoStatsId: MEMBER_COUNT,

        lastInsertedDummyChallengeGroupId: blocks.length * GROUPS_IN_PARALLEL,
        lastInsertedDummyChallengeGroupMemberId: blocks.length * GROUPS_IN_PARALLEL * MEMBERS_PER_GROUP,
        lastInsertedDummyLastSelectedChallengeGroupRecordId: 0, // Past는 기록 안함

        lastInsertedDummyDailyTodoId: todoCount,
        lastInsertedDummyDailyTodoHistoryId: historyCount,
        lastInsertedDummyDailyTodoCertificationId: certCount,
        lastInsertedDummyDailyTodoCertificationReviewerId: certReviewerCount,
    };
}

export function getLastInsertedIds() {
    return computeLastIds();
}
