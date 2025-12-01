import { sleep } from 'k6';
import {check} from 'k6';
import { SharedArray } from 'k6/data';
import http from 'k6/http';
import {getTodoTargetGroupIdsPerMember} from "../../../../common/test-data/test-data-common.js";
import {parseResponseBody, setRequestHeader} from "../../../../common/util/api-util.js";
import {API_BASE_URL} from "../../../../common/secret/secret.js";

const tokens = new SharedArray('tokens', () => JSON.parse(open('../../../../common/secret/tokens.json')));

export const options = {
    setupTimeout: '30m',
    scenarios: {
        default: {
            executor: 'per-vu-iterations',
            vus: 100,
            // vus: 400,
            iterations: 1,
            maxDuration: '30m',
        },
    },
};

export function setup() {
    const challengeGroupIds = getTodoTargetGroupIdsPerMember();

    console.log("⏰ 10초 대기 시작.");
    sleep(10);
    console.log("✅ 10초 대기 완료.");

    return {challengeGroupIds};
}

export default function (data) {
    const vuIndex = __VU - 1;
    const response = requestApi(vuIndex, data.challengeGroupIds[vuIndex]);
    const responseBody = parseResponseBody(response);

    check(null, {
        'API HTTP 상태 코드 200': () => response?.status === 200,
        'API 응답 코드 success': () => responseBody?.code === 'success',
    });
}

function requestApi(vuIndex, challengeGroupId) {
    const timeout = '1800s';
    const headers = setRequestHeader(tokens[vuIndex]);
    const body = JSON.stringify({
        todos: [
            `${vuIndex}번 사용자 투두 A`,
            `${vuIndex}번 사용자 투두 B`,
            `${vuIndex}번 사용자 투두 C`,
            `${vuIndex}번 사용자 투두 D`,
            `${vuIndex}번 사용자 투두 E`,
            `${vuIndex}번 사용자 투두 F`,
            `${vuIndex}번 사용자 투두 G`,
            `${vuIndex}번 사용자 투두 H`,
            `${vuIndex}번 사용자 투두 I`,
            `${vuIndex}번 사용자 투두 J`,
        ]
    });

    return http.post(`${API_BASE_URL}/challenge-groups/${challengeGroupId}/todos`, body, { headers, timeout });
}

export function teardown() {
    console.log("🧹 5초 후 테스트 데이터 정리 시작.");
    sleep(5);
}
