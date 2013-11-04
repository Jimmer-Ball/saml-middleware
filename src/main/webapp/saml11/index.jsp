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
    <title>SAML1.1 scenarios</title>
    <script type="text/javascript">
        function toDo() {
            alert("This item is TODO");
        }
    </script>
</head>
<body>
<div>
    <h1>Introduction</h1>
    <p>The examples in this section illustrate how far we've got with implementing the full set of scenario
        flows required for SAML1.1 as defined in the document
        <a href="http://www.oasis-open.org/committees/download.php/6837/sstc-saml-tech-overview-1.1-cd.pdf">
            SAML V1.1 techincal overview</a>.
    </p>
</div>
<div>
    <!-- Web browser SSO Profile -->
    <h1>Web Browser Single Sign On (SSO) Profile</h1>

    <p>This is the traditional web app use cases for SAML, where some remote network sends us SAML assertions that we
        have to process using a shared Public Key Infrastructure agreement and a known format for the assertions,
        to allow us to silently login users at our application.
        <b>Note:</b>Key to these exchanges is the remote network entity sending us the "right" user identifier for our
        local application, and the remote network entity sending us the "right" metadata identifier for itself as an
        identity provider, as this is what we use to lookup the capabilities of the remote network entity from
        our shared metadata.</p>
    <ul>
        <li><a href="<c:out value="${pageContext.servletContext.contextPath}"/>/saml11/idpDetails">Source Site first (IdP) initiated SSO: POST binding</a></li>
        <li onclick="toDo();">Source Site first (IdP) initiated SSO: POST/artifact bindings</li>
        <li onclick="toDo();">Destination Site first (SP) initiated SSO: POST/artifact bindings</li>
    </ul>
    <!-- Using SAML1.1 in SOAP and REST messages -->
    <h1 onclick="toDo();">Using SAML1.1 in SOAP and REST</h1>
</div>
 <div>
    <a href="<c:out value="${pageContext.servletContext.contextPath}"/>/index.jsp">Back to main page</a>
</div>
</body>
</html>