# Extensions

You can easily extend Streamr by writing custom modules in Java programming language. When a custom module is activated in a Streamr canvas, your code is executed as if the module were a part of the built-in machinery. As an alternative to custom code, you can also do abstraction, i.e. reuse existing canvases as modules. 

## JavaModule

JavaModule is the tool to use for custom code. Start by creating a new canvas or by opening an existing canvas in the editor. Then insert a JavaModule on the workspace, click on the “Edit code” button, and a code editor will open in a resizable pop-up window. This is what you'll see:

```
// Define inputs and outputs here
// TimeSeriesInput input = new TimeSeriesInput(this,"in");
// TimeSeriesOutput output = new TimeSeriesOutput(this,"out");

public void initialize() {
    // Initialize local variables
}
 
public void sendOutput() {
    //Write your module code here
}
 
public void clearState() {
    // Clear internal state
}
```

The Java editor contains a code template that you need to fill in with the appropriate components. A custom module consists of inputs, parameters, outputs, an optional state, and a few specific methods. For the magic to happen, you'll need to specify the inputs and outputs and override the relevant methods. We'll go through the components below.

## Inputs and outputs

Module inputs correspond to the endpoints that receive incoming events. On the canvas, they are shown as small circles on the left-hand side of a module. The outputs correspond to the endpoints which send out computed values after module activation. The output endpoints are shown as small circles on the right-hand side of a module.

Inputs and outputs are defined in the beginning of the code template. To help you get started, there's two lines of commented code near the top.

```
// Define inputs and outputs here
TimeSeriesInput input = new TimeSeriesInput(this,"in");
TimeSeriesOutput output = new TimeSeriesOutput(this,"out");

public void initialize() {
    // Initialize local variables
}
 
public void sendOutput() {
    //Write your module code here
}
 
public void clearState() {
    // Clear internal state
}
```

If you uncomment those lines (as we've done above), you will get a module with one numerical input and one numerical output. If you want to see the result on the canvas, first click the “Apply” button and then the “Close” button. 

<r:img plugin="unifina-core" dir="images/user-guide" file="java-module-on-canvas.png" class="img-responsive center-block" />

In this example, the inputs belong to the `TimeSeriesInput` class and the outputs to the `TimeSeriesOutput` class. The first argument of an input or output constructor is always `this`, a reference to the current module. The second argument is there for the display name, i.e. a visual label for the endpoint. The variable name on the left-hand side of the assignment can be any valid variable name in Java.

The input and output variables must be unique within a module, but the display names are only labels with no deeper meaning. They don’t have to be unique, and an empty string is a valid name. A common convention is to make the display name equal to the variable name, but this is not a requirement.

You're not limited to numerical endpoints. These are the possible choices for an input event:

TimeSeriesInput
:   Used for numeric floating point data.

BooleanInput
:   Used for boolean data.

StringInput
:   Used for string data.

ListInput
:   Used for lists (or arrays) of data.

MapInput
:   Used for key-value pairs.

These are the possible choices for an output event:

TimeSeriesOutput
:   Used for numeric floating point data.

BooleanOutput
:   Used for boolean data.

StringOutput
:   Used for string data.

ListOutput
:   Used for lists (or arrays) of data.

MapOutput
:   Used for key-value pairs.

Parameters are just inputs with default values. Such inputs have a distict visual look in the Streamr editor. Because a parameter has a default value, there is no need for an incoming connection in the corresponding endpoint. If there is a connection, however, the custom module should take any parameter changes into account at run-time. Possible parameter types are listed below.

BooleanParameter
:   Used for boolean values (displayed as a drop-down selection).

DoubleParameter
:   Used for numeric floating point data (displayed as an input).

IntegerParameter
:   Used for integers (displayed as an input).

StringParameter
:   Used for strings (displayed as an input).

ColorParameter
:   Used for RGB colors (displayed as a color selector).

There’s no limitations on the number of incoming and outgoing connections. An an example, the following code would give you three inputs (one of which is a parameter) and two outputs:

```
// Define inputs and outputs here
TimeSeriesInput value = new TimeSeriesInput(this,"Value");
StringInput source = new StringInput(this,"Source");
BooleanParameter mode = new BooleanParameter(this,"Mode");
TimeSeriesOutput score = new TimeSeriesOutput(this,"Score");
BooleanOutput match = new BooleanOutput(this,"Match?");
```

## State and methods

Every Streamr module can have a state. If present, the state persists between module activations and even when a live service is stopped and later restarted. Whilst a module does not need to have a state, there are many streaming data operations which simply cannot be implemented without one.

The state of a module is kept in its *instance variables* (aka *member variables* or *member fields*). Each instance of JavaModule has its own variables, and these are visible and acccessible in that one instance only. You can use any of the valid Java data types for the instance variables. Here's some examples of valid declarations:

```
private int counter;
private boolean active;
private double temperature;
private String greeting = "Hello world!";
private double[] assignments = new long[10];
```

Any manipulation of the module state and the generation of module output is handled by JavaModule's methods. There are three methods that you need to override and implement. They are the following:

`initialize()`
:   This method is called once when a specific JavaModule is activated. This is where you define and initialise the instance variables (i.e. the module state).

`sendOutput()`
:   This is where you read the incoming events, perform arbitrary calculations, and send events downstream. You can see and alter the module state here.

`clearState()`
:   This is where you reset the module state. Any module **must** be able to reset itself to its initial state on request. This is typically done by reinitialising the instance variables.

## Custom module example

For the sake of illustration, let's create a new JavaModule. It will be similar to the built-in **Sum**, but instead of keeping a running sum, we'll calculate a running product of successive numerical events.

We'll start with the module inputs and outputs. We only need one numerical input and one output here, so we'll just uncomment the relevant lines:

```
// Define inputs and outputs here
TimeSeriesInput input = new TimeSeriesInput(this,"in");
TimeSeriesOutput output = new TimeSeriesOutput(this,"out");

public void initialize() {
    // Initialize local variables
}
 
public void sendOutput() {
    //Write your module code here
}
 
public void clearState() {
    // Clear internal state
}
```

In this example the module state is equal to the current value of the cumulative product. We'll call the state `product` and initialise it by the assignment `product = 1` in the `initialize` method. As discussed, we also need to reset the module to its initial state on request. Let's just redo the initial assignment when the `clearState` method is called.

```
TimeSeriesInput in = new TimeSeriesInput(this, "in");
TimeSeriesOutput out = new TimeSeriesOutput(this, "out");
 
private double product;
 
public void initialize() {
    product = 1;
}
 
public void sendOutput() {
//Write your module code here
}
 
public void clearState() {
    product = 1;
}
```

All that's left to do is to write the code to multiply the product by the new incoming event, save the state, and submit the output.

```
TimeSeriesInput in = new TimeSeriesInput(this, "in");
TimeSeriesOutput out = new TimeSeriesOutput(this, "out");
 
private double product;
 
public void initialize() {
    product = 1;
}
 
public void sendOutput() {
    // Read input.
    double newValue = in.getValue();
 
    // Update the state.
    product = product * newValue;
 
    // Send output.
    out.send(product);
}
 
public void clearState() {
    product = 1;
}
```

The code in a JavaModule is compiled and validated when you click the “Apply” button. Unless there's syntax errors in the code, the module is now ready to use. This is what the output looks like with sample input data:

<r:img plugin="unifina-core" dir="images/user-guide" file="java-module-example-on-canvas.png" class="img-responsive center-block" />




