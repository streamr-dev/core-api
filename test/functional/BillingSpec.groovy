import core.LoginTester1Spec
import core.pages.BillingAccountPage
import core.pages.CanvasPage
import core.pages.ChangePasswordPage
import core.pages.LoginPage
import core.pages.ProfileEditPage

public class BillingSpec extends LoginTester1Spec {

	def "user can see billing account view"() {
		when: "billing account page is clicked open"
		$("#navSettingsLink").click()
		$("#navBillingAccountLink").click()
		then: "I should see billing account sign up page"
		waitFor {at BillingAccountPage }

		when: "signup with expired credit card"
		$("#signup_customer_first_name") 		<< "Derp"
		$("#signup_customer_last_name")  		<< "Derpson"
		$("#signup_customer_email")  			<< "derp.derpson@streamr.com"
		$("#signup_payment_profile_first_name")  << "Derp"
		$("#signup_payment_profile_last_name")   << "Derpson"
		$("#signup_payment_profile_card_number") << "1"
		$('#signup-form .save').click()
		then: "should receive an error"
		waitFor {at BillingAccountPage }
		$(".alert-danger").text().contains("Credit card: cannot be expired.")

		when: "signup with credit card"
		$("#signup_customer_first_name") 		 << "Derp"
		$("#signup_customer_last_name")  		 << "Derpson"
		$("#signup_customer_email")  			 << "derp.derpson@streamr.com"
		$("#signup_payment_profile_first_name")  << "Derp"
		$("#signup_payment_profile_last_name")   << "Derpson"
		$("#signup_payment_profile_card_number") << "1"

		$('#signup_payment_profile_expiration_month').find("option").find{ it.value() == "12"}.click()

		$('#signup-form .save').click()
		then: "should receive an error"
		waitFor {at BillingAccountPage }
		$(".alert").text().contains("Subscription successful!")


	}

	def "user can see updated billing account view"() {
		when: "billing account page is clicked open"
		$("#navSettingsLink").click()
		$("#navBillingAccountLink").click()
		then: "I should see billing account sign up page"
		waitFor { at BillingAccountPage }
		$("#subsSliderForm .panel-title").text().contains("Modify Streamr Subscription")
		$("#subsSliderForm legend").text().contains("Select Plan")
	}




}



