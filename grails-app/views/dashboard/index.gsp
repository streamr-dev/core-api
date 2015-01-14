<html>
<head>
    <meta name="layout" content="main" />
    <title><g:message code="dashboard.show.label"/></title>

	<r:require module="webcomponents"/>

	<link rel="import" href="${resource(dir:"webcomponents/components", file:"streamr-imports.html", plugin:"unifina-core")}"/>
</head>
<body class="dashboard">
<%--	<streamr-client server="${serverUrl}"></streamr-client>--%>
	<streamr-client server="192.168.10.123:8090"></streamr-client>
	
	<div class="row">
		<g:render template="/dashboard/streamr-label" model="[title:'Apple', channel:'db402d42-a21a-4402-b95d-a0a4f507a641']"></g:render>
		<g:render template="/dashboard/streamr-label" model="[title:'Netflix', channel:'b7d7461a-1fcb-4357-b626-0347509ee3b1']"></g:render>
		<g:render template="/dashboard/streamr-label" model="[title:'Google', channel:'32ce9e52-5198-4060-9324-01ac00319446']"></g:render>  
    </div>
</body>
</html>
