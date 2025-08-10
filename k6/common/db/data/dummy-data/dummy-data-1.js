/**
 * # 회원 정보만 생성
 */
import { getDateNDaysAgoInKst } from "../../util/time-util.js";

// ===== 공통 옵션 =====
const MEMBER_COUNT = 100;  // 전체 회원수 (⭐️ 핵심)
const FIRST_MEMBER_ID = 1;

const DEFAULT_CREATED_AT = getDateNDaysAgoInKst(30);

// 삽입 데이터 없음
export const getLastInsertedIds = () => {
    const lastInsertedDummyChallengeGroupId = 0;
    const lastInsertedDummyChallengeGroupMemberId = 0;
    const lastInsertedDummyDailyTodoId = 0;
    const lastInsertedDummyDailyTodoHistoryId = 0;
    const lastInsertedDummyDailyTodoCertificationId = 0;
    const lastInsertedDummyDailyTodoCertificationReviewerId = 0;

    return {
        lastInsertedDummyChallengeGroupId,
        lastInsertedDummyChallengeGroupMemberId,
        lastInsertedDummyDailyTodoId,
        lastInsertedDummyDailyTodoHistoryId,
        lastInsertedDummyDailyTodoCertificationId,
        lastInsertedDummyDailyTodoCertificationReviewerId
    };
};

export function createDummyData() {
    console.log('👷 더미 데이터 MK.3 생성 시작!\n');

    const batch_size = 100;
    const member_data = createMemberData();
    const notification_token_data = createNotificationTokenData();
    const daily_todo_stats_data = createDailyTodoStatsData();

    console.log(`✅ 더미 데이터 생성 완료!\n`);
    return {
        batch_size,
        member_data,
        notification_token_data,
        daily_todo_stats_data
    };
}

const createMemberData = () => {
    console.log("🗂️ member 테이블 더미 데이터 생성중...");

    const member_data = [];
    const profileImageUrl = `http://profile-image.site`;

    const createdAt = DEFAULT_CREATED_AT;
    const rowInsertedAt = DEFAULT_CREATED_AT;
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
};

const createNotificationTokenData = () => {
    console.log("🗂️ notification_token 테이블 더미 데이터 생성중...");

    const notification_token_data = [];
    const rowInsertedAt = DEFAULT_CREATED_AT;
    const rowUpdatedAt = null;

    for (let i = 0; i < MEMBER_COUNT; i++) {
        const id = FIRST_MEMBER_ID + i;
        const memberId = FIRST_MEMBER_ID + i;
        const tokenValue = `t-${id}`;

        notification_token_data.push([ id, memberId, tokenValue, rowInsertedAt, rowUpdatedAt ]);
    }
    return notification_token_data;
};

const createDailyTodoStatsData = () => {
    console.log("🗂️ daily_todo_stats 테이블 더미 데이터 생성중...");

    const daily_todo_stats_data = [];
    const certificatedCount = 0;
    const approvedCount = 0;
    const rejectedCount = 0;
    const rowInsertedAt = DEFAULT_CREATED_AT;
    const rowUpdatedAt = null;

    for (let i = 0; i < MEMBER_COUNT; i++) {
        const id = FIRST_MEMBER_ID + i;
        const memberId = FIRST_MEMBER_ID + i;
        daily_todo_stats_data.push([
            id, memberId, certificatedCount, approvedCount, rejectedCount, rowInsertedAt, rowUpdatedAt
        ]);
    }
    return daily_todo_stats_data;
};
