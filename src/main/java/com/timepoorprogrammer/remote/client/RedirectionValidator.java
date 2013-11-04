package com.timepoorprogrammer.remote.client;

import com.timepoorprogrammer.saml.configuration.ConsumerRedirectionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Redirection validator given feedback from a remote Authoriser service implementation
 *
 * @author Jim Ball
 */
public class RedirectionValidator {
    private static final Logger log = LoggerFactory.getLogger(RedirectionValidator.class);

    public static final String OLD_VERSION_SESSION_COOKIE = "JSESSIONID";
    public static final String NEW_VERSION_SESSION_COOKIE = "AUTHSERVICE";

    private static final String HTTPS_TRANSPORT = "https";

    /**
     * Validate the redirection configuration setup given feedback from the remote service that tells us whether
     * we are talking to a "new" implementation that uses AUTHSERVICE cookie or an "old" implementation that uses
     * JSESSIONID cookie.
     *
     * @param feedback feedback from remote Authoriser service implementation
     * @param config   redirection configuration
     * @return validation results for the redirection setup
     */
    public static RedirectionSetup validate(final RedirectionDetails feedback, final ConsumerRedirectionConfiguration config) {
        if (feedback == null || config == null) {
            throw new IllegalArgumentException("We cannot validate the redirection configuration without feedback and configuration");
        }
        RedirectionSetup validationResults = new RedirectionSetup();
        if (config.getBaseUrl().startsWith(HTTPS_TRANSPORT)) {
            if (!config.getSessionCookieSecureFlag()) {
                validationResults.addError("We are using HTTPS for customer "
                        + config.getCustomerCode() + " to service "
                        + config.getServiceCode() +
                        ", so the sessionCookieSecureFlag needs to be true");
            }
            if (!config.getArrowPointCookieHttpOnlyFlag()) {
                validationResults.addError("We are using HTTPS for customer "
                        + config.getCustomerCode() + " to service "
                        + config.getServiceCode() + " so the arrowPointCookieSecureFlag needs to be true");
            }
        }
        if (config.getSessionCookieDomain().startsWith(".")) {
            log.warn("The sessionCookieDomain for customer " + config.getCustomerCode() + " to service "
                    + config.getServiceCode() + " starts with a \".\", so is not to RFC http://www.rfc-editor.org/rfc/rfc6265.txt standards.");
        }
        if (!config.getSessionCookiePath().startsWith("/") || !config.getSessionCookiePath().endsWith("/")) {
            validationResults.addError("The sessionCookiePath for customer " + config.getCustomerCode() + " to service "
                    + config.getServiceCode() + " needs to start and end with \"/\"");
        }
        if (feedback.isOldVersionOfAuthoriser()) {
            if (!config.getSessionCookieName().equals(OLD_VERSION_SESSION_COOKIE)) {
                validationResults.addError("The remote Authoriser service for customer " + config.getCustomerCode()
                        + " to service " + config.getServiceCode() + " is an \"old\" type of service, so the sessionCookieName must be set to " + OLD_VERSION_SESSION_COOKIE);
            }
        } else {
            if (!config.getSessionCookieName().equals(NEW_VERSION_SESSION_COOKIE)) {
                validationResults.addError("The remote Authoriser service for customer " + config.getCustomerCode()
                        + " to service " + config.getServiceCode() + " is a \"new\" type of service, so the sessionCookieName must be set to " + NEW_VERSION_SESSION_COOKIE);
            }
        }
        if (!config.getSessionCookieHttpOnlyFlag()) {
            log.warn("You realise the sessionCookieHttpOnlyFlag for customer " + config.getCustomerCode()
                    + "to service " + config.getServiceCode()
                    + " is false? This means JavaScript could be used to read our cookies.  Are you sure you want to allow this security risk?");
        }
        if (config.getArrowPointCookieDomain().startsWith(".")) {
            log.info("The arrowPointCookieDomain for customer " + config.getCustomerCode() + " to service "
                    + config.getServiceCode() + " starts with a \".\", so is not to RFC http://www.rfc-editor.org/rfc/rfc6265.txt standards");
        }
        if (!config.getArrowPointCookiePath().startsWith("/") || !config.getArrowPointCookiePath().endsWith("/")) {
            validationResults.addError("The arrowPointCookiePath for customer " + config.getCustomerCode() + " to service "
                    + config.getServiceCode() + " needs to start and end with \"/\"");
        }
        if (!config.getArrowPointCookieHttpOnlyFlag()) {
            log.warn("You realise the arrowPointCookieHttpOnlyFlag for customer " + config.getCustomerCode()
                    + "to service " + config.getServiceCode()
                    + " is false? This means JavaScript could be used to read our cookies.  Are you sure you want to allow this security risk?");
        }
        return validationResults;
    }
}
