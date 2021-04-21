package com.unifina.service

import com.unifina.domain.*
import com.unifina.security.MyPolicy
import com.unifina.security.MySecurityManager
import com.unifina.utils.ApplicationConfig
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

	StreamService streamService
	EthereumIntegrationKeyService ethereumIntegrationKeyService
	PermissionService permissionService

	private static final Logger log = Logger.getLogger(BootService.class)

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
		for (plugin in pm.getAllPlugins()) {
			for (wp in plugin.getWatchedResourcePatterns()) {
				if ("plugins" == wp.getDirectory()?.getName() && "groovy" == wp.getExtension())
					wp.extension = "groovyXX";
			}
		}

		TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

		// Create user roles if not present
		def userRole = Role.findByAuthority('ROLE_USER') ?: new Role(authority: 'ROLE_USER').save(failOnError: true)
		def adminRole = Role.findByAuthority('ROLE_ADMIN') ?: new Role(authority: 'ROLE_ADMIN').save(failOnError: true)
		def liveRole = Role.findByAuthority('ROLE_LIVE') ?: new Role(authority: 'ROLE_LIVE').save(failOnError: true)

		createStorageNodeAssignmentsStream()
	}

	def createStorageNodeAssignmentsStream() {
		String streamId = StorageNodeService.createAssignmentStreamId()
		Stream stream = streamService.getStream(streamId)
		if (stream == null) {
			EthereumAddress nodeAddress = EthereumAddress.fromPrivateKey(ApplicationConfig.getString("streamr.ethereum.nodePrivateKey"))
			User nodeUser = ethereumIntegrationKeyService.getOrCreateFromEthereumAddress(nodeAddress.toString(), SignupMethod.UNKNOWN)
			stream = streamService.createStream(new CreateStreamCommand(id: streamId), nodeUser, null)
			permissionService.systemGrantAnonymousAccess(stream, Permission.Operation.STREAM_GET)
			permissionService.systemGrantAnonymousAccess(stream, Permission.Operation.STREAM_SUBSCRIBE)
		}
	}
}
