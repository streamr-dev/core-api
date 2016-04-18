#Modules

Modules are the workhorses which you'll use time and again as part of Streamr services. These are the module categories:

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

In this chapter, we'll go through the module basics. For details on different modules, either see the individual module help in Streamr editor or have a look at the <g:link controller="help" action="module">module reference</g:link>.

##Inputs, outputs, parameters, and options

A module can have inputs, outputs, parameters, and options.  Whilst a module does not need to have any inputs or outputs, useful modules will typically allow for either incoming or outgoing data (and usually both).

When placed on a canvas, the inputs are shown as circular connectors along the left-hand side of the module.  The outputs are shown as connectors along the right-hand side.

Many modules have parameters which control their operation.  Module parameters can be hardcoded, but their values are usually not immutable.  If a parameter can be modified at run-time, there is an associated parameter input at the left-hand edge of the module.

<g:img dir="images/user-guide" file="round-to-step-module.png" class="side-image" />

As an example, the **RoundToStep** module has three inputs, two parameters, and one output.  The first two inputs correspond to the module’s parameters, i.e. precision and mode.  The last input is a numeric value which will be rounded with the specified precision in the direction specified by the mode.  The module output is equal to the rounded input.

<br style="clear:both;" />

Options control its behaviour or appearance of a module.  Options apply to a specific instance of the module, and they can only be changed through the canvas.

As an example, **Table** only shows one data column by default.  If you hover on top of the module and click on the wrench icon, you’ll see the available options in a pop-up window.  Change the number of inputs to 3, press **OK**, and you’ll get a **Table** with three columns.

<g:img dir="images/user-guide" file="table-module-options.png" class="img-responsive center-block" />

Inputs, outputs, and parameters can be renamed.  If you move the mouse on top of a name, a click brings up a pop-up menu which allows you to give the endpoint or parameter a new display name.  Renaming has no bearing on functionality.

<g:img dir="images/user-guide" file="module-popup-menu.png" class="img-responsive center-block" />

You’ll see a number of small icons next to the endpoints when you hover on top of a module.  These icons correspond to additional controls which are relevant to module activation.  We’ll have more to say on this below, but let’s first discuss the data flow between modules.

<hr style="width: 50%; border-top: #E9570F solid 1px;  margin-top: 20px; margin-bottom: 20px">

##Making connections

A data flow between two modules — or a data flow between a stream and a module — is created by drawing a connection from an output endpoint to an input endpoint with the mouse or other pointing device.  You can create as many outgoing connections (i.e. connections that originate from an output connector) as you wish.  All connections are unidirectional, i.e. the data always flows from an output to one or more inputs in one direction only.  Only one connection per input is allowed, and the modules form a directed graph.

On a canvas, the direction of the data flow is indicated by an arrow on top of the connection path.  You can alter the endpoint of an existing connection by dragging it to another input endpoint.  If you instead drop the endpoint in an empty space, the connection is cleared.  A mouse click on top of a module brings up an pop-up menu where you can choose to disconnect all incoming connections to the module.

<hr style="width: 50%; border-top: #E9570F solid 1px;  margin-top: 20px; margin-bottom: 20px">

##Module activation 

A module processes its inputs as soon as it is activated.  The activation happens when the following conditions are both satisfied.
1. Every input has a value.
2. An event arrives at one of the driving inputs.

That’s all there is to it.  As soon as the two conditions are met, a module processes its inputs.  A module with no driving inputs will never activate.

As part of the processing, the module may send one or more events downstream from the output endpoints.  It is important to note that a module does not need to submit any output.  It may take care of some side effect instead, or the inputs may be such that there’s no point in sending data onwards.

<g:img dir="images/user-guide" file="and-module.png" class="side-image" />

At least one endpoint is designated as a driving input by default.  To change the default settings, hover on top of a module and you’ll see a number of additional controls.  You can make any input a driver by clicking on the associated **DR** icon (a toggle button) next to an input connector.  

As mentioned, every input must a have a value before anything happens.  The input values typically arrive either as events from a stream, or from the output endpoints in some other module(s).  If the input corresponds to a numeric value or a string, you can also specify an explicit initial value.  If you click on a **IV** icon next to an input, you’ll see an initial value dialog.

<g:img dir="images/user-guide" file="initial-value-dialog.png" class="img-responsive center-block" />

By default, feedback loops are not allowed.  This is because feedback and event processing do not always mix well (just think back to the last time you placed a live microphone too close to a loudspeaker).

But if you really want to create a feedback loop, we won’t stop you.  If you click on the **FB** icon next to an input connector, the endpoint will now accept events that originate from the same module.  If feedback is disabled, the endpoint won’t accept feedback events either directly or indirectly (i.e. not even when recycled through other modules).

Lastly, note the **NR** icon next to each output connector.  This is a non-repeat button, and if it’s on, the module suppresses any output that would be an exact replica of the last outgoing event.  This covers the use case where you’re only interested in events that represent something new.

Module activation is an important concept.  The governing principles are few in number, and once you master those principles, you’re well on your way to understanding how event processing works. 

##Reusing functionality

Explain how to do abstraction, reuse services as modules, and code custom modules.
