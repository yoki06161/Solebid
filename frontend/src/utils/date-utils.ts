import { startOfDay, subMonths, subYears } from "date-fns";

export const parseOrderDate = (dateString: string): Date | null => {
    if (!dateString) {
        return null;
    }

    try {
        let normalizedDate: string;

        // "YYYY.MM.DD" 형식을 "YYYY-MM-DD"로 변환
        if (dateString.includes('.')) {
            normalizedDate = dateString.replace(/\./g, '-');
        } else {
            normalizedDate = dateString;
        }

        const date = new Date(normalizedDate);

        // 유효한 날짜인지 확인
        if (isNaN(date.getTime())) {
            console.warn(`Invalid date format: ${dateString}`);
            return null;
        }

        return date;
    } catch (error) {
        console.error(`Error parsing date: ${dateString}`, error);
        return null;
    }
};

/**
 * 날짜를 시작 시간(00:00:00)으로 설정
 */
export const getDateOnly = (date: Date): Date => {
    return new Date(date.getFullYear(), date.getMonth(), date.getDate());
};

/**
 * 두 날짜가 같은 날인지 확인
 */
export const isSameDate = (date1: Date, date2: Date): boolean => {
    return (
        date1.getFullYear() === date2.getFullYear() &&
        date1.getMonth() === date2.getMonth() &&
        date1.getDate() === date2.getDate()
    );
};

/**
 * 날짜 범위 내에 있는지 확인
 */
export const isDateInRange = (targetDate: Date, fromDate: Date, toDate: Date): boolean => {
    const target = getDateOnly(targetDate);
    const from = getDateOnly(fromDate);
    const to = getDateOnly(toDate);

    return target >= from && target <= to;
};

/**
 * 날짜를 "YYYY-MM-DD" 형식으로 포맷
 */
export const formatDate = (date: Date): string => {
    return date.toISOString().split('T')[0];
};

/**
 * 날짜를 "YYYY.MM.DD" 형식으로 포맷
 */
export const formatDateWithDots = (date: Date): string => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}.${month}.${day}`;
};

const PERIODS = {
    MONTH_1: "최근 1개월",
    MONTH_3: "최근 3개월",
    MONTH_6: "최근 6개월",
    YEAR_1: "최근 1년",
    ALL: "전체",
} as const;

export type Period = typeof PERIODS[keyof typeof PERIODS];

const periodActions: Record<string, (date: Date) => Date> = {
    [PERIODS.MONTH_1]: (date) => subMonths(date, 1),
    [PERIODS.MONTH_3]: (date) => subMonths(date, 3),
    [PERIODS.MONTH_6]: (date) => subMonths(date, 6),
    [PERIODS.YEAR_1]: (date) => subYears(date, 1),
};

export const getFromDate = (period: Period): Date | null => {
    if (period === PERIODS.ALL) {
        return null;
    }

    const today = new Date();

    const action = periodActions[period];

    if (!action) {
        console.warn(`Unknown period: ${period}`);
        return null;
    }

    const fromDate = action(today);
    return startOfDay(fromDate);
}