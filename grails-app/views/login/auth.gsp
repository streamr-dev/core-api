<html>
	<head>
	    <meta name="layout" content="login" />
	    <title><g:message code='springSecurity.ui.login.title'/></title>

		<r:script>
			$(document).ready(function() {
			    // From https://stackoverflow.com/questions/901115/how-can-i-get-query-string-values-in-javascript
				function getParameterByName(name, url) {
					if (!url) url = window.location.href;
					name = name.replace(/[\[\]]/g, "\\$&");
					var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
							results = regex.exec(url);
					if (!results) return null;
					if (!results[2]) return '';
					return decodeURIComponent(results[2].replace(/\+/g, " "));
				}

				$('#loginForm').on('login-success', function() {
					var defaultRedirectUrl = Streamr.createLink('canvas', 'editor')
					var redirectParam = getParameterByName('redirect')
					window.location.replace(redirectParam || defaultRedirectUrl)
				})
			})
		</r:script>
	</head>

	<body class="login-page show-sign-up">
		<g:render template="/login/form" />
	</body>
</html>
