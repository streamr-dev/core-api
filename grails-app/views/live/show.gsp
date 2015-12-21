
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
			resendOptions: { resend_all:false, resend_last: 0 },
			errorHandler: function(msg) {
				console.error(msg)
			}
		});
		
		SignalPath.loadSignalPath({
			url: '${createLink(controller: "liveApi", action:"show", id:rsp.id)}'
		}, function(saveData, signalPathData, signalPathContext, runData) {
		
			<g:if test="${rsp.state=='running'}">
				SignalPath.subscribe(runData, true);
			</g:if>
			
			<g:if test="${params.embedded}">
			// Terrible workaround for jsPlumb rendering problems within iframe on IE
			var count = 0
			var interval = setInterval(function() {
				SignalPath.jsPlumb.repaintEverything()
				if (count++ === 10)
					clearInterval(interval)
			}, 2000)
			</g:if>
		});
	});
	$(document).unload(function () {
		SignalPath.unload();
	});
</r:script>

</head>

<body class="live-show-view full-width-height">
	<!-- <div class="table"> -->
		<ui:breadcrumb>
			<g:render template="/live/breadcrumbList" model="[runningSignalPath:rsp]"/>
			<g:render template="/live/breadcrumbShow" model="[runningSignalPath:rsp, active:true]"/>
		</ui:breadcrumb>
		<ui:flashMessage/>
		<form method="post" role="form" id="toolbarForm">
			<g:hiddenField name="id" value="${rsp.id}" />

			<div id="toolbar" class="toolbar text-left">
				<!-- <div class="btn-group"> -->
					<g:if test="${rsp.state=="running"}">
						<button id="stopButton" class="btn btn-default confirm" data-action="${createLink(action:'stop')}" data-confirm="<g:message code="runningSignalPath.stop.confirm" args="[rsp.name]"></g:message>">
							<i class="fa fa-pause"></i>
							${message(code: 'runningSignalPath.stop.label')}
						</button>        	
					</g:if>
					<g:elseif test="${rsp.state=="stopped"}">

						<div id="run-group" class="btn-group">
							<button id="startButton" class="btn btn-primary" data-action="${createLink(action:'start')}">
								<i class="fa fa-play"></i>
								${message(code: 'runningSignalPath.start.label')}
							</button>

							<button id="runDropdown" type="button" class="btn btn-primary dropdown-toggle"
									data-toggle="dropdown">
								<span class="caret"></span>
								<span class="sr-only">Toggle Dropdown</span>
							</button>

							<ul id="liveCanvasStartDropdownMenu" class="dropdown-menu" role="menu">
								<li>
									<a id="clearAndStartButton" class="confirm" href="#"
									   data-action="${createLink(action:'start', params:[clear: true])}"
									   data-confirm="<g:message code="runningSignalPath.clearAndStart.confirm" args="[rsp.name]"></g:message>">
										${message(code: 'runningSignalPath.clearAndStart.label')}
									</a>
								</li>
							</ul>
						</div>

						<button id="deleteButton" class="btn btn-default confirm" data-action="${createLink(action:'delete')}" data-confirm="<g:message code="runningSignalPath.delete.confirm" args="[rsp.name]"></g:message>">
							<i class="fa fa-trash-o"></i>
							${message(code: 'runningSignalPath.delete.label')}
						</button>
					</g:elseif>
				<!-- </div> -->
			</div>
		</form>
		<!-- <div class="full"> -->
			<div id="canvas" class="embeddable"></div>
		<!-- </div> -->
	<!-- </div> -->
</body>
</html>
