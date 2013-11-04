package com.timepoorprogrammer.remote.client;

import org.apache.commons.lang.StringUtils;

import javax.servlet.http.Cookie;

/**
 * Simple encapsulation of the redirection details to apply at the middleware following a call to the authorise
 * method at a remote Authoriser service provided by say MyView or WebView.
 * <p/>
 * This is filled in by the HttpClientAuthoriserHandler which has to cope with "old" versions of the Authoriser service
 * implementation at remote WebView and MyView instances and the "new" version of the Authoriser service.  So, ones
 * that create traditional HTTP session state at the remote end, and ones that create state identified by some other
 * cookie than JSESSIONID (which cannot be overwritten if the middleware is on a different "logical" host than the
 * service.
 *
 * @author Jim Ball.
 */
public class RedirectionDetails {
    private Cookie sessionCookie;
    private Cookie arrowPointCookie;
    private String errorDetails;
    private String sessionId;
    private boolean isSessionCookieHttpOnly;
    private boolean isArrowPointCookieHttpOnly;
    private boolean isOldVersionOfAuthoriser;

    public RedirectionDetails() {
    }

    /**
     * The session identifier returned by the remote application following a successful call to the authorise method on
     * a remote Authoriser service.
     *
     * @return session identifier or null if no logical session was established
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Set the sessionId that comes back from the call to the remote authorise method.
     *
     * @param sessionId session identifier
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * The error response returned by the remote application following an unsuccessful call to the authorise method on
     * the remote Authoriser service
     *
     * @return error details error details
     */
    public String getErrorDetails() {
        return errorDetails;
    }

    /**
     * Set the error details returned by the remote application
     *
     * @param errorDetails error details
     */
    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    /**
     * Get the session cookie as a servlet 2.5 cookie
     *
     * @return servlet 2.5 cookie
     */
    public Cookie getSessionCookie() {
        return sessionCookie;
    }

    /**
     * Set the session cookie details
     *
     * @param sessionCookie session cookie details
     */
    public void setSessionCookie(Cookie sessionCookie) {
        this.sessionCookie = sessionCookie;
    }

    /**
     * Is the session cookie HttpOnly?
     *
     * @return true if HttpOnly, false otherwise
     */
    public boolean isSessionCookieHttpOnly() {
        return isSessionCookieHttpOnly;
    }

    /**
     * Set whether the session cookie is HttpOnly
     *
     * @param sessionCookieHttpOnly true if HttpOnly, false otherwise
     */
    public void setSessionCookieHttpOnly(boolean sessionCookieHttpOnly) {
        isSessionCookieHttpOnly = sessionCookieHttpOnly;
    }

    /**
     * The session Set-Cookie header to apply on redirection to the service backdoor which will include
     * the HttpOnly flag as well.
     *
     * @return session cookie header to apply on redirection to the service backdoor
     */
    public String getSessionSetCookieHeader() {
        return CookieHelper.getSetCookieHeaderString(sessionCookie, isSessionCookieHttpOnly);
    }

    /**
     * Get the arrow point cookie to apply
     *
     * @return servlet 2.5 arrow point cookie
     */
    public Cookie getArrowPointCookie() {
        return arrowPointCookie;
    }

    /**
     * Set the arrow point cookie
     *
     * @param arrowPointCookie arrow point cookie
     */
    public void setArrowPointCookie(Cookie arrowPointCookie) {
        this.arrowPointCookie = arrowPointCookie;
    }

    /**
     * Is the arrow point cookie HttpOnly?
     *
     * @return true if HttpOnly, false otherwise
     */
    public boolean isArrowPointCookieHttpOnly() {
        return isArrowPointCookieHttpOnly;
    }

    /**
     * Set whether the arrow point cookie is HttpOnly
     *
     * @param arrowPointCookieHttpOnly true if HttpOnly, false otherwise
     */
    public void setArrowPointCookieHttpOnly(boolean arrowPointCookieHttpOnly) {
        isArrowPointCookieHttpOnly = arrowPointCookieHttpOnly;
    }

    /**
     * The arrow point Set-Cookie header to apply on redirection to the service backdoor which will include
     * the HttpOnly flag.
     *
     * @return arrow point cookie to apply on redirection to the service backdoor
     */
    public String getArrowPointSetCookieHeader() {
        return CookieHelper.getSetCookieHeaderString(arrowPointCookie, isArrowPointCookieHttpOnly);
    }

    /**
     * Are we dealing with an "old" version of the Authoriser service that expects the middleware to attempt
     * to overwrite the JSESSION cookie for the remote service, or are we dealing with a "new" version of the
     * Authoriser service that expects the middleware to provide an AUTHSERVICE cookie.
     * <p/>
     * Knowing this allows us to validate the redirection configuration in use at the middleware during the
     * commissioning phase, so we can be pro-active about the configuration rather than just "hoping" it will
     * work out okay during commissioning.
     *
     * @return true if the remote application has an "old" HTTP Session overwriting implementation version, false
     *         otherwise.
     */
    public boolean isOldVersionOfAuthoriser() {
        return isOldVersionOfAuthoriser;
    }

    public void setOldVersionOfAuthoriser(boolean oldVersionOfAuthoriser) {
        isOldVersionOfAuthoriser = oldVersionOfAuthoriser;
    }

    /**
     * Was a session created?
     *
     * @return true if a session was created, false if the remote Authoriser service implementation sends back
     *         an error
     */
    public boolean sessionCreated() {
        return StringUtils.isBlank(errorDetails) && !StringUtils.isBlank(sessionId);
    }
}
