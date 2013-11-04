package com.timepoorprogrammer.saml.impls.myview.consumer.servlet;

import com.timepoorprogrammer.remote.client.*;
import com.timepoorprogrammer.saml.common.AuditMessages;
import com.timepoorprogrammer.saml.configuration.ConfigurationProperties;
import com.timepoorprogrammer.saml.configuration.ConsumerConfiguration;
import com.timepoorprogrammer.saml.configuration.ConsumerRedirectionConfiguration;
import com.timepoorprogrammer.saml.configuration.EntityTranslation;
import com.timepoorprogrammer.saml.core.IOHelper;
import com.timepoorprogrammer.saml.core.SAML11Handler;
import com.timepoorprogrammer.saml.core.SAMLAssertionValidationResult;
import com.timepoorprogrammer.saml.core.SAMLResponseValidationResult;
import com.timepoorprogrammer.saml.impls.MetaDataHandler;
import com.timepoorprogrammer.saml.impls.MetaDataHandlerFactory;
import com.timepoorprogrammer.saml.impls.SAML11AssertionConsumerProcessor;
import com.timepoorprogrammer.saml.impls.SAML11AssertionConsumerProcessorFactory;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.binding.decoding.HTTPPostDecoder;
import org.opensaml.saml1.core.Assertion;
import org.opensaml.saml1.core.Response;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * SAML1.1 assertion consumer for MyView. Note SAML1.1 does not transport encrypted assertions.
 *
 * @author Jim Ball
 */
public class SAML11AssertionConsumerServlet extends HttpServlet {
    private static final long serialVersionUID = 3722318194351693580L;
    private static final Logger log = LoggerFactory.getLogger(SAML11AssertionConsumerServlet.class);

    /**
     * The name of the assertion consumer we are
     */
    private static final String ASSERTION_CONSUMER_NAME = "MyView";

    /**
     * Properties file used by the assertion consumer
     */
    private static final String PROPERTIES_FILE = "saml.properties";

    /**
     * Properties file holding lookup details between SAML Issuer details and our Northgate internal
     * customer code.
     */
    private static final String ENTITY_TRANSLATION_FILE = "samlentitytranslation.properties";

    /**
     * SAML2 handler
     */
    @SuppressWarnings({"NonSerializableFieldInSerializableClass"})
    private SAML11Handler samlHandler = null;

    /**
     * Input and output helper
     */
    @SuppressWarnings({"NonSerializableFieldInSerializableClass"})
    IOHelper ioHelper;

    /**
     * Initialise the servlet setting up a SAML handler.
     *
     * @throws javax.servlet.ServletException on servlet error
     */
    public void init() throws ServletException {
        super.init();
        try {
            samlHandler = new SAML11Handler();
            ioHelper = new IOHelper();
        } catch (Exception anyE) {
            final String errorDetails = AuditMessages.ConsumerCode.CONSUMER_INIT_ERROR.getDetailsPattern();
            log.error(errorDetails, anyE);
            throw new ServletException(errorDetails, anyE);
        }
    }

    /**
     * We don't do GET requests.
     *
     * @param request  request
     * @param response response
     * @throws javax.servlet.ServletException on servlet error
     * @throws java.io.IOException            on IO error
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String errorDetails = AuditMessages.ConsumerCode.CONSUMER_HTTP_TYPE_ERROR.getDetailsPattern();
        log.error(errorDetails);
        response.sendError(HttpServletResponse.SC_FORBIDDEN, errorDetails);
    }

    /**
     * Process POST request holding signed SAML1.1 response body holding an assertion.
     *
     * @param request  request
     * @param response response
     * @throws javax.servlet.ServletException on servlet error
     * @throws java.io.IOException            on IO error
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Decode the message
            SAMLMessageContext context = new BasicSAMLMessageContext();
            context.setInboundMessageTransport(new HttpServletRequestAdapter(request));
            HTTPPostDecoder messageDecoder = new HTTPPostDecoder();
            messageDecoder.decode(context);

            // Get the response and application URL destination
            final Response samlResponse = (Response) context.getInboundMessage();
            // In SAML1.1, the parameter passed across in the POST is called TARGET but is retrieved as the common
            // context attribute RelayState by the OpenSAML library.
            final String relayState = context.getRelayState();

            // Show the application URL this consumer is expected to redirect to
            log.debug("We have a response with relay state (application URL to go to) of: {}", relayState);

            // SAML1.1 doesn't support encrypted assertions, so we need to get the issuer details from the assertion
            Assertion gotAssertion = samlResponse.getAssertions().get(0);
            if (gotAssertion != null) {
                final String issuer = gotAssertion.getIssuer();
                if (issuer != null) {
                    // See if we have a customer code for this user that differs from the provided Issuer details string
                    final EntityTranslation lookup = new EntityTranslation(ioHelper.buildAppServerFilePath(ENTITY_TRANSLATION_FILE));
                    final String customerCode = lookup.lookupInternalCodeUsingEntityIdentifier(issuer);

                    // Allow for configuration change by picking up our properties and metadata configuration now.
                    final ConfigurationProperties props = new ConfigurationProperties(ioHelper.buildAppServerFilePath(PROPERTIES_FILE));
                    final ConsumerConfiguration config = new ConsumerConfiguration(props, ASSERTION_CONSUMER_NAME);
                    // Our redirection configuration depends on the internal Northgate customer code
                    final ConsumerRedirectionConfiguration redirectConfig = new ConsumerRedirectionConfiguration(props, customerCode, ASSERTION_CONSUMER_NAME);
                    final MetaDataHandler mdHandler = MetaDataHandlerFactory.getInstance(null);
                    final String mdFilePath = ioHelper.buildAppServerFilePath(config.getMetadataFileName());

                    // Create a consumer processor which does no decryption (as SAML1.1 doesn't support it) using the internal customerCode as
                    // a means of looking up the right implementation.
                    SAML11AssertionConsumerProcessor consumerProcessor =
                            SAML11AssertionConsumerProcessorFactory.getInstance(mdFilePath, issuer, customerCode,
                                    SAMLConstants.SAML11P_NS, ASSERTION_CONSUMER_NAME, mdHandler);

                    // Check the response for validity (for example a single use policy)
                    SAMLResponseValidationResult responseResult = consumerProcessor.validate(samlResponse);
                    if (!responseResult.isValid()) {
                        final String errorDetails = responseResult.getErrorDetails();
                        log.error(errorDetails);
                        consumerProcessor.auditError(AuditMessages.ConsumerCode.CONSUMER_RESPONSE_CONTENT_ERROR.name(), errorDetails);
                        ConsumerServletHelper.onErrorRedirect(redirectConfig.getErrorUrl(),
                                AuditMessages.ConsumerCode.CONSUMER_RESPONSE_CONTENT_ERROR.name(),
                                errorDetails, response);
                        return;
                    }

                    // Does the identity provider sign the content it sends us, it should do, and if it doesn't
                    // we reject the request
                    if (consumerProcessor.idpSignsMessages()) {
                        log.debug("Issuer " + issuer + " signs its SAML content according to our shared metadata, checking signature");
                        if (!consumerProcessor.isSignatureGood(samlResponse.getSignature())) {
                            final String errorDetails = AuditMessages.ConsumerCode.CONSUMER_SIGNATURE_ERROR.getDetailsPattern();
                            consumerProcessor.auditError(AuditMessages.ConsumerCode.CONSUMER_SIGNATURE_ERROR.name(), errorDetails);
                            ConsumerServletHelper.onErrorRedirect(redirectConfig.getErrorUrl(),
                                    AuditMessages.ConsumerCode.CONSUMER_SIGNATURE_ERROR.name(),
                                    errorDetails, response);
                            return;
                        }
                    }

                    // Validate the contents of the assertion
                    SAMLAssertionValidationResult assertionResult = consumerProcessor.validate(gotAssertion, issuer);
                    if (!assertionResult.isValid()) {
                        final String errorDetails = assertionResult.getErrorDetails();
                        log.error(errorDetails);
                        consumerProcessor.auditError(AuditMessages.ConsumerCode.CONSUMER_INVALID_ASSERTION_ERROR.name(), errorDetails);
                        ConsumerServletHelper.onErrorRedirect(redirectConfig.getErrorUrl(),
                                AuditMessages.ConsumerCode.CONSUMER_INVALID_ASSERTION_ERROR.name(),
                                errorDetails, response);
                        return;
                    }

                    // Get the user identifier
                    final String userIdentifier = gotAssertion.getAuthenticationStatements().get(0).getSubject().getNameIdentifier().getNameIdentifier();
                    log.debug("User Identifier is {}", userIdentifier);
                    // Show the assertion content in all its glory
                    samlHandler.printToFile(gotAssertion, null);

                    // Authorise at the remote application.
                    HttpClientAuthoriserHandler appHandler = new HttpClientAuthoriserHandler();
                    final RedirectionDetails redirectionDetails = appHandler.authorise(redirectConfig, userIdentifier, relayState);
                    if (redirectionDetails.sessionCreated()) {
                        RedirectionSetup redirectionSetup = RedirectionValidator.validate(redirectionDetails, redirectConfig);
                        if (redirectionSetup.isValid()) {
                            consumerProcessor.auditSuccess(AuditMessages.ConsumerCode.CONSUMER_SUCCESS.name(),
                                    String.format(AuditMessages.ConsumerCode.CONSUMER_SUCCESS.getDetailsPattern(),
                                            userIdentifier, ASSERTION_CONSUMER_NAME, redirectConfig.getServiceUrl(),
                                            relayState, redirectConfig.getBaseUrl()));
                            // Redirect the user's browser to the application's SAML backdoor.
                            ConsumerServletHelper.onSuccessRedirect(redirectConfig.getBaseUrl(), response, redirectionDetails);
                        } else {
                            final String errorDetails = redirectionSetup.getErrorDetails();
                            consumerProcessor.auditError(RedirectionSetup.REDIRECTION_SETUP_ERROR, errorDetails);
                            ConsumerServletHelper.onErrorRedirect(redirectConfig.getErrorUrl(),
                                    RedirectionSetup.REDIRECTION_SETUP_ERROR,
                                    errorDetails, response);
                        }
                    } else {
                        final String errorDetails = String.format(AuditMessages.ConsumerCode.CONSUMER_REMOTE_APP_ERROR.getDetailsPattern(), userIdentifier,
                                ASSERTION_CONSUMER_NAME, redirectionDetails.getErrorDetails());
                        consumerProcessor.auditError(AuditMessages.ConsumerCode.CONSUMER_REMOTE_APP_ERROR.name(), errorDetails);
                        ConsumerServletHelper.onErrorRedirect(redirectConfig.getErrorUrl(),
                                AuditMessages.ConsumerCode.CONSUMER_REMOTE_APP_ERROR.name(),
                                errorDetails, response);
                    }
                } else {
                    final String errorDetails = AuditMessages.ConsumerCode.CONSUMER_ASSERTION_ISSUER_ERROR.getDetailsPattern();
                    log.error(errorDetails);
                    // We don't know who it came from so we don't know where to redirect to
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, errorDetails);
                }
            } else {
                final String errorDetails = AuditMessages.ConsumerCode.CONSUMER_MISSING_ASSERTION_ERROR.getDetailsPattern();
                log.error(errorDetails);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, errorDetails);
            }
        } catch (Exception
                anyE) {
            final String errorDetails = AuditMessages.ConsumerCode.CONSUMER_RESPONSE_CONTENT_ERROR.getDetailsPattern() + anyE.getMessage();
            log.error(errorDetails, anyE);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, errorDetails);
        }
    }
}
