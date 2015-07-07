import pages.*
import spock.lang.*
import core.mixins.ConfirmationMixin

/**
 * Basic features in the backtest views
 */
@Mixin(ConfirmationMixin)
class BasicDateRangeSetSpec extends LoginTester1Spec {
	
	def "basic dateRangeSet flow must work"() {
		when: "navbar settings link is clicked"	
			navbar.navSettingsLink.click()
		then: "dropdown menu must open"
			waitFor { $("#navDateRangeSetLink").displayed }
			
		when: "link is clicked"
			$("#navDateRangeSetLink").click()
		then: "must navigate to dateRangeSet list page"
			waitFor { at DateRangeSetListPage }
			
		when: "create button is clicked"
			createButton.click()
		then: "must navigate to create page"
			waitFor { at DateRangeSetCreatePage }
			
		when: "name is entered and create button is clicked"
			nameInput << "BasicDateRangeSetSpec"
			createButton.click()
		then: "must navigate to show page"
			waitFor { at DateRangeSetShowPage }
			
		when: "date range is entered and add range button is clicked"
			beginDayInput.firstElement().clear()
			beginDayInput << "2014-09-04"
			endDayInput.firstElement().clear()
			endDayInput << "2014-09-05"
			addRangeButton.click()
		then: "the date range must show up"
			waitFor { $(".dateRange").size() == 1 }
			$(".dateRange").text().contains("2014-09-04")
			$(".dateRange").text().contains("2014-09-05")
			
		when: "a single date is entered and add day button is clicked"
			dayInput.firstElement().clear()
			dayInput << "2014-09-06"
			addDayButton.click()
		then: "the date must show up"
			waitFor { $(".dateRange", text:"2014-09-06") }
			$(".dateRange").size() == 2

		when: "the remove button is clicked on a day"
			$(".dateRange", text:"2014-09-06").find(".btn").click()
		then: "that row is removed"
			waitFor { $(".dateRange").size() == 1 }
			
		when: "the delete button is clicked"
			deleteButton.click()
		then: "confirm dialog is shown"
			waitForConfirmation()
			
		when: "confirmation is accepted"
			acceptConfirmation()
		then: "must navigate to list page that shows delete message"
			waitFor { at DateRangeSetListPage }
			$(".alert")
			
	}
	
}
