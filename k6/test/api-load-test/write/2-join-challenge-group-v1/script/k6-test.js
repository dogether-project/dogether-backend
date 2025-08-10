import { sleep } from 'k6';
import {check} from 'k6';
import { SharedArray } from 'k6/data';
import {joinChallengeGroup} from "../../../../../common/api/api-call/api-call.js";
import {parseResponseBody} from "../../../../../common/api/util/api-util.js";
import {getJoinCodesPerMember} from "../../../../../common/db/data/set-up-data/write-test/join-challenge-group-v1-set-up-data.js";

const tokens = new SharedArray('tokens', () => JSON.parse(open('../../../../../secret/tokens.json')));

export const options = {
    setupTimeout: '30m',
    scenarios: {
        default: {
            executor: 'per-vu-iterations',
            vus: 100,
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
    const token = tokens[vuIndex];
    const joinCode = data.joinCodes[vuIndex];

    const res = joinChallengeGroup(token, { joinCode });
    const responseData = parseResponseBody(res).data;

    check(res, {
        'API 응답 상태 코드 200': (r) => r.status === 200,
        '응답 데이터 - groupName 존재': () => responseData?.groupName !== undefined,
        '응답 데이터 - duration 존재': () => responseData?.duration !== undefined,
        '응답 데이터 - maximumMemberCount 존재': () => responseData?.maximumMemberCount !== undefined,
        '응답 데이터 - startAt 존재': () => responseData?.startAt !== undefined,
        '응답 데이터 - endAt 존재': () => responseData?.endAt !== undefined,
    });
}

export function teardown() {
    console.log("🧹 5초 후 테스트 데이터 정리 시작.");
    sleep(5);
}
