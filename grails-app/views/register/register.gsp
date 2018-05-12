<html>
<head>
    <meta name="layout" content="login" />
    <title>Register</title>

	<r:require module="moment-timezone"/>
	<r:require module="bootbox"/>
	<r:require module="password-meter"/>

	<r:script>
	// Async-loading zxcvbn (used by password-meter) requires this
	var zxcvbn_js_url = "${resource(dir:'js/zxcvbn', file:'zxcvbn.min.js', plugin:'unifina-core')}"

	$(document).ready(function() {
		$('#name').focus()

		var currentTz = moment.tz.guess()

		$('span.timezone').text(currentTz)
		$('#weDetectedYourTimezone').show()
		$('#changeTimezone').click(function() {
			$('#timezoneSelect').show()
		})

    	var $tzSelect = $('#timezone')
		var tzOpts = moment.tz.names().map(function(tz) {
			return $('<option/>', {
			    selected: tz === currentTz ? 'selected' : undefined,
			    value: tz,
			    text: tz
			})
		})

		$tzSelect.append(tzOpts)

		new PasswordMeter("password", 8, function() {
			return ["algocanvas", "streamr", $("#name").val(), $("#username").val()]
		})

	});

	</r:script>
</head>

<body>

	<g:if test='${emailSent}'>
		<h1 class="form-header">Thank you!</h1>

		<div class="panel">
			<p>We've sent you a confirmation email. Please check your email, and click on the link to continue.</p>
		</div>
	</g:if>

	<g:else>
		<h1 class="form-header">Register</h1>

		<g:form action='register' name='registerForm' class="panel">

			<g:if test='${flash.message}'>
				<p class='text-danger'>${flash.message}</p>
			</g:if>

			<input type="hidden" name="invite" value="${invite}"/>
			<input type="hidden" id="username" name="username" value="${user.username}"/>

			<div class="form-group ${hasErrors(bean:user,field:'username', 'has-error')}">
				<input type="text" disabled="disabled"
					class="form-control input-lg"
					value="${user.username}" required>

				<g:hasErrors bean="${user}" field="username">
					<span class="text-danger">
						<g:renderErrors bean="${user}" field="username" as="list" />
					</span>
				</g:hasErrors>
			</div>

			<div class="form-group signin-name ${hasErrors(bean:user,field:'name', 'has-error')}">
				<input type="text" name="name" id="name"
					class="form-control input-lg"
					placeholder="${message(code:'secuser.name.label')}" value="${user.name}" required>

				<g:hasErrors bean="${user}" field="name">
					<span class="text-danger">
						<g:renderErrors bean="${user}" field="name" as="list" />
					</span>
				</g:hasErrors>
			</div>

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

			<p id="weDetectedYourTimezone" style="display:none">
				We auto-detected your timezone as <span class="timezone">Europe/Helsinki</span>.
				<a href="#" id="changeTimezone">Change timezone</a>
			</p>

			<div class="form-group" id="timezoneSelect" style="display:none">
				<select name="timezone" id="timezone" class="form-control"></select>
			</div>

			<div class="form-group" style="margin-top: 20px;margin-bottom: 20px;">
				<label class="checkbox-inline">
					<input type="checkbox" name="tosConfirmed" class="px" id="tosConfirmed" value="on" required>
					<span class="lbl">I agree with the <a href="https://s3.amazonaws.com/streamr-public/streamr-terms-of-use.pdf" target="_blank" id="tc-link">Terms of Use</a> and <a href="https://s3.amazonaws.com/streamr-public/streamr-privacy-policy.pdf" target="_blank" id="privacy-link">Privacy Policy</a></span>
				</label>

				<g:hasErrors bean="${user}" field="tosConfirmed">
					<p></p>
					<span class="text-danger">
						<g:renderErrors bean="${user}" field="tosConfirmed" as="list" />
					</span>
				</g:hasErrors>
			</div>

			<div class="form-actions">
				<input id="loginButton" type="submit" value="${message(code:'springSecurity.register.button')}" class="btn btn-primary btn-block btn-lg">
			</div>
		</g:form>
	</g:else>
</body>
</html>




