package com.timepoorprogrammer.saml.impls.ga.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is here purely to illustrate the bespoking mechanism to show that ALL the bespoke class factories in the project
 * work in the same way.  See AuditMessengerTest for details.
 *
 * @author Jim Ball
 */
public class AuditMessengerImpl extends com.timepoorprogrammer.saml.impls.standard.common.AuditMessengerImpl {
    /**
     * Logging handle
     */
    private static final Logger log = LoggerFactory.getLogger(AuditMessengerImpl.class);

    /**
     * Default constructor
     */
    public AuditMessengerImpl() {
        log.info("This is a bespoke implementation of a GA audit messenger");
    }

    /**
     * Raise an error message with auditing
     *
     * @param messagePrefix message prefix (e.g. "SAML PRODUCER AUDIT ERROR MESSAGE")
     * @param idpId         identity provider id
     * @param idpProtocol   identity provider protocol (e.g. SAML2)
     * @param spId          service provider identity
     * @param message       message to put in audit trail
     */
    public void auditError(final String messagePrefix,
                           final String idpId,
                           final String idpProtocol,
                           final String spId,
                           final String message) {
        log.info("Bespoke GA audit error");
    }

    /**
     * Raise a success message with auditing
     *
     * @param messagePrefix message prefix (e.g. "SAML PRODUCER AUDIT SUCCESS MESSAGE")
     * @param idpId         identity provider id
     * @param idpProtocol   identity provider protocol (e.g. SAML2)
     * @param spId          service provider identity
     * @param message       message to put in audit trail
     */
    public void auditSuccess(final String messagePrefix,
                             final String idpId,
                             final String idpProtocol,
                             final String spId,
                             final String message) {
       log.info("Bespoke GA audit success");
    }
}
