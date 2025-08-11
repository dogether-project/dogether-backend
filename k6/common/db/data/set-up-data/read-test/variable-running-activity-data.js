/**
 * # 회원별 가변적인 현재 활동 데이터 생성
 * - 멤버별 가입 그룹 수: memberId 규칙
 * - 그룹 용량: 고정 시퀀스 규칙
 * - 투두/결근/주말 약화/인증/리뷰 상태: 전부 규칙
 * - 리뷰어: 같은 그룹원 중 '다음' 멤버(원형), 항상 1명
 *
 * 주의: READ 전용 테스트 (WRITE X)
 */
import { getCurrentDateInKst, getDateNDaysAgoInKst } from "../../../util/time-util.js";
import { getLastInsertedIds } from "../../dummy-data/only-member-info-data.js";

// ========= 파라미터 =========
const MEMBER_COUNT = 100;

const MIN_JOIN_GROUPS = 1;
const MAX_JOIN_GROUPS = 5;

const MIN_GROUP_SIZE = 2;
const MAX_GROUP_SIZE = 20;

const DURATION_PER_GROUP = 28;

// 상태(오타 방지)
const STATUS_CERTIFY_COMPLETED = 'CERTIFY_COMPLETED';
const STATUS_CERTIFY_PENDING   = 'CERTIFY_PENDING';

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

// ========= 결정적 규칙 유틸 =========
function isWeekend(d) {
    const dt = new Date(getDateNDaysAgoInKst((DURATION_PER_GROUP - 1) - d));
    const day = dt.getDay(); // 0 Sun, 6 Sat
    return day === 0 || day === 6;
}

// 세그먼트 규칙: 0..19 모듈러
function segmentOf(memberId) {
    const mod = (memberId - FIRST_MEMBER_ID) % 20;
    if (mod < 3) return 'HIGH';   // 15%
    if (mod < 10) return 'MID';   // 35%
    return 'LOW';                 // 50%
}

// 가입 그룹 수 규칙(결정적): 5,4,3,2,1 분포
function joinCountOf(memberId) {
    const mod = (memberId - FIRST_MEMBER_ID) % 20;
    if (mod < 4)  return 5; // 20%
    if (mod < 10) return 4; // 30%
    if (mod < 16) return 3; // 30%
    if (mod < 19) return 2; // 15%
    return 1;               // 5%
}

function todosToday(memberId, d) {
    // 결근 규칙
    if ((memberId + d) % 9 === 0) return 0;
    const weekend = isWeekend(d);
    const seg = segmentOf(memberId);
    if (seg === 'HIGH') return weekend ? 3 : 6;
    if (seg === 'MID')  return weekend ? 1 : 3;
    return weekend ? 0 : 1; // LOW
}

function evenSplit(total, parts) {
    if (parts <= 0) return [];
    const base = Math.floor(total / parts);
    const rem  = total % parts;
    const arr = new Array(parts).fill(base);
    for (let i = 0; i < rem; i++) arr[i] += 1; // 앞쪽부터 +1
    return arr;
}

function todoStatusOf(currentTodoId, memberId) {
    // ~20% 미인증
    return ((currentTodoId + memberId) % 5 === 0) ? STATUS_CERTIFY_PENDING : STATUS_CERTIFY_COMPLETED;
}

function reviewStateOf(currentCertId, challengeGroupId) {
    // ~14% 리뷰 미완료
    if ((currentCertId + challengeGroupId) % 7 === 0) {
        return { status: 'REVIEW_PENDING', feedback: null };
    }
    // 나머지에서 ~20% REJECT
    if (currentCertId % 5 === 0) {
        return { status: 'REJECT', feedback: '기준 미충족' };
    }
    return { status: 'APPROVE', feedback: '괜찮네요' };
}

// 같은 그룹원 중 '다음' 멤버(원형)
function pickReviewerDeterministic(groupMemberList, writerId) {
    const idx = groupMemberList.indexOf(writerId);
    if (idx === -1) {
        const first = groupMemberList[0];
        return first === writerId ? groupMemberList[1] : first;
    }
    const nextIdx = (idx + 1) % groupMemberList.length;
    return (groupMemberList[nextIdx] === writerId)
        ? groupMemberList[(nextIdx + 1) % groupMemberList.length]
        : groupMemberList[nextIdx];
}

// ========= 레이아웃 빌더(결정적) =========
function sum(arr) { return arr.reduce((a, b) => a + b, 0); }

function makeJoinCountsPerMember() {
    const res = new Array(MEMBER_COUNT);
    for (let i = 0; i < MEMBER_COUNT; i++) {
        const memberId = FIRST_MEMBER_ID + i;
        let join = joinCountOf(memberId);
        res[i] = Math.max(MIN_JOIN_GROUPS, Math.min(MAX_JOIN_GROUPS, join));
    }
    return res;
}

// 고정 시퀀스 용량
function makeGroupCapacities(totalNeeded) {
    const baseSeq = [12, 16, 10, 14, 18, 8];
    const caps = [];
    let capSum = 0;
    const maxCap = Math.max(1, Math.min(MAX_GROUP_SIZE, MEMBER_COUNT));
    const minCap = Math.max(1, Math.min(MIN_GROUP_SIZE, maxCap));
    let i = 0;
    while (capSum < totalNeeded) {
        let cap = baseSeq[i % baseSeq.length];
        cap = Math.max(minCap, Math.min(maxCap, cap));
        caps.push(cap);
        capSum += cap;
        i++;
    }
    return caps;
}

// 라운드로빈(결정적), 중복 방지, 용량 준수
function assignMembersToGroups(joinCountsPerMember, groupCapacities) {
    const G = groupCapacities.length;
    const remaining = groupCapacities.slice();
    const groupsPerMemberIdx = Array.from({ length: MEMBER_COUNT }, () => []);
    const membersPerGroupIdx = Array.from({ length: G }, () => []);

    let gPtr = 0;
    for (let mIdx = 0; mIdx < MEMBER_COUNT; mIdx++) {
        let need = joinCountsPerMember[mIdx];
        while (need > 0) {
            let tried = 0;
            while (tried < G && (remaining[gPtr] <= 0 || groupsPerMemberIdx[mIdx].includes(gPtr))) {
                gPtr = (gPtr + 1) % G;
                tried++;
            }
            if (tried >= G) throw new Error("배치 실패: 그룹 용량 부족/중복 제한");
            groupsPerMemberIdx[mIdx].push(gPtr);
            membersPerGroupIdx[gPtr].push(mIdx);
            remaining[gPtr]--;
            need--;
            gPtr = (gPtr + 1) % G;
        }
    }

    const groupsPerMemberIds = groupsPerMemberIdx.map(list =>
        list.map(g => FIRST_CHALLENGE_GROUP_ID + g)
    );
    const membersPerGroupIds = membersPerGroupIdx.map(list =>
        list.map(mIdx => FIRST_MEMBER_ID + mIdx)
    );
    return { groupsPerMemberIds, membersPerGroupIds, groupCapacities };
}

// 최소 2명 보장(결정적 이동)
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
            // 가장 큰 그룹에서 이동
            let donorIdx = -1;
            let donorSize = -1;
            for (let k = 0; k < groupCount; k++) {
                if (k === gIdx) continue;
                const sz = membersPerGroupIds[k].length;
                if (sz > donorSize && sz > 2) { donorSize = sz; donorIdx = k; }
            }
            if (donorIdx === -1) break;

            let moved = false;
            for (let i = 0; i < membersPerGroupIds[donorIdx].length; i++) {
                const mId = membersPerGroupIds[donorIdx][i];
                const mIdx = mId - FIRST_MEMBER_ID;
                const donorRealId = FIRST_CHALLENGE_GROUP_ID + donorIdx;
                if (memberGroupsSet[mIdx].size <= 1) continue;
                if (memberGroupsSet[mIdx].has(realGroupId)) continue;

                membersPerGroupIds[donorIdx].splice(i, 1);
                members.push(mId);
                groupsPerMemberIds[mIdx] = groupsPerMemberIds[mIdx].filter(gid => gid !== donorRealId);
                groupsPerMemberIds[mIdx].push(realGroupId);
                memberGroupsSet[mIdx].delete(donorRealId);
                memberGroupsSet[mIdx].add(realGroupId);
                moved = true;
                toFill--;
                break;
            }
            if (!moved) break;
        }
    }
    return { groupsPerMemberIds, membersPerGroupIds };
}

/**
 * 결정적 레이아웃을 생성해 반환
 * 외부(헬퍼/셋업)에서 동일 규칙으로 언제든 재생성 가능
 */
function buildLayout() {
    const joinCountsPerMember = makeJoinCountsPerMember();
    const totalNeeded = sum(joinCountsPerMember);
    const groupCapacities = makeGroupCapacities(totalNeeded);
    const assigned = assignMembersToGroups(joinCountsPerMember, groupCapacities);
    const fixed = rebalanceGroupsToMinTwo(assigned.groupsPerMemberIds, assigned.membersPerGroupIds, groupCapacities);
    return {
        groupCapacities,
        groupsPerMemberIds: fixed.groupsPerMemberIds,
        membersPerGroupIds: fixed.membersPerGroupIds,
    };
}

// ========= 본 데이터 생성 =========
export function createSetUpData() {
    console.log('✏️ [Set up data] - 회원별 가변적인 현재 활동 데이터 생성 시작!\n');
    const batch_size = 2000;

    // 레이아웃(결정적)
    const { groupCapacities, groupsPerMemberIds, membersPerGroupIds } = buildLayout();

    // B) challenge_group
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
                groupCapacities[i],
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

    // 그룹별 멤버 목록 조회용
    const groupIdToMembers = new Map();
    for (let gIdx = 0; gIdx < membersPerGroupIds.length; gIdx++) {
        const realGroupId = FIRST_CHALLENGE_GROUP_ID + gIdx;
        groupIdToMembers.set(realGroupId, membersPerGroupIds[gIdx]);
    }

    for (let memberId = FIRST_MEMBER_ID; memberId <= MEMBER_COUNT; memberId++) {
        const memberGroups = groupsPerMemberIds[memberId - FIRST_MEMBER_ID] || [];
        if (memberGroups.length === 0) continue;

        for (let d = 0; d < DURATION_PER_GROUP; d++) {
            const totalTodos = todosToday(memberId, d);
            if (totalTodos <= 0) continue;

            const writtenAt = getDateNDaysAgoInKst((DURATION_PER_GROUP - 1) - d);
            const rowInsertedAt = CURRENT_ROW_INSERTED_AT;
            const rowUpdatedAt = null;

            const alloc = evenSplit(totalTodos, memberGroups.length);

            for (let gi = 0; gi < memberGroups.length; gi++) {
                const challengeGroupId = memberGroups[gi];
                const count = alloc[gi];
                const groupMembers = groupIdToMembers.get(challengeGroupId) || [];

                for (let n = 0; n < count; n++) {
                    const currentTodoId = dailyTodoId++;
                    const content = `td=${currentTodoId}`;
                    const status = todoStatusOf(currentTodoId, memberId);

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

                        const reviewerMemberId = pickReviewerDeterministic(groupMembers, memberId);
                        const { status: reviewStatus, feedback: reviewFeedback } =
                            reviewStateOf(currentCertId, challengeGroupId);

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

// ========= 헬퍼(독립 호출 가능: 항상 같은 결과) =========
export const getChallengeGroupIdsPerMember = () => {
    // 레이아웃을 규칙으로 매번 재생성 → 언제 호출해도 동일
    const { groupsPerMemberIds } = buildLayout();
    return groupsPerMemberIds.map(list => [...list]);
};

export const getChallengeGroupMembersPerMember = () => {
    const { membersPerGroupIds } = buildLayout();
    // result[memberId-1] → 같은 그룹원들(본인 제외) 정렬 배열
    const result = Array.from({ length: MEMBER_COUNT }, () => new Set());
    for (let gIdx = 0; gIdx < membersPerGroupIds.length; gIdx++) {
        const members = membersPerGroupIds[gIdx] || [];
        for (const m of members) {
            members.forEach(x => { if (x !== m) result[m - FIRST_MEMBER_ID].add(x); });
        }
    }
    return result.map(s => Array.from(s).sort((a, b) => a - b));
};
