$.ajaxSetup({
	statusCode: {
		// Set up a global AJAX error handler to handle the 401
		// unauthorized responses. If a 401 status code comes back,
		// the user is no longer logged-into the system and can not
		// use it properly.
		401: showLogin,
		403: showLogin
	}
});

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