
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title><g:message code="runningSignalPath.show.label" args="[rsp.name]"/></title>

<r:require module="signalpath-theme"/>
<g:render template="/canvas/signalPathExtensions"/>

<r:script>
			$(document).ready(function() {
				SignalPath.init({
					canvas: "canvas",
					zoom: ${params.zoom ?: 1}
				});
				
				$.getJSON('${createLink(action:"getJson")}', {id: ${rsp.id}}, function(data) {
					if (!data.error) {
						SignalPath.loadJSON(data);
						SignalPath.subscribe(data.runData, true);
					}
					else alert(data.error);
				});
			});
			$(document).unload(function () {
				SignalPath.unload();
			});
</r:script>

</head>
<body class="full-width-height">	
	<div id="canvas"></div>
</body>
</html>
