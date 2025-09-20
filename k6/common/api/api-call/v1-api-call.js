import http from 'k6/http';
import {setRequestHeader} from "../util/api-util.js";
import { API_BASE_URL } from "../../../secret/secret.js";

const API_URL_PREFIX = `${API_BASE_URL}/api/v1`;

/**
 * 챌린지 그룹
 */

// 챌린지 그룹 생성 API
export const createChallengeGroupV1 = (token, requestData) => {
    const headers = setRequestHeader(token);
    const payload = JSON.stringify({
        groupName: requestData.groupName,
        maximumMemberCount: requestData.maximumMemberCount,
        startAt: requestData.startAt,
        duration: requestData.duration,
    });

    return http.post(`${API_URL_PREFIX}/groups`, payload, {headers});
}

// 챌린지 그룹 참여 API
export const joinChallengeGroupV1 = (token, requestData, timeout) => {
    const headers = setRequestHeader(token);
    const payload = JSON.stringify({
        joinCode: requestData.joinCode,
    });

    return http.post(`${API_URL_PREFIX}/groups/join`, payload, { headers, timeout });
}

// 챌린지 그룹 참여 여부 조회 API
export function checkGroupParticipatingV1(token) {
    const headers = setRequestHeader(token);
    return http.get(`${API_URL_PREFIX}/groups/participating`, { headers });
}

// 사용자가 참여중인 챌린지 그룹 정보 전체 조회 API
export function getJoiningChallengeGroupsInfoV1(token) {
    const headers = setRequestHeader(token);
    return http.get(`${API_URL_PREFIX}/groups/my`, { headers });
}

// 참여중인 특정 챌린지 그룹의 그룹원 전체 순위 조회 API
export function getRankingInChallengeGroupV1(token, challengeGroupId) {
    const headers = setRequestHeader(token);
    return http.get(`${API_URL_PREFIX}/groups/${challengeGroupId}/ranking`, { headers });
}


/**
 * 데일리 투두
 */
// 데일리 투두 작성 API
export const createDailyTodosV1 = (token, challengeGroupId, requestData) => {
    const headers = setRequestHeader(token);
    const payload = JSON.stringify({
        todos: requestData.todos,
    });

    return http.post(`${API_URL_PREFIX}/challenge-groups/${challengeGroupId}/todos`, payload, { headers });
}

// 특정 챌린지 그룹에서 사용자가 작성한 데일리 투두 전체 조회 API
export function getMyDailyTodosV1(token, challengeGroupId, date) {
    const headers = setRequestHeader(token);
    return http.get(`${API_URL_PREFIX}/challenge-groups/${challengeGroupId}/my-todos?date=${date}`, { headers });
}


/**
 * 데일리 투두 인증
 */
// 데일리 투두 인증 API
export const certifyDailyTodoV1 = (token, dailyTodoId, requestData) => {
    const headers = setRequestHeader(token);
    const payload = JSON.stringify({
        content: requestData.content,
        mediaUrl: requestData.mediaUrl,
    });

    return http.post(`${API_URL_PREFIX}/todos/${dailyTodoId}/certify`, payload, { headers });
}

// 데일리 투두 인증 검사 API
export const reviewDailyTodoCertificationV1 = (token, dailyTodoCertificationId, requestData) => {
    const headers = setRequestHeader(token);
    const payload = JSON.stringify({
        result: requestData.result,
        reviewFeedback: requestData.reviewFeedback,
    });

    return http.post(`${API_URL_PREFIX}/todo-certifications/${dailyTodoCertificationId}/review`, payload, { headers });
}

// 사용자가 검사해 줘야 하는 데일리 투두 수행 인증 전체 조회 API
export function getReviewPendingDailyTodoCertificationsV1(token) {
    const headers = setRequestHeader(token);
    return http.get(`${API_URL_PREFIX}/todo-certifications/pending-review`, { headers });
}


/**
 * 데일리 투두 히스토리
 */
// 참여중인 특정 챌린지 그룹에 속한 특정 그룹원의 당일 데일리 투두 히스토리 전체 조회 API
export function getTodayDailyTodoHistoriesV1(token, challengeGroupId, memberId) {
    const headers = setRequestHeader(token);
    return http.get(`${API_URL_PREFIX}/challenge-groups/${challengeGroupId}/challenge-group-members/${memberId}/today-todo-history`, { headers });
}


/**
 * 사용자 활동 통계
 */
// 참여중인 특정 챌린지 그룹의 활동 통계 조회 API
export function getChallengeGroupActivityInfoV1(token, challengeGroupId) {
    const headers = setRequestHeader(token);
    return http.get(`${API_URL_PREFIX}/my/groups/${challengeGroupId}/activity`, { headers });
}

// 사용자의 활동 통계 및 작성한 인증 목록 전체 조회 API V1
export function getTotalActivityInfoAndDailyTodoCertificationsV1(token, sort, page, timeout) {
    const headers = setRequestHeader(token);
    return http.get(`${API_URL_PREFIX}/my/activity?sort=${sort}&page=${page}`, { headers, timeout});
}

// 사용자 프로필 조회
export function getMemberProfileV1(token) {
    const headers = setRequestHeader(token);
    return http.get(`${API_URL_PREFIX}/my/profile`, { headers });
}
