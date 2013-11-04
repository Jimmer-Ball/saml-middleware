<%@page pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
    response.setDateHeader("Expires", 0); // Proxies.
%>
<!DOCTYPE html>
<!-- Send the Goldman Sachs details in session (created by ga/AssertionProducerServlet) off to
     the Goldman Sachs assertion consumer service (ga/AssertionConsumerServlet) -->
<html>
    <head>
        <title>Goldman Sachs assertion sender</title>
    </head>
    <body>

        <div>
             <p>POST body contents for Goldman Sachs:</p>
            <c:if test="${not empty sessionScope.ID_STRING}">
                <c:choose>
                    <c:when test="${not empty sessionScope.SERVICE}">
                        <TEXTAREA name="data" rows="30" cols="100">
                            <c:out value="ID_STRING=${sessionScope.ID_STRING}"/>
                            <c:out value="SIGNATURE=${sessionScope.SIGNATURE}"/>
                            <c:out value="SERVICE=${sessionScope.SERVICE}"/>
                        </TEXTAREA>
                    </c:when>
                    <c:otherwise>
                        <TEXTAREA name="data" rows="30" cols="100">
                            <c:out value="ID_STRING=${sessionScope.ID_STRING}"/>
                            <c:out value="SIGNATURE=${sessionScope.SIGNATURE}"/>
                        </TEXTAREA>
                    </c:otherwise>
                </c:choose>
            </c:if>
        </div>
        <div>
            <p>Send it to the consumer:</p>
            <!-- Send data direct to the Goldman Sachs assertion consumer along with a SERVICE setting to
                 allow us to commission the consumer slowly, namely direct it to DummyApp first, and then
                 the actual service itself when ready -->
            <form id="senderForm" action="<%= request.getContextPath()%>/ga/AssertionConsumer" method="POST">
                <input type="hidden" name="ID_STRING" value="${sessionScope.ID_STRING}"/>
                <input type="hidden" name="SIGNATURE" value="${sessionScope.SIGNATURE}"/>
                <c:if test="${not empty sessionScope.SERVICE}">
                    <input type="hidden" name="SERVICE" value="${sessionScope.SERVICE}"/>
                </c:if>
                <input type="submit" id="sendResponse" name="sendResponse" value="Send Assertion">
            </form>
        </div>
    </body>
</html>