package com.unifina.service

import com.streamr.client.StreamrClient
import com.streamr.client.authentication.AuthenticationMethod
import com.streamr.client.authentication.InternalAuthenticationMethod
import com.streamr.client.options.EncryptionOptions
import com.streamr.client.options.SigningOptions
import com.streamr.client.options.StreamrClientOptions
import com.unifina.domain.SignupMethod
import com.unifina.utils.ApplicationConfig
import org.apache.log4j.Logger

import java.lang.reflect.Constructor
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.ReentrantLock

class StreamrClientService {

	EthereumUserService ethereumUserService
	SessionService sessionService

	private static final Logger log = Logger.getLogger(StreamrClientService)

	private StreamrClient instanceForThisEngineNode
	private final ReentrantLock instanceForThisEngineNodeLock = new ReentrantLock()

	private Constructor<StreamrClient> clientConstructor

	StreamrClientService() {
		clientConstructor = StreamrClient.class.getConstructor(StreamrClientOptions)
	}

	/**
	 * Useful for testing, this method allows a StreamrClient class with some mocked methods to be passed
	 */
	void setClientClass(Class<StreamrClient> streamrClientClass) {
		if (instanceForThisEngineNode) {
			throw new IllegalStateException("StreamrClient instance has already been created. Call setClientClass() before calling getInstanceForThisEngineNode()!")
		}
		clientConstructor = streamrClientClass.getConstructor(StreamrClientOptions)
	}

	/**
	 * Returns a shared StreamrClient instance authenticated with the private key
	 * of this Engine node. Other services needing to publish messages as this node
	 * may use this instance to do so.
	 *
	 * This is a long running instance which should not be closed as long as this
	 * application is running. Whoever calls this method should not close the instance.
	 *
	 * This method is thread-safe
	 */
	StreamrClient getInstanceForThisEngineNode() {
		// Mutex lock with timeout to avoid race conditions
		if (instanceForThisEngineNodeLock.tryLock(3L, TimeUnit.SECONDS)) {
			try {
				if (!instanceForThisEngineNode) {
					String nodePrivateKey = ApplicationConfig.getString("streamr.ethereum.nodePrivateKey")
					log.debug("Creating StreamrClient instance for this Engine node. Using private key ${nodePrivateKey?.substring(0, 2)}...")

					// Create a custom EthereumAuthenticationMethod which doesn't call the API, but instead uses the internal services to
					// get a sessionToken. Calling the API here can lead to a deadlock situation in some corner cases, because the
					// service calls "itself" while blocking in a mutex-lock.
					InternalAuthenticationMethod authenticationMethod = new InternalAuthenticationMethod(nodePrivateKey, ethereumUserService, sessionService, SignupMethod.API)
					instanceForThisEngineNode = createInstance(authenticationMethod)
					// Make sure the instance is authenticated before returning
					instanceForThisEngineNode.getSessionToken()
					log.debug("StreamrClient instance created. State: ${instanceForThisEngineNode.getState()}")
				}
				return instanceForThisEngineNode
			} finally {
				instanceForThisEngineNodeLock.unlock()
			}
		} else {
			throw new TimeoutException("Timed out waiting for the lock for this Engine node's StreamrClient instance!")
		}
	}

	private StreamrClient createInstance(AuthenticationMethod authenticationMethod) {
		String websocketUrl = ApplicationConfig.getString("streamr.api.websocket.url")
		String restUrl = ApplicationConfig.getString("streamr.api.http.url")
		log.info(String.format("Creating StreamrClient instance. Websocket: %s, REST: %s", websocketUrl, restUrl))

		StreamrClientOptions options = new StreamrClientOptions(
			authenticationMethod,
			SigningOptions.getDefault(),
			EncryptionOptions.getDefault(),
			websocketUrl,
			restUrl
		)
		options.setMainnetRpcUrl(ApplicationConfig.getString("streamr.ethereum.networks.${ApplicationConfig.getString("streamr.ethereum.defaultNetwork")}"))
		options.setSidechainRpcUrl(ApplicationConfig.getString("streamr.ethereum.networks.${ApplicationConfig.getString("streamr.dataunion.sidechainName")}"))
		options.setDataUnionMainnetFactoryAddress(ApplicationConfig.getString("streamr.dataunion.mainnet.factory.address"))
		options.setDataUnionSidechainFactoryAddress(ApplicationConfig.getString("streamr.dataunion.sidechain.factory.address"))
		return clientConstructor.newInstance(options)
	}
}
