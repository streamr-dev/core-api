<!doctype html>
<html>

<head>
	<title><g:message code='spring.security.ui.login.title'/></title>
	<r:require module="bootstrap"/>
	<r:layoutResources/>
</head>

<body>
	<div class="container">
		<div class="row">
			<div class="col-xs-6">
			<h1><g:message code="springSecurity.login.header"/></h1>

			<g:if test='${flash.message}'>
				<p class='text-danger'>${flash.message}</p>
			</g:if>

			<form action='${postUrl}' method='POST' id='loginForm' autocomplete='off'>
				<div class="form-group">
					<label for='username'><g:message code="springSecurity.login.username.label"/>:</label>
					<input type='text' class='form-control' name='j_username' id='username'/>
				</div>

				<div class="form-group">
					<label for='password'><g:message code="springSecurity.login.password.label"/>:</label>
					<input type='password' class='form-control' name='j_password' id='password'/>
				</div>

				<div class="form-group">
					<div class="checkbox">
						<label>
							<input type='checkbox' name='${rememberMeParameter}' id='remember_me' <g:if test='${hasCookie}'>checked='checked'</g:if>>
						  	<g:message code="springSecurity.login.remember.me.label"/>
				  		</label>
					</div>
				</div>

				<div class="form-group">
					<input type='submit' class="btn btn-default" id="loginButton"
						value='${message(code: "springSecurity.login.button")}'/>
				</div>
			</form>
		</div>
		</div>
	</div>

	<script>
	$(document).ready(function() {
		$('#username').focus();
	});
	</script>

	<r:layoutResources/>
</body>
</html>
