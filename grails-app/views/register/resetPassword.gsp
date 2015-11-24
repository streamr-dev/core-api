<html>
<head>
	<title><g:message code='spring.security.ui.resetPassword.title'/></title>
	<meta name='layout' content='login'/>
	
	<r:require module="password-meter"/>
    
    <r:script>
		// Async-loading zxcvbn (used by password-meter) requires this
		var zxcvbn_js_url = "${resource(dir:'js/zxcvbn', file:'zxcvbn.min.js', plugin:'unifina-core')}"
	
    	$(document).ready(function() {
	    	new PasswordMeter("password", 8, function() {
				return ["algocanvas", "streamr", "${user.username}", "${user.name}"]
			})

			$('#password').focus();
		});
	</r:script>
	
	<g:set var="user" value="${command}"/>
</head>

<body>

	<h1 class="form-header">Reset Password</h1>

	<g:form action='resetPassword' name='resetPasswordForm' class="panel">
		<ui:flashMessage/>
	
		<g:hiddenField name='t' value='${token}'/>

		<div class="form-group signin-password ${hasErrors(bean:user,field:'password', 'has-error')}">
			<input type="password" name="password" id="password"
				class="form-control input-lg"
				placeholder="Password" required>

			<g:hasErrors bean="${user}" field="password">
				<span class="text-danger">
					<g:renderErrors bean="${user}" field="password" as="list" />
				</span>
			</g:hasErrors>
		</div>

		<div class="form-group signin-password ${hasErrors(bean:user,field:'password2', 'has-error')}">
			<input type="password" name="password2" id="password2" class="form-control input-lg" placeholder="Password (again)" required>
			<g:hasErrors bean="${user}" field="password2">
				<span class="text-danger">
					<g:renderErrors bean="${user}" field="password2" as="list" />
				</span>
			</g:hasErrors>
		</div>

		<div class="form-actions">
			<input id="reset" type="submit" value="${message(code:'spring.security.ui.resetPassword.submit', default: 'Reset Password')}" class="btn btn-primary btn-block btn-lg">
		</div>
	</g:form>

</body>
</html>
