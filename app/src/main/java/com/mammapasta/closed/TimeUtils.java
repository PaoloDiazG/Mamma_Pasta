package com.mammapasta.closed;

import java.util.Calendar;

public class TimeUtils {

    private static final int START_HOUR = 22; // 10 PM
    private static final int END_HOUR = 10;   // 10 AM

    public static boolean isWithinClosedHours() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return (hour >= START_HOUR || hour < END_HOUR);
    }

    public static int getCurrentHour() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    public static int getCurrentMinute() {
        return Calendar.getInstance().get(Calendar.MINUTE);
    }
}
