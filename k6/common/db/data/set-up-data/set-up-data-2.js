/**
 * # 회원별로 가변적인 현재 활동 데이터 생성
 * - 멤버별 가입 그룹 수: 랜덤 (예: 1~5개)
 * - 그룹별 최대 인원(용량): 랜덤 (예: 2~20명, MEMBER_COUNT에 맞춰 자동 보정)
 * - 활동 편차/주말/결근/미인증/리뷰 미완료 등 현실 분포 유지
 * - daily_todo.status: CERTIFY_COMPLETED | CERTIFY_PENDING
 * - 인증 1건당 리뷰어: **항상 1명**, 인증자를 제외한 같은 그룹원 중 랜덤
 * - 리뷰 미완료: review_status='REVIEW_PENDING', review_feedback=null
 *
 * 주의: READ 전용 테스트 (WRITE X)
 */
import { getCurrentDateInKst, getDateNDaysAgoInKst } from "../../util/db-util.js";
import { getLastInsertedIds } from "../dummy-data/dummy-data-1.js";

// ========= 파라미터(유동 조절) =========
const MEMBER_COUNT = 100;

// 멤버별 가입 그룹 수 범위
const MIN_JOIN_GROUPS = 1;
const MAX_JOIN_GROUPS = 5;

// 그룹 최대 인원(용량) 범위 (MEMBER_COUNT 상한 고려)
const MIN_GROUP_SIZE = 2;
const MAX_GROUP_SIZE = 20;

// 투두 상한(하루 1인)
const ONE_DAY_TODO_COUNT_PER_GROUP_MEMBER = 10;

// 상태(오타 방지)
const STATUS_CERTIFY_COMPLETED = 'CERTIFY_COMPLETED';
const STATUS_CERTIFY_PENDING   = 'CERTIFY_PENDING';

// 현실 분포
const DURATION_PER_GROUP = 28;
const APPROVE_RATE = 0.85;
const WEEKEND_MULTIPLIER = 0.6;
const DAILY_ABSENCE_PROB = 0.10;
// 리뷰 완료 여부를 결정하는 추가 누락 확률(리뷰어는 배정되더라도 완료되지 않을 수 있음)
const REVIEW_MISS_EXTRA_PROB = 0.05;

const SEGMENTS = [
    { ratio: 0.15, activityMu: 0.65, certRate: 0.92, reviewRate: 0.85 },
    { ratio: 0.35, activityMu: 0.30, certRate: 0.75, reviewRate: 0.55 },
    { ratio: 0.50, activityMu: 0.08, certRate: 0.40, reviewRate: 0.25 },
];

// ========= ID/시간 =========
const FIRST_MEMBER_ID = 1;
const lastInsertedIds = getLastInsertedIds();
const FIRST_CHALLENGE_GROUP_ID = lastInsertedIds.lastInsertedDummyChallengeGroupId + 1;
const FIRST_CHALLENGE_GROUP_MEMBER_ID = lastInsertedIds.lastInsertedDummyChallengeGroupMemberId + 1;
const FIRST_DAILY_TODO_ID = lastInsertedIds.lastInsertedDummyDailyTodoId + 1;
const FIRST_DAILY_TODO_HISTORY_ID = lastInsertedIds.lastInsertedDummyDailyTodoHistoryId + 1;
const FIRST_DAILY_TODO_CERTIFICATION_ID = lastInsertedIds.lastInsertedDummyDailyTodoCertificationId + 1;
const FIRST_DAILY_TODO_CERTIFICATION_REVIEWER_ID = lastInsertedIds.lastInsertedDummyDailyTodoCertificationReviewerId + 1;

const CURRENT_ROW_INSERTED_AT = getCurrentDateInKst();

// ========= RNG / 유틸 =========
function createRng(seed = 77337731) {
    let state = seed >>> 0;
    return function rand() {
        state = (1664525 * state + 1013904223) >>> 0;
        return (state / 0xFFFFFFFF);
    };
}
const rand = createRng(20250810);
const clamp = (v, lo, hi) => Math.max(lo, Math.min(hi, v));
const pickIndexByRatio = (ratios) => {
    const r = rand();
    let acc = 0;
    for (let i = 0; i < ratios.length; i++) {
        acc += ratios[i];
        if (r < acc) return i;
    }
    return ratios.length - 1;
};
const personalActivity = (mu) => clamp(mu + (rand() - 0.5) * 0.2, 0, 1);
const shuffleInPlace = (arr) => {
    for (let i = arr.length - 1; i > 0; i--) {
        const j = Math.floor(rand() * (i + 1));
        [arr[i], arr[j]] = [arr[j], arr[i]];
    }
    return arr;
};
function isWeekend(daysAgo) {
    const dt = new Date(getDateNDaysAgoInKst((DURATION_PER_GROUP - 1) - daysAgo));
    const day = dt.getDay(); // 0=Sun, 6=Sat
    return day === 0 || day === 6;
}

// ========= 멤버/그룹 랜덤 구성 사전 작업 =========
function makeJoinCountsPerMember() {
    const res = new Array(MEMBER_COUNT);
    for (let i = 0; i < MEMBER_COUNT; i++) {
        const r = rand();
        let join = MIN_JOIN_GROUPS + Math.floor(r * (MAX_JOIN_GROUPS - MIN_JOIN_GROUPS + 1));
        if (r > 0.85) join = MAX_JOIN_GROUPS; // 상방 가중(원치 않으면 제거)
        res[i] = join;
    }
    return res; // index 0 → memberId 1
}
function sum(arr) { return arr.reduce((a, b) => a + b, 0); }

function makeGroupCapacities(totalNeeded) {
    const caps = [];
    let capSum = 0;
    const maxCap = Math.max(1, Math.min(MAX_GROUP_SIZE, MEMBER_COUNT));
    const minCap = Math.max(1, Math.min(MIN_GROUP_SIZE, maxCap));

    while (capSum < totalNeeded) {
        const cap = minCap + Math.floor(rand() * (maxCap - minCap + 1));
        caps.push(cap);
        capSum += cap;
    }
    return caps;
}

function assignMembersToGroups(joinCountsPerMember, groupCapacities) {
    const G = groupCapacities.length;
    const remaining = groupCapacities.slice();
    const groupsPerMemberIdx = Array.from({ length: MEMBER_COUNT }, () => []);
    const membersPerGroupIdx = Array.from({ length: G }, () => []);

    let groupOrder = shuffleInPlace([...Array(G).keys()]);

    for (let mIdx = 0; mIdx < MEMBER_COUNT; mIdx++) {
        let need = joinCountsPerMember[mIdx];
        let safety = 0;

        while (need > 0) {
            if (safety++ > G * 5) {
                // 남은 용량 부족 → 그룹 추가(안전장치)
                const extraCap = Math.max(1, Math.min(MAX_GROUP_SIZE, MEMBER_COUNT));
                remaining.push(extraCap - 1);
                groupCapacities.push(extraCap);
                membersPerGroupIdx.push([]);
                groupOrder.push(remaining.length - 1);
            }

            const candidates = groupOrder.filter(g => remaining[g] > 0 && !groupsPerMemberIdx[mIdx].includes(g));
            if (candidates.length === 0) {
                groupOrder = shuffleInPlace(groupOrder);
                continue;
            }
            const g = candidates[Math.floor(rand() * candidates.length)];
            groupsPerMemberIdx[mIdx].push(g);
            membersPerGroupIdx[g].push(mIdx);
            remaining[g] -= 1;
            need -= 1;
        }
    }

    // index → 실제 ID
    const groupsPerMemberIds = groupsPerMemberIdx.map(list =>
        list.map(g => FIRST_CHALLENGE_GROUP_ID + g)
    );
    const membersPerGroupIds = membersPerGroupIdx.map(list =>
        list.map(mIdx => FIRST_MEMBER_ID + mIdx)
    );
    return { groupsPerMemberIds, membersPerGroupIds, groupCapacities };
}

// 모든 그룹 최소 2명 보장(이동 우선, 불가 시 추가 가입)
function rebalanceGroupsToMinTwo(groupsPerMemberIds, membersPerGroupIds, groupCapacities) {
    const groupCount = membersPerGroupIds.length;
    const memberCount = groupsPerMemberIds.length;

    const memberGroupsSet = Array.from({ length: memberCount }, (_, i) => new Set(groupsPerMemberIds[i]));

    for (let gIdx = 0; gIdx < groupCount; gIdx++) {
        const realGroupId = FIRST_CHALLENGE_GROUP_ID + gIdx;
        const capacity = groupCapacities[gIdx];
        const members = membersPerGroupIds[gIdx];

        if (members.length >= 2) continue;

        const need = Math.min(2 - members.length, Math.max(0, capacity - members.length));
        let toFill = need;

        while (toFill > 0) {
            // 1) donor 그룹에서 이동(도너는 2명 이상 유지)
            let moved = false;
            for (let dIdx = 0; dIdx < groupCount && !moved; dIdx++) {
                if (dIdx === gIdx) continue;
                const donorMembers = membersPerGroupIds[dIdx];
                if (donorMembers.length <= 2) continue;

                for (let k = 0; k < donorMembers.length; k++) {
                    const mId = donorMembers[k];
                    const mIdx = mId - FIRST_MEMBER_ID;
                    if (memberGroupsSet[mIdx].has(realGroupId)) continue;
                    if (memberGroupsSet[mIdx].size <= 1) continue; // 최소 1개 그룹은 유지

                    // 이동
                    donorMembers.splice(k, 1);
                    members.push(mId);

                    const donorRealId = FIRST_CHALLENGE_GROUP_ID + dIdx;
                    groupsPerMemberIds[mIdx] = groupsPerMemberIds[mIdx].filter(gid => gid !== donorRealId);
                    groupsPerMemberIds[mIdx].push(realGroupId);

                    memberGroupsSet[mIdx].delete(donorRealId);
                    memberGroupsSet[mIdx].add(realGroupId);

                    moved = true;
                    toFill--;
                    break;
                }
            }
            if (moved) continue;

            // 2) 이동 불가 → 추가 가입(용량 여유 시)
            if (members.length < capacity) {
                let picked = false;
                for (let mIdx = 0; mIdx < memberCount; mIdx++) {
                    const mId = FIRST_MEMBER_ID + mIdx;
                    if (memberGroupsSet[mIdx].has(realGroupId)) continue;
                    members.push(mId);
                    groupsPerMemberIds[mIdx].push(realGroupId);
                    memberGroupsSet[mIdx].add(realGroupId);
                    picked = true;
                    toFill--;
                    break;
                }
                if (!picked) break;
            } else {
                break;
            }
        }
    }
    return { groupsPerMemberIds, membersPerGroupIds };
}

// ========= 동적 매핑 =========
let __groupsPerMemberCache = null; // index: memberId-1 → [groupId...]
let __membersPerGroupCache = null; // index: groupIdx(0-based) → [memberId...]

function setDynamicMappings(gpm, mpg) {
    __groupsPerMemberCache = gpm;
    __membersPerGroupCache = mpg;
}
function getGroupIdsForMember_dynamic(memberId) {
    return __groupsPerMemberCache?.[memberId - 1] ?? [];
}
function getMembersOfGroup_dynamic(realGroupId) {
    const idx = realGroupId - FIRST_CHALLENGE_GROUP_ID;
    return __membersPerGroupCache?.[idx] ?? [];
}

// ========= 본 생성 =========
export function createSetUpData() {
    console.log("👷 현실형(가변) 셋업 데이터 생성 시작!\n");
    const batch_size = 2000;

    // A) 멤버별 가입 수, 그룹 용량 만들기 & 배치
    const joinCountsPerMember = makeJoinCountsPerMember();
    const totalNeeded = sum(joinCountsPerMember);
    const groupCapacities = makeGroupCapacities(totalNeeded);
    const assigned = assignMembersToGroups(joinCountsPerMember, groupCapacities);

    // 모든 그룹 최소 2명 보장
    const fixed = rebalanceGroupsToMinTwo(assigned.groupsPerMemberIds, assigned.membersPerGroupIds, groupCapacities);
    const groupsPerMemberIds = fixed.groupsPerMemberIds;
    const membersPerGroupIds = fixed.membersPerGroupIds;

    setDynamicMappings(groupsPerMemberIds, membersPerGroupIds);

    // B) challenge_group 데이터
    const challenge_group_data = (() => {
        const arr = [];
        const status = 'RUNNING';
        const startAt = getDateNDaysAgoInKst(DURATION_PER_GROUP - 1);
        const endAt = getCurrentDateInKst();
        const createdAt = startAt;
        const rowInsertedAt = CURRENT_ROW_INSERTED_AT;
        const rowUpdatedAt = null;

        for (let i = 0; i < groupCapacities.length; i++) {
            const id = FIRST_CHALLENGE_GROUP_ID + i;
            arr.push([
                id,
                `g-${id}`,
                groupCapacities[i], // maximumMemberCount
                `jc-${id}`,
                status,
                startAt,
                endAt,
                createdAt,
                rowInsertedAt,
                rowUpdatedAt,
            ]);
        }
        return arr;
    })();

    // C) challenge_group_member / last_selected_challenge_group_record
    const challenge_group_member_data = [];
    const last_selected_challenge_group_record_data = [];
    {
        let challengeGroupMemberId = FIRST_CHALLENGE_GROUP_MEMBER_ID;
        let lastSelId = 1;
        const createdAt = getDateNDaysAgoInKst(DURATION_PER_GROUP - 1);
        const rowInsertedAt = CURRENT_ROW_INSERTED_AT;
        const rowUpdatedAt = null;

        for (let gIdx = 0; gIdx < membersPerGroupIds.length; gIdx++) {
            const realGroupId = FIRST_CHALLENGE_GROUP_ID + gIdx;
            for (const memberId of membersPerGroupIds[gIdx]) {
                challenge_group_member_data.push([
                    challengeGroupMemberId++,
                    realGroupId,
                    memberId,
                    createdAt,
                    rowInsertedAt,
                    rowUpdatedAt
                ]);
            }
        }

        for (let mIdx = 0; mIdx < MEMBER_COUNT; mIdx++) {
            const gids = groupsPerMemberIds[mIdx];
            if (!gids || gids.length === 0) continue;
            const lastGid = gids[gids.length - 1];
            last_selected_challenge_group_record_data.push([
                lastSelId++,
                lastGid,
                FIRST_MEMBER_ID + mIdx,
                rowInsertedAt,
                rowUpdatedAt
            ]);
        }
    }

    // D) DailyTodo / History / Certification / Reviewer
    const daily_todo_data = [];
    const daily_todo_history_data = [];
    const daily_todo_certification_data = [];
    const daily_todo_certification_reviewer_data = [];

    let dailyTodoId = FIRST_DAILY_TODO_ID;
    let dailyTodoHistoryId = FIRST_DAILY_TODO_HISTORY_ID;
    let todoCertificationId = FIRST_DAILY_TODO_CERTIFICATION_ID;
    let todoCertificationReviewerId = FIRST_DAILY_TODO_CERTIFICATION_REVIEWER_ID;

    const ratios = SEGMENTS.map(s => s.ratio);
    const segmentsOfMember = Array.from({ length: MEMBER_COUNT }, () => {
        const idx = pickIndexByRatio(ratios);
        const seg = SEGMENTS[idx];
        return { ...seg, personalMu: personalActivity(seg.activityMu) };
    });

    for (let memberId = FIRST_MEMBER_ID; memberId <= MEMBER_COUNT; memberId++) {
        const seg = segmentsOfMember[memberId - FIRST_MEMBER_ID];
        const memberGroups = getGroupIdsForMember_dynamic(memberId);
        if (memberGroups.length === 0) continue;

        for (let d = 0; d < DURATION_PER_GROUP; d++) {
            if (rand() < DAILY_ABSENCE_PROB) continue;

            const writtenAt = getDateNDaysAgoInKst((DURATION_PER_GROUP - 1) - d);
            const rowInsertedAt = CURRENT_ROW_INSERTED_AT;
            const rowUpdatedAt = null;

            const weekendMul = isWeekend(d) ? WEEKEND_MULTIPLIER : 1.0;
            const dailyNoise = 0.75 + rand() * 0.5;
            const dayActivity = clamp(seg.personalMu * weekendMul * dailyNoise, 0, 1);
            const targetTotalTodos = Math.floor(dayActivity * ONE_DAY_TODO_COUNT_PER_GROUP_MEMBER);
            if (targetTotalTodos <= 0) continue;

            // 그룹 분배
            const weights = memberGroups.map(() => 0.8 + rand() * 0.4);
            const sumW = weights.reduce((a, b) => a + b, 0);
            const alloc = weights.map(w => Math.floor((w / sumW) * targetTotalTodos));
            let remain = targetTotalTodos - alloc.reduce((a, b) => a + b, 0);
            while (remain > 0) {
                const idx = Math.floor(rand() * alloc.length);
                alloc[idx] += 1;
                remain--;
            }

            // 생성
            for (let gi = 0; gi < memberGroups.length; gi++) {
                const challengeGroupId = memberGroups[gi];
                const count = alloc[gi];
                for (let n = 0; n < count; n++) {
                    const currentTodoId = dailyTodoId++;
                    const content = `td=${currentTodoId}`;

                    const status = (rand() < seg.certRate) ? STATUS_CERTIFY_COMPLETED : STATUS_CERTIFY_PENDING;

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

                    if (status === STATUS_CERTIFY_COMPLETED) {
                        const currentCertId = todoCertificationId++;

                        // 리뷰어: 반드시 1명(인증자 제외, 같은 그룹원 중 랜덤)
                        const candidates = getMembersOfGroup_dynamic(challengeGroupId).filter(mid => mid !== memberId);
                        const reviewerMemberId = candidates[Math.floor(rand() * candidates.length)];

                        // 리뷰 완료 여부
                        const reviewed = rand() < (seg.reviewRate * (1 - REVIEW_MISS_EXTRA_PROB));

                        let reviewStatus, reviewFeedback;
                        if (reviewed) {
                            const approve = rand() < APPROVE_RATE;
                            reviewStatus = approve ? 'APPROVE' : 'REJECT';
                            reviewFeedback = approve ? '괜찮네요' : '기준 미충족';
                        } else {
                            reviewStatus = 'REVIEW_PENDING';
                            reviewFeedback = null;
                        }

                        // 인증
                        daily_todo_certification_data.push([
                            currentCertId,
                            currentTodoId,
                            `tc-${currentTodoId}`,
                            `http://certification-media.site/m${memberId}/t${currentTodoId}`,
                            reviewStatus,
                            reviewFeedback,
                            writtenAt,
                            rowInsertedAt,
                            rowUpdatedAt
                        ]);

                        // 리뷰어(항상 1:1)
                        daily_todo_certification_reviewer_data.push([
                            todoCertificationReviewerId++,
                            currentCertId,
                            reviewerMemberId,
                            rowInsertedAt,
                            rowUpdatedAt
                        ]);
                    }
                }
            }
        }
    }

    console.log(`✅ 현실형(가변) 데이터 생성 완료!\n`);
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

// ========= 헬퍼 (동적 매핑 기반) =========
export const getChallengeGroupIdsPerMember = () => {
    return __groupsPerMemberCache
        ? __groupsPerMemberCache.map(list => [...list])
        : Array.from({ length: MEMBER_COUNT }, () => []);
};

export const getChallengeGroupMembersPerMember = () => {
    if (!__groupsPerMemberCache || !__membersPerGroupCache) {
        return Array.from({ length: MEMBER_COUNT }, () => []);
    }
    const result = Array.from({ length: MEMBER_COUNT }, () => new Set());
    for (let gIdx = 0; gIdx < __membersPerGroupCache.length; gIdx++) {
        const members = __membersPerGroupCache[gIdx] || [];
        for (const m of members) {
            members.forEach(x => { if (x !== m) result[m - FIRST_MEMBER_ID].add(x); });
        }
    }
    return result.map(s => Array.from(s).sort((a, b) => a - b));
};