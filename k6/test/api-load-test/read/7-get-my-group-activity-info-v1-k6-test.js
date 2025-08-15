import { sleep } from 'k6';
import {check} from 'k6';
import { SharedArray } from 'k6/data';
import {getChallengeGroupActivityInfoV1} from "../../../common/api/api-call/v1-api-call.js";
import {parseResponseBody} from "../../../common/api/util/api-util.js";

import {getChallengeGroupIdsPerMember} from "../../../common/db/data/set-up-data/read-test/variable-running-activity-data.js";
// import {getChallengeGroupIdsPerMember} from "../../../common/db/data/set-up-data/read-test/maximum-running-activity-data.js";

const tokens = new SharedArray('tokens', () => JSON.parse(open('../../../secret/tokens.json')));

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
    const challengeGroupIds = getChallengeGroupIdsPerMember();

    console.log("⏰ 5초 대기 시작.");
    sleep(5);
    console.log("✅ 5초 대기 완료.\n");

    return {challengeGroupIds};
}

export default function (data) {
    const vuIndex = __VU - 1;
    const token = tokens[vuIndex];
    const challengeGroupId = data.challengeGroupIds[vuIndex][0];

    const res = getChallengeGroupActivityInfoV1(token, challengeGroupId);
    const responseData = parseResponseBody(res).data;

    // TODO : 검증 로직 추가
    check(res, {
        'API 응답 상태 코드 200': (r) => r.status === 200
    });
}
