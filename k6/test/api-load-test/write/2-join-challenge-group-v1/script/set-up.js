import fs from 'fs';
import path from 'path';
import {
    createLocalDbConnection, createSshTunnelDbConnection,
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

const TEMP_OUTPUT_PATH = path.join('./script/temp.json');
const connection = await createLocalDbConnection(); // Local DB 커넥션
// const connection = await createSshTunnelDbConnection(); // AWS DB 커넥션

const batchSize = 100;

const TEST_MEMBER_COUNT = 110;
const TEST_CHALLENGE_GROUP_COUNT = 10;

const firstTestChallengeGroupId = await getLastInsertedChallengeGroupId(connection) + 1;
const firstTestChallengeGroupMemberId = await getLastInsertedChallengeGroupMemberId(connection) + 1;
const firstTestLastSelectedChallengeGroupRecordId = await getLastInsertedLastSelectedChallengeGroupRecordId(connection) + 1;

const firstTestMemberId = 1;
const firstTestNotificationTokenId = await getLastInsertedNotificationTokenId(connection) + 1;
const firstTestDailyTodoStatsId = await getLastInsertedDailyTodoStatsId(connection) + 1;

const firstChallengeGroupJoinMemerId = 101;

async function setUp() {
    try {
        await connection.beginTransaction();

        await insertTestMemberData(connection, batchSize);
        const joinCodes = await insertTestChallengeGroupData(connection, batchSize);

        createTempFile(joinCodes);

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

        const certificatedCount = 0;
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
    const testChallengeGroupMemberData = [];
    const testLastSelectedChallengeGroupRecordData = [];
    const joinCodes = [];

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

        testChallengeGroupMemberData.push([
            firstTestChallengeGroupMemberId + i,
            id,
            firstChallengeGroupJoinMemerId + i,
            createdAt,
            rowInsertedAt,
            rowUpdatedAt
        ]);

        testLastSelectedChallengeGroupRecordData.push([
            firstTestLastSelectedChallengeGroupRecordId + i,
            id,
            firstChallengeGroupJoinMemerId + i,
            rowInsertedAt,
            rowUpdatedAt
        ]);

        joinCodes.push(joinCode);
    }

    await insertChallengeGroup(connection, testChallengeGroupData, batchSize);
    await insertChallengeGroupMember(connection, testChallengeGroupMemberData, batchSize);
    await insertLastSelectedChallengeGroupRecord(connection, testLastSelectedChallengeGroupRecordData, batchSize);

    return joinCodes;
}

const createTempFile = (joinCodes) => {
    const tempData = {
        firstTestMemberId: firstTestMemberId,
        testMemberDataSize: TEST_MEMBER_COUNT,
        joinCodes
    };

    try {
        fs.writeFileSync(TEMP_OUTPUT_PATH, JSON.stringify(tempData, null, 2), 'utf-8');
        console.log(`✅ temp.json 저장 완료.`);
    } catch (err) {
        console.error('파일 저장 실패 :', err.message);
    }
}

setUp();
