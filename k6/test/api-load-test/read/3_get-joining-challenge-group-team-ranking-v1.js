import { sleep } from 'k6';
import {check} from 'k6';
import { SharedArray } from 'k6/data';
import http from 'k6/http';
import {parseResponseBody, setRequestHeader} from "../../../common/util/api-util.js";
import {API_BASE_URL} from "../../../common/secret/secret.js";
import {getChallengeGroupIdsPerMember} from "../../../common/test-data/test-data-common.js";

const tokens = new SharedArray('tokens', () => JSON.parse(open('../../../common/secret/tokens.json')));

export const options = {
    setupTimeout: '30m',
    scenarios: {
        default: {
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

    console.log("⏰ 5초 대기 시작.");
    sleep(5);
    console.log("✅ 5초 대기 완료.");

    return { challengeGroupIds };
}

export default function (data) {
    const vuIndex = __VU - 1;
    const response = requestApi(vuIndex, data.challengeGroupIds[vuIndex][0]);
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
}

function requestApi(vuIndex, challengeGroupId) {
    const timeout = '1800s';
    const headers = setRequestHeader(tokens[vuIndex]);

    return http.get(`${API_BASE_URL}/api/v1/groups/${challengeGroupId}/ranking`, { headers, timeout });
}
