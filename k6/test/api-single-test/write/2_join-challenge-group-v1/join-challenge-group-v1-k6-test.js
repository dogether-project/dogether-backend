import { sleep } from 'k6';
import {check} from 'k6';
import { SharedArray } from 'k6/data';
import http from 'k6/http';
import {getJoinCodesPerMember} from "../../../../common/test-data/test-data-common.js";
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
    const joinCodes = getJoinCodesPerMember();

    console.log("⏰ 10초 대기 시작.");
    sleep(10);
    console.log("✅ 10초 대기 완료.");

    return {joinCodes};
}

export default function (data) {
    const vuIndex = __VU - 1;
    const response = requestApi(vuIndex, data.joinCodes[vuIndex]);
    const responseBody = parseResponseBody(response);
    const responseData = responseBody.data;

    check(null, {
        'API HTTP 상태 코드 200': () => response.status === 200,
        'API 응답 코드 success': () => responseBody.code === 'success',
        '응답 데이터 - groupName 존재': () => responseData?.groupName !== undefined,
        '응답 데이터 - duration 존재': () => responseData?.duration !== undefined,
        '응답 데이터 - maximumMemberCount 존재': () => responseData?.maximumMemberCount !== undefined,
        '응답 데이터 - startAt 존재': () => responseData?.startAt !== undefined,
        '응답 데이터 - endAt 존재': () => responseData?.endAt !== undefined,
    });
}

function requestApi(vuIndex, joinCode) {
    const timeout = '1800s';
    const headers = setRequestHeader(tokens[vuIndex]);
    const body = JSON.stringify({
        joinCode: joinCode
    });

    return http.post(`${API_BASE_URL}/api/v1/groups/join`, body, { headers, timeout });
}

export function teardown() {
    console.log("🧹 5초 후 테스트 데이터 정리 시작.");
    sleep(5);
}
