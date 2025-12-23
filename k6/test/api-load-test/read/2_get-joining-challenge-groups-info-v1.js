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
}

function requestApi(vuIndex) {
    const timeout = '1800s';
    const headers = setRequestHeader(tokens[vuIndex]);

    return http.get(`${API_BASE_URL}/api/v1/groups/my`, { headers, timeout });
}
