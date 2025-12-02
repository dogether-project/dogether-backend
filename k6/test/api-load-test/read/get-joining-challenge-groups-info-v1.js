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
    const joiningChallengeGroupsFirstData = responseData.joiningChallengeGroups[0];

    check(null, {
        'API HTTP 상태 코드 200': () => response?.status === 200,
        'API 응답 코드 success': () => responseBody?.code === 'success',
        '응답 데이터 - lastSelectedGroupIndex 존재': () => responseData?.lastSelectedGroupIndex !== undefined,
        '응답 데이터 - joiningChallengeGroups 빈 배열 X': () => responseData?.joiningChallengeGroups.length > 0,
        '응답 데이터 - joiningChallengeGroups[groupId] 존재': () => joiningChallengeGroupsFirstData?.groupId !== undefined,
        '응답 데이터 - joiningChallengeGroups[groupName] 존재': () => joiningChallengeGroupsFirstData?.groupName !== undefined,
        '응답 데이터 - joiningChallengeGroups[currentMemberCount] 존재': () => joiningChallengeGroupsFirstData?.currentMemberCount !== undefined,
        '응답 데이터 - joiningChallengeGroups[maximumMemberCount] 존재': () => joiningChallengeGroupsFirstData?.maximumMemberCount !== undefined,
        '응답 데이터 - joiningChallengeGroups[joinCode] 존재': () => joiningChallengeGroupsFirstData?.joinCode !== undefined,
        '응답 데이터 - joiningChallengeGroups[status] 존재': () => joiningChallengeGroupsFirstData?.status !== undefined,
        '응답 데이터 - joiningChallengeGroups[startAt] 존재': () => joiningChallengeGroupsFirstData?.startAt !== undefined,
        '응답 데이터 - joiningChallengeGroups[endAt] 존재': () => joiningChallengeGroupsFirstData?.endAt !== undefined,
        '응답 데이터 - joiningChallengeGroups[progressDay] 존재': () => joiningChallengeGroupsFirstData?.progressDay !== undefined,
        '응답 데이터 - joiningChallengeGroups[progressRate] 존재': () => joiningChallengeGroupsFirstData?.progressRate !== undefined,
    });
}

function requestApi(vuIndex) {
    const timeout = '1800s';
    const headers = setRequestHeader(tokens[vuIndex]);

    return http.get(`${API_BASE_URL}/groups/my`, { headers, timeout });
}
