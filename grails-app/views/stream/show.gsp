<html>
    <head>
        <meta name="layout" content="main" />
        <title><g:message code="stream.show.label" args="[stream.name]"/></title>
        <r:require module="dropzone"/>
		<r:require module="toolbar"/>
		<r:require module="bootstrap-datepicker"/>
		<r:require module="sharing-dialog"/>
		<r:script>
			$(document).ready(function() {
		 		new Toolbar($("#stream-delete-form"))
		 	})
		</r:script>
    </head>
    <body class="stream-show">
    	<ui:breadcrumb>
			<g:render template="/stream/breadcrumbList" model="[stream:stream]"/>
			<g:render template="/stream/breadcrumbShow" model="[stream:stream, active:true]"/>
		</ui:breadcrumb>
	
		<ui:flashMessage/>

		<div class="row">
			<div class="col-sm-6 col-md-4">
				<div class="panel ">
					<div class="panel-heading">
						<form id="stream-delete-form">
							<g:hiddenField name="id" value="${ stream.id }" />

							<span class="panel-title">${message(code:"stream.show.label", args:[stream.name])}</span>
							<g:if test="${writable}">
								<div class="panel-heading-controls">
									<li class="dropdown">
										<a id="stream-menu-toggle" href="#" class="dropdown-toggle" data-toggle="dropdown">
											<i class="navbar-icon fa fa-bars"></i>
										</a>
										<ul class="dropdown-menu pull-right">
											<li><g:link action="edit" id="${stream.id}"><i class="fa fa-pencil"></i> Edit info</g:link></li>
											<li><a href="#" id="delete-stream-button" data-action="${ createLink(action:'delete') }" class="confirm" data-confirm="Are you sure you want to delete the stream?"><i class="fa fa-trash-o"></i> Delete stream</a></li>
											<g:if test="${shareable}">
												<li><ui:shareButton url="${createLink(uri: "/api/v1/streams/" + stream.id)}" name="Stream ${stream.name}" type="link">Share</ui:shareButton></li>
											</g:if>
										</ul>
									</li>
								</div>
							</g:if>
						</form>
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
