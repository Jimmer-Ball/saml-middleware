package com.timepoorprogrammer.saml.soak;

/**
 * Soak constants
 */
public class SoakConstants {

    /**
     * Common time and date calculation settings
     */
    public static final int MILLISECONDS_IN_A_SECOND = 1000;
    public static final int SECONDS_IN_A_MINUTE = 60;
    public static final int MINUTES_IN_AN_HOUR = 60;
    public static final int HOURS_IN_A_DAY = 24;

    public static final int PER_DAY = MILLISECONDS_IN_A_SECOND * SECONDS_IN_A_MINUTE * MINUTES_IN_AN_HOUR * HOURS_IN_A_DAY;
    public static final int PER_HOUR = MILLISECONDS_IN_A_SECOND * SECONDS_IN_A_MINUTE * MINUTES_IN_AN_HOUR;
    public static final int PER_MINUTE = MILLISECONDS_IN_A_SECOND * SECONDS_IN_A_MINUTE;
    public static final int PER_SECOND = MILLISECONDS_IN_A_SECOND;

    public static final String DAY = "day";
    public static final String HOUR = "hour";
    public static final String MINUTE = "minute";
    public static final String SECOND = "second";
    public static final String MILLISECOND = "millisecond";
    public static final String PLURAL = "s";

    /**
     * Session attributes used by both the servlet and by the SoakSenders
     */
    public static final String RESULTS = "results";
}
