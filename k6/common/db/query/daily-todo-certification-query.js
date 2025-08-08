import { batchInsert } from "../util/db-util.js";

/**
 * daily_todo_certification 테이블 관련 쿼리
 */
export async function insertDailyTodoCertification(connection, insertData, batchSize) {
    console.log(`✏️ daily_todo_certification 테이블에 데이터 ${insertData.length}건 삽입 시작.`);

    const query = `
        INSERT INTO daily_todo_certification (
            id,
            daily_todo_id,
            content,
            media_url,
            review_status,
            review_feedback,
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
        'daily_todo_certification'
    );
}

export const getLastInsertedDailyTodoCertificationId = async (connection) => {
    const query = `SELECT MAX(id) AS lastInsertedDailyTodoCertificationId FROM daily_todo_certification`;
    const [rows] = await connection.query(query);

    return rows[0].lastInsertedDailyTodoCertificationId; // 가장 큰 ID가 없으면 null 반환됨
}

export async function deleteAllDailyTodoCertificationByMember(connection, memberIds) {
    const placeholders = memberIds.map(() => '?').join(', ');
    const query = `
        DELETE dc FROM daily_todo_certification dc
        JOIN daily_todo d ON dc.daily_todo_id = d.id
        WHERE d.writer_id IN (${placeholders})`;

    const [result] = await connection.query(query, memberIds);
    console.log(`🗑️ daily_todo_certification 테이블 데이터 ${result.affectedRows}건이 삭제되었습니다.`);
}

/**
 * daily_todo_certification_reviewer 테이블 관련 쿼리
 */
export async function insertDailyTodoCertificationReviewer(connection, insertData, batchSize) {
    console.log(`✏️ daily_todo_certification_reviewer 테이블에 데이터 ${insertData.length}건 삽입 시작.`);

    const query = `
        INSERT INTO daily_todo_certification_reviewer (
            id,
            daily_todo_certification_id,
            reviewer_id,
            row_inserted_at,
            row_updated_at                
        ) VALUES ?
    `;

    await batchInsert(
        connection,
        query,
        insertData,
        batchSize,
        'daily_todo_certification_reviewer'
    );
}

export const getLastInsertedDailyTodoCertificationReviewerId = async (connection) => {
    const query = `SELECT MAX(id) AS lastInsertedDailyTodoCertificationReviewerId FROM daily_todo_certification_reviewer`;
    const [rows] = await connection.query(query);

    return rows[0].lastInsertedDailyTodoCertificationReviewerId; // 가장 큰 ID가 없으면 null 반환됨
}

export async function deleteAllDailyTodoCertificationReviewerByMember(connection, memberIds) {
    const placeholders = memberIds.map(() => '?').join(', ');
    const query = `
        DELETE dcr FROM daily_todo_certification_reviewer dcr
        JOIN daily_todo_certification dc ON dcr.daily_todo_certification_id = dc.id
        JOIN daily_todo d ON dc.daily_todo_id = d.id
        WHERE d.writer_id IN (${placeholders})`;

    const [result] = await connection.query(query, memberIds);
    console.log(`🗑️ daily_todo_certification_reviewer 테이블 데이터 ${result.affectedRows}건이 삭제되었습니다.`);
}
