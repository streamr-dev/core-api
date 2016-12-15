<html>
<head>
    <meta name="layout" content="main" />

    <title>Canvas</title>

    <r:require module='jquery-ui'/>
    <r:require module="bootbox"/>
    <r:require module="bootstrap-contextmenu"/>
    <r:require module="bootstrap-datepicker"/>
	<r:require module="streamr-search"/>
    <r:require module="signalpath-theme"/>
    <r:require module="touchpunch"/>

    <r:script>

$(document).ready(function() {

	SignalPath.init({
		parentElement: $('#canvas'),
		settings: function() { return {}; },
		errorHandler: function(data) {
			Streamr.showError(data.msg)
		},
		notificationHandler: function(data) {
			Streamr.showInfo(data.msg)
		},
		connectionOptions: {
			server: "${grailsApplication.config.streamr.ui.server}",
			autoConnect: true,
			autoDisconnect: true
		}
	});

    <g:if test="${id}">
            SignalPath.load('${id}');
        </g:if>    $(document).unload(function () {
        SignalPath.unload()
    });
})
    </r:script>
</head>

<body class="canvas-editor-page main-menu-fixed embedded">

<div id="canvas" class="scrollable embeddable"></div>

</body>
</html>

