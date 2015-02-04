<html>
<head>
    <meta name="layout" content="main" />
    <title><g:message code="dashboard.show.label" args="[dashboard.name]"/></title>

	<r:require module="webcomponents"/>
	<r:require module="toolbar"/>

	<r:script>
		 $(document).ready(function() {
		 	new Toolbar($("#toolbar"))
		 })
	</r:script>

	<link rel="import" href="${resource(dir:"webcomponents/components", file:"streamr-imports.html", plugin:"unifina-core")}"/>
</head>

<body class="dashboard">
	<streamr-client server="${serverUrl}"></streamr-client>
	
	<form method="post" role="form" id="toolbarForm">
		<g:hiddenField name="id" value="${dashboard.id}" />

		<div id="toolbar" class="btn-group toolbar">
			<button id="editButton" class="btn btn-default" data-action="${createLink(action:'edit')}">
				<i class="fa fa-edit"></i>
				${message(code: 'default.button.edit.label', default: 'Edit')}
			</button>

			<button id="deleteButton" class="btn btn-default confirm" data-action="${createLink(action:'delete')}" data-confirm="<g:message code="dashboard.delete.confirm" args="[dashboard.name]"></g:message>">
				<i class="fa fa-trash-o"></i>
				${message(code: 'default.button.delete.label', default: 'Delete')}
			</button>        	
		</div>
	</form>
	
	<div class="row">
		<g:each in="${dashboard.items}" var="item">
			<g:render template="/dashboard/streamr-label" model="[title:"${item.title}", channel:"${item.uiChannel.id}"]"></g:render>
		</g:each>
    </div>
</body>
</html>
