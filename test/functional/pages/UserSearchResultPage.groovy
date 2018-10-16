package pages

class UserSearchResultPage extends GrailsPage {
    
    static controller = "me"
    static action = "userSearch"

    static content = {
        searchResult { $("div.list table tbody tr td a") }
    }
}
