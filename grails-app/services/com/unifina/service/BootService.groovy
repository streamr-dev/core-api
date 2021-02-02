package com.unifina.service

import com.unifina.domain.HostConfig
import com.unifina.domain.Role
import com.unifina.domain.Stream
import com.unifina.domain.User
import com.unifina.domain.SignupMethod
import com.unifina.domain.EthereumAddress
import com.unifina.domain.Permission
import com.unifina.security.MyPolicy
import com.unifina.security.MySecurityManager
import com.unifina.utils.MapTraversal
import grails.util.Environment
import grails.util.Holders
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.plugins.GrailsPluginManager

import java.security.Policy

/**
 * The onInit and onDestroy methods should be triggered from conf/BootStrap.groovy of the app.
 * This works around the fact that BootStrap.groovy of a plugin can't be executed.
 * @author Henri
 */
class BootService {

	def grailsApplication
	def taskService
	NodeService nodeService
	def servletContext
	StreamService streamService
	EthereumIntegrationKeyService ethereumIntegrationKeyService
	PermissionService permissionService

	private static final Logger log = Logger.getLogger(BootService.class)

	boolean isFullEnvironment() {
		return Environment.getCurrent()!=Environment.TEST || System.getProperty("grails.test.phase") == "functional"
	}

	def onInit() {
		// Do not remove these!
		Policy.setPolicy(new MyPolicy())
		System.setSecurityManager(new MySecurityManager())

		/**
		 * Workaround for GRAILS-8895
		 * https://jira.grails.org/browse/GRAILS-8895
		 *
		 * Some plugins watch all *.groovy files for changes in the plugins directory,
		 * for example the domainClass plugin. When modifying any non-domain groovy
		 * classes in the core plugin, an exception will be thrown.
		 */
		GrailsPluginManager pm = Holders.pluginManager
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
		def userRole = Role.findByAuthority('ROLE_USER') ?: new Role(authority: 'ROLE_USER').save(failOnError: true)
		def adminRole = Role.findByAuthority('ROLE_ADMIN') ?: new Role(authority: 'ROLE_ADMIN').save(failOnError: true)
		def liveRole = Role.findByAuthority('ROLE_LIVE') ?: new Role(authority: 'ROLE_LIVE').save(failOnError: true)

		createStorageNodeAssignmentsStream()

		/**
		 * Start a number of taskWorkers, specified by system property or config
		 */
		if (isFullEnvironment()) {
			String ip = nodeService.getIPAddress()
			log.info("Using IP address: $ip")

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
		}
		else {
			log.info("onInit: Task workers and listeners not started due to reduced environment: "+Environment.getCurrent()+", grails.test.phase: "+System.getProperty("grails.test.phase"))
		}
	}

	def createStorageNodeAssignmentsStream() {
		String streamId = getNodeAddress().toString() + "/storage-node-assignments"
		Stream stream = streamService.getStream(streamId)
		if (stream == null) {
			User nodeUser = ethereumIntegrationKeyService.getOrCreateFromEthereumAddress(nodeAddress.toString(), SignupMethod.UNKNOWN)
			stream = streamService.createStream(new CreateStreamCommand(id: streamId), nodeUser, null)
			permissionService.systemGrantAnonymousAccess(stream, Permission.Operation.STREAM_GET)
			permissionService.systemGrantAnonymousAccess(stream, Permission.Operation.STREAM_SUBSCRIBE)
		}
	}

	def getNodeAddress() {
		String nodePrivateKey = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.nodePrivateKey")
		return EthereumAddress.fromPrivateKey(nodePrivateKey)
	}
}
