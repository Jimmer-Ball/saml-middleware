package com.timepoorprogrammer.saml.impls.ga.common;

import com.timepoorprogrammer.saml.configuration.ConfigurationProperties;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;

/**
 * Custom (bespoke) producer configuration for Goldman Sachs.
 *
 * @author Jim Ball
 */
public class ProducerConfiguration implements Serializable {
    private static final long serialVersionUID = 1370115294523867490L;
    private static final Logger log = LoggerFactory.getLogger(ProducerConfiguration.class);

    /**
     * Shared secret key used for digital signing
     */
    private String sharedKey;
    /**
     * Acceptable time drift in seconds
     */
    private String acceptableTimeDrift;
    /**
     * Module to display to Goldman Sachs users.  This can be null in which case we'll display the main entry page
     * of the target application.
     */
    private String module = null;

    /**
     * Key algorithm to apply for Goldman Sachs when generating a Message Authentication Code (MAC) from
     * the shared key.
     * <p/>
     * A MAC provides a way to check the integrity of information transmitted over an unreliable medium,
     * based on a secret key. Typically, message authentication codes are used between two parties
     * that share a secret key in order to validate information transmitted between these parties.
     */
    public static final String KEY_ALGORITHM = "HmacSHA1";

    /**
     * Get all the properties of the producer and throw if any of them are missing
     *
     * @param properties         properties
     * @param identityProviderId identity provider id (GA)
     */
    public ProducerConfiguration(final ConfigurationProperties properties, final String identityProviderId) {
        if (properties != null && identityProviderId != null) {
            sharedKey = properties.getParameter("saml", identityProviderId, "sharedKey");
            acceptableTimeDrift = properties.getParameter("saml", identityProviderId, "acceptableTimeDrift");
            module = properties.getParameter("saml", identityProviderId, "module");
            if (sharedKey == null || acceptableTimeDrift == null) {
                final String errorMessage = "Key Goldman Sachs (GA) parameter values are missing, check your setup holds all the GA parameters needed to define a Goldman Sachs producer";
                log.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
        } else {
            final String errorMessage = "Configuration properties and/or identityProviderId missing";
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }

    /**
     * Get the shared secret key
     *
     * @return shared key
     */
    public String getSharedKey() {
        return sharedKey;
    }

    public void setSharedKey(String sharedKey) {
        this.sharedKey = sharedKey;
    }

    /**
     * Return the acceptable time drift in seconds
     *
     * @return int representing the acceptable time drift in seconds
     */
    public int getAcceptableTimeDrift() {
        try {
            return Integer.parseInt(acceptableTimeDrift);
        } catch (Exception anyE) {
            final String errorMessage = "Error parsing time drift provided in properties file";
            log.error(errorMessage, anyE);
            throw new RuntimeException(errorMessage, anyE);
        }
    }

    public void setAcceptableTimeDrift(String acceptableTimeDrift) {
        this.acceptableTimeDrift = acceptableTimeDrift;
    }

    /**
     * Generate a Message Authentication Code (MAC) from the shared key, we can subsequently use to compare
     * against a MAC sent with the assertion provided by Goldman Sachs.
     *
     * @param dataString String holding a number of seconds since the epoch value and a user identifier
     *                   munged together
     * @return generated Message Authentication Code (MAC)
     */
    public String generateMAC(final String dataString) {
        try {
            SecretKey macKey = new SecretKeySpec(this.getSharedKey().getBytes(), KEY_ALGORITHM);
            Mac mac = Mac.getInstance(KEY_ALGORITHM);
            mac.init(macKey);
            // TODO: This should not rely on the default character encoding for a platform.
            // But this is the algorithm as supplied by Goldman Sachs, so I have to duplicate it.
            // They should fix it to UTF-8 on their side really. So getBytes("UTF-8");
            byte[] raw = mac.doFinal(dataString.getBytes());
            return new String(Hex.encodeHex(raw));
        } catch (Exception anyE) {
            final String errorMessage = "Error generating MAC for comparison";
            log.error(errorMessage, anyE);
            throw new RuntimeException(errorMessage, anyE);
        }
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    /**
     * What is the shared key and the acceptable time drift applied for Goldman Sachs
     *
     * @return string representation of the producer configuration for Goldman Sachs
     */
    public String toString() {
        return "ProducerConfiguration{" +
                "sharedKey='" + sharedKey + '\'' +
                ", acceptableTimeDrift='" + acceptableTimeDrift + '\'' +
                ", module='" + module + '\'' +
                '}';
    }
}
