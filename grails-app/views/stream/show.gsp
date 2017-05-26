<html>
    <head>
        <meta name="layout" content="main" />
        <title>
			<g:message code="stream.show.label" args="[stream.name]"/>
		</title>
        <r:require module="dropzone"/>
		<r:require module="confirm-button"/>
		<r:require module="bootstrap-datepicker"/>
		<r:require module="sharing-dialog"/>
		<r:require module="streamr-credentials-control"/>
		<r:script>
			$(function() {
				new ConfirmButton($("#delete-stream-button"), {
					message: "${ message(code:'stream.delete.confirm' )}",
				}, function(result) {
					if (result) {
						$.ajax("${ createLink(uri:"/api/v1/streams/$stream.id", absolute: true)}", {
							method: 'DELETE',
							success: function() {
								window.location = "${ createLink(controller:'stream', action:'list') }"
							},
							error: function(e, t, msg) {
								Streamr.showError("${ message(code:'stream.delete.error' )}", "${ message(code:'stream.delete.error.title' )}")
							}
						})
					}
				})
				new StreamrCredentialsControl({
					el: "#stream-credentials",
					url: '${createLink(uri: "/api/v1/streams/${stream.id}/keys")}',
					streamId: '${stream.id}',
					showPermissions: true
				})
			})
		</r:script>
    </head>
    <body class="stream-show main-menu-fixed">
		<ui:breadcrumb>
			<li>
				<g:link controller="stream" action="list">
					<g:message code="stream.list.label"/>
				</g:link>
			</li>
			<li class="active">
				<g:link controller="stream" action="show" id="$stream.id">
					${ stream.name }
				</g:link>
			</li>
		</ui:breadcrumb>

		<ui:flashMessage/>

		<div class="row">
			<div class="col-sm-6 col-md-4">
				<div class="panel ">
					<div class="panel-heading">
						<g:hiddenField name="id" value="${ stream.id }" />

						<span class="panel-title">${message(code:"stream.show.label", args:[stream.name])}</span>
						<g:if test="${writable}">
							<div class="panel-heading-controls">
								<li class="dropdown">
									<button id="stream-menu-toggle" href="#" class="dropdown-toggle btn btn-sm" data-toggle="dropdown">
										<i class="navbar-icon fa fa-bars"></i>
									</button>
									<ul class="dropdown-menu pull-right">
										<li>
											<g:link action="edit" id="${stream.id}">
												<i class="fa fa-pencil"></i> <g:message code="stream.editInfo.label"/>
											</g:link>
										</li>
										<g:if test="${shareable}">
											<li>
												<ui:shareButton url="${createLink(uri: "/api/v1/streams/" + stream.id)}" name="Stream ${stream.name}" type="link">
													<g:message code="stream.share.label"/>
												</ui:shareButton>
											</li>
										</g:if>
										<li>
											<a href="#" id="delete-stream-button">
												<i class="fa fa-trash-o"></i> <g:message code="stream.delete.label"/>
											</a>
										</li>
									</ul>
								</li>
							</div>
						</g:if>
					</div>
					<div class="panel-body">

						<ui:labeled label="${message(code:"stream.name.label")}">
							${stream.name}
						</ui:labeled>

						<ui:labeled label="${message(code:"stream.description.label")}">
								${stream.description}
						</ui:labeled>

						<ui:labeled label="${message(code:"stream.type.label")}">
								${stream.feed.name}
						</ui:labeled>
					</div>
				</div>
			</div>

			<g:include action="details" id="${stream.id}"/>


		</div>
    </body>
</html>
