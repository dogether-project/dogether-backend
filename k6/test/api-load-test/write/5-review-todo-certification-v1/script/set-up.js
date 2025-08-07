import fs from 'fs';
import path from 'path';
import {
    createLocalDbConnection, createSshTunnelDbConnection, getConsecutiveNumbersByGroup,
    getCurrentDateInKst,
    getDateNDaysAgoInKst,
    getEndDateFromStartAgoAndDuration
} from "../../../../../common/db/util/db-util.js";
import {
    getLastInsertedDailyTodoStatsId,
    getLastInsertedNotificationTokenId,
    insertDailyTodoStats,
    insertMember,
    insertNotificationToken
} from "../../../../../common/db/query/member-query.js";
import {
    getLastInsertedChallengeGroupId,
    getLastInsertedChallengeGroupMemberId,
    getLastInsertedLastSelectedChallengeGroupRecordId,
    insertChallengeGroup, insertChallengeGroupMember, insertLastSelectedChallengeGroupRecord
} from "../../../../../common/db/query/challenge-group-query.js";
import {
    getLastInsertedDailyTodoId,
    insertDailyTodo,
    insertDailyTodoHistory
} from "../../../../../common/db/query/daily-todo-query.js";
import {
    getLastInsertedDailyTodoCertificationId,
    insertDailyTodoCertification, insertDailyTodoCertificationReviewer
} from "../../../../../common/db/query/daily-todo-certification-query.js";

const TEMP_OUTPUT_PATH = path.join('./script/temp.json');
const connection = await createLocalDbConnection(); // Local DB 커넥션
// const connection = await createSshTunnelDbConnection(); // AWS DB 커넥션

const batchSize = 100;

const TEST_MEMBER_COUNT = 100;
const TEST_CHALLENGE_GROUP_COUNT = 50;
const TODOS_PER_TEST_MEMBER_IN_GROUP = 10;
const TODO_CERTIFICATIONS_PER_TEST_MEMBER_IN_GROUP = 3;

const firstTestChallengeGroupId = await getLastInsertedChallengeGroupId(connection) + 1;
const firstTestChallengeGroupMemberId = await getLastInsertedChallengeGroupMemberId(connection) + 1;
const firstTestLastSelectedChallengeGroupRecordId = await getLastInsertedLastSelectedChallengeGroupRecordId(connection) + 1;

const firstTestMemberId = 1;
const firstTestNotificationTokenId = await getLastInsertedNotificationTokenId(connection) + 1;
const firstTestDailyTodoStatsId = await getLastInsertedDailyTodoStatsId(connection) + 1;

const firstTestDailyTodoId = await getLastInsertedDailyTodoId(connection) + 1;
const firstTestDailyTodoCertificationId = await getLastInsertedDailyTodoCertificationId(connection) + 1;

async function setUp() {
    try {
        await connection.beginTransaction();

        await insertTestMemberData(connection, batchSize);
        const challengeGroupIds = await insertTestChallengeGroupData(connection, batchSize);
        await insertTestChallengeGroupMemberAndRecordData(connection, batchSize, challengeGroupIds);
        const certifiedDailyTodoIds = await insertTestDailyTodoData(connection, batchSize, challengeGroupIds);
        const dailyTodoCertificationIds = await insertTestDailyTodoCertificationData(connection, batchSize, certifiedDailyTodoIds);

        createTempFile(dailyTodoCertificationIds);

        await connection.commit();
        console.log('\n🥳 테스트 데이터 삽입 완료!\n');
    } catch (error) {
        await connection.rollback();
        console.error(`❌ 에러 발생! 롤백 수행됨.`);
        console.error(error);
    } finally {
        await connection.end();
    }
}

const insertTestMemberData = async (connection, batchSize) => {
    const testMemberData = [];
    const testNotificationTokenData = [];
    const testDailyTodoStatsData = [];

    const profileImageUrl = `http://profile-image.site`;
    const createdAt = getCurrentDateInKst();
    const rowInsertedAt = getCurrentDateInKst();
    const rowUpdatedAt = null;
    for (let i = 0; i < TEST_MEMBER_COUNT; i++) {
        const id = i + 1;
        const providerId = `pid-${id}`;
        const name = `m-${id}`;
        testMemberData.push([
            id, providerId, name, profileImageUrl, createdAt, rowInsertedAt, rowUpdatedAt
        ]);

        testNotificationTokenData.push([
            firstTestNotificationTokenId + i, id, `t-${firstTestNotificationTokenId + i}`, rowInsertedAt, rowUpdatedAt
        ]);

        const certificatedCount = TODO_CERTIFICATIONS_PER_TEST_MEMBER_IN_GROUP;
        const approvedCount = 0;
        const rejectedCount = 0;
        testDailyTodoStatsData.push([
            firstTestDailyTodoStatsId + i, id, certificatedCount, approvedCount, rejectedCount, rowInsertedAt, rowUpdatedAt
        ]);
    }

    await insertMember(connection, testMemberData, batchSize);
    await insertNotificationToken(connection, testNotificationTokenData, batchSize);
    await insertDailyTodoStats(connection, testDailyTodoStatsData, batchSize);
}

const insertTestChallengeGroupData = async (connection, batchSize) => {
    const testChallengeGroupData = [];
    const challengeGroupIds = [];

    const maximumMemberCount = 20;
    const status = 'RUNNING';
    const startAt = getDateNDaysAgoInKst(3);
    const endAt = getEndDateFromStartAgoAndDuration(3, 28);
    const createdAt = getDateNDaysAgoInKst(3);
    const rowInsertedAt = getCurrentDateInKst();
    const rowUpdatedAt = null;
    for (let i = 0; i < TEST_CHALLENGE_GROUP_COUNT; i++) {
        const id = firstTestChallengeGroupId + i;
        const name = `g-${id}`;
        const joinCode = `jc-${id}`;

        testChallengeGroupData.push([
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

        challengeGroupIds.push(id);
    }

    await insertChallengeGroup(connection, testChallengeGroupData, batchSize);

    return challengeGroupIds;
}

const insertTestChallengeGroupMemberAndRecordData = async (connection, batchSize, challengeGroupIds) => {
    const testChallengeGroupMemberData = [];
    const testLastSelectedChallengeGroupRecordData = [];

    const createdAt = getDateNDaysAgoInKst(3);
    const rowInsertedAt = getCurrentDateInKst();
    const rowUpdatedAt = null;
    for (let i = 0; i < TEST_MEMBER_COUNT; i++) {
        const memberId = i + 1;
        const challengeGroupId = (i < 50) ? challengeGroupIds[i] : challengeGroupIds[i - 50];   // 1 & 51, 2 & 52 ... 50 & 100
        testChallengeGroupMemberData.push([
            firstTestChallengeGroupMemberId + i,
            challengeGroupId,
            memberId,
            createdAt,
            rowInsertedAt,
            rowUpdatedAt
        ]);

        testLastSelectedChallengeGroupRecordData.push([
            firstTestLastSelectedChallengeGroupRecordId + i,
            challengeGroupId,
            memberId,
            rowInsertedAt,
            rowUpdatedAt
        ]);
    }

    await insertChallengeGroupMember(connection, testChallengeGroupMemberData, batchSize);
    await insertLastSelectedChallengeGroupRecord(connection, testLastSelectedChallengeGroupRecordData, batchSize);
}

const insertTestDailyTodoData = async (connection, batchSize, challengeGroupIds) => {
    const daily_todo_data = [];
    const daily_todo_history_data = [];
    const certifiedDailyTodoIds = [];

    const writtenAt = getCurrentDateInKst();
    const rowInsertedAt = getCurrentDateInKst();
    const rowUpdatedAt = null;
    for (let i = 0; i < TEST_MEMBER_COUNT; i++) {
        const writerId = i + 1;
        const challengeGroupId = (i < 50) ? challengeGroupIds[i] : challengeGroupIds[i - 50];   // 1 & 51, 2 & 52 ... 50 & 100

        const todoIds = getConsecutiveNumbersByGroup(writerId, TODOS_PER_TEST_MEMBER_IN_GROUP);
        for (let j = 0; j < TODOS_PER_TEST_MEMBER_IN_GROUP; j++) {
            const id = firstTestDailyTodoId + todoIds[j];
            const content = `td-${id}`;
            const status = (j < TODO_CERTIFICATIONS_PER_TEST_MEMBER_IN_GROUP) ? 'CERTIFY_COMPLETED' : 'CERTIFY_PENDING';

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

            if (j < TODO_CERTIFICATIONS_PER_TEST_MEMBER_IN_GROUP) {
                certifiedDailyTodoIds.push(id);
            }
        }
    }

    await insertDailyTodo(connection, daily_todo_data, batchSize);
    await insertDailyTodoHistory(connection, daily_todo_history_data, batchSize);

    return certifiedDailyTodoIds;
}

const insertTestDailyTodoCertificationData = async (connection, batchSize, certifiedDailyTodoIds) => {
    const daily_todo_certification_data = [];
    const daily_todo_certification_reviewer_data = [];
    const reviewPendingCertificationIds = Array(TEST_MEMBER_COUNT).fill(0);

    const mediaUrl = 'http://certification-media.site';
    const createdAt = getDateNDaysAgoInKst(3);
    const rowInsertedAt = getCurrentDateInKst();
    const rowUpdatedAt = null;
    for (let i = 0; i < TEST_MEMBER_COUNT; i++) {
        const writerId = i + 1;
        const todoCertificationIds = getConsecutiveNumbersByGroup(writerId, TODO_CERTIFICATIONS_PER_TEST_MEMBER_IN_GROUP);

        for (let j = 0; j < TODO_CERTIFICATIONS_PER_TEST_MEMBER_IN_GROUP; j++) {
            const id = firstTestDailyTodoCertificationId + todoCertificationIds[j];
            const dailyTodoId = certifiedDailyTodoIds[i * TODO_CERTIFICATIONS_PER_TEST_MEMBER_IN_GROUP + j];
            const content = `c-${id}`;
            const reviewerId = (writerId <= 50) ? writerId + 50 : writerId - 50;
            const reviewStatus = 'REVIEW_PENDING';
            const reviewFeedback = null;

            daily_todo_certification_data.push([
                id, dailyTodoId, content, mediaUrl, reviewStatus, reviewFeedback, createdAt, rowInsertedAt, rowUpdatedAt
            ]);

            daily_todo_certification_reviewer_data.push([
                id, id, reviewerId, rowInsertedAt, rowUpdatedAt
            ]);

            if (j === 0) {
                reviewPendingCertificationIds[reviewerId - 1] = id;
            }
        }
    }

    await insertDailyTodoCertification(connection, daily_todo_certification_data, batchSize);
    await insertDailyTodoCertificationReviewer(connection, daily_todo_certification_reviewer_data, batchSize);

    return reviewPendingCertificationIds;
}

const createTempFile = (dailyTodoCertificationIds) => {
    const tempData = {
        firstTestMemberId: firstTestMemberId,
        testMemberDataSize: TEST_MEMBER_COUNT,
        dailyTodoCertificationIds
    };

    try {
        fs.writeFileSync(TEMP_OUTPUT_PATH, JSON.stringify(tempData, null, 2), 'utf-8');
        console.log(`✅ temp.json 저장 완료.`);
    } catch (err) {
        console.error('파일 저장 실패 :', err.message);
    }
}

setUp();
