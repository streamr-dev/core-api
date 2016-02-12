<html>
<head>
	<meta name="layout" content="main" />
	<title><g:message code="stream.configure.label" args="[stream.name]"/></title>

	<r:require module="stream-fields"/>

	<r:script>
        	var listView
        
        	$(document).ready(function() {
        		var autodetecting = false
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

        				
						Object.keys(message).forEach(function(key) {
							var type = message[key]
							
							console.log("Detected field: "+key+", type: "+type)
							listView.collection.add({
								name: key, 
								type: type
							})
						})
						
						autodetecting = false
						$("#autodetect").removeAttr("disabled").html("Autodetect")
	        		}
        		}
        	
        		$("#autodetect").click(function() {
        			if (!autodetecting) {
        				autodetecting = true
        				$(this).attr("disabled","disabled")
						$(this).html("Waiting for data...")

						$.get('${createLink(controller: "stream", action: "detectFields", params: [id: stream.id])}', function(data) {
							handleMessage(data)
						})
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

<div class="col-sm-8 col-sm-offset-2">
	<ui:panel title="${message(code:"stream.configure.label", args:[stream.name])}">
		<button class="btn btn-lg" id="autodetect">Autodetect</button>

		<div id="stream-fields"></div>
	</ui:panel>
</div>
</body>
</html>
