import { sleep } from 'k6';
import {check} from 'k6';
import { SharedArray } from 'k6/data';
import {getMyDailyTodos} from "../../../../../common/api/api-call/api-call.js";
import {parseResponseBody} from "../../../../../common/api/util/api-util.js";

const tokens = new SharedArray('tokens', () => JSON.parse(open('../../../../../secret/tokens.json')));
const data = new SharedArray('temp', () => [JSON.parse(open('./data.json'))])[0];
const challengeGroupIds = data.groupIds;

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
    const challengeGroupId = challengeGroupIds[vuIndex][0];
    const todayDate = getCurrentDateInKst();

    const res = getMyDailyTodos(token, challengeGroupId, todayDate);
    const responseData = parseResponseBody(res).data;

    // TODO : 검증 로직 추가
    check(res, {
        'API 응답 상태 코드 200': (r) => r.status === 200
    });
}

export function teardown() {
    console.log("🧹 5초 후 테스트 데이터 정리 시작.");
    sleep(5);
}

function getCurrentDateInKst() {
    const now = new Date();
    const offset = 9 * 60; // KST는 UTC+9
    const kst = new Date(now.getTime() + offset * 60 * 1000);
    return kst.toISOString().slice(0, 10); // "YYYY-MM-DD"
}
