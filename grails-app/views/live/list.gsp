<html>
    <head>
        <meta name="layout" content="main" />
        <title><g:message code="runningSignalPath.list.label" /></title>
        
    </head>
    <body>
		
	<ui:flashMessage/>
		
	<div class="panel">
            <div class="panel-heading">
            	<span class="panel-title">
            		<g:message code="runningSignalPath.list.label" />
            	</span>
				<div class="panel-heading-controls" style="width:30%">
					<g:form name="search-form" action="list" role="form">
						<div class="input-group input-group-sm">
							<input name="term" value="${params.term}" placeholder="Search" class="form-control"/>
							<span class="input-group-btn">
								<button class="btn" type="submit">
									<span class="fa fa-search"></span>
								</button>
							</span>
						</div> <!-- / .input-group -->
					</g:form>
				</div>
            </div>
            
            <div class="panel-body">
	
				<ui:clickableTable>
				    <thead>
				        <tr>
				        	<th><g:message code="runningSignalPath.name.label" /></th>
							<th><g:message code="runningSignalPath.state.label" /></th>
				        	<th><g:message code="runningSignalPath.created.label" /></th>
				        </tr>
				    </thead>
				    <tbody>
					    <g:each in="${running}" var="rsp">
					        <ui:clickableRow link="${createLink(action:'show', id:rsp.id) }" id="${rsp.id }">					        
					            <td>${fieldValue(bean: rsp, field: "name")}</td>
					            <td>${fieldValue(bean: rsp, field: "state")}</td>
					            <td><g:formatDate date="${rsp.dateCreated}" timeZone="${user.timezone}"/></td>
							</ui:clickableRow>
						</g:each>
					</tbody>
				</ui:clickableTable>

            </div> <%-- end panel body --%>
        </div> <%-- end panel --%>
    </body>
</html>
