package com.timepoorprogrammer.saml.impls;

import com.timepoorprogrammer.saml.TestHelper;
import com.timepoorprogrammer.saml.configuration.EntityTranslation;
import com.timepoorprogrammer.saml.core.SAML11Handler;
import org.junit.Assert;
import org.junit.Test;
import org.opensaml.common.xml.SAMLConstants;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test class for the assertion consumer which also illustrates how every single
 * factory in the whole project actually works in terms of picking up default
 * implementations and bespoke implementations from a standard sub package
 * organisation to be used for every single customer bespoke work needs to be done for.
 *
 * @author Jim Ball
 */
public class SAML11AssertionConsumerProcessorTest {

    /**
     * The name of the assertion consumer we are
     */
    private static final String ASSERTION_CONSUMER_NAME = "MyView";

    /**
     * Properties file holding lookup details between SAML Issuer details and our Northgate internal
     * customer code.
     */
    private static final String ENTITY_TRANSLATION_FILE = TestHelper.getFullPath("^.*fixtures\\\\configuration\\\\samlentitytranslation.properties$");

    /**
     * Path to SAML metadata file describing producers and consumers of assertions
     */
    private static final String META_DATA_FILE = TestHelper.getFullPath("^.*fixtures\\\\metadata\\\\idp_and_sp_metadata.xml$");
    /**
     * Issuer details, which correspond to the customer code and the organisation of the code
     */
    private static final String ISSUER = "NZ";

    // To test a processor requires an initialised SAML library which means an initialed handler
    public static final SAML11Handler GOTTA_HAVE_A_HANDLER = new SAML11Handler();

    /**
     * Test the return of the default SAML11AssertionConsumerProcessor is correct.
     * <p/>
     * This can be used to validate all the factories work the same way too in terms of picking up
     * a bespoke implementation via a factory along the path of
     * com.ngahr.saml.impls.<lowercase_customer_code>.<whatever_the_subdirectory_is_for_the_factory>.<interface_name>Impl
     */
    @Test
    public void testDefaults() {
        try {
            final MetaDataHandler mdHandler = MetaDataHandlerFactory.getInstance(null);

            // See if we have a customer code for this user that differs from the provided Issuer details string
            final EntityTranslation lookup = new EntityTranslation(ENTITY_TRANSLATION_FILE);
            final String customerCode = lookup.lookupInternalCodeUsingEntityIdentifier("idp_saml11");

            final SAML11AssertionConsumerProcessor defaultProcessor =
                    SAML11AssertionConsumerProcessorFactory.getInstance(META_DATA_FILE, "idp_saml11", customerCode, SAMLConstants.SAML11P_NS, ASSERTION_CONSUMER_NAME, mdHandler);

            final Class defaultClass = defaultProcessor.getClass();
            assertThat(defaultClass.getName(), is("com.timepoorprogrammer.saml.impls.standard.consumer.processor.SAML11AssertionConsumerProcessorImpl"));

        } catch (Exception anyE) {
            Assert.fail("Error running processor factory main scenario test " + anyE.getMessage());
        }
    }

    /**
     * Test the return of a bespoke processor is correct.
     * <p/>
     * This can be used to validate all the factories work the same way too in terms of picking up
     * a bespoke implementation via a factory along the path of
     * com.ngahr.saml.impls.<lowercase_customer_code>.<whatever_the_subdirectory_is_for_the_factory>.<interface_name>Impl
     */
    @Test
    public void testBespoke() {
        try {
            final MetaDataHandler mdHandler = MetaDataHandlerFactory.getInstance(null);

            // See if we have a customer code for this user that differs from the provided Issuer details string
            final EntityTranslation lookup = new EntityTranslation(ENTITY_TRANSLATION_FILE);
            final String customerCode = lookup.lookupInternalCodeUsingEntityIdentifier(ISSUER);

            final SAML11AssertionConsumerProcessor bespokeProcessor =
                    SAML11AssertionConsumerProcessorFactory.getInstance(META_DATA_FILE, ISSUER, customerCode, SAMLConstants.SAML11P_NS, ASSERTION_CONSUMER_NAME, mdHandler);
            final Class bespokeClass = bespokeProcessor.getClass();
            assertThat(bespokeClass.getName(), is("com.timepoorprogrammer.saml.impls.nz.consumer.processor.SAML11AssertionConsumerProcessorImpl"));
        } catch (Exception anyE) {
            Assert.fail("Error running bespoke scenario test " + anyE.getMessage());
        }
    }
}
