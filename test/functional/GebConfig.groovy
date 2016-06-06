/*
 This is the Geb configuration file.
 See: http://www.gebish.org/manual/current/configuration.html
*/


import org.openqa.selenium.Dimension
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver

def env = System.getenv()
def inJenkins = (env['BUILD_NUMBER'] != null)

def sutHost = env['SUT_HOST'] ? env['SUT_HOST'] : 'localhost'
def sutPort = env['SUT_PORT'] ? env['SUT_PORT'] : '8081'

baseUrl = 'http://' + sutHost + ':' + sutPort + '/unifina-core/'
println('Geb baseUrl '+baseUrl)

driver = {
	def dr
	if (inJenkins)
		dr = new RemoteWebDriver(new URL("http://dev.unifina:4444/wd/hub"), DesiredCapabilities.chrome())
	else
		dr = new ChromeDriver()
	// Resolution where everything should be visible
	dr.manage().window().setSize(new Dimension(1280,1024));
	return dr
}

waiting {
	timeout = 10
}