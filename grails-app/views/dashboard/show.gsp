<html>
<head>
    <meta name="layout" content="main" />
    <title>${dashboard.name}</title>

	<r:require module="webcomponents"/>
	<link rel="import" href="${createLink(uri:"/webcomponents/index.html", plugin:"unifina-core")}">
	
	<r:require module="toolbar"/>

	<r:script>
		 $(document).ready(function() {
		 	new Toolbar($("#toolbar"))
		 })
	</r:script>
</head>

<body class="dashboard">
	
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
	
	<g:render template="dashboard-content" />
</body>
</html>
