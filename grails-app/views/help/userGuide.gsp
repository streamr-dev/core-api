<html>
<head>
	<meta name="layout" content="main" />
	<title>User guide</title>

	<r:require module="user-guide"/>
	<r:require module="codemirror"/>

	<r:script>
		// Draws sidebar with scrollspy. If h1 -> first level title. If h2 -> second level title.
		// Scrollspy uses only titles to track scrolling. div.help-text elements are not significant for the scrollspy.
		new UserGuide("#module-help-tree", "#sidebar")
	</r:script>

	<r:script>
		$(document).ready(function() {
			var textAreaElements = document.querySelectorAll("textarea");
			for (var i=0; i < textAreaElements.length; ++i) {
				CodeMirror.fromTextArea(textAreaElements[i]);
			}
		});
	</r:script>

</head>
<body class="user-guide">

<ui:flashMessage/>

<ui:breadcrumb>
	<g:render template="/help/breadcrumb"/>
</ui:breadcrumb>

<div class="row">
	<div class="col-sm-12">
		<div class="scrollspy-wrapper col-md-9" id="module-help-tree">

// Add a nice clear divider between each of the sections below.

			<markdown:renderHtml template="userGuide/what_is_streamr" />
			<markdown:renderHtml template="userGuide/getting_started" />
			<markdown:renderHtml template="userGuide/what_is_a_stream" />
			<markdown:renderHtml template="userGuide/working_with_streams" />
			<markdown:renderHtml template="userGuide/working_with_canvases" />

			<h1>Defining streams</h1>
			<h2>API Streams</h2>
			<h2>Database (MongoDb) poller streams</h2>

			<h1>Building Logic</h1>
			<h2>Canvas basics (adding, connecting, running)</h2>
			<h2>Visualizing data</h2>
			<h2>Modules</h2>
			<h2>Controlling Module Activation</h2>
			<h2>Reusing Functionality</h2>
			<h2>Best Practices</h2>
			<h1>Running Live</h1>
			<h1>Dashboards</h1>
			<h1>Embedding Widgets</h1>
			<h1>Custom Modules</h1>
			<p>
				Once in a while, you may find yourself in need of a custom module.
				Perhaps you need some functionality that cannot be implemented with existing modules. Or perhaps
				implementing some functionality with existing modules is possible, but cumbersome. Maybe your bottleneck
				is performance -- you want to do some hefty calculations as fast as possible or you want to
				minimize memory use.
			</p>

			<p>
				Whatever the reason, Streamr platform can be extended by writing your own specialized custom modules. In
				this Chapter, we will go through the different ways of doing so.
			</p>

			<h2>JavaModule</h2>
			<p>
				<code>JavaModule</code> supports writing custom behavior in the Java programming language. Simply add the module
				to a Canvas, and press the "Edit code"-button to open a code editor. The default template code will look
				as follows.
			</p>

			<textarea>
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
			</textarea>

			<p>
				You will need to fill in this template with appropriate components for the module to work.
			</p>

			<p>
				A module in Streamr consists of <em>inputs</em>, <em>parameters</em>, <em>outputs</em>, possibly
				<em>state</em>, and <em>methods</em> that need to be overriden for the magic to happen.
			</p>

			<h3>Tutorial: Writing Your First Custom Module</h3>
			<p>
				Let's write a module similar to <code>Sum</code>, but instead of keeping a running sum, we will
				keep a running product of given multiplicands.
			</p>

			<p>
				How would you write this? First of all, let's visualize how running the module would look like with some
				example input and expected output.
			</p>

			<table>
				<caption>Example input and output for a single run.</caption>
				<thead>
					<tr>
						<th>Input</th>
						<th>Output</th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>5</td>
						<td>5</td>
					</tr>
					<tr>
						<td>2</td>
						<td>10</td>
					</tr>
					<tr>
						<td>3</td>
						<td>30</td>
					</tr>
					<tr>
						<td>10</td>
						<td>300</td>
					</tr>
					<tr>
						<td>1/3</td>
						<td>100</td>
					</tr>
					<tr>
						<td>1</td>
						<td>100</td>
					</tr>
					<tr>
						<td>0</td>
						<td>0</td>
					</tr>
				</tbody>
			</table>

			<p>
				Essentially we have a single input and a single output. Let's start off by declaring our endpoints in the code
				template.
			</p>

			<textarea>
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
			</textarea>

			<p>
				Above we declare and instantiate an input field <code>in</code> as a <code>TimeSeriesInput</code>. This
				type of input receives floating-point numbers. We also declare and instantiate an output
				field <code>out</code> as <code>TimeSeriesOutput</code>. This type of output is used to pass out
				floating-point numbers to other connected modules.
			</p>

			<p>
				The constructors for input and output are similar. The first argument is always <code>this</code>,
				a reference to the current module. The second argument defines the name of the endpoint as seen by the
				rest of the system. By convention, field variable name and input name are the same.
			</p>

			<p>
				Next, let us add in state by keeping track of the current product.
			</p>

<textarea>
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
</textarea>

			<p>
				We initialize <code>product = 1</code> (instead of zero, or else <code>a * 0 = 0</code> for all
				<code>a</code>). Because we have state in our module, we must also implement method
				<code>clearState()</code> so that it resets our module to the initial state. Luckily, our example
				module consists of such a simple state that this can be achieved through one assignment statement.
			</p>

			<p>
				Finally, we are only left with the main concern of our module: multiplying numbers. This is achieved by
				implementing <code>public void sendOutput()</code> as is shown below:
			</p>

			<textarea>
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
			</textarea>

			<p>
				After inserting the code above into <code>JavaModule</code>'s editor and clicking 'Apply', you should
				see the module appear with input <code>in</code> and output <code>out</code> as illustrated in the
				Figure below.
			</p>

			<figure>
				<g:img dir="images/user-guide" file="custom-module.png" alt="JavaModule on Canvas with input named in and output named out." />
				<figcaption>JavaModule for running multiplication. Input and output visible.</figcaption>
			</figure>

			<p>
				The module is now done and can be used like any other on the Canvas. If you want some extra challenge,
				you can try making the initial value into a parameter yourself. Another useful feature is the ability
				to keep a running product over a moving window (as module <code>Sum</code> can be configured to do).
			</p>

			<h3>Inputs</h3>

			<p>
				<em>Inputs</em>, visually speaking, are the colored circles and labels found on the left-side of a module on a Canvas. For
				example, the module <code>Multiply</code> requires inputs <code>A</code> and <code>B</code> to
				calculate the result of multiplication. The inputs of a module must be <em>connected</em> to compatible
				outputs for data to arrive and for it to be processed.
			</p>

			<figure>
				<g:img dir="images/user-guide" file="multiply.png" alt="Multiply module" />
				<figcaption>Multiply module</figcaption>
			</figure>

			<p>
				Inputs can be <em>optional</em> or <em>required</em>. Optional inputs may or may not be connected to an output depending
				on desired outcome. Required inputs must be connected to outputs in order for the module to
				<em>activate</em>, i.e., for the module to read received values on inputs, perform some computation,
				and spit out results to outputs. Required inputs are colored
				<span style="color: #FF6969;">red</span> when not connected. Optional inputs are gray even
				when not connected.
			</p>

			<p>
				TODO: Driving inputs, Initial value, feedback connections...
			</p>

			<p>
				Inputs are typed according to the type of data they can receive. The types of inputs are listed below.
			</p>

			<ul>
				<li>
					<code>TimeSeriesInput</code> for receiving floating-point numeric data. Booleans can be represented
					as 0.0 (false) and 1.0 (true).
				</li>
				<li>
					<code>StringInput</code> for receiving string data.
				</li>
				<li>
					<code>ListInput</code> for receiving multiple values as lists (or arrays) of data.
				</li>
				<li>
					<code>MapInput</code> for receiving map data (think <code>java.util.Map</code> in Java, object or JSON
					in Javascript, (associative) array in PHP.)
				</li>
				<li>
					<code>Input&lt;Object&gt;</code> for any data. Often used when not interested in content itself,
					but rather arrival of content.
				</li>
			</ul>

			<p>
				Input types must match output types. In other words, you cannot connect a output that produces strings
				to an input that expects floating-point numbers.
			</p>

			<h3>Parameters</h3>

			<p>
				Parameters are basically just inputs that have default values and that have a distinct visual look on the
				canvas. A parameter is optional in the sense that no connection to an output is necessary. However, if
				connected, the module should be written in such a way as to be able to handle parameter value changes
				while being run.
			</p>

			<p>
				Possible parameter types are delineated below.
			</p>

			<ul>
				<li><code>BooleanParameter</code> for true/false values (displayed as drop-down selection)</li>
				<li><code>DoubleParameter</code> for floating-point numbers (displayed as input)</li>
				<li><code>IntegerParameter</code>for integers (displayed as input)</li>
				<li><code>StringParameter</code>for strings (displayed as input)</li>
				<li><code>ColorParameter</code> for RGB colors (displayed as color selector)</li>
			</ul>

			<h3>Outputs</h3>

			<p>
				Outputs, the counterpart of inputs, reside at the right-hand side of a module and are responsible for
				sending out computed values after a module has been activated. E.g., the <code>Multiply</code> module,
				on activation, multiplies the values of its two inputs and then sends the product to an output named
				<code>A*B</code>. The product will then be passed to 0..n inputs connected to the output.
			</p>

			<p>
				Outputs are typed according to the type of data they send out. The types of outputs are listed below.
			</p>

			<ul>
				<li>
					<code>TimeSeriesOutput</code> for sending floating-point numeric data. Booleans can be represented
				as 0.0 (false) and 1.0 (true).
				</li>
				<li>
					<code>StringOutput</code> for sending string data.
				</li>
				<li>
					<code>ListOutput</code> for sending a list (or array).
				</li>
				<li>
					<code>MapOutput</code> for sending map data (think <code>java.util.Map</code> in Java, object or JSON
				in Javascript, (associative) array in PHP.)
				</li>
				<li>
					<code>Output&lt;Object&gt;</code> for any data. Often used when not interested in content itself,
					but rather occurrence of content.
				</li>
			</ul>


			<h3>State</h3>
			<p>
				The state of a module can be kept in its
				<a href="http://tutorials.jenkov.com/java/fields.html">fields</a> (aka instance variables or member
				variables). For example, the <code>Count</code> module keeps track of the number of received values by
				having an integer counter as a field (<code>private int counter = 0;</code>) Everytime a new value is
				received, it increments the counter by one and spits the new counter value through its output.
			</p>

			<p>
				There is a caveat concerning fields. You should make sure that they are serializable, i.e., their type
				implements <code>java.io.Serializable</code>. If this is not the case, you can declare the field
				transient by adding keyword <code>transient</code> to its declaration. However, you need take special
				precautions when working with transient fields in modules. Specifically, the value of a transient field
				may be <code>null</code> at the beginning of <code>sendOutput()</code> due to Java's object
				de-serialization.
			</p>

			<h3>Methods</h3>

			<p>
				There are three methods that you need to override and implement.
			</p>

			<p>
				Reading input values, performing calculations, and sending results to outputs are all done in
				the method <code>public void sendOutput()</code>.
			</p>

			<p>
				The remaining two methods are related to state. Method <code>public void initialize()</code> is used to
				initialize the fields of a module. Method <code>public void clearState()</code> clears the fields of
				a module. The desired semantic is that invoking <code>clearState()</code> reset the module to its
				initial state.
			</p>

			<h1>Using Streams Outside Streamr</h1>
		</div>


		<!-- Don't remove this div -->
		<div class="col-xs-0 col-sm-0 col-md-3" id="sidebar"></div>
	</div>
</div>
</body>
</html>
