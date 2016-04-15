<html>
    <head>
        <meta name="layout" content="main" />
        <title><g:message code="canvas.list.label" /></title>
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
				$(".filter-toggle-button").click(function() {
					$(this).toggleClass('active')
					applyFilter()
				})
				$("#search-button").click(applyFilter)
				$("#search-term").change(applyFilter)
				$('#search-term').focus(function(event) {
					setTimeout(function() {$('#search-term').select();}, 0);
				});
			})
		</r:script>
    </head>
    <body class="canvas-list-page">
		
	<ui:flashMessage/>

	<div class="panel">
		<div class="panel-heading">
			<span class="panel-title"> <g:message
					code="canvas.list.label" />
			</span>
			<div class="panel-heading-controls">
				<div class="form-inline">
					<button type="button" class="filter-toggle-button btn btn-xs btn-default btn-outline ${stateFilter.contains('running') ? 'active' : ''}" data-state="running" data-toggle="button" aria-pressed="false" autocomplete="off">
						running
					</button>
					<button type="button" class="filter-toggle-button btn btn-xs btn-default btn-outline ${stateFilter.contains('stopped') ? 'active' : ''}" data-state="stopped" data-toggle="button" aria-pressed="false" autocomplete="off">
						stopped
					</button>
					<div class="input-group input-group-sm">
						<input id="search-term" name="term" value="${params.term}" placeholder="Search by name"
							class="form-control" /> <span class="input-group-btn">
							<button id="search-button" class="btn" type="submit">
								<span class="fa fa-search"></span>
							</button>
						</span>
					</div>
					<!-- / .input-group -->
				</div>
			</div>
		</div>

		<div class="panel-body">
			<ui:table>
				<ui:thead>
					<ui:tr>
						<ui:th><g:message code="canvas.name.label" /></ui:th>
						<ui:th><g:message code="canvas.state.label" /></ui:th>
						<ui:th><g:message code="canvas.created.label" /></ui:th>
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
							<ui:td>
								<g:formatDate date="${canvas.dateCreated}" formatName="default.date.format" timeZone="${user.timezone}" />
								<g:if test="${shareable.contains(canvas)}">
									<ui:shareButton class="btn-end-of-row" url="${createLink(uri: "/api/v1/canvases/" + canvas.id)}" name="Canvas ${canvas.name}" />
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
