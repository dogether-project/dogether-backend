import { sleep } from 'k6';
import {check} from 'k6';
import { SharedArray } from 'k6/data';
import http from 'k6/http';
import {parseResponseBody, setRequestHeader} from "../../../common/util/api-util.js";
import {API_BASE_URL} from "../../../common/secret/secret.js";

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
}

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
        '응답 데이터 - dailyTodoCertifications 빈 배열 X': () => responseData?.dailyTodoCertifications.length > 0,
        '응답 데이터 - dailyTodoCertifications[0].id 존재': () => responseData?.dailyTodoCertifications[0].id !== undefined,
        '응답 데이터 - dailyTodoCertifications[0].content 존재': () => responseData?.dailyTodoCertifications[0].content !== undefined,
        '응답 데이터 - dailyTodoCertifications[0].mediaUrl 존재': () => responseData?.dailyTodoCertifications[0].mediaUrl !== undefined,
        '응답 데이터 - dailyTodoCertifications[0].todoContent 존재': () => responseData?.dailyTodoCertifications[0].todoContent !== undefined,
        '응답 데이터 - dailyTodoCertifications[0].doer 존재': () => responseData?.dailyTodoCertifications[0].doer !== undefined,
    });
}

function requestApi(vuIndex) {
    const timeout = '1800s';
    const headers = setRequestHeader(tokens[vuIndex]);

    return http.get(`${API_BASE_URL}/todo-certifications/pending-review`, { headers, timeout });
}
