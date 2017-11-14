package core.pages

class UserSearchResultPage extends GrailsPage {
    
    static controller = "user"
    static action = "userSearch"

    static content = {
        searchResult { $("div.list table tbody tr td a") }
    }
}
