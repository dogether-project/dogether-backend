import { fileURLToPath } from 'url';
import fs from 'fs';
import path from 'path';
import { Client } from "ssh2";
import mysql from "mysql2/promise";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const dbConfig = JSON.parse(
    fs.readFileSync(path.join(__dirname, '../../../secret/db-secret.json'), 'utf-8')
);

export async function createLocalDbConnection() {
    const connection = await mysql.createConnection(dbConfig.local);

    console.log(`✅ Local DB 커넥션 생성 완료.\n`);
    return connection;
}

export async function createSshTunnelDbConnection() {
    const bastionConfig = dbConfig.bastion;
    const forwardConfig = dbConfig.forward;
    const awsDbConfig = dbConfig.aws;

    return new Promise((resolve, reject) => {
        const sshClient = new Client();

        sshClient.on("error", (err) => {
            console.error("❌ SSH 터널 에러 발생:", err);
        });

        sshClient.on("ready", () => {
            console.log("✅ Bastion server ssh 연결 성공.");

            sshClient.forwardOut(
                forwardConfig.srcHost,
                forwardConfig.srcPort,
                forwardConfig.dstHost,
                forwardConfig.dstPort,
                async (err, stream) => {
                    if (err) {
                        sshClient.end();
                        return reject(err);
                    }

                    try {
                        const connection = await mysql.createConnection({
                            ...awsDbConfig,
                            stream,
                        });

                        console.log("✅ SSH 터널링 DB 커넥션 생성 완료.\n");

                        // 👇 DB 연결 종료 시 SSH 터널도 함께 닫도록 end 메서드 오버라이드
                        const originalEnd = connection.end.bind(connection);
                        connection.end = async function (...args) {
                            console.log("🛑 DB 연결 종료 요청. SSH 터널도 함께 종료합니다.\n");
                            sshClient.end();
                            return originalEnd(...args);
                        };

                        resolve(connection); // ✅ connection만 반환
                    } catch (dbErr) {
                        sshClient.end();
                        reject(dbErr);
                    }
                }
            );
        });

        // 실제 SSH 연결 시작
        sshClient.connect({
            host: bastionConfig.host,
            port: 22,
            username: bastionConfig.username,
            privateKey: fs.readFileSync(path.join(__dirname, bastionConfig.privateKeyPath)),
        });
    });
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
