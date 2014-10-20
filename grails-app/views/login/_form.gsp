<div class="page-signin-alt">

	<h1 class="form-header"><g:message code="springSecurity.login.header"/></h1>

	<!-- Form -->
	<form action='${postUrl}' method='POST' id='loginForm' autocomplete='off' class="panel">
	
		<g:if test='${flash.message}'>
			<p class='text-danger'>${flash.message}</p>
		</g:if>
	
		<div class="form-group">
			<input type="text" name="j_username" id="username" class="form-control input-lg" placeholder="Username">
		</div> <!-- / Username -->

		<div class="form-group signin-password">
			<input type="password" name="j_password" id="password" class="form-control input-lg" placeholder="Password">
			<g:link controller="register" action="forgotPassword" class="forgot">Forgot?</g:link>
		</div> <!-- / Password -->

		<div class="form-actions">
			<input id="loginButton" type="submit" value="${message(code:'springSecurity.login.button')}" class="btn btn-primary btn-block btn-lg">
		</div> <!-- / .form-actions -->
	</form>
	<!-- / Form -->

</div>