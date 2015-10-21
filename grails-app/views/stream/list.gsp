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
			<ui:clickableTable>
			    <div class="thead">
			        <a class="tr">
			        	<span class="th"><g:message code="stream.name.label" /></span>
			        	<span class="th"><g:message code="stream.type.label" /></span>
			        	<span class="th"><g:message code="stream.description.label" /></span>
			        </a>
			    </div>
			    <div class="tbody">
				    <g:each in="${streams}" var="stream">
				        <ui:clickableRow link="${ createLink(action:'show', id: stream.id) }" id="${stream.id }">					        
				            <div class="td">${fieldValue(bean: stream, field: "name")}</div>
				            <div class="td">${fieldValue(bean: stream.feed, field: "name")}</div>
				            <div class="td">${fieldValue(bean: stream, field: "description")}</div>
						</ui:clickableRow>
					</g:each>
				</div>
			</ui:clickableTable>
		</ui:panel>
		
    </body>
</html>
