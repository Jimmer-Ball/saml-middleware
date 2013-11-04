package com.timepoorprogrammer.saml.soak;

import com.timepoorprogrammer.saml.common.AuditMessages;
import com.timepoorprogrammer.saml.configuration.ConfigurationProperties;
import com.timepoorprogrammer.saml.configuration.EntityTranslation;
import com.timepoorprogrammer.saml.configuration.ProducerConfiguration;
import com.timepoorprogrammer.saml.core.IOHelper;
import com.timepoorprogrammer.saml.core.SAML11Handler;
import com.timepoorprogrammer.saml.core.SAML2Handler;
import com.timepoorprogrammer.saml.impls.*;
import org.opensaml.common.xml.SAMLConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * Controller servlet for soak testing the SAML producers and consumers addressed by the hidden soak testing page
 * to be found at <path_to_middleware>/SAMLWeb/soakClient
 *
 * @author Jim Ball
 */
public class SourceServlet extends HttpServlet {
    private static final long serialVersionUID = -4806074768838940613L;

    /**
     * Logging handle
     */
    private static final Logger log = LoggerFactory.getLogger(SourceServlet.class);

    /**
     * The remote destination SAML consumer.
     */
    private static final String DESTINATION_SOAK_CONSUMER = "SoakConsumer";

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
     * Action parameter that tells us what to do.
     */
    private static final String ACTION_TYPE_PARAM = "action";

    /**
     * Control parameters on the session
     */
    private static final String CONTROL_PARAMS = "controlParams";

    /**
     * Results presentation
     */
    private static final String RESULTS_JSP = "/soakResults.jsp";

    /**
     * Lock for concurrent access to test running flag
     */
    private static final Object LOCK = new Object();

    /**
     * Is running flag.  It is better here on the class than in a thread-unsafe session.
     * Obviously, on initial class load it needs to be false.
     */
    private static boolean sIsRunning = false;

    /**
     * The SAML11 soak sender instance
     */
    private static SAML11AssertionSoakSender saml11SoakSender;

    /**
     * The SAML2 soak sender instance
     */
    private static SAML2AssertionSoakSender saml2SoakSender;

    private static Thread senderThread;


    /**
     * SAML1.1 handler
     */
    @SuppressWarnings({"NonSerializableFieldInSerializableClass"})
    SAML11Handler saml11Handler;

    /**
     * SAML2 handler
     */
    @SuppressWarnings({"NonSerializableFieldInSerializableClass"})
    SAML2Handler saml2Handler;

    /**
     * IOHelper
     */
    @SuppressWarnings({"NonSerializableFieldInSerializableClass"})
    IOHelper ioHelper;

    public static void setTestRunning(final boolean isRunning) {
        synchronized (LOCK) {
            sIsRunning = isRunning;
        }
    }

    public static boolean getTestRunning() {
        synchronized (LOCK) {
            return sIsRunning;
        }
    }

    /**
     * Setup the SAML library.
     *
     * @throws javax.servlet.ServletException on error
     */
    public void init() throws ServletException {
        super.init();
        try {
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
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            @SuppressWarnings("unchecked")
            final Map<String, String[]> requestParameters = getTypedMap(request.getParameterMap());
            final Action action = validateAction(getSimpleParameter(requestParameters, ACTION_TYPE_PARAM));
            HttpSession session = request.getSession(true);
            final boolean testOngoing = SourceServlet.getTestRunning();
            if (action == Action.START) {
                // Start test if not already running
                if (!testOngoing) {
                    SourceServlet.setTestRunning(true);
                    startTest(requestParameters, session);
                } else {
                    log.info("Ignoring the request to start a test, as one is already running");
                }
            } else if (action == Action.CHECK) {
                // If the test has been stopped already report the results.
                if (!testOngoing) {
                    log.info("Test has been marked as finished, so we will display the results");
                    // Ensure the sender thread is dead
                    senderThread.join();

                    // Pick up the results and add them to the request
                    request.setAttribute(SoakConstants.RESULTS, session.getAttribute(SoakConstants.RESULTS));
                    session.removeAttribute(SoakConstants.RESULTS);
                    forward(request, response, RESULTS_JSP);
                } else {
                    log.info("Test is still going according to the poller");
                }
            } else {
                // Stop current test, and report the results
                if (testOngoing) {
                    log.info("Forceably stopping test");
                    stopTest(session, request);
                    forward(request, response, RESULTS_JSP);
                } else {
                    log.info("Ignoring the request to stop a test as no test is running");
                }
            }
        } catch (Exception anyE) {
            final String errorDetails = "Error in soak control" + anyE.getMessage();
            response.sendError(HttpServletResponse.SC_FORBIDDEN, errorDetails);
        }
    }

    /**
     * Our SoakClient can start a test, check if a test is still going, and end a test.
     */
    public enum Action {
        START,
        CHECK,
        END
    }

    @SuppressWarnings(value = "unchecked")
    private <T extends Map<?, ?>> T getTypedMap(final Map map) {
        return (T) map;
    }

    private String getSimpleParameter(Map<String, String[]> requestParameters, final String parameterKey) {
        return requestParameters.get(parameterKey)[0];
    }

    /**
     * Start the test
     *
     * @param requestParameters parameters sent on the request
     * @param session           HTTP session
     */
    private void startTest(Map<String, String[]> requestParameters, HttpSession session) {
        final ControlParameters controlParameters = new ControlParameters(requestParameters);
        final String producerCode = controlParameters.getIdp();
        log.info("Starting new test with producer code of: " + producerCode);
        session.setAttribute(CONTROL_PARAMS, controlParameters);

        // Get hold of our configuration now, as we need to stay adaptable to configuration change
        final ConfigurationProperties props = new ConfigurationProperties(ioHelper.buildAppServerFilePath(PROPERTIES_FILE));
        final ProducerConfiguration config = new ProducerConfiguration(props, producerCode);
        final MetaDataHandler mdHandler = MetaDataHandlerFactory.getInstance(null);
        final String mdFilePath = ioHelper.buildAppServerFilePath(config.getMetadataFileName());
        final String privateKeyStorePath = ioHelper.buildAppServerFilePath(config.getKeyStoreName());

        // Translate the producerCode into the producer entity identifier for outbound SAML and set the issuer
        // details on the SAML handler accordingly.
        final EntityTranslation lookup = new EntityTranslation(ioHelper.buildAppServerFilePath(ENTITY_TRANSLATION_FILE));
        final String issuer = lookup.lookupEntityIdentifierUsingInternalCode(producerCode);
        log.debug("Producer Issuer is {}", producerCode);

        // Setup our soak sender runnable in a seperate thread
        if (controlParameters.getSamlType().equals(ControlParameters.SamlType.SAML11)) {
            // SAML1.1
            final SAML11Handler handler = new SAML11Handler(issuer);
            SAML11AssertionProducerProcessor processor = SAML11AssertionProducerProcessorFactory.getInstance(
                    mdFilePath,
                    issuer,
                    SAMLConstants.SAML11P_NS,
                    DESTINATION_SOAK_CONSUMER,
                    DESTINATION_SOAK_CONSUMER,
                    mdHandler,
                    privateKeyStorePath,
                    config.getKeyStorePassword(),
                    config.getSigningKeyAlias(),
                    config.getSigningKeyPassword());
            saml11SoakSender = new SAML11AssertionSoakSender(controlParameters, processor, handler, session);
            senderThread = new Thread(saml11SoakSender);
            senderThread.start();

        } else {
            // SAML2
            SAML2Handler handler = new SAML2Handler(issuer);
            SAML2AssertionProducerProcessor processor =
                    SAML2AssertionProducerProcessorFactory.getInstance(
                            mdFilePath,
                            issuer,
                            SAMLConstants.SAML20P_NS,
                            DESTINATION_SOAK_CONSUMER,
                            DESTINATION_SOAK_CONSUMER,
                            mdHandler,
                            privateKeyStorePath,
                            config.getKeyStorePassword(),
                            config.getSigningKeyAlias(),
                            config.getSigningKeyPassword());
            saml2SoakSender = new SAML2AssertionSoakSender(controlParameters, processor, handler, session);
            senderThread = new Thread(saml2SoakSender);
            senderThread.start();
        }
    }

    /**
     * Stop the current test details
     *
     * @param session session
     * @param request request
     */
    private void stopTest(HttpSession session, HttpServletRequest request) {

        log.info("We are forceably stopping the current test");
        try {
            final ControlParameters controlParameters = (ControlParameters) session.getAttribute(CONTROL_PARAMS);
            if (controlParameters.getSamlType().equals(ControlParameters.SamlType.SAML11)) {
                saml11SoakSender.stop();
            } else {
                saml2SoakSender.stop();
            }
            // Wait for this thread to die
            senderThread.join();

            // Pick up the results and add them to the request
            request.setAttribute(SoakConstants.RESULTS, session.getAttribute(SoakConstants.RESULTS));
            session.removeAttribute(SoakConstants.RESULTS);

        } catch (Exception anyE) {
            final String errorMessage = "Error stopping test";
            log.error(errorMessage, anyE);
            throw new RuntimeException(errorMessage, anyE);
        }
    }

    private Action validateAction(final String action) {
        if (action != null) {
            if (action.equalsIgnoreCase("S")) {
                return Action.START;
            } else if (action.equalsIgnoreCase("E")) {
                return Action.END;
            } else if (action.equalsIgnoreCase("C")) {
                return Action.CHECK;
            } else {
                final String errorMessage = "Invalid action given " + action + ", we need S, C or E please.";
                log.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
        } else {
            final String errorMessage = "Null action given, we need S, C or E please.";
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }

    /**
     * Forward request on to next destination
     *
     * @param request       Request
     * @param response      Response
     * @param a_Destination Destination
     * @throws ServletException Servlet error
     * @throws IOException      IO error
     */
    private void forward(HttpServletRequest request,
                         HttpServletResponse response,
                         String a_Destination)
            throws ServletException, IOException {
        log.debug("Forwarding to {} ...............", a_Destination);
        final RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(a_Destination);
        dispatcher.forward(request, response);
    }
}
