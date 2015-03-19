
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title><g:message code="runningSignalPath.show.label" args="[rsp.name]"/></title>

<r:require module="signalpath-theme"/>
<g:render template="/canvas/signalPathExtensions"/>

<r:require module="toolbar"/>
<r:script>
	 $(document).ready(function() {
	 	new Toolbar($("#toolbar"))
	 })
</r:script>

<r:script>
	$(document).ready(function() {
		SignalPath.init({
			canvas: "canvas",
			zoom: ${params.zoom ?: 1},
			connectionOptions: {
				server: "${grailsApplication.config.streamr.ui.server}"
			},
			errorHandler: function(msg) {
				console.error(msg)
			}
		});
		
		SignalPath.loadSignalPath({
			url: '${createLink(action:"getJson", id:rsp.id)}'
		}, function(saveData, signalPathData, signalPathContext, runData) {
			SignalPath.subscribe(runData, true);
		});
	});
	$(document).unload(function () {
		SignalPath.unload();
	});
</r:script>

</head>

<body class="full-width-height">

	<form method="post" role="form" id="toolbarForm">
		<g:hiddenField name="id" value="${rsp.id}" />

		<div id="toolbar" class="btn-group toolbar">
			<g:if test="${rsp.state=="running"}">
				<button id="stopButton" class="btn btn-default confirm" data-action="${createLink(action:'stop')}" data-confirm="<g:message code="runningSignalPath.stop.confirm" args="[rsp.name]"></g:message>">
					<i class="fa fa-pause"></i>
					${message(code: 'runningSignalPath.stop.label')}
				</button>        	
			</g:if>
			<g:elseif test="${rsp.state=="stopped"}">
				<button id="startButton" class="btn btn-default" data-action="${createLink(action:'start')}">
					<i class="fa fa-play"></i>
					${message(code: 'runningSignalPath.start.label')}
				</button> 
			</g:elseif>
		</div>
	</form>

	<div id="canvas" class="embeddable"></div>

</body>
</html>
