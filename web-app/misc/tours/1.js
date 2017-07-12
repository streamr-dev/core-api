(function() {

    var tour = Tour.create()

    tour

        .setGrailsPage('canvas', 'editor')

        .beforeStart(function(cb) {
            SignalPath.clear()
            $('#beginDate').val('2017-04-09')
            $('#endDate').val('2017-04-10')
            cb()
        })

        .step("Hello again! In this (second) tutorial, we'll see how to build logic around incoming data by connecting various " +
            "modules together. <br/><br/>Remember that you can restart the tours from the <code>?</code> menu, as well as find " +
            "tutorial videos and examples there. "+
            "<br/><br/>Click Next when you are ready to begin!", '#navHelpLink',
            { placement: 'left', animated: true })

        .step("Let's start by adding the Public transport demo data stream (as in the 1st tutorial). <br/><br/>"+
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

        .step("For the duration of this tutorial, we'll be focusing on a single, specific tram. To achieve this goal, " +
            "we'll need to filter the stream's incoming data appropriately.",
            '.tourStream1'
        )

        .step(" Open the <b>Utils</b> section by clicking it to list utility-related modules.",
            '#moduleTree',
            function() {
                $('.jstree a:contains(Utils)').parent().one('click', tour.next)
            }
        )

        .step('Under <b>Utils</b>, find the <code>Filter</code> module and drag and drop it to the canvas.',
            '#moduleTree',
            function(cb) {
                tour.waitForModuleAdded('Filter')(cb)
            }
        )

        .step("Next, let's add the <code>TextEquals</code> module. This time we will use the search box for this.<br><br>" +
            "Go ahead and type <b>Text</b> into the search and choose the <code>TextEquals</code> module from the " +
            "results.",
            '#search',
            function(cb) {
                tour.waitForModuleAdded('TextEquals')(cb)
            }
        )

        .step("Feel free to move the module into a more spacious area.", ".tourTextEquals1")

        .step("Search is a faster and more convenient way of adding modules that are already familiar to you by name.", "#search")

        .step("Our goal is to filter data related to a single tram (specifically, vehicle RHKL00112) from all tram " +
            "data being pushed out by <b>Stream</b>. To do so, we will need to pass forward only those events whose " +
            "<b>veh</b> equals <b>RHKL00112</b>.",
            '.tourStream1'
        )

        .step("Let's make it happen!<br/><br/>"+
            "Start dragging from the highlighted circle near the output <code>veh</code> on the Stream, and drop on " +
            "the second input (called <code>text</code>) on the TextEquals module.",
            '.tourStream1',
            { placement: 'bottom' },
            tour.highlightOutputUntilDraggingStarts("tourStream1.veh")
        )

        .step("Drop the connection on the second input of the TextEquals, called <code>text</code>.",
            '.tourTextEquals1',
            function(cb) {
                tour.highlightInputUntilConnected("tourTextEquals1.text")(function() {
                    tour.waitForConnection(['tourStream1.veh', 'tourTextEquals1.text'])(cb)
                })
            }
        )

        .step("Next connect <code>equals?</code> of TextEquals to <code>pass</code> of Filter.",
            '.tourTextEquals1',
            { placement: 'bottom' },
            tour.highlightOutputUntilDraggingStarts("tourTextEquals1.equals?")
        )

        .step("Drop the connection on the first input of the Filter, called <code>pass</code>.",
            '.tourFilter1',
            { placement: 'top' },
            function(cb) {
                tour.highlightInputUntilConnected("tourFilter1.pass")(function() {
                    tour.waitForConnection(['tourTextEquals1.equals?', 'tourFilter1.pass'])(cb)
                })
            }
        )

        .step("Now let's define the vehicle we are filtering.<br><br>Type <b>RHKL00112</b> into parameter <code>search</code> of module Filter.",
            '.tourTextEquals1',
            { placement: 'left' },
            tour.waitForInput(".tourTextEquals1 .parameterInput", "RHKL00112")
        )

        .step("Then connect <code>lat</code> of Stream to <code>in1</code> of Filter.",
            '.tourStream1',
            tour.waitForConnection(['tourStream1.lat', 'tourFilter1.lat'])
        )

        .step("Notice how a new input (<code>in2</code>) appeared on Filter.", '.tourFilter1')

        .step("Connect <code>long</code> of Stream to <code>in2</code> of Filter.",
            '.tourStream1',
            tour.waitForConnection(['tourStream1.long', 'tourFilter1.long'])
        )

        .step("Then connect <code>spd</code> of Stream to <code>in3</code> of Filter.",
            '.tourStream1',
            tour.waitForConnection(['tourStream1.spd', 'tourFilter1.spd'])
        )

        .step("Let's add a <code>Table</code> to confirm that data is indeed flowing in and to see how the data looks " +
            "like.<br><br>Search for <b>Table</b> and add it to the canvas.",
            '#search',
            function(cb) {
                tour.waitForModuleAdded('Table')(cb)
            }
        )

        .step("Feel free to move the Table into a more spacious area.", ".tourTable1")

        .step("Connect outputs <code>lat</code>, <code>long</code>, and <code>spd</code> of Filter to Table (in that order.)",
            '.tourFilter1',
            function(cb) {
                tour.waitForConnections([
                    ['tourFilter1.lat', 'tourTable1.lat'],
                    ['tourFilter1.long', 'tourTable1.long'],
                    ['tourFilter1.spd', 'tourTable1.spd']
                ])(cb)
            }
        )

        .step("Everything should now be properly connected for the filtering to work.<br><br>" +
            "<b>Run</b> this canvas (in historical mode) to verify that this is the case.",
            '#run-historical-button',
            { nextOnTargetClick: true }
        )

        .step("If you see data appearing on the table, our filtering is working as intended! Well done!",
            '.tourTable1',
            { placement: 'left' }
        )

        .step("<b>Abort</b> the running canvas to continue.",
            '#run-historical-button',
            tour.waitForCanvasStopped()
        )

        .step("Let's proceed to build something a bit more visual and interesting.<br><br>First off, remove the <b>Table</b> module " +
            "from the canvas by hovering on top of the module and pressing the close button (visual x) in the top-right corner.",
            '.tourTable1',
            { placement: 'left' },
            function(cb) {
                tour.waitForModuleRemoved('Table')(cb)
            }
        )

        .step("Add the <code>Chart</code> module and feel free to position it as you please.",
            '#search',
            function(cb) {
                tour.waitForModuleAdded('Chart')(cb)
            }
        )

        .step("Connect output <code>spd</code> of <b>Filter</b> to the first input of <b>Chart</b>.",
            '.tourFilter1',
            function(cb) {
                tour.waitForConnection(['tourFilter1.spd', 'tourChart1.in1'])(cb)
            }
        )

        .step('The Chart module is used to plot quantitative values by time.',
            '.tourChart1',
            { placement: 'left' })

        .step("Currently we have routed <code>spd</code> of Stream to input <code>spd</code> of Filter, which is passed along " +
            "to the Chart via output <code>spd</code> of Filter.", '.tourFilter1')

        .step("<b>Run</b> the canvas.",
            '#run-historical-button',
            { nextOnTargetClick: true }
        )

        .step("The speed of tram RHKL00112 is visualized on the chart by time.",
            '.tourChart1',
            { placement: 'left' }
        )

        .step("When you are ready to continue, press <b>Abort</b>.",
            '#run-historical-button',
            tour.waitForCanvasStopped()
        )

        .offerNextTour("Extraordinary! In the next tour, we'll go even deeper.<br><br> Click Begin when you are ready!")

        .ready()

})()
