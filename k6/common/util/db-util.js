import { fileURLToPath } from 'url';
import fs from 'fs';
import path from 'path';
import mysql from "mysql2/promise";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const dbConfig = JSON.parse(fs.readFileSync(path.join(__dirname, '../secret/db-secret.json'), 'utf-8'));

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
