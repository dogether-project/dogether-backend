/**
 * 현재 KST 날짜 계산 함수
 */
export function getCurrentDateInKst() {
    const nowDate = new Date();
    return nowDate.toLocaleString('sv-SE', { timeZone: 'Asia/Seoul' })
        .replace('T', ' ');
}

export function getCurrentDateInKstWithoutTime() {
    return getCurrentDateInKst().slice(0, 10);
}

/**
 * n일 전 KST 날짜 계산 함수
 */
export function getDateNDaysAgoInKst(n) {
    const kstTime = new Date();        // 현재 시각
    kstTime.setDate(kstTime.getDate() - n);  // n일 전
    return kstTime.toLocaleString('sv-SE', { timeZone: 'Asia/Seoul' });
}

/**
 * n일 후 KST 날짜 계산 함수
 */
export function getDateNDaysLaterInKst(n) {
    const now = new Date();
    const kstTime = new Date(now.getTime() + 9 * 60 * 60 * 1000); // UTC → KST
    kstTime.setDate(kstTime.getDate() + n);
    return kstTime.toLocaleString('sv-SE', { timeZone: 'Asia/Seoul' });
}

/**
 * startAgoDays일 전에 시작하여 durationDays일 만큼 진행하는 그룹의 종료일 계산 함수
 */
export function getEndDateFromStartAgoAndDuration(startAgoDays, durationDays) {
    const now = new Date();

    // 시작일 계산: 현재 - startAgoDays
    const startDate = new Date(now.getTime() + 9 * 60 * 60 * 1000); // UTC → KST
    startDate.setDate(startDate.getDate() - startAgoDays);

    // 종료일 계산: 시작일 + (durationDays - 1)
    const endDate = new Date(startDate.getTime());
    endDate.setDate(endDate.getDate() + durationDays - 1);

    return endDate.toLocaleString('sv-SE', { timeZone: 'Asia/Seoul' });
}

/**
 * startDate에서 cycle * duration일 이후 날짜를 구하는 계산 함수
 */
export function calculateNextDate(startDate, cycle, duration) {
    const d = new Date(startDate);
    d.setDate(d.getDate() + (cycle * duration));
    return d; // Date 객체 반환
}

/**
 * startAt 날짜에 시작해 duration일 만큼 진행하는 활동의 마지막 활동 일을 구하는 계산 함수
 */
export function calculateEndAt(startAt, duration) {
    const d = new Date(startAt);
    d.setDate(d.getDate() + (duration - 1));
    return d; // Date 객체 반환
}

/**
 * 주어진 그룹 번호(n)와 그룹 크기(groupSize)에 따라 해당 그룹에 속하는 연속된 정수 배열을 반환
 * ex) n = 2, groupSize = 3 → [4, 5, 6]
 */
export function getConsecutiveNumbersByGroup(n, groupSize) {
    const start = (n - 1) * groupSize + 1;
    return Array.from({ length: groupSize }, (_, i) => start + i);
}
