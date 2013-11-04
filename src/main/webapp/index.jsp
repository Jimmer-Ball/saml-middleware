<%@page pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
    response.setDateHeader("Expires", 0); // Proxies.
%>
<!DOCTYPE html>
<html>
<head>
    <title>Welcome to the SAML middleware project</title>
    <script type="text/javascript">
        function toDo() {
            alert("This item is TODO");
        }
    </script>
</head>
<body>
<div>
    <h1>Introduction</h1>
    <p>It is assumed that you have read and understood the project
        <a href="http://confluence/display/SOAStrategy/SAMLAssertion+Processing">WIKI page</a>
        prior to using this UI.</p>
    <p>This is middleware that supports the <i>validation of authentication information we receive from customers</i>
    to allow their users to access applications silently without providing credentials during cross-domain single
    sign-on.  In most cases validation of authentication information means processing SAML assertions we receive from
    our customers, but not always, as some customers have custom assertions.  This UI allows the commissioner to
    validate that the SAML meta-data provided by a customer makes sense, that the back-door authorisation at a service
    and the subsequent re-routing of a user's browser to the right part of that service is working correctly
    <b>without</b> needing to involve the customer</p>
    <p>SAML (Security Assertion Markup Language) is a growing standard for cross domain interaction that
        has five key purposes:</p>
    <ul>
        <li>It can be used to access web applications in our network (say MyView) from somebody else's
            network, where the remote network maintains identity information that allows it to send the
            correct application identifier for our local application to apply on login, in things called
            SAML assertions. The aim of this is to silently login a particular user to our application.
            This is known as cross domain web single sign on.
        </li>
        <li>It can be used to assert that a particular identity has been authenticated (think "logged on")
            by providing a SAML assertion in the Web Services security header within a SOAP request. This is
            increasingly used for SOAP interactions that require an identity and authorisation context, and can
            also be used for non-SOAP, more REST-ful applications like the mobile initiative.
        </li>
        <li>It can be used by our applications (say the Aurora portal) to send SAML assertions to somebody
            else's applications within a different network (either web application or web service) to assert
            that a particular identity (user) should be allowed access to the remote application or web service.
        </li>
        <li>It can be used to describe "trust" metadata that fully outlines the source of an assertion (Identity Provider)
            and destination of an assertion (Service Provider) in a mutual relationship. So what SAML scenarios each can
            perform, what endpoints they have (were SAML gets sent), what encryption should be used and how, what
            certificates there are to use for a Public Key Infrastructure (PKI), and what shape of assertions and
            attributes should be given and are supported. So, if you will, the metadata describes the <b>public contract</b>
            between an IdP and SP in a service relationship and vice-versa.
        </li>
    </ul>
    <p>There are two versions of SAML that NGA will support, SAML1.1 and SAML2</p>
    <p>The difference between the versions is in the number, complexity, and standards based definition of the flows
        between identity providers and service providers supported by the two levels of the protocol, and the level of
        security each provides.  For example, in SAML1.1, assertions cannot be encrypted, and only the browser-post
        profile is officially supported through the profile definitions, and the concepts of Identity Provider and
        Service Provider are only unofficially part of the 1.1 specification.</p>

    <h2>Choose a SAML version to see the scenarios we can currently support</h2>
    <p>Use the links below to navigate to the index pages that go through the different scenarios currently supported for
        each SAML version:</p>
    <div>
        <a href="<c:out value="${pageContext.servletContext.contextPath}"/>/saml2/index.jsp">SAML2 index page</a>
    </div>
     <div>
        <a href="<c:out value="${pageContext.servletContext.contextPath}"/>/saml11/index.jsp">SAML1.1 index page</a>
    </div>

    <h2>Choose a bespoke non-SAML based authentication validation implementation we also support</h2>
    <p>Use the links below to navigate to the right non-SAML based bespoke implementation testing page.</p>
    <div>
        <a href="<c:out value="${pageContext.servletContext.contextPath}"/>/ga/getUserDetails.jsp">Goldman Sachs</a>
    </div>
</div>
</body>
</html>