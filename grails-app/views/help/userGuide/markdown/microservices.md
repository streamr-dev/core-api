# Canvases

A Streamr canvas is a microservice which consumes and acts upon real-time data.  A canvas contains one or more [streams](#streams) (these provide the data) and one or more [modules](#modules) (these do the processing). Streams and modules are connected in the configuration that you'll design.  The connections determine how the data flows through the canvas.

There's a wide variety of built-in modules in Streamr.  Some of those perform basic arithmetic and logical operations, filtering, sampling, aggregation, and so on. Others transform the data in some fashion and feed it to the next stage.  Yet other modules interact with the outside world and with external systems.

Computation in Streamr is entirely event-based. Any module will execute immediately when activated by incoming events. When new events arrive in the input stream, the data automatically flows through the canvas. This inherently asynchronous process allows for fast and continuous in-memory processing of large volumes of real-time data.

As a simple example, here's a canvas consisting of one stream and a chart module connected together.  When you run the microservice, the events flow from the stream to the chart, and the chart draws the data points as they arrive.

<r:img plugin="unifina-core" dir="images/user-guide" file="my-first-stream-on-canvas.png" class="img-responsive center-block" />

You can run a canvas in either historical or real-time mode.

- In the *historical mode*, running a canvas is a playback of what would have happened in the past. A playback can be extremely useful when you’re testing, refining, or demonstrating the functionality of a canvas.
- The *real-time mode* is used in production where you want to react to events as they arrive. There's no need to modify the canvas in order to run it live.  One click is all it takes to activate the microservice and start consuming real-time data.

In this chapter, we’ll show how to do the following: 

- Use the canvas editor.
- Subscribe to streams.
- Build canvases.
- Run a historical playback.
- Start or stop a live canvas.
- Reuse canvases as modules.

## Using the editor

You create a new service or modify an existing service by using the Streamr editor. When you log in to Streamr, the editor with a blank workspace is what you’ll first see. The editor is always accessible by clicking on the **Editor** tab.

<r:img plugin="unifina-core" dir="images/user-guide" file="blank-canvas-with-arrow.png" class="img-responsive center-block" />

<r:img plugin="unifina-core" dir="images/user-guide" file="hide-control-bar-button.png" align="right"  hspace="0" vspace="0" />

As a space-saving hint, note the small icon in the top left corner, just left of the Streamr log.  Click on the icon to hide the editor sidebar.  Click again, and the sidebar reappears. 

There are three things you can do in the editor:

- If you want to create a new canvas, click on the left-most icon in the top row of the control sidebar. This is where you are taken by default.

<r:img plugin="unifina-core" dir="images/user-guide" file="new-service-with-arrow.png" class="img-responsive"/>

- If you want to view or modify an existing canvas, click on the icon in the middle.

    <r:img plugin="unifina-core" dir="images/user-guide" file="open-service-with-arrow.png" class="img-responsive" />

- If you want to save the canvas, click on the icon on the right.

    <r:img plugin="unifina-core" dir="images/user-guide" file="save-service-with-arrow.png" class="img-responsive" />

The editor is your workspace for building or modifying a canvas and the event processing logic. You can test the canvas with a playback of historical data and launch it live when you're ready to go.

There's a natural iterative workflow where you first build a perhaps rudimentary version of a canvas, test it with historical data where possible, refine the design based on the test findings, and repeat until you're happy.

You can also create a service programmatically by using the <g:link controller="help" action="api">canvas API</g:link>. 

## Building a canvas

<r:img plugin="unifina-core" dir="images/user-guide" file="add-twitter-stream.png" class="side-image"/>

When you want to build a canvas, you’ll typically start by adding one or more data streams to the editor workspace.  You’ll then create the processing logic by adding modules to the canvas and connecting the streams and modules together. You can do all this interactively by dragging and dropping streams and modules from the sidebar to the workspace and by drawing connections between them.

When you place a stream on the canvas, you effectively subscribe to a real-time data source. To find a stream, just start typing its name in a text box labeled **Add Stream / Module** (see the editor sidebar).  We’ll autocomplete the stream name as you type. Either click on the highlighted name or press <kbd>Enter</kbd> to select the  stream. Real-time events are now available at the output endpoints.

<r:img plugin="unifina-core" dir="images/user-guide" file="module-browser.png" class="side-image"/>

There are [modules](#streams) for streaming analytics, visualisation, communication, and many other purposes. You'll find all the built-in components in the **Module Browser** which is organised by category.

If you already know the name of the module you need, type its name (with autocomplete) in the search box (**Add Stream / Module**).  If there’s several partial matches, you can select the one you want from the popup window. As a shortcut, you can press <kbd>Enter</kbd> to select the first match. You can also drill down in the module browser to the module you want. Then either highlight the module with a mouse click and then click on the **Add Module** button. Or just drag a module from the browser to the canvas.

You can move modules around on the workspace as you wish, but the placement of a module has no impact on functionality.  For clarity, you may want to design the canvas so that module placement reflects the data flow from the input streams through the modules.

A data flow between two modules — or a data flow between a stream and a module — is created by drawing a connection from an outgoing endpoint to an incoming endpoint with the mouse (or other pointing device).  You can create as many outgoing connections as you wish. You can only have one incoming connection per an endpoint.

<r:img plugin="unifina-core" dir="images/user-guide" file="connecting-stream-to-module.png" class="img-responsive" />

You can only connect endpoints with compatible data types. For instance, you cannot create a connection which feeds string events to an endpoint where numerical events are expected.

However, any non-boolean events are automatically converted to logical truth values if a boolean input is required. Numbers exactly equal to zero are deemed to be False, and any non-zero values True. An empty string ("") is False, a non-empty string is True. An empty list is False, a non-empty list is True. An empty Map is False, and anything else is True.

All connections are unidirectional, i.e. the data always flows from an output to one or more inputs in one direction only.  The modules form a directed graph. [Feedback loops](#feedbackloops) are discouraged, but you can create them if you really want.

When you view a canvas, the direction of a data connection is indicated by an arrow.  You can alter the endpoint of an existing connection by dragging it to another input endpoint.  If you instead drop the endpoint in an empty area in the workspace, the connection is cleared.  A mouse click on top of a module brings up an pop-up menu where you can choose to disconnect all incoming connections to the module.

The topology of a service can be arbitrarily complex. You can of course design a simple sequential work flow, and in many cases it will be perfectly adequate. In other cases the flow of data may involve merging data pathways, branches and even loops. Go ahead, be adventurous, but also bear in mind Streamr's abstraction capabilities. Reusing existing canvases will help you manage the development process and keep things tidy and neat.

## Running a playback

<r:img plugin="unifina-core" dir="images/user-guide" file="start-historical-run.png" class="side-image"/>

The historical playback facility is a great way to test a canvas.  In a playback, a canvas consumes historical events stored in the subscribed streams. A playback is a simulation of what would have happened in the past.

By default, any canvas is in a historical mode. Provided that the streams used by the canvas contain some historical data, you can run a playback at any time that the canvas is open in the editor.

You specify the time period (the start date and the end date) for a historical playback period in the editor sidebar.  The playback starts when you press the **Run** button.

By default, playback events are processed sequentially but at a much faster pace compared to the actual history. You can easily change the playback speed for the historical run. Click on the Options icon, and a pop-up menu shows the available choices.

<r:img plugin="unifina-core" dir="images/user-guide" file="playback-options.png" class="img-responsive" />

## Running live services

<r:img plugin="unifina-core" dir="images/user-guide" file="start-realtime-run.png" class="side-image"/>

When a canvas is live, it will listen to real-time events arriving in the subscribed streams, and process them as soon as they're available. You can think of live canvas as digital agents who'll react to new events in real-time on your behalf.

A canvas does not need to be modified in any way when you want to take it live. It will work as is, in the same exact form as used in historical testing. Simply switch to the realtime tab, press the **Start** button, and voilà!

<r:img plugin="unifina-core" dir="images/user-guide" file="stop-realtime-run.png" class="side-image"/>

A live canvas keeps running until you explictly tell it to stop. When you stop a canvas, its internal state is saved on the disk. If you later restart the microservice, it will gracefully resume from the point where it stopped. It will not, however, process any events that occurred when it was not running.

You'll see all of your canvases (and their state) in the Canvases tab. Click on a canvas to open it in the editor. You can then stop it (if it is live) or launch it live (if it is stopped).

## Reusing canvases as modules

You can easily reuse a canvas as a component of another canvas. This is done via *abstraction*, where you encapsulate a canvas as a module. You can then use the new module when you build additional microservices.

To create an abstraction, you'll need to expose — or export — inputs and outputs. The exported endpoints will show up as endpoints of the abstracted canvas.


