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
			<ui:clickableTable>
				<div class="thead">
					<div class="tr">
						<span class="th"><g:message
								code="runningSignalPath.name.label" /></span> <span class="th"><g:message
								code="runningSignalPath.state.label" /></span> <span class="th"><g:message
								code="runningSignalPath.created.label" /></span>
					</div>
				</div>
				<div class="tbody">
					<g:each in="${running}" var="rsp">
						<ui:clickableRow link="${createLink(action:'show', id:rsp.id) }"
							id="${rsp.id }">
							<span class="td">
								${fieldValue(bean: rsp, field: "name")}
							</span>
							<span class="td">
								${fieldValue(bean: rsp, field: "state")}
							</span>
							<span class="td"><g:formatDate date="${rsp.dateCreated}"
									timeZone="${user.timezone}" /></span>
						</ui:clickableRow>
					</g:each>
				</div>
			</ui:clickableTable>
		</div>
		<%-- end panel body --%>
	</div>
	<%-- end panel --%>

</body>
</html>
