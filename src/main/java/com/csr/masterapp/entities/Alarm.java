package com.csr.masterapp.entities;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * 闹钟模型
 * Timer Model
 */
public class Alarm implements Serializable {
    private int id;
    private Calendar alarmTime = Calendar.getInstance();
    private int[] days = new int[0];

    private String description = "execute once";

    public Alarm() {}


    public void addDay(int day) {
        boolean contains = false;
        int[] temp = getDays();
        if(temp.length != 0){
            for (int d : temp)
                if (d == day)
                    contains = true;
        }
        if (!contains) {
            int[] result = new int[temp.length + 1];
            for (int i = 0; i < temp.length; i++) {
                result[i] = temp[i];
            }
            result[temp.length] = day;
            setDays(result);
        }
    }

    public void removeDay(int day) {
        boolean contains = false;
        int[] temp = getDays();
        int[] result = new int[temp.length];
        int xiabiao = temp.length;
        for (int i = 0; i < temp.length; i++) {
            if (temp[i] == day) {
                contains = true;
                result = new int[temp.length - 1];
                xiabiao = i;
            }
        }
        if (contains) {
            for (int i = 0; i < xiabiao; i++) {
                result[i] = temp[i];
            }
            for (int i = xiabiao + 1; i < temp.length; i++) {
                result[i - 1] = temp[i];
            }
            setDays(result);
        }
    }

    public void removeAllDay(){
        this.days = new int[0];
        this.description = "execute once";
    }

    public void addAllDay(){
        int[] result = {Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY,};
        this.days = result;
        this.description = "every day";
    }

    public void workDay(){
        int[] result = { Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY,};
        this.days = result;
        this.description = "work day";
    }

    /**
     * 这一天是否重复
     *
     * @param dayOfWeek
     * @return
     */
    public boolean IsRepeat(int dayOfWeek) {
        if (days == null || days.length < 1)
            return false;
        for (int i = 0; i < days.length; i++) {
            if (days[i] == dayOfWeek)
                return true;
        }
        return false;
    }

    /**
     * @return the alarmTime
     */
    public Calendar getAlarmTime() {
//        if (alarmTime.before(Calendar.getInstance()))
//            alarmTime.add(Calendar.DAY_OF_MONTH, 1);
        return alarmTime;
    }

    /**
     * @return the alarmTime
     */
    public String getAlarmTimeString() {

        String time = "";
        if (alarmTime.get(Calendar.HOUR_OF_DAY) <= 9)
            time += "0";
        time += String.valueOf(alarmTime.get(Calendar.HOUR_OF_DAY));
        time += ":";

        if (alarmTime.get(Calendar.MINUTE) <= 9)
            time += "0";
        time += String.valueOf(alarmTime.get(Calendar.MINUTE));

        return time;
    }

    /**
     * @param alarmTime the alarmTime to set
     */
    public void setAlarmTime(Calendar alarmTime) {
        this.alarmTime = alarmTime;
    }

    /**
     * @param alarmTime the alarmTime to set
     */
    public void setAlarmTime(String alarmTime) {

        String[] timePieces = alarmTime.split(":");
        Calendar newAlarmTime = Calendar.getInstance();
        newAlarmTime.set(Calendar.HOUR_OF_DAY,
                Integer.parseInt(timePieces[0]));
        newAlarmTime.set(Calendar.MINUTE, Integer.parseInt(timePieces[1]));
        newAlarmTime.set(Calendar.SECOND, 0);
        setAlarmTime(newAlarmTime);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int[] getDays() {
        return days;
    }

    public void setDays(int[] days) {
        this.days = days;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getTimeUntilNextAlarmMessage() {
        long timeDifference = getAlarmTime().getTimeInMillis() - System.currentTimeMillis();
        long days = timeDifference / (1000 * 60 * 60 * 24);
        long hours = timeDifference / (1000 * 60 * 60) - (days * 24);
        long minutes = timeDifference / (1000 * 60) - (days * 24 * 60) - (hours * 60);
        long seconds = timeDifference / (1000) - (days * 24 * 60 * 60) - (hours * 60 * 60) - (minutes * 60);
        String alert = "闹钟将会在";
        if (days > 0) {
            alert += String.format("%d day %d hour %d minute %d second", days, hours, minutes, seconds);
        } else {
            if (hours > 0) {
                alert += String.format("%d hour, %d minute %d second", hours, minutes, seconds);
            } else {
                if (minutes > 0) {
                    alert += String.format("%d minute %d second", minutes, seconds);
                } else {
                    alert += String.format("%d second", seconds);
                }
            }
        }
        alert += "remind";
        return alert;
    }


    public String getRepeatDaysString() {
        Map<Integer, String> map = new HashMap<>(7);
        map.put(Calendar.SUNDAY, "Sunday");
        map.put(Calendar.MONDAY, "Monday");
        map.put(Calendar.TUESDAY, "Tuesday");
        map.put(Calendar.WEDNESDAY, "Wednesday");
        map.put(Calendar.THURSDAY, "Thursday");
        map.put(Calendar.FRIDAY, "Friday");
        map.put(Calendar.SATURDAY, "Saturday");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < days.length; i++) {
            if (map.containsKey(days[i])) {
                sb.append(" " + map.get(days[i]));
            }
        }
        this.description = sb.toString();
        return description;
    }
}
