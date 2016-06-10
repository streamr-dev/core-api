package com.unifina.controller.security

import com.unifina.domain.signalpath.Canvas
import core.LoginTesterAdminSpec
import core.mixins.CanvasMixin
import core.mixins.ConfirmationMixin
import core.pages.*

import java.util.concurrent.TimeUnit

public class ShutdownSpec extends LoginTesterAdminSpec {

	def setupSpec() {
		// @Mixin is buggy, use runtime mixins instead
		this.class.metaClass.mixin(CanvasMixin)
		this.class.metaClass.mixin(ConfirmationMixin)
	}

	void shutdown() {
		// Async callback signaling done is passed as last argument by executeAsyncScript
		// https://groups.google.com/forum/#!topic/geb-user/Lpi_4lroTcQ
		browser.driver.manage().timeouts().setScriptTimeout(20, TimeUnit.SECONDS);
		browser.driver.executeAsyncScript("jQuery.ajax({url: Streamr.createLink('host', 'shutdown'), method: 'POST'}).done(arguments[arguments.length - 1])")
	}

	void startTaskWorker() {
		// Start task worker
		to TaskWorkerPage
		startWorkerButton.click()
		waitFor {
			taskWorkerTable.find("tbody tr td", text: "True").size() > 0
		}
	}

	def "shutdown can stop and restart canvases via task system"() {
		setup:
		startTaskWorker()

		to CanvasPage
		String canvasName = "ShutdownSpec"+System.currentTimeMillis()
		addModule("Button")
		addModule("Count")
		moveModuleBy("Count", 250, 0)
		addModule("Label")
		moveModuleBy("Label", 500, 0)
		connectEndpoints(findOutput("Button", "out"), findInput("Count", "in"))
		connectEndpoints(findOutput("Count", "count"), findInput("Label", "label"))

		ensureRealtimeTabDisplayed()
		saveCanvasAs(canvasName)
		resetAndStartCanvas(true)
		String canvasId = getCanvasId()

		when: "Button clicked"
		findModuleOnCanvas("Button").find(".button-module-button").click()

		then: "Label shows 1"
		waitFor {
			Double.parseDouble(findModuleOnCanvas("Label").find(".modulelabel").text()) == 1D
		}

		when: "Shutdown command given"
		shutdown()

		then: "On task worker page there must be no alive workers"
		to TaskWorkerPage
		waitFor {
			taskWorkerTable.find("tbody tr td", text: "True").size() == 0
			taskWorkerTable.find("tbody tr td", text: "False").size() > 0
		}
		and: "The canvas must be in stopped state"
		waitFor {
			getCanvasState(canvasId) == Canvas.State.STOPPED
		}

		when: "Worker started"
		startTaskWorker()

		then: "Canvas must end up in running state"
		waitFor {
			getCanvasState(canvasId) == Canvas.State.RUNNING
		}
	}

}
