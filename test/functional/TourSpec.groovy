import core.mixins.TourMixin
import spock.lang.*
import core.LoginTester1Spec
import core.mixins.CanvasMixin
import core.mixins.ConfirmationMixin
import core.pages.*

@Mixin(CanvasMixin)
@Mixin(ConfirmationMixin)
@Mixin(TourMixin)
class TourSpec extends LoginTester1Spec {

	def setup(){
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

		advance { dragAndDropModule("Map", 500, -400) }

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
			def ep = getJSPlumbEndpoint(findInput("Map", "id"))
			interact {
				moveToElement(ep)
				release()
			}
		}

		advance {
			connectEndpoints(findOutput("Stream", "lat"), findInput("Map", "latitude"))
			connectEndpoints(findOutput("Stream", "long"), findInput("Map", "longitude"))
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
			connectEndpoints(findOutputByDisplayName("Filter", "out1"), findInputByDisplayName("Table", "in1"))
			connectEndpoints(findOutputByDisplayName("Filter", "out2"), findInputByDisplayName("Table", "in2"))
			connectEndpoints(findOutputByDisplayName("Filter", "out3"), findInputByDisplayName("Table", "in3"))
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
			connectEndpoints(findOutputByDisplayName("Filter", "out3"), findInputByDisplayName("Chart", "in1"))
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

}