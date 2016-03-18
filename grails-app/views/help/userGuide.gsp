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
				this Chapter, we will go through the technology alternatives for doing so.
			</p>

			<h2>JavaModule</h2>
			<p>
				<code>JavaModule</code> supports programming custom behavior in Java. Simply add the module
				to a Canvas and press the "Edit code"-button to open a code editor. The default template code will look
				as follows.
			</p>

			<pre><code>
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
				TODO: some väliteksti
			</p>

			<p>
				A module in Streamr consists of <em>inputs</em>, <em>outputs</em>, possibly
				<em>state</em>, and <em>methods</em> that need to be overriden for the magic to happen.
			</p>

			<h3>Inputs</h3>

			<p>
				<em>Inputs</em>, visually speaking, are the colored circles found on the left-side of a module on a canvas. For
				example, the module <code>Multiply</code> requires inputs <code>A</code> and <code>B</code> to
				calculate the result of multiplication. The inputs of a model must be <em>connected</em> to compatible
				outputs for data to arrive and for it to be processed.
			</p>

			<span>TODO: image of Multiplty module</span>

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

			<h3>Tutorial: Writing Your First Custom Module</h3>
			<p>
				...
			</p>

			<h1>Using Streams Outside Streamr</h1>
		</div>


		<!-- Don't remove this div -->
		<div class="col-xs-0 col-sm-0 col-md-3" id="sidebar"></div>
	</div>
</div>
</body>
</html>
