import fs from 'fs';
import path from 'path';
import { createDbConnection } from "../util/db-util.js";
import { createDummyData } from "./data/dummy-data-1.js";
import {
    insertDailyTodoStats,
    insertMember,
    insertNotificationToken
} from "../query/member-query.js";
import {
    insertChallengeGroup,
    insertChallengeGroupMember,
    insertLastSelectedChallengeGroupRecord
} from "../query/challenge-group-query.js";
import {
    insertDailyTodo,
    insertDailyTodoHistory
} from "../query/daily-todo-query.js";
import {
    insertDailyTodoCertification,
    insertDailyTodoCertificationReviewer
} from "../query/daily-todo-certification-query.js";

async function main() {
    const dbConfig = JSON.parse(fs.readFileSync(path.join('../../../secret/db-secret.json'), 'utf-8'));
    const connection = await createDbConnection(dbConfig);
    const dummyData = createDummyData();
    const batchSize = dummyData.batch_size;

    try {
        await connection.beginTransaction();

        await insertMember(connection, dummyData.member_data, batchSize);
        await insertNotificationToken(connection, dummyData.notification_token_data, batchSize);
        await insertDailyTodoStats(connection, dummyData.daily_todo_stats_data, batchSize);

        await insertChallengeGroup(connection, dummyData.challenge_group_data, batchSize);
        await insertChallengeGroupMember(connection, dummyData.challenge_group_member_data, batchSize);
        await insertLastSelectedChallengeGroupRecord(connection, dummyData.last_selected_challenge_group_record_data, batchSize);

        await insertDailyTodo(connection, dummyData.daily_todo_data, batchSize);
        await insertDailyTodoHistory(connection, dummyData.daily_todo_history_data, batchSize);

        await insertDailyTodoCertification(connection, dummyData.daily_todo_certification_data, batchSize);
        await insertDailyTodoCertificationReviewer(connection, dummyData.daily_todo_certification_reviewer_data, batchSize);

        await connection.commit();

        console.log('🥳 전체 테이블 더미 데이터 모두 삽입 완료!');
    } catch (error) {
        await connection.rollback();
        console.error(`❌ 에러 발생! 롤백 수행됨.`);
        console.error(error);
    } finally {
        await connection.end();
    }
}

main();
