package com.timepoorprogrammer.saml.impls.ga.producer.servlet;

import com.timepoorprogrammer.saml.common.AuditMessages;
import com.timepoorprogrammer.saml.configuration.ConfigurationProperties;
import com.timepoorprogrammer.saml.core.IOHelper;
import com.timepoorprogrammer.saml.impls.ga.common.ProducerConfiguration;
import com.timepoorprogrammer.saml.impls.ga.common.ProducerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * Custom (bespoke) assertion producer for Goldman Sachs commissioning testing.
 *
 * @author Jim Ball
 */
public class AssertionProducerServlet extends HttpServlet {
    private static final long serialVersionUID = -8928560694519709663L;
    private static final Logger log = LoggerFactory.getLogger(AssertionProducerServlet.class);

    /**
     * This is fro Goldman Sachs and no-one else.
     */
    private static final String DEFAULT_WHO_AM_I = "GA";

    /**
     * SAML properties file
     */
    private static final String PROPERTIES_FILE = "saml.properties";

    /**
     * Default service that Goldman Sachs sends to
     */
    private static final String DEFAULT_SERVICE = "MyView";

    /**
     * The JSP that sends the Goldman Sachs assertion
     */
    private static final String IDP_SEND_JSP = "sendAssertion.jsp";

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
     * Process POST request received from a front-end form
     *
     * @param request  request
     * @param response response
     * @throws javax.servlet.ServletException on servlet error
     * @throws java.io.IOException            on IOException
     */
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Create a new session or get the existing one if one already exists
        final HttpSession session = request.getSession(true);
        // Get the attributes from the request body then (service name, userIdentifierName, and relay state)
        final Map<String, String[]> requestParameters = getTypedMap(request.getParameterMap());
        if (requestParameters != null && requestParameters.containsKey("userIdentifier")
                && requestParameters.containsKey("timeout") && requestParameters.containsKey("service")) {
            final String userIdentifier = getSimpleParameter(requestParameters, "userIdentifier");
            final String timeout = getSimpleParameter(requestParameters, "timeout");
            final String service = getSimpleParameter(requestParameters, "service");
            log.info("We have a user identifier name of {}", userIdentifier);
            log.info("We have timeout parameter of {}", timeout);
            log.info("We have service parameter of {}", service);
            if (userIdentifier != null) {
                final IOHelper ioHelper = new IOHelper();
                // Get hold of our configuration now, as we need to stay adaptable to configuration change
                final ConfigurationProperties props = new ConfigurationProperties(ioHelper.buildAppServerFilePath(PROPERTIES_FILE));
                final ProducerConfiguration config = new ProducerConfiguration(props, DEFAULT_WHO_AM_I);
                final Long secondsInThePast = (System.currentTimeMillis() / 1000) - Long.parseLong(timeout);
                final String dataString = Long.toString(secondsInThePast) + "|" + userIdentifier;
                log.info("The data string content we will send is " + dataString);
                final String mac = config.generateMAC(dataString);
                log.info("And the MAC content we will send is " + mac);
                log.info("And the service identifier is " + service);
                session.setAttribute("ID_STRING", dataString);
                session.setAttribute("SIGNATURE", mac);
                // Only add the service identifier if we are not
                // directing to MyView
                if (!service.equals(DEFAULT_SERVICE)) {
                    session.setAttribute("SERVICE", service);
                } else {
                    session.removeAttribute("SERVICE");
                }
                response.sendRedirect(IDP_SEND_JSP);
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
