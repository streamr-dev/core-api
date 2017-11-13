package core.mixins

import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebDriverException

/**
 * Handle login / logout
 */
class NotificationMixin {

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
		$(".ui-pnotify .alert-warning")
	}

	def waitForSuccessNotification() {
		waitFor {
			findSuccessNotification().displayed
		}
	}

	def waitForErrorNotification() {
		waitFor {
			findErrorNotification().displayed
		}
	}

}
