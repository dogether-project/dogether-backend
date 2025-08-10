import { sleep } from 'k6';
import {check} from 'k6';
import { SharedArray } from 'k6/data';
import {createDailyTodos} from "../../../../../common/api/api-call/api-call.js";
import {getTodoTargetGroupIdsPerMember} from "../../../../../common/db/data/set-up-data/write-test/create-daily-todos-v1-set-up-data.js";

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
    const challengeGroupIds = getTodoTargetGroupIdsPerMember();

    console.log("⏰ 10초 대기 시작.");
    sleep(10);
    console.log("✅ 10초 대기 완료.");

    return {challengeGroupIds};
}

export default function (data) {
    const vuIndex = __VU - 1;
    const token = tokens[vuIndex];
    const challengeGroupId = data.challengeGroupIds[vuIndex];
    const todos = [
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
    ];

    const res = createDailyTodos(token, challengeGroupId, { todos });

    check(res, {
        'API 응답 상태 코드 200': (r) => r.status === 200
    });
}

export function teardown() {
    console.log("🧹 5초 후 테스트 데이터 정리 시작.");
    sleep(5);
}
