<html>
    <head>
        <meta name="layout" content="main" />
        <title><g:message code="stream.list.label" /></title>
		<r:require module="sharing-dialog"/>
    </head>
    <body class="stream-list-page">
		<ui:flashMessage/>
		
		<div class="btn-group toolbar">
			<a id="createButton" class="btn btn-primary" href="${createLink(action:'create')}">
				<i class="fa fa-plus"></i>
				<g:message code="stream.create.label"/>
			</a>        	
		</div>
		
		<ui:panel title="${message(code:"stream.list.label")}">
			<ui:table>
			    <ui:thead>
			        <ui:tr>
			        	<ui:th><g:message code="stream.name.label" /></ui:th>
			        	<ui:th><g:message code="stream.type.label" /></ui:th>
			        	<ui:th><g:message code="stream.description.label" /></ui:th>
			        </ui:tr>
			    </ui:thead>
			    <ui:tbody>
				    <g:each in="${streams}" var="stream">
				        <ui:tr link="${ createLink(action:'show', id: stream.id) }" data-id="${stream.id }">					        
				            <ui:td>${fieldValue(bean: stream, field: "name")}</ui:td>
				            <ui:td>${fieldValue(bean: stream.feed, field: "name")}</ui:td>
				            <ui:td>
								${fieldValue(bean: stream, field: "description")}
								<button class="btn share-button" onclick="sharePopup('${createLink(uri: "/api/v1/streams/" + stream.uuid)}', 'Stream ${stream.name}')">
									<span class="fa fa-users"></span>
								</button>
							</ui:td>
						</ui:tr>
					</g:each>
				</ui:tbody>
			</ui:table>
		</ui:panel>
		
    </body>
</html>
