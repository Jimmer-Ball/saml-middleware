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
    <title>SAML2 scenarios</title>
    <script type="text/javascript">
        function toDo() {
            alert("This item is TODO");
        }
    </script>
</head>
<body>
<div>
    <h1>Introduction</h1>
    <p>The pages in this section illustrate how far we've got with implementing the full set of scenario flows
        required for SAML2 as defined in the document
        <a href="http://www.oasis-open.org/committees/download.php/27819/sstc-saml-tech-overview-2.0-cd-02.pdf">
            SAML2 Technical Overview</a>.
    </p>
</div>
<div>
    <!-- Web browser SSO Profile -->
    <h1>Web Browser Single Sign On (SSO) Profile</h1>

    <p>These are the traditional web app use cases for SAML, where some remote network sends us SAML assertions that we
        have to process using a shared Public Key Infrastructure agreement and a known format for the assertions,
        to allow us to silently login users at our application.
        <b>Note:</b>Key to these exchanges is the remote network entity sending us the "right" user identifier for our
        local application, and the remote network entity sending us the "right" metadata identifier for itself as an
        identity provider, as this is what we use to lookup the capabilities of the remote network entity from
        our shared metadata.</p>
    <ul>
        <li><a href="<c:out value="${pageContext.servletContext.contextPath}"/>/saml2/idpDetails">Identity Provider (IdP)
            initiated SSO: POST binding</a></li>
        <li onclick="toDo();">Service Provider (SP) initiated SSO: Redirect/POST bindings</li>
        <li onclick="toDo();">Service Provider (SP) initiated SSO: POST/artifact bindings</li>
    </ul>

    <!-- Single logout profile -->
    <h1 onclick="toDo();">Single logout profile</h1>
    <!-- ECP profile -->
    <h1 onclick="toDo();">Enhanced Client And Proxy (ECP) Profile</h1>
    <!-- Managing federated identities examples -->
    <h1 onclick="toDo();">Message exchanges used to managed federated identities</h1>
    <!-- Using SAML2 in SOAP or REST messages -->
    <h1 onclick="toDo();">Using SAML2 in SOAP or REST</h1>
</div>
 <div>
    <a href="<c:out value="${pageContext.servletContext.contextPath}"/>/index.jsp">Back to main page</a>
</div>
</body>
</html>