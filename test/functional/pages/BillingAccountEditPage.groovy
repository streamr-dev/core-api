package pages

class BillingAccountEditPage extends GrailsPage {

	static controller = "billingAccount"
	static action = "edit"

	static url = "$controller/$action"

	static content = {
            navbar { module NavbarModule }

	}
}

