package com.kony.adminconsole.commons.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility functions on dates
 * 
 * @author Venkateswara Rao Alla
 *
 */

public class DateUtils {

    public static final String PATTERN_MM_DD_YYYY = "MM/dd/yyyy";
    public static final String PATTERN_YYYY_MM_DD = "yyyy-MM-dd";
    public static final String PATTERN_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String PATTERN_YYYY_MM_DD_T_HH_MM_SS = "yyyy-MM-dd'T'HH:mm:ss";

    /**
     * Parses the provided date string to start of day applying specified pattern
     * 
     * @param dateStr
     * @param pattern
     * @return
     * @throws ParseException
     */
    public static Date parseToStartOfDay(String dateStr, String pattern) throws ParseException {

        if (StringUtils.isBlank(dateStr) || StringUtils.isBlank(pattern)) {
            return null;
        }
        SimpleDateFormat dateTimeZoneFormat = new SimpleDateFormat(pattern);
        dateTimeZoneFormat.setLenient(true);
        return dateTimeZoneFormat.parse(dateStr);
    }

    /**
     * Parses the provided date string to end of day applying specified pattern
     * 
     * @param dateStr
     * @param pattern
     * @return
     * @throws ParseException
     */
    public static Date parseToEndOfDay(String dateStr, String pattern) throws ParseException {

        if (StringUtils.isBlank(dateStr) || StringUtils.isBlank(pattern)) {
            return null;
        }
        SimpleDateFormat dateTimeZoneFormat = new SimpleDateFormat(pattern);
        dateTimeZoneFormat.setLenient(true);
        Date date = dateTimeZoneFormat.parse(dateStr);

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }

    /**
     * Parses the provided date string and converts into Server timezone
     * 
     * @param dateTimeStr
     * @param timezoneOffset
     * @return
     * @throws ParseException
     */
    public static String convertLocalDateTimeToServerZone(String dateTimeStr, int timezoneOffset)
            throws ParseException {

        SimpleDateFormat datetimeformatter = new SimpleDateFormat(PATTERN_YYYY_MM_DD_HH_MM_SS);
        Date localDateTime = datetimeformatter.parse(dateTimeStr);

        TimeZone timeZone = TimeZone.getDefault();
        long serverOffset = timeZone.getOffset(System.currentTimeMillis());

        Date dateTimeUTC = new Date(localDateTime.getTime() + (timezoneOffset * 60 * 1000) + serverOffset);

        SimpleDateFormat targetDateTimeFormatter = new SimpleDateFormat(PATTERN_YYYY_MM_DD_T_HH_MM_SS);
        return targetDateTimeFormatter.format(dateTimeUTC);
    }

}
