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

                $('#loginForm').on('login-success', function() {
                    bootbox.hideAll()
                })

				dialog.on("shown.bs.modal", function() {
					$("#username").focus()
				})
			})
		}
	}
})();
