/**
 * [ 데이터 셋 특정 ]
 * 테이블당 최소 10만 or 100만 건의 데이터를 삽입하는 게 목적
 * 우선 많은 데이터를 삽입하는 것에 초점을 맞춰 현실성은 떨어짐.
 *
 * [ 테이블당 데이터 개수 (10만개 삽입) ]
 * member : 10만개
 * notification_token : 10만개
 * daily_todo_stats : 10만개
 *
 * challenge_group : 10만개
 * challenge_group_member : 20만개
 * last_selected_challenge_group_record : 10만개
 *
 * daily_todo : 60만개
 * daily_todo_history : 60만개
 * daily_todo_history_read : 0개
 *
 * daily_todo_certification : 40만개
 * daily_todo_certification_reviewer : 40만개
 *
 * [ 테이블당 데이터 개수 (100만개 삽입) ]
 * member : 100만개
 * notification_token : 100만개
 * daily_todo_stats : 100만개
 *
 * challenge_group : 100만개
 * challenge_group_member : 200만개
 * last_selected_challenge_group_record : 100만개
 *
 * daily_todo : 600만개
 * daily_todo_history : 600만개
 * daily_todo_history_read : 0개
 *
 * daily_todo_certification : 400만개
 * daily_todo_certification_reviewer : 400만개
 */

import {
    getConsecutiveNumbersByGroup,
    getCurrentDateInKst,
    getDateNDaysAgoInKst,
    getEndDateFromStartAgoAndDuration
} from "../../util/db-util.js";

// 데이터 10만건 삽입 옵션
const MEMBER_COUNT = 100000;
const CHALLENGE_GROUP_COUNT = 100000;

// 데이터 100만건 삽입 옵션
// const MEMBER_COUNT = 1000000;
// const CHALLENGE_GROUP_COUNT = 1000000;

// 공통 설정
const FIRST_MEMBER_ID = 111;
const JOIN_GROUPS_PER_MEMBER = 2;
const TODOS_PER_MEMBER_IN_GROUP = 3;
const TODO_CERTIFICATIONS_PER_MEMBER_IN_GROUP = 2;
const TODO_CERTIFICATIONS_REVIEW_PER_MEMBER_IN_GROUP = 1;

export function createDummyData(connection) {
    const batch_size = 1000;
    const member_data = createMemberData();
    const notification_token_data = createNotificationTokenData();
    const daily_todo_stats_data = createDailyTodoStatsData();
    const challenge_group_data = createChallengeGroupData();
    const { challenge_group_member_data, last_selected_challenge_group_record_data } = createChallengeGroupMemberAndLastSelectedChallengeGroupRecordData();
    const { daily_todo_data, daily_todo_history_data } = createDailyTodoAndDailyTodoHistoryData();
    const { daily_todo_certification_data, daily_todo_certification_reviewer_data } = createDailyTodoCertificationAndReviewerData();

    console.log(`✅ 더미 데이터 생성 완료!\n`);
    return {
        batch_size,
        member_data,
        notification_token_data,
        daily_todo_stats_data,
        challenge_group_data,
        challenge_group_member_data,
        last_selected_challenge_group_record_data,
        daily_todo_data,
        daily_todo_history_data,
        daily_todo_certification_data,
        daily_todo_certification_reviewer_data,
    };
}

function createMemberData() {
    console.log("🗂️ member 테이블 더미 데이터 생성중...");

    const member_data = [];

    const profileImageUrl = `http://profile-image.site`;
    const createdAt = getCurrentDateInKst();
    const rowInsertedAt = getCurrentDateInKst();
    const rowUpdatedAt = null;
    for (let i = 0; i < MEMBER_COUNT; i++) {
        const id = FIRST_MEMBER_ID + i;
        const providerId = `pid-${id}`;
        const name = `m-${id}`;

        member_data.push([
            id, providerId, name, profileImageUrl, createdAt, rowInsertedAt, rowUpdatedAt
        ]);
    }

    return member_data;
}

function createNotificationTokenData() {
    console.log("🗂️ notification_token 테이블 더미 데이터 생성중...");

    const notification_token_data = [];

    const rowInsertedAt = getCurrentDateInKst();
    const rowUpdatedAt = null;
    for (let i = 0; i < MEMBER_COUNT; i++) {
        const id = i + 1;
        const memberId = FIRST_MEMBER_ID + i;
        const tokenValue = `t-${id}`;

        notification_token_data.push([
            id, memberId, tokenValue, rowInsertedAt, rowUpdatedAt
        ]);
    }

    return notification_token_data;
}

function createDailyTodoStatsData() {
    console.log("🗂️ daily_todo_stats 테이블 더미 데이터 생성중...");

    const daily_todo_stats_data = [];

    const certificatedCount = TODO_CERTIFICATIONS_PER_MEMBER_IN_GROUP * JOIN_GROUPS_PER_MEMBER;
    const approvedCount = TODO_CERTIFICATIONS_REVIEW_PER_MEMBER_IN_GROUP * JOIN_GROUPS_PER_MEMBER;
    const rejectedCount = 0;
    const rowInsertedAt = getCurrentDateInKst();
    const rowUpdatedAt = null;
    for (let i = 0; i < MEMBER_COUNT; i++) {
        const id = i + 1;
        const memberId = FIRST_MEMBER_ID + i;

        daily_todo_stats_data.push([
            id, memberId, certificatedCount, approvedCount, rejectedCount, rowInsertedAt, rowUpdatedAt
        ]);
    }

    return daily_todo_stats_data;
}

function createChallengeGroupData() {
    console.log("🗂️ challenge_group 테이블 더미 데이터 생성중...");

    const challenge_group_data = [];

    const maximumMemberCount = 20;
    const status = 'RUNNING';
    const startAt = getDateNDaysAgoInKst(3);
    const endAt = getEndDateFromStartAgoAndDuration(3, 28);
    const createdAt = getDateNDaysAgoInKst(3);
    const rowInsertedAt = getCurrentDateInKst();
    const rowUpdatedAt = null;
    for (let i = 0; i < CHALLENGE_GROUP_COUNT; i++) {
        const id = i + 1;
        const name = `g-${id}`;
        const joinCode = `jc-${id}`;

        challenge_group_data.push([
            id,
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

function createChallengeGroupMemberAndLastSelectedChallengeGroupRecordData() {
    console.log("🗂️ challenge_group_member & last_selected_challenge_group_record 테이블 더미 데이터 생성중...");

    const challenge_group_member_data = [];

    const createdAt = getDateNDaysAgoInKst(3);
    const rowInsertedAt = getCurrentDateInKst();
    const rowUpdatedAt = null;
    for (let i = 0; i < MEMBER_COUNT; i++) {
        const id = i + 1;
        const challengeGroupId = i + 1;
        const memberId = FIRST_MEMBER_ID + i;

        challenge_group_member_data.push([
            id,
            challengeGroupId,
            memberId,
            createdAt,
            rowInsertedAt,
            rowUpdatedAt,
        ]);
    }

    const last_selected_challenge_group_record_data = [];
    for (let i = 0; i < MEMBER_COUNT; i++) {
        const id = MEMBER_COUNT + i + 1;
        const challengeGroupId = MEMBER_COUNT - i;
        const memberId = FIRST_MEMBER_ID + i;

        challenge_group_member_data.push([
            id,
            challengeGroupId,
            memberId,
            createdAt,
            rowInsertedAt,
            rowUpdatedAt,
        ]);

        last_selected_challenge_group_record_data.push([
            id,
            challengeGroupId,
            memberId,
            rowInsertedAt,
            rowUpdatedAt,
        ]);
    }

    return { challenge_group_member_data, last_selected_challenge_group_record_data };
}

function createDailyTodoAndDailyTodoHistoryData() {
    console.log("🗂️ daily_todo & daily_todo_history 테이블 더미 데이터 생성중...");

    const daily_todo_data = [];
    const daily_todo_history_data = [];

    const writtenAt = getCurrentDateInKst();
    const rowInsertedAt = getCurrentDateInKst();
    const rowUpdatedAt = null;
    for (let i = 0; i < MEMBER_COUNT; i++) {
        const writerId = FIRST_MEMBER_ID + i;
        const challengeGroupId = i + 1;

        const todoIds = getConsecutiveNumbersByGroup(i + 1, TODOS_PER_MEMBER_IN_GROUP);
        for (let j = 0; j < TODOS_PER_MEMBER_IN_GROUP; j++) {
            const id = todoIds[j];
            const content = `td-${id}`;
            const status = (j === 0) ? 'CERTIFY_PENDING' : 'CERTIFY_COMPLETED';

            daily_todo_data.push([
                id, challengeGroupId, writerId, content, status, writtenAt, rowInsertedAt, rowUpdatedAt
            ]);

            daily_todo_history_data.push([
                id,
                id,
                writtenAt,
                rowInsertedAt,
                rowUpdatedAt
            ]);
        }
    }

    for (let i = 0; i < MEMBER_COUNT; i++) {
        const writerId = FIRST_MEMBER_ID + i;
        const challengeGroupId = MEMBER_COUNT - i;

        const todoIds = getConsecutiveNumbersByGroup(i + 1, TODOS_PER_MEMBER_IN_GROUP);
        for (let j = 0; j < TODOS_PER_MEMBER_IN_GROUP; j++) {
            const id = todoIds[j] + MEMBER_COUNT * TODOS_PER_MEMBER_IN_GROUP;
            const content = `td-${id}`;
            const status = (j === 0) ? 'CERTIFY_PENDING' : 'CERTIFY_COMPLETED';

            daily_todo_data.push([
                id, challengeGroupId, writerId, content, status, writtenAt, rowInsertedAt, rowUpdatedAt
            ]);

            daily_todo_history_data.push([
                id,
                id,
                writtenAt,
                rowInsertedAt,
                rowUpdatedAt
            ]);
        }
    }

    return { daily_todo_data, daily_todo_history_data };
}

function createDailyTodoCertificationAndReviewerData() {
    console.log("🗂️ daily_todo_certification & daily_todo_certification_reviewer 테이블 더미 데이터 생성중...");

    const daily_todo_certification_data = [];
    const daily_todo_certification_reviewer_data = [];

    const mediaUrl = 'http://certification-media.site';
    const createdAt = getDateNDaysAgoInKst(3);
    const rowInsertedAt = getCurrentDateInKst();
    const rowUpdatedAt = null;
    for (let i = 0; i < MEMBER_COUNT; i++) {
        const todoIds = getConsecutiveNumbersByGroup(i + 1, TODOS_PER_MEMBER_IN_GROUP);
        const todoCertificationIds = getConsecutiveNumbersByGroup(i + 1, TODO_CERTIFICATIONS_PER_MEMBER_IN_GROUP);
        setCertificationAndReviewerData(
            daily_todo_certification_data,
            daily_todo_certification_reviewer_data,
            todoCertificationIds,
            todoIds,
            mediaUrl,
            createdAt,
            rowInsertedAt,
            rowUpdatedAt,
            i
        );
    }

    for (let i = 0; i < MEMBER_COUNT; i++) {
        const todoIds = getConsecutiveNumbersByGroup(MEMBER_COUNT + i + 1, TODOS_PER_MEMBER_IN_GROUP);
        const todoCertificationIds = getConsecutiveNumbersByGroup(MEMBER_COUNT + i + 1, TODO_CERTIFICATIONS_PER_MEMBER_IN_GROUP);
        setCertificationAndReviewerData(
            daily_todo_certification_data,
            daily_todo_certification_reviewer_data,
            todoCertificationIds,
            todoIds,
            mediaUrl,
            createdAt,
            rowInsertedAt,
            rowUpdatedAt,
            i
        );
    }

    return { daily_todo_certification_data, daily_todo_certification_reviewer_data };
}

function setCertificationAndReviewerData(
    daily_todo_certification_data,
    daily_todo_certification_reviewer_data,
    todoCertificationIds,
    todoIds,
    mediaUrl,
    createdAt,
    rowInsertedAt,
    rowUpdatedAt,
    i
) {
    for (let j = 0; j < TODO_CERTIFICATIONS_PER_MEMBER_IN_GROUP; j++) {
        const id = todoCertificationIds[j];
        const dailyTodoId = todoIds[j];
        const content = `c-${id}`;
        const reviewStatus = (j === 0) ? 'REVIEW_PENDING' : 'APPROVE';
        const reviewFeedback = (j === 0) ? null : `와...ㄷㄷㄷ ${id}번 회원님 미쳤따리`;

        daily_todo_certification_data.push([
            id, dailyTodoId, content, mediaUrl, reviewStatus, reviewFeedback, createdAt, rowInsertedAt, rowUpdatedAt
        ]);

        daily_todo_certification_reviewer_data.push([
            id, id, (FIRST_MEMBER_ID - 1) + (MEMBER_COUNT - i), rowInsertedAt, rowUpdatedAt
        ]);
    }
}
