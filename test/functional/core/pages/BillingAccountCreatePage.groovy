package core.pages

class BillingAccountCreatePage extends GrailsPage {

	static controller = "billingAccount"
	static action = "create"

	static url = "$controller/$action"

	static content = {
            navbar { module NavbarModule }

	}
}

