import fs from 'fs';
import {format} from "fast-csv";
import {
    DAY_TODO_PER_MEMBER_COUNT,
    GROUP_PER_MEMBER_COUNT, MEMBER_COUNT, MEMBER_PER_GROUP_COUNT,
    PAST_GROUP_RUNNING_DAY,
    waitForStreamFinish,
    calculateNextDate, calculateEndAt, convertDateTimeFormatString, PAST_ACTIVITY_DATA_BASE_PATH, toDateOnly,
    PAST_TOTAL_ACTIVITY_CYCLE, PAST_GROUP_ACTIVITY_START_AT, PAST_ONE_CYCLE_PER_GROUP_COUNT,
    PAST_TOTAL_DAILY_TODO_CERTIFICATION_COUNT
} from "./test-data-common.js";

// =========== CSV Stream ===========
const member_stream = format({ headers: true });
member_stream.pipe(fs.createWriteStream(`${PAST_ACTIVITY_DATA_BASE_PATH}/member.csv`));

const notification_token_stream = format({ headers: true });
notification_token_stream.pipe(fs.createWriteStream(`${PAST_ACTIVITY_DATA_BASE_PATH}/notification_token.csv`));

const daily_todo_stats_stream = format({ headers: true });
daily_todo_stats_stream.pipe(fs.createWriteStream(`${PAST_ACTIVITY_DATA_BASE_PATH}/daily_todo_stats.csv`));

const challenge_group_stream = format({ headers: true });
challenge_group_stream.pipe(fs.createWriteStream(`${PAST_ACTIVITY_DATA_BASE_PATH}/challenge_group.csv`));

const challenge_group_member_stream = format({ headers: true });
challenge_group_member_stream.pipe(fs.createWriteStream(`${PAST_ACTIVITY_DATA_BASE_PATH}/challenge_group_member.csv`));

const daily_todo_stream = format({ headers: true });
daily_todo_stream.pipe(fs.createWriteStream(`${PAST_ACTIVITY_DATA_BASE_PATH}/daily_todo.csv`));

const daily_todo_history_stream = format({ headers: true });
daily_todo_history_stream.pipe(fs.createWriteStream(`${PAST_ACTIVITY_DATA_BASE_PATH}/daily_todo_history.csv`));

const daily_todo_certification_stream = format({ headers: true });
daily_todo_certification_stream.pipe(fs.createWriteStream(`${PAST_ACTIVITY_DATA_BASE_PATH}/daily_todo_certification.csv`));

const daily_todo_certification_reviewer_stream = format({ headers: true });
daily_todo_certification_reviewer_stream.pipe(fs.createWriteStream(`${PAST_ACTIVITY_DATA_BASE_PATH}/daily_todo_certification_reviewer.csv`));

// =========== 캐싱 ===========
const groupIdsByMember = Array.from({ length: MEMBER_COUNT }, () => []);
const todoIdsByMember = Array.from({ length: MEMBER_COUNT }, () => []);

// =========== 메인 로직 ===========
let memberId = 1;  // member & notification_token & daily_todo_stats
let challengeGroupId = 1;
let challengeGroupMemberId = 1;
let dailyTodoId = 1;  // daily_todo & daily_todo_history
let dailyTodoCertificationId = 1;

async function createTestData() {
    console.log(`👷[Const Past Activity Data] 과거 활동 테스트 데이터 생성중... (총 과거 진행일 수 : ${PAST_TOTAL_ACTIVITY_CYCLE * PAST_GROUP_RUNNING_DAY}일)\n`);

    await createMemberInfoData();
    await createChallengeAndTodoData();

    console.log(`🎉 테스트 데이터 생성 모두 완료!`);
}

// member & notification_token & daily_todo_stats 데이터 생성
async function createMemberInfoData() {
    console.log("🧑‍🍳 member & notification_token & daily_todo_stats 테스트 데이터 생성중...");

    let memberCreatedAt = PAST_GROUP_ACTIVITY_START_AT;
    memberCreatedAt = memberCreatedAt.substring(0, 10) + " 06:00:00";

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
        waitForStreamFinish(daily_todo_stats_stream),
    ]);

    console.log("✅ member & notification_token & daily_todo_stats 테스트 데이터 생성 완료!\n");
}

// challenge_group & challenge_group_member & daily_todo & daily_todo_history & daily_todo_certification & daily_todo_certification_reviewer 데이터 생성
async function createChallengeAndTodoData() {
    console.log("🧑‍🍳 챌린지 그룹 & 투두 & 투두 인증 관련 테스트 데이터 생성중...");

    for (let cycle = 0; cycle < PAST_TOTAL_ACTIVITY_CYCLE; cycle++) {
        let groupStartAtInCycle = calculateNextDate(PAST_GROUP_ACTIVITY_START_AT, cycle, PAST_GROUP_RUNNING_DAY);
        groupStartAtInCycle = groupStartAtInCycle.substring(0, 10) + " 07:00:00";
        const groupStartAtInCycleOnlyDate = toDateOnly(groupStartAtInCycle);

        const groupEndAtInCycle = toDateOnly(calculateEndAt(groupStartAtInCycle, PAST_GROUP_RUNNING_DAY + 1));
        const firstGroupIdInCycle = 1 + cycle * PAST_ONE_CYCLE_PER_GROUP_COUNT;

        // 2. challenge_group & challenge_group_member 데이터 생성
        for (let i = 0; i < PAST_ONE_CYCLE_PER_GROUP_COUNT; i++) {
            const currentChallengeGroupId = challengeGroupId++;
            challenge_group_stream.write({
                id: currentChallengeGroupId,
                name: `g-${currentChallengeGroupId}`,
                maximum_member_count: MEMBER_PER_GROUP_COUNT,
                join_code: `jc-${currentChallengeGroupId}`,
                status: "FINISHED",
                start_at: groupStartAtInCycleOnlyDate,
                end_at: groupEndAtInCycle,
                created_at: groupStartAtInCycle,
                row_inserted_at: groupStartAtInCycle,
                row_updated_at: null
            });
        }

        let groupId = firstGroupIdInCycle;
        for (let i = 0; i < GROUP_PER_MEMBER_COUNT; i++) {
            for (let j = 0; j < MEMBER_COUNT / MEMBER_PER_GROUP_COUNT; j++) {
                let memberId = 1 + j * MEMBER_PER_GROUP_COUNT;
                for (let k = 0; k < MEMBER_PER_GROUP_COUNT; k++) {
                    let currentMemberId = memberId++;
                    challenge_group_member_stream.write({
                        id: challengeGroupMemberId++,
                        challenge_group_id: groupId,
                        member_id: currentMemberId,
                        created_at: groupStartAtInCycle,
                        row_inserted_at: groupStartAtInCycle,
                        row_updated_at: null
                    });
                    groupIdsByMember[currentMemberId - 1].push(groupId);
                }
                groupId++;
            }
        }

        for (let day = 0; day < PAST_GROUP_RUNNING_DAY; day++) {
            let currentTodoWrittenAt = new Date(groupStartAtInCycle);
            currentTodoWrittenAt.setDate(currentTodoWrittenAt.getDate() + day);
            currentTodoWrittenAt.setHours(8, 0, 0, 0);
            currentTodoWrittenAt = convertDateTimeFormatString(currentTodoWrittenAt);

            let currentTodoCertifyAt = new Date(currentTodoWrittenAt);
            currentTodoCertifyAt.setHours(17, 0, 0, 0);
            currentTodoCertifyAt = convertDateTimeFormatString(currentTodoCertifyAt);

            for (let memberId = 1; memberId <= MEMBER_COUNT; memberId++) {
                const reviewerId = getReviewerId(memberId);
                for (let i = 0; i < GROUP_PER_MEMBER_COUNT; i++) {
                    let reviewStatusToggle = true;
                    for (let j = 0; j < DAY_TODO_PER_MEMBER_COUNT; j++) {
                        // 3. daily_todo & daily_todo_history 데이터 생성
                        const currentTodoId = dailyTodoId++;
                        daily_todo_stream.write({
                            id: currentTodoId,
                            challenge_group_id: groupIdsByMember[memberId - 1][i],
                            writer_id: memberId,
                            content: `td=${currentTodoId}`,
                            status: "CERTIFY_COMPLETED",
                            written_at: currentTodoWrittenAt,
                            row_inserted_at: currentTodoWrittenAt,
                            row_updated_at: null
                        });

                        daily_todo_history_stream.write({
                            id: currentTodoId,
                            daily_todo_id: currentTodoId,
                            event_time: currentTodoWrittenAt,
                            row_inserted_at: currentTodoWrittenAt,
                            row_updated_at: null
                        });
                        todoIdsByMember[memberId - 1].push(currentTodoId);

                        // 4. daily_todo_certification & daily_todo_certification_reviewer 데이터 생성
                        // 현재는 투두 개수 == 인증 개수로 고정했지만 인증 개수만 다르게 하고 싶다면 아래 로직을 별도 루프로 분리해야함.
                        const currentTodoCertificationId = dailyTodoCertificationId++;
                        const reviewStatus = reviewStatusToggle ? "APPROVE" : "REJECT";
                        const reviewFeedBack = reviewStatusToggle ? `와 미쳤다 ㄷㄷ - ${currentTodoCertificationId}` : `그게 최선인가? ㅎ - ${currentTodoCertificationId}`;
                        reviewStatusToggle = !reviewStatusToggle;

                        daily_todo_certification_stream.write({
                            id: currentTodoCertificationId,
                            daily_todo_id: currentTodoId,
                            content: `tc-${currentTodoId}`,
                            media_url: `http://certification-media.site/m${memberId}/t${currentTodoId}`,
                            review_status: reviewStatus,
                            review_feedback: reviewFeedBack,
                            created_at: currentTodoCertifyAt,
                            row_inserted_at: currentTodoCertifyAt,
                            row_updated_at: null
                        });

                        daily_todo_certification_reviewer_stream.write({
                            id: currentTodoCertificationId,
                            daily_todo_certification_id: currentTodoCertificationId,
                            reviewer_id: reviewerId,
                            row_inserted_at: currentTodoCertifyAt,
                            row_updated_at: null
                        });
                    }
                }
            }
        }
    }

    challenge_group_stream.end();
    challenge_group_member_stream.end();
    daily_todo_stream.end();
    daily_todo_history_stream.end();
    daily_todo_certification_stream.end();
    daily_todo_certification_reviewer_stream.end();

    await Promise.all([
        waitForStreamFinish(challenge_group_stream),
        waitForStreamFinish(challenge_group_member_stream),
        waitForStreamFinish(daily_todo_stream),
        waitForStreamFinish(daily_todo_history_stream),
        waitForStreamFinish(daily_todo_certification_stream),
        waitForStreamFinish(daily_todo_certification_reviewer_stream),
    ]);

    console.log("✅ 챌린지 그룹 & 투두 & 투두 인증 관련 테스트 데이터 생성 완료!\n");
}

function getReviewerId(memberId) {
    // 그룹의 시작과 끝 ID를 구함
    const groupIndex = Math.floor((memberId - 1) / MEMBER_PER_GROUP_COUNT);
    const startId = groupIndex * MEMBER_PER_GROUP_COUNT + 1;         // 예: 1, 21, 41, ...

    // 그룹 내 offset (0~19)
    const offset = memberId - startId;

    // 매칭 규칙: 0 <-> 19, 1 <-> 18, ...
    const reviewerOffset = MEMBER_PER_GROUP_COUNT - 1 - offset;

    return startId + reviewerOffset;
}

createTestData().then(() => {});
