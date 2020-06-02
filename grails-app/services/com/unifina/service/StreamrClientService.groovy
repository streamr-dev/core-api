package com.unifina.service


import com.streamr.client.StreamrClient
import com.streamr.client.authentication.ApiKeyAuthenticationMethod
import com.streamr.client.authentication.AuthenticationMethod
import com.streamr.client.authentication.InternalAuthenticationMethod
import com.streamr.client.options.EncryptionOptions
import com.streamr.client.options.SigningOptions
import com.streamr.client.options.StreamrClientOptions
import com.unifina.domain.security.Key
import com.unifina.utils.MapTraversal
import grails.util.Holders
import org.apache.log4j.Logger

import java.lang.reflect.Constructor
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.ReentrantLock

class StreamrClientService {

	EthereumIntegrationKeyService ethereumIntegrationKeyService
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
	 * Returns a StreamrClient instance, authenticated with one of the provided user's
	 * API keys. This method fetches from the centralized database an API key to be
	 * used with the StreamrClient.
	 *
	 * Whoever calls this should take care of closing the client when it is no longer needed.
	 */
	StreamrClient getAuthenticatedInstance(Long userIdToAuthenticate) {
		// Uses superpowers to get an API key for the user to authenticate the data
		return createInstance(new ApiKeyAuthenticationMethod(getApiKeyForUser(userIdToAuthenticate)))
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
					String nodePrivateKey = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.nodePrivateKey")
					log.debug("Creating StreamrClient instance for this Engine node. Using private key ${nodePrivateKey?.substring(0, 2)}...")

					// Create a custom EthereumAuthenticationMethod which doesn't call the API, but instead uses the internal services to
					// get a sessionToken. Calling the API here can lead to a deadlock situation in some corner cases, because the
					// service calls "itself" while blocking in a mutex-lock.
					InternalAuthenticationMethod authenticationMethod = new InternalAuthenticationMethod(nodePrivateKey, ethereumIntegrationKeyService, sessionService)
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
		String websocketUrl = MapTraversal.getString(Holders.getConfig(), "streamr.api.websocket.url")
		String restUrl = MapTraversal.getString(Holders.getConfig(), "streamr.api.http.url")
		log.info(String.format("Creating StreamrClient instance. Websocket: %s, REST: %s", websocketUrl, restUrl))

		StreamrClientOptions options = new StreamrClientOptions(
			authenticationMethod,
			SigningOptions.getDefault(),
			EncryptionOptions.getDefault(),
			websocketUrl,
			restUrl
		)
		return clientConstructor.newInstance(options)
	}

	/**
	 * Returns an API key for the given user. This is used
	 * by Canvases to subscribe to the Streams required by the Canvas.
	 *
	 * Currently, the first returned key is chosen. It would be better
	 * if the user could specify which key to use to run their Canvases
	 * by marking one key as "default", or offering a choice
	 * in Canvas run settings.
	 */
	private String getApiKeyForUser(Long userId) {
		List<Key> keys = Key.createCriteria().list {
			user {
				idEq(userId)
			}
		}
		if (keys.isEmpty()) {
			throw new IllegalStateException("User does not have an API key! This should not happen!")
		} else {
			return keys[0].id
		}
	}
}
