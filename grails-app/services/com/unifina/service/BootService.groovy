package com.unifina.service

import grails.util.Environment

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.web.context.support.WebApplicationContextUtils

import com.unifina.domain.config.HostConfig
import com.unifina.domain.security.SecRole
import com.unifina.task.TaskMessageListener
import com.unifina.task.TaskWorker
import com.unifina.utils.NetworkInterfaceUtils

/**
 * The onInit and onDestroy methods should be triggered from conf/BootStrap.groovy of the app.
 * This works around the fact that BootStrap.groovy of a plugin can't be executed.
 * @author admin
 *
 */
class BootService {
	
	def grailsApplication
	def feedService
	def servletContext
	
	def onInit() {
		mergeDefaultConfig(grailsApplication)
		
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
		servletContext["signalPathRunners"] = [:]
		
		/**
		 * Read some host-specific config from the database
		 */
		
		def ip = NetworkInterfaceUtils.getIPAddress()
		println "My network interface is: $ip"
		
		// Start a number of taskWorkers, specified by system property or config
		def taskWorkers = []
		servletContext["taskWorkers"] = taskWorkers
		
		HostConfig taskWorkerConfig = HostConfig.findByHostAndParameter(ip.toString(),"task.workers")
		
		int workerCount
		if (System.getProperty("task.workers")!=null)
			workerCount = Integer.parseInt(System.getProperty("task.workers"))
		else if (taskWorkerConfig!=null)
			workerCount = Integer.parseInt(taskWorkerConfig.value)
		else workerCount = config.unifina.task.workers ?: 0
		
		for (int i=1;i<=workerCount;i++) {
			TaskWorker worker = new TaskWorker(grailsApplication,i)
			worker.start()
			taskWorkers.add(worker)
		}
		
		// Start a listener for Task-related events
		servletContext["taskMessageListener"] = new TaskMessageListener(grailsApplication, taskWorkers)
	}
	
	// Run from UnifinaCoreGrailsPlugin.groovy -> doWithSpring
	// as well as some unit tests that require config.
	// from http://swestfall.blogspot.fi/2011/08/grails-plugins-and-default-configs.html
	static void mergeDefaultConfig(GrailsApplication app) {
		ConfigObject currentConfig = app.config
		ConfigSlurper slurper = new ConfigSlurper(Environment.getCurrent().getName());
		ConfigObject secondaryConfig = slurper.parse(app.classLoader.loadClass("UnifinaCoreDefaultConfig"))
		
		ConfigObject config = new ConfigObject();
		config.putAll(secondaryConfig.merge(currentConfig))
		
		app.config = config;
	}
	
	def onDestroy() {
		servletContext["signalPathRunners"]?.values().each {it.abort()}
		servletContext["taskWorkers"]?.each {it.quit()}
		servletContext["taskMessageListener"]?.quit()
		servletContext["realtimeDataSource"]?.stopFeed()
	}
}
