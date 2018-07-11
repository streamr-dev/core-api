package pages

class ProfileEditPage extends GrailsPage {

	static controller = "profile"
	static action = "edit"

	static url = "$controller/$action"

	static at = {
		waitFor {
			changePasswordButton.displayed // Wait for React
		}
	}

	static content = {
		navbar { module NavbarModule }

		changePasswordButton { $('.form-group a', text: "Change Password") }
		timeZone { $("#timezone") }
		defaultCommission { $("input", name: "defaultCommission") }
		defaultLatency { $("input", name: "defaultLatency") }
		defaultPortfolioName { $("input", name: "defaultPortfolio") }
		alert { $(".alert") }
		saveButton { $("#submit") }
	}
}

