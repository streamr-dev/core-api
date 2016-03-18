<html>
<head>
	<meta name="layout" content="main" />
	<title>User guide</title>

	<r:require module="user-guide"/>

	<r:script>
		// Draws sidebar with scrollspy. If h1 -> first level title. If h2 -> second level title.
		// Scrollspy uses only titles to track scrolling. div.help-text elements are not significant for the scrollspy.
		new UserGuide("#module-help-tree", "#sidebar")
	</r:script>

	<!-- TODO -->
	<link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.2.0/styles/default.min.css">
	<script src="//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.2.0/highlight.min.js"></script>
	<script>hljs.initHighlightingOnLoad();</script>
</head>
<body class="user-guide">

<ui:flashMessage/>

<ui:breadcrumb>
	<g:render template="/help/breadcrumb"/>
</ui:breadcrumb>

<div class="row">
	<div class="col-sm-12">
		<div class="scrollspy-wrapper col-md-9" id="module-help-tree">

			<h1>Introduction</h1>

			<h2>What is Streamr?</h2>

			<p>Alat kirjoittaa diiba daoijsoidöaosdj öoaijsdöoias iodasioj dölaksj ölkdasölk dasölkmd ölaksm ölksmd ölkasmö lkdmasölk mdasölkm ödlaksm öldkasm ölkdsm ölkasmd ölaksmdölkasm öldkmas ölkmdasölkm döalskm öalskmd ölkasmd ölaksmdöl ksamö lkdmasölk masölkdm asölkmdöalskmdaölskmöldkmasölkdm asölkmdlöksam öldm asöklmd öalskmd löaksmdlöksam ökldmsa ölkdm asölkdm asölkmd öalskmd ölaskmd öklasm öklaslkö</p>

			<p>This is an inline code block: <code>curl https://www.streamr.com</code></p>

			<pre>
<code>Here is a multiline code block
Hello
Yes</code></pre>

			<ul>
				<li>Ut vitae diam eu lacus mollis pulvinar eu non felis.</li>
				<li>Praesent commodo nibh at laoreet rhoncus.</li>
				<li>Curabitur auctor sapien quis lacus pharetra ultricies a et erat.</li>
			</ul>

			<h2>Use Cases</h2>

			<h1>Getting Started</h1>
			<h2>Getting Help</h2>
			<h2>Building your first Canvas</h2>

			<h1>Defining Streams</h1>
			<h2>API Streams</h2>
			<h2>Database (MongoDb) poller streams</h2>

			<p>Aökajsdöaskdaskdj aöjdöklasjökld askld klasm lkdaslkm dmsakö kladsdlkj naslkjdasjk dkjasn dkljasnlkjd naskjd nlkjasnd kjlasnkj ansdkljna slkjnd lkjasn</p>

			<h2>Importing CSV Files</h2>

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
				Once in a while, you may find yourself in a situation where you'd like a custom module.
				Perhaps you need some functionality that cannot be implemented with existing modules. Or perhaps
				implementing said functionality with existing modules is possible, but cumbersome. Maybe your bottleneck
				is performance -- perhaps you want to do some hefty calculations as fast as possible or you want to
				minimize memory use.
			</p>

			<p>
				Whatever the reason, Streamr platform can be extended by writing your own specialized custom modules. In
				this Chapter, we will go through the different ways of doing so.
			</p>

			<h2>JavaModule</h2>
			<p>
				<code>JavaModule</code> supports writing custom behavior in the Java programming language. Simply add the module
				to a Canvas and press the "Edit code"-button to open a code editor. The default template code will look
				as follows.
			</p>

			<pre><code class="java">
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
				</code></pre>

			<p>
				You will need to fill in this template with appropriate components for the module to work.
			</p>

			<p>
				A module in Streamr consists of <em>inputs</em>, <em>parameters</em>, <em>outputs</em>, possibly
				<em>state</em>, and <em>methods</em> that need to be overriden for the magic to happen.
			</p>

			<h3>Tutorial: Writing Your First Custom Module</h3>
			<p>
				...
			</p>

			<h3>Inputs</h3>

			<p>
				<em>Inputs</em>, visually speaking, are the colored circles and labels found on the left-side of a module on a Canvas. For
				example, the module <code>Multiply</code> requires inputs <code>A</code> and <code>B</code> to
				calculate the result of multiplication. The inputs of a module must be <em>connected</em> to compatible
				outputs for data to arrive and for it to be processed.
			</p>

			<p>TODO: image of Multiplty module</p>

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
				connected, the module should be written in such a way as to be able to respond to changing parameter
				values.
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
				Outputs reside at the right-hand side of a module and are responsible for sending out computed
				values after a module has been activated. E.g., the <code>Multiply</code> module, on activation,
				multiplies the values of its two inputs and then sends the product to an output named <code>A*B</code>.
				The product will then be passed to 0..n inputs connected to the output.
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
				The state of a module can be kept in its fields (aka instance variables or member variables). For example,
				the <code>Count</code> module keeps track of the number of received values by having an integer counter
				as field (<code>private int counter = 0;</code>) Everytime a new value is received, it increments the
				counter by one and spits the new counter value through its output.
			</p>

			<p>
				There is one caveat concerning fields. You should make sure that they are serializable, i.e., their type
				implements <code>java.io.Serializable</code>. If this is not the case, you can declare the field
				transient by adding keyword <code>transient</code> to its declaration. However, you need take special
				precautions when working with transient fields in modules. Specifically, the value of a transient field
				may suddenly become <code>null</code> at the beginning of <code>sendOutput()</code>.
			</p>

			<h3>Methods</h3>

			<h1>Using Streams Outside Streamr</h1>
		</div>


		<!-- Don't remove this div -->
		<div class="col-xs-0 col-sm-0 col-md-3" id="sidebar"></div>
	</div>
</div>
</body>
</html>
