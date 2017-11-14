package core.mixins

import java.util.concurrent.TimeUnit

/**
 * Helper methods for functional testing tours.
 */
class TourMixin {

	def advance(Closure c, waitingTime = 10) {
		def oldState = getState()
		c()
		waitFor(waitingTime) { oldState != getState() }
		repeatNextStep()
		return true
	}

	def getTourBubble() {
		return $('div.hopscotch-bubble')
	}

	def setTourIncomplete(int tour) {
		// Async callback signaling done is passed as last argument by executeAsyncScript
		// https://groups.google.com/forum/#!topic/geb-user/Lpi_4lroTcQ
		browser.driver.manage().timeouts().setScriptTimeout(2, TimeUnit.SECONDS);
		browser.driver.executeAsyncScript("jQuery.ajax({method:'GET', url:Streamr.createLink('tourUser', 'reset'), data: {id:$tour}}).done(arguments[arguments.length - 1])")
	}

	def setTourComplete(int tour) {
		// Async callback signaling done is passed as last argument by executeAsyncScript
		// https://groups.google.com/forum/#!topic/geb-user/Lpi_4lroTcQ
		browser.driver.manage().timeouts().setScriptTimeout(2, TimeUnit.SECONDS);
		browser.driver.executeAsyncScript("jQuery.ajax({method:'POST', url:Streamr.createLink('tourUser', 'completed'), data: {tourNumber:$tour}}).done(arguments[arguments.length - 1])")
	}

	def startTourFromHelpMenu(int tour) {
		$("#navHelpLink").click()
		$("#help-tour-list a").getElement(tour).click()
	}

	def getState() {
		return js.exec("return hopscotch.getState()")
	}

	def repeatNextStep() {
		while (hasNextButton()) {
			nextStep()
		}
		return true
	}

	def nextStep() {
		def oldState = getState()
		waitFor { $("div.hopscotch-bubble:not(.hide) .next").displayed }
		$("div.hopscotch-bubble:not(.hide) .next").click()
		waitFor { oldState != getState() }
		return true
	}

	def hasNextButton() {
		def element = $("div.hopscotch-bubble:not(.hide) .next")
		return element && element.text() != "Begin"
	}

	boolean atEndOfTour() {
		def element = $("div.hopscotch-bubble:not(.hide) .next")
		return element && element.text() == "Begin"
	}

}
