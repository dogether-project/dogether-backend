/**
 * [ 시나리오 02. 앱 실행 + 랭킹 페이지/투두 히스토리 4회 조회 ]
 * 읽기 API만 구성
 */

import { sleep } from 'k6';
import {check} from 'k6';
import { SharedArray } from 'k6/data';
import http from 'k6/http';
import {parseResponseBody, setRequestHeader} from "../../common/util/api-util.js";
import {API_BASE_URL} from "../../common/secret/secret.js";
import {getDateNDaysAgoMySqlDateFormatString} from "../../common/util/time-util.js";
import {
    getChallengeGroupIdsPerMember,
    getChallengeGroupMembersPerMember
} from "../../common/test-data/test-data-common.js";

const tokens = new SharedArray('tokens', () => JSON.parse(open('../../common/secret/tokens.json')));

export const options = {
    setupTimeout: '30m',
    scenarios: {
        scenario_02_test: {
            executor: 'ramping-vus',
            stages: [
                { duration: '2m', target: 200 }, // 2분 동안 0명에서 200명까지 점진적 증가
                { duration: '5m', target: 200 }, // 200명 유지 (여기서 병목 확인)
                { duration: '1m', target: 0 },   // 테스트 종료
            ],
        },
    },
    thresholds: {
        iteration_duration: ['p(95)<3000'], // sleep()에 사용된 1s를 더한 값
        'http_req_duration{name:00_force_update}': ['p(95)<300'],
        'http_req_duration{name:01_check-challenge-group-participation-required}': ['p(95)<300'],
        'http_req_duration{name:02_get-todo-certifications-for-reviewer}': ['p(95)<300'],
        'http_req_duration{name:03_get-joining-challenge-groups-info}': ['p(95)<300'],
        'http_req_duration{name:04_get-my-todos-in-group}': ['p(95)<300'],

        http_req_failed: ['rate<0.01'],
    },
};

export function setup() {
    const challengeGroupIds = getChallengeGroupIdsPerMember();
    const otherChallengeGroupMemberIds = getChallengeGroupMembersPerMember();

    console.log("⏰ 5초 대기 시작.");
    sleep(5);
    console.log("✅ 5초 대기 완료.");

    return {challengeGroupIds, otherChallengeGroupMemberIds};
}

export default function (data) {
    const vuIndex = __VU - 1;

    // 1. 앱 실행
    requestForceUpdateCheckApi(vuIndex);
    requestCheckChallengeGroupParticipationRequiredApi(vuIndex);
    requestGetTodoCertificationsForReviewerApi(vuIndex);
    requestGetJoiningChallengeGroupsInfoApi(vuIndex);
    requestGetMyTodosInGroupApi(vuIndex, data.challengeGroupIds[vuIndex][0]);

    // 2. 랭킹 페이지 진입 -> 다른 사람의 투두 히스토리 조회 반복
    for (let cnt = 0; cnt < 4; cnt++) {
        sleep(1);

        requestGetJoiningChallengeGroupTeamRankingApi(vuIndex, data.challengeGroupIds[vuIndex][0]);
        sleep(1);

        requestGetMemberTodayTodoHistoriesApi(vuIndex, data.challengeGroupIds[vuIndex][0], data.otherChallengeGroupMemberIds[vuIndex][0]);
        sleep(3);
    }

    sleep(1);
}

function requestForceUpdateCheckApi(vuIndex) {
    const timeout = '1800s';
    const headers = setRequestHeader(tokens[vuIndex]);
    const endpoint = `${API_BASE_URL}/api/v1/app-info/force-update-check?app-version=1.1.2`;

    if (__VU === 1 && __ITER === 0) {
        console.log(`앱 강제 업데이트 필요 여부 조회 API 요청 엔드포인트 : ${endpoint}`);
    }

    const response = http.get(endpoint, { headers, timeout , tags: { name: '00_force_update' }});
    const responseBody = parseResponseBody(response);
    const responseData = responseBody.data;

    check(null, {
        'API HTTP 상태 코드 200': () => response?.status === 200,
        'API 응답 코드 success': () => responseBody?.code === 'success',
        '응답 데이터 - forceUpdateRequired 존재': () => responseData?.forceUpdateRequired !== undefined
    });

    return responseData;
}

function requestCheckChallengeGroupParticipationRequiredApi(vuIndex) {
    const timeout = '1800s';
    const headers = setRequestHeader(tokens[vuIndex]);
    const endpoint = `${API_BASE_URL}/api/v1/groups/participating`;

    if (__VU === 1 && __ITER === 0) {
        console.log(`챌린지 그룹 참여 필요 여부 조회 API 요청 엔드포인트 : ${endpoint}`);
    }

    const response = http.get(endpoint, { headers, timeout, tags: { name: '01_check-challenge-group-participation-required' } });
    const responseBody = parseResponseBody(response);
    const responseData = responseBody.data;

    check(null, {
        'API HTTP 상태 코드 200': () => response?.status === 200,
        'API 응답 코드 success': () => responseBody?.code === 'success',
        '응답 데이터 - checkParticipating 존재': () => responseData?.checkParticipating !== undefined
    });

    return responseData;
}

function requestGetTodoCertificationsForReviewerApi(vuIndex) {
    const timeout = '1800s';
    const headers = setRequestHeader(tokens[vuIndex]);
    const endpoint = `${API_BASE_URL}/api/v1/todo-certifications/pending-review`;

    if (__VU === 1 && __ITER === 0) {
        console.log(`본인이 검사해 줘야 하는 투두 수행 인증 전체 조회 API 요청 엔드포인트 : ${endpoint}`);
    }

    const response = http.get(endpoint, { headers, timeout, tags: { name: '02_get-todo-certifications-for-reviewer' } });
    const responseBody = parseResponseBody(response);
    const responseData = responseBody.data;

    check(null, {
        'API HTTP 상태 코드 200': () => response?.status === 200,
        'API 응답 코드 success': () => responseBody?.code === 'success',
        '응답 데이터 - dailyTodoCertifications 빈 배열 X': () => responseData?.dailyTodoCertifications.length > 0,
        '응답 데이터 - dailyTodoCertifications[0].id 존재': () => responseData?.dailyTodoCertifications[0].id !== undefined,
        '응답 데이터 - dailyTodoCertifications[0].content 존재': () => responseData?.dailyTodoCertifications[0].content !== undefined,
        '응답 데이터 - dailyTodoCertifications[0].mediaUrl 존재': () => responseData?.dailyTodoCertifications[0].mediaUrl !== undefined,
        '응답 데이터 - dailyTodoCertifications[0].todoContent 존재': () => responseData?.dailyTodoCertifications[0].todoContent !== undefined,
        '응답 데이터 - dailyTodoCertifications[0].doer 존재': () => responseData?.dailyTodoCertifications[0].doer !== undefined,
    });

    return responseData;
}

function requestGetJoiningChallengeGroupsInfoApi(vuIndex) {
    const timeout = '1800s';
    const headers = setRequestHeader(tokens[vuIndex]);
    const endpoint = `${API_BASE_URL}/api/v1/groups/my`;

    if (__VU === 1 && __ITER === 0) {
        console.log(`참여중인 챌린지 그룹 정보 전체 조회 API 요청 엔드포인트 : ${endpoint}`);
    }

    const response = http.get(endpoint, { headers, timeout, tags: { name: '03_get-joining-challenge-groups-info' } });
    const responseBody = parseResponseBody(response);
    const responseData = responseBody.data;

    check(null, {
        'API HTTP 상태 코드 200': () => response?.status === 200,
        'API 응답 코드 success': () => responseBody?.code === 'success',
        '응답 데이터 - lastSelectedGroupIndex 존재': () => responseData?.lastSelectedGroupIndex !== undefined,
        '응답 데이터 - joiningChallengeGroups 빈 배열 X': () => responseData?.joiningChallengeGroups.length > 0,
        '응답 데이터 - joiningChallengeGroups[0].groupId 존재': () => responseData.joiningChallengeGroups[0]?.groupId !== undefined,
        '응답 데이터 - joiningChallengeGroups[0].groupName 존재': () => responseData.joiningChallengeGroups[0]?.groupName !== undefined,
        '응답 데이터 - joiningChallengeGroups[0].currentMemberCount 존재': () => responseData.joiningChallengeGroups[0]?.currentMemberCount !== undefined,
        '응답 데이터 - joiningChallengeGroups[0].maximumMemberCount 존재': () => responseData.joiningChallengeGroups[0]?.maximumMemberCount !== undefined,
        '응답 데이터 - joiningChallengeGroups[0].joinCode 존재': () => responseData.joiningChallengeGroups[0]?.joinCode !== undefined,
        '응답 데이터 - joiningChallengeGroups[0].status 존재': () => responseData.joiningChallengeGroups[0]?.status !== undefined,
        '응답 데이터 - joiningChallengeGroups[0].startAt 존재': () => responseData.joiningChallengeGroups[0]?.startAt !== undefined,
        '응답 데이터 - joiningChallengeGroups[0].endAt 존재': () => responseData.joiningChallengeGroups[0]?.endAt !== undefined,
        '응답 데이터 - joiningChallengeGroups[0].progressDay 존재': () => responseData.joiningChallengeGroups[0]?.progressDay !== undefined,
        '응답 데이터 - joiningChallengeGroups[0].progressRate 존재': () => responseData.joiningChallengeGroups[0]?.progressRate !== undefined,
    });

    return responseData;
}

function requestGetMyTodosInGroupApi(vuIndex, challengeGroupId) {
    const timeout = '1800s';
    const headers = setRequestHeader(tokens[vuIndex]);
    const todayDate = getDateNDaysAgoMySqlDateFormatString(0);
    const endpoint = `${API_BASE_URL}/api/v2/challenge-groups/${challengeGroupId}/my-todos?date=${todayDate}`;

    if (__VU === 1 && __ITER === 0) {
        console.log(`참여중인 특정 챌린지 그룹에서 내 데일리 투두 전체 조회 API 요청 엔드포인트 : ${endpoint}`);
    }

    const response = http.get(endpoint, { headers, timeout, tags: { name: '04_get-my-todos-in-group' } });
    const responseBody = parseResponseBody(response);
    const responseData = responseBody.data;

    const checks = {
        'API HTTP 상태 코드 200': () => response?.status === 200,
        'API 응답 코드 success': () => responseBody?.code === 'success',
        '응답 데이터 - todos 빈 배열 X': () => responseData?.todos.length > 0,
        '응답 데이터 - todos[0].id 존재': () => responseData?.todos[0].id !== undefined,
        '응답 데이터 - todos[0].content 존재': () => responseData?.todos[0].content !== undefined,
        '응답 데이터 - todos[0].status 존재': () => responseData?.todos[0].status !== undefined,
        '응답 데이터 - todos[0].canRequestCertificationReview 존재': () => responseData?.todos[0].status !== undefined,
    };

    // 투두가 인증 상태일 경우
    if (responseData?.todos[0].status !== 'CERTIFY_PENDING') {
        checks['응답 데이터 - todos[0].certificationContent 존재'] = () => responseData?.todos[0].certificationContent !== undefined,
            checks['응답 데이터 - todos[0].certificationMediaUrl 존재'] = () => responseData?.todos[0].certificationMediaUrl !== undefined,
            checks['응답 데이터 - todos[0].reviewFeedback 존재'] = () => responseData?.todos[0].reviewFeedback !== undefined
    }

    check(null, checks);

    return responseData;
}

function requestGetJoiningChallengeGroupTeamRankingApi(vuIndex, challengeGroupId) {
    const timeout = '1800s';
    const headers = setRequestHeader(tokens[vuIndex]);
    const endpoint = `${API_BASE_URL}/api/v1/groups/${challengeGroupId}/ranking`;

    if (__VU === 1 && __ITER === 0) {
        console.log(`참여중인 특정 챌린지 그룹의 그룹원 전체 랭킹 조회 API 요청 엔드포인트 : ${endpoint}`);
    }

    const response = http.get(endpoint, { headers, timeout, tags: { name: '05_get-joining-challenge-group-team-ranking' } });
    const responseBody = parseResponseBody(response);
    const responseData = responseBody.data;

    check(null, {
        'API HTTP 상태 코드 200': () => response?.status === 200,
        'API 응답 코드 success': () => responseBody?.code === 'success',
        '응답 데이터 - ranking 빈 배열 X': () => responseData?.ranking.length > 0,
        '응답 데이터 - ranking[0].memberId 존재': () => responseData?.ranking[0].memberId !== undefined,
        '응답 데이터 - ranking[0].rank 존재': () => responseData?.ranking[0].rank !== undefined,
        '응답 데이터 - ranking[0].profileImageUrl 존재': () => responseData?.ranking[0].profileImageUrl !== undefined,
        '응답 데이터 - ranking[0].name 존재': () => responseData?.ranking[0].name !== undefined,
        '응답 데이터 - ranking[0].historyReadStatus 존재': () => responseData?.ranking[0].historyReadStatus !== undefined,
        '응답 데이터 - ranking[0].achievementRate 존재': () => responseData?.ranking[0].achievementRate !== undefined,
    });

    return responseData;
}

function requestGetMemberTodayTodoHistoriesApi(vuIndex, challengeGroupId, otherMemberId) {
    const timeout = '1800s';
    const headers = setRequestHeader(tokens[vuIndex]);
    const endpoint = `${API_BASE_URL}/api/v2/challenge-groups/${challengeGroupId}/challenge-group-members/${otherMemberId}/today-todo-history`;

    if (__VU === 1 && __ITER === 0) {
        console.log(`데일리 투두 히스토리 전체 조회 API 요청 엔드포인트 : ${endpoint}`);
    }

    const response = http.get(endpoint, { headers, timeout, tags: { name: '06_get-member-today-todo-histories' } });
    const responseBody = parseResponseBody(response);
    const responseData = responseBody.data;

    check(null, {
        'API HTTP 상태 코드 200': () => response?.status === 200,
        'API 응답 코드 success': () => responseBody?.code === 'success',
        '응답 데이터 - isMine 존재': () => responseData?.isMine !== undefined,
        '응답 데이터 - currentTodoHistoryToReadIndex 존재': () => responseData?.currentTodoHistoryToReadIndex !== undefined,
        '응답 데이터 - todos 빈 배열 X': () => responseData?.todos.length > 0,
        '응답 데이터 - todos[0].historyId 존재': () => responseData?.todos[0].historyId !== undefined,
        '응답 데이터 - todos[0].todoId 존재': () => responseData?.todos[0].todoId !== undefined,
        '응답 데이터 - todos[0].content 존재': () => responseData?.todos[0].content !== undefined,
        '응답 데이터 - todos[0].status 존재': () => responseData?.todos[0].status !== undefined,
        '응답 데이터 - todos[0].canRequestCertification 존재': () => responseData?.todos[0].canRequestCertification !== undefined,
        '응답 데이터 - todos[0].canRequestCertificationReview 존재': () => responseData?.todos[0].canRequestCertificationReview !== undefined,
        '응답 데이터 - todos[0].isRead 존재': () => responseData?.todos[0].isRead !== undefined,
    });

    return responseData;
}