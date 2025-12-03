/**
 * 현재 날짜/시간을 반환
 */
export function getTodayDate() {
    return new Date();
}

/**
 * 현재 날짜를 기준으로 n일 전 날짜를 반환
 */
export function getDateNDaysAgo(n) {
    const date = new Date();
    date.setDate(date.getDate() - n);
    return date;
}

/**
 * target date를 기준으로 n일 후 날짜를 반환
 */
export function getDateNDaysLater(targetDate, n) {
    const date = new Date(targetDate);
    date.setDate(date.getDate() + n);
    return date;
}

/**
 * 특정 사이클의 그룹 시작일 반환
 */
export function getGroupStartAtInCycle(groupStartAtInCycle, cycle, groupRunningDay) {
    const date = new Date(groupStartAtInCycle);
    date.setDate(date.getDate() + (cycle * groupRunningDay));
    return date;
}

/**
 * Date 객체를 MySql Datetime 문자열로 변환
 */
export function convertDateObjectToMySqlDatetimeFormat(date) {
    const pad = (n) => n.toString().padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
}

/**
 * Date 객체를 MySql Date 문자열로 변환
 */
export function convertDateObjectToMySqlDateFormat(date) {
    const pad = (n) => n.toString().padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

export function getDateNDaysAgoMySqlDateFormatString(n) {
    const dateNDaysAgo = getDateNDaysAgo(n);
    const pad = (n) => n.toString().padStart(2, "0");

    const year = dateNDaysAgo.getFullYear();
    const month = pad(dateNDaysAgo.getMonth() + 1);
    const day = pad(dateNDaysAgo.getDate());

    return `${year}-${month}-${day}`;
}
