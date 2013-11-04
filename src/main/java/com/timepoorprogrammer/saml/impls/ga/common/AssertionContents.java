package com.timepoorprogrammer.saml.impls.ga.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.StringTokenizer;

/**
 * An assertion from Goldman Sachs contains a timestamp holding the number of seconds since the
 * epoch (it is in seconds, not millseconds as it should be really)) and a user identifier to
 * apply at the target application.
 *
 * @author Jim Ball
 */
public class AssertionContents {
    private static final Logger log = LoggerFactory.getLogger(AssertionContents.class);

    private static final String PARSE_SEPARATOR = "|";
    private String receivedSecondsTimestamp;
    private String identifier;

    /**
     * Seperate the input data string into identifier and timestamp details.
     *
     * @param parseMe string to parse
     */
    public AssertionContents(final String parseMe) {
        StringTokenizer tokenizer = new StringTokenizer(parseMe, PARSE_SEPARATOR);
        if (tokenizer.hasMoreElements()) {
            receivedSecondsTimestamp = tokenizer.nextElement().toString();
            if (tokenizer.hasMoreElements()) {
                identifier = tokenizer.nextElement().toString();
                log.debug("Assertion holds timestamp of {} and user identifier of {}", receivedSecondsTimestamp, identifier);
            } else {
                final String errorMessage = "No user identifier provided, access denied";
                log.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
        } else {
            final String errorMessage = "No timestamp provided, access denied";
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }

    /**
     * Return the received number of seconds since the epoch timestamp.
     *
     * @return int representing the number of seconds since the epoch
     */
    public long getReceivedSecondsTimestamp() {
        try {
            return Long.parseLong(receivedSecondsTimestamp);
        } catch (Exception anyE) {
            final String errorMessage = "Error parsing received timestamp to long value";
            log.error(errorMessage, anyE);
            throw new RuntimeException(errorMessage, anyE);
        }
    }

    /**
     * Return true if the assertion contents have arrived within the acceptable time drift
     * false otherwise.
     *
     * @param acceptableTimeDrift The acceptable time drift between now and when the assertion should have been
     *                            produced at the latest.
     * @return true if assertion within time tolerance, false otherwise
     */
    public boolean withinTimeTolerance(final int acceptableTimeDrift) {
        final long currentTimeInSecs = Calendar.getInstance().getTime().getTime() / 1000;
        final long timeDifference = currentTimeInSecs - this.getReceivedSecondsTimestamp();
        return timeDifference <= acceptableTimeDrift;
    }

    public void setReceivedSecondsTimestamp(String receivedTimestamp) {
        this.receivedSecondsTimestamp = receivedTimestamp;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String toString() {
        return "AssertionContents{" +
                "receivedTimestamp='" + receivedSecondsTimestamp + '\'' +
                ", identifier='" + identifier + '\'' +
                '}';
    }
}
