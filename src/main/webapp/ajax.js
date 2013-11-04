var m_LoadedScripts = new Array();
/*
 * Helper method to get hold of a DOM element handle.
 *
 * param a_Id The unique name (identifier) of the DOM element 
 */
function $(a_Id) 
{ 
    return document.getElementById(a_Id); 
}
/*
 * Factory method for making XMLHttpRequests
 */
function createXMLHttpRequest()
{
    try { return new ActiveXObject("Msxml2.XMLHTTP"); } catch (e) {}
    try { return new ActiveXObject("Microsoft.XMLHTTP"); } catch (e) {}
    try { return new XMLHttpRequest(); } catch(e) {}
    alert('XMLHttpRequest not supported on your browser');
    return null;
}
/**
 * Submit a form via ajax and display the content in specified div.
 * Additionally an associated scipt can also be specified to be loaded at the same time,
 * Any parameters can be passed in as content (or can be prepared by getFormValues), and finally
 * a callback function can be applied to amend the content following its successfull processing.
 *
 * param a_URL the destination page URL
 * param a_ContainerId The ID of the DIV to be written to
 * param a_ScriptURL any new script to load the new URL content may reference 
 * param a_Args (parameter arguments of the form item=value?item=value, etc
 * param a_Callback A Callback JS function to invoke to amend the content following successful processing 
 */
function sendRequest(a_URL, a_ContainerId, a_ScriptURL, a_Args, a_CallBack)
{
    try
    {
        // Show the please wait notice, this will be over written once the results are loaded
        //showPleaseWait(a_ContainerId);
        //var pageRequest = false
        pageRequest = createXMLHttpRequest();
        pageRequest.onreadystatechange = function()
        {
            if (pageRequest.readyState == 4)
            {
                // Fix for Bugs 7795 and 7778 on session timeout
                var status = "";
                try
                {
                   status = pageRequest.status;
                }
                catch (e)
                {
                    // This catch covers Firefox's failure to translate HTTP
                    // error response codes to an XMLHttpRequest correctly.
                    alert('Your session has timed out, please login again');
                    window.onbeforeunload = null;
                    location.replace("/SAMLWeb");
                    return;
                }
                // The same, done properly for IE7
                // which does return HTTP error codes
                if (status != 200 && status != 0)
                {
                   alert('Your session has timed out, please login again.  Code provided is: ' + status);
                    window.onbeforeunload = null;
                    location.replace("/SAMLWeb");
                    return;
                }
                document.getElementById(a_ContainerId).innerHTML = pageRequest.responseText;
                // Add any specified script to the overall doc header
                // so the URL content can reference JS not yet added to the 
                // document header
                if (a_ScriptURL !== null && !isLoaded(a_ScriptURL))
                {
                    var element = document.createElement("script");
                    element.src = a_ScriptURL;
                    element.type="text/javascript";
                    document.getElementsByTagName("head")[0].appendChild(element);
                    m_LoadedScripts[m_LoadedScripts.length++] = a_ScriptURL;
                }
                // Peform any callback if specified
                if (a_CallBack !== null)
                {
                    a_CallBack(pageRequest.status);
                }
            }
        };
        // Use POST to obtain the URL content and send any arguments along
        pageRequest.open("POST", a_URL, true );
        pageRequest.setRequestHeader("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
        pageRequest.send(a_Args);
    }
    catch (e)
    {
        alert(e);
    }
}
/**
 * Returns true if the script for the given URL has already been loaded, so we
 * don't add JS scripts to the document header that are already loaded. 
 */
function isLoaded(a_URL)
{
    var result = false;
    if (a_URL !== null)
    {
        for (script in m_LoadedScripts)
        {
            if (script == a_URL)
            {
                result = true;
                break;
            }
        }
    }    
    return result;
}

