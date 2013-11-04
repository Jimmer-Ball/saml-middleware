On The WIKI site http://hrs-dev6.hrs.northgate-is.com/display/
SOAStrategy/SAMLAssertion+Processing, you will find an 
attachment, amendments_for_tomcat.zip.  This ZIP holds the 
set of amendments you would make to a vanilla Tomcat 6.0.20
installation to get it to run the SAML middleware.

Now, you do need to know your Tomcat before you do this.  So
if you don't well go and learn, like say from the documentation
at http://tomcat.apache.org/tomcat-6.0-doc/index.html.

I HAVE NOT "HARDENED" the tomcat installation in anyway, all
I've done is provide these installation instructions for Tomcat.

It is "NOT" my responsibility to provide battle hardening instructions
for secure tomcat installations for hosting.  This should be hosting's
bread and butter, not mine.

So be aware, the admin web UI and the Tomcat examples, and the default 
fluff that comes with a vanilla Tomcat 6.0.20, will still be in with 
a vanilla Tomcat regardless of whether you follow these instructions 
for setting up the SAML middleware.

You are responsible for the hardening of Tomcat instances, not me.

Jim Ball (17/09/2010)
