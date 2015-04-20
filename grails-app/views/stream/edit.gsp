<html>
    <head>
        <meta name="layout" content="main" />
        <title><g:message code="stream.edit.label" args="[stream.name]"/></title>
    </head>
    <body>
    	<ui:breadcrumb>
			<g:render template="/stream/breadcrumbList" />
			<g:render template="/stream/breadcrumbShow" />
			<li class="active">
				<g:link controller="stream" action="edit">
					<g:message code="stream.edit.label" args="[stream.name]"/>
				</g:link>
			</li>
		</ui:breadcrumb>
    
		<ui:flashMessage/>

		<div class="col-xs-12 col-md-8 col-md-offset-2">
			<ui:panel title="${message(code:"stream.edit.label", args:[stream.name])}">
				<g:form action="update">
					<g:hiddenField name="id" value="${stream.id}"/>
					<div class="form-group">
						<label for="name">${message(code:"stream.name.label")}</label> 
						<input class="form-control" type="text" placeholder="Name" name="name" value="${stream.name}"></input>
					</div>
					<div class="form-group">
						<label for="description">${message(code:"stream.description.label")}</label> 
						<textarea class="form-control" placeholder="Description" name="description">${stream.description}</textarea>
					</div>
					<g:submitButton name="submit" class="btn btn-lg btn-primary" value="${message(code:"stream.update.label")}" />
				</g:form>
			</ui:panel>
		</div>
    </body>
</html>
