package com.timepoorprogrammer.saml.impls.nz.consumer.processor;

import com.timepoorprogrammer.saml.impls.MetaDataHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bespoke SAML2 assertion consumer processor example.
 *
 * @author Jim Ball
 */
public class SAML2AssertionConsumerProcessorImpl extends com.timepoorprogrammer.saml.impls.standard.consumer.processor.SAML2AssertionConsumerProcessorImpl {
    private static final Logger log = LoggerFactory.getLogger(SAML2AssertionConsumerProcessorImpl.class);

    /**
     * Bespoke assertion consumer for customer code NZ
     *
     * @param metaDataFilePath               SAML metadata file path
     * @param idpId                          identity provider SAML entity identifier
     * @param customerCode                   internal Northgate customer code
     * @param idpProtocol                    SAML protocol supported by the bespoke consumer
     * @param spId                           service provider SAML entity identifier
     * @param mdHandler                      metadata handler
     * @param decryptionKeyStoreFilePath     path to key store holding decryption key
     * @param decryptionKeyStoreFilePassword password for the key store
     * @param decryptionKeyAlias             decryption key alias
     * @param decryptionKeyPassword          password for the decrpytion key
     */
    public SAML2AssertionConsumerProcessorImpl(final String metaDataFilePath,
                                               final String idpId,
                                               final String customerCode,
                                               final String idpProtocol,
                                               final String spId,
                                               final MetaDataHandler mdHandler,
                                               final String decryptionKeyStoreFilePath,
                                               final String decryptionKeyStoreFilePassword,
                                               final String decryptionKeyAlias,
                                               final String decryptionKeyPassword) {
        super(metaDataFilePath, idpId, customerCode, idpProtocol, spId, mdHandler, decryptionKeyStoreFilePath, decryptionKeyStoreFilePassword, decryptionKeyAlias, decryptionKeyPassword);
        log.info("Bespoke SAML2 consumer processor implementation for customer code NZ");
    }
}
