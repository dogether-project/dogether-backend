import mysql from "mysql2/promise";

export async function createDbConnection(dbConfig) {
    const connection = await mysql.createConnection(dbConfig);

    console.log(`✅ DB 커넥션 생성 완료.\n`);
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

/**
 * 현재 KST 날짜 계산 함수
 */
export function getCurrentDateInKst() {
    const nowDate = new Date();
    return nowDate.toLocaleString('sv-SE', { timeZone: 'Asia/Seoul' })
        .replace('T', ' ');
}

/**
 * n일 전 KST 날짜 계산 함수
 */
export function getDateNDaysAgoInKst(n) {
    const now = new Date();
    const kstTime = new Date(now.getTime() + 9 * 60 * 60 * 1000); // UTC → KST
    kstTime.setDate(kstTime.getDate() - n);
    return kstTime.toLocaleString('sv-SE', { timeZone: 'Asia/Seoul' });
}

/**
 * n일 후 KST 날짜 계산 함수
 */
export function getDateNDaysLaterInKst(n) {
    const now = new Date();
    const kstTime = new Date(now.getTime() + 9 * 60 * 60 * 1000); // UTC → KST
    kstTime.setDate(kstTime.getDate() + n);
    return kstTime.toLocaleString('sv-SE', { timeZone: 'Asia/Seoul' });
}

/**
 * startAgoDays일 전에 시작하여 durationDays일 만큼 진행하는 그룹의 종료일 계산 함수
 */
export function getEndDateFromStartAgoAndDuration(startAgoDays, durationDays) {
    const now = new Date();

    // 시작일 계산: 현재 - startAgoDays
    const startDate = new Date(now.getTime() + 9 * 60 * 60 * 1000); // UTC → KST
    startDate.setDate(startDate.getDate() - startAgoDays);

    // 종료일 계산: 시작일 + (durationDays - 1)
    const endDate = new Date(startDate.getTime());
    endDate.setDate(endDate.getDate() + durationDays - 1);

    return endDate.toLocaleString('sv-SE', { timeZone: 'Asia/Seoul' });
}

/**
 * 주어진 그룹 번호(n)와 그룹 크기(groupSize)에 따라 해당 그룹에 속하는 연속된 정수 배열을 반환
 * ex) n = 2, groupSize = 3 → [4, 5, 6]
 */
export function getConsecutiveNumbersByGroup(n, groupSize) {
    const start = (n - 1) * groupSize + 1;
    return Array.from({ length: groupSize }, (_, i) => start + i);
}
