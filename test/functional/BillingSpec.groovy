import core.LoginTester1Spec
import core.pages.BillingAccountCreatePage
import core.pages.BillingAccountEditPage
import org.openqa.selenium.Keys

public class BillingSpec extends LoginTester1Spec {

	def "user can see billing account view"() {
		when: "billing account page is clicked open"
		$("#navSettingsLink").click()
		$("#navBillingAccountLink").click()
		then: "I should see billing account sign up page"
		waitFor {at BillingAccountCreatePage }

		when: "signup with expired credit card"
		$("#signup_customer_first_name") 		 << "Derp"
		$("#signup_customer_last_name")  		 << "Derpson"
		$("#signup_customer_email")  			 << "derp.derpson@streamr.com"
		$("#creditCardFirstName")  				 << "Derp"
		$("#creditCardLastName")   				 << "Derpson"
		$("#creditCardNumber") 					 << "1"
		$('#signup-form .save').click()
		then: "should receive an error"
		waitFor {at BillingAccountCreatePage }
		$(".alert-danger").text().contains("Credit card: cannot be expired.")

		when: "signup with credit card"
		$("#signup_customer_first_name") 		 << "Derp"
		$("#signup_customer_last_name")  		 << "Derpson"
		$("#signup_customer_email")  			 << "derp.derpson@streamr.com"
		$("#creditCardFirstName")  				 << "Derp"
		$("#creditCardLastName")   				 << "Derpson"
		$("#creditCardNumber")					 << "1"

		//$('#signup_payment_profile_expiration_month').find("option").find{ it.value() == "12"}.click()
		$('input[name="signup[payment_profile][expiration_year]"]')  << "2020"
		$('input[name="signup[payment_profile][expiration_month]"]').value(Keys.chord(Keys.CONTROL, "A")+Keys.BACK_SPACE)
		$('input[name="signup[payment_profile][expiration_month]"]') << "12"

		$('#signup-form .save').click()
		then: "should receive an error"
		waitFor {at BillingAccountEditPage }
		$(".alert").text().contains("Subscription successful!")
	}

	def "user can see updated billing account view"() {
		when: "billing account page is clicked open"
		$("#navSettingsLink").click()
		$("#navBillingAccountLink").click()
		then: "I should see billing account sign up page"
		waitFor { at BillingAccountEditPage }
		$('.panel-title').first().text().contains("Modify Streamr Subscription")
	}




}



