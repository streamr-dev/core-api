package pages

class ProfileEditPage extends GrailsPage {

	static controller = "profile"
	static action = "edit"

	static url = "$controller/$action"

	static at = {
		waitFor {
			changePassword.displayed // Wait for React
		}
	}

	static content = {
            navbar { module NavbarModule }

            changePassword { $('.form-group a', text: "Change Password") }
            timeZone { $("#timezone") }
            defaultCommission { $("input", name: "defaultCommission") }
            defaultLatency { $("input", name: "defaultLatency") }
            defaultPortfolioName { $("input", name: "defaultPortfolio") }
            saveButton { $("#submit") }
	}
}

