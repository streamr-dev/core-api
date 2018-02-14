(function() {

    var tour = Tour.create()

    tour

        .setGrailsPage('canvas', 'editor')

        .beforeStart(function(cb) {
            SignalPath.clear()
            cb()
        })

        .step("Hello there! In this third tutorial, we'll see how to work with real-time data and react to events." +
            "<br/><br/>Remember that you can restart the tours from the <code>?</code> menu, as well as find " +
            "tutorial videos and examples there.<br/><br/>Click Next when you are ready to begin!", '#navHelpLink',
            { placement: 'left', animated: true })

        .step("Let's build a system that reacts, in real-time, to Bitcoin-related tweets, alerting us if there is a sudden surge in the frequency of said tweets.<br><br>" +
            "Search for <b>Twitter-Bitcoin</b>, and add it to the canvas.",
            '#search',
            function() {
                function listener(e, jsonData, div) {
                    if (jsonData.name === 'Stream' && jsonData.params[0].streamName === 'Twitter-Bitcoin') {
                        tour.bindModule(jsonData.name, div)
                        $(SignalPath).off('moduleAdded', listener)
                        tour.next()
                    }
                }

                $(SignalPath).on('moduleAdded', listener)
            }
        )

        .step("First let's have a look at what kind of data we are working with.<br><br>Add a <strong>Table</strong> to the canvas.",
            '#search',
            function(cb) {
                tour.waitForModuleAdded('Table')(cb)
            }
        )

        .step("Connect <code>text</code>, <code>retweet_count</code>, <code>favorite_count</code>, and <code>lang</code> of Stream to Table (in that order.)",
            '.tourStream1',
            function(cb) {
                tour.waitForConnections([
                    ['tourStream1.text', 'tourTable1.text'],
                    ['tourStream1.retweet_count', 'tourTable1.retweet_count'],
                    ['tourStream1.favorite_count', 'tourTable1.favorite_count'],
                    ['tourStream1.lang', 'tourTable1.lang']
                ])(cb)
            }
        )

        .step("Time to see the data in real-time!<br><br>Open up realtime mode by clicking <strong>Realtime</strong>.",
            '#open-realtime-tab-link',
            { placement: 'right' },
            function() {
                $("#open-realtime-tab-link").one("shown.bs.tab", function() {
                    tour.next()
                })
            }
        )

        .step("Start the canvas.",
            '#run-realtime-button',
            { placement: 'right' },
            function() {
                console.log("Binding")
                $(document).one("shown.bs.modal", function() {
                    console.log("shown")
                    tour.next()
                })
            }
        )

        .step("You must save a real-time canvas before running it.<br><br><strong>Save</strong> the canvas.",
            '.save-on-start-confirmation-dialog .modal-content',
            { nextOnTargetClick: true, placement: 'bottom' }
        )

        .step("You should see the data appearing. Take a quick look to see how it is structured.",
            ".tourTable1"
        )

        .step("Alright, <b>Stop</b> the running canvas to continue the tour.",
            '#run-realtime-button',
            function() {
                console.log("Binding")

                // We must rebind modules because their .tourXXX classes were removed during saving...
                tour.bindModule('Stream', $("#module_0"))
                tour.bindModule('Table', $("#module_1"))

                $(document).one("shown.bs.modal", function() {
                    console.log("shown")
                    tour.next()
                })
            }
        )

        .step("Confirm that you want to stop the canvas.",
            '.stop-confirmation-dialog .modal-content',
            { placement: 'bottom' },
            tour.waitForCanvasStopped()
        )

        .step("Remove the <b>Table</b> module from the canvas.",
            '.tourTable2', // == .tourTable1 (canvas was reloaded, classes were lost hence modules had to be rebinded)
            { placement: 'left' },
            function(cb) {
                tour.waitForModuleRemoved('Table')(cb)
            }
        )

        .step("Next, let's build some more advanced logic. We will build a system that alerts every time the frequency of Bitcoin mentions in tweets explodes.<br><br>Start by adding module <strong>Count</strong> to the canvas.",
            '#search',
            function(cb) {
                tour.waitForModuleAdded('Count')(cb)
            }
        )

        .step("Let's count the tweets in a 1 minute window.<br/><br/>Set parameter <code>windowLength</code> of Count to <strong>1</strong>",
            '.tourCount1',
            tour.waitForInput(".tourCount1 .parameterInput:eq(0)", "1")
        )

        .step("Set parameter <code>windowType</code> to <strong>minutes</strong>",
            '.tourCount1',
            tour.waitForInput(".tourCount1 .parameterInput:eq(1)", "MINUTES")
        )

        .step("Connect <code>text</code> of Stream to <code>in</code> of Count.",
            '.tourStream2', // same as tourStream1
            tour.waitForConnection(['tourStream2.text', 'tourCount1.in'])
        )

        .step("Add module <strong>GreaterThan</strong> to the canvas.",
            '#search',
            function(cb) {
                tour.waitForModuleAdded('GreaterThan')(cb)
            }
        )

        .step("Connect <code>count</code> of Count to <code>A</code> of GreaterThan.",
            '.tourCount1',
            tour.waitForConnection(['tourCount1.count', 'tourGreaterThan1.A'])
        )

        .step("Add module <strong>Constant</strong> to the canvas.",
            '#search',
            function(cb) {
                tour.waitForModuleAdded('Constant')(cb)
            }
        )

        .step("Set parameter <code>constant</code> of Constant to <strong>350</strong>",
            '.tourConstant1',
            tour.waitForInput(".tourConstant1 .parameterInput", "350")
        )

        .step("Connect <code>out</code> of Constant to <code>B</code> of GreaterThan.",
            '.tourConstant1',
            tour.waitForConnection(['tourConstant1.out', 'tourGreaterThan1.B'])
        )

        .step("Now, we only want alerts when the output of GreaterThan is true.<br/><br/>Add module <strong>Filter</strong> to the canvas.",
            '#search',
            function(cb) {
                tour.waitForModuleAdded('Filter')(cb)
            }
        )

        .step("Connect <code>A>B</code> of GreaterThan to <code>pass</code> of Filter.",
            '.tourGreaterThan1',
            tour.waitForConnection(['tourGreaterThan1.A>B', 'tourFilter1.pass'])
        )

        .step("Also connect <code>A>B</code> of GreaterThan to <code>in1</code> of Filter.",
            '.tourGreaterThan1',
            tour.waitForConnection(['tourGreaterThan1.A>B', 'tourFilter1.A&gt;B'])
        )

        .step("Add module <strong>Email</strong> to the canvas.",
            '#search',
            function(cb) {
                tour.waitForModuleAdded('Email')(cb)
            }
        )

        .step("Set parameter <code>subject</code> of Email to <strong>alert</strong>",
            '.tourEmail1',
            tour.waitForInput(".tourEmail1 .parameterInput:first-child", "alert")
        )

        .step("Connect output <code>A>B</code> of Filter to <code>value1</code> of Email.",
            '.tourFilter1',
            tour.waitForConnection(['tourFilter1.A&gt;B', 'tourEmail1.value1'])
        )

        .step("One last thing. Hover over the module GreaterThan. Then click the red NR (No Repeat) button just right of <code>A&gt;B</code> so it becomes green.",
            '.tourGreaterThan1',
            function(cb) {
                $(".tourGreaterThan1 .ioSwitch.noRepeat.ioSwitchFalse").click(function() {
                    tour.next()
                })
            }
        )

        .step("This makes sure the EmailModule does not activate multiple times in a row", ".tourGreaterThan1")

        .step("We have now set-up a system that sends us an email every time the number of Bitcoin related tweets within a minute exceeds a threshold.")

        .step("Just to make sure that everything is working correctly let's add a Table.<br><br>Add a <strong>Table</strong> to the canvas.",
            "#search",
            function(cb) {
                tour.waitForModuleAdded('Table')(cb)
            }
        )

        .step("Connect <code>count</code> of Count to <code>in1</code> of Table.",
            '.tourCount1',
            tour.waitForConnection(['tourCount1.count', 'tourTable4.count'])
        )

        .step("Connect <code>A>B</code> of GreaterThan to <code>in2</code> of Table.",
            '.tourGreaterThan1',
            tour.waitForConnection(['tourGreaterThan1.A>B', 'tourTable4.A&gt;B'])
        )

        .step("Start the canvas.",
            '#run-realtime-button',
            { placement: 'right' },
            function() {
                console.log("Binding")
                $(document).one("shown.bs.modal", function() {
                    console.log("shown")
                    tour.next()
                })
            }
        )

        .step("You must save a real-time canvas before running it.<br><br><strong>Save</strong> the canvas.",
            '.save-on-start-confirmation-dialog .modal-content',
            { nextOnTargetClick: true, placement: 'bottom' }
        )

        .step("You will now see some debug data on the Table. The current tweet count is shown along with whether the " +
            "threshold to send an email has been reached. If the threshold is reached you will receive an email indicating " +
            "this.<br><br>Press Next to continue.",
            '.tourTable4'
        )

        .step("<b>Stop</b> the running canvas when you feel you are ready to continue.",
            '#run-realtime-button',
            function() {
                $(document).one("shown.bs.modal", function() {
                    console.log("shown")
                    tour.next()
                })
            }
        )

        .step("Confirm that you want to stop the canvas.",
            '.stop-confirmation-dialog .modal-content',
            { placement: 'bottom' },
            tour.waitForCanvasStopped()
        )

        .step("That's it for now, stay tuned for new tours. Have fun using Streamr!")

        .ready()

})()
