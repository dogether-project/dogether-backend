import fs from 'fs';
import {format} from "fast-csv";
import {
    CSV_SAVED_BASE_PATH,
    MEMBER_COUNT,
    PAST_GROUP_ACTIVITY_START_AT,
    PAST_TOTAL_DAILY_TODO_CERTIFICATION_COUNT,
} from "../test-data-common.js";
import {convertDateObjectToMySqlDatetimeFormat} from "../../util/time-util.js";

// =========== CSV Stream ===========
const member_stream = format({ headers: true });
member_stream.pipe(fs.createWriteStream(`${CSV_SAVED_BASE_PATH}/1_member.csv`));

const notification_token_stream = format({ headers: true });
notification_token_stream.pipe(fs.createWriteStream(`${CSV_SAVED_BASE_PATH}/2_notification_token.csv`));

const daily_todo_stats_stream = format({ headers: true });
daily_todo_stats_stream.pipe(fs.createWriteStream(`${CSV_SAVED_BASE_PATH}/3_daily_todo_stats.csv`));

// =========== 메인 로직 ===========
let memberId = 1;  // member & notification_token & daily_todo_stats

async function createMemberTestData() {
    console.log("🧑‍🍳 member & notification_token & daily_todo_stats 테스트 데이터 생성중...");
    await generateData();
    console.log("✅ member & notification_token & daily_todo_stats 테스트 데이터 생성 완료!");
}

async function generateData() {
    let memberCreatedAt = PAST_GROUP_ACTIVITY_START_AT;
    memberCreatedAt.setHours(6, 0, 0, 0);
    memberCreatedAt = convertDateObjectToMySqlDatetimeFormat(memberCreatedAt);

    const dailyTodoCertificationCountPerMember = PAST_TOTAL_DAILY_TODO_CERTIFICATION_COUNT / MEMBER_COUNT;
    const approvedDailyTodoCertificationCountPerMember = Math.ceil(dailyTodoCertificationCountPerMember / 2);
    const rejectedDailyTodoCertificationCountPerMember = dailyTodoCertificationCountPerMember - approvedDailyTodoCertificationCountPerMember;

    for (let i = 0; i < MEMBER_COUNT; i++) {
        const currentMemberId = memberId++;

        member_stream.write({
            id: currentMemberId,
            provider_id: `pid-${currentMemberId}`,
            name: `m-${currentMemberId}`,
            profile_image_url: `http://profile-image.site/${currentMemberId}`,
            created_at: memberCreatedAt,
            row_inserted_at: memberCreatedAt,
            row_updated_at: null
        });

        notification_token_stream.write({
            id: currentMemberId,
            member_id: currentMemberId,
            token_value: `t-${currentMemberId}`,
            row_inserted_at: memberCreatedAt,
            row_updated_at: null
        });

        daily_todo_stats_stream.write({
            id: currentMemberId,
            member_id: currentMemberId,
            certificated_count: dailyTodoCertificationCountPerMember,
            approved_count: approvedDailyTodoCertificationCountPerMember,
            rejected_count: rejectedDailyTodoCertificationCountPerMember,
            row_inserted_at: memberCreatedAt,
            row_updated_at: null
        });
    }

    member_stream.end();
    notification_token_stream.end();
    daily_todo_stats_stream.end();

    await Promise.all([
        waitForStreamFinish(member_stream),
        waitForStreamFinish(notification_token_stream),
        waitForStreamFinish(daily_todo_stats_stream)
    ]);
}

/**
 * CSV 파일 Stream Flush 체크
 */
function waitForStreamFinish(stream) {
    return new Promise((resolve, reject) => {
        stream.on('finish', resolve);
        stream.on('error', reject);
    });
}

createMemberTestData().then(() => {});
