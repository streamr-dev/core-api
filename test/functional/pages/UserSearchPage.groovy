package pages

class UserSearchPage extends GrailsPage {
    
    static controller = "user"
    static action = "search"

    static url = "$controller/$action"

    static content = {
        username { $("#username") }
        searchButton { $("#search_submit") }
        userList { $("div.list") }
    }
}

