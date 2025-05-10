package com.example.createwardrobe.classes;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateHelper {

    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    public static String formatDate(Date date) {
        return dateFormatter.format(date);
    }

    public static Date parseDate(String dateString) {
        try {
            return dateFormatter.parse(dateString);
        } catch (java.text.ParseException e) {
            return null;
        }
    }

    public static Calendar getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static Calendar getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar;
    }

    public static String convertDayNumberToName(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY:    return "Понеділок";
            case Calendar.TUESDAY:   return "Вівторок";
            case Calendar.WEDNESDAY: return "Середа";
            case Calendar.THURSDAY:  return "Четвер";
            case Calendar.FRIDAY:    return "П'ятниця";
            case Calendar.SATURDAY:  return "Субота";
            case Calendar.SUNDAY:    return "Неділя";
            default:                 return "Невідомий день";
        }
    }
}