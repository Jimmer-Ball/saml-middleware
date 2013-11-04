<%@page pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
    response.setDateHeader("Expires", 0); // Proxies.
%>
<!DOCTYPE html>
<!-- Send the SAMLResponse held in session (created by SAMLAssertionProducer) off to the remote
     assertion consumer service depending on the metadata. -->
<html>
    <head>
        <title>SAML2: IdP initiated POST binding SAML Assertion Sender</title>
    </head>
    <body>
        <div>
            <c:if test="${not empty sessionScope.prettySAMLResponse}">
                <TEXTAREA name="signedResponseHoldingEncryptedAssertion" rows="30" cols="100">
                    <c:out value="${sessionScope.prettySAMLResponse}"/>
                </TEXTAREA>
            </c:if>
        </div>
        <div>
            <!-- The form contains two hidden fields, one called SAMLResponse and one called RelayState, each
                 holding their respective values.  If RelayState is unset its not added.  Also, the destination
                 comes from the SAML metadata for the peer protocol assertion consumer service at the remote
                 service provider -->
            <form id="senderForm" action="<c:out value="${sessionScope.destination}"/>" method="POST">
                <input type="hidden" name="SAMLResponse" value="<c:out value="${sessionScope.SAMLResponse}"/>"/>
                <c:if test="${not empty sessionScope.RelayState}">
                    <input type="hidden" name="RelayState" value="<c:out value="${sessionScope.RelayState}"/>"/>
                </c:if>
                <input type="submit" id="sendResponse" name="sendResponse" value="Send Response">
            </form>
        </div>
    </body>
</html>