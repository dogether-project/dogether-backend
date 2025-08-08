import fs from 'fs';
import path from 'path';
import {
    deleteAllMemberById,
    deleteDailyTodoStatsByMemberId,
    deleteNotificationTokenByMemberId
} from "../../../../../common/db/query/member-query.js";
import {
    deleteAllChallengeGroupById,
    deleteAllChallengeGroupMemberByChallengeGroupId,
    deleteAllLastSelectedChallengeGroupRecordByChallengeGroupId,
    getLastSelectedChallengeGroupIdsByMembers
} from "../../../../../common/db/query/challenge-group-query.js";
import {createLocalDbConnection, createSshTunnelDbConnection} from "../../../../../common/db/util/db-util.js";
import {
    deleteAllDailyTodoByMember,
    deleteAllDailyTodoHistoryByMember
} from "../../../../../common/db/query/daily-todo-query.js";

const temp = JSON.parse(fs.readFileSync(path.join('./script/temp.json'), 'utf-8'));
const connection = await createLocalDbConnection(); // Local DB 커넥션
// const connection = await createSshTunnelDbConnection(); // AWS DB 커넥션

async function tearDown() {
    const {
        firstTestMemberId,
        testMemberDataSize
    } = temp;

    const targetMemberIds = [];
    for (let i = 0; i < testMemberDataSize; i++) {
        targetMemberIds.push(firstTestMemberId + i);
    }
    const targetChallengeGroupIds = await getLastSelectedChallengeGroupIdsByMembers(connection, targetMemberIds);

    try {
        await connection.beginTransaction();

        await deleteAllDailyTodoHistoryByMember(connection, targetMemberIds);
        await deleteAllDailyTodoByMember(connection, targetMemberIds);

        await deleteAllLastSelectedChallengeGroupRecordByChallengeGroupId(connection, targetMemberIds);
        await deleteAllChallengeGroupMemberByChallengeGroupId(connection, targetMemberIds);
        await deleteAllChallengeGroupById(connection, targetChallengeGroupIds);

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
