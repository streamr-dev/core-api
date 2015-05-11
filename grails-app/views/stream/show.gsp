<html>
    <head>
        <meta name="layout" content="main" />
        <title><g:message code="stream.show.label" args="[stream.name]"/></title>
        <r:require module="dropzone"/>
		<r:require module="toolbar"/>
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
							<g:link action="edit" id="${stream.id}"><span class="btn btn-sm">Edit</span></g:link>
						</div>
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
