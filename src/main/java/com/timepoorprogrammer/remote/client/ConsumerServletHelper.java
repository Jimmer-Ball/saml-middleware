package com.timepoorprogrammer.remote.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

/**
 * Consumer servlet helper class used following the middleware's interaction with remote Authoriser service instances.
 * <p/>
 * If the Authoriser service says "yes", then the middleware redirects to the application's backdoor setting a
 * couple og cookies along the way.
 * <p/>
 * If the Authoriser service says "no", then the middleware redirects to the configured (see saml.properties)
 * error page for the customer service combination, passing the error code and details along for the ride.
 * <p/>
 * What the customer's infrastructure does with the error code and details is up to them, its outside out control.
 *
 * @author Jim Ball
 */
public class ConsumerServletHelper {
    private static final Logger log = LoggerFactory.getLogger(ConsumerServletHelper.class);

    /**
     * Redirect the successfully authorised user to the remote application backdoor using the
     * redirection details to apply, making sure yuo don't let the user's browser do any caching of this
     * response along the way.
     *
     * @param redirectUrl        redirect to the front-door
     * @param response           response to work on
     * @param redirectionDetails redirection details
     */
    public static void onSuccessRedirect(String redirectUrl,
                                         HttpServletResponse response,
                                         final RedirectionDetails redirectionDetails) {
        if (redirectUrl == null || response == null || redirectionDetails == null) {
            throw new IllegalArgumentException
                    ("Cannot redirect on successful backdoor authorisation without an URL, a response, and remote session details");
        }
        try {
            final String sessionSetCookieHeader = redirectionDetails.getSessionSetCookieHeader();
            final String arrowPointCookieSetCookieHeader = redirectionDetails.getArrowPointSetCookieHeader();
            log.debug("sessionSetCookieHeader is   : " + sessionSetCookieHeader);
            log.debug("arrowPointSetCookieHeader is: " + arrowPointCookieSetCookieHeader);
            response.addHeader("Set-Cookie", sessionSetCookieHeader);
            response.addHeader("Set-Cookie", arrowPointCookieSetCookieHeader);
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
            response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
            response.setDateHeader("Expires", 0);
            response.sendRedirect(redirectUrl);
        } catch (Exception anyE) {
            throw new RuntimeException("Error redirecting on successful back-door authorisation", anyE);
        }
    }

    /**
     * The default behaviour for a given application on any assertion consumption error is to redirect to a customer
     * supplied (via configuration) error page.  The customer can choose what to do with the error details
     * supplied along with the response, it is completely up to them.
     * <p/>
     * Again stop the user's browser from doing any caching of the response along the way.
     *
     * @param redirectUrl  Where to redirect to
     * @param errorCode    error code indicating the issue type to the customer's infrastructure.  Useful if the
     *                     customer's infrastructure wants to display a message of their own choosing (e.g. an i18n one)
     *                     given the code returned.
     * @param errorDetails error details (in English) we provide to the customer's infrastructure which they can choose
     *                     to ignore.
     * @param response     response to work on
     */
    public static void onErrorRedirect(String redirectUrl,
                                       String errorCode,
                                       String errorDetails,
                                       HttpServletResponse response) {
        try {
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
            response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
            response.setDateHeader("Expires", 0);
            response.sendRedirect(redirectUrl
                    + "?errorCode=" + URLEncoder.encode(errorCode, "UTF-8")
                    + "&errorDetails=" + URLEncoder.encode(errorDetails, "UTF-8"));
        } catch (Exception anyE) {
            throw new RuntimeException("Error redirecting on error", anyE);
        }
    }
}
