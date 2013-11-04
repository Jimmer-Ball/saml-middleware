This module holds the fixture data used by the test and integration
modules and used in the working version of the software.

This whole module is included as a dependency by the test module,
so the unit tests can pickup XML files holding SAML2 statements
so we can test the parsing capabilities of the library, and can pickup 
keystore details to test the keystore reading capabilities of the library.

If you want to run the software example servlets first read the WIKI at
http://confluence/display/SOAStrategy/SAMLAssertion+Processing,
then the idp_and_sp_metadata.xml file holding the SAML metadata, and finally
the saml.properties holding the routing data. This way you will find out for sure 
what files you need to copy where depending on whether you are testing on a JBoss
instance with both the middleware and the dummy application or, more realistically
testing the middleware on Tomcat with the application MyView running on a different 
host under JBoss.

JB (11/06/2010 && 07/07/2010 && 17/09/2010)

