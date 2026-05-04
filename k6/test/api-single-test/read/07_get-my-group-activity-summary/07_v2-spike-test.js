import { sleep } from 'k6';
import {check} from 'k6';
import { SharedArray } from 'k6/data';
import http from 'k6/http';
import {getChallengeGroupIdsPerMember} from "../../../../common/test-data/test-data-common.js";
import {parseResponseBody, setRequestHeader} from "../../../../common/util/api-util.js";
import {API_BASE_URL} from "../../../../common/secret/secret.js";

const tokens = new SharedArray('tokens', () => JSON.parse(open('../../../../common/secret/tokens.json')));

export const options = {
    setupTimeout: '30m',
    scenarios: {
        v2_07_spike_test: {
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

    return {challengeGroupIds};
}

export default function (data) {
    const vuIndex = __VU - 1;
    const response = requestApi(vuIndex, data.challengeGroupIds[vuIndex][0]);
    const responseBody = parseResponseBody(response);
    const responseData = responseBody.data;

    check(null, {
        'API HTTP 상태 코드 200': () => response?.status === 200,
        'API 응답 코드 success': () => responseBody?.code === 'success',
        '응답 데이터 - groupInfo.name 존재': () => responseData?.groupInfo.name !== undefined,
        '응답 데이터 - groupInfo.maximumMemberCount 존재': () => responseData?.groupInfo.maximumMemberCount !== undefined,
        '응답 데이터 - groupInfo.currentMemberCount. 존재': () => responseData?.groupInfo.currentMemberCount !== undefined,
        '응답 데이터 - groupInfo.joinCode 존재': () => responseData?.groupInfo.joinCode !== undefined,
        '응답 데이터 - groupInfo.endAt 존재': () => responseData?.groupInfo.endAt !== undefined,
        '응답 데이터 - certificationPeriods 빈 배열 X': () => responseData?.certificationPeriods.length > 0,
        '응답 데이터 - certificationPeriods[0].day 존재': () => responseData?.certificationPeriods[0].day !== undefined,
        '응답 데이터 - certificationPeriods[0].createdCount 존재': () => responseData?.certificationPeriods[0].createdCount !== undefined,
        '응답 데이터 - certificationPeriods[0].certificatedCount 존재': () => responseData?.certificationPeriods[0].certificatedCount !== undefined,
        '응답 데이터 - certificationPeriods[0].certificationRate 존재': () => responseData?.certificationPeriods[0].certificationRate !== undefined,
        '응답 데이터 - ranking.totalMemberCount 존재': () => responseData?.ranking.totalMemberCount !== undefined,
        '응답 데이터 - ranking.myRank 존재': () => responseData?.ranking.myRank !== undefined,
    });
}

function requestApi(vuIndex, challengeGroupId) {
    const timeout = '1800s';
    const headers = setRequestHeader(tokens[vuIndex]);
    const endpoint = `${API_BASE_URL}/api/v2/my/groups/${challengeGroupId}/activity-summary`;

    if (__VU === 1 && __ITER === 0) {
        console.log(`API 요청 엔드포인트 : ${endpoint}`);
    }

    return http.get(endpoint, { headers, timeout });
}
