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

			<h1>Introduction</h1>

			<h2>What is Streamr?</h2>

			<p>
				Streamr is a cloud-based platform for automatically reacting to events in real-time data streams.  It
				abstracts the complexity of the underlying stream processing technology and provides a user-friendly
				front end, saving you time and money in the process.
			</p>
			<p>
				There’s many things you can do with Streamr. For instance, you’ll be able to monitor real-time
				industrial processes, visualise sensory data from IoT devices, calculate complex streaming analytics,
				take automatic action when abnormal patterns are detected, extract real-time social media analytics,
				track customer payments, orders, or deliveries, and automate business processes. If you have a source
				of real-time data and you want to do useful things with the data, Streamr is for you.
			</p>
			<p>
				Streamr makes building real-time automation easy. You can automate complicated sequences of events or
				calculate streaming analytics. There is a browser-based interface where you build up a digital canvas
				by visual programming. You can also code processing modules in Java if that's your preference. Or you
				can mix and match visual programming elements with Java modules.
			</p>
			<p>
				The platform is flexible and fast. Data streams can contain any kind of data in any format, and the
				technology can handle millions of events per second. The underlying software runs in the cloud and
				additional computing capacity is deployed automatically as required.
			</p>
			<p>
				There is also a playback mode where you can simulate your processing logic over days, weeks, months, or
				years of data over billions of events. When you’re happy, you can go live at a click of a button. In
				live mode, Streamr listens to real-time data arriving in your data streams, processes the events as
				instructed, and takes the specified actions.
			</p>
			<p>
				The secret sauce of Streamr is the hidden technology which makes real-time automation easy. Apart from
				the browser-based user interface and visual programming tools, Streamr takes care of the data flow,
				event propagation, and message queues. It also manages the cloud computing services and stores the event
				data on your behalf.
			</p>

			<h3>Data Live in Streams</h3>
			<p>
				All real-time data in Streamr is stored in a stream (TODO: ADD A LINK TO “What is a stream?”). A stream
				is essentially a timestamped sequence of events. Each event may contain more than one field. You can
				create and work on dozens, hundreds, or thousands of streams. A stream is persistent, identified by a
				unique ID, and stored in the cloud.
			</p>
			<p>
				Any kind of real-time data can be stored in a stream. The incoming data could consist of sensory
				readings of speed, geolocation, orientation, ambient temperature, humidity, and so forth. Or the data
				might consist of social media messages, stock market events, mobile ad impressions, and so on.
			</p>
			<p>
				There is no limit on the kind, format, or quantity of the data you can feed into Streamr. The data may
				originate from machines in a factory, from devices you sell to your clients, or from commercial
				streaming data feeds.
			</p>
			<p>
				There are a few different ways to get data into a stream. Streamr has a simple HTTP API which allows you
				to push events in JSON format to a stream automatically from any programming language. You can also
				batch load historical events using Streamr’s CSV loader. If the events originate in a database or in a
				commercial data feed, talk to us. We'll figure out what kind of adapter is needed.
			</p>
			<p>
				Streams implement a publish-subscribe paradigm, or pub/sub for short. A stream can receive data from
				many sources (or publishers), and there can be several listeners who subscribe to data from a stream.
				There are several variations on the possible pub/sub topologies, such as many-to-one, one-to-many, or
				many-to-many.  Streamr supports all of these.
			</p>

			<h3>Turn data into action</h3>
			<p>
				You can build many kinds of automatic functionality on the Streamr platform. You can refine the incoming
				real-time data by calculating streaming analytics. You can visualise the data or refined versions of the
				data as the events flow by. You can communicate with the outside world by sending messages or alerts.
				You can save the results of real-time computations in new streams. Or you can control external
				applications and devices. And getting data out a stream is easy, too. If you subscribe to a stream in
				external web pages and applications, you’ll receive new events or instructions in real-time as soon as
				they appear in a stream.
			</p>
			<p>
				To help you do all of the above, there is a built-in library which contains an extensive range of
				modules. All of these modules do useful things on their own, but the real power of Streamr comes for
				combining simple operations in a sequence as the data flows through the event processing canvas.
			</p>
			<p>
				Here's a few examples of specific things you can do.
			</p>
			<ul>
				<li>
					Refine the data by passing it through different operations. The built-in module library includes
					arithmetic and logical operations. There are also functions for smoothing, sampling, and aggregating
					streaming data.
				</li>
				<li>
					Chain modules together so that the output from one operation flows as an input to another. This is
					one way to build arbitrarily complex streaming analytics (and don't worry, you can encapsulate the
					complexity and reuse the abstracted result).
				</li>
				<li>
					Visualise the data by directing it to a charting module on a canvas. The chart shows the new data in
					real-time as new events arrive or streaming analytics are computed.
				</li>
				<li>
					Embed visualisation modules in external web pages by inserting a single line of HTML code. Such
					embedded visualisations will show live data as soon as the underlying canvas remains in live mode.
				</li>
				<li>
					Communicate with the outside world by sending text messages or emails when specific conditions are
					satisfied. You can embed real-time data, refined data, or natural language in the messages.
				</li>
				<li>
					Save refined data in another stream. When you do that, the saved data are instantly accessible as
					new events. The refined data can also be streamed from the platform to external data consumers.
				</li>
				<li>
					Control external devices. As an example, you could override manual controls and make a remotely
					operated drone return to base when it's about to go out of range or running low on battery. Because
					the control interface is likely to be machine specific, this is one case where you'd be looking at
					coding a custom module in Java.
				</li>
				<li>
					Receive real-time events in external applications. You can subscribe to a stream in web pages and
					applications, and you’ll receive every event as soon as it’s available. It’s up to you what you do
					with the event; the possibilities are endless.
				</li>
			</ul>

			<h3>Who is Streamr for?</h3>
			<p>
				The short answer is that Streamr is for anyone who wants to create new, interesting, and valuable
				things on top of real-time data, and wants to do it quickly and with minimum fuss.
			</p>
			<p>
				Streamr offers value to many different user groups. Domain experts and R&D teams can use Streamr for
				quick prototyping and exploration before building production level offerings. Streamr gives students,
				hobbyists, and enthusiasts a fully functional but affordable event processing platform in the cloud.
			</p>
			<p>
				For corporations and organisations, Streamr is a tool for sharing and teamwork. The real-time data that
				you collect may be a treasure trove for innovation, but nothing will happen unless the data is available
				and usable. Streamr can be the spark which makes innovation happen for you: It brings the data into the
				open, and makes it easy to experiment with ideas for new products and services.
			</p>


			<h1>Use Cases</h1>

			<h1>Getting Started</h1>
			<h2>Getting Help</h2>
			<h2>What is a Stream?</h2>
			<p>
				All data in Streamr is stored in a stream. A stream is simply a sequence of events in time. You can add
				new data to the end of a stream, and you can get data out from a stream in the correct order. Each
				stream is identified by a unique ID. There’s no technical limit on the overall number of streams.
			</p>
			<p>
				You can store different kinds of data in the same stream. The data may be numeric, but they can equally
				well consist of strings, collections of elementary data types, or associative arrays. Each event
				contains at least one data field, but you can have as many fields per event as required. The data are
				persistent and stored in the cloud.
			</p>
			<p>
				The platform includes a number of tools for working with streams(TODO: ADD A LINK TO “Working with streams”).
				You can manage streams, upload batches of historical data, add real-time data, and subscribe to a
				stream within the platform or from external applications.
			</p>

			<h3>Examples of Streams</h3>
			<p>
				Here’s an example of what a small part of a stream could look like. Each row shows one event, and the
				columns correspond to the timestamp followed by two data fields, a measurement of the operating
				temperature and the number of rotations per minute (RPM).
			</p>
			<pre>
<strong>Timestamp</strong>                   <strong>Temperature</strong>   <strong>RPM</strong>
...
2016-02-01 11:30:01.012     312.56        3550
2016-02-01 11:30:02.239     312.49        3549
2016-02-01 11:30:04.105	    312.42        3543
2016-02-01 11:30:08.122     313.21        3565
2016-02-01 11:30:11.882     317.45        3602</pre>

			<p>
				As an example of a more complicated event, here’s a data point in a stock market stream.
			</p>

			<pre>
{
	"Symbol": "PFFT",
	"EventType": 1,
	"OrderId": 6454321,
	"Direction": "Up",
	"Trade": {"Price": 118.55, "Size": 100},
	"Ask": [
		{"Price": 118.6, "Size": 22500},
		{"Price": 118.65, "Size": 18000},
		{"Price": 118.7, "Size": 13000},
		{"Price": 118.8, "Size": 8000},
		{"Price": 119, "Size": 45000}
	],
	"Bid": [
		{"Price": 118.5, "Size": 16500},
		{"Price": 118.45, "Size": 11000},
		{"Price": 118.4, "Size": 14200},
		{"Price": 118.2, "Size": 19000},
		{"Price": 118, "Size": 50000}
	]
}</pre>

			<h3>Built-In Data Types</h3>
			<p>
				There’s a number of specific built-in data types which can be used in a standard stream. They are the
				following:
			</p>
			<ul>
				<li>
					<strong>Number</strong> is a numeric data type internally stored as a double precision (64-bit)
					float.
				</li>
				<li>
					<strong>Boolean</strong> is a logical data type with two possible values, True and False. In Streamr,
					a numeric value exactly equal to one represents logical truth. Anything else is interpreted as a
					logical falsehood.
				</li>
				<li>
					<strong>String</strong> is a sequence of zero or more alphabetical characters.
				</li>
				<li>
					<strong>Map</strong> is a collection of key-value pairs. Each key is a string, and the value can
					be of any built-in data type (even a Map). Map is the same as a dictionary or an associative array
					found in a number of programming languages.
				</li>
				<li>
					<strong>List</strong> is an ordered collection of zero or more elements.
				</li>
			</ul>

			<p>
				Data types can be freely mixed in one event. And you can freely add new fields to an existing stream;
				you don’t have to know what fields you might eventually need. A single event can be of any size within
				reason, and a stream can grow indefinitely when extended by new events.
			</p>
			<p>
				Whilst the standard streams only support specific data types, there is no theoretical limitation as to
				the format or type of data in a stream. Anything which can be expressed in digital form is fair game.
				It is perfectly possible to create streams which contain digital images, streaming video, or other
				domain-specific data. If your use case takes you beyond the built-in data types, come and talk to us on
				what you have in mind.
			</p>

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
