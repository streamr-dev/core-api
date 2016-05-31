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
                    ['tourStream1.text', 'tourTable1.in1'],
                    ['tourStream1.retweet_count', 'tourTable1.in2'],
                    ['tourStream1.favorite_count', 'tourTable1.in3'],
                    ['tourStream1.lang', 'tourTable1.in4']
                ])(cb)
            }
        )

        .step("Time to see the data in real-time!<br><br>Open up realtime mode by clicking <strong>Realtime</strong>.",
            '#open-realtime-tab-link',
            { placement: 'right' },
            function() {
                $("#open-realtime-tab-link").on("shown.bs.tab", function() {
                    tour.next()
                })
            }
        )

        .step("Start the canvas.",
            '#run-realtime-button',
            { placement: 'right' },
            function() {
                console.log("Binding")
                $(document).on("shown.bs.modal", function() {
                    console.log("shown")
                    tour.next()
                })
            }
        )

        .step("You must save a real-time canvas before running it.<br><br><strong>Save</strong> the canvas.",
            '.save-on-start-confirmation-dialog .modal-content',
            { nextOnTargetClick: true, placement: 'bottom' }
        )

        .step("You should see the data appearing now.",
            ".tourTable1"
        )

        .offerNextTour("Absolutely fantastic! In the next tour, we'll go even deeper.<br><br> Click Begin when you are ready!")

        .ready()

})()
