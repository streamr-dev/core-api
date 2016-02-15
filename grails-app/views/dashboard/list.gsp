<html>
<head>
    <meta name="layout" content="main" />
    <title><g:message code="dashboard.list.label"/></title>
	<r:require module="sharing-dialog"/>
</head>

<body class="dashboard">

	<ui:flashMessage/>

	<div class="btn-group toolbar">
		<a id="createButton" class="btn btn-primary" href="${createLink(action:'create')}">
			<i class="fa fa-plus"></i> Create a new dashboard
		</a>        	
	</div>
	
	<div class="panel">
            <div class="panel-heading">
            	<span class="panel-title">
            		<g:message code="dashboard.list.label" />
            	</span>
            </div>
            
            <div class="panel-body">
				<ui:table>
				    <ui:thead>
				        <ui:tr>
				            <ui:th>Name</ui:th>
				            <ui:th>Created</ui:th>
				            <ui:th>Modified</ui:th>
				        </ui:tr>
				    </ui:thead>
				    <ui:tbody>
					    <g:each in="${dashboards}" status="i" var="dashboard">
					    	<ui:tr title="Show or edit dashboard" link="${createLink(action: 'show', id:dashboard.id) }" data-id="${dashboard.id}">
					            <ui:td>${dashboard.name}</ui:td>					        
					           	<ui:td>${dashboard.dateCreated.format("yyyy-MM-dd")}</ui:td>
					            <ui:td>
									${dashboard.lastUpdated.format("yyyy-MM-dd")}
									<%-- uncomment when /v1/api/dashboards is available
									<g:if test="${shareable.contains(dashboard)}">
										<button class="btn share-button-20px" onclick="sharePopup('${createLink(uri: "/api/v1/dashboards/" + dashboard.id)}', 'Dashboard ${dashboard.name}')"></button>
									</g:if>
									--%>
								</ui:td>
				            </ui:tr>	            	
						</g:each>
					</ui:tbody>
				</ui:table>
            </div> <%-- end panel body --%>
        </div> <%-- end panel --%>
</body>
</html>
