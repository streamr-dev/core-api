<html>
<head>
    <meta name="layout" content="main" />
    <title><g:message code="dashboard.list.label"/></title>

	<r:require module="confirm-button"/>
	<r:script>
		$(document).ready(function() {
			$(".delete-button").each(function(i, el) {
				new ConfirmButton(el, {
					message: "${ message(code:"dashboard.delete.confirm") }"
				}, function(result) {
					if (result) {
						$.ajax("${ createLink(uri:"/api/v1/dashboards", absolute: true)}" + '/' + $(el).data('id'), {
							method: 'DELETE',
							success: function() {
								location.reload()
							},
							error: function(e, t, msg) {
								Streamr.showError("${ message(code:"dashboard.delete.error") }", "${ message(code:"dashboard.delete.error.title") }")
							}
						})
					}
				})
			})
		})
	</r:script>
</head>

<body class="dashboard">

	<ui:flashMessage/>

	<div class="btn-group toolbar">
		<a id="createButton" class="btn btn-primary" href="${createLink(action:'show')}">
			<i class="fa fa-plus"></i> Create Dashboard
		</a>        	
	</div>
	
	<div class="panel list-panel">
            <div class="panel-heading">
            	<span class="panel-title">
            		<g:message code="dashboard.list.label" />
            	</span>
            </div>
            
            <div class="panel-body">
				<ui:table>
				    <ui:thead>
				        <ui:tr>
				            <ui:th><g:message code="dashboard.name.label" /></ui:th>
				            <ui:th class="hidden-xs"><g:message code="dashboard.updated.label" /></ui:th>
				            <ui:th class="button-column"/>
				        </ui:tr>
				    </ui:thead>
				    <ui:tbody>
					    <g:each in="${dashboards}" status="i" var="dashboard">
					    	<ui:tr title="Show or edit dashboard" link="${createLink(action: 'show', id:dashboard.id) }" data-id="${dashboard.id}">
					            <ui:td>${dashboard.name}</ui:td>					        
					            <ui:td class="hidden-xs"><g:formatDate date="${dashboard.lastUpdated}" formatName="default.date.format" timeZone="${user.timezone}" /></ui:td>
								<ui:td class="button-column">
									<g:if test="${writable.contains(dashboard) || shareable.contains(dashboard)}">
										<div class="streamr-dropdown">
											<button class="dashboard-menu-toggle dropdown-toggle btn btn-sm" data-toggle="dropdown">
												<i class="navbar-icon fa fa-caret-down"></i>
											</button>
											<ul class="dropdown-menu pull-right">
												<g:if test="${shareable.contains(dashboard)}">
													<li>
														<ui:shareButton type="span" url="${createLink(uri: "/api/v1/dashboards/" + dashboard.id)}" name="Dashboard ${dashboard.name}">Share</ui:shareButton>
													</li>
												</g:if>
												<g:if test="${writable.contains(dashboard)}">
													<li>
														<span data-id="${dashboard.id}" class="delete-button confirm">
															<i class="fa fa-trash-o"></i> Delete
														</span>
													</li>
												</g:if>
											</ul>
										</div>
									</g:if>
								</ui:td>
				            </ui:tr>
						</g:each>
					</ui:tbody>
				</ui:table>
            </div> <%-- end panel body --%>
        </div> <%-- end panel --%>
</body>
</html>
