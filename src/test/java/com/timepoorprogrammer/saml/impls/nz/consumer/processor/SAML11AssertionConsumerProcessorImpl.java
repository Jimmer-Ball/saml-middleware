package com.timepoorprogrammer.saml.impls.nz.consumer.processor;

import com.timepoorprogrammer.saml.impls.MetaDataHandler;
import com.timepoorprogrammer.saml.impls.SAML11AssertionConsumerProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bespoke SAML1.1 assertion consumer processor for customer GA.
 *
 * @author Jim Ball
 */
public class SAML11AssertionConsumerProcessorImpl extends com.timepoorprogrammer.saml.impls.standard.consumer.processor.SAML11AssertionConsumerProcessorImpl implements SAML11AssertionConsumerProcessor {
    private static final Logger log = LoggerFactory.getLogger(SAML11AssertionConsumerProcessorImpl.class);

    /**
     * Construct a SAML1.1 assertion consumer processor
     *
     * @param metaDataFilePath metadata file path
     * @param idpId            identity provider SAML entity identifier
     * @param customerCode     identity provider Northgate internal customer code
     * @param idpProtocol      identity provider SAML protocol to use
     * @param spId             service provider id
     * @param mdHandler        metadata handler
     */
    public SAML11AssertionConsumerProcessorImpl(final String metaDataFilePath,
                                                final String idpId,
                                                final String customerCode,
                                                final String idpProtocol,
                                                final String spId,
                                                final MetaDataHandler mdHandler) {
        super(metaDataFilePath, idpId, customerCode, idpProtocol, spId, mdHandler);
        log.info("Bespoke SAML 1.1 assertion consumer processor for customer code NZ");
    }
}
