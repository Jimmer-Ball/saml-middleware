// Cache the loader image 
var m_LoaderImage = new Image();
m_LoaderImage.src = "/SAMLWeb/images/icons/loading.gif";

// isIE indicator 
var isIE = navigator.userAgent.indexOf("MSIE") != -1;

function init()
{
    if (!isIE)
    {
        window.onbeforeunload = function()
                                {
                                    return "Otherwise please use the Menu to select the page required";
                                };
    }
}

// Returns a string representing the form values suitable for submitting as arguments to the
// next js/jsp/servlet in the chain
function getFormValues(a_Form)
{
   var str = "";
   for(var i = 0;i < a_Form.elements.length;i++)
   {
        if (!a_Form.elements[i].disabled && a_Form.elements[i].value) 
        {
            switch(a_Form.elements[i].type)
            {
                case "text":
                case "hidden":
                {
                    str += a_Form.elements[i].name + "=" + encodeURIComponent(a_Form.elements[i].value) + "&";
                    break;
                }
                case "select-one":
                {
                    str += a_Form.elements[i].name + "=" + encodeURIComponent(a_Form.elements[i].options[a_Form.elements[i].selectedIndex].value) + "&";
                    break;
                }        
                case "radio":
                case "checkbox":
                {
                    if (a_Form.elements[i].checked)
                    {
                        str += a_Form.elements[i].name + "=" + a_Form.elements[i].value + "&";
                    }
                    break; 
                }
            }
        }
   }
   return str.substr(0,(str.length - 1));
}
// -------------------------------------------------
// Helper method to get hold of a DOM element handle 
// -------------------------------------------------
function $(id) 
{
    return document.getElementById(id); 
}
// -------------------------------------------
// Show the wait animation with relying on JSP
// -------------------------------------------
function showPleaseWait(a_Container)
{
    document.getElementById(a_Container).innerHTML = '<div style="margin-left:auto; margin-right:auto; color:grey; font-size:14pt;"><img src="/SAMLWeb/images/icons/loading.gif" style="vertical-align: middle"/> Please Wait</div>';
}
// --------------------
// Is the input numeric 
// --------------------
function isNumeric(a_Element)
{
    var numericExpression = /^[0-9]+$/;
    return validate(numericExpression, a_Element);
}
// -----------------------------
// Base level validation routine 
// using regular expressions.
// -----------------------------
function validate(a_Expression, a_Element)
{
    return a_Element.value.match(a_Expression) == a_Element.value;
}

