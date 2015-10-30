<html>
<head>
    <meta name="layout" content="login" />
    <title>Sign Up</title>

	<r:script>
	$(document).ready(function() {
		$('#username').focus()
	});
	</r:script>
</head>

<body>
	<g:if test='${signupOk}'>
	
		<h1 id="signup-ok" class="form-header">Thanks for signing up!</h1>
	
		<div class="panel">
			<p>
				As we are still in private beta, we are letting people in little by little.

				As soon as we are ready for you, we'll send you an invite.

				We'll be in touch!
			</p>
		</div>
	</g:if>

	<g:else>
		<h1 class="form-header">Sign Up</h1>

		<g:form action='signup' name='signupForm' class="panel">
			<g:if test='${flash.message}'>
				<p class='text-danger'>${flash.message}</p>
			</g:if>

			<div class="form-group ${hasErrors(bean:user,field:'username', 'has-error')}">
				<input type="text" name="username" id="username"
					class="form-control input-lg"
					placeholder="Email"
					value="${user.username}">

				<g:hasErrors bean="${user}" field="username">
					<span class="text-danger">
						<g:renderErrors bean="${user}" field="username" as="list" />
					</span>
				</g:hasErrors>
			</div>

			<div class="form-actions">
				<input id="loginButton" type="submit" value="${message(code:'springSecurity.register.button', default: 'Sign up')}" class="btn btn-primary btn-block btn-lg">
			</div>
		</g:form>
	</g:else>
</body>
</html>

 


