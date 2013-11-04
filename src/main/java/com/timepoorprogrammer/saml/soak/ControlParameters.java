package com.timepoorprogrammer.saml.soak;

import org.opensaml.common.xml.SAMLConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;
import java.util.Date;

/**
 * ControlParameters used for SOAK testing.  This class encapsulates the validation of the parameters too.
 *
 * @author Jim Ball
 */
public class ControlParameters implements Serializable {
    private static final long serialVersionUID = -333930293636137860L;
    private static final Logger log = LoggerFactory.getLogger(ControlParameters.class);

    private static final String IDP_PARAM = "idp";
    private static final String SAML_TYPE_PARAM = "samlType";
    private static final String DURATION_SCALE_PARAM = "durationScale";
    private static final String DURATION_VALUE_PARAM = "durationValue";
    private static final String RATE_VALUE_PARAM = "rateValue";

    private String idp;
    private SamlType samlType;
    private DurationScale durationScale;
    private int durationValue;
    private int rateValue;

    /**
     * Parse the SOAK test parameters for what we need and go from there.
     *
     * @param parameters requestParameters provided
     */
    public ControlParameters(final Map<String, String[]> parameters) {
        idp = validateStringValue(getSimpleParameter(parameters, IDP_PARAM));
        samlType = validateSamlType(getSimpleParameter(parameters, SAML_TYPE_PARAM));
        durationScale = validateDurationScale(getSimpleParameter(parameters, DURATION_SCALE_PARAM));
        durationValue = validateDurationValue(convertToInteger(getSimpleParameter(parameters, DURATION_VALUE_PARAM)));
        rateValue = validateRateValue(convertToInteger(getSimpleParameter(parameters, RATE_VALUE_PARAM)));
    }

    public String getIdp() {
        return idp;
    }

    public void setIdp(String idp) {
        this.idp = idp;
    }

    public SamlType getSamlType() {
        return samlType;
    }

    public void setSamlType(SamlType samlType) {
        this.samlType = samlType;
    }

    public DurationScale getDurationScale() {
        return durationScale;
    }

    public void setDurationScale(DurationScale durationScale) {
        this.durationScale = durationScale;
    }

    public int getDurationValue() {
        return durationValue;
    }

    public void setDurationValue(int durationValue) {
        this.durationValue = durationValue;
    }


    public int getRateValue() {
        return rateValue;
    }

    public void setRateValue(int rateValue) {
        this.rateValue = rateValue;
    }

    public enum Action {
        START,
        END
    }

    public enum SamlType {
        SAML2(SAMLConstants.SAML20P_NS),
        SAML11(SAMLConstants.SAML11P_NS);
        private String protocol;

        SamlType(final String protocol) {
            this.protocol = protocol;
        }

        public String getProtocol() {
            return this.protocol;
        }
    }

    public enum DurationScale {
        SECONDS,
        MINUTES,
        HOURS,
        DAYS
    }

    public enum RateScale {
        PER_SECOND,
        PER_MINUTE,
        PER_HOUR
    }

    /**
     * Get the calculated (and truncated) sleep interval the threads shoulsd apply
     * when sending assertions to the remote consumer.
     *
     * @return sleep interval between sending assertions
     */
    public long getSleepInterval() {
        return SoakConstants.MILLISECONDS_IN_A_SECOND / rateValue;
    }

    /**
     * Get the expected end timestamp for the test
     *
     * @param startTime starttime according to the tester
     * @return expected end time timestamp.
     */
    public long getExpectedEndTimestamp(final Date startTime) {
        long totalNumberOfMilliseconds = 0;
        switch (durationScale) {
            case DAYS:
                totalNumberOfMilliseconds = durationValue
                        * SoakConstants.MILLISECONDS_IN_A_SECOND
                        * SoakConstants.SECONDS_IN_A_MINUTE
                        * SoakConstants.MINUTES_IN_AN_HOUR
                        * SoakConstants.HOURS_IN_A_DAY;
                break;
            case HOURS:
                totalNumberOfMilliseconds = durationValue
                        * SoakConstants.MILLISECONDS_IN_A_SECOND
                        * SoakConstants.SECONDS_IN_A_MINUTE
                        * SoakConstants.MINUTES_IN_AN_HOUR;
                break;
            case MINUTES:
                totalNumberOfMilliseconds = durationValue
                        * SoakConstants.MILLISECONDS_IN_A_SECOND
                        * SoakConstants.SECONDS_IN_A_MINUTE;
                break;
            case SECONDS:
                totalNumberOfMilliseconds = durationValue
                        * SoakConstants.MILLISECONDS_IN_A_SECOND;
                break;
        }
        return startTime.getTime() + totalNumberOfMilliseconds;
    }

    public String toString() {
        return "ControlParameters{" +
                ", idp='" + idp + '\'' +
                ", samlType=" + samlType.name() +
                ", durationScale=" + durationScale.name() +
                ", durationValue=" + durationValue +
                ", rateValue=" + rateValue +
                '}';
    }

    private String getSimpleParameter(Map<String, String[]> requestParameters, final String parameterKey) {
        return requestParameters.get(parameterKey)[0];
    }

    private int convertToInteger(final String convertMe) {
        try {
            return Integer.parseInt(convertMe.trim());
        }
        catch (Exception anyE) {
            final String errorMessage = "Error converting value " + convertMe + "to integer";
            log.error(errorMessage, anyE);
            throw new RuntimeException(errorMessage, anyE);
        }
    }

    private SamlType validateSamlType(final String samlType) {
        if (samlType != null) {
            if (samlType.equalsIgnoreCase("SAML2")) {
                return SamlType.SAML2;
            } else if (samlType.equalsIgnoreCase("SAML1.1")) {
                return SamlType.SAML11;
            } else {
                final String errorMessage = "SAML type provided is invalid, we need SAML1.1 or SAML2";
                log.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
        } else {
            final String errorMessage = "SAML type cannot be null, we need SAML1.1 or SAML2";
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }

    private int validateDurationValue(final int duration) {
        return validateIntValue(duration);
    }

    private int validateRateValue(final int rate) {
        return validateIntValue(rate);
    }

    private int validateIntValue(final int value) {
        if (value >= 0) {
            return value;
        } else {
            final String errorMessage = "Value cannot be zero or less";
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }

    private String validateStringValue(final String value) {
        if (value != null) {
            return value;
        } else {
            final String errorMessage = "Value cannot be null";
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }

    private DurationScale validateDurationScale(final String durationScale) {
        if (durationScale != null) {
            if (durationScale.equalsIgnoreCase("S")) {
                return DurationScale.SECONDS;
            } else if (durationScale.equalsIgnoreCase("M")) {
                return DurationScale.MINUTES;
            } else if (durationScale.equalsIgnoreCase("H")) {
                return DurationScale.HOURS;
            } else if (durationScale.equalsIgnoreCase("D")) {
                return DurationScale.DAYS;
            } else {
                final String errorMessage = "Invalid duration scale given " + durationScale + ", we need S, M, H, or D please.";
                log.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
        } else {
            final String errorMessage = "Null duration scale given, we need S, M, H, or D please.";
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }
}
