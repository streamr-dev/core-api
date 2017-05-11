<html>
<head>
    <meta name="layout" content="main" />
    <title>
		<g:message code="profile.edit.label"/>
	</title>

	<r:require module="confirm-button"/>
	<r:require module="moment-timezone"/>
	<r:require module="streamr-credentials-control"/>

	<r:require module="profile-page-webpack-bundle" />

</head>
<body>

	<div class="row">
		<div class="col-sm-12 col-md-offset-2 col-md-8">
			<ui:flashMessage/>
		</div>
	</div>

	<div id="profilePageRoot"></div>

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

			%{--new StreamrCredentialsControl({--}%
				%{--el: "#api-credentials",--}%
				%{--url: '${createLink(uri: "/api/v1/users/me/keys")}',--}%
				%{--username: '${user.username}'--}%
			%{--})--}%

			%{--var tzOpts = moment.tz.names().map(function(tz) {--}%
				%{--return $('<option/>', {--}%
				    %{--selected: tz === '${user.timezone}',--}%
				    %{--value: tz,--}%
				    %{--text: tz--}%
				%{--})--}%
			%{--})--}%
			%{--$("#timezone").append(tzOpts)--}%
		})
	</r:script>
</body>
</html>
