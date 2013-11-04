Please go and read http://hrs-dev6.hrs.northgate-is.com/display/SOAStrategy/SAMLAssertion+Processing 
in FULL detail before you do anything with these files.

They are templates for routing, PKI, and logging.

The routing template saml.properties is where you should add the routing 
information that allows the SAML middleware to talk to the authoriser services
on remote applications (say MyView) and if said Authoriser says the user is 
authorised, then where you configure the route to the backdoor of the 
application.  Be really careful with the paths and with the cookie settings
so go read the file first.  Chances are you will need to amend ports and
introduce new services/remote application routing as time goes on.  The name of 
this file should not change else your assertion consumers will break.

The PKI template idp_and_saml_metadata.xml holds the metadata used to describe
the addressable endpoints that consumer and produce SAML and hold the public
X509 certificates each side of a "trust" relationship use for their respective
tasks, so encryption if you are a customer and signature checking if you are us.

The certificates in this file are initially self-signed certificates.  You really
need to know your potatoes to do PKI via trusted certificate authorities with customers
while commissioning a customer to access a service via SAML (or any PKI).  If you
don't feel up to it, talk to Matt Townley please.

The logging file log4j.xml is for Tomcat, so the middleware logs consistently to
output files in the logs directory of the Tomcat installation regardless of the 
standard commons logging setup you may have for your Tomcat instance.  So its a way
of isolating the middleware logging into seperate application and audit logs.  Please be
aware the application logging (see the log4j.xml) is set to DEBUG initially, so
if you want to cut down the chatter on the application following deployment amend 
the DEBUG threshold values you find in the file to INFO.  You will have to do this 
yourself.

So, in summary, be careful, you have to understand the principles of SAML assertion
consumption, circles of trust, routing within Hosting, and Tomcat before you can
sensibly change these files if donig a Tomcat install in hosting.

Jim Ball (03/09/2010)
