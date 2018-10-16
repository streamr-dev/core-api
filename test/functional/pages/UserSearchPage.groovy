package pages

class UserSearchPage extends GrailsPage {
    
    static controller = "me"
    static action = "search"

    static url = "$controller/$action"

    static content = {
        username { $("#username") }
        searchButton { $("#search_submit") }
        userList { $("div.list") }
    }
}

