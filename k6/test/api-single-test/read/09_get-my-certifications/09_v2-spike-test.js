import { sleep } from 'k6';
import {check} from 'k6';
import { SharedArray } from 'k6/data';
import http from 'k6/http';
import {parseResponseBody, setRequestHeader} from "../../../../common/util/api-util.js";
import {API_BASE_URL} from "../../../../common/secret/secret.js";

const tokens = new SharedArray('tokens', () => JSON.parse(open('../../../../common/secret/tokens.json')));

export const options = {
    setupTimeout: '30m',
    scenarios: {
        v2_09_spike_test: {
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
    console.log("⏰ 5초 대기 시작.");
    sleep(5);
    console.log("✅ 5초 대기 완료.");
}

export default function () {
    const vuIndex = __VU - 1;
    const response = requestApi(vuIndex);
    const responseBody = parseResponseBody(response);
    const responseData = responseBody.data;

    check(null, {
        'API HTTP 상태 코드 200': () => response?.status === 200,
        'API 응답 코드 success': () => responseBody?.code === 'success',
        '응답 데이터 - certifications 빈 배열 X': () => responseData?.certifications.length > 0,
        '응답 데이터 - certifications[0].groupedBy 존재': () => responseData?.certifications[0].groupedBy !== undefined,
        '응답 데이터 - certifications[0].certificationInfo 빈 배열 X': () => responseData?.certifications[0].certificationInfo.length > 0,
        '응답 데이터 - certifications[0].certificationInfo[0].id 존재': () => responseData?.certifications[0].certificationInfo[0].id !== undefined,
        '응답 데이터 - certifications[0].certificationInfo[0].content 존재': () => responseData?.certifications[0].certificationInfo[0].content !== undefined,
        '응답 데이터 - certifications[0].certificationInfo[0].status 존재': () => responseData?.certifications[0].certificationInfo[0].status !== undefined,
        '응답 데이터 - certifications[0].certificationInfo[0].certificationContent 존재': () => responseData?.certifications[0].certificationInfo[0].certificationContent !== undefined,
        '응답 데이터 - certifications[0].certificationInfo[0].certificationMediaUrl 존재': () => responseData?.certifications[0].certificationInfo[0].certificationMediaUrl !== undefined,
        '응답 데이터 - certifications[0].certificationInfo[0].reviewFeedback 존재': () => responseData?.certifications[0].certificationInfo[0].reviewFeedback !== undefined
    });
}

function requestApi(vuIndex) {
    const timeout = '1800s';
    const headers = setRequestHeader(tokens[vuIndex]);
    const endpoint = `${API_BASE_URL}/api/v2/my/certifications?sortBy=CERTIFICATED_AT&page=0`;

    if (__VU === 1 && __ITER === 0) {
        console.log(`API 요청 엔드포인트 : ${endpoint}`);
    }

    return http.get(endpoint, { headers, timeout });
}
