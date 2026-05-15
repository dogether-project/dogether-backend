import {createDbConnection} from "../../util/db-util.js";
import {getDateNDaysAgoMySqlDateFormatString} from "../../util/time-util.js";
import {deleteAllByRowInsertedAt} from "../../util/db-query.js";

async function clearCurrentActivityData() {
    // nDay전 날짜로 row_inserted_at이 설정된 데이터를 모두 삭제
    // 기본값은 0, 원하는 날짜로 변경해서 사용후 원복할 것
    const nDay = 1;
    console.log(`🧹 현재 활동 테스트 데이터 삭제 시작. (${nDay}일전 데이터, ${getDateNDaysAgoMySqlDateFormatString(nDay)})\n`);
    await clearData(nDay);
    console.log("🎉 현재 활동 데이터 삭제 완료!\n");
}

async function clearData(nDay) {
    const today = getDateNDaysAgoMySqlDateFormatString(nDay);
    const tables = [
        "daily_todo_certification_reviewer",
        "daily_todo_certification",
        "daily_todo_history_read",
        "daily_todo_history",
        "daily_todo",
        "last_selected_challenge_group_record",
        "challenge_group_member",
        "challenge_group"
    ];

    const connection = await createDbConnection();
    await connection.beginTransaction();

    try {
        for (const tbl of tables) {
            await deleteAllByRowInsertedAt(connection, tbl, today);
        }

        await connection.commit();
    } catch (err) {
        await connection.rollback();
        console.error("❌ 현재 활동 데이터 삭제 중 오류 발생, 롤백 수행 완료.\n");
        throw err;
    } finally {
        await connection.end();
    }
}

clearCurrentActivityData().then(() => {});
