<html>
<head>
    <meta name="layout" content="main" />
    <title><g:message code="dashboard.show.label"/></title>

</head>

<body class="dashboard">

	<ui:flashMessage/>

	<div class="btn-group toolbar">
		<a id="createButton" class="btn btn-primary" href="${createLink(action:'create')}">
			<i class="fa fa-plus"></i> Create new dashboard
		</a>        	
	</div>
	
	<div class="panel">
            <div class="panel-heading">
            	<span class="panel-title">
            		<g:message code="dashboard.list.label" />
            	</span>
            </div>
            
            <div class="panel-body">
            
				<table class="table table-striped table-bordered table-hover table-condensed table-responsive">
				    <thead>
				        <tr>
				            <g:sortableColumn property="id" title="Id" />
				            <th>Name</th>
				            <th>Created</th>
				            <th>Modified</th>
				        </tr>
				    </thead>
				    <tbody>
					    <g:each in="${dashboards}" status="i" var="dashboard">
					        <tr>
					            <td><g:link action="show" id="${dashboard.id}">${fieldValue(bean: dashboard, field: "id")}</g:link></td>
					            <td>${dashboard.name}</td>					        
					           	<td>${dashboard.dateCreated.format("yyyy-MM-dd")}</td>
					            <td>${dashboard.lastUpdated.format("yyyy-MM-dd")}</td>		            	
							</tr>
						</g:each>
					</tbody>
				</table>
            </div> <%-- end panel body --%>
        </div> <%-- end panel --%>
</body>
</html>
