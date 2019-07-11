package com.unifina.service

import com.streamr.client.StreamrClient
import com.streamr.client.authentication.ApiKeyAuthenticationMethod
import com.streamr.client.options.StreamrClientOptions
import com.unifina.domain.security.Key
import com.unifina.utils.MapTraversal
import grails.util.Holders

class StreamrClientService {

	UserService userService

	StreamrClient getAuthenticatedInstance(Long userIdToAuthenticate) {

		StreamrClientOptions options = new StreamrClientOptions(
			// Uses superpowers to get an API key for the user to authenticate the data
			new ApiKeyAuthenticationMethod(getApiKeyForUser(userIdToAuthenticate))
		)

		options.setRestApiUrl(MapTraversal.getString(Holders.getConfig(), "streamr.api.http.url"))

		String wsUrl = MapTraversal.getString(Holders.getConfig(), "streamr.api.websocket.url")

		// TODO: Remove when Melchior adds this to the Java client
		if (!wsUrl.contains("controlLayerVersion") && !wsUrl.contains("messageLayerVersion")) {
			if (!wsUrl.contains("?")) {
				wsUrl += "?"
			}
			wsUrl += "&controlLayerVersion=1&messageLayerVersion=31"
		}

		options.setWebsocketApiUrl(wsUrl)
		// options.setWebsocketApiUrl(MapTraversal.getString(Holders.getConfig(), "streamr.api.websocket.url"));

		return new StreamrClient(options)
	}

	/**
	 * Returns an API key for the given user. This is used
	 * by Canvases to subscribe to the Streams required by the Canvas.
	 *
	 * Currently, the key is chosen arbitrarily. It would be better
	 * if the user could specify which key to use to run their Canvases
	 * by marking one key as "default", or offering a choice
	 * in Canvas run settings.
	 */
	private String getApiKeyForUser(Long userId) {
		Key key = Key.where {
			user.id == userId
		}.find()

		if (!key) {
			throw new RuntimeException("User does not have an API key! This should not happen!")
		} else {
			return key.id
		}
	}
}
