(function() {

    var tour = Tour.create()

    tour

        .setGrailsPage('canvas', 'editor')

        .beforeStart(function(cb) {
            SignalPath.clear()
            $('#beginDate').val('2016-04-11')
            $('#endDate').val('2016-04-12')
            cb()
        })

        .step("Hello and welcome! Let's take a quick tour of <b>Streamr</b>. "+
            "To skip the tour, just close this bubble.<br/><br/>You can restart the tours from the <code>?</code> menu, as well as find tutorial videos and examples there. "+
            "<br/><br/>Click Next when you are ready to begin!", '#navHelpLink',
            { placement: 'left', animated: true })

        .step("What would you like to build? A real-time public transport monitoring system, perhaps?<br/><br/>Let's do it now.<br/><br/>" +
            "In Streamr, logic is built visually by combining <b>modules</b> on the <b>canvas</b>. ")

        .step('This search box is the quickest way to add <b>modules</b> to the canvas.<br/><br/>'+
            'Search for <b>Public transport demo</b>, and add it to the canvas by selecting it in the results. Do this now.',
            '#search',
            function() {
                function listener(e, jsonData, div) {
                    if (jsonData.name === 'Stream' && jsonData.params[0].streamName === 'Public transport demo') {
                        tour.bindModule(jsonData.name, div)
                        $(SignalPath).off('moduleAdded', listener)
                        tour.next()
                    }
                }

                $(SignalPath).on('moduleAdded', listener)
            }
        )

        .step("Stream modules bring data into the canvas. Events in this stream contain updates on location, speed etc. of trams running in Helsinki. These properties appear as values from these outputs.<br/><br/>Outputs can be connected to other modules' inputs by dragging from the round endpoints.<br/><br/>Hit <b>next</b>.",
            '.tourStream1'
        )

        .step("Let's add another module so that we can get connecting!<br/><br/>This is the module browser.<br/><br/> Open the <b>Visualizations</b> section by clicking it to list the modules in that category.",
            '#moduleTree',
            function() {
                $('.jstree a:contains(Visualizations)').parent().one('click', tour.next)
            }
        )

        .step('Under <b>Visualizations</b>, find the <code>Map</code> module and drag and drop it to the canvas.',
            '#moduleTree',
            tour.waitForModuleAdded('Map')
        )

        .step("Let's make a connection!<br/><br/>"+
            "Start dragging from the highlighted circle near the output <code>veh</code> on the Stream, and drop on the first input (called <code>id</code>) on the Map.",
            '.tourStream1',
            { placement: 'bottom' },
            tour.highlightOutputUntilDraggingStarts("tourStream1.veh")
        )

        .step("Drop the connection on the first input of the Map, called <code>id</code>.",
            '.tourMap1',
            { placement: 'top' },
            function(cb) {
                tour.highlightInputUntilConnected("tourMap1.id")(function() {
                    tour.waitForConnection(['tourStream1.veh', 'tourMap1.id'])(cb)
                })
            }
        )

        .step("Nice! Your first connection!<br/><br/>"+
            "Let's do two more. This tour will advance when you're done. <br/><br/>" +
            "Connect <ul><li><code>lat</code> to <code>latitude</code></li><li><code>long</code> to <code>longitude</code>.</li></ul>",
            '.tourStream1',
            { placement: 'bottom' },
            function(cb) {
                tour.waitForConnections([['tourStream1.lat', 'tourMap1.latitude'], ['tourStream1.long', 'tourMap1.longitude']])(cb)
            }
        )

        .step("Pretty easy, right?")

        .step("Canvases can run in <b>Historical</b> or <b>Realtime</b> mode.",
            '.run-mode-tabs'
        )

        .step("Let's run this canvas in historical mode. The events from a day I selected will be replayed, "+
            "and our Stream will power the Map with tram locations on that day.<br><br>" +
            "Do it now by pressing <b>Run</b>.",
            '#run-historical-button',
            { nextOnTargetClick: true }
        )

        .step("Doesn't it look beautiful? <br/><br/>When you are ready to move to the next tour, press Abort",
            '#run-historical-button',
            tour.waitForCanvasStopped()
        )
        
        .offerNextTour("Great job! In the next tour, we'll develop this a bit further.<br><br> Click Begin when you are ready!")

        .ready()

})()
