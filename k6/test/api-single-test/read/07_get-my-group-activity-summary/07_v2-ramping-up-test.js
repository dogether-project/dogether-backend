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
        v2_07_ramping_up_test: {
            executor: 'ramping-vus',
            stages: [
                { duration: '2m', target: 200 }, // 2분 동안 0명에서 200명까지 점진적 증가
                { duration: '5m', target: 200 }, // 200명 유지 (여기서 병목 확인)
                { duration: '1m', target: 0 },   // 테스트 종료
            ],
        },
    },
    thresholds: {
        // 1. 응답 시간 기준: 95%의 요청이 500ms 이내에 완료되어야 함
        http_req_duration: ['p(95)<500'],

        // 2. 실패율 기준: 실패한 요청의 비율이 1% 미만이어야 함 (사실상 거의 없어야 함)
        // 'rate == 0'으로 설정하면 단 하나의 에러만 나도 테스트가 실패(Fail)로 처리됩니다.
        http_req_failed: ['rate<0.01'],
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

    sleep(1);
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
