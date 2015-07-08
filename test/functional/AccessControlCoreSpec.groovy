import spock.lang.*
import core.LoginTester2Spec;
import core.mixins.ConfirmationMixin
import core.pages.*

/**
 * Basic features in the backtest views
 */
@Mixin(ConfirmationMixin)
class AccessControlCoreSpec extends LoginTester2Spec {

	
	void checkDenied(String url) {
		go url
		waitFor { at AccessDeniedPage }
	}
	
	void checkEmpty(String url) {
		go url
		waitFor { $("body").text()=="" }
	}
	
	def "search won't show modules the user doesn't have access to"() {
		when: "the name of a forbidden module is entered"
			search << "orderage"
		then: "no search results are displayed"
			$('.tt-suggestion').size()==0
	}
	
	def "user cannot access admin pages"() {
		when: "user logins"
			at CanvasPage
		then: "Admin dropdown menu must not be displayed"
			!navbar.navAdminLink
		
		expect:
		checkDenied "feedFile"
		checkDenied "kafka/collect"
		checkDenied "taskWorker/status"
		checkDenied "securityManager"
		
	}
	
	def "user cannot load other users' SavedSignalPaths"() {
		expect:
		checkEmpty "savedSignalPath/load/1"
	}
	

	def "user cannot view other people's Accounts"() {
		expect:
		checkDenied "live/show/13"
		
	}

	def "user cannot add modules the user doesn't have access to"() {
		go "module/jsonGetModule/22"
		waitFor { $("body").text().contains("error") }
	}

}
