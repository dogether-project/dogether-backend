import { sleep } from 'k6';
import {check} from 'k6';
import { SharedArray } from 'k6/data';
import {reviewDailyTodoCertificationV1} from "../../../../../common/api/api-call/v1-api-call.js";
import {getPendingCertificationIdsPerReviewer} from "../../../../../common/db/data/set-up-data/write-test/review-todo-certification-v1-set-up-data.js";

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
}

export function setup() {
    const dailyTodoCertificationIds = getPendingCertificationIdsPerReviewer();

    console.log("⏰ 10초 대기 시작.");
    sleep(10);
    console.log("✅ 10초 대기 완료.");

    return {dailyTodoCertificationIds};
}

export default function (data) {
    const vuIndex = __VU - 1;
    const token = tokens[vuIndex];
    const reviewPendingDailyTodoCertificationId = data.dailyTodoCertificationIds[vuIndex][0];
    const reviewData = {
        result: "APPROVE",
        reviewFeedback: `굿좝 - ${vuIndex}`
    };

    const res = reviewDailyTodoCertificationV1(token, reviewPendingDailyTodoCertificationId, reviewData);

    check(res, {
        'API 응답 상태 코드 200': (r) => r.status === 200,
    });
}

export function teardown() {
    console.log("🧹 5초 후 테스트 데이터 정리 시작.");
    sleep(5);
}
