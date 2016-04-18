<html>
<head>
    <meta name="layout" content="main" />
    <title><g:message code="profile.edit.label"/></title>

	<r:require module="detect-timezone"/>

	<r:script>
		$(document).ready(function() {
			var timezoneList = jstz.getTimezoneList()
			var tzOpts = timezoneList.map(function(tz) {
				return $('<option '+(tz === "${user.timezone}" ? 'selected': '')+' value="'+tz+'">'+tz+'</option>')
			})
			$("#timezone").append(tzOpts)
		})
	</r:script>

</head>
<body>

	<g:render template="variables"/>
	
	<div class="row">
		<div class="col-sm-12 col-md-offset-2 col-md-8">
			<ui:flashMessage/>
		</div>
	</div>

	<form method="post" action="update">
		
		<div class="row">
			<div class="col-sm-6 col-md-offset-2 col-md-4">
				<ui:panel title="Profile Settings">
					<div class="form-group ${hasErrors(bean: user, field: 'username', 'has-error')}">
					
						<label for="username" class="control-label">
							<g:message code="secuser.username.label" default="Username" />
						</label>
					    
					    <div>
					    	${user.username}
					    </div>
					    
					</div>
					
					<div class="form-group">
					
						<label for="changePassword" class="control-label">
							<g:message code="secuser.password.label" />
						</label>
					    
					    <div>
							<g:link action="changePwd">
								<g:message code="profile.changePassword.label"/>
							</g:link>
						</div>

					</div>
					
					<div class="form-group ${hasErrors(bean: user, field: 'name', 'has-error')}">
					
						<label for="name" class="control-label">
							<g:message code="secuser.name.label" />
						</label>
					    
						<input name="name" type="text" class="form-control input-lg" value="${user.name}" required>
		
						<g:hasErrors bean="${user}" field="name">
							<span class="text-danger">
								<g:renderErrors bean="${user}" field="name" as="list" />
							</span>
						</g:hasErrors>
					</div>	
					
					<div class="form-group ${hasErrors(bean: user, field: 'timezone', 'has-error')}">
					
						<label for="timezone" class="control-label">
							<g:message code="secuser.timezone.label"/>
						</label>
					    
						<select name="timezone" id="timezone" class="form-control input-lg"></select>
		
						<g:hasErrors bean="${user}" field="timezone">
							<span class="text-danger">
								<g:renderErrors bean="${user}" field="timezone" as="list" />
							</span>
						</g:hasErrors>
					</div>
					
				</ui:panel>

			</div>

			<div class="col-sm-6 col-md-4">
				<ui:panel title="Credentials">
					<label class="control-label">
						<g:message code="secuser.credentials.apiKey" default="Api Key" />
					</label>

					<div>
						${user.apiKey}
					</div>
				</ui:panel>
			</div>
			
			<g:render template="extensions" />
			
		</div>

		<div class="row">
			<div class="col-sm-12 col-md-8 col-md-offset-2">
				<g:submitButton name="submit" class="save btn btn-lg btn-primary" value="${message(code: 'default.button.update.label', default: 'Update')}" />
			</div>
		</div>
		
	</form>
</body>
</html>
