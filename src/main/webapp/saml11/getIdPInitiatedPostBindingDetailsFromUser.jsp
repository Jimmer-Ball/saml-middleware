<%@page pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
    response.setDateHeader("Expires", 0); // Proxies.
%>
<!DOCTYPE html>
<!-- Get hold of the user identifier and extra data to use for a SAML 1.1 source site initiated POST binding scenario
     including the service we want send an assertion to target -->
<html>
    <head>
        <title>SAML1.1: Source Site first (IdP) initiated POST binding SAML Assertion Details</title>
    </head>
    <body>
        <div>
            <form id="detailsForm" action="<c:out value="${pageContext.servletContext.contextPath}"/>/SAML11AssertionProducer" method="POST">
                <label id="serviceLabel" for="service">Service to send to:</label>
                <input type="text" id="service" name="service" value="DummyApp">
                <label id="userIdentifierNameLabel" for="userIdentifierName">User Identifier:</label>
                <input type="text" id="userIdentifierName" name="userIdentifierName" value="">
                <!-- In SAML1.1 the extra information is called TARGET -->
                <label id="TARGETLabel" for="TARGET">Extra information (say module to use) we could provide (if any):</label>
                <input type="text" id="TARGET" name="TARGET" value="">
                <input type="submit" id="createResponse" name="createResponse" value="Create Response">
            </form>
        </div>
    </body>
</html>