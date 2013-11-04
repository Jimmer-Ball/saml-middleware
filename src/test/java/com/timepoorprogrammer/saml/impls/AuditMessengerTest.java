package com.timepoorprogrammer.saml.impls;

import static org.hamcrest.CoreMatchers.is;
import org.junit.Assert;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 * Test class for the AuditMessenger which also illustrates how every single
 * factory in the whole project actually works in terms of picking up default
 * implementations and bespoke implementations from a standard sub package
 * organsiation to be used for every single customer bespoke work needs to be done for.
 *
 * @author Jim Ball
 */
public class AuditMessengerTest {
    /**
     * Test the return of the default audit messenger is correct.
     * <p/>
     * This can be used to validate all the factories work the same way too in terms of picking up
     * a bespoke implementation via a factory along the path of
     * com.northgatearinso.saml.impls.<lowercase_customer_code>.<whatever_the_subdirectory_is_for_the_factory>.<interface_name>Impl
     */
    @Test
    public void testDefaults() {
        try {
            // This is an explicit call to pick up the standard package implementation of the
            // audit messenger and is the default behaviour for any factory.  If you were
            // hoping to pick up a bespoke class then you would provide the uppercase
            // customer code here instead and would make sure you had an implementation in the
            // right place which in full path terms is going to be
            // com.northgatearinso.saml.impls.<lowercase_customer_code>.<whatever_the_subdirectory_is_for_the_factory>.<interface_name>Impl
            //
            // This way any customer specific code can be accessed from factories that work in a common
            // way, in packages in a common location, and from classes with a common name pattern.
            final AuditMessenger explicitMessenger = AuditMessengerFactory.getInstance("STANDARD");
            // This is to pickup the default mechanism when the customer  a null
            final AuditMessenger defaultMessenger = AuditMessengerFactory.getInstance(null);
            // And when the implementation doesn't exist
            final AuditMessenger missingMessenger = AuditMessengerFactory.getInstance("DOESN'T EXIST");

            // Note, all the three behaviours above work in the same way.
            final Class explicitClass = explicitMessenger.getClass();
            final Class defaultDueToNullCustomerCodeClass = defaultMessenger.getClass();
            final Class defaultDueToMissingCustomerCodeClass = missingMessenger.getClass();

            assertThat(explicitClass.getName(), is("com.timepoorprogrammer.saml.impls.standard.common.AuditMessengerImpl"));
            assertThat(defaultDueToNullCustomerCodeClass.getName(), is("com.timepoorprogrammer.saml.impls.standard.common.AuditMessengerImpl"));
            assertThat(defaultDueToMissingCustomerCodeClass.getName(), is("com.timepoorprogrammer.saml.impls.standard.common.AuditMessengerImpl"));
            assertThat(explicitClass.getName(), is(defaultDueToNullCustomerCodeClass.getName()));
            assertThat(explicitClass.getName(), is(defaultDueToMissingCustomerCodeClass.getName()));
            assertThat(defaultDueToNullCustomerCodeClass.getName(), is(defaultDueToMissingCustomerCodeClass.getName()));
        } catch (Exception anyE) {
            Assert.fail("Error running audit messenger factory main scenario test " + anyE.getMessage());
        }
    }

    /**
     * Test the return of a bespoke messenger is correct.
     * <p/>
     * This can be used to validate all the factories work the same way too in terms of picking up
     * a bespoke implementation via a factory along the path of
     * com.northgatearinso.saml.impls.<lowercase_customer_code>.<whatever_the_subdirectory_is_for_the_factory>.<interface_name>Impl
     */
    @Test
    public void testBespoke() {
        try {
            // To illustrate the specialisation we'll have a bespoke implementation under the local
            // ga sub-package in the test module.
            final AuditMessenger bespokeMessenger = AuditMessengerFactory.getInstance("GA");
            final Class bespokeClass = bespokeMessenger.getClass();
            assertThat(bespokeClass.getName(), is("com.timepoorprogrammer.saml.impls.ga.common.AuditMessengerImpl"));
        } catch (Exception anyE) {
            Assert.fail("Error running audit messenger factory bespoke scenario test " + anyE.getMessage());
        }

    }
}
