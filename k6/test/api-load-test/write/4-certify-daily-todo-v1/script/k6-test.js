import { sleep } from 'k6';
import {check} from 'k6';
import { SharedArray } from 'k6/data';
import {certifyDailyTodoV1} from "../../../../../common/api/api-call/v1-api-call.js";
import {getOneCertifiableTodoIdPerMember} from "../../../../../common/db/data/set-up-data/write-test/certify-daily-todo-v1-set-up-data.js";

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
    const dailyTodoIds = getOneCertifiableTodoIdPerMember();

    console.log("⏰ 10초 대기 시작.");
    sleep(10);
    console.log("✅ 10초 대기 완료.");

    return {dailyTodoIds};
}

export default function (data) {
    const vuIndex = __VU - 1;
    const token = tokens[vuIndex];
    const dailyTodoId = data.dailyTodoIds[vuIndex]
    const certifyData = {
        content: `${vuIndex}번 사용자 데일리 투두 인증 땅땅`,
        mediaUrl: `http://인증-이미지-${vuIndex}.site`
    };

    const res = certifyDailyTodoV1(
        token,
        dailyTodoId,
        certifyData
    );

    check(res, {
        'API 응답 상태 코드 200': (r) => r.status === 200,
    });
}

export function teardown() {
    console.log("🧹 5초 후 테스트 데이터 정리 시작.");
    sleep(5);
}
