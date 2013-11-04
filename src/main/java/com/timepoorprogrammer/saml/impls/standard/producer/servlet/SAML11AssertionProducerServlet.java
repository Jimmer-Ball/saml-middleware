package com.timepoorprogrammer.saml.impls.standard.producer.servlet;

import com.timepoorprogrammer.saml.common.AuditMessages;
import com.timepoorprogrammer.saml.configuration.ConfigurationProperties;
import com.timepoorprogrammer.saml.configuration.EntityTranslation;
import com.timepoorprogrammer.saml.configuration.ProducerConfiguration;
import com.timepoorprogrammer.saml.core.IOHelper;
import com.timepoorprogrammer.saml.core.SAML11Handler;
import com.timepoorprogrammer.saml.impls.MetaDataHandler;
import com.timepoorprogrammer.saml.impls.MetaDataHandlerFactory;
import com.timepoorprogrammer.saml.impls.SAML11AssertionProducerProcessor;
import com.timepoorprogrammer.saml.impls.SAML11AssertionProducerProcessorFactory;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.Assertion;
import org.opensaml.saml1.core.Response;
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
 * SAML1.1 assertion producer for talking to SAML1.1 consumers (like say SAP systems from our portal).
 * This is used by the middleware management UI.
 * <p/>
 * The producers are much more likely to change compared to the consumers as they depend on how Aurora will them
 * eventually to talk out to third-party SAML1.1 applications it want to embed in the dashboard.  It may well be
 * that on sending assertions to remote apps in someone else's domain we don't
 * need to go via middleware deployed in hosting at all, instead we use the library from Aurora direct and
 * go from there instead (which means incorporating the best parts of the servlet examples in this library
 * into Aurora). Like say from the saml-core library now.
 *
 * @author Jim Ball
 */
public class SAML11AssertionProducerServlet extends HttpServlet {
    private static final long serialVersionUID = -1733949731612569451L;
    /**
     * Logging handle
     */
    private static final Logger log = LoggerFactory.getLogger(SAML11AssertionProducerServlet.class);

    /**
     * Default setting for producerCustomerCode. Namely, from a SAML metadata point of view, who am I?
     */
    private static final String DEFAULT_WHO_AM_I = "idp_saml11";

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
    private static final String IDP_SEND_JSP = "saml11/sendIdPInitiatedPostBinding.jsp";

    /**
     * SAML1.1 handler
     */
    @SuppressWarnings({"NonSerializableFieldInSerializableClass"})
    SAML11Handler samlHandler;

    /**
     * IOHelper
     */
    @SuppressWarnings({"NonSerializableFieldInSerializableClass"})
    IOHelper ioHelper;

    /**
     * Setup the SAML library.
     *
     * @throws javax.servlet.ServletException on error
     */
    public void init() throws ServletException {
        super.init();
        try {
            // The issuer details are now set once we know what the customer code should be
            samlHandler = new SAML11Handler();
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
     * @throws javax.servlet.ServletException on servlet error
     * @throws java.io.IOException
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String errorDetails = AuditMessages.ProducerCode.PRODUCER_HTTP_TYPE_ERROR.getDetailsPattern();
        log.error(errorDetails);
        response.sendError(HttpServletResponse.SC_FORBIDDEN, errorDetails);
    }

    /**
     * Process POST request received from a front-end form and redirect to a display form that holds the contents
     * of the SAML to be posted to a consumer.
     *
     * @param request  request
     * @param response response
     * @throws javax.servlet.ServletException on servlet error
     * @throws java.io.IOException            on IOException
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Create a new session or get the existing one if one already exists
        final HttpSession session = request.getSession(true);
        // Get the attributes from the request body then (serviceUrl and userIdentifierName)
        @SuppressWarnings("unchecked")
        final Map<String, String[]> requestParameters = getTypedMap(request.getParameterMap());
        if (requestParameters != null && requestParameters.containsKey("TARGET")
                && requestParameters.containsKey("userIdentifierName")
                && requestParameters.containsKey("service")) {
            final String serviceCode = getSimpleParameter(requestParameters, "service");
            final String userIdentifier = getSimpleParameter(requestParameters, "userIdentifierName");
            final String TARGET = getSimpleParameter(requestParameters, "TARGET");
            log.debug("We have a target service code parameter of {}", serviceCode);
            log.debug("We have user identifier name of {}", userIdentifier);
            log.debug("We have TARGET parameter of {}", TARGET);
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
                // take new trust relationships without restarting. Note that this will get the right producer
                // processor given the destination we are sending to as defined by serviceCode
                SAML11AssertionProducerProcessor producerProcessor = SAML11AssertionProducerProcessorFactory.getInstance(
                        mdFilePath,
                        issuer,
                        SAMLConstants.SAML11P_NS,
                        serviceCode,
                        serviceEntityIdentifier,
                        mdHandler,
                        privateKeyStorePath,
                        config.getKeyStorePassword(),
                        config.getSigningKeyAlias(),
                        config.getSigningKeyPassword());

                // Create a SAML 1.1 assertion
                final Assertion assertion = producerProcessor.createAuthnAssertion(samlHandler, userIdentifier);

                // Create a SAML 1.1 response
                final Response samlResponse = producerProcessor.createResponse(samlHandler);

                try {
                    // In SAML1.1 we cannot encrypt assertions, all we can do is sign the response holding them
                    // to ensure that if they are tampered with in transit the remote service provider will know.
                    Element elem;
                    Signature signature = (Signature) samlHandler.create(Signature.DEFAULT_ELEMENT_NAME);
                    producerProcessor.finishSignature(signature);
                    if (signature != null) {
                        // Add the assertion to the  SAML response, add the signature to the SAML response, and
                        // finally sign the SAML response with the signature.
                        log.debug("Creating signed response holding assertion");
                        samlResponse.getAssertions().add(assertion);
                        samlResponse.setSignature(signature);
                        elem = Configuration.getMarshallerFactory().getMarshaller(samlResponse).marshall(samlResponse);
                        Signer.signObject(signature);
                    } else {
                        final String errorDetails = AuditMessages.ProducerCode.PRODUCER_MISSING_SIGNING_CERTIFICATE.getDetailsPattern();
                        log.error(errorDetails);
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, errorDetails);
                        return;
                    }

                    // Set session attributes to hold what we are sending to the remote service provider in Base64, and
                    // to hold a pretty version for viewing prior to sending the Base64 version.
                    final String samlResponseString = XMLHelper.nodeToString(elem);
                    session.setAttribute("prettySAMLResponse", samlHandler.printToString(samlResponse));
                    session.setAttribute("SAMLResponse", Base64.encodeBytes(samlResponseString.getBytes()));
                    session.setAttribute("destination", producerProcessor.getDestination());

                    // In SAML1.1 the application URL the identity proivder wants the service provider to redirect to is
                    // called TARGET
                    if (TARGET != null) {
                        session.setAttribute("TARGET", TARGET);
                    }
                    producerProcessor.auditSuccess(AuditMessages.ProducerCode.PRODUCER_SUCCESS.name(),
                            String.format(AuditMessages.ProducerCode.PRODUCER_SUCCESS.getDetailsPattern(),
                                    userIdentifier, producerProcessor.getDestination(), serviceCode));

                    // Cart this off to the IdP Initiated POST binding sender ready for transmission.
                    response.sendRedirect(IDP_SEND_JSP);
                } catch (Exception anyE) {
                    final String errorDetails = AuditMessages.ProducerCode.PRODUCER_GENERIC_ERROR.getDetailsPattern()
                            + anyE.getMessage();
                    producerProcessor.auditError(AuditMessages.ProducerCode.PRODUCER_GENERIC_ERROR.name(), errorDetails);
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
