<a name="extensions"></a>
#Extensions

**Explain how to do abstraction, reuse services as modules, and code custom Java modules.**

Once in a while, you may find yourself in need of a custom module. Perhaps you need some functionality that cannot be implemented with existing modules. Or perhaps implementing some functionality with existing modules is possible, but cumbersome. Maybe your bottleneck is performance -- you want to do some hefty calculations as fast as possible or you want to minimize memory use.

Whatever the reason, Streamr platform can be extended by writing your own specialized custom modules. In this chapter, we will go through the different ways of doing so.

##JavaModule

**JavaModule** supports writing custom behavior in the Java programming language. Simply add the module to a canvas, and press the "Edit code"-button to open a code editor. The default template code will look as follows.

```
// Define inputs and outputs here
 
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


You will need to fill in this template with appropriate components for the module to work.

A module in Streamr consists of inputs, parameters, outputs, possibly state, and methods that need to be overriden for the magic to happen.

##Writing Your First Custom Module

Let's write a module similar to **Sum**, but instead of keeping a running sum, we will keep a running product of given multiplicands.

How would you write this? First of all, let's visualize how running the module would look like with some example input and expected output.

Example input and output for a single run.

Input   Output
5   5
2   10
3   30
10  300
1/3 100
1   100
0   0

Essentially we have a single input and a single output. Let's start off by declaring our endpoints in the code template.

```
TimeSeriesInput in = new TimeSeriesInput(this, "in");
TimeSeriesOutput out = new TimeSeriesOutput(this, "out");
 
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

Above we declare and instantiate an input field `in` as a `TimeSeriesInput`. This type of input receives floating-point numbers. We also declare and instantiate an output field `out` as `TimeSeriesOutput`. This type of output is used to pass out floating-point numbers to other connected modules.

The constructors for input and output are similar. The first argument is always `this`, a reference to the current module. The second argument defines the name of the endpoint as seen by the rest of the system. By convention, field variable name and input name are the same.

Next, let us add in state by keeping track of the current product.

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

We initialize `product = 1` (instead of zero, or else `a * 0 = 0` for all `a`). Because we have state in our module, we must also implement method `clearState()` so that it resets our module to the initial state. Luckily, our example module consists of such a simple state that this can be achieved through one assignment statement.

Finally, we are only left with the main concern of our module: multiplying numbers. This is achieved by implementing `public void sendOutput()` as shown below:

```
TimeSeriesInput in = new TimeSeriesInput(this, "in");
TimeSeriesOutput out = new TimeSeriesOutput(this, "out");
 
private double product;
 
public void initialize() {
    product = 1;
}
 
public void sendOutput() {
    // Read input
    double newValue = in.getValue();
 
    // Update state by multiplying
    product = product * newValue;
 
    // Send new state via output
    out.send(product);
}
 
public void clearState() {
    product = 1;
}
```

After inserting the code above into **JavaModule** editor and clicking **Apply**, you should see the module appear with input `in` and output `out` as illustrated in the figure below.

<g:img dir="images/user-guide" file="custom-module.png" class="img-responsive center-block" />

The module is now done and can be used like any other on the canvas. If you want some extra challenge, you can try making the initial value into a parameter yourself. Another useful feature is the ability to keep a running product over a moving window (as module **Sum** can be configured to do).

##Inputs

Inputs, visually speaking, are the colored circles and labels found on the left-side of a module on a Canvas. For example, the module Multiply requires inputs A and B to calculate the result of multiplication. The inputs of a module must be connected to compatible outputs for data to arrive and for it to be processed.

<g:img dir="images/user-guide" file="multiply.png" class="img-responsive center-block" />

Inputs can be *optional* or *required*. Optional inputs may or may not be connected to an output depending on desired outcome. Required inputs must be connected to outputs in order for the module to activate, i.e., for the module to read received values on inputs, perform some computation, and spit out results to outputs. Required inputs are colored red when not connected. Optional inputs are gray even when not connected.

Inputs are typed according to the type of data they can receive. The types of inputs are listed below.

TimeSeriesInput for receiving floating-point numeric data. Booleans can be represented as 0.0 (false) and 1.0 (true).

StringInput for receiving string data.

ListInput for receiving multiple values as lists (or arrays) of data.
MapInput for receiving map data (think `java.util.Map ` in Java, object or JSON in Javascript, (associative) array in PHP.)

`Input<Object>` for any data. Often used when not interested in content itself, but rather arrival of content.

Input types must match output types. In other words, you cannot connect a output that produces strings to an input that expects floating-point numbers.

##Parameters

Parameters are basically just inputs that have default values and that have a distinct visual look on the canvas. A parameter is optional in the sense that no connection to an output is necessary. However, if connected, the module should be written in such a way as to be able to handle parameter value changes while being run.

Possible parameter types are listed below.

BooleanParameter
:   This is used for boolean (true or false) values. These are displayed as drop-down selection.

DoubleParameter
:   This is used for floating-point numbers (displayed as input).

IntegerParameter
:   This is used for integers (displayed as input).

StringParameter
:   This is used for strings (displayed as input)

ColorParameter
:   This is used for RGB colors (displayed as a color selector).

##Outputs

Outputs, the counterpart of inputs, reside at the right-hand side of a module and are responsible for sending out computed values after a module has been activated. E.g., the **Multiply** module, on activation, multiplies the values of its two inputs and then sends the product to an output named `A*B`. The product will then be passed to 0..n inputs connected to the output.

Outputs are typed according to the type of data they send out. The types of outputs are listed below.

TimeSeriesOutput
:   This is used for floating-point numeric data. Booleans can be represented as 0.0 (false) and 1.0 (true).

StringOutput
:   This is used for string data.

ListOutput
:   This is used for a list (or array).

MapOutput
:   This is used for map data (think `java.util.Map` in Java, object or JSON in Javascript, or associative array in PHP and in many other languages).

`Output<Object>` 
:   This can be used for any data. `Output<Object>` is often used when you are not interested in the content itself, but rather in whether there is content or not.

##State

The state of a module can be kept in its fields (aka instance variables or member variables). For example, the **Count** module keeps track of the number of received values by having an integer counter as a field (`private int counter = 0;`) Everytime a new value is received, it increments the counter by one and spits the new counter value through its output.

There is a caveat concerning fields. You should make sure that they are serializable, i.e., their type implements `java.io.Serializable`. If this is not the case, you can declare the field transient by adding keyword `transient` to its declaration. However, you need take special precautions when working with transient fields in modules. Specifically, the value of a transient field may be `null` at the beginning of `sendOutput() due to Java's object de-serialization.

##Methods

There are three methods that you need to override and implement.

Reading input values, performing calculations, and sending results to outputs are all done in the method `public void sendOutput()`.

The remaining two methods are related to state. Method `public void initialize()` is used to initialize the fields of a module. Method `public void clearState()` clears the fields of a module. The desired semantic is that invoking `clearState() reset the module to its initial state.
