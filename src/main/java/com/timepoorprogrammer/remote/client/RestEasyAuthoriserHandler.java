package com.timepoorprogrammer.remote.client;

import com.timepoorprogrammer.saml.configuration.ConsumerRedirectionConfiguration;
import com.timepoorprogrammer.remote.service.AuthoriserRequest;
import com.timepoorprogrammer.remote.service.AuthoriserResponse;
import com.timepoorprogrammer.remote.service.DeauthoriseRequest;
import com.timepoorprogrammer.remote.service.DeauthoriseResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a remote application's Authoriser that uses RestEasy.
 * <p/>
 * This is useful but doesn't let you get "under-the-hood" like HttpClient, so has been deprecated.
 *
 * @author Jim Ball
 */

@Deprecated
@SuppressWarnings("deprecation")
public class RestEasyAuthoriserHandler implements AuthoriserHandler {
    private static final Logger log = LoggerFactory.getLogger(RestEasyAuthoriserHandler.class);
    private RestEasyAuthoriserClient client = null;

    /**
     * Create a client stub we need to talk to the remote application.
     */
    public RestEasyAuthoriserHandler() {
        this.client = new RestEasyAuthoriserClient();
    }

    /**
     * Authorise at the remote application, which will send us a response indicating if the user
     * is authorised to use the remote application.
     *
     * @param config         Consumer to customer specific redirection configuration out of saml.properties
     * @param userIdentifier user identifier
     * @param module         module identifier or null
     * @return remote session details
     */
    public RemoteSession authorise(final ConsumerRedirectionConfiguration config,
                                   final String userIdentifier,
                                   final String module) {
        AuthoriserResponse details;
        RemoteSession session = new RemoteSession(config);
        try {
            log.debug(String.format("Contacting Authoriser service on remote application to see if user %s can access module %s", userIdentifier, module));
            details = client.getAuthoriserHandle(session.getServiceUrl()).authorise(null, new AuthoriserRequest(userIdentifier, module, config.getCustomerCode()));
            if (details != null) {
                log.debug("Got returned temporary token/session id of: " + details.getSessionId());
                session.setProviderResponse(details);
            } else {
                // We'll create bad response details on behalf of the remote application
                final String errorMessage = "Empty session details returned from remote application ";
                log.error(errorMessage);
                details = new AuthoriserResponse(null);
                details.setErrorMessage(errorMessage);
                session.setProviderResponse(details);
            }
        } catch (Exception anyE) {
            final String errorMessage = "Error authorising with remote application: ";
            log.error(errorMessage + anyE.getMessage());
            details = new AuthoriserResponse(null);
            details.setErrorMessage(errorMessage + anyE.getMessage());
            session.setProviderResponse(details);
        }
        return session;
    }

    /**
     * Finalise or end the session with a remote application if required.  Normally you'd only use this in point to
     * point communication between a client and a service. The middleware though, is middleware acting on behalf of
     * a client, so as yet (29/11/2012) doesn't need to call this.
     *
     * @param session       remote application session ID
     * @param isCookieBased true if Authoriser service on far end is cookie based, false otherwise
     */
    public void finaliseSession(RemoteSession session, boolean isCookieBased) {
        try {
            if (isCookieBased) {
                client.getAuthoriserHandle(session.getServiceUrl()).finaliseSession(null, session.getProviderResponse().getSessionId());
            } else {
                DeauthoriseRequest request = new DeauthoriseRequest(session.getProviderResponse().getSessionId());
                DeauthoriseResponse response = client.getAuthoriserHandle(session.getServiceUrl()).deauthorise(null, request);
                String errorMessage = response.getErrorMessage();
                if (!StringUtils.isBlank(errorMessage)) {
                    throw new RuntimeException("Error de-authorising at the remote application: " + errorMessage);
                }
            }
        } catch (Exception anyE) {
            // We don't re-throw here, instead we swallow, as there is nothing we can do about it, and the middleware
            // needs to keep trucking regardless of remote Authoriser service implementation errors.
            final String errorMessage = "Error finalising session for remote application: " + session.getServiceUrl() + " with exception of " + anyE.getMessage();
            log.error(errorMessage, anyE);
        }
    }
}

