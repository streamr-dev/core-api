<div class="page-signin-alt">

	<h1 class="form-header"><g:message code="springSecurity.login.header"/></h1>

	<!-- Form -->
	<form action='${postUrl}' method='POST' id='loginForm' autocomplete='off' class="panel">

		<p class='text-danger login-failed-message' ${flash.message ? "" : "style='display:none'"}>${flash.message}</p>

		<div class="form-group">
			<input type="text" name="j_username" id="username" class="form-control input-lg" placeholder="${message(code:"secuser.username.label", default:"Username")}">
		</div> <!-- / Username -->

		<div class="form-group signin-password">
			<input type="password" name="j_password" id="password" class="form-control input-lg" placeholder="Password">

		</div> <!-- / Password -->

		<div class="form-actions">
			<button id="loginButton" class="btn btn-primary btn-block btn-lg">
				<span class="loginText">${message(code:'springSecurity.login.button')}</span>
				<span class="spinner" style="display: none"><i class="fa fa-spin fa-spinner"></i></span>
			</button>
		</div> <!-- / .form-actions -->

		<input type="hidden" name="spring-security-redirect" value="<g:createLink controller="login" action="ajaxSuccess"/>">

		<g:if test="${ grailsApplication.config.grails.plugin.springsecurity.rememberMe.enabled == true }">
			<div class="remember">
				<label id="rememberMeCheckbox" class="checkbox-inline" style="margin-top:15px;width:100%;">
					<input type="checkbox" class="px" name="_spring_security_remember_me">
					<span class="lbl">Remember me</span>
		</g:if>
		<g:else>
			<div>
				<label style="margin-top:15px;width:100%;">
		</g:else>

					<g:link controller="register" action="forgotPassword" class="forgot" style="float:right;">Forgot password?</g:link>
				</label>
			</div>
	</form>
	<!-- / Form -->

</div>

<script>
	$(document).ready(function() {
		$('#username').focus();

		var $form = $("#loginForm")
		var $btn = $('#loginButton')

		function disableButton() {
			$btn.find('.loginText').hide()
			$btn.find('.spinner').show()
			$btn.attr('disabled', 'disabled')
		}

		function enableButton() {
			$btn.find('.loginText').show()
			$btn.find('.spinner').hide()
			$btn.removeAttr('disabled')
		}

		function handleError(error) {
			$form.find('.login-failed-message').text(error).show()
			enableButton()
		}

		// Submit form on button click
		$btn.click(function(event) {
			disableButton()

			$.ajax({
				type: "POST",
				url: $form.attr('action'),
				data: $form.serialize()+"&ajax=true",
				global: false, // disables event propagation to global 401/403 handler
				dataType: 'JSON',
				success: function(response) {
					if (response.success) {
						// Trigger an event on the form to signal success to whoever is wrapping this template
						$form.trigger('login-success', response)
					} else {
						handleError(response.error)
					}
				},
				error: function (response) {
					var responseText = response.responseText || '[]';

					try {
						var json = responseText.evalJSON();
						if (json.error) {
							handleError(json.error);
						} else {
							handleError(responseText);
						}
					} catch(err) {
						handleError("Sorry, there was an internal error!")
					}
				}
			})
			event.preventDefault()
			return false
		})
	})
</script>
