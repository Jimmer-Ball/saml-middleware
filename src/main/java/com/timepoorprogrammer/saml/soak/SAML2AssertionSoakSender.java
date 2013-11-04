package com.timepoorprogrammer.saml.soak;

import com.timepoorprogrammer.saml.common.AuditMessages;
import com.timepoorprogrammer.saml.core.SAML2Handler;
import com.timepoorprogrammer.saml.impls.SAML2AssertionProducerProcessor;
import com.timepoorprogrammer.saml.security.encryption.AsymmetricalSessionKeySAMLEncrypter;
import com.timepoorprogrammer.remote.client.SecureProtocolSocketFactory;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.protocol.Protocol;
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

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;

public class SAML2AssertionSoakSender implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(SAML2AssertionSoakSender.class);

    /**
     * HTTP client dependencies
     */
    private static final HttpConnectionManager httpClientConnectionManager;
    private HttpClient httpClient;
    private PostMethod postMethod;

    /**
     * SAML processor and handler
     */
    private SAML2AssertionProducerProcessor processor;
    private SAML2Handler handler;

    /**
     * Session, results and loop handling
     */
    private ControlParameters controlParameters;
    private HttpSession session;
    private boolean keepGoing = true;
    private long sleepInterval = 0;
    private long userCount = 1;
    private static final String OK = "OK";

    /**
     * Object lock for managing access to the stop signal
     */
    private final Object lock = new Object();

    /**
     * Initialise the connection manager in order to have a connection pool we can grab potentially re-usable
     * connections from for performance reasons.
     */
    static {
        httpClientConnectionManager = new MultiThreadedHttpConnectionManager();
        HttpConnectionManagerParams connectionManagerParams = new HttpConnectionManagerParams();
        connectionManagerParams.setDefaultMaxConnectionsPerHost(50);
        connectionManagerParams.setConnectionTimeout(5000);
        httpClientConnectionManager.setParams(connectionManagerParams);
        Protocol.registerProtocol("https", new Protocol("https", new SecureProtocolSocketFactory(), 443));
    }

    /**
     * Setup a soak sender
     *
     * @param controlParameters control parameters
     * @param processor         SAML processor
     * @param handler           SAML handler
     * @param session           session
     */
    public SAML2AssertionSoakSender(final ControlParameters controlParameters,
                                    final SAML2AssertionProducerProcessor processor,
                                    final SAML2Handler handler,
                                    HttpSession session) {
        this.controlParameters = controlParameters;
        this.processor = processor;
        this.handler = handler;
        this.session = session;
        sleepInterval = this.controlParameters.getSleepInterval();
        httpClient = new HttpClient(httpClientConnectionManager);
        postMethod = new PostMethod(processor.getDestination());
    }

    public void run() {
        log.info("Inside SAML2 soak sender run method");
        try {
            // Prime the results data
            final SoakResults results = new SoakResults();
            results.setSleepIntervalApplied(sleepInterval);
            results.setExpectedRatePerSecond(controlParameters.getRateValue());
            final Date startTime = new Date();
            long endTimestamp = controlParameters.getExpectedEndTimestamp(startTime);
            results.setStartTime(startTime);

            // Loop per sleepInterval and check each time round the loop if
            // we have either been stopped manually or been stopped due to
            // reaching the end time.
            while (keepGoing) {
                Thread.sleep(sleepInterval);
                if (System.currentTimeMillis() < endTimestamp) {
                    final String encodedPayload = getEncodedData();
                    // Set both of the required HTTP POST parameters in the request
                    // sent to the remote consumer
                    final NameValuePair[] data = {
                            new NameValuePair("RelayState", ""),
                            new NameValuePair("SAMLResponse", encodedPayload)};
                    postMethod.setRequestBody(data);
                    postMethod.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
                    httpClient.executeMethod(postMethod);
                    final int httpStatusCode = postMethod.getStatusCode();
                    results.incrementNumberOfCalls();
                    if (httpStatusCode == HttpServletResponse.SC_OK) {
                        final String processingStatus = postMethod.getResponseBodyAsString();
                        if (processingStatus.equals(OK)) {
                            results.incrementNumberOfSuccessfulCalls();
                        } else {
                            results.incrementNumberOfErrorCalls();
                        }
                    } else {
                        results.incrementNumberOfErrorCalls();
                    }
                    postMethod.releaseConnection();
                } else {
                    stop();
                }
            }

            // Finalise the test results add the results to the session and tell our
            // controller we've stopped processing
            results.setEndTime(new Date());
            log.info("Results are " + results.toString());
            session.setAttribute(SoakConstants.RESULTS, results);
            SourceServlet.setTestRunning(false);
        } catch (Exception anyE) {
            final String errorMessage = "Error in runnable run method";
            log.error(errorMessage, anyE);
            throw new RuntimeException(errorMessage, anyE);
        } finally {
            // If there is an error while we are trying to send don't tie up a thread
            postMethod.releaseConnection();
        }
    }

    /**
     * Manually stop the processing
     */
    public void stop() {
        synchronized (lock) {
            keepGoing = false;
        }
    }

    /**
     * Get the appropriate encoded SAML payload data
     *
     * @return SAML payload in Base64
     */
    private String getEncodedData() {
        try {
            final String userIdentifier = Long.toString(userCount++);
            log.debug("New data for user {}", userIdentifier);
            final Assertion assertion = processor.createAuthnAssertion(handler, userIdentifier);
            final Response samlResponse = processor.createResponse(handler);
            Element elem;
            final AsymmetricalSessionKeySAMLEncrypter encrypter = processor.getEncrypter();
            if (encrypter != null) {
                EncryptedAssertion encryptedAssertion = encrypter.encryptAssertion(assertion);
                samlResponse.getEncryptedAssertions().add(encryptedAssertion);
                Signature signature = (Signature) handler.create(Signature.DEFAULT_ELEMENT_NAME);
                processor.finishSignature(signature);
                if (signature != null) {
                    samlResponse.setSignature(signature);
                    elem = Configuration.getMarshallerFactory().getMarshaller(samlResponse).marshall(samlResponse);
                    Signer.signObject(signature);
                } else {
                    final String errorDetails = AuditMessages.ProducerCode.PRODUCER_MISSING_SIGNING_CERTIFICATE.getDetailsPattern();
                    log.error(errorDetails);
                    throw new RuntimeException(errorDetails);
                }
            } else {
                samlResponse.getAssertions().add(assertion);
                elem = Configuration.getMarshallerFactory().getMarshaller(samlResponse).marshall(samlResponse);
            }
            final String samlResponseString = XMLHelper.nodeToString(elem);
            return Base64.encodeBytes(samlResponseString.getBytes());
        } catch (Exception anyE) {
            final String errorMessage = "Error creating new SAML2 payload";
            log.error(errorMessage, anyE);
            throw new RuntimeException(errorMessage, anyE);
        }
    }
}
