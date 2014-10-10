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
					<g:render template="form"/>
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
