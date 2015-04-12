package com.example.chordec.chordec.Helper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by thearith on 11/4/15.
 */
public class Constants {

    public static final int MILLISECONDS_RATE = 1000;
    public static final int HOURS_RATE = 3600;
    public static final int MINUTES_RATE = 60;

    public static final int BUFFER_SIZE = 1024;

    public static final String[] COLORS = {
            "#27ae60",
            "#2980b9",
            "#2c3e50",
            "#8e44ad",
            "#16a085",
            "#d35400",
            "#f39c12",
            "#bdc3c7",
            "#7f8c8d",
            "#3498db",
            "#34495e"
    };

    public static final String CHORD_ID = "com.example.chordec.chordec.CHORD_ID";

    public static String getDateFormat(long milliseconds) {
        Date date = new Date(milliseconds);
        SimpleDateFormat ft = new SimpleDateFormat("MMMM dd yyyy | HH:mm a");

        return ft.format(date);
    }

    public static String getDurationFormat(int duration) {

        int seconds = duration / MILLISECONDS_RATE;

        int hour = seconds / HOURS_RATE;
        int minute = (seconds - hour*HOURS_RATE) / MINUTES_RATE;
        int second = (seconds - minute*MINUTES_RATE);

        String text = second < 10 ?
                minute + ":0" + second : minute + ":" + second;

        return hour == 0 ?
                text : (hour + ":" + text);
    }
}
