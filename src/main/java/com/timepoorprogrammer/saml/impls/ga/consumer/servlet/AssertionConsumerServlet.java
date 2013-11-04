package com.timepoorprogrammer.saml.impls.ga.consumer.servlet;

import com.timepoorprogrammer.saml.common.AuditMessages;
import com.timepoorprogrammer.saml.configuration.ConfigurationProperties;
import com.timepoorprogrammer.saml.configuration.ConsumerRedirectionConfiguration;
import com.timepoorprogrammer.saml.core.IOHelper;
import com.timepoorprogrammer.saml.impls.AuditMessenger;
import com.timepoorprogrammer.saml.impls.AuditMessengerFactory;
import com.timepoorprogrammer.saml.impls.ga.common.AssertionContents;
import com.timepoorprogrammer.saml.impls.ga.common.Constants;
import com.timepoorprogrammer.saml.impls.ga.common.ProducerConfiguration;
import com.timepoorprogrammer.saml.impls.ga.consumer.processor.AssertionConsumerProcessor;
import com.timepoorprogrammer.remote.client.ConsumerServletHelper;
import com.timepoorprogrammer.remote.client.RedirectionDetails;
import com.timepoorprogrammer.remote.client.RedirectionSetup;
import com.timepoorprogrammer.remote.client.RedirectionValidator;
import com.timepoorprogrammer.saml.impls.ga.common.AssertionContents;
import com.timepoorprogrammer.saml.impls.ga.common.Constants;
import com.timepoorprogrammer.saml.impls.ga.common.ProducerConfiguration;
import com.timepoorprogrammer.saml.impls.ga.consumer.processor.AssertionConsumerProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Bespoke assertion consumer for Goldman Sachs.
 * <p/>
 * Goldman Sachs creates a Message Authentication Code (MAC) based on a key it shares
 * with other businesses, a MAC which it then sends within a message, in order to assert that
 * the user identifier held within the data part of the message is currently authenticated at
 * Goldman Sachs's portal.
 * <p/>
 * So any inbound message holds a MAC and a dataString which contains two parts, a timestamp
 * (in seconds) since the epoch and a user identifier.
 * <p/>
 * The dataString it sends is neither digitally signed nor digitally encrypted, the assertion
 * validation process simply relies on us comparing the MAC sent in the message with a MAC we
 * generate on reception, given the dataString to ensure validity and trust.
 * <p/>
 * IMHO, this is not a secure mechanism to use for business to business transfer of user
 * identifiers.
 *
 * @author Jim Ball
 */
public class AssertionConsumerServlet extends HttpServlet {
    private static final long serialVersionUID = -865315304546570931L;
    private static final Logger log = LoggerFactory.getLogger(AssertionConsumerServlet.class);

    /**
     * The name of the service exposed to Goldman Sachs by default
     */
    private static final String DEFAULT_SERVICE_NAME = "MyView";

    /**
     * Empty string
     */
    private static final String EMPTY_STRING = "";


    /**
     * We don't do GET requests.
     *
     * @param request  request
     * @param response response
     * @throws javax.servlet.ServletException on servlet error
     * @throws java.io.IOException            on IO error
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String errorDetails = "The Goldman Sachs assertion consumer does not process HTTP GET requests";
        log.error(errorDetails);
        response.sendError(HttpServletResponse.SC_FORBIDDEN, errorDetails);
    }

    /**
     * Process POST request holding data and Message Authentication Code (MAC).
     *
     * @param request  request
     * @param response response
     * @throws ServletException on servlet error
     * @throws IOException      on IO error
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Get the request parameters from the body
            String dataString = request.getParameter(Constants.DATA_STRING);
            String mac = request.getParameter(Constants.MAC);
            // Cope with the fact that during commissioning we can over-ride what service to send to, to help
            // with the commissioning.
            String service;
            final String serviceParameter = request.getParameter(Constants.SERVICE);
            if (serviceParameter != null && !serviceParameter.trim().equals(EMPTY_STRING)) {
                service = serviceParameter;
            } else {
                // By default (when the parameter is missing or empty) we redirect to MyView
                service = DEFAULT_SERVICE_NAME;
            }

            // Get hold of an audit message handler
            final AuditMessenger auditMessenger = AuditMessengerFactory.getInstance(Constants.ASSERTION_PRODUCER_NAME);
            // Get hold of an assertion processor
            final AssertionConsumerProcessor processor = new AssertionConsumerProcessor();

            // Get the IO helper and configuration details
            final IOHelper ioHelper = new IOHelper();
            final ConfigurationProperties props = new ConfigurationProperties(ioHelper.buildAppServerFilePath(Constants.PROPERTIES_FILE));
            final ProducerConfiguration producerConfig = new ProducerConfiguration(props, Constants.ASSERTION_PRODUCER_NAME);
            final ConsumerRedirectionConfiguration redirectConfig = new ConsumerRedirectionConfiguration(props, Constants.ASSERTION_PRODUCER_NAME, service);
            if (dataString != null && !dataString.trim().equals(EMPTY_STRING) && mac != null && !mac.trim().equals(EMPTY_STRING)) {
                // Determine what the MAC should be given the algorithm to apply and the identifier given
                final String generatedMAC = producerConfig.generateMAC(dataString);

                // Check the MAC is as expected
                if (generatedMAC.equals(mac)) {
                    final AssertionContents assertionContents = new AssertionContents(dataString);
                    if (assertionContents.withinTimeTolerance(producerConfig.getAcceptableTimeDrift())) {
                        final String userIdentifier = assertionContents.getIdentifier();
                        // Remember to add the module as configuration to the Goldman Sachs saml.properties else
                        // we will end up redirecting to the main page.
                        final RedirectionDetails redirectionDetails = processor.authorise(redirectConfig, userIdentifier, producerConfig.getModule());
                        if (redirectionDetails.sessionCreated()) {
                            RedirectionSetup redirectionSetup = RedirectionValidator.validate(redirectionDetails, redirectConfig);
                            if (redirectionSetup.isValid()) {
                                auditMessenger.auditSuccess(
                                        AuditMessages.ConsumerCode.CONSUMER_SUCCESS.name(),
                                        Constants.ASSERTION_PRODUCER_NAME, Constants.ASSERTION_PRODUCER_PROTOCOL,
                                        service, String.format(AuditMessages.ConsumerCode.CONSUMER_SUCCESS.getDetailsPattern(),
                                        userIdentifier, service, redirectConfig.getServiceUrl(),
                                        null, redirectConfig.getBaseUrl()));
                                ConsumerServletHelper.onSuccessRedirect(redirectConfig.getBaseUrl(), response, redirectionDetails);
                            } else {
                                final String errorDetails = redirectionSetup.getErrorDetails();
                                auditMessenger.auditError(RedirectionSetup.REDIRECTION_SETUP_ERROR,
                                        Constants.ASSERTION_PRODUCER_NAME, Constants.ASSERTION_PRODUCER_PROTOCOL,
                                        service, errorDetails);
                                ConsumerServletHelper.onErrorRedirect(redirectConfig.getErrorUrl(),
                                        RedirectionSetup.REDIRECTION_SETUP_ERROR,
                                        errorDetails, response);
                            }
                        } else {
                            final String errorDetails = String.format(AuditMessages.ConsumerCode.CONSUMER_REMOTE_APP_ERROR.getDetailsPattern(), userIdentifier,
                                    service, redirectionDetails.getErrorDetails());
                            auditMessenger.auditError(
                                    AuditMessages.ConsumerCode.CONSUMER_REMOTE_APP_ERROR.name(),
                                    Constants.ASSERTION_PRODUCER_NAME, Constants.ASSERTION_PRODUCER_PROTOCOL,
                                    service, errorDetails);
                            ConsumerServletHelper.onErrorRedirect(redirectConfig.getErrorUrl(),
                                    AuditMessages.ConsumerCode.CONSUMER_REMOTE_APP_ERROR.name(),
                                    errorDetails, response);
                        }
                    } else {
                        final String errorMessage = "Assertion is too old, access denied";
                        auditMessenger.auditError(
                                AuditMessages.ConsumerCode.CONSUMER_ASSERTION_INVALID_TIMEFRAME_ERROR.name(),
                                Constants.ASSERTION_PRODUCER_NAME, Constants.ASSERTION_PRODUCER_PROTOCOL,
                                service, errorMessage);
                        ConsumerServletHelper.onErrorRedirect(redirectConfig.getErrorUrl(),
                                AuditMessages.ConsumerCode.CONSUMER_ASSERTION_INVALID_TIMEFRAME_ERROR.name(),
                                errorMessage, response);
                    }
                } else {
                    final String errorMessage = "Content has been altered in transit, access denied";
                    auditMessenger.auditError(AuditMessages.ConsumerCode.CONSUMER_SIGNATURE_ERROR.name(),
                            Constants.ASSERTION_PRODUCER_NAME, Constants.ASSERTION_PRODUCER_PROTOCOL,
                            service, errorMessage);
                    ConsumerServletHelper.onErrorRedirect(redirectConfig.getErrorUrl(),
                            AuditMessages.ConsumerCode.CONSUMER_SIGNATURE_ERROR.name(),
                            errorMessage, response);
                }
            } else {
                final String errorMessage = AuditMessages.ConsumerCode.CONSUMER_INVALID_ASSERTION_ERROR.getDetailsPattern();
                auditMessenger.auditError(AuditMessages.ConsumerCode.CONSUMER_INVALID_ASSERTION_ERROR.name(),
                        Constants.ASSERTION_PRODUCER_NAME, Constants.ASSERTION_PRODUCER_PROTOCOL,
                        service, errorMessage);
                ConsumerServletHelper.onErrorRedirect(redirectConfig.getErrorUrl(),
                        AuditMessages.ConsumerCode.CONSUMER_INVALID_ASSERTION_ERROR.name(),
                        errorMessage, response);
            }
        } catch (Exception anyE) {
            final String errorMessage = "Error caught when processing Goldman Sachs assertion contents: " + anyE.getMessage();
            log.error(errorMessage, anyE);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, errorMessage);
        }
    }
}

