import {moment} from 'entcore';

export class DateUtils {
    static FORMAT = {
        'DAY/MONTH/YEAR-HOUR-MIN': 'DD/MM/YYYY HH:mm',
    }
    /**
     * Format date based on given format using moment
     * @param date date to format
     * @param format format
     */
    static format(date: any, format: string) {
        return moment(date).format(format);
    }
}