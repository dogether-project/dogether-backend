import { batchInsert } from "../util/db-util.js";

/**
 * member 테이블 관련 쿼리
 */
export async function insertMember(connection, insertData, batchSize) {
    console.log(`✏️ member 테이블에 데이터 ${insertData.length}건 삽입 시작.`);

    const query = `
        INSERT INTO member (
            id,
            provider_id,
            name,
            profile_image_url,
            created_at,
            row_inserted_at,
            row_updated_at                
        ) VALUES ?
    `;

    await batchInsert(
        connection,
        query,
        insertData,
        batchSize,
        'member'
    );
}

export async function deleteAllMemberById(connection, ids) {
    const placeholders = ids.map(() => '?').join(', ');
    const query = `DELETE FROM member where id IN (${placeholders})`;

    const [result] = await connection.query(query, ids);
    console.log(`🗑️ member 테이블 데이터 ${result.affectedRows}건이 삭제되었습니다.`);
}

/**
 * notification_token 테이블 관련 쿼리
 */
export async function insertNotificationToken(connection, insertData, batchSize) {
    console.log(`✏️ notification_token 테이블에 데이터 ${insertData.length}건 삽입 시작.`);

    const query = `
        INSERT INTO notification_token (
            id,
            member_id,
            token_value,
            row_inserted_at,
            row_updated_at
        ) VALUES ?
    `;

    await batchInsert(
        connection,
        query,
        insertData,
        batchSize,
        'notification_token'
    );
}

export async function getLastInsertedNotificationTokenId(connection) {
    const query = `SELECT MAX(id) AS lastInsertedNotificationTokenId FROM notification_token`;
    const [rows] = await connection.query(query);

    return rows[0].lastInsertedNotificationTokenId; // 가장 큰 ID가 없으면 null 반환됨
}

export async function deleteNotificationTokenByMemberId(connection, memberIds) {
    const placeholders = memberIds.map(() => '?').join(', ');
    const query = `DELETE FROM notification_token where member_id IN (${placeholders})`;

    const [result] = await connection.query(query, memberIds);
    console.log(`🗑️ notification_token 테이블 데이터 ${result.affectedRows}건이 삭제되었습니다.`);
}

/**
 * daily_todo_stats 테이블 관련 쿼리
 */
export async function insertDailyTodoStats(connection, insertData, batchSize) {
    console.log(`✏️ daily_todo_stats 테이블에 데이터 ${insertData.length}건 삽입 시작.`);

    const query = `
        INSERT INTO daily_todo_stats (
            id,
            member_id,
            certificated_count,
            approved_count,
            rejected_count,
            row_inserted_at,
            row_updated_at                
        ) VALUES ?
    `;

    await batchInsert(
        connection,
        query,
        insertData,
        batchSize,
        'daily_todo_stats'
    );
}

export async function getLastInsertedDailyTodoStatsId(connection) {
    const query = `SELECT MAX(id) AS lastInsertedDailyTodoStatsId FROM daily_todo_stats`;
    const [rows] = await connection.query(query);

    return rows[0].lastInsertedDailyTodoStatsId; // 가장 큰 ID가 없으면 null 반환됨
}

export async function deleteDailyTodoStatsByMemberId(connection, memberIds) {
    const placeholders = memberIds.map(() => '?').join(', ');
    const query = `DELETE FROM daily_todo_stats where member_id IN (${placeholders})`;

    const [result] = await connection.query(query, memberIds);
    console.log(`🗑️ daily_todo_stats 테이블 데이터 ${result.affectedRows}건이 삭제되었습니다.`);
}
