<html>
    <head>
        <meta name="layout" content="main" />
        <title><g:message code="stream.create.label" /></title>

		<r:script>
			$(function() {
				$("input:first").focus()
			})
		</r:script>
    </head>
    <body>
    	<ui:breadcrumb>
			<g:render template="/stream/breadcrumbList" />
			
			<li class="active">
				<g:link controller="stream" action="create">
					<g:message code="stream.create.label"/>
				</g:link>
			</li>
		</ui:breadcrumb>
    
		<ui:flashMessage/>

		<ui:panel title="${message(code:"stream.create.label")}">
			<g:form action="create">
		
				<g:render template="form"/>

				<button class="btn btn-primary" name="next">
					${message(code:"next.button.label")}
					<i class="fa fa-angle-right"></i>
				</button>
			</g:form>
		</ui:panel>
		
    </body>
</html>
