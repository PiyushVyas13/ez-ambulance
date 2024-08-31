package com.swasthavyas.emergencyllp.util;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimestampUtility {
    private Timestamp timestamp;
    private Date date;
    private Calendar calendar;

    public TimestampUtility(Timestamp timestamp) {
        this.date = timestamp.toDate();
        this.calendar = Calendar.getInstance();
        this.calendar.setTime(date);
    }

    public TimestampUtility() {

    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
        this.date = timestamp.toDate();
        this.calendar = Calendar.getInstance();
        this.calendar.setTime(date);
    }

    public int getYear() {
        return calendar.get(Calendar.YEAR);
    }

    public int getMonth() {
        return calendar.get(Calendar.MONTH) + 1; // January is 0, so add 1
    }

    public int getDayOfMonth() {
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public int getDayOfWeek() {
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public int getHour() {
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public int getMinute() {
        return calendar.get(Calendar.MINUTE);
    }

    public int getSecond() {
        return calendar.get(Calendar.SECOND);
    }

    public long getEpochMilli() {
        return date.getTime();
    }

    public String getFormattedDate(String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
        return formatter.format(date);
    }

    public boolean isLeapYear() {
        int year = getYear();
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    public int getDayOfYear() {
        return calendar.get(Calendar.DAY_OF_YEAR);
    }

    public int getWeekOfYear() {
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    public int getWeekOfMonth() {
        return calendar.get(Calendar.WEEK_OF_MONTH);
    }

    public String getTimeZone() {
        return calendar.getTimeZone().getID();
    }

    public Date toDate() {
        return this.date;
    }

    public Calendar toCalendar() {
        return this.calendar;
    }

    // Add more methods if needed to extract more date-related information
}
