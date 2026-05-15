import { sleep } from 'k6';
import {check} from 'k6';
import { SharedArray } from 'k6/data';
import http from 'k6/http';
import {getOneCertifiableTodoIdPerMember} from "../../../../common/test-data/test-data-common.js";
import {parseResponseBody, setRequestHeader} from "../../../../common/util/api-util.js";
import {API_BASE_URL} from "../../../../common/secret/secret.js";

const tokens = new SharedArray('tokens', () => JSON.parse(open('../../../../common/secret/tokens.json')));

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
    const dailyTodoIds = getOneCertifiableTodoIdPerMember();

    console.log("⏰ 10초 대기 시작.");
    sleep(10);
    console.log("✅ 10초 대기 완료.");

    return {dailyTodoIds};
}

export default function (data) {
    const vuIndex = __VU - 1;
    const response = requestApi(vuIndex, data.dailyTodoIds[vuIndex]);
    const responseBody = parseResponseBody(response);

    check(null, {
        'API HTTP 상태 코드 200': () => response?.status === 200,
        'API 응답 코드 success': () => responseBody?.code === 'success',
    });
}

function requestApi(vuIndex, dailyTodoId) {
    const timeout = '1800s';
    const headers = setRequestHeader(tokens[vuIndex]);
    const body = JSON.stringify({
        content: `${vuIndex}번 사용자 데일리 투두 인증 땅땅`,
        mediaUrl: `http://인증-이미지-${vuIndex}.site`
    });

    return http.post(`${API_BASE_URL}/api/v1/todos/${dailyTodoId}/certify`, body, { headers, timeout });
}

export function teardown() {
    console.log("🧹 5초 후 테스트 데이터 정리 시작.");
    sleep(5);
}
