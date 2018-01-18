package mixins

import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebDriverException

/**
 * Handle login / logout
 */
trait NotificationMixin {

	def closeNotifications() {
		waitFor {
			!$(".ui-pnotify-closer").each {
				try { it.click() } catch (StaleElementReferenceException | WebDriverException e) {}
			}
			!$(".ui-pnotify").displayed
		}
	}

	def findSuccessNotification() {
		$(".ui-pnotify .alert-success")
	}

	def findErrorNotification() {
		$(".ui-pnotify .alert-danger")
	}

	def waitForSuccessNotification() {
		waitFor {
			findSuccessNotification()[0].displayed
		}
	}

	def waitForErrorNotification() {
		waitFor {
			findErrorNotification()[0].displayed
		}
	}

}
