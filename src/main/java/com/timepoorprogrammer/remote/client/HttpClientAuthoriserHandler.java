package com.timepoorprogrammer.remote.client;

import com.timepoorprogrammer.common.utilities.xml.XMLUtilities;
import com.timepoorprogrammer.remote.client.SecureProtocolSocketFactory;
import com.timepoorprogrammer.saml.configuration.ConsumerRedirectionConfiguration;
import com.timepoorprogrammer.remote.service.AuthoriserRequest;
import com.timepoorprogrammer.remote.service.AuthoriserResponse;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a remote application's Authoriser that uses low-level HttpClient calls making it all
 * thread-safe and a bit more high-performance than the deprecated RestEasy implementation.
 *
 * @author Jim Ball
 */
public class HttpClientAuthoriserHandler {
    private static final Logger log = LoggerFactory.getLogger(HttpClientAuthoriserHandler.class);
    private XMLUtilities xmlUtils;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 5000;
    private static final int DEFAULT_MAX_CONNECTIONS = 50;
    private static final String ACCEPT_FORMAT = "text/xml";
    private static final String ENCODING = "UTF-8";
    private static final String SERVICE_POSTFIX = "/authorise";

    /**
     * We want the one pool of thread safe HttpClient connections available to avoid the overhead of
     * creating connections for every new HttpClient created to boost the overall performance of the
     * middleware, and to ensure a client doesn't hang around forever if the service cannot respond,
     * and to promote connection re-use where we can.
     * <p/>
     * We will get the occasional connection the client tries to use having been closed on the server
     * side, but this is okay, the HttpClient library will pick another from the pool automatically.
     */
    static MultiThreadedHttpConnectionManager connectionManagerForAllClients;

    static {
        connectionManagerForAllClients = new MultiThreadedHttpConnectionManager();
        HttpConnectionManagerParams connectionManagerParams = new HttpConnectionManagerParams();
        connectionManagerParams.setDefaultMaxConnectionsPerHost(DEFAULT_MAX_CONNECTIONS);
        connectionManagerParams.setConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT);
        connectionManagerForAllClients.setParams(connectionManagerParams);
        Protocol.registerProtocol("https", new Protocol("https", new SecureProtocolSocketFactory(), 443));
    }

    /**
     * Create an HttpClient  we can use to talk to the remote application with, and some XML utilities that will
     * allow us to build the response into an object.
     */
    public HttpClientAuthoriserHandler() {
        xmlUtils = new XMLUtilities();
    }

    /**
     * Use an HttpClient to authorise with the remote service as we need to get at cookies on the response so we
     * need low-level control.
     *
     * @param config         consumer redirection configuration
     * @param userIdentifier user identifier
     * @param module         module
     * @return Redirection details
     */
    public RedirectionDetails authorise(final ConsumerRedirectionConfiguration config,
                                        final String userIdentifier,
                                        final String module) {
        RedirectionDetails redirectionDetails = new RedirectionDetails();
        PostMethod authorise = null;
        try {
            // If the full path to the authorise service is not complete in saml.properties, make it so, as this is a
            // raw HttpClient connection to a service that gets published with @Path(/authorise) (see the Authorise
            // interface for details).
            String serviceUrl = config.getServiceUrl();
            if (!serviceUrl.endsWith(SERVICE_POSTFIX)) {
                serviceUrl = serviceUrl + SERVICE_POSTFIX;
            }
            HttpClient client = getClient();
            authorise = new PostMethod(serviceUrl);
            authorise.setRequestHeader("Accept", ACCEPT_FORMAT);
            AuthoriserRequest authRequest = new AuthoriserRequest(userIdentifier, module, config.getCustomerCode());
            StringRequestEntity content = new StringRequestEntity(xmlUtils.marshallToXML(authRequest), ACCEPT_FORMAT, ENCODING);
            authorise.setRequestEntity(content);
            final int gotCode = client.executeMethod(authorise);
            AuthoriserResponse response;
            if (gotCode == HttpStatus.SC_OK) {
                final String responseAsString = authorise.getResponseBodyAsString();
                response = xmlUtils.unmarshallFromXML(responseAsString, AuthoriserResponse.class);
                final String errorMessage = response.getErrorMessage();
                final String sessionId = response.getSessionId();
                if (StringUtils.isBlank(errorMessage) && !StringUtils.isBlank(sessionId)) {
                    // Add the session Id to the redirection details
                    redirectionDetails.setSessionId(sessionId);

                    // Add the service cookie to the redirection details
                    boolean oldVersionOfAuthoriser = isOldVersionOfAuthoriser(client, sessionId);
                    redirectionDetails.setOldVersionOfAuthoriser(oldVersionOfAuthoriser);
                    redirectionDetails.setSessionCookieHttpOnly(config.getSessionCookieHttpOnlyFlag());
                    redirectionDetails.setSessionCookie(CookieHelper.getSessionCookie(config, oldVersionOfAuthoriser, sessionId));

                    // Add the arrow point cookie to the redirection details
                    redirectionDetails.setArrowPointCookieHttpOnly(config.getArrowPointCookieHttpOnlyFlag());
                    redirectionDetails.setArrowPointCookie(CookieHelper.getArrowPointCookie(config));

                } else {
                    // We got an error from the remote Authoriser implementation
                    redirectionDetails.setErrorDetails(response.getErrorMessage());
                }
            } else {
                redirectionDetails.setErrorDetails("Unexpected service response code " + gotCode + " returned from remote Authoriser service");
            }
        } catch (Exception anyE) {
            final String errorMessage = "Error authorising with remote Authoriser service: " + anyE.getMessage();
            redirectionDetails.setErrorDetails(errorMessage);
        } finally {
            if (authorise != null) {
                authorise.releaseConnection();
            }
        }
        return redirectionDetails;
    }


    /**
     * Get a client from the pool when you need it.
     *
     * @return HTTP client
     */
    private HttpClient getClient() {
        HttpClient client = new HttpClient(connectionManagerForAllClients);
        client.getParams().setParameter("http.protocol.single-cookie-header", true);
        client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        return client;
    }

    /**
     * Are we dealing with an old version of the authoriser service which expects the middleware to attempt to
     * overwrite the JSESSIONID state established by the remote Authoriser implementation?.
     * <p/>
     * If the call to authorise returns a Set-Cookie JSESSIONID directive that holds the same value as the sessionId
     * returned by the call to the authorise method, then we are dealing with an "old" implementation.
     * <p/>
     * An "old" implementation means that at the remote Authoriser service a piece of standard HTTP session state
     * will been been created within the Authoriser implementation. The remote service is assuming this state is
     * re-bindable at the application's backdoor following re-direction by the middleware to the application's backdoor.
     * <p/>
     * Unfortunately re-binding HTTP session state assumes the JSESSIONID cookie of a remote service can be overwritten
     * by a Set-Cookie directive at the middleware.  This though is can only be done if the service and the middleware
     * are seen to be resident on the same logical host by the user's browser.
     *
     * @param client    client to check for state
     * @param sessionId session Id provided by Authoriser service implementation
     * @return true if dealing with an "old" version of an Authoriser implementation, false otherwise
     */
    private boolean isOldVersionOfAuthoriser(HttpClient client, final String sessionId) {
        boolean oldVersionOfAuthoriser = false;
        HttpState state = client.getState();
        Cookie[] cookies = state.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(RedirectionValidator.OLD_VERSION_SESSION_COOKIE)) {
                    final String value = cookie.getValue();
                    if (!StringUtils.isBlank(value)) {
                        if (value.equals(sessionId)) {
                            oldVersionOfAuthoriser = true;
                        }
                    }
                    break;
                }
            }
        }
        if (oldVersionOfAuthoriser) {
            log.debug("We are dealing with an \"old\" version of the remote Authoriser which assumes a " 
                    + RedirectionValidator.OLD_VERSION_SESSION_COOKIE + " cookie");
        } else {
            log.debug("We are dealing with a \"new\" version of the remote Authoriser which assumes a " 
                    + RedirectionValidator.NEW_VERSION_SESSION_COOKIE + " cookie");
        }
        return oldVersionOfAuthoriser;
    }
}

