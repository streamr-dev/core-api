#Services

A Streamr service is a process which consumes and acts upon real-time data.  A service contains one or more modules (these do the processing) and one or more [streams](#streams) (these provide the data).  Streams and modules are connected in the configuration you'll design.  Those connections define how the data flows through the service.

There's a wide variety of built-in modules in Streamr.  Some of those perform basic arithmetic and logical operations, filtering, sampling, aggregation, and so on. Others transform the data in some fashion and feed it to the next stage.  Yet other modules interact with the outside world and with external systems.

Computation in a Streamr service is entirely event-based. Any module will execute immediately when activated by incoming events. When new events arrive in the input stream, the data automatically flows through the service. This inherently asynchronous process allows for fast and continuous in-memory processing of large volumes of data.

**Add a paragraph to make the case that the service topology is more than a sequential workflow?**

As a simple example, here's a service consisting of one stream and a chart module connected together.  When you run the service, the events flow from the stream to the chart, and the chart draws the data points as they arrive.

<g:img dir="images/user-guide" file="my-first-stream-on-canvas.png" class="img-responsive center-block" />

You can run a service with either historical or real-time data.

- In the *historical mode*, running a service is a playback of what would have happened in the past. The playback mode can be extremely useful when you’re testing, refining, or demonstrating the functionality of a service.
- The *real-time mode* is used in production where you want to react to events as they arrive. There's no need to modify the service after testing with historical data.  One click is all it takes to activate a service and take it live.

In this chapter, we’ll show how to do the following: 

- Create and edit services.
- Run a historical playback.
- Launch a live service.

We'll also discuss error handling and the best practices that will make your life easier.

##Creating and editing services

You can create a new service or modify an existing service by using the Streamr editor. When you log in to Streamr, the editor with a blank canvas is what you’ll first see. The editor is always accessible by clicking on the **Editor** tab.

You'll find that using the editor is an easy way to create new services. However, you can also create a service programmatically by using the <g:link controller="help" action="api">canvas API</g:link>. 

<g:img dir="images/user-guide" file="blank-canvas-with-arrow.png" class="img-responsive center-block" />

You choose what to do using the control sidebar. There are three things you can do:

- If you want to create a new service, click on the left-most icon in the top row of the sidebar. This is where you'll be taken by default.

   <g:img dir="images/user-guide" file="new-service-with-arrow.png" class="img-responsive"/>

- If you want to view or modify an existing service, click on the icon in the middle.

    <g:img dir="images/user-guide" file="open-service-with-arrow.png" class="img-responsive" />

- If you want to save the service, click on the icon on the right.

    <g:img dir="images/user-guide" file="save-service-with-arrow.png" class="img-responsive" />

The editor's canvas is your workspace for building a service and the event processing logic. You can test the service with a playback of historical data and launch it live when you're ready to go.

When you build a service, you’ll typically start by adding data streams on the canvas.  You’ll then add modules for analytics, visualisation or communication, and define how the data flows streams to modules and from one module to another.  You can do all this interactively by dragging and dropping streams and modules from the sidebar to the canvas and by drawing connections between them.

You can move modules around on the canvas as you wish, but the location of a module has no impact on functionality.  For clarity, though, you may want to design the canvas so that module placement reflects the data flow from the input streams through the modules.

<g:img dir="images/user-guide" file="hide-control-bar-button.png" align="left"  hspace="10" vspace="5" />

As a space-saving hint, note the small icon in the top left corner, just left of the Streamr log.  Click on the icon to hide the editor sidebar.  Click again, and the sidebar reappears. 

If you want to add a stream as an input source, type its name in the search box (**Add Stream / Module**) in the sidebar. The stream name will be autocompleted as soon as it can be identified. Either click on the stream name or press <kbd>Enter</kbd> to select the highlighted stream.

The **Module Browser** lists all the available built-in modules.  You can drill down in different categories when looking for a specific module. If you already know the name of the module you need on the canvas, you can type that name (again with autocomplete) in the sidebar search box.  If there’s several partial matches, you can select the one you want from the popup window or just press <kbd>Enter</kbd> to select the first match.

You can place any module on the canvas in three different ways:

1. By clicking on the **Add Module** button
2. By clicking on a module name.
3. By dragging a module to the canvas.

There’s no limitations on the number of module instances.  Different instances of the same module are independent of each other (unless they are connected, of course).

##Running a playback

By default, the canvas is in a historical mode.  You can specify the time period (the start date and the end date) for a historical playback period in the sidebar.  The playback starts when you press the Run button.

In the historical mode, you can use the dropdown menu to save all Chart inputs in a CSV file during a run.  If there’s more than one Chart on the canvas, each such module will produce a separate output file.

A service can run in either historical mode or real-time mode.  The historical mode is a playback of what would have happened in the past.  The playback mode is useful when you’re testing, refining, or demonstrating functionality.  The real-time mode is used in production where you want to react to events as they arrive.  The same exact canvas and module layout is used in either case.

Whenever you edit the workflow on a canvas, the historical mode is in force.  You’ll switch to the realtime mode when you’re ready for production.

##Launching a live service

What are live services?
How to launch them
They keep running until you stop them
Saved state

<g:img dir="images/user-guide" file="launch-realtime-run.png" class="side-image" />

If you want to run the canvas in the realtime mode, simply switch to the realtime tab.  Press the Run button to activate the canvas.

##Error handling

- What are the possible error situations?
- What runtime errors may happen? Numerical overflow, wrong type of events?
- What happens when the cloud instance dies?
- What happens if the Internet connection is interrupted?
- What other errors are possible?

##Best practises


