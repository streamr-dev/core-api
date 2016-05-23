<html>
    <head>
        <meta name="layout" content="main" />
        <title><g:message code="dashboard.create.label" /></title>

        <r:script>
			$(function() {
				$("input:first").focus()
			})
		</r:script>
    </head>
    <body>    
		<ui:flashMessage/>
		<ui:panel title="${message(code:"dashboard.create.label")}">
			<g:form action="save">
				<ui:labeled label="${message(code: "dashboard.name.label")}">
					<input name="name" type="text" class="form-control input-lg" required>
					<g:hasErrors bean="${dashboard}" field="name">
						<span class="text-danger">
							<g:renderErrors bean="${dashboard}" field="name" as="list" />
						</span>
					</g:hasErrors>
				</ui:labeled>
				<button class="btn btn-primary" name="create">
					<g:message code="dashboard.create.button" />					
					<i class="fa fa-angle-right"></i>
				</button>
			</g:form>
		</ui:panel>
    </body>
</html>