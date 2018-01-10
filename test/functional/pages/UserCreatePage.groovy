package pages

class UserCreatePage extends GrailsPage {
    
    static controller = "user"
    static action = "create"

    static url = "$controller/$action"

    static content = {
        username { $("#username") }
        name { $("#name") }
        password { $("#password") }
        accountExpired { $("#accountExpired") }
        accountLocked { $("#accountLocked") }
        passwordExpired { $("#passwordExpired") }
        timezone { $("#timezone") }

        roleAdmin { $("input", name:"role", value:"ROLE_ADMIN") }
        roleLive { $("input", name:"role", value:"ROLE_LIVE") }
        roleUser { $("input", name:"role", value:"ROLE_USER") }

        modulePackageCore { $("input", name:"modulePackage", value:"1") }
        modulePackageTrading { $("input", name:"modulePackage", value:"2") }
        modulePackageUnifina { $("input", name:"modulePackage", value:"3") }
        modulePackageUnsafe { $("input", name:"modulePackage", value:"4") }
        modulePackageDeprecated { $("input", name:"modulePackage", value:"5") }

        feedUserStream { $("input", name:"feed", value:"7") }

        createButton { $("#create_submit") }
    }
}

