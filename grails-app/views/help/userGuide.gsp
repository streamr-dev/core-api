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
Yes</code>
			     		</pre>

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
		     		<h1>Using Streams Outside Streamr</h1>
	     		</div>


	     		<!-- Don't remove this div -->
		        <div class="col-xs-0 col-sm-0 col-md-3" id="sidebar"></div>
        	</div>
        </div>
    </body>
</html>
