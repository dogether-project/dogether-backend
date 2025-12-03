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
        '응답 데이터 - dailyTodoStats.totalCertificatedCount 존재': () => responseData?.dailyTodoStats.totalCertificatedCount !== undefined,
        '응답 데이터 - dailyTodoStats.totalApprovedCount 존재': () => responseData?.dailyTodoStats.totalApprovedCount !== undefined,
        '응답 데이터 - dailyTodoStats.totalRejectedCount 존재': () => responseData?.dailyTodoStats.totalRejectedCount !== undefined,
        '응답 데이터 - certificationsGroupedByTodoCompletedAt 빈 배열 X': () => responseData?.certificationsGroupedByTodoCompletedAt.length > 0,
        '응답 데이터 - certificationsGroupedByTodoCompletedAt[0].createdAt 존재': () => responseData?.certificationsGroupedByTodoCompletedAt[0].createdAt !== undefined,
        '응답 데이터 - certificationsGroupedByTodoCompletedAt[0].certificationInfo 빈 배열 X': () => responseData?.certificationsGroupedByTodoCompletedAt[0].certificationInfo.length > 0,
        '응답 데이터 - certificationsGroupedByTodoCompletedAt[0].certificationInfo[0].id 존재': () => responseData?.certificationsGroupedByTodoCompletedAt[0].certificationInfo[0].id !== undefined,
        '응답 데이터 - certificationsGroupedByTodoCompletedAt[0].certificationInfo[0].content 존재': () => responseData?.certificationsGroupedByTodoCompletedAt[0].certificationInfo[0].content !== undefined,
        '응답 데이터 - certificationsGroupedByTodoCompletedAt[0].certificationInfo[0].status 존재': () => responseData?.certificationsGroupedByTodoCompletedAt[0].certificationInfo[0].status !== undefined,
        '응답 데이터 - certificationsGroupedByTodoCompletedAt[0].certificationInfo[0].certificationContent 존재': () => responseData?.certificationsGroupedByTodoCompletedAt[0].certificationInfo[0].certificationContent !== undefined,
        '응답 데이터 - certificationsGroupedByTodoCompletedAt[0].certificationInfo[0].certificationMediaUrl 존재': () => responseData?.certificationsGroupedByTodoCompletedAt[0].certificationInfo[0].certificationMediaUrl !== undefined,
        '응답 데이터 - certificationsGroupedByTodoCompletedAt[0].certificationInfo[0].reviewFeedback 존재': () => responseData?.certificationsGroupedByTodoCompletedAt[0].certificationInfo[0].reviewFeedback !== undefined,
        '응답 데이터 - pageInfo.totalPageCount 존재': () => responseData?.pageInfo.totalPageCount !== undefined,
        '응답 데이터 - pageInfo.recentPageNumber 존재': () => responseData?.pageInfo.recentPageNumber !== undefined,
        '응답 데이터 - pageInfo.hasNext 존재': () => responseData?.pageInfo.hasNext !== undefined,
        '응답 데이터 - pageInfo.pageSize 존재': () => responseData?.pageInfo.pageSize !== undefined,
    });
}

function requestApi(vuIndex) {
    const timeout = '1800s';
    const headers = setRequestHeader(tokens[vuIndex]);

    return http.get(`${API_BASE_URL}/api/v1/my/activity?sortBy=TODO_COMPLETED_AT&page=0`, { headers, timeout });
}
