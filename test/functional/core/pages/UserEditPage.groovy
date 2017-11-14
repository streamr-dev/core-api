package core.pages

import geb.Page

class UserEditPage extends GrailsPage {
    
    static controller = "user"
    static action = "edit"

    static url = "$controller/$action"

    static content = {
        username { $("#username") }
        password { $("#password") }
        saveButton { $("#update_submit") }
        deleteButton { $("#deleteButton") }
        confirmationBox { $(".ui-dialog", "aria-describedby": "deleteConfirmDialog") }
        deleteConfirmButton { $(".ui-dialog span.ui-button-text", text:"Delete") }
		logoutLink { $("#logout-link") }
    }
}

