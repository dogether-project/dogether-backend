import { sleep } from 'k6';
import {check} from 'k6';
import { SharedArray } from 'k6/data';
import http from 'k6/http';
import {
    getChallengeGroupIdsPerMember,
    getChallengeGroupMembersPerMember
} from "../../../../common/test-data/test-data-common.js";
import {parseResponseBody, setRequestHeader} from "../../../../common/util/api-util.js";
import {API_BASE_URL} from "../../../../common/secret/secret.js";

const tokens = new SharedArray('tokens', () => JSON.parse(open('../../../../common/secret/tokens.json')));

export const options = {
    setupTimeout: '30m',
    scenarios: {
        v2_06_spike_test: {
            executor: 'per-vu-iterations',
            vus: 1,
            // vus: 100,
            // vus: 400,
            iterations: 1,
            maxDuration: '30m',
        },
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
    const response = requestApi(vuIndex, data.challengeGroupIds[vuIndex][0], data.otherChallengeGroupMemberIds[vuIndex][0]);
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
}

function requestApi(vuIndex, challengeGroupId, otherMemberId) {
    const timeout = '1800s';
    const headers = setRequestHeader(tokens[vuIndex]);
    const endpoint = `${API_BASE_URL}/api/v2/challenge-groups/${challengeGroupId}/challenge-group-members/${otherMemberId}/today-todo-history`;

    if (__VU === 1 && __ITER === 0) {
        console.log(`API 요청 엔드포인트 : ${endpoint}`);
    }

    return http.get(endpoint, { headers, timeout });
}
