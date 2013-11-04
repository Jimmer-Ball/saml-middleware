<%@page pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
    response.setDateHeader("Expires", 0); // Proxies.
%>
<!DOCTYPE html>
<!-- Print out the content of an error if we have been redirected here and use redirect params as this is domain transferable -->
<html>
<head>
    <title>Error</title>
</head>
<body>
<div>
    <p>We've been redirected here due to an error in processing at the assertion consumer. So this
        is where the assertion consumer instructs the browser to redirect to on a processing error.</p>
</div>
<div>
    <h1>ErrorCode:</h1>
</div>
<div>
    <c:if test="${not empty param.errorCode}">
        <p><c:out value="${param.errorCode}"/></p>
    </c:if>
</div>
<div>
    <h1>ErrorDetails:</h1>
</div>
<div>
     <c:if test="${not empty param.errorDetails}">
        <p><c:out value="${param.errorDetails}"/></p>
    </c:if>
</div>
<div>
    <a href="<c:out value="${pageContext.servletContext.contextPath}"/>/index.jsp">Back to index page</a>
</div>
</body>
</html>