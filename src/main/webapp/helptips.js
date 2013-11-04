// ------------------------------------------
// Offset the tip location from the container 
// which has the associated tip.
// ------------------------------------------
var offsetxpoint=-20
var offsetypoint=20
// ----------------------------------------
// Working out what kind of browser we have
// ----------------------------------------
var ie=document.all;
var ns6=document.getElementById && !document.all;
// --------------------------------------
// Get a handle to the global placemarker
// --------------------------------------
var tipobj=null;
// ----------------------------
// Set the enable flag to false
// ----------------------------
var enabletip=false;
// -------------------------------------------
// When you move the mouse about in the tipped
// area the tip moves with you. This also 
// provides the event we need.
// -------------------------------------------
document.onmousemove=positiontip
// ---------------------------------------
// Return the right kind of element for IE
// to position off.
// ---------------------------------------
function ietruebody()
{
    return (document.compatMode && document.compatMode!="BackCompat")? document.documentElement : document.body;
}
// --------------------------
// Show the help tip with the 
// input text and popup width
// --------------------------
function showhelptip(thetext, thewidth)
{
    // -----------------------
    // Grab the global tip div
    // -----------------------
    tipobj=$("helptip");
    if (tipobj !== null)
    {    
        if (typeof thewidth!="undefined")
        {
            tipobj.style.width=thewidth+"px";
        }
        tipobj.innerHTML=thetext;
        enabletip=true;
    }
    else
    {
        alert("Cannot find the helptip within the document structure");
    }
}
// --------------------------------------------
// Position the help tip relative to the mouse.
// Most of the details in here are conditional 
// on the browser type.
// --------------------------------------------
function positiontip(e)
{
    if (enabletip)
    {
        // -----------------------------------------------------
        // Get the cursor position depending on the browser type
        // -----------------------------------------------------
        var curX=(ns6)?e.pageX : event.clientX+ietruebody().scrollLeft;
        var curY=(ns6)?e.pageY : event.clientY+ietruebody().scrollTop;
        // -----------------------------------------------------------
        // Find out how close the mouse is to the corner of the window
        // -----------------------------------------------------------
        var rightedge=ie&&!window.opera? ietruebody().clientWidth-event.clientX-offsetxpoint : window.innerWidth-e.clientX-offsetxpoint-20;
        var bottomedge=ie&&!window.opera? ietruebody().clientHeight-event.clientY-offsetypoint : window.innerHeight-e.clientY-offsetypoint-20;
        var leftedge=(offsetxpoint<0)? offsetxpoint*(-1) : -1000;
        // -----------------------------------------------------------------------------------
        // If the horizontal distance isn't enough to accomodate the width of the context menu
        // -----------------------------------------------------------------------------------
        if (rightedge<tipobj.offsetWidth)
        {
            // ------------------------------------------------------------------
            // Move the horizontal position of the menu to the left by it's width
            // ------------------------------------------------------------------
            tipobj.style.left=ie? ietruebody().scrollLeft+event.clientX-tipobj.offsetWidth+"px" : window.pageXOffset+e.clientX-tipobj.offsetWidth+"px";
        }
        else if (curX<leftedge)
        {
            tipobj.style.left="5px";
        }
        else
        {
            // --------------------------------------------------------------------------
            // Position the horizontal position of the menu where the mouse is positioned
            // --------------------------------------------------------------------------
            tipobj.style.left=curX+offsetxpoint+"px";
            // ---------------------------------------
            // Same concept with the vertical position
            // ---------------------------------------
            if (bottomedge<tipobj.offsetHeight)
            {
                tipobj.style.top=ie? ietruebody().scrollTop+event.clientY-tipobj.offsetHeight-offsetypoint+"px" : window.pageYOffset+e.clientY-tipobj.offsetHeight-offsetypoint+"px";
            }
            else
            {
                tipobj.style.top=curY+offsetypoint+"px";
                tipobj.style.visibility="visible";
            }
        }
    }
}
// -----------------
// Hide the help tip
// -----------------
function hidehelptip()
{
    if (ns6||ie)
    {
        enabletip=false;
        tipobj.style.visibility="hidden";
        tipobj.style.left="-1000px";
        tipobj.style.backgroundColor='';
        tipobj.style.width='';
    }
}

