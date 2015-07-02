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
    		<div class="col-sm-12 col-md-8 col-md-offset-2">
		        <div class="scrollspy-wrapper col-md-9" id="module-help-tree">
		        	<h1>Introduction</h1>
			     		<h2>What is Streamr?</h2>
			     		<div class="help-text">
			     			Lorem ipsum dolor sit amet, consectetur adipiscing elit. 
			     			Vestibulum accumsan odio mauris, nec luctus lorem vehicula eget. Suspendisse potenti. Aliquam erat volutpat. Nulla facilisi. Aliquam posuere ante leo, a condimentum justo suscipit nec. 
			     			Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus.
			     			Lorem ipsum dolor sit amet, consectetur adipiscing elit.
			     			<ul>
								<li>Ut vitae diam eu lacus mollis pulvinar eu non felis.</li>
								<li>Praesent commodo nibh at laoreet rhoncus.</li>
								<li>Curabitur auctor sapien quis lacus pharetra ultricies a et erat.</li>
							</ul>
			     			Proin placerat, dolor sed fringilla tempor, dolor lorem ullamcorper purus, vel sagittis tellus tortor et ante. 
			     			Fusce imperdiet mi nec rutrum sodales. Morbi venenatis ipsum sed sapien bibendum aliquam. 
			     			Nunc vel ligula fermentum, dapibus felis a, dignissim neque. Donec in metus eu mi aliquet consequat. 
			     			Maecenas tincidunt congue scelerisque. Suspendisse potenti.
			     		</div>
			     		<h2>Use Cases</h2>
			     		<div class="help-text">
			     			Lorem ipsum dolor sit amet, consectetur adipiscing elit. 
			     			Vestibulum accumsan odio mauris, nec luctus lorem vehicula eget. Suspendisse potenti. Aliquam erat volutpat. Nulla facilisi. Aliquam posuere ante leo, a condimentum justo suscipit nec. 
			     			Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus.
			     			Lorem ipsum dolor sit amet, consectetur adipiscing elit.
			     			<ul>
								<li>Ut vitae diam eu lacus mollis pulvinar eu non felis.</li>
								<li>Praesent commodo nibh at laoreet rhoncus.</li>
								<li>Curabitur auctor sapien quis lacus pharetra ultricies a et erat.</li>
							</ul>
			     			Proin placerat, dolor sed fringilla tempor, dolor lorem ullamcorper purus, vel sagittis tellus tortor et ante. 
			     			Fusce imperdiet mi nec rutrum sodales. Morbi venenatis ipsum sed sapien bibendum aliquam. 
			     			Nunc vel ligula fermentum, dapibus felis a, dignissim neque. Donec in metus eu mi aliquet consequat. 
			     			Maecenas tincidunt congue scelerisque. Suspendisse potenti.
			     		</div>
		     		<h1>Getting Started</h1>
		     		<div class="help-text">
			     			Lorem ipsum dolor sit amet, consectetur adipiscing elit. 
			     			Vestibulum accumsan odio mauris, nec luctus lorem vehicula eget. Suspendisse potenti. Aliquam erat volutpat. Nulla facilisi. Aliquam posuere ante leo, a condimentum justo suscipit nec. 
			     			Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus.
			     			Lorem ipsum dolor sit amet, consectetur adipiscing elit.
			     			<ul>
								<li>Ut vitae diam eu lacus mollis pulvinar eu non felis.</li>
								<li>Praesent commodo nibh at laoreet rhoncus.</li>
								<li>Curabitur auctor sapien quis lacus pharetra ultricies a et erat.</li>
							</ul>
			     			Proin placerat, dolor sed fringilla tempor, dolor lorem ullamcorper purus, vel sagittis tellus tortor et ante. 
			     			Fusce imperdiet mi nec rutrum sodales. Morbi venenatis ipsum sed sapien bibendum aliquam. 
			     			Nunc vel ligula fermentum, dapibus felis a, dignissim neque. Donec in metus eu mi aliquet consequat. 
			     			Maecenas tincidunt congue scelerisque. Suspendisse potenti.
			     		</div>
		     		<h1>Defining Streams</h1>
			     		<h2>API Streams</h2>
			     		<h2>Importing CSV Files</h2>
			     		<h2>Twitter</h2>
			     		<h2>Pubnub</h2>
			     		<h2>Zapier</h2>
			     		<h2>Jitterbit</h2>
			     		<h2>Snaplogic</h2>
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
		        <div class="col-xs-0 col-sm-0 col-md-3" id="sidebar">
		        	
		        </div>
        	</div>
        </div>
    </body>
</html>
