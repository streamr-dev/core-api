<html>
    <head>
        <meta name="layout" content="main" />
        <title><g:message code="stream.list.label" /></title>
		<r:require module="sharing-dialog"/>
		<r:require module="confirm-button"/>
		<r:script>
			$(document).ready(function() {
				$(".delete-stream-link").each(function(i, el) {
					new ConfirmButton(el, {
						message: "${ message(code:'stream.delete.confirm' )}"
					}, function(result) {
						if (result) {
							$.ajax("${ createLink(uri:"/api/v1/streams", absolute: true)}" + '/' + $(el).data('id'), {
								method: 'DELETE',
								success: function() {
									location.reload()
								},
								error: function(e, t, msg) {
									Streamr.showError("${ message(code:'stream.delete.error' )}", "${ message(code:'stream.delete.error.title' )}")
								}
							})
						}
					})
				})
			})
		</r:script>
    </head>
    <body class="stream-list-page">
		<ui:flashMessage/>
		
		<div class="btn-group toolbar">
			<a id="createButton" class="btn btn-primary" href="${createLink(action:'create')}">
				<i class="fa fa-plus"></i>
				<g:message code="stream.create.label"/>
			</a>        	
		</div>
		
		<ui:panel class="list-panel" title="${message(code:"stream.list.label")}">
			<ui:table>
			    <ui:thead>
			        <ui:tr>
			        	<ui:th><g:message code="stream.name.label" /></ui:th>
			        	<ui:th><g:message code="stream.type.label" /></ui:th>
			        	<ui:th class="hidden-xs"><g:message code="stream.description.label" /></ui:th>
			        	<ui:th class="hidden-xs"><g:message code="stream.updated.label" /></ui:th>
						<ui:th class="button-column"></ui:th>
			        </ui:tr>
			    </ui:thead>
			    <ui:tbody>
				    <g:each in="${streams}" var="stream">
				        <ui:tr link="${ createLink(action:'show', id: stream.id) }" data-id="${stream.id }">					        
				            <ui:td>${fieldValue(bean: stream, field: "name")}</ui:td>
				            <ui:td>${fieldValue(bean: stream.feed, field: "name")}</ui:td>
				            <ui:td class="hidden-xs">${fieldValue(bean: stream, field: "description")}</ui:td>
							<ui:td class="hidden-xs"><g:formatDate date="${stream.lastUpdated}" formatName="default.date.format" timeZone="${user.timezone}" /></ui:td>
							<ui:td class="button-column">
								<g:if test="${writable.contains(stream) || shareable.contains(stream)}">
									<div class="dropdown">
										<button class="stream-menu-toggle dropdown-toggle btn btn-sm" data-toggle="dropdown">
											<i class="navbar-icon fa fa-caret-down"></i>
										</button>
										<ul class="dropdown-menu pull-right">
											<g:if test="${shareable.contains(stream)}">
												<li>
													<ui:shareButton type="span" url="${createLink(uri: "/api/v1/streams/" + stream.id)}" name="Stream ${stream.name}">Share</ui:shareButton>
												</li>
											</g:if>
											<g:if test="${writable.contains(stream)}">
												<li>
													<span data-id="${stream.id}" class="delete-stream-link confirm">
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
		</ui:panel>
		
    </body>
</html>
