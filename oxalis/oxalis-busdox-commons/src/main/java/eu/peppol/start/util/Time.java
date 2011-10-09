package eu.peppol.start.util;

/**
 * User: nigel
 * Date: Oct 8, 2011
 * Time: 8:55:27 AM
 */
public class Time {

    public static final int MILLISECONDS = 1;
    public static final int SECONDS = 2;
    public static final int MINUTES = 3;
    public static final int HOURS = 4;
    public static final int DAYS = 5;

    private long milliseconds;

    public Time(int units, int type) {
        switch (type) {
            case MILLISECONDS:
                milliseconds = milliseconds(units);
                return;

            case SECONDS:
                milliseconds = seconds(units);
                return;

            case MINUTES:
                milliseconds = minutes(units);
                return;

            case HOURS:
                milliseconds = hours(units);
                return;

            case DAYS:
                milliseconds = days(units);
                return;

            default:
                throw new IllegalArgumentException("Ugyldig type " + type);
        }
    }

    public long getMilliseconds() {
        return milliseconds;
    }

    public static long milliseconds(int i) {
        return i;
    }

    public static long seconds(int i) {
        return milliseconds(1000) * i;
    }

    public static long minutes(int i) {
        return seconds(60) * i;
    }

    public static long hours(int i) {
        return minutes(60) * i;
    }

    public static long days(int i) {
        return hours(24) * i;
    }
}
