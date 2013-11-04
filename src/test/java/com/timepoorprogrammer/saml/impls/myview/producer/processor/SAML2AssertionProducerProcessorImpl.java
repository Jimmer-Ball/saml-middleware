package com.timepoorprogrammer.saml.impls.myview.producer.processor;

import com.timepoorprogrammer.saml.impls.MetaDataHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bespoke SAML2 assertion producer processor.
 *
 * @author Jim Ball
 */
public class SAML2AssertionProducerProcessorImpl extends com.timepoorprogrammer.saml.impls.standard.producer.processor.SAML2AssertionProducerProcessorImpl {
    private static final Logger log = LoggerFactory.getLogger(SAML2AssertionProducerProcessorImpl.class);


    /**
     * Construct a SAML2 assertion producer processor passing the following arguments at minimum:-
     * <ul>
     * <li>The filepath for our metadata</li>
     * <li>The identity provider's unique id which matches up with metadata</li>
     * <li>The SAML protocol (either 2.0 or 1.1) our identity provider uses to create SAML messages, so either
     * SAMLConstant.SAML11P_NS == urn:oasis:names:tc:SAML:1.1:protocol, or SAMLConstants.SAML20P_NS ==
     * urn:oasis:names:tc:SAML:2.0:protocol</li>
     * <li>The service provider's unique id which matches up with metadata</li>
     * <li>The metadata handler</li>
     * </ul>
     * <p/>
     * The rest of the arguments describe any signing architecture we might apply to outgoing SAML messages
     * and can (in development) be null, but must be not null in a production system to meet the SAML specs.
     * <p/>
     * Note: This is called from a factory because there may be many implementations of these depending on the
     * remote service provider we are sending to, their metadata, and any "special" features they may want.
     *
     * @param metaDataFilePath            metadata file path
     * @param idpId                       identity provider SAML entity identifier
     * @param idpProtocol                 identity provider SAML protocol to use
     * @param serviceCode                 service provider or partner internal Northgate code
     * @param spId                        service provider SAML entity identifier
     * @param mdHandler                   metadata handler
     * @param signingKeyStoreFilePath     The file path for the keystore holding the private key we as a identity
     *                                    provider would use to sign an outbound SAML message or null.
     * @param signingKeyStoreFilePassword The password for the keystore holding the signing key or null.
     * @param signingKeyAlias             The alias of the signing key in the keystore or null;
     * @param signingKeyPassword          The password for the signing key or null.
     */
    public SAML2AssertionProducerProcessorImpl(final String metaDataFilePath,
                                               final String idpId,
                                               final String idpProtocol,
                                               final String serviceCode,
                                               final String spId,
                                               final MetaDataHandler mdHandler,
                                               final String signingKeyStoreFilePath,
                                               final String signingKeyStoreFilePassword,
                                               final String signingKeyAlias,
                                               final String signingKeyPassword) {
        super(metaDataFilePath, idpId, idpProtocol, serviceCode, spId, mdHandler, signingKeyStoreFilePath,
                signingKeyStoreFilePassword, signingKeyAlias, signingKeyPassword);
        log.info("Bespoke SAML2 assertion producer processor for destination SP MyView");
    }
}
