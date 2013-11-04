package com.timepoorprogrammer.saml.impls.ga.consumer.processor;

import com.timepoorprogrammer.saml.configuration.ConsumerRedirectionConfiguration;
import com.timepoorprogrammer.remote.client.HttpClientAuthoriserHandler;
import com.timepoorprogrammer.remote.client.RedirectionDetails;

/**
 * Custom (bespoke) assertion consumer processor for Goldman Sachs who don't use SAML
 * but instead roll their own and expect us to process it.
 *
 * @author Jim Ball
 */
public class AssertionConsumerProcessor {
    private HttpClientAuthoriserHandler appHandler = new HttpClientAuthoriserHandler();

    /**
     * Authorise with a remote application definied in the consumer configuration.
     *
     * @param config         configuration
     * @param userIdentifier user identifier
     * @param module         module identifier
     * @return redirection details
     */
    public RedirectionDetails authorise(ConsumerRedirectionConfiguration config, String userIdentifier, String module) {
        return appHandler.authorise(config, userIdentifier, module);
    }
}
