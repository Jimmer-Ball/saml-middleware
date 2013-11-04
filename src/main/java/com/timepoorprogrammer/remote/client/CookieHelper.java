package com.timepoorprogrammer.remote.client;

import com.timepoorprogrammer.saml.configuration.ConsumerRedirectionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;

/**
 * Cookie helper class
 *
 * @author Jim Ball
 */
public class CookieHelper {
    private static final Logger log = LoggerFactory.getLogger(CookieHelper.class);

    /**
     * Get the session cookie for the remote service from the consumer redirection configuration.  This produces a
     * servlet 2.5 specification cookie that should expire at the end of the user-agents interaction with our
     * infrastructure.
     *
     * @param config                   consumer redirection configuration
     * @param isOldVersionOfAuthoriser true if the client is dealing with an "old" version of the Authoriser service
     *                                 that expects the middleware to try and overwrite a host based JSESSIONID cookie.
     * @param sessionCookieValue       session cookie value
     * @return session cookie details
     */
    public static Cookie getSessionCookie(final ConsumerRedirectionConfiguration config,
                                          boolean isOldVersionOfAuthoriser,
                                          String sessionCookieValue) {
        String sessionCookieName;
        if (isOldVersionOfAuthoriser) {
            // We force the use of the JSESSIONID cookie, as the remote Authoriser service implementation has created
            // HTTP session state, and is expecting the middleware to attempt to overwrite it.  Note overwriting a
            // JSESSIONID cookie will only work if the middleware and the service are on the same logical host.
            if (!config.getSessionCookieName().equals(RedirectionValidator.OLD_VERSION_SESSION_COOKIE)) {
                log.warn("What are you doing, \"old\" versions of the Authoriser expect us to use "
                        + RedirectionValidator.OLD_VERSION_SESSION_COOKIE + " for the session cookie name");
            }
            sessionCookieName = RedirectionValidator.OLD_VERSION_SESSION_COOKIE;
        } else {
            if (!config.getSessionCookieName().equals(RedirectionValidator.NEW_VERSION_SESSION_COOKIE)) {
                log.warn("What are you doing, \"new\" versions of the Authoriser expect us to use "
                        + RedirectionValidator.NEW_VERSION_SESSION_COOKIE + " for the session cookie name");
            }
            // There is no point in the middleware binding a UUID to a cookie the "new" versions of the Authoriser
            // service at MyView and WebView are not actually looking for, so here we bind to
            sessionCookieName = RedirectionValidator.NEW_VERSION_SESSION_COOKIE;
        }
        final String sessionCookieDomain = config.getSessionCookieDomain();
        final String sessionCookiePath = config.getSessionCookiePath();
        Cookie cookie = new Cookie(sessionCookieName, sessionCookieValue);
        if (sessionCookieDomain != null) {
            cookie.setDomain(sessionCookieDomain);
        }
        if (sessionCookiePath != null) {
            cookie.setPath(sessionCookiePath);
        }
        cookie.setVersion(0);
        cookie.setSecure(config.getSessionCookieSecureFlag());
        cookie.setMaxAge(-1);
        return cookie;
    }

    /**
     * Get the arrow point cookie for the remote service from the consumer redirection configuration.  This produces
     * a servlet 2.5 specification cookie.
     *
     * @param config The consumer redirection configuration
     * @return servlet 2.5 specification cookie for arrow point
     */
    public static Cookie getArrowPointCookie(final ConsumerRedirectionConfiguration config) {
        final String arrowPointCookieName = config.getArrowPointCookieName();
        final String arrowPointCookieValue = config.getArrowPointCookieValue();
        final String arrowPointCookieDomain = config.getArrowPointCookieDomain();
        final String arrowPointCookiePath = config.getArrowPointCookiePath();
        Cookie cookie = new Cookie(arrowPointCookieName, arrowPointCookieValue);
        if (arrowPointCookieDomain != null) {
            cookie.setDomain(arrowPointCookieDomain);
        }
        if (arrowPointCookiePath != null) {
            cookie.setPath(arrowPointCookiePath);
        }
        cookie.setVersion(0);
        cookie.setSecure(config.getArrowPointCookieSecureFlag());
        cookie.setMaxAge(-1);
        return cookie;
    }

    /**
     * Given an input cookie, convert its contents into a Set-Cookie string.  Why?, Well we need to add in the
     * HttpOnly flag that is not part of the Java Servlet 2.5 specification, and unless we are running on a
     * Servlet 3 container which we cannot guarantee, then we have to use a method that manually builds these
     * Set-Cookie instructions to include HttpOnly for the browser to obey.
     * <p/>
     * The string format here comes directly from the ABNF definition details found in Chapter 4 of the document
     * http://www.rfc-editor.org/rfc/rfc6265.txt
     *
     * @param cookie     Servlet 2.5 specification cookie
     * @param isHttpOnly true if HttpOnly (cookie not accessible by JavaScript), false otherwise
     * @return Set-Cookie header content according to the ABNF definition that instructs a browser to keep hold
     *         of the cookie till it (the browser user agent) ends its logical session.
     */
    public static String getSetCookieHeaderString(final Cookie cookie, boolean isHttpOnly) {
        // Start with a space
        StringBuilder buf = new StringBuilder(" ");
        // Add Name and value pair
        buf.append(cookie.getName()).append("=").append(cookie.getValue());
        // Add Domain attribute
        final String domain = cookie.getDomain();
        if (domain != null) {
            buf.append("; ").append("Domain=").append(cookie.getDomain());
        }
        // Add Path attribute
        final String path = cookie.getPath();
        if (path != null) {
            buf.append("; ").append("Path=").append(cookie.getPath());
        }
        // Add Secure attribute
        if (cookie.getSecure()) {
            buf.append("; Secure");
        }
        // Add HttpOnly attribute
        if (isHttpOnly) {
            buf.append("; HttpOnly");
        }
        return buf.toString();
    }
}
