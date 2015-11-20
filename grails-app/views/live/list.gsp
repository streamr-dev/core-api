<html>
    <head>
        <meta name="layout" content="main" />
        <title><g:message code="runningSignalPath.list.label" /></title> 
        
    </head>
    <body>
		
	<ui:flashMessage/>

	<div class="panel">
		<div class="panel-heading">
			<span class="panel-title"> <g:message
					code="runningSignalPath.list.label" />
			</span>
			<div class="panel-heading-controls" style="width: 30%">
				<g:form name="search-form" action="list" role="form">
					<div class="input-group input-group-sm">
						<input name="term" value="${params.term}" placeholder="Search"
							class="form-control" /> <span class="input-group-btn">
							<button class="btn" type="submit">
								<span class="fa fa-search"></span>
							</button>
						</span>
					</div>
					<!-- / .input-group -->
				</g:form>
			</div>
		</div>

		<div class="panel-body">
			<ui:table>
				<ui:thead>
					<ui:tr>
						<ui:th><g:message code="runningSignalPath.name.label" /></ui:th>
						<ui:th><g:message code="runningSignalPath.state.label" /></ui:th>
						<ui:th><g:message code="runningSignalPath.created.label" /></ui:th>
					</ui:tr>
				</ui:thead>
				<ui:tbody>
					<g:each in="${running}" var="rsp">
						<ui:tr link="${createLink(action:'show', id:rsp.id) }" data-id="${rsp.id }">
							<ui:td>
								${fieldValue(bean: rsp, field: "name")}
							</ui:td>
							<ui:td>
								${fieldValue(bean: rsp, field: "state")}
							</ui:td>
							<ui:td><g:formatDate date="${rsp.dateCreated}"
									timeZone="${user.timezone}" /></ui:td>
						</ui:tr>
					</g:each>
				</ui:tbody>
			</ui:table>
		</div>
		<%-- end panel body --%>
	</div>
	<%-- end panel --%>

</body>
</html>
