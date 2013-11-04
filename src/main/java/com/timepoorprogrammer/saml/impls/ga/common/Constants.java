package com.timepoorprogrammer.saml.impls.ga.common;

/**
 * Constants used by both the dummy producer and actual consumer side of Goldman Sachs "roll your own"
 * security.
 *
 * @author Jim Ball
 */
public class Constants {
    /**
     * Data string holder
     */
    public static final String DATA_STRING = "ID_STRING";

    /**
     * Signature holder
     */
    public static final String MAC = "SIGNATURE";

    /**
     * So we can commission the consumer to redirect to whatever service is required 
     */
    public static final String SERVICE = "SERVICE";

    /**
     * The name of the assertion producer for Goldman Sachs, so the hosting applicable company prefix.
     */
    public static final String ASSERTION_PRODUCER_NAME = "GA";

    /**
     * Trust mechanism employed by Goldman Sachs.
     */
    public static final String ASSERTION_PRODUCER_PROTOCOL = "MAC shared key mechanism";

    /**
     * Properties file used by both the assertion producer and the assertion consumer
     */
    public static final String PROPERTIES_FILE = "saml.properties";
}
