import {
    insertDailyTodoStats,
    insertMember,
    insertNotificationToken
} from "../query/member-query.js";
import {
    insertChallengeGroup,
    insertChallengeGroupMember,
    insertLastSelectedChallengeGroupRecord
} from "../query/challenge-group-query.js";
import {
    insertDailyTodo,
    insertDailyTodoHistory
} from "../query/daily-todo-query.js";
import {
    insertDailyTodoCertification,
    insertDailyTodoCertificationReviewer
} from "../query/daily-todo-certification-query.js";
import {createLocalDbConnection, createSshTunnelDbConnection} from "./db-util.js";

function hasRows(rows) {
    return Array.isArray(rows) && rows.length > 0;
}

export async function insertData(dataGenerator) {
    const connection = await createLocalDbConnection(); // Local DB 커넥션
    // const connection = await createSshTunnelDbConnection(); // AWS DB 커넥션

    const data = dataGenerator(connection);
    const batchSize = data.batch_size ?? 100;

    // insert 순서 정의
    const steps = [
        { label: "member", fn: insertMember, key: "member_data" },
        { label: "notification_token", fn: insertNotificationToken, key: "notification_token_data" },
        { label: "daily_todo_stats", fn: insertDailyTodoStats, key: "daily_todo_stats_data" },

        { label: "challenge_group", fn: insertChallengeGroup, key: "challenge_group_data" },
        { label: "challenge_group_member", fn: insertChallengeGroupMember, key: "challenge_group_member_data" },
        { label: "last_selected_challenge_group_record", fn: insertLastSelectedChallengeGroupRecord, key: "last_selected_challenge_group_record_data" },

        { label: "daily_todo", fn: insertDailyTodo, key: "daily_todo_data" },
        { label: "daily_todo_history", fn: insertDailyTodoHistory, key: "daily_todo_history_data" },

        { label: "daily_todo_certification", fn: insertDailyTodoCertification, key: "daily_todo_certification_data" },
        { label: "daily_todo_certification_reviewer", fn: insertDailyTodoCertificationReviewer, key: "daily_todo_certification_reviewer_data" }
    ];

    try {
        await connection.beginTransaction();

        for (const step of steps) {
            const rows = data[step.key];
            if (hasRows(rows)) {
                await step.fn(connection, rows, batchSize);
            } else {
                console.log(`⏭️  ${step.label} 스킵 (데이터 없음)\n`);
            }
        }

        await connection.commit();
        console.log("🥳 전체 테이블 더미 데이터 삽입 완료!");
    } catch (error) {
        await connection.rollback();
        console.error("❌ 에러 발생! 롤백 수행됨.");
        console.error(error);
    } finally {
        await connection.end();
    }
}
