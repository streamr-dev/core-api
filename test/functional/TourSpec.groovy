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

		advance { dragAndDropModule("Map", 200, -100) }

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

		// TODO: check that map gets data

		waitFor {
			!getTourBubble().displayed
		}
	}

}