<%@page pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
    response.setDateHeader("Expires", 0); // Proxies.
%>
<!DOCTYPE html>
<!-- Get hold of the user identifier and timeout to apply -->
<html>
    <head>
        <title>Goldman Sachs: Create assertion</title>
    </head>
    <body>
    <div>
        <p>
            The bespoke assertion consumer for Goldman Sachs can be found at <i>path to middleware</i>/ga/AssertionConsumer
            and this is what the customer will address from their portal.  To test the consumer we also need to be able to call the
            assertion consumer from the SAML middleware management UI.  Hence this screen.  It sits on top of a bespoke assertion
            producer for Goldman Sachs (GA).  All bespoke consumers outside the SAML framework but implemented at this middleware
            can apply this <i>create a new UI screen per consumer</i> pattern when we need to allow hosting to commission the
            new route in from the new customer.
        </p>
    </div>
        <div>
            <form id="detailsForm" action="<c:out value="${pageContext.servletContext.contextPath}"/>/ga/AssertionProducer" method="POST">
                <label id="serviceLabel" for="service">Service to send to:</label>
                <input type="text" id="service" name="service" value="DummyApp">
                <label id="userIdentifierLabel" for="userIdentifier">User Identifier:</label>
                <input type="text" id="userIdentifier" name="userIdentifier" value="189502">
                <label id="timeoutLabel" for="timeout">Timeout (seconds):</label>
                <input type="text" id="timeout" name="timeout" value="30">
                <input type="submit" id="submit" name="sumbit" value="Create Assertion">
            </form>
        </div>
    </body>
</html>