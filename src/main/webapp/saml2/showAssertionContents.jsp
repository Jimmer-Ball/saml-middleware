<%@page pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
    response.setDateHeader("Expires", 0); // Proxies.
%>
<!DOCTYPE html>
<!-- Print out the content of a received assertion -->
<html>
    <head>
        <title>SAML2: Assertion contents</title>
    </head>
    <body>
        <!-- Assertion details -->
        <div>
             <p>Showing the internal contents of an assertion</p>
            <div>
                <h1>Assertion Contents:</h1>          
            </div>
            <div>
                <c:if test="${not empty sessionScope.SAMLAssertionText}">
                    <TEXTAREA name="assertionText" rows="30" cols="100">
                        <c:out value="${sessionScope.SAMLAssertionText}"/>
                    </TEXTAREA>
                </c:if>
            </div>
        </div>
        <!-- RelayState details -->
         <div>
            <div>
                <h1>RelayState Details:</h1>
            </div>
            <div>
                <c:if test="${not empty sessionScope.RelayState}">
                    <p><c:out value="${sessionScope.RelayState}"/></p>
                </c:if>
            </div>
        </div>
        <div>
            <a href="<c:out value="${pageContext.servletContext.contextPath}"/>/saml2/index.jsp">Back to SAML2 index page</a>
        </div>
    </body>
</html>