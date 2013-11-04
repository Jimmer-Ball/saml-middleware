<%@page pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
    response.setDateHeader("Expires", 0); // Proxies.
%>
<!DOCTYPE html>
<%@page import="com.timepoorprogrammer.saml.soak.SoakResults"%>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.TimeZone" %>
<div class="padding">
</div>
<%
    final SoakResults results = (SoakResults) request.getAttribute("results");
    // Same format as the standard JavaScript Mozilla full format in GMT
    final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
%>
    <table class="datatable" border="1">
        <tr>
            <th>Server Start time</th>
            <th>Server End time</th>
            <th>Expected rate per second</th>
            <th>Sleep interval applied (Ms)</th>
            <th>Number of assertions processed</th>
            <th>Successful count</th>
            <th>Error count</th>
            <th>Elapsed time</th>
            <th>AVG success rate per second</th>
        </tr>
        <tr>
            <td><%=dateFormat.format(results.getStartTime())%></td>
            <td><%=dateFormat.format(results.getEndTime())%></td>
            <td><%=results.getExpectedRatePerSecond()%></td>
            <td><%=results.getSleepIntervalApplied()%></td>
            <td><%=results.getNumberOfCalls()%></td>
            <td><%=results.getNumberOfSuccessfullCalls()%></td>
            <td><%=results.getNumberOfErrorCalls()%></td>
            <td><%=results.getElapsedTime()%></td>
            <td><%=results.getAverageSuccessRatePerSecond()%></td>
        </tr>
    </table>