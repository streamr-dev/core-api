package com.unifina.service

import grails.util.Environment

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication

import com.unifina.domain.config.HostConfig
import com.unifina.domain.security.SecRole
import com.unifina.task.TaskMessageListener
import com.unifina.task.TaskWorker
import com.unifina.utils.NetworkInterfaceUtils

/**
 * The onInit and onDestroy methods should be triggered from conf/BootStrap.groovy of the app.
 * This works around the fact that BootStrap.groovy of a plugin can't be executed.
 * @author Henri
 */
class BootService {
	
	def grailsApplication
	def taskService
	def servletContext
	
	private static final Logger log = Logger.getLogger(BootService.class)
	
	boolean isFullEnvironment() {
		return Environment.getCurrent()!=Environment.TEST || System.getProperty("grails.test.phase") == "functional"
	}
	
	def onInit() {
		/**
		 * Workaround for GRAILS-8895
		 * https://jira.grails.org/browse/GRAILS-8895
		 *
		 * Some plugins watch all *.groovy files for changes in the plugins directory,
		 * for example the domainClass plugin. When modifying any non-domain groovy
		 * classes in the core plugin, an exception will be thrown.
		 */
		def pm = org.codehaus.groovy.grails.plugins.PluginManagerHolder.pluginManager;
		for ( plugin in pm.getAllPlugins() ) {
			for ( wp in plugin.getWatchedResourcePatterns() ) {
				if ( "plugins" == wp.getDirectory()?.getName() && "groovy" == wp.getExtension() )
					wp.extension = "groovyXX";
			}
		}
		
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
		
		def config = grailsApplication.config
		def flatConfig = grailsApplication.flatConfig
		
		
		// Create user roles if not present
		def userRole = SecRole.findByAuthority('ROLE_USER') ?: new SecRole(authority: 'ROLE_USER').save(failOnError: true)
		def adminRole = SecRole.findByAuthority('ROLE_ADMIN') ?: new SecRole(authority: 'ROLE_ADMIN').save(failOnError: true)
		def liveRole = SecRole.findByAuthority('ROLE_LIVE') ?: new SecRole(authority: 'ROLE_LIVE').save(failOnError: true)
		
		/**
		 * Create a map for signalPathRunners
		 */
		if (servletContext)
			servletContext["signalPathRunners"] = [:]
		
		/**
		 * Start a number of taskWorkers, specified by system property or config
		 */
		if (isFullEnvironment()) {
			def ip = NetworkInterfaceUtils.getIPAddress()
			log.info("My network interface is: $ip")

			HostConfig taskWorkerConfig = HostConfig.findByHostAndParameter(ip.toString(),"task.workers")
			
			int workerCount
			if (System.getProperty("task.workers")!=null)
				workerCount = Integer.parseInt(System.getProperty("task.workers"))
			else if (taskWorkerConfig!=null)
				workerCount = Integer.parseInt(taskWorkerConfig.value)
			else workerCount = config.unifina.task.workers ?: 0
			
			for (int i=0; i<workerCount; i++) {
				taskService.startTaskWorker()
			}
			log.info("onInit: started $workerCount task workers")
			
			// Start a listener for Task-related events
			servletContext["taskMessageListener"] = new TaskMessageListener(grailsApplication, taskService.getTaskWorkers())
			log.info("onInit: started TaskMessageListener")
		}
		else {
			log.info("onInit: Task workers and listeners not started due to reduced environment: "+Environment.getCurrent()+", grails.test.phase: "+System.getProperty("grails.test.phase"))
		}
	}
	
	def onDestroy() {
		taskService?.stopAllTaskWorkers()

		if (servletContext) {
			servletContext["signalPathRunners"]?.values().each {it.abort()}
			servletContext["taskMessageListener"]?.quit()
			servletContext["realtimeDataSource"]?.stopFeed()
		}
	}
}
