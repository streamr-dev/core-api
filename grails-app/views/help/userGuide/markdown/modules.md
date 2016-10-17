# Modules

Modules process the data emanating from event streams. All Streamr [canvases](#canvases) consist of streams (they provide the data) and modules (they process the data).

A module is close akin to what you'd call a function, subroutine, procedure, or a method in various programming languages. In Streamr, modules are specialised computation units for handling streaming real-time data. A module processes its inputs as soon as it is activated by the arrival of a new event. The module may have one or more outputs, or it may take care of some side effect instead. 

There’s no limitations on the number of module instances, i.e. on the number of of times the same module is used in a canvas or in different canvases.  Different instances of the same module are independent of each other (unless one feeds data to the other).

A module has an internal state, and it can and typically will update that state when it is executed. How this is done depends on the particular module. The statefulness is an important feature and one the key ingredients in real-time stream processing.

## Built-in modules

There's a number of built-in modules on the Streamr platform. They fall into the following categories:

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

For details on different modules, either see the individual module help in the Streamr editor or have a look at the <g:link controller="module" action="list">module reference</g:link>.

## Inputs, outputs, and parameters

A module can have inputs, outputs, and parameters.  Whilst a module does not need to have any inputs or outputs, useful modules will typically allow for either incoming or outgoing data (and usually both).

When placed on a canvas, the inputs are shown as circular connectors along the left-hand side of the module.  The outputs are shown as connectors along the right-hand side.

<r:img plugin="unifina-core" dir="images/user-guide" file="round-to-step-module.png" class="side-image" />

Many modules have parameters which control their operation.  Module parameters can be hardcoded, but their values are typically not immutable.  If a parameter can be modified at run-time, there is an associated parameter input at the left-hand edge of the module.

As an example, the **RoundToStep** module has three inputs, two parameters, and one output.  The first two inputs correspond to the module’s parameters, i.e. precision and mode.  The last input is a numeric value which will be rounded with the specified precision in the direction specified by the mode.  The module output is equal to the rounded input.

Inputs, outputs, and parameters can be renamed.  If you move the mouse on top of a name, a click brings up a pop-up menu which allows you to give the endpoint or parameter a new display name.  Renaming has no bearing on functionality.

<r:img plugin="unifina-core" dir="images/user-guide" file="module-popup-menu.png" class="img-responsive center-block" />

You’ll see a number of small icons next to the endpoints when you hover on top of a module.  These icons correspond to additional controls which are relevant to module activation.  We’ll have more to say on this below, but let’s first discuss the data flow between modules.

## Module options

Modules can have options which control their behaviour or appearance.  Options apply to a specific instance of the module, and they can only be changed through the editor.

<r:img plugin="unifina-core" dir="images/user-guide" file="table-module-options.png" class="img-responsive center-block" />

As an example, **Table** only shows one data column by default.  If you hover on top of the module and click on the wrench icon, you’ll see the available options in a pop-up window.  Change the number of inputs to 3, press **OK**, and you’ll get a **Table** with three columns.

## Activation

A module processes its inputs as soon as it is activated.  This happens when the following conditions are both satisfied.
1. Every input has a value.
2. An event arrives at one of the driving inputs.

<r:img plugin="unifina-core" dir="images/user-guide" file="and-module.png" class="side-image" />

At least one endpoint in any module is designated as a driving input by default.  To change the default settings, hover on top of a module and you’ll see a number of additional controls.  You can make any input a driver by clicking on the associated **DR** icon (a toggle button) next to an input connector.  A module with no driving inputs will never activate.

As mentioned, every input must a have a value before anything happens.  The input values typically arrive either as events from a stream, or from the output endpoints in some other module(s).  If the input corresponds to a numeric value or a string, you can specify an explicit initial value.  If you click on a **IV** icon next to an input, you’ll see an initial value dialog.

<r:img plugin="unifina-core" dir="images/user-guide" file="initial-value-dialog.png" class="img-responsive center-block" />

<a name="feedbackloops"></a>

By default, feedback loops are not allowed.  This is because feedback and event processing do not always mix well (just think back to the last time you placed a live microphone too close to a loudspeaker).

If you really want to create a feedback loop, we won’t stop you.  If you click on the **FB** icon next to an input connector, the endpoint will now accept events that originate from the same module.  If feedback is disabled, the endpoint won’t accept feedback events either directly or indirectly (i.e. not even when recycled through other modules).

Lastly, note the **NR** icon next to each output connector.  This is a non-repeat button, and if it’s on, the module suppresses any output that would be an exact replica of the last outgoing event.  This covers the use case where you’re only interested in events that represent something new.


