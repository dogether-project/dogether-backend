import {createDbConnection} from "../util/db-util.js";
import {createAllTable, removeAllTable} from "../util/db-query.js";

async function clearAllTableData() {
    console.log("🧹 모든 테이블 데이터 삭제 & 테이블 재생성 중...\n");
    await clearTables();
    console.log("🎉 모든 테이블 데이터 삭제 & 테이블 재생성 완료!\n");
}

async function clearTables() {
    const connection = await createDbConnection();
    await connection.beginTransaction();

    try {
        await removeAllTable(connection);
        await createAllTable(connection);

        await connection.commit();
    } catch (err) {
        connection.rollback();
        console.error("❌ 모든 테이블 데이터 삭제 & 테이블 재생성 중 오류 발생, 롤백 수행 완료.\n");
        throw err;
    } finally {
        await connection.end();
    }
}

clearAllTableData().then(() => {});
