package com.unifina.service

import com.unifina.domain.*
import com.unifina.utils.ApplicationConfig
import org.apache.log4j.Logger

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
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			void uncaughtException(Thread t, Throwable e) {
				String s = String.format("Thread %s error caught by default thread error handler: %s", t.getName(), e)
				log.error(s)
			}
		})
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

		// Create user roles if not present
		def userRole = Role.findByAuthority('ROLE_USER') ?: new Role(authority: 'ROLE_USER').save(failOnError: true)
		def adminRole = Role.findByAuthority('ROLE_ADMIN') ?: new Role(authority: 'ROLE_ADMIN').save(failOnError: true)
		def liveRole = Role.findByAuthority('ROLE_LIVE') ?: new Role(authority: 'ROLE_LIVE').save(failOnError: true)

		try {
			createStorageNodeAssignmentsStream()
		} catch (Exception e) {
			log.error("failed to create storage node assignment stream.", e)
		}
	}

	def createStorageNodeAssignmentsStream() {
		String streamId = StorageNodeService.createAssignmentStreamId()
		Stream stream = streamService.getStream(streamId)
		if (stream == null) {
			log.info("creating stream for storage node assignments: " + streamId)
			EthereumAddress nodeAddress = EthereumAddress.fromPrivateKey(ApplicationConfig.getString("streamr.ethereum.nodePrivateKey"))
			User nodeUser = ethereumIntegrationKeyService.getOrCreateFromEthereumAddress(nodeAddress.toString(), SignupMethod.UNKNOWN)
			stream = streamService.createStream(new CreateStreamCommand(id: streamId), nodeUser, null)
			permissionService.systemGrantAnonymousAccess(stream, Permission.Operation.STREAM_GET)
			permissionService.systemGrantAnonymousAccess(stream, Permission.Operation.STREAM_SUBSCRIBE)
		}
	}
}
