<html>
<head>
    <meta name="layout" content="main" />
    <title><g:message code="profile.changePassword.label"/></title>
    
    <r:require module="password-meter"/>
    
    <r:script>
		// Async-loading zxcvbn (used by password-meter) requires this
		var zxcvbn_js_url = "${resource(dir:'js/zxcvbn', file:'zxcvbn.min.js', plugin:'unifina-core')}"
	
    	$(document).ready(function() {
	    	new PasswordMeter("password", 8, function() {
				return ["algocanvas", "streamr", "${user.username}", "${user.name}"]
			})
		})
	</r:script>
    
</head>
<body>
	
	<form method="post">
		
		<div class="row">
			<div class="col-sm-offset-4 col-sm-4">
				<ui:panel>
				
					<ui:flashMessage/>
				
					<div class="form-group signin-password ${hasErrors(bean:cmd,field:'currentpassword', 'has-error')}">
						<input type="password" name="currentpassword" id="currentpassword"
							class="form-control input-lg"
							placeholder="Current Password" required>
			
						<g:hasErrors bean="${cmd}" field="currentpassword">
							<span class="text-danger">
								<g:renderErrors bean="${cmd}" field="currentpassword" as="list" />
							</span>
						</g:hasErrors>
					</div>
				
					<div class="form-group signin-password ${hasErrors(bean:cmd,field:'password', 'has-error')}">
						<input type="password" name="password" id="password"
							class="form-control input-lg"
							placeholder="New Password" required>
			
						<g:hasErrors bean="${cmd}" field="password">
							<span class="text-danger">
								<g:renderErrors bean="${cmd}" field="password" as="list" />
							</span>
						</g:hasErrors>
					</div>
			
					<div class="form-group signin-password ${hasErrors(bean:cmd,field:'password2', 'has-error')}">
						<input type="password" name="password2" id="password2" class="form-control input-lg" placeholder="New Password (again)" required>
						<g:hasErrors bean="${cmd}" field="password2">
							<span class="text-danger">
								<g:renderErrors bean="${cmd}" field="password2" as="list" />
							</span>
						</g:hasErrors>
					</div>
				
					<g:submitButton name="submit" class="save btn btn-lg btn-primary btn-block" value="${message(code: 'profile.changePassword.label')}" />
					
				</ui:panel>
			</div>
		</div>
		
	</form>
</body>
</html>
