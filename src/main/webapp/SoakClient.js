function SoakClient()
{
       // Is there a test running?
       var testRunning = false;
       // Start time and end time of the test
       var startTime = null;
       var endTime = null;
       // 10 second interval between each client side poll of the test status on the server side
       var interval = 10;
       // Handle to timerID for polling mechanism to pro-actively see if a test has finished and
       // also used to keep the browser session alive.
       this.checkStatus = null;

        /**
         * Start the test on the server and create the interval poller if
         * the asynchronous call was successfull.
         */
        this.startTest = function() {
            if (!testRunning) {
                $("detailsForm").action.value = "S";
                $("results").innerHTML = "";
                var myself = this;
                sendRequest("sourceServlet",
                    "status",
                    null,
                    getFormValues($("detailsForm")),
                    function() {
                        startTime = new Date();
                        $("status").innerHTML = ["<p class=\"visibleErrorStatus\">",
                            "Client details: Soak test started on ",
                            startTime.toGMTString(),
                            ", either wait for it to finish itself, or click on STOP.",
                            "</p>",
                            ].join("");
                        showPleaseWait("working");
                        testRunning = true;
                        myself.checkStatus = setInterval(function() {
                            myself.checkTestRunning();
                            }, interval * 1000);
                    });
            } else {
                alert("Sorry, but there is already a test running, stop it first, and then try again ...");
            }
        }
        /**
         * End the test running on the server, and if the asynchronous call is successfull
         * clear the interval poller.
         */
        this.stopTest = function() {
            if (testRunning) {

                $("detailsForm").action.value = "E";
                sendRequest("sourceServlet",
                    "results",
                    null,
                    getFormValues($("detailsForm")),
                    function() {
                        endTime = new Date();
                        $("status").innerHTML = ["<p class=\"visibleErrorStatus\">",
                            "Client details: Soak test started on ",
                            startTime.toGMTString(),
                            " was stopped manually on ",
                            endTime.toGMTString(),
                            " with the following results:</p>"].join("");
                        $("working").innerHTML = "";
                        testRunning = false;
                        clearInterval(this.checkStatus);
                    });
            } else {
                alert("There is no test to stop, as no test is running");
            }
        }
        /**
         * Check if the test is still running and clear the interval poller if its not and we have
         * results of some sort.
         *
         * Note, this polling behaviour will also keep the session going on the server side.
         */
        this.checkTestRunning = function() {
            if (testRunning) {
                var myself = this;
                $("detailsForm").action.value = "C";
                sendRequest("sourceServlet",
                    "results",
                    null,
                    getFormValues($("detailsForm")),
                    function() {
                        if ($("results").innerHTML !== "") {
                            endTime = new Date();
                            $("status").innerHTML = ["<p class=\"visibleErrorStatus\">",
                                "Client details: Soak test started on ",
                                startTime.toGMTString(),
                                " was completed on ",
                                endTime.toGMTString(),
                                " with the following results:</p>"].join("");
                            $("working").innerHTML = "";
                            testRunning = false;
                            clearInterval(myself.checkStatus);
                        }
                    });
            }
        }
}