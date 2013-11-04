package com.timepoorprogrammer.saml.soak;

import java.io.Serializable;
import java.util.Date;

/**
 * Soak test results updated as we go.
 *
 * @author Jim Ball
 */
public class SoakResults implements Serializable {
    private static final long serialVersionUID = -5476818243895575313L;

    private long numberOfCalls = 0;
    private long numberOfSuccessfullCalls = 0;
    private long numberOfErrorCalls = 0;
    private Date startTime;
    private Date endTime;
    private long sleepIntervalApplied = 0;
    private int expectedRatePerSecond = 0;

    public long getNumberOfCalls() {
        return numberOfCalls;
    }

    public void setNumberOfCalls(long numberOfCalls) {
        this.numberOfCalls = numberOfCalls;
    }

    public long getNumberOfSuccessfullCalls() {
        return numberOfSuccessfullCalls;
    }

    public void setNumberOfSuccessfullCalls(long numberOfSuccessFullCalls) {
        this.numberOfSuccessfullCalls = numberOfSuccessFullCalls;
    }

    public long getNumberOfErrorCalls() {
        return numberOfErrorCalls;
    }

    public void setNumberOfErrorCalls(long numberOfErrorCalls) {
        this.numberOfErrorCalls = numberOfErrorCalls;
    }

    /**
     * Provide UTC date, not the standard corrected to the locale malarky.
     *
     * @return UTC date
     */
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public long getSleepIntervalApplied() {
        return sleepIntervalApplied;
    }

    public void setSleepIntervalApplied(long sleepIntervalApplied) {
        this.sleepIntervalApplied = sleepIntervalApplied;
    }

    public int getExpectedRatePerSecond() {
        return expectedRatePerSecond;
    }

    public void setExpectedRatePerSecond(int expectedRatePerSecond) {
        this.expectedRatePerSecond = expectedRatePerSecond;
    }

    /**
     * Work out the elapsed time on behalf of the user
     *
     * @return String representing the total elapsed time in Days, Hours. Minutes, Seconds, and milliseconds
     */
    public String getElapsedTime() {
        final StringBuffer buffer = new StringBuffer();
        final long difference = endTime.getTime() - startTime.getTime();
        int wholeDays = (int) difference / SoakConstants.PER_DAY;
        addToBuffer(true, wholeDays, buffer, SoakConstants.DAY);
        int wholeHours = ((int) difference / SoakConstants.PER_HOUR) % SoakConstants.HOURS_IN_A_DAY;
        addToBuffer(false, wholeHours, buffer, SoakConstants.HOUR);
        int wholeMinutes = ((int) difference / SoakConstants.PER_MINUTE) % SoakConstants.MINUTES_IN_AN_HOUR;
        addToBuffer(false, wholeMinutes, buffer, SoakConstants.MINUTE);
        int wholeSeconds = ((int) difference / SoakConstants.PER_SECOND) % SoakConstants.SECONDS_IN_A_MINUTE;
        addToBuffer(false, wholeSeconds, buffer, SoakConstants.SECOND);
        int milliseconds = (int) difference % SoakConstants.PER_SECOND;
        addToBuffer(false, milliseconds, buffer, SoakConstants.MILLISECOND);
        return buffer.toString();
    }

    /**
     * Get the average rate per second of successfull calls made
     *
     * @return average rate per second of successfull calls made
     */
    public String getAverageSuccessRatePerSecond() {
        final double seconds = (endTime.getTime() - startTime.getTime()) / 1000;
        final double ratePerSecond = numberOfSuccessfullCalls / seconds;
        return Double.toString(ratePerSecond);
    }

    public void incrementNumberOfCalls() {
        numberOfCalls++;
    }

    /**
     * Each time there is a successfull call, increment the success total
     */
    public void incrementNumberOfSuccessfulCalls() {
        numberOfSuccessfullCalls++;
    }

    /**
     * Each time there is an error call, increment the error total
     */
    public void incrementNumberOfErrorCalls() {
        numberOfErrorCalls++;
    }

    /**
     * Add the metric (Days, Hours, Minutes, Seconds, Milliseconds) to the elapsed total.
     *
     * @param initialMetric Is this the initial metric in the elapsed total
     * @param wholeNumber   whole number value
     * @param buffer        buffer holding elpased total
     * @param metric        what metric is this (days, hours, minutes, seconds, milliseconds)
     */
    private void addToBuffer(final boolean initialMetric,
                             final int wholeNumber,
                             StringBuffer buffer,
                             final String metric) {
        if (initialMetric) {
            if (wholeNumber > 1) {
                buffer.append(wholeNumber).append(" ").append(metric).append(SoakConstants.PLURAL);
            } else if (wholeNumber == 1) {
                buffer.append(wholeNumber).append(" ").append(metric);
            }
        } else {
            if (wholeNumber > 1) {
                if (buffer.length() > 0) {
                    buffer.append(", ").append(wholeNumber).append(" ").append(metric).append(SoakConstants.PLURAL);
                } else {
                    buffer.append(wholeNumber).append(" ").append(metric).append(SoakConstants.PLURAL);
                }
            } else if (wholeNumber == 1) {
                if (buffer.length() > 0) {
                    buffer.append(", ").append(wholeNumber).append(" ").append(metric);
                } else {
                    buffer.append(wholeNumber).append(" ").append(metric);
                }
            }
        }
    }

    public String toString() {
        return "SoakResults{" +
                "numberOfCalls=" + numberOfCalls +
                ", numberOfSuccessfullCalls=" + numberOfSuccessfullCalls +
                ", numberOfErrorCalls=" + numberOfErrorCalls +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", sleepIntervalApplied=" + sleepIntervalApplied +
                ", expectedRatePerSecond=" + expectedRatePerSecond +
                '}';
    }
}
