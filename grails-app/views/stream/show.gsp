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

		<webpack:cssBundle name="commons"/>
		<webpack:cssBundle name="streamPageDataPreview"/>
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

		<div class="container">
			<div class="row">
				<div class="col-md-6">
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
				<div class="col-md-6">
					<g:render template="fieldsPanel" model="[config:config]"/>
				</div>
			</div>
			<div class="row">
				<div class="col-xs-12 col-lg-6">
					<ui:panel title="API Credentials">
						<g:render template="userStreamCredentials" model="[stream:stream]"/>
					</ui:panel>
				</div>
				<div id="streamPageDataPreviewRoot" class="col-xs-12 col-lg-6"></div>
			</div>
			<div class="row">

				<div class="col-xs-12">
					<ui:panel title="Stored Events">
						<div class="col-sm-6">
							<g:include controller="stream" action="files" params="[id:stream.id]"/>
						</div>

						<div class="col-sm-6">
							<script>
								$(document).ready(function() {
									var dz = $("#dropzone").dropzone({
										url: "${createLink(controller:'stream', action:'upload', params:[id:stream.id])}",
										paramName: "file", // The name that will be used to transfer the file
										maxFilesize: 512, // MB

										addRemoveLinks : true,
										dictResponseError: "Can't upload file!",
										thumbnailWidth: 138,
										thumbnailHeight: 120,

										previewTemplate: '<div class="dz-preview dz-file-preview"><div class="dz-details"><div class="dz-filename"><span data-dz-name></span></div><div class="dz-size">File size: <span data-dz-size></span></div><div class="dz-thumbnail-wrapper"><div class="dz-thumbnail"><img data-dz-thumbnail><span class="dz-nopreview">No preview</span><div class="dz-success-mark"><i class="fa fa-check-circle-o"></i></div><div class="dz-error-mark"><i class="fa fa-times-circle-o"></i></div><div class="dz-error-message"><span data-dz-errormessage></span></div></div></div></div><div class="progress progress-striped active"><div class="progress-bar progress-bar-success" data-dz-uploadprogress></div></div></div>',

										acceptedFiles: '.csv',
										resize: function(file) {
											var info = { srcX: 0, srcY: 0, srcWidth: file.width, srcHeight: file.height },
													srcRatio = file.width / file.height;
											if (file.height > this.options.thumbnailHeight || file.width > this.options.thumbnailWidth) {
												info.trgHeight = this.options.thumbnailHeight;
												info.trgWidth = info.trgHeight * srcRatio;
												if (info.trgWidth > this.options.thumbnailWidth) {
													info.trgWidth = this.options.thumbnailWidth;
													info.trgHeight = info.trgWidth / srcRatio;
												}
											} else {
												info.trgHeight = file.height;
												info.trgWidth = file.width;
											}
											return info;
										}
									});

									var redirects = []
									var errorMessages = []
									dz[0].dropzone.on("error", function(e, msg, xhr){
										if(msg.redirect)
											redirects.push(msg.redirect)
										errorMessages.push(msg)
									})

									dz[0].dropzone.on("queuecomplete", function() {
										if(redirects.length)
											window.location = redirects[0]
										else if(errorMessages.length) {
											errorMessages.forEach(function (msg) {
												Streamr.showError(msg.error)
											})
											errorMessages = []
										} else
											location.reload()
									})
								});
							</script>

							<div id="dropzone" class="dropzone-box">
								<div class="dz-default dz-message">
									<i class="fa fa-cloud-upload"></i>
									Drop a .csv file here to load event history<br><span class="dz-text-small">or click to pick a file</span>
								</div>
								<g:form action="upload" controller="stream">
									<div class="fallback">
										<g:hiddenField name="id" value="${stream.id}"/>
										<input name="file" type="file" multiple="" />
									</div>
								</g:form>
							</div>

						</div>
					</ui:panel>
				</div>

			</div>
		</div>

		%{-- The data preview React app root is in grails-app/views/stream/_userStreamDetails.gsp --}%

		<script>
			const keyId = "${key.id}"
			const streamId = "${stream.id}"
		</script>

		<webpack:jsBundle name="commons"/>
		<webpack:jsBundle name="streamPageDataPreview"/>
    </body>
</html>
