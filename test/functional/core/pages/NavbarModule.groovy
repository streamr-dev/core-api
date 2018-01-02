package core.pages

import geb.Module
import geb.navigator.Navigator

class NavbarModule extends Module {

	static content = {
		navEditorLink { $("#navEditorLink") }
		navHelpLink { $("#navHelpLink") }
		navAdminLink(required:false) { $("#navAdminLink") }
		navSettingsLink { $("#navSettingsLink") }
		navProfileLink { $("#navProfileLink") }
		navLogoutLink { $("#navLogoutLink") }
		navModuleReferenceLink { $("#navModuleReferenceLink")}
	}

}
