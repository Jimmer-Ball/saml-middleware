package com.timepoorprogrammer.saml.impls;

import com.timepoorprogrammer.saml.TestHelper;
import com.timepoorprogrammer.saml.configuration.ConfigurationProperties;
import com.timepoorprogrammer.saml.configuration.EntityTranslation;
import com.timepoorprogrammer.saml.configuration.ProducerConfiguration;
import com.timepoorprogrammer.saml.core.SAML11Handler;
import static org.hamcrest.CoreMatchers.is;

import org.junit.Assert;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.opensaml.common.xml.SAMLConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for the assertion producer which also illustrates how every single
 * factory in the whole project actually works in terms of picking up default
 * implementations and bespoke implementations from a standard sub package
 * organisation to be used for every single customer bespoke work needs to be done for.
 *
 * @author Jim Ball
 */
public class SAML11AssertionProducerProcessorTest {
    private static final Logger log = LoggerFactory.getLogger(SAML11AssertionProducerProcessorTest.class);
    /**
     * Default setting for producerCustomerCode. Namely, from a SAML metadata point of view, who am I?
     */
    private static final String DEFAULT_WHO_AM_I = "idp_saml11";

    /**
     * The name of the assertion consumer we are
     */
    private static final String SERVICE_NAME = "MyView";

    /**
     * Properties file used by the assertion consumer
     */
    private static final String PROPERTIES_FILE = TestHelper.getFullPath("^.*fixtures\\\\configuration\\\\saml.properties$");

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
     * Path to key file holding the private key needed to do digital signatures
     */
    private static final String PRIVATE_SIGNING_KEY_FILE = TestHelper.getFullPath("^.*fixtures\\\\keystores\\\\clientKeyStore.jks$");


    // To test a processor requires an initialised SAML library which means an initialed handler
    private static final SAML11Handler GOTTA_HAVE_A_HANDLER = new SAML11Handler();

    /**
     * Test the return of the default SAML11AssertionProducerProcessor is correct.
     * <p/>
     * This can be used to validate all the factories work the same way too in terms of picking up
     * a bespoke implementation via a factory along the path of
     * com.ngahr.saml.impls.<lowercase_serviceCode>.<whatever_the_subdirectory_is_for_the_factory>.<interface_name>Impl
     */
    @Test
    public void testDefaults() {
        try {
            // Get hold of our configuration now, as we need to stay adaptable to configuration change
            final ConfigurationProperties props = new ConfigurationProperties(PROPERTIES_FILE);
            // We can be configured to pretend to be a particular customer producer as needed, or to just apply the defaults
            // We can be configured to pretend to be a particular customer producer as needed, or to just apply the defaults
            final String producerCode = props.getProducerCode(DEFAULT_WHO_AM_I);
            log.debug("Producer code is {}", producerCode);
            final ProducerConfiguration config = new ProducerConfiguration(props, producerCode);
            final MetaDataHandler mdHandler = MetaDataHandlerFactory.getInstance(null);

            // Translate the producerCode into the producer entity identifier for outbound SAML and set the issuer
            // details on the SAML handler accordingly.
            final EntityTranslation lookup = new EntityTranslation(ENTITY_TRANSLATION_FILE);
            final String issuer = lookup.lookupEntityIdentifierUsingInternalCode(producerCode);
            log.debug("Producer Issuer is {}", issuer);
            GOTTA_HAVE_A_HANDLER.setIssuer(issuer);

            // Get the service entity identifier for outbound SAML given the serviceCode
            final String serviceCode = "DummyApp";
            log.debug("Service code is {}", serviceCode);
            final String serviceEntityIdentifier = lookup.lookupEntityIdentifierUsingInternalCode(serviceCode);
            log.debug("Service entity identifier is {}", serviceEntityIdentifier);

            // Setup the assertion producer processor that tells us if we need to encrypt and sign
            // This will react to dynamic changes in the metadata without having to restart so we can
            // take new trust relationships without restarting.
            SAML11AssertionProducerProcessor defaultProcessor = SAML11AssertionProducerProcessorFactory.getInstance(
                    META_DATA_FILE,
                    issuer,
                    SAMLConstants.SAML11P_NS,
                    serviceCode,
                    serviceEntityIdentifier,
                    mdHandler,
                    PRIVATE_SIGNING_KEY_FILE,
                    config.getKeyStorePassword(),
                    config.getSigningKeyAlias(),
                    config.getSigningKeyPassword());
            final Class defaultClass = defaultProcessor.getClass();
            assertThat(defaultClass.getName(), is("com.timepoorprogrammer.saml.impls.standard.producer.processor.SAML11AssertionProducerProcessorImpl"));
        } catch (Exception anyE) {
            Assert.fail("Error running processor factory main scenario test " + anyE.getMessage());
        }
    }

    /**
     * Test the return of a bespoke processor is correct.
     * <p/>
     * This can be used to validate all the factories work the same way too in terms of picking up
     * a bespoke implementation via a factory along the path of
     * com.ngahr.saml.impls.<lowercase_serviceCode>.<whatever_the_subdirectory_is_for_the_factory>.<interface_name>Impl
     */
    @Test
    public void testBespoke() {
        try {
            // We can be configured to pretend to be a particular customer producer as needed, or to just apply the defaults
            final ConfigurationProperties props = new ConfigurationProperties(PROPERTIES_FILE);
            final String producerCode = props.getProducerCode(DEFAULT_WHO_AM_I);
            log.debug("Producer code is {}", producerCode);
            final ProducerConfiguration config = new ProducerConfiguration(props, producerCode);
            final MetaDataHandler mdHandler = MetaDataHandlerFactory.getInstance(null);

            // Translate the producerCode into the producer entity identifier for outbound SAML and set the issuer
            // details on the SAML handler accordingly.
            final EntityTranslation lookup = new EntityTranslation(ENTITY_TRANSLATION_FILE);
            final String issuer = lookup.lookupEntityIdentifierUsingInternalCode(producerCode);
            log.debug("Producer Issuer is {}", issuer);
            GOTTA_HAVE_A_HANDLER.setIssuer(issuer);

            // Get the service entity identifier for outbound SAML given the serviceCode
            final String serviceCode = SERVICE_NAME;
            log.debug("Service code is {}", serviceCode);
            final String serviceEntityIdentifier = lookup.lookupEntityIdentifierUsingInternalCode(serviceCode);
            log.debug("Service entity identifier is {}", serviceEntityIdentifier);

            // Setup the assertion producer processor that tells us if we need to encrypt and sign
            // This will react to dynamic changes in the metadata without having to restart so we can
            // take new trust relationships without restarting.
            SAML11AssertionProducerProcessor bespokeProcessor = SAML11AssertionProducerProcessorFactory.getInstance(
                    META_DATA_FILE,
                    issuer,
                    SAMLConstants.SAML11P_NS,
                    serviceCode,
                    serviceEntityIdentifier,
                    mdHandler,
                    PRIVATE_SIGNING_KEY_FILE,
                    config.getKeyStorePassword(),
                    config.getSigningKeyAlias(),
                    config.getSigningKeyPassword());
            final Class bespokeClass = bespokeProcessor.getClass();
            assertThat(bespokeClass.getName(), is("com.timepoorprogrammer.saml.impls.myview.producer.processor.SAML11AssertionProducerProcessorImpl"));

        } catch (Exception anyE) {
            Assert.fail("Error running processor factory main scenario test " + anyE.getMessage());
        }
    }
}
