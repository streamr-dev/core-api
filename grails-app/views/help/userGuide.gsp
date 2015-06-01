<html>
    <head>
        <meta name="layout" content="main" />
        <title>User guide</title>
        
        <r:script>
        	var offset = 70
       		$('body').scrollspy({
       			offset: offset
       		})
       		$('.streamr-sidebar li a').click(function(event) {
       			event.preventDefault()
			    $($(this).attr('href'))[0].scrollIntoView()
			    scrollBy(0, -(offset-30))
			    this.blur()
			});
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
		     		<ui:scrollSpyPanel title="What is Streamr">
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
		     		</ui:scrollSpyPanel>
		     		<ui:scrollSpyPanel title="Use Cases">
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
		     		</ui:scrollSpyPanel>
		     		<ui:scrollSpyPanel title="Getting Started">
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
		     		</ui:scrollSpyPanel>
		     		<ui:scrollSpyPanel title="Defining Streams">
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
		     		</ui:scrollSpyPanel>
		     		<ui:scrollSpyPanel title="API Streams">
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
		     		</ui:scrollSpyPanel>
		     		<ui:scrollSpyPanel title="Importing CSV files">
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
		     		</ui:scrollSpyPanel>
		     		<ui:scrollSpyPanel title="Twitter">
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
		     		</ui:scrollSpyPanel>
		        </div>
		        <div class="col-xs-0 col-sm-0 col-md-3" id="sidebar">
		        	<ui:sidebarNav titles="['What is Streamr', 'Use Cases', 'Getting Started', 'Defining Streams', 'API Streams', 'Importing CSV Files', 'Twitter']" />
		        </div>
        	</div>
        </div>
    </body>
</html>
