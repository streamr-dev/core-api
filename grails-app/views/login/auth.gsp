<html>
	<head>
	    <meta name="layout" content="login" />
	    <title><g:message code='spring.security.ui.login.title'/></title>
	    
		<r:script>
			$(document).ready(function() {
				$('#username').focus();
			});
		</r:script>
	</head>

	<body class="show-sign-up">
		<g:render template="/login/form" plugin="unifina-core"/>
	</body>
</html>
