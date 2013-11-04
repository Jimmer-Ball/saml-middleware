package com.timepoorprogrammer.remote.client;

import com.timepoorprogrammer.remote.service.Authoriser;
import org.apache.commons.httpclient.protocol.Protocol;
import org.jboss.resteasy.client.ClientRequestFactory;

/**
 * Use the RestEasy client library to establish a handle to a remote Authoriser service implementation.
 * <p/>
 * This is nice and simple, but doesn't give you the chance to get "under-the-hood" like HttpClient so has
 * been deprecated.
 *
 * @author Jim Ball
 */
@Deprecated
public class RestEasyAuthoriserClient {
    /**
     * Provide a SSL connection client that doesn't get bothered about HTTPS ports, or the SSL algorithms supported, or
     * trust or certificates.
     */
    static {
        try {
            Protocol.registerProtocol("https", new Protocol("https", new SecureProtocolSocketFactory(), 443));
        } catch (Exception anyE) {
            throw new RuntimeException("Error registering SecureProtocolSocketFactory", anyE);
        }
    }

    /**
     * Get hold of the remote implementation of the Authoriser API at the configured (see saml.properties) service URL
     * on the remote application.
     *
     * @param serviceUrl service url that points to the root URL under which the authoriser service lives
     *                   on the remote application.
     * @return handle to the remote authoriser service which can then be called via RestEasy
     */
    public Authoriser getAuthoriserHandle(final String serviceUrl) {
        try {
            final ClientRequestFactory clientRequestFactory = new ClientRequestFactory();
            return clientRequestFactory.createProxy(Authoriser.class, serviceUrl);
        } catch (Exception anyE) {
            throw new RuntimeException("Error establishing client to given the serviceUrl " + serviceUrl, anyE);
        }
    }
}
