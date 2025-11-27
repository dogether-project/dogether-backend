import {createDbConnection} from "../../util/db-util.js";
import {
    insertChallengeGroup,
    insertChallengeGroupMember,
    insertLastSelectedChallengeGroupRecord,
    insertDailyTodo,
    insertDailyTodoHistory,
    insertDailyTodoHistoryRead,
    insertDailyTodoCertification,
    insertDailyTodoCertificationReviewer
} from "../../util/db-query.js";
import {
    createCurrentActivityForReadTestData
} from "./for-read-current-activity-test-data.js";
import {
    createCurrentActivityForWriteTestData}
    from "./for-write-current-activity-test-data.js";

async function insertCurrentActivityData() {
    /**
     * 필요한 데이터 생성 옵션 주석을 풀어서 사용! (기본값은 쓰기 테스트용, 원하는 옵션으로 변경해서 사용 후 기본값으로 원복할 것)
     * 1. 쓰기 테스트용 더미 데이터 : await createAwsDbConnection();
     * 2. 조회 테스트용 더미 데이터 : await createLocalDbConnection();
     */
    await insertData(createCurrentActivityForWriteTestData());
    // await insertData(await createCurrentActivityForReadTestData());
}

async function insertData(currentActivityData) {
    const connection = await createDbConnection();
    console.log(`👷 [Const Current Activity Data For ${currentActivityData.data_type}] 현재 활동 테스트 데이터 DB 삽입중...\n`);

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

insertCurrentActivityData().then(() => {});
