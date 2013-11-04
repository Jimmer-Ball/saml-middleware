<%@page pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
    response.setDateHeader("Expires", 0); // Proxies.
%>
<!DOCTYPE html>
<!-- SAML soak test start -->
<html>
<head>
    <title>SAML SOAK test start</title>

    <link rel="stylesheet" type="text/css" href="styles.css"/>
    <!--  Import the required support JS libraries -->
    <script type="text/javascript" src="<c:out value="${pageContext.servletContext.contextPath}"/>/common.js"></script>
    <script type="text/javascript" src="<c:out value="${pageContext.servletContext.contextPath}"/>/ajax.js"></script>
    <script type="text/javascript" src="<c:out value="${pageContext.servletContext.contextPath}"/>/helptips.js"></script>
    <script type="text/javascript" src="<c:out value="${pageContext.servletContext.contextPath}"/>/SoakClient.js"></script>
    <script type="text/javascript">
        // Create a new SoakClient to deal with the back-end
        var soakClient = new SoakClient();
    </script>
</head>
<body>
<!-- Give the help tips some DOM to hang off -->
<div id="helptip">
</div>
<div>
    <h1>Introduction</h1>
    <p class="welcomeBlock">
        This SOAK test client is used to soak a local or remote copy of the SAML middleware with assertion consumption
        requests to see how it behaves under load.  Amend the soakConsumer section of your SAML metadata to determine
        where you'll be sending your assertions.  This client sets up an assertion producer to repeat call a
        <i>custom assertion consumer at the middleware under test that does <b>not</b> do request redirection</i> to
        applications. We are testing the performance of the middleware for assertion consumption, and not the
        performance of the applications or the performance of the network with respect to browser redirection or
        calls to the Authorise service on remote applications. So use this to see if the middleware <i>leaks</i> under
        load, or has issues with connection maangement, etc.  Stress it basically is what this is for.
    </p>

    <p class="welcomeBlock">
        You can have many of these clients working at the same time, pointing to the same middleware to be soaked, but
        provide each with different traffic settings. This way you can <b>really</b> make the middleware work hard.
        Simply make sure the SAML metadata (e.g. idp_and_sp_metadata.xml) entry for soakConsumer is pointing to the
        middleware you want to stress.
    </p>

    <p class="welcomeBlock">
        There are several traffic settings you can use to vary the content and rapidity of the SAML traffic sent.
        The tooltips explain them.  Please note <b>all</b> times are in GMT notation, so it doesn't matter whether
        your browser is in a different locale from the server sending the assertions, the time reference points are
        the same on the client and ther server.
    </p>
</div>
<div>
    <h1>Settings</h1>
    <form id="detailsForm" action="">
        <table class="entrytable">
            <tr>
                <th>Identity Provider customer code:</th>
                <td><input type="text" name="idp" value="idp_saml2"
                        onmouseover="showhelptip('Set the identity provider source, so <i>TW</i>, <i>idp_saml11</i>, <i>idp_saml2</i>, whatever the customer code is. The default is <i>idp_saml2</i> as this uses the PKI already bundled with the middleware.', 200);"
                        onmouseout="hidehelptip()"/></td>
            </tr>
            <tr>
                <th>SAML type to apply:</th>
                <td><input type="text" name="samlType" value="SAML2"
                        onmouseover="showhelptip('Set the SAML type, so <i>SAML2</i> or <i>SAML1.1</i>. The default is <i>SAML2</i> to match the default identity provider setting.', 200);"
                        onmouseout="hidehelptip()"/></td>
            </tr>
            <tr>
                <th>Duration scale (S, M, H, D):</th>
                <td><input type="text" name="durationScale" value="M"
                        onmouseover="showhelptip('Set the test duration scale, so Seconds, Minutes, Hours, or Days <i>(S, M, H, D)</i>. The default is <i>M</i>.', 200)"
                        onmouseout="hidehelptip()"/></td>
            </tr>
            <tr>
                <th>Duration value:</th>
                <td><input type="text" name="durationValue" value="2"
                        onmouseover="showhelptip('Set the duration value that will apply the duration scale given and so set the overall duration of the test. The default is <i>2</i>.', 200)"
                        onmouseout="hidehelptip()"/></td>
            </tr>
            <tr>
                <th>Rate per second value:</th>
                <td><input type="text" name="rateValue" value="10"
                        onmouseover="showhelptip('Set the rate value to set the overall throughput of the test. The default is <i>10</i>.', 200)"
                        onmouseout="hidehelptip()"/></td>
            </tr>
        </table>
        <input type="hidden" name="action" value="S"/>
    </form>
    <input type="button" class="input-button-save" id="go" name="go" value="GO" onclick="soakClient.startTest()">
    <input type="button" class="input-button-cancel" id="stop" name="stop" value="STOP" onclick="soakClient.stopTest()">
</div>
<div class="padding">
</div>
<div id="status">
</div>
<div id="working">
</div>
<div id="results">
</div>
</body>
</html>