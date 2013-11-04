package com.timepoorprogrammer.saml.impls.standard.producer.servlet;

import com.timepoorprogrammer.saml.common.AuditMessages;
import com.timepoorprogrammer.saml.configuration.ConfigurationProperties;
import com.timepoorprogrammer.saml.configuration.EntityTranslation;
import com.timepoorprogrammer.saml.configuration.ProducerConfiguration;
import com.timepoorprogrammer.saml.core.IOHelper;
import com.timepoorprogrammer.saml.core.SAML2Handler;
import com.timepoorprogrammer.saml.impls.MetaDataHandler;
import com.timepoorprogrammer.saml.impls.MetaDataHandlerFactory;
import com.timepoorprogrammer.saml.impls.SAML2AssertionProducerProcessor;
import com.timepoorprogrammer.saml.impls.SAML2AssertionProducerProcessorFactory;
import com.timepoorprogrammer.saml.security.encryption.AsymmetricalSessionKeySAMLEncrypter;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * Sample SAML2 assertion producer servlet that uses the SAML library used by the middleware management UI.
 * <p/>
 * The producers are much more likely to change compared to the consumers as they depend on how
 * Aurora will use them eventually to talk out to third-party services (e.g. GoogleApps/Salesforce/etc/etc).
 * It may well be that on sending assertions to remote apps in someone else's domain we don't need to go via
 * middleware deployed in hosting at all, instead we use the best bits of the servlets in this library
 * and update the code in Aurora directly and go from there instead.  Like say from the saml-core library now.
 *
 * @author Jim Ball
 */
public class SAML2AssertionProducerServlet extends HttpServlet {
    private static final long serialVersionUID = 2159106523423108281L;
    /**
     * Logging handle
     */
    private static final Logger log = LoggerFactory.getLogger(SAML2AssertionProducerServlet.class);

    /**
     * Default setting for producerCustomerCode, namely from a SAML metadata point of view, who am I.
     * This can be overridden by a configuration setting to allow a specific customer's back-end to
     * be tested from the middleware without involving the customer, so long as the right metadata
     * and routing information is also provided for the producerCustomerCode set.
     */
    private static final String DEFAULT_WHO_AM_I = "idp_saml2";

    /**
     * SAML properties file
     */
    private static final String PROPERTIES_FILE = "saml.properties";

    /**
     * Properties file holding lookup details between SAML Issuer details and our Northgate internal
     * customer code.
     */
    private static final String ENTITY_TRANSLATION_FILE = "samlentitytranslation.properties";

    /**
     * The JSP that builds and sends the full SAML IdP initiated POST binding details
     */
    private static final String IDP_SEND_JSP = "saml2/sendIdPInitiatedPostBinding.jsp";

    /**
     * SAML2 handler
     */
    @SuppressWarnings({"NonSerializableFieldInSerializableClass"})
    SAML2Handler samlHandler;

    /**
     * IOHelper
     */
    @SuppressWarnings({"NonSerializableFieldInSerializableClass"})
    IOHelper ioHelper;

    /**
     * Setup the SAML library.
     *
     * @throws ServletException on error
     */
    public void init() throws ServletException {
        super.init();
        try {
            // The issuer details are now set once we know what the customer code should be
            samlHandler = new SAML2Handler();
            ioHelper = new IOHelper();
        } catch (Exception anyE) {
            final String errorDetails = AuditMessages.ProducerCode.PRODUCER_INIT_ERROR.getDetailsPattern();
            log.error(errorDetails, anyE);
            throw new ServletException(errorDetails, anyE);
        }
    }

    /**
     * Process GET request.
     *
     * @param request  Servlet request
     * @param response Servlet response
     * @throws ServletException on servlet error
     * @throws IOException
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String errorDetails = AuditMessages.ProducerCode.PRODUCER_HTTP_TYPE_ERROR.getDetailsPattern();
        log.error(errorDetails);
        response.sendError(HttpServletResponse.SC_FORBIDDEN, errorDetails);
    }

    /**
     * Process POST request received from a front-end form
     *
     * @param request  request
     * @param response response
     * @throws ServletException on servlet error
     * @throws IOException      on IOException
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Create a new session or get the existing one if one already exists
        final HttpSession session = request.getSession(true);
        // Get the attributes from the request body then (service name, userIdentifierName, and relay state)
        @SuppressWarnings("unchecked")
        final Map<String, String[]> requestParameters = getTypedMap(request.getParameterMap());
        if (requestParameters != null && requestParameters.containsKey("RelayState")
                && requestParameters.containsKey("userIdentifierName")
                && requestParameters.containsKey("service")) {
            final String serviceCode = getSimpleParameter(requestParameters, "service");
            final String userIdentifier = getSimpleParameter(requestParameters, "userIdentifierName");
            final String relayState = getSimpleParameter(requestParameters, "RelayState");
            log.debug("We have service code name of {}", serviceCode);
            log.debug("We have user identifier name of {}", userIdentifier);
            log.debug("We have relayState parameter of {}", relayState);
            if (userIdentifier != null) {

                // Get hold of our configuration now, as we need to stay adaptable to configuration change
                final ConfigurationProperties props = new ConfigurationProperties(ioHelper.buildAppServerFilePath(PROPERTIES_FILE));

                // Get the correct producer configuration given the calculated producerCode
                final String producerCode = props.getProducerCode(DEFAULT_WHO_AM_I).trim();
                log.debug("ProducerCode is {}", producerCode);
                final ProducerConfiguration config = new ProducerConfiguration(props, producerCode);
                final String mdFilePath = ioHelper.buildAppServerFilePath(config.getMetadataFileName());
                final String privateKeyStorePath = ioHelper.buildAppServerFilePath(config.getKeyStoreName());
                final MetaDataHandler mdHandler = MetaDataHandlerFactory.getInstance(null);

                // Translate the producerCode into the producer entity identifier for outbound SAML and set the issuer
                // details on the SAML handler accordingly.
                final EntityTranslation lookup = new EntityTranslation(ioHelper.buildAppServerFilePath(ENTITY_TRANSLATION_FILE));
                final String issuer = lookup.lookupEntityIdentifierUsingInternalCode(producerCode);
                log.debug("Producer Issuer is {}", issuer);
                // In SAML 1.1 the issuer is just a string, so doesn't require "type" identification in the assertion
                samlHandler.setIssuer(issuer);

                // Get the service entity identifier for outbound SAML given the serviceCode
                final String serviceEntityIdentifier = lookup.lookupEntityIdentifierUsingInternalCode(serviceCode);
                log.debug("Service entity identifier is {}", serviceEntityIdentifier);

                // Setup the assertion producer processor that tells us if we need to encrypt and sign
                // This will react to dynamic changes in the metadata without having to restart so we can
                // take new trust relationships without restarting.  Note that this will get the right producer
                // processor given the destination we are sending to as defined by serviceCode
                SAML2AssertionProducerProcessor producerProcessor =
                        SAML2AssertionProducerProcessorFactory.getInstance(
                                mdFilePath,
                                issuer,
                                SAMLConstants.SAML20P_NS,
                                serviceCode,
                                serviceEntityIdentifier,
                                mdHandler,
                                privateKeyStorePath,
                                config.getKeyStorePassword(),
                                config.getSigningKeyAlias(),
                                config.getSigningKeyPassword());

                // Get our destination URL this assertion producer should send to
                final String destination = producerProcessor.getDestination();

                // Create the SAML2 authentication assertion
                final Assertion assertion = producerProcessor.createAuthnAssertion(samlHandler, userIdentifier);

                // Create the SAML2 response
                final Response samlResponse = producerProcessor.createResponse(samlHandler);

                try {
                    // What we do depends on whether we have to encrypt and sign accroding to ur metadata
                    Element elem;
                    final AsymmetricalSessionKeySAMLEncrypter encrypter = producerProcessor.getEncrypter();
                    if (encrypter != null) {
                        // Add the encrypted assertion to the  SAML response,  create our signature using our
                        // private key for signing and add to the SAML response, and finally sign the SAML response
                        // with our signature.
                        log.debug("Creating signed response holding encrypted assertion");
                        EncryptedAssertion encryptedAssertion = encrypter.encryptAssertion(assertion);
                        samlResponse.getEncryptedAssertions().add(encryptedAssertion);
                        Signature signature = (Signature) samlHandler.create(Signature.DEFAULT_ELEMENT_NAME);
                        producerProcessor.finishSignature(signature);
                        if (signature != null) {
                            samlResponse.setSignature(signature);
                            elem = Configuration.getMarshallerFactory().getMarshaller(samlResponse).marshall(samlResponse);
                            Signer.signObject(signature);
                        } else {
                            final String errorDetails = AuditMessages.ProducerCode.PRODUCER_MISSING_SIGNING_CERTIFICATE.getDetailsPattern();
                            log.error(errorDetails);
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, errorDetails);
                            return;
                        }
                    } else {
                        log.debug("Adding simple assertion (without encryption) to simple response (this better not be a production system as there is no real security here)");
                        samlResponse.getAssertions().add(assertion);
                        elem = Configuration.getMarshallerFactory().getMarshaller(samlResponse).marshall(samlResponse);
                    }

                    // Set session attributes to hold what we are sending to the remote service provider in Base64,
                    // to hold a pretty version for viewing prior to sending the Base64 version, and to hold
                    // or destination URL.
                    final String samlResponseString = XMLHelper.nodeToString(elem);
                    session.setAttribute("prettySAMLResponse", samlHandler.printToString(samlResponse));
                    session.setAttribute("SAMLResponse", Base64.encodeBytes(samlResponseString.getBytes()));
                    session.setAttribute("destination", destination);

                    // Add in the relay state details to hold what should be redirected to
                    if (relayState != null) {
                        session.setAttribute("RelayState", relayState);
                    }
                    producerProcessor.auditSuccess(AuditMessages.ProducerCode.PRODUCER_SUCCESS.name(),
                            String.format(AuditMessages.ProducerCode.PRODUCER_SUCCESS.getDetailsPattern(), userIdentifier,
                                    producerProcessor.getDestination(), serviceCode));

                    // Cart this off to the IdP Initiated POST binding sender ready for transmission by the user
                    // so they can at least see the content they are sending prior to transport.  In a real
                    // IdP scenario we'd need an auto-submit script to force the user's browser to send the
                    // assertion without them needing to manually do it.
                    response.sendRedirect(IDP_SEND_JSP);

                } catch (Exception anyE) {
                    final String errorDetails = AuditMessages.ProducerCode.PRODUCER_GENERIC_ERROR.getDetailsPattern() + anyE.getMessage();
                    log.error(errorDetails, anyE);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, errorDetails);
                }
            } else {
                final String errorDetails = AuditMessages.ProducerCode.PRODUCER_MISSING_IDENTIFIER_ERROR.getDetailsPattern();
                log.error(errorDetails);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, errorDetails);
            }
        } else {
            final String errorDetails = AuditMessages.ProducerCode.PRODUCER_MISSING_CONTEXT_ERROR.getDetailsPattern();
            log.error(errorDetails);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, errorDetails);
        }
    }

    /**
     * Get a typed map of the parameters in the POST body
     *
     * @param map of parameters in the post body
     * @return typed map of parameters
     */
    @SuppressWarnings(value = "unchecked")
    public <T extends Map<?, ?>> T getTypedMap(final Map map) {
        return (T) map;
    }

    /**
     * Get a simple parameter from the POST body, note simple means one with a single value
     *
     * @param requestParameters request parameters
     * @param parameterKey      parameter key
     * @return parameter value or null
     */
    public String getSimpleParameter(Map<String, String[]> requestParameters, final String parameterKey) {
        return requestParameters.get(parameterKey)[0];
    }
}
