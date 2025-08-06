import fs from 'fs';
import path from 'path';
import {createDbConnection, getCurrentDateInKst} from "../../../../../common/db/util/db-util.js";
import {
    getLastInsertedDailyTodoStatsId,
    getLastInsertedNotificationTokenId,
    insertDailyTodoStats,
    insertMember,
    insertNotificationToken
} from "../../../../../common/db/query/member-query.js";

const TEMP_OUTPUT_PATH = path.join('./script/temp.json');
const dbConfig = JSON.parse(fs.readFileSync(path.join('../../../../secret/db-secret.json'), 'utf-8'));
const connection = await createDbConnection(dbConfig);
const batchSize = 100;

const TEST_MEMBER_COUNT = 100;
const firstTestMemberId = 1;
const firstTestNotificationTokenId = await getLastInsertedNotificationTokenId(connection) + 1;
const firstTestDailyTodoStatsId = await getLastInsertedDailyTodoStatsId(connection) + 1;

async function setUp() {
    try {
        await connection.beginTransaction();

        await insertTestMemberData(connection, batchSize);
        await createTestNotificationTokenData(connection, batchSize);
        await createTestDailyTodoStatsData(connection, batchSize);
        createTempFile();

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
    }

    await insertMember(connection, testMemberData, batchSize);
}

const createTestNotificationTokenData = async (connection, batchSize) => {
    const testNotificationTokenData = [];

    const rowInsertedAt = getCurrentDateInKst();
    const rowUpdatedAt = null;
    for (let i = 0; i < TEST_MEMBER_COUNT; i++) {
        const id = firstTestNotificationTokenId + i;
        const memberId = i + 1;
        const tokenValue = `t-${id}`;

        testNotificationTokenData.push([
            id, memberId, tokenValue, rowInsertedAt, rowUpdatedAt
        ]);
    }

    await insertNotificationToken(connection, testNotificationTokenData, batchSize);
}

const createTestDailyTodoStatsData = async (connection, batchSize) => {
    const testDailyTodoStatsData = [];

    const certificatedCount = 0;
    const approvedCount = 0;
    const rejectedCount = 0;
    const rowInsertedAt = getCurrentDateInKst();
    const rowUpdatedAt = null;
    for (let i = 0; i < TEST_MEMBER_COUNT; i++) {
        const id = firstTestDailyTodoStatsId + i;
        const memberId = i + 1;

        testDailyTodoStatsData.push([
            id, memberId, certificatedCount, approvedCount, rejectedCount, rowInsertedAt, rowUpdatedAt
        ]);
    }

    await insertDailyTodoStats(connection, testDailyTodoStatsData, batchSize);
};

const createTempFile = () => {
    const tempData = {
        firstTestMemberId: firstTestMemberId,
        testMemberDataSize: TEST_MEMBER_COUNT
    };

    try {
        fs.writeFileSync(TEMP_OUTPUT_PATH, JSON.stringify(tempData, null, 2), 'utf-8');
        console.log(`✅ temp.json 저장 완료.`);
    } catch (err) {
        console.error('파일 저장 실패 :', err.message);
    }
}

setUp();
