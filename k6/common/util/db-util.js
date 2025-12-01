import { fileURLToPath } from 'url';
import fs from 'fs';
import path from 'path';
import mysql from "mysql2/promise";
import {
    insertChallengeGroup,
    insertChallengeGroupMember,
    insertDailyTodo, insertDailyTodoCertification,
    insertDailyTodoCertificationReviewer, insertDailyTodoHistory, insertDailyTodoHistoryRead,
    insertLastSelectedChallengeGroupRecord
} from "./db-query.js";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const dbConfig = JSON.parse(fs.readFileSync(path.join(__dirname, '../secret/db-secret.json'), 'utf-8'));

/**
 * DB 커넥션 생성
 */
export async function createDbConnection() {
    /**
     * 필요한 커넥션의 주석을 풀어서 사용! (기본값은 Local, 원하는 옵션으로 변경해서 사용 후 기본값으로 원복할 것)
     * 1. Local : await createLocalDbConnection();
     * 2. AWS : await createAwsDbConnection();
     */
    const connection = await createLocalDbConnection(); // Local DB 커넥션
    // const connection = await createAwsDbConnection(); // AWS DB 커넥션

    return connection;
}

async function createLocalDbConnection() {
    console.log(`🏃 Local DB 커넥션 생성중...`);
    const connection = await mysql.createConnection(dbConfig.local);
    console.log(`✅ Local DB 커넥션 생성 완료!\n`);

    return connection;
}

async function createAwsDbConnection() {
    console.log(`🏃 AWS DB 커넥션 생성중...`);
    const connection = await mysql.createConnection(dbConfig.aws);
    console.log(`✅ AWS DB 커넥션 생성 완료!\n`);

    return connection;
}

/**
 * 현재 활동 데이터 삽입
 */
export async function insertCurrentActivityData(currentActivityData) {
    const connection = await createDbConnection();
    console.log(`👷 [Const Current Activity Data] 현재 활동 테스트 데이터 DB 삽입중...\n`);

    // insert 순서 정의
    const steps = [
        { label: "challenge_group", fn: insertChallengeGroup, key: "challenge_group_data" },
        { label: "challenge_group_member", fn: insertChallengeGroupMember, key: "challenge_group_member_data" },
        { label: "last_selected_challenge_group_record", fn: insertLastSelectedChallengeGroupRecord, key: "last_selected_challenge_group_record_data" },

        { label: "daily_todo", fn: insertDailyTodo, key: "daily_todo_data" },
        { label: "daily_todo_history", fn: insertDailyTodoHistory, key: "daily_todo_history_data" },
        { label: "daily_todo_history_read", fn: insertDailyTodoHistoryRead, key: "daily_todo_history_read_data" },

        { label: "daily_todo_certification", fn: insertDailyTodoCertification, key: "daily_todo_certification_data" },
        { label: "daily_todo_certification_reviewer", fn: insertDailyTodoCertificationReviewer, key: "daily_todo_certification_reviewer_data" }
    ];

    try {
        await connection.beginTransaction();

        const batchSize = currentActivityData.batch_size ?? 100000;
        for (const step of steps) {
            const rows = currentActivityData[step.key];
            if (hasRows(rows)) {
                await step.fn(connection, rows, batchSize);
            } else {
                console.log(`⏭️ ${step.label} 스킵 (데이터 없음)\n`);
            }
        }

        await connection.commit();
        console.log("🥳 현재 활동 테스트 데이터 DB 삽입 완료!\n");
    } catch (error) {
        await connection.rollback();
        console.error("❌ 에러 발생! 롤백 수행됨.");
        console.error(error);
    } finally {
        await connection.end();
    }
}

function hasRows(rows) {
    return Array.isArray(rows) && rows.length > 0;
}

export async function batchInsert(connection, query, data, batchSize, targetTable) {
    const totalInsertDataCount = data.length;
    let totalInsertedCount = 0;

    for (let i = 0; i < totalInsertDataCount; i += batchSize) {
        const batch = data.slice(i, i + batchSize);
        const [result] = await connection.query(query, [batch]);

        totalInsertedCount += result.affectedRows;
        const insertedDataCount = Math.min(i + batch.length, totalInsertDataCount);
        process.stdout.write(`\r🟢 ${targetTable} 테이블 데이터 삽입 진행률 : ${insertedDataCount}/${totalInsertDataCount}`);
    }

    process.stdout.write('\n');
    console.log(`✅ ${targetTable} 테이블 데이터 ${totalInsertedCount}건 삽입 완료!\n`);
}
