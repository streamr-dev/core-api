#Services

A Streamr service is a process which consumes and acts upon real-time data.  A service contains one or more [modules](#modules) (these do the processing) and one or more [streams](#streams) (these provide the data).  Streams and modules are connected in the configuration you'll design.  Those connections determine how the data flows through the service.

There's a wide variety of built-in modules in Streamr.  Some of those perform basic arithmetic and logical operations, filtering, sampling, aggregation, and so on. Others transform the data in some fashion and feed it to the next stage.  Yet other modules interact with the outside world and with external systems.

Computation in a Streamr service is entirely event-based. Any module will execute immediately when activated by incoming events. When new events arrive in the input stream, the data automatically flows through the service. This inherently asynchronous process allows for fast and continuous in-memory processing of large volumes of real-time data.

As a simple example, here's a service consisting of one stream and a chart module connected together.  When you run the service, the events flow from the stream to the chart, and the chart draws the data points as they arrive.

<g:img dir="images/user-guide" file="my-first-stream-on-canvas.png" class="img-responsive center-block" />

You can run a service in either historical or real-time mode.

- In the *historical mode*, running a service is a playback of what would have happened in the past. A playback can be extremely useful when you’re testing, refining, or demonstrating the functionality of a service.
- The *real-time mode* is used in production where you want to react to events as they arrive. There's no need to modify the service in order to run it live.  One click is all it takes to activate a service and start consuming real-time data.

In this chapter, we’ll show how to do the following: 

- Create and edit services.
- Subscribe to streams.
- Build services.
- Run a historical playback.
- Start or stop a live service.
- Reuse services as modules.

##Using the editor

You create a new service or modify an existing service by using the Streamr editor. When you log in to Streamr, the editor with a blank canvas is what you’ll first see. The editor is always accessible by clicking on the **Editor** tab.

<g:img dir="images/user-guide" file="blank-canvas-with-arrow.png" class="img-responsive center-block" />

<g:img dir="images/user-guide" file="hide-control-bar-button.png" align="right"  hspace="0" vspace="0" />

As a space-saving hint, note the small icon in the top left corner, just left of the Streamr log.  Click on the icon to hide the editor sidebar.  Click again, and the sidebar reappears. 

There are three things you can do in the editor:

- If you want to create a new service, click on the left-most icon in the top row of the control sidebar. This is where you'll be taken by default.

   <g:img dir="images/user-guide" file="new-service-with-arrow.png" class="img-responsive"/>

- If you want to view or modify an existing service, click on the icon in the middle.

    <g:img dir="images/user-guide" file="open-service-with-arrow.png" class="img-responsive" />

- If you want to save the service, click on the icon on the right.

    <g:img dir="images/user-guide" file="save-service-with-arrow.png" class="img-responsive" />

The editor's canvas is your workspace for building a service and the event processing logic. You can test the service with a playback of historical data and launch it live when you're ready to go. There's a natural iterative workflow, where you build a perhaps rudimentary version of a service, test it with historical data where possible, refine the design based on the test findings, and repeat until you're happy.

You'll find that the editor works well with a build-test-refine cycle. However, you can also create a service programmatically by using the <g:link controller="help" action="api">canvas API</g:link>. 

##Building a service

<g:img dir="images/user-guide" file="add-twitter-stream.png" class="side-image"/>

When you want to build a service, you’ll typically start by adding one or more data streams on the canvas.  You’ll then create the processing logic by adding modules to a service and connecting the streams and modules together. You can do all this interactively by dragging and dropping streams and modules from the sidebar to the canvas and by drawing connections between them.

When you place a stream on the canvas, you effectively subscribe to a real-time data source. To find a stream, just start typing its name in a text box labeled **Add Stream / Module** (see the editor sidebar).  We’ll autocomplete the stream name as you type. Either click on the highlighted name or press <kbd>Enter</kbd> to select the  stream. Real-time events are now available at the output endpoints.

<g:img dir="images/user-guide" file="module-browser.png" class="side-image"/>

There are [modules](#streams) for streaming analytics, visualisation, communication, and many other purposes. You'll find all the built-in components in the **Module Browser** which is organised by category.

If you already know the name of the module you need, type its name (with autocomplete) in the search box (**Add Stream / Module**).  If there’s several partial matches, you can select the one you want from the popup window. As a shortcut, you can press <kbd>Enter</kbd> to select the first match. You can also drill down in the module browser to the module you want. Then either highlight the module with a mouse click and then click on the **Add Module** button. Or just drag a module from the browser to the canvas.

You can move modules around on the canvas as you wish, but the placement of a module has no impact on functionality.  For clarity, you may want to design the canvas so that module placement reflects the data flow from the input streams through the modules.

A data flow between two modules — or a data flow between a stream and a module — is created by drawing a connection from an outgoing endpoint to an incoming endpoint with the mouse (or other pointing device).  You can create as many outgoing connections as you wish. You can only have one incoming connection per an endpoint.

<g:img dir="images/user-guide" file="connecting-stream-to-module.png" class="img-responsive" />

All connections are unidirectional, i.e. the data always flows from an output to one or more inputs in one direction only.  The modules form a directed graph. [Feedback loops](#loops) are discouraged, but you can create them if you really want.

**TODO: FIX THE LINK TO FEEDBACK LOOPS IN MODULES SECTION.**

When you view a service on a canvas, the direction of a data connection is indicated by an arrow.  You can alter the endpoint of an existing connection by dragging it to another input endpoint.  If you instead drop the endpoint in an empty space, the connection is cleared.  A mouse click on top of a module brings up an pop-up menu where you can choose to disconnect all incoming connections to the module.

The topology of a service can be arbitrarily complex. You can of course design a simple sequential work flow, and in many cases it will be perfectly adequate. In other cases the flow of data may involve merging data pathways, branches and even loops. Go ahead, be adventurous, but also bear in mind Streamr's abstraction capabilities. Reusing existing canvases will help you manage the development process and keep things tidy and neat.

##Running a playback

<g:img dir="images/user-guide" file="start-historical-run.png" class="side-image"/>

The historical playback facility is a great way to test a service.  In a playback, a service is applied to historical events stored in the subscribed streams. A playback is a simulation of what would have happened in the past.

By default, a service is in a historical mode. Provided that the streams used by the service contain some historical data, you can run a playback at any time that the service is open in the editor.

You specify the time period (the start date and the end date) for a historical playback period in the editor sidebar.  The playback starts when you press the **Run** button.

By default, playback events are processed sequentially but at a much faster pace compared to the actual history. You can easily change the playback speed for the historical run. Click on the Options icon, and a pop-up menu shows the available choices.

<g:img dir="images/user-guide" file="playback-options.png" class="img-responsive" />

You can use the dropdown menu to save all Chart inputs in a CSV file during a historical run.  If there’s more than one Chart in the service, each one will produce a separate output file.

##Running live services

<g:img dir="images/user-guide" file="start-realtime-run.png" class="side-image"/>

When a service is live, it will listen to real-time events arriving in the subscribed streams, and process them as soon as they're available. You can think of live services as digital agents who'll react to new events in real-time on your behalf.

A service does not need to be modified in any way when you want to take it live. It will work as is, in the same exact form as used in historical testing. Simply switch to the realtime tab, press the **Start** button, and voilà!

**TODO: ADD AN IMAGE OF THE SAMPLE SERVICE GOING LIVE.**

**TODO: NEED A NICE SIMPLE EXAMPLE TO USE THROUGHOUT THIS CHAPTER.**

<g:img dir="images/user-guide" file="stop-realtime-run.png" class="side-image"/>

A live service keeps running until you explictly tell it to stop. When you stop a service, its internal state is saved on the disk. If you later restart the service, it will gracefully resume from the point where it stopped. It will not, however, process any events that occurred when it was not running.

You'll see all of your services (and their state) in the Canvases tab. Click on a service to open it in the editor. You can then stop it (if it is live) or launch it live (if it is stopped).

##Reusing services as modules

You can easily reuse a service as a component of another service. This is done via *abstraction*, where you encapsulate a service as a module. You can then use the new module when you build additional services.

To create an abstraction, you'll need to expose -- or export -- inputs and outputs. The exported endpoints will show up as endpoints of the abstracted service.

**TODO: EXTEND AND SHOW AN EXAMPLE.**

