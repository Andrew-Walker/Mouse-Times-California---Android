package com.andrewnwalker.mousetimes_california;

import org.joda.time.Period;

/**
 * Created by andy500mufc on 28/01/2016.
 */
public class MTString {
    static public String str;

    public MTString(String str) {
        this.str = str;
    }

    static public String convertPeriodToString(Period period) {
        if (period.getDays() >= 1) {
            return "Over a day ago";
        } else if (period.getHours() >= 1) {
            return "Over an hour ago";
        } else if (period.getMinutes() < 2) {
            return "Just now";
        } else {
            return period.getMinutes() + " minutes ago";
        }
    }

    static public String convertWaitTimeToDisplayWaitTime(String waitTime) {
        if (waitTime.equalsIgnoreCase("Open") || waitTime.equalsIgnoreCase("Closed")) {
            return waitTime;
        } else if (waitTime.equalsIgnoreCase("80")) {
            return waitTime + "m+";
        } else {
            return waitTime + "m";
        }
    }
}