package com.timepoorprogrammer.saml.impls.ga;

import com.timepoorprogrammer.saml.TestHelper;
import com.timepoorprogrammer.saml.configuration.ConfigurationProperties;
import com.timepoorprogrammer.saml.impls.ga.common.AssertionContents;
import com.timepoorprogrammer.saml.impls.ga.common.ProducerConfiguration;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test class for the Goldman Sachs (GA) specific producer and
 * configuration which serves up a secret key for use in Goldman
 * Sachs's "roll-your-own" assertion security.
 *
 * @author Jim Ball
 */
public class ProducerConfigurationTest {
    /**
     * Logging handle
     */
    private static final Logger log = LoggerFactory.getLogger(ProducerConfigurationTest.class);

    private static final String PROPERTIES_FILE = TestHelper.getFullPath("^.*fixtures\\\\configuration\\\\saml.properties$");

    /**
     * Test the generation and validation of a Message Authentication Code (MAC) and test the
     * extraction of the identifier and time stamp from the parseable data provided to test
     * whether the time of reception is within the window of
     */
    @Test
    public void testMainScenario() {
        try {
            // ==============================
            // Setup some data we can compare
            // ==============================
            // Create a dataString representing something that Goldman Sachs might send us.
            final Long thirtySecondsInThePast = (System.currentTimeMillis() / 1000) - 30;
            log.info("Thirty seconds in the past is " + thirtySecondsInThePast);
            final String dataString = Long.toString(thirtySecondsInThePast) + "|" + "189502";
            log.info("The dataString is " + dataString);
            // Create a key object from the shared secret key.  Note this key has to match what is provided
            // in the saml.properties file as trhe shared secret else this test will fail.
            final String sharedSecretKey = "ABCDE123435678FGHIJKLM9876543210ABCDEFGH";
            final SecretKey key = new SecretKeySpec(sharedSecretKey.getBytes(), ProducerConfiguration.KEY_ALGORITHM);
            // Generate the MAC from the shared secret key
            Mac mac = Mac.getInstance(key.getAlgorithm());
            mac.init(key);
            byte[] byteData = dataString.getBytes();
            byte[] macBytes = mac.doFinal(byteData);
            String macAsString = new String(Hex.encodeHex(macBytes));
            log.info("Locally generated MAC is: " + macAsString);

            // =======================================================================
            // Compare the locally generated MAC to what we get from our configuration
            // =======================================================================
            final ConfigurationProperties properties = new ConfigurationProperties(PROPERTIES_FILE);
            final ProducerConfiguration configuration = new ProducerConfiguration(properties, "GA");

            assertThat(configuration.getAcceptableTimeDrift(), is(120));
            assertThat(configuration.getSharedKey(), is(sharedSecretKey));
            assertThat(configuration.generateMAC(dataString), is(macAsString));

            // =====================================================================
            // Go the whole way and complete a functional test of assertion contents
            // =====================================================================
            final AssertionContents contents = new AssertionContents(dataString);
            assertThat(contents.getIdentifier(), is("189502"));
            assertThat(contents.getReceivedSecondsTimestamp(), is(thirtySecondsInThePast));
            assertTrue(contents.withinTimeTolerance(configuration.getAcceptableTimeDrift()));

        } catch (Exception anyE) {
            Assert.fail("Error processing Goldman Sachs configuration: " + anyE.getMessage());
        }
    }
}
