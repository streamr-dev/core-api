<html>
    <head>
        <meta name="layout" content="main" />
        <title><g:message code="canvas.list.label" /></title>

		<r:require module="confirm-button"/>

        <r:script>
        	$(function() {
				function applyFilter() {
					var params = {
						state: [],
						term: $("#search-term").val()
					}

					$(".filter-toggle-button.active").each(function(i, btn) {
						params.state.push($(btn).data('state'))
					})

					window.location = '${createLink(controller:"canvas", action:"list")}?' + $.param(params, true)
				}
				// The buttons are in two places at the page so we have to add/remove the class to/from both of them
				$(".filter-toggle-button.running-filter").click(function() {
					if(!$(this).hasClass("active"))
						$(".filter-toggle-button.running-filter").addClass('active')
					else
						$(".filter-toggle-button.running-filter").removeClass('active')
					applyFilter()
				})
				$(".filter-toggle-button.stopped-filter").click(function() {
					if(!$(this).hasClass("active"))
						$(".filter-toggle-button.stopped-filter").addClass('active')
					else
						$(".filter-toggle-button.stopped-filter").removeClass('active')
					applyFilter()
				})
				$("#search-button").click(applyFilter)
				$("#search-term").change(applyFilter)
				$('#search-term').focus(function(event) {
					setTimeout(function() {
						$('#search-term').select();
					});
				});
			})
		</r:script>
		<r:script>
			$(document).ready(function() {
				$(".delete-canvas-link[data-confirm=true]").each(function(i, el) {
					new ConfirmButton(el, {
						title: "${ message(code: 'canvas.delete.title') }",
						message: "${ message(code: 'canvas.delete.confirm') }"
					}, function(result) {
						if (result) {
							$.ajax("${ createLink(uri:"/api/v1/canvases", absolute: true)}" + '/' + $(el).data('id'), {
								method: 'DELETE',
								success: function() {
									location.reload()
								},
								error: function(e, t, msg) {
									Streamr.showError("${ message(code: 'canvas.delete.error') }", "${ message(code: 'canvas.delete.error.title') }")
								}
							})
						}
					})
				})
			})
		</r:script>

		<r:layoutResources disposition="head"/>
    </head>
    <body class="canvas-list-page">
		
		<ui:flashMessage/>

		<div class="btn-group toolbar">
			<a id="createButton" class="btn btn-primary" href="${createLink(action:'editor')}">
				<i class="fa fa-plus"></i> Create Canvas
			</a>
		</div>

		<div class="panel list-panel">
			<div class="panel-heading">
				<span class="panel-title"> <g:message
						code="canvas.list.label" />
				</span>
				<div class="panel-heading-controls">
					<div class="hidden-xs">
						<g:render template="canvasListSearch"/>
					</div>
				</div>
			</div>
			<div class="panel-body">
				<ui:table>
					<ui:thead>
						<ui:tr>
							<ui:th><g:message code="canvas.name.label" /></ui:th>
							<ui:th><g:message code="canvas.state.label" /></ui:th>
							<ui:th class="hidden-xs"><g:message code="canvas.updated.label" /></ui:th>
							<ui:th class="button-column" />
						</ui:tr>
					</ui:thead>
					<ui:tbody>
						<g:each in="${canvases}" var="canvas">
							<ui:tr link="${createLink(controller:'canvas', action:'editor', id:canvas.id) }" data-id="${canvas.id }">
								<ui:td>
									${fieldValue(bean: canvas, field: "name")}
								</ui:td>
								<ui:td>
									<span class="label ${canvas.state == com.unifina.domain.signalpath.Canvas.State.RUNNING ? "label-primary" : "label-default"}">${canvas.state.id.toLowerCase()}</span>
								</ui:td>
								<ui:td class="hidden-xs">
									<g:formatDate date="${canvas.dateCreated}" formatName="default.date.format" timeZone="${user.timezone}" />
								</ui:td>
								<ui:td class="button-column">
									<g:if test="${writableCanvases.contains(canvas) || shareableCanvases.contains(canvas)}">
										<div class="streamr-dropdown">
											<button class="canvas-menu-toggle dropdown-toggle btn btn-sm" data-toggle="dropdown">
												<i class="navbar-icon fa fa-caret-down"></i>
											</button>
											<ul class="dropdown-menu pull-right">
												<g:if test="${shareableCanvases.contains(canvas)}">
													<li>
														<ui:shareButton url="${createLink(uri: "/api/v1/canvases/$canvas.id")}" name="Canvas ${canvas.name}" type="span">Share</ui:shareButton>
													</li>
												</g:if>
												<g:if test="${writableCanvases.contains(canvas)}">
													<li>
														<span data-id="${canvas.id}" class="delete-canvas-link" data-confirm="true">
															<i class="fa fa-trash-o"></i> Delete canvas
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
			</div>
			<%-- end panel body --%>
		</div>
		<%-- end panel --%>

	</body>
</html>
