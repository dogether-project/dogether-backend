import { batchInsert } from "../util/db-util.js";

/**
 * daily_todo 테이블 관련 쿼리
 */
export async function insertDailyTodo(connection, insertData, batchSize) {
    console.log(`✏️ daily_todo 테이블에 데이터 ${insertData.length}건 삽입 시작.`);

    const query = `
        INSERT INTO daily_todo (
            id,
            challenge_group_id,
            writer_id,
            content,
            status,
            written_at,
            row_inserted_at,
            row_updated_at                
        ) VALUES ?
    `;

    await batchInsert(
        connection,
        query,
        insertData,
        batchSize,
        'daily_todo'
    );
}

export const getLastInsertedDailyTodoId = async (connection) => {
    const query = `SELECT MAX(id) AS lastInsertedDailyTodoId FROM daily_todo`;
    const [rows] = await connection.query(query);

    return rows[0].lastInsertedDailyTodoId; // 가장 큰 ID가 없으면 null 반환됨
}

export async function deleteAllDailyTodoByMember(connection, memberIds) {
    const placeholders = memberIds.map(() => '?').join(', ');
    const query = `DELETE FROM daily_todo where writer_id IN (${placeholders})`;

    const [result] = await connection.query(query, memberIds);
    console.log(`🗑️ daily_todo 테이블 데이터 ${result.affectedRows}건이 삭제되었습니다.`);
}

/**
 * daily_todo_history 테이블 관련 쿼리
 */
export async function insertDailyTodoHistory(connection, insertData, batchSize) {
    console.log(`✏️ daily_todo_history 테이블에 데이터 ${insertData.length}건 삽입 시작.`);

    const query = `
        INSERT INTO daily_todo_history (
            id,
            daily_todo_id,
            event_time,
            row_inserted_at,
            row_updated_at                
        ) VALUES ?
    `;

    await batchInsert(
        connection,
        query,
        insertData,
        batchSize,
        'daily_todo_history'
    );
}

export const getLastInsertedDailyTodoHistoryId = async (connection) => {
    const query = `SELECT MAX(id) AS lastInsertedDailyTodoHistoryId FROM daily_todo_history`;
    const [rows] = await connection.query(query);

    return rows[0].lastInsertedDailyTodoHistoryId; // 가장 큰 ID가 없으면 null 반환됨
}

export async function deleteAllDailyTodoHistoryByMember(connection, memberIds) {
    const placeholders = memberIds.map(() => '?').join(', ');
    const query = `
        DELETE dh FROM daily_todo_history dh
        JOIN daily_todo d ON dh.daily_todo_id = d.id
        WHERE d.writer_id IN (${placeholders})`;

    const [result] = await connection.query(query, memberIds);
    console.log(`🗑️ daily_todo_history 테이블 데이터 ${result.affectedRows}건이 삭제되었습니다.`);
}

/**
 * daily_todo_history_read 테이블 관련 쿼리
 */
export async function insertDailyTodoHistoryRead(connection, insertData, batchSize) {
    console.log(`✏️ daily_todo_history_read 테이블에 데이터 ${insertData.length}건 삽입 시작.`);

    const query = `
        INSERT INTO daily_todo_history_read (
            id,
            daily_todo_history_id,
            member_id,
            row_inserted_at,
            row_updated_at                
        ) VALUES ?
    `;

    await batchInsert(
        connection,
        query,
        insertData,
        batchSize,
        'daily_todo_history_read'
    );
}
