<html>
<head>
    <meta name="layout" content="main" />
    <title><g:message code="dashboard.list.label"/></title>

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
            
				<ui:clickableTable>
				    <div class="thead">
				        <a class="tr">
				            <span class="th">Name</span>
				            <span class="th">Created</span>
				            <span class="th">Modified</span>
				        </a>
				    </div>
				    <div class="tbody">
					    <g:each in="${dashboards}" status="i" var="dashboard">
					    	<ui:clickableRow title="Show or edit dashboard" link="${createLink(action: 'show', id:dashboard.id) }" id="${dashboard.id}">
					            <div class="td">${dashboard.name}</div>					        
					           	<div class="td">${dashboard.dateCreated.format("yyyy-MM-dd")}</div>
					            <div class="td">${dashboard.lastUpdated.format("yyyy-MM-dd")}</div>	
				            </ui:clickableRow>	            	
						</g:each>
					</div>			
				</ui:clickableTable>
            </div> <%-- end panel body --%>
        </div> <%-- end panel --%>
</body>
</html>
