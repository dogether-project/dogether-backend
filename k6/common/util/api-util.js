/**
 * API 요청 헤더 설정
 */
export function setRequestHeader(token) {
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
    };
}

/**
 * API 응답 바디 파싱
 */
export function parseResponseBody(response) {
    return JSON.parse(response.body);
}
