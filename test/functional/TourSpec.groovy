import LoginTester1Spec
import mixins.CanvasMixin
import mixins.ConfirmationMixin
import mixins.TourMixin
import pages.CanvasPage

class TourSpec extends LoginTester1Spec implements CanvasMixin, ConfirmationMixin, TourMixin {

	def setup() {
		at CanvasPage
	}

	def "The first tour must start if it has not been completed"() {
		setTourIncomplete(0)
		when:
		js.exec("location.reload()")

		then:
		waitFor {
			getTourBubble().displayed
			getState().contains("0:0")
		}
	}

	def "tour 0 must not autostart if completed, but must start from help menu"() {
		when:
		setTourComplete(0)
		js.exec("location.reload()")
		Thread.sleep(3000)

		then:
		!getTourBubble().displayed

		when: "tour 0 is started from menu"
		startTourFromHelpMenu(0)

		then: "bubble must appear"
		waitFor {
			$('div.hopscotch-bubble').displayed
			getState().contains("0:0")
		}

	}

	def "tour 0 must not start anymore if closed using the close button"() {
		setTourIncomplete(0)

		when:
		js.exec("location.reload()")

		then:
		waitFor {
			getTourBubble().displayed
		}

		when:
		getTourBubble().find(".hopscotch-bubble-close").click()

		then:
		!getTourBubble().displayed

		when:
		js.exec("location.reload()")
		Thread.sleep(3000)

		then:
		!getTourBubble().displayed
	}

	def "tour 0"() {
		startTourFromHelpMenu(0)

		expect:
		waitFor {
			getTourBubble().displayed
			getTourBubble().find('.hopscotch-next').displayed
		}
		repeatNextStep()

		advance { searchAndClickContains("public trans") }

		advance { selectCategoryInModuleBrowser("Visualizations") }

		advance { dragAndDropModule("Map (geo)", 500, -400) }

		advance {
			// Start dragging
			def ep = getJSPlumbEndpoint(findOutput("Stream", "veh"))
			interact {
				clickAndHold(ep)
				moveByOffset(50, 0)
			}
		}

		advance {
			// Drop on endpoint
			def ep = getJSPlumbEndpoint(findInput("Map (geo)", "id"))
			interact {
				moveToElement(ep)
				release()
			}
		}

		advance {
			connectEndpoints(findOutput("Stream", "lat"), findInput("Map (geo)", "latitude"))
			connectEndpoints(findOutput("Stream", "long"), findInput("Map (geo)", "longitude"))
		}

		advance {
			runHistoricalButton.click()
		}

		waitFor {
			$(".leaflet-marker-icon").size() > 45
		}

		advance {
			runHistoricalButton.click()
		}

		waitFor {
			atEndOfTour()
		}
	}

	def "tour 1"() {
		startTourFromHelpMenu(1)

		expect:
		waitFor {
			getTourBubble().displayed
			getTourBubble().find('.hopscotch-next').displayed
		}
		repeatNextStep()

		advance { searchAndClickContains("public trans") }

		advance { selectCategoryInModuleBrowser("Utils") }

		advance { dragAndDropModule("Filter", 500, -400) }

		advance {
			searchForModule("Text")
			clickSearchResult("TextEquals")
		}

		moveModuleBy("TextEquals", 250, 50, 0, true)

		advance {
			// Start dragging
			def ep = getJSPlumbEndpoint(findOutput("Stream", "veh"))
			interact {
				clickAndHold(ep)
				moveByOffset(50, 0)
			}
		}

		advance {
			// Drop on endpoint
			def ep = getJSPlumbEndpoint(findInput("TextEquals", "text"))
			interact {
				moveToElement(ep)
				release()
			}
		}

		advance {
			// Start dragging
			def ep = getJSPlumbEndpoint(findOutput("TextEquals", "equals?"))
			interact {
				clickAndHold(ep)
				moveByOffset(50, 0)
			}
		}

		advance {
			// Drop on endpoint
			def ep = getJSPlumbEndpoint(findInput("Filter", "pass"))
			interact {
				moveToElement(ep)
				release()
			}
		}

		advance {
			setParameterValueForModule("TextEquals", "search", "RHKL00112")
		}

		advance {
			connectEndpoints(findOutput("Stream", "lat"), findInputByDisplayName("Filter", "in1"))
		}

		advance {
			connectEndpoints(findOutput("Stream", "long"), findInputByDisplayName("Filter", "in2"))
		}

		advance {
			connectEndpoints(findOutput("Stream", "spd"), findInputByDisplayName("Filter", "in3"))
		}

		advance {
			searchAndClick("Table")
		}

		moveModuleBy("Table", 600, 150)

		advance {
			connectEndpoints(findOutputByDisplayName("Filter", "lat"), findInputByDisplayName("Table", "in1"))
			connectEndpoints(findOutputByDisplayName("Filter", "long"), findInputByDisplayName("Table", "in2"))
			connectEndpoints(findOutputByDisplayName("Filter", "spd"), findInputByDisplayName("Table", "in3"))
		}

		advance {
			runHistoricalButton.click()
		}

		waitFor(30) {
			$(".event-table-module-content tbody tr").size() >= 20
		}

		advance {
			runHistoricalButton.click()
		}

		sleep(1000)

		advance {
			closeModule("Table")
		}

		advance {
			searchAndClick("Chart")
		}

		moveModuleBy("Chart", 600, 150)

		advance {
			connectEndpoints(findOutputByDisplayName("Filter", "spd"), findInputByDisplayName("Chart", "in1"))
		}

		advance {
			runHistoricalButton.click()
		}

		sleep(1000)

		waitFor {
			!$(".chartDrawArea div").isEmpty() // test that anything is added to Chart module's body
		}

		advance {
			runHistoricalButton.click()
		}

		waitFor {
			atEndOfTour()
		}
	}

	def "tour 2"() {
		startTourFromHelpMenu(2)

		expect:
		waitFor {
			getTourBubble().displayed
			getTourBubble().find('.hopscotch-next').displayed
		}
		repeatNextStep()

		advance { searchAndClickContains("Twitter-Bitcoin") }

		advance { searchAndClickContains("Table") }

		moveModuleBy("Table", 200, 200)

		advance {
			connectEndpoints(findOutputByDisplayName("Stream", "text"), findInputByDisplayName("Table", "in1"))
			connectEndpoints(findOutputByDisplayName("Stream", "retweet_count"), findInputByDisplayName("Table", "in2"))
			connectEndpoints(findOutputByDisplayName("Stream", "favorite_count"), findInputByDisplayName("Table", "in3"))
			connectEndpoints(findOutputByDisplayName("Stream", "lang"), findInputByDisplayName("Table", "in4"))
		}

		advance {
			ensureRealtimeTabDisplayed()
		}

		advance {
			startCanvas(true)
		}

		waitFor(30) {
			$(".event-table-module-content tbody tr").size() >= 2
		}

		advance {
			openUpStopConfirmation()
		}

		waitFor {
			getTourBubble().displayed
		}

		advance {
			acceptStopConfirmationAndWaitForStopped()
		}

		advance {
			closeModule("Table")
		}

		advance { searchAndClickContains("Count") }

		moveModuleBy("Count", 200, 25)

		advance {
			setParameterValueForModule("Count", "windowLength", "1")
		}

		advance {
			chooseDropdownParameterForModule("Count", "windowType", "minutes")
		}

		advance {
			connectEndpoints(findOutput("Stream", "text"), findInput("Count", "in"))
		}

		advance { searchAndClickContains("GreaterThan") }

		moveModuleBy("GreaterThan", 400, 25)

		advance {
			connectEndpoints(findOutput("Count", "count"), findInput("GreaterThan", "A"))
		}

		advance { searchAndClickContains("Constant") }

		moveModuleBy("Constant", 600, 25)

		advance {
			setParameterValueForModule("Constant", "constant", "350")
		}

		advance {
			connectEndpoints(findOutput("Constant", "out"), findInput("GreaterThan", "B"))
		}

		advance { searchAndClickContains("Filter") }

		moveModuleBy("Filter", 800, 25)

		advance {
			connectEndpoints(findOutput("GreaterThan", "A&gt;B"), findInput("Filter", "pass"))
		}

		advance {
			connectEndpoints(findOutput("GreaterThan", "A&gt;B"), findInputByDisplayName("Filter", "in1"))
		}

		advance { searchAndClickContains("Email") }

		moveModuleBy("Email", 1000, 25)

		advance {
			setParameterValueForModule("Email", "subject", "alert")
		}

		advance {
			connectEndpoints(findOutputByDisplayName("Filter", "A&gt;B"), findInputByDisplayName("Email", "value1"))
		}

		advance {
			$(".tourGreaterThan1 .ioSwitch.noRepeat.ioSwitchFalse").click()
		}

		advance { searchAndClickContains("Table") }

		moveModuleBy("Table", 500, 500)

		advance {
			connectEndpoints(findOutput("Count", "count"), findInputByDisplayName("Table", "in1"))
		}

		advance {
			connectEndpoints(findOutput("GreaterThan", "A&gt;B"), findInputByDisplayName("Table", "in2"))
		}

		advance {
			startCanvas(true)
		}

		waitFor(30) {
			$(".event-table-module-content tbody tr").size() >= 5
		}

		advance {
			openUpStopConfirmation()
		}

		waitFor {
			getTourBubble().displayed
		}

		advance {
			acceptStopConfirmationAndWaitForStopped()
		}

		cleanup:
		stopCanvasIfRunning()
	}

}