import fs from 'fs';
import {format} from "fast-csv";
import {
    CSV_SAVED_BASE_PATH,
    DAY_TODO_PER_MEMBER_COUNT,
    getReviewerId,
    MEMBER_COUNT,
    MEMBER_PER_GROUP_COUNT,
    PAST_GROUP_ACTIVITY_START_AT,
    PAST_GROUP_PER_MEMBER_COUNT,
    PAST_GROUP_RUNNING_DAY,
    PAST_ONE_CYCLE_PER_GROUP_COUNT,
    PAST_TOTAL_ACTIVITY_CYCLE,
} from "../test-data-common.js";
import {
    getDateNDaysLater,
    getGroupStartAtInCycle,
    convertDateObjectToMySqlDateFormat,
    convertDateObjectToMySqlDatetimeFormat
} from "../../util/time-util.js";

// =========== CSV Stream ===========
const challenge_group_stream = format({ headers: true });
challenge_group_stream.pipe(fs.createWriteStream(`${CSV_SAVED_BASE_PATH}/past_challenge_group.csv`));

const challenge_group_member_stream = format({ headers: true });
challenge_group_member_stream.pipe(fs.createWriteStream(`${CSV_SAVED_BASE_PATH}/past_challenge_group_member.csv`));

const daily_todo_stream = format({ headers: true });
daily_todo_stream.pipe(fs.createWriteStream(`${CSV_SAVED_BASE_PATH}/past_daily_todo.csv`));

const daily_todo_history_stream = format({ headers: true });
daily_todo_history_stream.pipe(fs.createWriteStream(`${CSV_SAVED_BASE_PATH}/past_daily_todo_history.csv`));

const daily_todo_certification_stream = format({ headers: true });
daily_todo_certification_stream.pipe(fs.createWriteStream(`${CSV_SAVED_BASE_PATH}/past_daily_todo_certification.csv`));

const daily_todo_certification_reviewer_stream = format({ headers: true });
daily_todo_certification_reviewer_stream.pipe(fs.createWriteStream(`${CSV_SAVED_BASE_PATH}/past_daily_todo_certification_reviewer.csv`));

// =========== 캐싱 ===========
const groupIdsByMember = Array.from({ length: MEMBER_COUNT }, () => []);
const todoIdsByMember = Array.from({ length: MEMBER_COUNT }, () => []);

// =========== 메인 로직 ===========
let challengeGroupId = 1;
let challengeGroupMemberId = 1;
let dailyTodoId = 1;  // daily_todo & daily_todo_history
let dailyTodoCertificationId = 1;

async function createPastActivityTestData() {
    console.log(`🧑‍🍳 [Const Past Activity Data] 과거 활동 테스트 데이터 생성중... (총 과거 진행일 수 : ${PAST_TOTAL_ACTIVITY_CYCLE * PAST_GROUP_RUNNING_DAY}일)`);
    await generateData();
    console.log(`✅ 과거 활동 테스트 데이터 생성 완료!`);
}

async function generateData() {
    for (let cycle = 0; cycle < PAST_TOTAL_ACTIVITY_CYCLE; cycle++) {
        const groupStartAtInCycle = getGroupStartAtInCycle(PAST_GROUP_ACTIVITY_START_AT, cycle, PAST_GROUP_RUNNING_DAY);
        groupStartAtInCycle.setHours(7, 0, 0, 0);
        const groupEndAtInCycle = getDateNDaysLater(groupStartAtInCycle, PAST_GROUP_RUNNING_DAY);

        // 1. challenge_group & challenge_group_member 데이터 생성
        const groupStartAtInCycleMySqlDateTimeString = convertDateObjectToMySqlDatetimeFormat(groupStartAtInCycle);
        const groupStartAtInCycleMySqlDateString = convertDateObjectToMySqlDateFormat(groupStartAtInCycle);
        const groupEndAtInCycleMySqlDateString = convertDateObjectToMySqlDateFormat(groupEndAtInCycle);
        for (let i = 0; i < PAST_ONE_CYCLE_PER_GROUP_COUNT; i++) {
            const currentChallengeGroupId = challengeGroupId++;
            challenge_group_stream.write({
                id: currentChallengeGroupId,
                name: `g-${currentChallengeGroupId}`,
                maximum_member_count: MEMBER_PER_GROUP_COUNT,
                join_code: `jc-${currentChallengeGroupId}`,
                status: "FINISHED",
                start_at: groupStartAtInCycleMySqlDateString,
                end_at: groupEndAtInCycleMySqlDateString,
                created_at: groupStartAtInCycleMySqlDateTimeString,
                row_inserted_at: groupStartAtInCycleMySqlDateTimeString,
                row_updated_at: null
            });
        }

        let groupId = 1 + cycle * PAST_ONE_CYCLE_PER_GROUP_COUNT;
        for (let i = 0; i < PAST_GROUP_PER_MEMBER_COUNT; i++) {
            for (let j = 0; j < MEMBER_COUNT / MEMBER_PER_GROUP_COUNT; j++) {
                let memberId = 1 + j * MEMBER_PER_GROUP_COUNT;
                for (let k = 0; k < MEMBER_PER_GROUP_COUNT; k++) {
                    let currentMemberId = memberId++;
                    challenge_group_member_stream.write({
                        id: challengeGroupMemberId++,
                        challenge_group_id: groupId,
                        member_id: currentMemberId,
                        created_at: groupStartAtInCycleMySqlDateTimeString,
                        row_inserted_at: groupStartAtInCycleMySqlDateTimeString,
                        row_updated_at: null
                    });
                    groupIdsByMember[currentMemberId - 1].push(groupId);
                }
                groupId++;
            }
        }

        for (let day = 0; day < PAST_GROUP_RUNNING_DAY; day++) {
            const currentTodoWrittenAt = getDateNDaysLater(groupStartAtInCycle, day);
            currentTodoWrittenAt.setHours(8, 0, 0, 0);
            const currentTodoCertifyAt = currentTodoWrittenAt;
            currentTodoCertifyAt.setHours(17, 0, 0, 0);

            const currentTodoWrittenAtMySqlDateTimeString = convertDateObjectToMySqlDatetimeFormat(currentTodoWrittenAt);
            for (let memberId = 1; memberId <= MEMBER_COUNT; memberId++) {
                const reviewerId = getReviewerId(memberId);
                for (let i = 0; i < PAST_GROUP_PER_MEMBER_COUNT; i++) {
                    let reviewStatusToggle = true;
                    for (let j = 0; j < DAY_TODO_PER_MEMBER_COUNT; j++) {
                        // 2. daily_todo & daily_todo_history 데이터 생성
                        const currentTodoId = dailyTodoId++;
                        daily_todo_stream.write({
                            id: currentTodoId,
                            challenge_group_id: groupIdsByMember[memberId - 1][i],
                            writer_id: memberId,
                            content: `td=${currentTodoId}`,
                            status: "CERTIFY_COMPLETED",
                            written_at: currentTodoWrittenAtMySqlDateTimeString,
                            row_inserted_at: currentTodoWrittenAtMySqlDateTimeString,
                            row_updated_at: null
                        });

                        daily_todo_history_stream.write({
                            id: currentTodoId,
                            daily_todo_id: currentTodoId,
                            event_time: currentTodoWrittenAtMySqlDateTimeString,
                            row_inserted_at: currentTodoWrittenAtMySqlDateTimeString,
                            row_updated_at: null
                        });
                        todoIdsByMember[memberId - 1].push(currentTodoId);

                        // 3. daily_todo_certification & daily_todo_certification_reviewer 데이터 생성
                        // 현재는 투두 개수 == 인증 개수로 고정했지만 인증 개수만 다르게 하고 싶다면 아래 로직을 별도 루프로 분리해야함.
                        const currentTodoCertificationId = dailyTodoCertificationId++;
                        const reviewStatus = reviewStatusToggle ? "APPROVE" : "REJECT";
                        const reviewFeedBack = reviewStatusToggle ? `와 미쳤다 ㄷㄷ - ${currentTodoCertificationId}` : `그게 최선인가? ㅎ - ${currentTodoCertificationId}`;
                        reviewStatusToggle = !reviewStatusToggle;
                        const currentTodoCertifyAtMySqlDateTimeString = convertDateObjectToMySqlDatetimeFormat(currentTodoCertifyAt);

                        daily_todo_certification_stream.write({
                            id: currentTodoCertificationId,
                            daily_todo_id: currentTodoId,
                            content: `tc-${currentTodoId}`,
                            media_url: `http://certification-media.site/m${memberId}/t${currentTodoId}`,
                            review_status: reviewStatus,
                            review_feedback: reviewFeedBack,
                            created_at: currentTodoCertifyAtMySqlDateTimeString,
                            row_inserted_at: currentTodoCertifyAtMySqlDateTimeString,
                            row_updated_at: null
                        });

                        daily_todo_certification_reviewer_stream.write({
                            id: currentTodoCertificationId,
                            daily_todo_certification_id: currentTodoCertificationId,
                            reviewer_id: reviewerId,
                            row_inserted_at: currentTodoCertifyAtMySqlDateTimeString,
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

createPastActivityTestData().then(() => {});
