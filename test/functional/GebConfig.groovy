/*
 This is the Geb configuration file.
 See: http://www.gebish.org/manual/current/configuration.html
*/

import org.openqa.selenium.Dimension
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver

def env = System.getenv()
def inJenkins = env['BUILD_NUMBER'] != null

String sutHost = env['SUT_HOST'] ? env['SUT_HOST'] : 'localhost'
String sutPort = env['SUT_PORT'] ? env['SUT_PORT'] : '8081'
boolean headless = env['HEADLESS'] != null

String baseUrl = "http://${sutHost}:${sutPort}/streamr-core/"
println("GebConfig ${baseUrl} (headless=${headless})")

driver = {
	def options = new ChromeOptions()
	if (headless) {
		options.addArguments("headless", "disable-gpu") //"remote-debugging-port=9222"
	}
	def dr = new ChromeDriver(options)
	// Resolution where everything should be visible
	dr.manage().window().setSize(new Dimension(1280,1024));
	return dr
}

waiting {
	timeout = 10
}