<html>
    <head>
        <meta name="layout" content="main" />
        <title><g:message code="stream.configure.label" args="[stream.name]"/></title>
        
        <r:require module="streamr-client"/>
        <r:require module="stream-fields"/>
        
        <r:script>
        	var listView
        
        	$(document).ready(function() {
        		var autodetecting = false
        		var client = new StreamrClient({
					server: "${grailsApplication.config.streamr.ui.server}"
				})
        		var streamConfig = ${raw(stream.config ?: "{}")}
        		
        		listView = new ListView({
        			el: $("#stream-fields"),
        			id: ${stream.id}
        		});
        		listView.on('saved', function() {
        			window.location = '${createLink(action:"show", id:stream.id)}'
        		})
        		
        		if (streamConfig && streamConfig.fields) {
        			listView.collection.reset(streamConfig.fields)
        			listView.render()
        		}
        	
        		function handleMessage(message) {
        			if (autodetecting) {
        				listView.clear()
        				
        				// Delete metadata keys
        				delete message.counter
        				delete message.channel
        				
						Object.keys(message).forEach(function(key) {
							var type = (typeof message[key])
							if (type==="object") {
								type = (Array.isArray(message[key]) ? "list" : "map")
							}
							
							console.log("Detected field: "+key+", type: "+type)
							listView.collection.add({
								name: key, 
								type: type
							})
						})
						
						autodetecting = false
						client.disconnect()
						$("#autodetect").removeAttr("disabled").html("Autodetect")
	        		}
        		}
        	
        		$("#autodetect").click(function() {
        			if (!autodetecting) {
	        			autodetecting = true
						client.subscribe("${stream.uuid}", handleMessage, {resend_last:1})
						client.connect()
						
						$(this).attr("disabled","disabled")
						$(this).html("Waiting for data...")
					}
        		})
        		
        		
        	})
        </r:script>
    </head>
    <body>
    	<ui:breadcrumb>
			<g:render template="/stream/breadcrumbList" />
			<g:render template="/stream/breadcrumbShow" />
			<li class="active">
				<g:message code="stream.edit.label" args="[stream.name]"/>
			</li>
		</ui:breadcrumb>
    
		<ui:flashMessage/>

		<div class="col-sm-8">
			<ui:panel title="${message(code:"stream.configure.label", args:[stream.name])}">
				<button class="btn btn-lg" id="autodetect">Autodetect</button>
							
				<div id="stream-fields"></div>
			</ui:panel>
		</div>
		
		<div class="col-sm-4">
			<ui:panel title="HTTP API credentials">
				<g:render template="userStreamCredentials" model="[stream:stream]"/>
			</ui:panel>
		</div>
		
    </body>
</html>
