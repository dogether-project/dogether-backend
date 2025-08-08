export function setRequestHeader(token) {
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
    };
}

export function parseResponseBody(response) {
    return JSON.parse(response.body);
}
