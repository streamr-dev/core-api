<html>
<head>
    <meta name="layout" content="login" />
    <title>Sign Up</title>

	<r:script>
	function reCaptchaSuccess(response) {
		$("form[name=signupForm]").submit()
	}
	</r:script>

	<!-- reCAPTCHA script-->
	<script src='https://www.google.com/recaptcha/api.js' async></script>
</head>

<body class="login-page show-sign-in">
	<h1 class="form-header">Sign Up</h1>

	<g:form action='signup' name='signupForm' class="panel">
		<g:if test='${flash.message}'>
			<p class='text-danger'>${flash.message}</p>
		</g:if>

		<div class="form-group ${hasErrors(bean:user,field:'username', 'has-error')}">
			<input type="text" name="username" id="username"
				class="form-control input-lg"
				placeholder="Email"
				value="${user.username}"
				autofocus>

			<g:hasErrors bean="${user}" field="username">
				<span class="text-danger">
					<g:renderErrors bean="${user}" field="username" as="list" />
				</span>
			</g:hasErrors>
		</div>

		%{-- If the captcha is changed, the secret key in RegisterController/signup must be changed (recaptchav2.secret/recaptchainvisible.secret) --}%

		 %{--reCaptcha v2--}%
		<div class="g-recaptcha" data-sitekey="${grailsApplication.config.recaptchav2.sitekey}" style="margin: 10px 0;"></div>
		<button class="btn btn-primary btn-block btn-lg">
			<g:message code="springSecurity.register.button" />
		</button>

		%{-- invisible reCaptcha --}%

		%{--<div class="form-actions">--}%
			%{--<button class="g-recaptcha btn btn-primary btn-block btn-lg"--}%
					%{--data-sitekey="${grailsApplication.config.recaptchainvisible.sitekey}"--}%
					%{--id="signupButton"--}%
					%{--type="submit"--}%
					%{--data-callback="reCaptchaSuccess"--}%
			%{-->--}%
				%{--<g:message code="springSecurity.register.button" />--}%
			%{--</button>--}%
		%{--</div>--}%
	</g:form>
</body>
</html>
