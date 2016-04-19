(function() {
	// Global ajax error handler to catch 401 and 403 errors and determine if the session has expired.
	// If it is expired, show a modal login screen.
	$(document).ajaxError(function(event, jqXHR, ajaxSettings, thrownError) {
		if (jqXHR.status === 401 || jqXHR.status === 403) {
			isSessionActive(function(isActive) {
				if (!isActive) {
					showLogin()
				}
			})
		}
	})

	function isSessionActive(callback) {
		$.ajax({
			url: Streamr.createLink({ uri: 'api/v1/users/me'}),
			global: false
		}).fail(function() {
			callback(false)
		}).done(function(data) {
			callback(true)
		})
	}

	function showLogin() {
		if (!$("#loginForm").length) {
			$.get(Streamr.createLink("login","loginForm"), function(data) {
				var dialog = bootbox.dialog({
					closeButton: false,
					message: data
				})

				$("#loginForm").on("submit", function(event) {
					var form = $("#loginForm")
					form.append("<input type='hidden' name='spring-security-redirect' value='"+Streamr.createLink("login","ajaxSuccess")+"'>")

					$.ajax({
						type: "POST",
						url: form.attr('action'),
						data: form.serialize()+"&ajax=true",
						async: false,
						global: false,
						dataType: 'JSON',
						success: function(response) {
							if (response.success)
								bootbox.hideAll()
							else {
								$("#loginForm .login-failed-message").html(response.error).show()
							}
						},
						error: function (response) {
							var responseText = response.responseText || '[]';

							try {
								var json = responseText.evalJSON();
								if (json.error) {
									alert("json.error: "+json.error);
								} else {
									alert(responseText);
								}
							} catch(err) {
								alert("Sorry, there was an internal error!")
							}
						}
					})
					event.preventDefault()
					return false
				})

				dialog.on("shown.bs.modal", function() {
					$("#username").focus()
				})
			})
		}
	}
})();
