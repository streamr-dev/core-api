<html>
<head>
    <meta name="layout" content="main" />
    <title><g:message code="profile.edit.label"/></title>

	<r:require module="confirm-button"/>
	<r:require module="moment-timezone"/>
	<r:require module="streamr-credentials-control"/>

	<r:script>
		$(document).ready(function() {
			var tzOpts = moment.tz.names().map(function(tz) {
				return $('<option/>', {
				    selected: tz === '${user.timezone}',
				    value: tz,
				    text: tz
				})
			})
			$("#timezone").append(tzOpts)
		})
	</r:script>

</head>
<body>

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
						<label class="control-label">
							<g:message code="secuser.username.label" />
						</label>
					    <div>
					    	${user.username}
					    </div>
					</div>

					<div class="form-group">
						<label class="control-label">
							<g:message code="secuser.password.label" />
						</label>
					    <div>
							<g:link action="changePwd">
								<g:message code="profile.changePassword.label"/>
							</g:link>
						</div>
					</div>

					<div class="form-group ${hasErrors(bean: user, field: 'name', 'has-error')}">
						<label class="control-label">
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
				<div class="panel">
					<div class="panel-heading">
						<span class="panel-title">${message(code:"profile.credentials.label")}</span>
						<div class="panel-heading-controls">

						</div>
					</div>
					<div class="panel-body">
						<div class="title-label">
							<label>API keys</label>
						</div>
						<div id="api-credentials" class="credentials-control row"></div>
					</div>
				</div>
			</div>
			
			<g:render template="extensions" />
			
		</div>

		<div class="row">
			<div class="col-sm-12 col-md-8 col-md-offset-2">
				<g:submitButton name="submit" class="save btn btn-lg btn-primary" value="${message(code: 'default.button.update.label')}" />
			</div>
		</div>
		
	</form>

	<r:script>
		$(function() {
			new ConfirmButton("#regenerateApiKeyButton", {
				title: "${message(code: "profile.regenerateAPIKeyConfirm.title")}",
				message: "${message(code: "profile.regenerateAPIKeyConfirm.message")}"
			}, function(result) {
				if(result) {
					$.post("${ createLink(action: 'regenerateApiKey') }", {}, function(response) {
						if(response.success) {
							location.reload()
						}
					})
				}
			})

			new StreamrCredentialsControl({
				el: "#api-credentials",
				url: '${createLink(uri: "/api/v1/users/me/keys")}',
				username: '${user.username}'
			})
		})
	</r:script>
</body>
</html>
