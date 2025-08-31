export const parseTimeToMinutes = (timeLeft: string): number => {
    if (!timeLeft) return Infinity;

    const hoursMatch = timeLeft.match(/(\d+)\s*시간/);
    const minutesMatch = timeLeft.match(/(\d+)\s*분/);

    const hours = hoursMatch ? parseInt(hoursMatch[1], 10) : 0;
    const minutes = minutesMatch ? parseInt(minutesMatch[1], 10) : 0;

    return hours * 60 + minutes;
};