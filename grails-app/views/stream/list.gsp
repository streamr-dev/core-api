<html>
    <head>
        <meta name="layout" content="main" />
        <title><g:message code="stream.list.label" /></title>
        
    </head>
    <body>
		<ui:flashMessage/>
		
		<div class="btn-group toolbar">
			<a id="createButton" class="btn btn-primary" href="${createLink(action:'create')}">
				<i class="fa fa-plus"></i>
				<g:message code="stream.create.label"/>
			</a>        	
		</div>
		
		<ui:panel title="${message(code:"stream.list.label")}">
			<table class="table table-striped table-bordered table-hover table-condensed table-responsive">
			    <thead>
			        <tr>
			        	<th><g:message code="stream.id.label" /></th>
			        	<th><g:message code="stream.name.label" /></th>
			        	<th><g:message code="stream.type.label" /></th>
			        	<th><g:message code="stream.description.label" /></th>
			        </tr>
			    </thead>
			    <tbody>
				    <g:each in="${streams}" var="stream">
				        <tr>					        
				            <td><g:link action="show" id="${stream.id}">${fieldValue(bean: stream, field: "id")}</g:link></td>					        
				            <td>${fieldValue(bean: stream, field: "name")}</td>
				            <td>${fieldValue(bean: stream.feed, field: "name")}</td>
				            <td>${fieldValue(bean: stream, field: "description")}</td>
						</tr>
					</g:each>
				</tbody>
			</table>
		</ui:panel>
		
    </body>
</html>
