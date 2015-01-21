<html>
<head>
    <meta name="layout" content="main" />
    <title><g:message code="dashboard.show.label"/></title>

	<r:require module="webcomponents"/>

	<link rel="import" href="${resource(dir:"webcomponents/components", file:"streamr-imports.html", plugin:"unifina-core")}"/>
</head>

<body class="dashboard">
	<streamr-client server="${serverUrl}"></streamr-client>
	
	<div class="row">
		<g:each in="${dashboard.items}" var="item">
			<g:render template="/dashboard/streamr-label" model="[title:"${item.title}", channel:"${item.uiChannel.id}"]"></g:render>
		</g:each>
    </div>
</body>
</html>
