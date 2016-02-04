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
						<span class="panel-title">${message(code:"stream.show.label", args:[stream.name])}</span>
						<div class="panel-heading-controls">
							<g:link action="edit" id="${stream.id}"><span class="btn btn-sm">Edit info</span></g:link>
							<form id="stream-delete-form">
								<g:hiddenField name="id" value="${ stream.id }" />
								<button id="delete-stream-button" data-action="${ createLink(action:'delete') }" data-confirm="Are you sure you want to delete the stream?" class="btn btn-danger btn-sm confirm">Delete stream</button>
							</form>
						</div>
					</div>
					<div class="panel-body">
						<button class="share-button btn" onclick="sharePopup('${createLink(uri: "/api/v1/streams/" + stream.uuid)}', 'Stream ${stream.name}')"> Share </button>

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
