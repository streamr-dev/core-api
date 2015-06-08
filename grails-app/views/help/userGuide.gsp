<html>
    <head>
        <meta name="layout" content="main" />
        <title>User guide</title>
        
        <r:script>
        	var offset = 80
       		$('body').scrollspy({
       			offset: offset
       		})
       		// Scrollspy's offset doesn't affect to the links so the offset correction must me written manually
       		$('.streamr-sidebar li a').click(function(event) {
       			event.preventDefault()
       			if($($(this).attr('href'))[0]){
			    	$($(this).attr('href'))[0].scrollIntoView()
			   		scrollBy(0, -(offset-30))
		   		}		
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
		        	<ui:scrollspyCategory title="Introduction" />
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
		     		<ui:scrollspyCategory title="Getting Started" />
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
		     		<ui:scrollspyCategory title="Defining Streams" />
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
		     		<ui:scrollspyCategory title="Building Logic" />
		     		<ui:scrollSpyPanel title="Canvas basics (adding, connecting, running)">
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
		     		<ui:scrollspyCategory title="Running Live" />
		     		<ui:scrollSpyPanel title="Running Live">
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
		     		<ui:scrollspyCategory title="Dashboards" />	
		     		<ui:scrollSpyPanel title="Dashboards">
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
		     		<ui:scrollspyCategory title="Embedding Widgets" />
		     		<ui:scrollSpyPanel title="Embedding Widgets">
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
		     		<ui:scrollspyCategory title="Custom Modules" />	
		     		<ui:scrollSpyPanel title="Custom Modules">
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
		     		<ui:scrollspyCategory title="Using Streams Outside Streamr" />
		     		<ui:scrollSpyPanel title="Using Streams Outside Streamr">
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
		        	<ui:sidebarNav>
		        		<ui:sidebarElement title="Introduction">
		        			<ui:sidebarElement title="What is Streamr?" />
		        			<ui:sidebarElement title="Use Cases" />
		        		</ui:sidebarElement>
		        		<ui:sidebarElement title="Getting Started" />
		        		<ui:sidebarElement title="Defining Streams">
		        			<ui:sidebarElement title="API Streams" />
		        			<ui:sidebarElement title="Importing CSV Files" />
		        			<ui:sidebarElement title="Twitter" />
		        			<ui:sidebarElement title="Pubnub" />
		        			<ui:sidebarElement title="Zapier" />
		        			<ui:sidebarElement title="Jitterbit" />
		        			<ui:sidebarElement title="Snaplogic" />
		        		</ui:sidebarElement>
		        		<ui:sidebarElement title="Building Logic">
		        			<ui:sidebarElement title="Canvas basics (adding, connecting, running)" />
		        			<ui:sidebarElement title="Visualizing data" />
		        			<ui:sidebarElement title="Modules" />
		        			<ui:sidebarElement title="Controlling Module Activation" />
		        			<ui:sidebarElement title="Reusing Functionality" />
		        			<ui:sidebarElement title="Best Practices" />
		        		</ui:sidebarElement>
		        		<ui:sidebarElement title="Running Live" />
		        		<ui:sidebarElement title="Dashboards" />
		        		<ui:sidebarElement title="Embedding Widgets" />
		        		<ui:sidebarElement title="Custom Modules" />
		        		<ui:sidebarElement title="Using Streams Outside Streamr" />
		        	</ui:sidebarNav>
		        </div>
        	</div>
        </div>
    </body>
</html>
