package com.timepoorprogrammer.saml.soak;

import com.timepoorprogrammer.saml.common.AuditMessages;
import com.timepoorprogrammer.saml.configuration.ConfigurationProperties;
import com.timepoorprogrammer.saml.configuration.ConsumerConfiguration;
import com.timepoorprogrammer.saml.configuration.EntityTranslation;
import com.timepoorprogrammer.saml.core.IOHelper;
import com.timepoorprogrammer.saml.core.SAML2Handler;
import com.timepoorprogrammer.saml.core.SAMLAssertionValidationResult;
import com.timepoorprogrammer.saml.core.SAMLResponseValidationResult;
import com.timepoorprogrammer.saml.impls.MetaDataHandler;
import com.timepoorprogrammer.saml.impls.MetaDataHandlerFactory;
import com.timepoorprogrammer.saml.impls.SAML2AssertionConsumerProcessor;
import com.timepoorprogrammer.saml.impls.SAML2AssertionConsumerProcessorFactory;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.encryption.Decrypter;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Sink consumer servlet.  This doesn't do any redirection to some mythical back-end application it simply responds
 * with OK or ERROR to a remote soak source servlet bombarding us with assertions.
 *
 * @author Jim Ball
 */
public class SAML2AssertionSinkConsumer extends HttpServlet {
    private static final long serialVersionUID = 3436945589270707491L;
    private static final Logger log = LoggerFactory.getLogger(SAML2AssertionSinkConsumer.class);

    private static final String ASSERTION_CONSUMER_NAME = "SoakConsumer";

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
    public static SAML2Handler GOT_TO_HAVE_ONE_OF_THESE = new SAML2Handler();

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
        writeErrorResponse(response);
    }

    /**
     * Process POST request holding signed SAML response body holding an encrypted assertion.
     *
     * @param request  request
     * @param response response
     * @throws javax.servlet.ServletException on servlet error
     * @throws java.io.IOException            on IO error
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {

            // Decode the response
            SAMLMessageContext context = new BasicSAMLMessageContext();
            context.setInboundMessageTransport(new HttpServletRequestAdapter(request));
            HTTPPostDecoder messageDecoder = new HTTPPostDecoder();
            messageDecoder.decode(context);

            // Check out the validity of the signature given our metadata
            final Response samlResponse = (Response) context.getInboundMessage();
            final String relayState = context.getRelayState();

            // Given the issuer, lookup metadata describing the IdentityProvider who sent us this SAML response
            final String issuer = samlResponse.getIssuer().getValue();

            // See if we have a customer code for this user that differs from the provided Issuer details string
            final EntityTranslation lookup = new EntityTranslation(ioHelper.buildAppServerFilePath(ENTITY_TRANSLATION_FILE));
            final String customerCode = lookup.lookupInternalCodeUsingEntityIdentifier(issuer);

            // Allow for configuration change by picking up our properties and metadata configuration now.
            final ConfigurationProperties props = new ConfigurationProperties(ioHelper.buildAppServerFilePath(PROPERTIES_FILE));
            final ConsumerConfiguration config = new ConsumerConfiguration(props, ASSERTION_CONSUMER_NAME);
            final MetaDataHandler mdHandler = MetaDataHandlerFactory.getInstance(null);
            final String mdFilePath = ioHelper.buildAppServerFilePath(config.getMetadataFileName());
            final String privateKeyStorePath = ioHelper.buildAppServerFilePath(config.getKeyStoreName());

            // Create a consumer processor
            SAML2AssertionConsumerProcessor consumerProcessor =
                    SAML2AssertionConsumerProcessorFactory.getInstance(mdFilePath, issuer, customerCode,
                            SAMLConstants.SAML20P_NS, ASSERTION_CONSUMER_NAME, mdHandler, privateKeyStorePath,
                            config.getKeyStorePassword(), config.getDecryptionKeyAlias(),
                            config.getDecryptionKeyPassword());

            // Check the response for validity (for example a single use policy)
            SAMLResponseValidationResult responseResult = consumerProcessor.validate(samlResponse);
            if (!responseResult.isValid()) {
                consumerProcessor.auditError(AuditMessages.ConsumerCode.CONSUMER_RESPONSE_CONTENT_ERROR.name(), responseResult.getErrorDetails());
                // Return error status
                writeErrorResponse(response);
                return;
            }

            // Check the signature if we need to
            if (consumerProcessor.idpSignsMessages()) {
                if (!consumerProcessor.isSignatureGood(samlResponse.getSignature())) {
                    final String errorDetails = AuditMessages.ConsumerCode.CONSUMER_SIGNATURE_ERROR.getDetailsPattern();
                    consumerProcessor.auditError(AuditMessages.ConsumerCode.CONSUMER_SIGNATURE_ERROR.name(), errorDetails);
                    // Return error status
                    writeErrorResponse(response);
                    return;
                }
            }

            // Are we expecting encrypted assertions?
            Assertion gotAssertion;
            final Decrypter samlDecrypter = consumerProcessor.getDecrypter();
            if (samlDecrypter != null) {
                final EncryptedAssertion encryptedAssertion = samlResponse.getEncryptedAssertions().get(0);
                if (encryptedAssertion != null) {
                    try {
                        // Decrypt the shared session key using the PKI and decrypt the assertion using the unencrypted
                        // shared session key using AES
                        gotAssertion = samlDecrypter.decrypt(encryptedAssertion);
                    } catch (Exception anyE) {
                        final String errorDetails = String.format(AuditMessages.ConsumerCode.CONSUMER_DECRYPTION_ERROR.getDetailsPattern(),
                                anyE.getMessage());
                        consumerProcessor.auditError(AuditMessages.ConsumerCode.CONSUMER_DECRYPTION_ERROR.name(), errorDetails);
                        // Return error status
                        writeErrorResponse(response);
                        return;
                    }
                } else {
                    final String errorDetails = AuditMessages.ConsumerCode.CONSUMER_EXPECTED_ENCRYPTED_ASSERTION_ERROR.getDetailsPattern();
                    consumerProcessor.auditError(AuditMessages.ConsumerCode.CONSUMER_EXPECTED_ENCRYPTED_ASSERTION_ERROR.name(), errorDetails);
                    // Return error status
                    writeErrorResponse(response);
                    return;
                }
            } else {
                gotAssertion = samlResponse.getAssertions().get(0);
            }

            // Validate the contents of the assertion
            SAMLAssertionValidationResult assertionResult = consumerProcessor.validate(gotAssertion, issuer);
            if (!assertionResult.isValid()) {
                consumerProcessor.auditError(AuditMessages.ConsumerCode.CONSUMER_INVALID_ASSERTION_ERROR.name(), assertionResult.getErrorDetails());
                // Return error status
                writeErrorResponse(response);
                return;
            }

            // Indicate success
            final String userIdentifier = gotAssertion.getSubject().getNameID().getValue();
            log.debug("User Identifier is {}", userIdentifier);

            // Return success status
            consumerProcessor.auditSuccess(AuditMessages.ConsumerCode.CONSUMER_SUCCESS.name(),
                    String.format(AuditMessages.ConsumerCode.CONSUMER_SUCCESS.getDetailsPattern(),
                            userIdentifier, ASSERTION_CONSUMER_NAME, "SAML2AssertionSinkConsumer",
                            relayState, "SAML2AssertionSinkConsumer"));
            writeSuccessResponse(response);
        } catch (Exception anyE) {
            final String errorDetails = AuditMessages.ConsumerCode.CONSUMER_CONTENT_ERROR.getDetailsPattern() + anyE.getMessage();
            log.error(errorDetails, anyE);
            // Return error status
            writeErrorResponse(response);
        }
    }

    /**
     * This is how we tell the source SAML2AssertionSoakSender that it did good
     *
     * @param response response
     */
    private void writeSuccessResponse(HttpServletResponse response) {
        try {
            response.setContentType("text/xml");
            PrintWriter out = response.getWriter();
            final String responseText = "OK";
            out.print(responseText);
        } catch (Exception anyE) {
            final String errorMessage = "Error writing success response";
            log.error(errorMessage, anyE);
            throw new RuntimeException(errorMessage, anyE);
        }
    }

    /**
     * If there is a problem processing the assertion sent in we need to let the SAML2AssertionSokaSender know.
     *
     * @param response response
     */
    private void writeErrorResponse(HttpServletResponse response) {
        try {
            response.setContentType("text/xml");
            PrintWriter out = response.getWriter();
            final String responseText = "ERROR";
            out.print(responseText);
        } catch (Exception anyE) {
            final String errorMessage = "Error writing error response";
            log.error(errorMessage, anyE);
            throw new RuntimeException(errorMessage, anyE);
        }
    }
}
