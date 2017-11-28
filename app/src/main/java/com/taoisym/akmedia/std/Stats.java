package com.taoisym.akmedia.std;

import android.util.Log;


public class Stats {
    private final static boolean debug = false;
    public static boolean log_ellips = false;
    public static boolean log_op_time = false;
    public static boolean log_action = true;
    long ticks;

    public Stats() {
        ticks = System.currentTimeMillis();
    }

    static public void printNow(String action) {
        if (log_op_time && debug)
            Log.e("now", action + " time =" + (System.currentTimeMillis()));
    }

    static public void print(String action) {
        if (log_action && debug)
            Log.e("action", action);
    }

    public void printUsedTime(String action) {
        if (log_ellips && debug)
            Log.e("ticks", action + " ms=" + (System.currentTimeMillis() - ticks));
    }
}
