import { sleep } from 'k6';
import {check} from 'k6';
import { SharedArray } from 'k6/data';
import { createChallengeGroup } from '../../../../../common/api/api-call/api-call.js';
import {parseResponseBody} from "../../../../../common/api/util/api-util.js";

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
    console.log("⏰ 10초 대기 시작.");
    sleep(10);
    console.log("✅ 10초 대기 완료.\n");
}

export default function () {
    const vuIndex = __VU - 1;
    const token = tokens[vuIndex];
    const requestData = {
        groupName: `예쁘니 그룹 - ${vuIndex}`,
        maximumMemberCount: 20,
        startAt: "TODAY",
        duration: 28
    };

    const res = createChallengeGroup(token, requestData);
    const responseData = parseResponseBody(res).data;

    check(res, {
        'API 응답 상태 코드 200': (r) => r.status === 200,
        '응답 데이터 - joinCode 존재': () => responseData?.joinCode !== undefined
    });
}

export function teardown() {
    console.log("🧹 5초 후 테스트 데이터 정리 시작.\n");
    sleep(5);
}
