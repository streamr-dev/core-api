<html>
<head>
    <meta name="layout" content="login" />
    <title>Forgot Password</title>
    
	<r:script>
		$(document).ready(function() {
			$('#username').focus();
		});
	</r:script>
</head>

<body>

	<h1 class="form-header">Forgot Password</h1>

	<g:if test='${emailSent}'>
		<div class="panel">
			<p>
				<g:message code='spring.security.ui.forgotPassword.sent'/>
			</p>
		</div>
	</g:if>

	<g:else>

		<g:form action='forgotPassword' name='forgotPassword' class="panel">
			<p><g:message code='spring.security.ui.forgotPassword.description'/></p>

			<div class="form-group ${hasErrors(bean:user,field:'username', 'has-error')}">
				<input type="text" name="username" id="username"
					class="form-control input-lg"
					placeholder="Email"
					value="">

				<g:hasErrors bean="${user}" field="username">
					<span class="text-danger">
						<g:renderErrors bean="${user}" field="username" as="list" />
					</span>
				</g:hasErrors>
			</div>

			<div class="form-actions">
				<input id="reset" type="submit" value="${message(code:'sprint.security.ui.forgotPassword.submit', default: 'Send')}" class="btn btn-primary btn-block btn-lg">
			</div>

		</g:form>
	</g:else>

</body>
</html>




