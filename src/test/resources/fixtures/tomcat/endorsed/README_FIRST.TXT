This directory holds version 2.7.1 of the Java API for XML processing libraries.

JBoss uses 2.7.0 you'll find under JBOSS_HOME/lib/endorsed and Tomcat uses an even older version.

We want ANY web application that needs to manage SAML2 structures to use 
OpenSAML which, for JBoss, according to the blogosphere, means creating an application
that holds a WEB-INF/lib/endorsed directory holding these endorsed libraries
instead of attempting to apply the ones held in the JBOSS endorsed directory.

This works according to 
http://groups.google.com/group/opensaml-users/browse_thread/thread/43886c0857125cd3

Jim Ball (11/06/2010)

For Tomcat, an endorsed subdirectory present in a web application's WEB -INF/lib directory gets
ignored by Tomcat, so instead you need an endorsed sub-directory under Tomcat's root installation
directory to effect the equivalent.  This means any application running on that instance of Tomcat
will use more up-to-date XML parsing than is provided by default.

Jim Ball (03/09/2010)
