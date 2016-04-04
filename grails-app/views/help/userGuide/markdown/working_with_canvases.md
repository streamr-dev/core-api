#Working with canvases

A canvas is a browser-based workspace where you build the event processing logic.  When you log in to Streamr, a blank canvas is what you’ll first see.


The canvas is where you define how real-time data flows from one module to the next.  You’ll typically use one or more streams as data sources.  You’ll connect the streams to different modules to process the data, transform it, or take some kind of action.  You can test the canvas with historical data and launch it live when ready to go.


##Data flow and event processing

Computation on the Streamr platform is entirely event-based.  Any module in a live application will execute when activated by incoming events.  When new events arrive in an input stream, the data automatically and immediately flows through the canvas.  This process is inherently asynchronous and allows for fast and continuous in-memory processing of large volumes of real-time data.

You’ll typically start building an application by adding data streams on the canvas.  You’ll then add modules for analytics, visualisation or communication, and define how the data flows from one module to another.  You can do all this interactively by dragging and dropping streams and modules in place and by drawing connections between them.  All the connections are unidirectional, i.e. the data flows in one direction only.  The modules form a directed graph.

As a simple example, let’s place a stream and a chart module on the canvas, and connect the stream outputs to chart inputs.  When you run the canvas, the events flow from the stream to the chart, and the chart draws the data points as they arrive.


You can move modules around on the canvas as you wish, but the location of a module has no impact on functionality.  For clarity, though, you may want to design the canvas so that module placement reflects the data flow from the input streams through the processing steps.

##Historical mode and real-time mode

An application can run in either historical mode or real-time mode.  The historical mode is a playback of what would have happened in the past.  The playback mode is useful when you’re testing, refining, or demonstrating what the application does.  The real-time mode is used in production where the application reacts to events as they arrive.  The same exact canvas and module layout is used in either case.

Whenever you edit the workflow on a canvas, the historical mode is in force.  You’ll need to explicitly launch the canvas as a live application when you’re ready for production.  We’ll describe later how this is done.

##Control elements

The sidebar on the left contains the control elements for working on a canvas.  As a space-saving hint, you can click on the small icon in the top left corner to hide the sidebar. 

On top of the sidebar, there’s pushbuttons which allow you to load an existing canvas or save the current canvas.

The sidebar also contains Run Options and the Run button.  The options are used to specify the playback period.  The playback starts when you press the Run button.


You can use the dropdown menu next to the Run button if you want to either launch the canvas live or save Chart inputs in a CSV file.  If there’s more than one chart on the canvas, each will produce a separate output file.


There’s also a search box (Add Stream / Module) and a module browser.  If you need to find a stream, type its name in the search box.  The stream name will be autocompleted as soon as it can be identified.

If you know the name of the module you need on the canvas, you can type that name (again with autocomplete) in the search box.  If there’s several partial matches, you can select the one you want from the popup window or just press Enter to select the first match.


The Module Browser lists all the available built-in modules.  You can drill down in different categories when looking for a specific module.  These are the module categories:
- Visualisation.
- Filtering, sampling and aggregation.
- Stream operations.
- Time series operations.
- Arithmetics.
- Statistics.
- Logical operators.
- Time & date.
- Text processing.
- Utility modules.
- Extensions and abstraction.
- Connectivity.

You can place any module on the canvas either (1) by clicking on the Add Module button, (2) by double-clicking on the highlighted module, or (3) by dragging the highlighted module to the canvas.  There’s no limitations on the number of module instances, and the modules are independent of each other.

##Inputs, outputs, parameters, and options

A module can have inputs, outputs, parameters, and options.  Whilst a module does not need to have any inputs or outputs, useful modules will typically allow for either incoming or outgoing data (and usually both).

When placed on a canvas, the inputs are shown as circular connectors along the left-hand side of the module.  The outputs are shown as connectors along the right-hand side.

Many modules have parameters which control their operation.  Module parameters can be hardcoded, but their values are usually not immutable.  If a parameter can be modified at run-time, there is an associated parameter input at the left-hand edge of the module.

As an example, the RoundToStep module has three inputs, two parameters, and one output.  The first two inputs correspond to the module’s parameters, i.e. precision and mode.  The last input is a numeric value which will be rounded with the specified precision in the direction specified by the mode.  The module output is equal to the rounded input.

Options control its behaviour or appearance of a module.  Table, for instance, only shows one data column by default.  If you hover on top of the module and click on the wrench icon, you’ll see the available options in a pop-up window.  Change the number of inputs to 3, press OK, and you’ll get a Table with three columns.


Options apply to a specific instance of the module, and they can only be changed through the canvas.

Inputs, outputs, and parameters can be renamed.  If you move the mouse on top of a name, a click brings up a pop-up menu which allows you to give the endpoint or parameter a new display name.  Renaming has no bearing on functionality.


You’ll see a number of small icons next to the endpoints when you hover on top of a module.  These icons correspond to additional controls which are relevant to module activation.  We’ll have more to say on this below, but let’s first discuss the data flow between modules.

##Making connections

A data flow between two modules — or a data flow between a stream and a module — is created by drawing a connection from an output endpoint to an input endpoint with the mouse or other pointing device.  You can create as many outgoing connections (i.e. connections that originate from an output connector) as you wish.  All connections are directional, i.e. the data always flows from an output to one or more inputs.  Only one connection per input is allowed.

On a canvas, the direction of the data flow is indicated by an arrow on top of the connection path.  You can alter the endpoint of an existing connection by dragging it to another input endpoint.  If you instead drop the endpoint in an empty space, the connection is cleared.  A mouse click on top of a module brings up an pop-up menu where you can choose to disconnect all incoming connections to the module.

##Module activation 

A module processes its inputs as soon as it is activated.  The activation happens when the following conditions are both satisfied.
1. Every input has a value.
2. An event arrives at one of the driving inputs.

That’s all there is to it.  As soon as the two conditions are met, a module processes its inputs.  A module with no driving inputs will never activate.

As part of the processing, the module may send one or more events downstream from the output endpoints.  It is important to note that a module does not need to submit any output.  It may take care of some side effect instead, or the inputs may be such that there’s no point in sending data onwards.

At least one endpoint is designated as a driving input by default.  To change the default settings, hover on top of a module and you’ll see a number of additional controls.  You can make any input a driver by clicking on the associated DR icon (a toggle button) next to an input connector.  

As mentioned, every input must a have a value before anything happens.  The input values typically arrive either as events from a stream, or from the output endpoints in some other module(s).  If the input corresponds to a numeric value or a string, you can also specify an explicit initial value.  If you click on a small IV icon next to an input, you’ll see an initial value dialog.


By default, feedback loops are not allowed.  This is because feedback and event processing do not always mix well (just think back to the last time you placed a live microphone too close to a loudspeaker).

But if you really want to create a feedback loop, we won’t stop you.  If you click on the FB icon next to an input connector, the endpoint will now accept events that originate from the same module.  If feedback is disabled, the endpoint won’t accept feedback events either directly or indirectly (i.e. not even when recycled through other modules).

Lastly, note the NR icon next to each output connector.  This is a non-repeat button, and if it’s on, the module suppresses any output that would be an exact replica of the last outgoing event.  This covers the use case where you’re only interested in events that represent something new.

Module activation is an important concept.  The governing principles are few in number, and once you master those principles, you’re well on your way to understanding how event processing works. 

------
