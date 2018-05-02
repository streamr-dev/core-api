<html>
<head>
    <meta name="layout" content="login" />
    <title>Sign Up</title>

	<g:if test="${grailsApplication.config.streamr.signup.requireCaptcha}">
		<r:script>
			function reCaptchaSuccess(response) {
				$("form[name=signupForm]").submit()
			}
		</r:script>

		<!-- reCAPTCHA script-->
		<script src='https://www.google.com/recaptcha/api.js' async></script>
	</g:if>
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

		 %{--reCaptcha v2--}%
		<g:if test="${grailsApplication.config.streamr.signup.requireCaptcha}">
			<div class="g-recaptcha" data-sitekey="${grailsApplication.config.recaptchav2.sitekey}" style="margin: 10px 0;"></div>
		</g:if>

		<button id="signUpButton" class="btn btn-primary btn-block btn-lg">
			<g:message code="springSecurity.register.button" />
		</button>
	</g:form>
</body>
</html>
