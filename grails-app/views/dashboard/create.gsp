<html>
<head>
    <meta name="layout" content="main" />
    <title><g:message code="dashboard.create.label"/></title>
</head>

<body class="create-dashboard">
	
	<g:form action="save">
		<div class="col-xs-12 col-md-8 col-md-offset-2">
			<ui:panel title="${message(code:"dashboard.create.label") }">
				<g:render template="form" />
				<g:submitButton class="btn btn-primary" name="submit" value="${message(code:"dashboard.create.button")}"/>
			</ui:panel>
		</div>
	</g:form>
</body>
</html>