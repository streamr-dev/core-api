import java.security.Policy
import java.security.Security

import org.codehaus.groovy.grails.plugins.GrailsPlugin

import com.unifina.security.MyPolicy
import com.unifina.security.MySecurityManager
import com.unifina.security.PackageAccessHelper

class UnifinaCoreGrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.2 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "Unifina Core Plugin" // Headline display name of the plugin
    def author = "Henri Pihkala"
    def authorEmail = "henri.pihkala@unifina.com"
    def description = '''\

'''

    // URL to the plugin's documentation
    def documentation = "http://www.unifina.com"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

    def doWithWebDescriptor = { xml ->
		def servletElement = xml.'servlet'
		
		def lastServlet = servletElement[servletElement.size() - 1]
		lastServlet + {
			'servlet' {
				'description'("AtmosphereServlet")
				'servlet-name'("AtmosphereServlet")
				'servlet-class'("org.atmosphere.cpr.AtmosphereServlet")
				'async-supported'("true") // ADDED
				
				'init-param' {
					'param-name'("org.atmosphere.useWebSocket")
					'param-value'("false")
				}
//				The below asyncSupport can be removed when atmosphere is upgraded to most recent 2.x version
				'init-param' {
					'param-name'("org.atmosphere.cpr.asyncSupport")
					'param-value'("com.unifina.atmosphere.FixedTomcat7CometSupport")
				}
				'init-param' {
					'param-name'("org.atmosphere.useNative")
					'param-value'("true")
				}
				'init-param' {
					'param-name'("org.atmosphere.cpr.AtmosphereInterceptor.disableDefaults")
					'param-value'("true")
				}
				'init-param' {
					'param-name'("org.atmosphere.cpr.AtmosphereHandler")
					'param-value'("com.unifina.atmosphere.AtmosphereHandlerPubSub")
				}
				'init-param' {
					'param-name'("org.atmosphere.cpr.broadcasterCacheClass")
					'param-value'("com.unifina.atmosphere.CounterBroadcasterCache")
				}
				'init-param' {
					'param-name'("org.atmosphere.cpr.broadcasterClass")
					'param-value'("com.unifina.atmosphere.MySimpleBroadcaster")
				}
				// ADDED
//				'init-param' {
//					'param-name'("org.atmosphere.cpr.asyncSupport")
//					'param-value'("javax.servlet.AsyncListener")
//				}
				// ADDED
				'init-param' {
					'param-name'("org.atmosphere.useBlocking")
					'param-value'("false")
				}
				
				
				'load-on-startup'("0")
			}
		}
		
		def mappingElement = xml.'servlet-mapping'
		
		def lastMapping = mappingElement[mappingElement.size() - 1]
		lastMapping + {
			'servlet-mapping' {
				'servlet-name'("AtmosphereServlet")
				'url-pattern'("/atmosphere/*")
			}
		}
    }

    def doWithSpring = {

    }
	
    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
		if (!System.securityManager) {
			Security.setProperty("package.access", PackageAccessHelper.getRestrictedPackages().join(","))
			Policy.setPolicy(new MyPolicy())
			System.securityManager = new MySecurityManager();
		}
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
