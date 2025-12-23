import fs from 'fs';
import {format} from "fast-csv";
import {
    CSV_SAVED_BASE_PATH,
    CURRENT_FOR_WRITE_GROUP_PER_MEMBER_COUNT,
    CURRENT_GROUP_ACTIVITY_START_AT,
    CURRENT_GROUP_RUNNING_DAY,
    DAY_TODO_PER_MEMBER_COUNT,
    getLastIdsOfPastActivityData,
    getReviewerId,
    MEMBER_COUNT,
    MEMBER_PER_GROUP_COUNT
} from "../../test-data-common.js";
import {
    convertDateObjectToMySqlDateFormat,
    convertDateObjectToMySqlDatetimeFormat,
    getDateNDaysLater,
    getTodayDate
} from "../../../util/time-util.js";

// =========== CSV Stream ===========
const challenge_group_stream = format({ headers: true });
challenge_group_stream.pipe(fs.createWriteStream(`${CSV_SAVED_BASE_PATH}/1_for_write_current_challenge_group.csv`));

const last_selected_challenge_group_record_stream = format({ headers: true });
last_selected_challenge_group_record_stream.pipe(fs.createWriteStream(`${CSV_SAVED_BASE_PATH}/2_for_write_current_last_selected_challenge_group_record.csv`));

const challenge_group_member_stream = format({ headers: true });
challenge_group_member_stream.pipe(fs.createWriteStream(`${CSV_SAVED_BASE_PATH}/3_for_write_current_challenge_group_member.csv`));

const daily_todo_stream = format({ headers: true });
daily_todo_stream.pipe(fs.createWriteStream(`${CSV_SAVED_BASE_PATH}/4_for_write_current_daily_todo.csv`));

const daily_todo_history_stream = format({ headers: true });
daily_todo_history_stream.pipe(fs.createWriteStream(`${CSV_SAVED_BASE_PATH}/5_for_write_current_daily_todo_history.csv`));

const daily_todo_history_read_stream = format({ headers: true });
daily_todo_history_read_stream.pipe(fs.createWriteStream(`${CSV_SAVED_BASE_PATH}/6_for_write_current_daily_todo_history_read.csv`));

const daily_todo_certification_stream = format({ headers: true });
daily_todo_certification_stream.pipe(fs.createWriteStream(`${CSV_SAVED_BASE_PATH}/7_for_write_current_daily_todo_certification.csv`));

const daily_todo_certification_reviewer_stream = format({ headers: true });
daily_todo_certification_reviewer_stream.pipe(fs.createWriteStream(`${CSV_SAVED_BASE_PATH}/8_for_write_current_daily_todo_certification_reviewer.csv`));

// =========== 캐싱 ===========
const groupMembersByGroup = {};
const groupIdsByMember = Array.from({ length: MEMBER_COUNT }, () => []);
const todoIdsByMember = Array.from({ length: MEMBER_COUNT }, () => []);

// =========== 메인 로직 ===========
const lastIds = getLastIdsOfPastActivityData();
let challengeGroupId = lastIds.lastChallengeGroupId + 1;
let challengeGroupMemberId = lastIds.lastChallengeGroupMemberId + 1;
let dailyTodoId = lastIds.lastDailyTodoId + 1;  // daily_todo & daily_todo_history
let dailyTodoHistoryReadId = 1;
let dailyTodoCertificationId = lastIds.lastDailyTodoCertificationId + 1;

async function createCurrentActivityForWriteTestData() {
    console.log(`🧑‍🍳 [Const Current Activity Data For Write (CSV)] 현재 활동 테스트 데이터 생성중...`);
    await generateTestData();
    console.log(`✅ 현재 활동 테스트 데이터 생성 완료!\n`);
}

async function generateTestData() {
    const todayDate = convertDateObjectToMySqlDatetimeFormat(getTodayDate());

    // 1. challenge_group & challenge_group_member 데이터 생성
    const groupStartAt = CURRENT_GROUP_ACTIVITY_START_AT;
    groupStartAt.setHours(7, 0, 0, 0);
    const groupEndAt = getDateNDaysLater(groupStartAt, CURRENT_GROUP_RUNNING_DAY);
    let joiningGroupId = challengeGroupId;
    const totalChallengeGroupCount = MEMBER_COUNT / MEMBER_PER_GROUP_COUNT * CURRENT_FOR_WRITE_GROUP_PER_MEMBER_COUNT;

    const groupStartAtInCycleMySqlDateTimeString = convertDateObjectToMySqlDatetimeFormat(groupStartAt);
    const groupStartAtInCycleMySqlDateString = convertDateObjectToMySqlDateFormat(groupStartAt);
    const groupEndAtInCycleMySqlDateString = convertDateObjectToMySqlDateFormat(groupEndAt);
    // a. 그룹 참여 테스트에 사용할 그룹을 포함해 생성
    for (let i = 0; i < totalChallengeGroupCount + (MEMBER_COUNT / MEMBER_PER_GROUP_COUNT); i++) {
        const currentChallengeGroupId = challengeGroupId++;
        challenge_group_stream.write({
            id: currentChallengeGroupId,
            name: `g-${currentChallengeGroupId}`,
            maximum_member_count: MEMBER_PER_GROUP_COUNT,
            join_code: `jc-${currentChallengeGroupId}`,
            status: "RUNNING",
            start_at: groupStartAtInCycleMySqlDateString,
            end_at: groupEndAtInCycleMySqlDateString,
            created_at: groupStartAtInCycleMySqlDateTimeString,
            row_inserted_at: todayDate,
            row_updated_at: null
        });
    }

    for (let i = 0; i < CURRENT_FOR_WRITE_GROUP_PER_MEMBER_COUNT; i++) {
        for (let j = 0; j < MEMBER_COUNT / MEMBER_PER_GROUP_COUNT; j++) {
            let memberId = 1 + j * MEMBER_PER_GROUP_COUNT;
            for (let k = 0; k < MEMBER_PER_GROUP_COUNT; k++) {
                let currentMemberId = memberId++;
                challenge_group_member_stream.write({
                    id: challengeGroupMemberId++,
                    challenge_group_id: joiningGroupId,
                    member_id: currentMemberId,
                    created_at: groupStartAtInCycleMySqlDateTimeString,
                    row_inserted_at: todayDate,
                    row_updated_at: null
                });

                // 그룹별 멤버 캐싱
                if (!groupMembersByGroup[joiningGroupId]) {
                    groupMembersByGroup[joiningGroupId] = [];
                }
                groupMembersByGroup[joiningGroupId].push(currentMemberId);
                groupIdsByMember[currentMemberId - 1].push(joiningGroupId);

                // 마지막 그룹 정보를 last_selected_challenge_group_record에 저장
                if (i === CURRENT_FOR_WRITE_GROUP_PER_MEMBER_COUNT - 1) {
                    last_selected_challenge_group_record_stream.write({
                        id: currentMemberId,
                        challenge_group_id: joiningGroupId,
                        member_id: currentMemberId,
                        row_inserted_at: todayDate,
                        row_updated_at: null
                    });
                }
            }
            joiningGroupId++;
        }
    }

    for (let day = 0; day < CURRENT_GROUP_RUNNING_DAY; day++) {
        const currentTodoWrittenAt = getDateNDaysLater(groupStartAt, day);
        currentTodoWrittenAt.setHours(8, 0, 0, 0);

        const currentTodoCertifyAt = currentTodoWrittenAt;
        currentTodoCertifyAt.setHours(17, 0, 0, 0);

        const currentTodoWrittenAtMySqlDateTimeString = convertDateObjectToMySqlDatetimeFormat(currentTodoWrittenAt);
        const currentTodoCertifyAtMySqlDateTimeString = convertDateObjectToMySqlDatetimeFormat(currentTodoCertifyAt);
        for (let memberId = 1; memberId <= MEMBER_COUNT; memberId++) {
            const reviewerId = getReviewerId(memberId);
            for (let i = 0; i < CURRENT_FOR_WRITE_GROUP_PER_MEMBER_COUNT; i++) {
                // b. 투두 작성 테스트를 위한 처리
                if (day === CURRENT_GROUP_RUNNING_DAY - 1 && i === 0) {
                    continue;
                }

                let reviewStatusToggle = true;
                for (let j = 0; j < DAY_TODO_PER_MEMBER_COUNT; j++) {
                    // 2. daily_todo & daily_todo_history & daily_todo_history_read 데이터 생성
                    const currentTodoId = dailyTodoId++;

                    // c. 투두 인증 테스트를 위한 처리
                    if (day === CURRENT_GROUP_RUNNING_DAY - 1 && i === 1) {
                        daily_todo_stream.write({
                            id: currentTodoId,
                            challenge_group_id: groupIdsByMember[memberId - 1][i],
                            writer_id: memberId,
                            content: `td=${currentTodoId}`,
                            status: "CERTIFY_PENDING",
                            written_at: currentTodoWrittenAtMySqlDateTimeString,
                            row_inserted_at: todayDate,
                            row_updated_at: null
                        });
                        daily_todo_history_stream.write({
                            id: currentTodoId,
                            daily_todo_id: currentTodoId,
                            event_time: currentTodoWrittenAtMySqlDateTimeString,
                            row_inserted_at: todayDate,
                            row_updated_at: null
                        });

                        todoIdsByMember[memberId - 1].push(currentTodoId);
                        continue;
                    }

                    daily_todo_stream.write({
                        id: currentTodoId,
                        challenge_group_id: groupIdsByMember[memberId - 1][i],
                        writer_id: memberId,
                        content: `td=${currentTodoId}`,
                        status: "CERTIFY_COMPLETED",
                        written_at: currentTodoWrittenAtMySqlDateTimeString,
                        row_inserted_at: todayDate,
                        row_updated_at: null
                    });
                    daily_todo_history_stream.write({
                        id: currentTodoId,
                        daily_todo_id: currentTodoId,
                        event_time: currentTodoWrittenAtMySqlDateTimeString,
                        row_inserted_at: todayDate,
                        row_updated_at: null
                    });

                    if (day === CURRENT_GROUP_RUNNING_DAY - 1) {
                        const membersInGroup = groupMembersByGroup[groupIdsByMember[memberId - 1][i]];
                        for (const groupMemberId of membersInGroup) {
                            daily_todo_history_read_stream.write({
                                id: dailyTodoHistoryReadId++,
                                member_id: groupMemberId,
                                daily_todo_history_id: currentTodoId,
                                ow_inserted_at: todayDate,
                                row_updated_at: null
                            });
                        }
                    }

                    todoIdsByMember[memberId - 1].push(currentTodoId);

                    // 3. daily_todo_certification & daily_todo_certification_reviewer 데이터 생성
                    // 현재는 투두 개수 == 인증 개수로 고정했지만 인증 개수만 다르게 하고 싶다면 아래 로직을 별도 루프로 분리해야함.
                    const currentTodoCertificationId = dailyTodoCertificationId++;
                    const reviewStatus = reviewStatusToggle ? "APPROVE" : "REJECT";
                    const reviewFeedBack = reviewStatusToggle ? `와 미쳤다 ㄷㄷ - ${currentTodoCertificationId}` : `그게 최선인가? ㅎ - ${currentTodoCertificationId}`;
                    reviewStatusToggle = !reviewStatusToggle;

                    // d. 투두 인증 검사 테스트를 위한 처리
                    if (day === CURRENT_GROUP_RUNNING_DAY - 1 && i === 2) {
                        daily_todo_certification_stream.write({
                            id: currentTodoCertificationId,
                            daily_todo_id: currentTodoId,
                            content: `tc-${currentTodoId}`,
                            media_url: `http://certification-media.site/m${memberId}/t${currentTodoId}`,
                            review_status: 'REVIEW_PENDING',
                            review_feedback: null,
                            created_at: currentTodoCertifyAtMySqlDateTimeString,
                            row_inserted_at: todayDate,
                            row_updated_at: null
                        });
                        daily_todo_certification_reviewer_stream.write({
                            id: currentTodoCertificationId,
                            daily_todo_certification_id: currentTodoCertificationId,
                            reviewer_id: reviewerId,
                            row_inserted_at: todayDate,
                            row_updated_at: null
                        });
                        continue;
                    }

                    daily_todo_certification_stream.write({
                        id: currentTodoCertificationId,
                        daily_todo_id: currentTodoId,
                        content: `tc-${currentTodoId}`,
                        media_url: `http://certification-media.site/m${memberId}/t${currentTodoId}`,
                        review_status: reviewStatus,
                        review_feedback: reviewFeedBack,
                        created_at: currentTodoCertifyAtMySqlDateTimeString,
                        row_inserted_at: todayDate,
                        row_updated_at: null
                    });
                    daily_todo_certification_reviewer_stream.write({
                        id: currentTodoCertificationId,
                        daily_todo_certification_id: currentTodoCertificationId,
                        reviewer_id: reviewerId,
                        row_inserted_at: todayDate,
                        row_updated_at: null
                    });
                }
            }
        }
    }

    challenge_group_stream.end();
    last_selected_challenge_group_record_stream.end();
    challenge_group_member_stream.end();
    daily_todo_stream.end();
    daily_todo_history_stream.end();
    daily_todo_history_read_stream.end();
    daily_todo_certification_stream.end();
    daily_todo_certification_reviewer_stream.end();

    await Promise.all([
        waitForStreamFinish(challenge_group_stream),
        waitForStreamFinish(last_selected_challenge_group_record_stream),
        waitForStreamFinish(challenge_group_member_stream),
        waitForStreamFinish(daily_todo_stream),
        waitForStreamFinish(daily_todo_history_stream),
        waitForStreamFinish(daily_todo_history_read_stream),
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

createCurrentActivityForWriteTestData().then(() => {});
