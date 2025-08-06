import { batchInsert } from "../util/db-util.js";

/**
 * challenge_group 테이블 관련 쿼리
 */
export async function insertChallengeGroup(connection, insertData, batchSize) {
    console.log(`✏️ challenge_group 테이블에 데이터 ${insertData.length}건 삽입 시작.`);

    const query = `
        INSERT INTO challenge_group (
            id,
            name,
            maximum_member_count,
            join_code,
            status,
            start_at,
            end_at,
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
        'challenge_group'
    );
}

export async function getLastInsertedChallengeGroupId(connection) {
    const query = `SELECT MAX(id) AS lastInsertedChallengeGroupId FROM challenge_group`;
    const [rows] = await connection.query(query);

    return rows[0].lastInsertedChallengeGroupId; // 가장 큰 ID가 없으면 null 반환됨
}

export async function deleteAllChallengeGroupById(connection, ids) {
    if (ids.length === 0) {
        console.log(`👀 ids가 빈 배열이므로 challenge_group 테이블 데이터 삭제를 진행하지 않습니다.`);
        return;
    }

    const placeholders = ids.map(() => '?').join(', ');
    const query = `DELETE FROM challenge_group where id IN (${placeholders})`;

    const [result] = await connection.query(query, ids);
    console.log(`🗑️ challenge_group 테이블 데이터 ${result.affectedRows}건이 삭제되었습니다.`);
}

/**
 * challenge_group_member 테이블 관련 쿼리
 */
export async function insertChallengeGroupMember(connection, insertData, batchSize) {
    console.log(`✏️ challenge_group_member 테이블에 데이터 ${insertData.length}건 삽입 시작.`);

    const query = `
        INSERT INTO challenge_group_member (
            id,
            challenge_group_id,
            member_id,
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
        'challenge_group_member'
    );
}

export async function getLastInsertedChallengeGroupMemberId(connection) {
    const query = `SELECT MAX(id) AS lastInsertedChallengeGroupMemberId FROM challenge_group_member`;
    const [rows] = await connection.query(query);

    return rows[0].lastInsertedChallengeGroupMemberId; // 가장 큰 ID가 없으면 null 반환됨
}

export async function deleteAllChallengeGroupMemberByChallengeGroupId(connection, memberIds) {
    if (memberIds.length === 0) {
        console.log(`👀 memberIds가 빈 배열이므로 challenge_group_member 테이블 데이터 삭제를 진행하지 않습니다.`);
        return;
    }

    const placeholders = memberIds.map(() => '?').join(', ');
    const query = `DELETE FROM challenge_group_member where member_id IN (${placeholders})`;

    const [result] = await connection.query(query, memberIds);
    console.log(`🗑️ challenge_group_member 테이블 데이터 ${result.affectedRows}건이 삭제되었습니다.`);
}

/**
 * last_selected_challenge_group_record 테이블 관련 쿼리
 */
export async function insertLastSelectedChallengeGroupRecord(connection, insertData, batchSize) {
    console.log(`✏️ last_selected_challenge_group_record 테이블에 데이터 ${insertData.length}건 삽입 시작.`);

    const query = `
        INSERT INTO last_selected_challenge_group_record (
            id,
            challenge_group_id,
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
        'last_selected_challenge_group_record'
    );
}

export async function getLastInsertedLastSelectedChallengeGroupRecordId(connection) {
    const query = `SELECT MAX(id) AS InsertedLastSelectedChallengeGroupRecordId FROM last_selected_challenge_group_record`;
    const [rows] = await connection.query(query);

    return rows[0].InsertedLastSelectedChallengeGroupRecordId; // 가장 큰 ID가 없으면 null 반환됨
}

export const getLastSelectedChallengeGroupIdsByMembers = async (connection, memberIds) => {
    const placeholders = memberIds.map(() => '?').join(', ');
    const query = `
        SELECT DISTINCT challenge_group_id
        FROM last_selected_challenge_group_record
        WHERE member_id IN (${placeholders})
    `;

    const [rows] = await connection.query(query, memberIds);
    return rows.map(row => row.challenge_group_id);
}

export async function deleteAllLastSelectedChallengeGroupRecordByChallengeGroupId(connection, memberIds) {
    if (memberIds.length === 0) {
        console.log(`👀 memberIds가 빈 배열이므로 last_selected_challenge_group_record 테이블 데이터 삭제를 진행하지 않습니다.`);
        return;
    }

    const placeholders = memberIds.map(() => '?').join(', ');
    const query = `DELETE FROM last_selected_challenge_group_record where member_id IN (${placeholders})`;

    const [result] = await connection.query(query, memberIds);
    console.log(`🗑️ last_selected_challenge_group_record 테이블 데이터 ${result.affectedRows}건이 삭제되었습니다.`);
}
