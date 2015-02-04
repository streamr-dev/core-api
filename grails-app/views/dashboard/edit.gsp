<html>
<head>
    <meta name="layout" content="main" />
    <title><g:message code="dashboard.edit.label"/></title>
</head>

<body class="edit-dashboard">
	<g:form action="update">
		<g:hiddenField name="id" value="${dashboard.id}"/>
		<div class="col-xs-12 col-md-8 col-md-offset-2">
			<ui:panel title="${message(code:"dashboard.edit.label") }">
				<g:render template="form" />
				<g:submitButton class="btn btn-primary" name="submit" value="${message(code:"dashboard.update.button")}"/>
			</ui:panel>
		</div>
	</g:form>
</body>
</html>
