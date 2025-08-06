import fs from 'fs';
import path from 'path';
import {
    deleteAllMemberById,
    deleteDailyTodoStatsByMemberId,
    deleteNotificationTokenByMemberId
} from "../../../../../common/db/query/member-query.js";
import {createDbConnection} from "../../../../../common/db/util/db-util.js";

const temp = JSON.parse(fs.readFileSync(path.join('./script/temp.json'), 'utf-8'));
const dbConfig = JSON.parse(fs.readFileSync(path.join('../../../../secret/db-secret.json'), 'utf-8'));
const connection = await createDbConnection(dbConfig);

async function tearDown() {
    const {
        firstTestMemberId,
        testMemberDataSize
    } = temp;

    const targetMemberIds = [];
    for (let i = 0; i < testMemberDataSize; i++) {
        targetMemberIds.push(firstTestMemberId + i);
    }

    try {
        await connection.beginTransaction();

        await deleteDailyTodoStatsByMemberId(connection, targetMemberIds);
        await deleteNotificationTokenByMemberId(connection, targetMemberIds);
        await deleteAllMemberById(connection, targetMemberIds);

        await connection.commit();
        console.log('\n🥳 테스트 데이터 삭제 완료!\n');
    } catch (error) {
        await connection.rollback();
        console.error(`❌ 에러 발생! 롤백 수행됨.`);
        console.error(error);
    } finally {
        await connection.end();
    }
}

tearDown();
