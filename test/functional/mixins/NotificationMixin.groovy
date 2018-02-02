package mixins

import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebDriverException

/**
 * Handle login / logout
 */
trait NotificationMixin {

	def closeNotifications() {
		$(".notification-dismiss").each {
			try { it.click() } catch (StaleElementReferenceException | WebDriverException e) {}
		}
		waitFor {
			!$(".ui-pnotify-closer").each {
				try { it.click() } catch (StaleElementReferenceException | WebDriverException e) {}
			}
			!$(".ui-pnotify").displayed
			!$(".notifications-wrapper .notification").displayed
		}
	}

	def findSuccessNotification() {
		if ($(".ui-pnotify .alert-success")) {
			return $(".ui-pnotify .alert-success")
		} else {
			return $(".notifications-wrapper .notification-success")
		}
	}

	def findErrorNotification() {
		if ($(".ui-pnotify .alert-danger")) {
			return $(".ui-pnotify .alert-danger")
		} else {
			return $(".notifications-wrapper .notification-error")
		}
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
