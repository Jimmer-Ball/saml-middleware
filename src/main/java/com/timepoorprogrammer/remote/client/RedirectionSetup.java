package com.timepoorprogrammer.remote.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Redirection setup class to hold details of ALL the errors within the redirection configuration
 * being used given the feedback from the remote service.
 *
 * @author Jim Ball
 */
public class RedirectionSetup {
    public static final String REDIRECTION_SETUP_ERROR = "Redirection setup error";
    private static final String NL = System.getProperty("line.separator");
    private List<String> errors = new ArrayList<String>(0);
    private boolean isValid = true;

    /**
     * Get the full set of errors with the redirection configuration given feedback from our remote Authoriser service
     * implementation.
     *
     * @return complete listing of any problems with the redirection configuration
     */
    public String getErrorDetails() {
        if (!errors.isEmpty()) {
            StringBuilder buf = new StringBuilder();
            for (String error : errors) {
                buf = buf.append(error).append(NL);
            }
            return buf.toString();
        } else {
            return null;
        }
    }

    public void addError(final String errorMessage) {
        if (errorMessage == null) {
            throw new IllegalArgumentException("Cannot add an empty error message to the list of errors");
        }
        isValid = false;
        this.errors.add(errorMessage);
    }

    /**
     * Is the redirection configuration in error given the feedback we got
     *
     * @return true if the redirection configuration has errors
     */
    public boolean isValid() {
        return isValid;
    }
}
