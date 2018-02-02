import LoginTester2Spec
import mixins.ConfirmationMixin
import pages.AccessDeniedPage
import pages.CanvasPage
import pages.LoginPage

/**
 * Basic features in the backtest views
 */
class AccessControlCoreSpec extends LoginTester2Spec implements ConfirmationMixin {

	void checkDenied(String url) {
		go url
		waitFor { at AccessDeniedPage }
	}
	
	def "search won't show modules the user doesn't have access to"() {
		when: "the name of a forbidden module is entered"
			search << "GroovyModule"
		then: "no search results are displayed"
			$('.streamr-search-suggestion').size()==0
	}
	
	def "search won't show other users streams"() {
		when: "the name of a forbidden stream is entered"
			search << "AccessControlCoreSpec"
			Thread.sleep(2000)
		then: "no search results are displayed"
			$('.streamr-search-suggestion').size()==0
	}
	
	def "user cannot access admin pages"() {
		when: "user logins"
			at CanvasPage
		then: "Admin dropdown menu must not be displayed"
			!navbar.navAdminLink
		
		expect:
		checkDenied "kafka/collect"
		checkDenied "taskWorker/status"
		checkDenied "securityManager"
		
	}
	
	def "anonymous users must be directed to login page when accessing protected resources"() {
		when: "user logs out"
			navbar.navSettingsLink.click()
			$("#navLogoutLink").click() // for some reason navbar.navSettingsLink did not work
		then: "must go to login page"
			at LoginPage
		
		when: "navigating to protected resource"
			go "savedSignalPath/load/1"
		then: "must go to login page"
			at LoginPage
			
		when: "navigating to admin-only resource"
			go "kafka/collect"
		then: "must go to login page"
			at LoginPage
	}
	
	def "user cannot load other users' SavedSignalPaths"() {
		expect:
		checkDenied "savedSignalPath/load/1"
	}
	

	def "user cannot view other people's RunningSignalPaths"() {
		expect:
		checkDenied "live/show/13"
		
	}

	def "user cannot add modules the user doesn't have access to"() {
		go "module/jsonGetModule/22"
		waitFor { $("body").text().contains("error") }
	}

	
	def "user doesn't see Live tabs in loadBrowser without ROLE_LIVE role"() {
		when: "user clicks load button"
			at CanvasPage
			loadButton.click()
		then: "load browser is opened with archive content"
			waitFor {
				$('#archiveLoadBrowser')
			}
		then: "Live tab must not be displayed"
			$("#archiveLoadBrowser").parents(".remote-tabs-content").find("ul.nav-tabs li").size()==2
			$("#archiveLoadBrowser").parents(".remote-tabs-content").find("ul.nav-tabs li", text:"Live").size()==0
	}
}
