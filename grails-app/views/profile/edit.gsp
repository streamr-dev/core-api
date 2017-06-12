<html>
<head>
    <meta name="layout" content="main" />
    <title>
		<g:message code="profile.edit.label"/>
	</title>

	<r:require module="confirm-button"/>
	<r:require module="moment-timezone"/>
	<r:require module="streamr-credentials-control"/>

	<r:require module="profile-page-webpack-bundle"/>

</head>
<body>

	<div class="row">
		<div class="container">
			<ui:flashMessage/>
		</div>
	</div>
	<div class="row">
		<div class="container">
			<div id="profilePageRoot"></div>
		</div>
	</div>

	<r:script>
		$(function() {
			new ConfirmButton("#regenerateApiKeyButton", {
				title: "${message(code: "profile.regenerateAPIKeyConfirm.title")}",
				message: "${message(code: "profile.regenerateAPIKeyConfirm.message")}"
			}, function(result) {
				if(result) {
					$.post("${ createLink(action: 'regenerateApiKey') }", {}, function(response) {
						if(response.success) {
							location.reload()
						}
					})
				}
			})
		})
	</r:script>
</body>
</html>
