package com.unifina.service

import com.unifina.api.InvalidSessionTokenException
import com.unifina.domain.security.SecUser
import com.unifina.security.SessionToken
import org.joda.time.DateTime

class SessionService {
	static final int TOKEN_LENGTH = 64
	static final int TTL_HOURS = 3

	KeyValueStoreService keyValueStoreService

	SessionToken generateToken(SecUser user) {
		SessionToken sk = new SessionToken(TOKEN_LENGTH, user, TTL_HOURS)
		keyValueStoreService.setWithExpiration(sk.getToken(), sk.getUser().id.toString(), TTL_HOURS * 3600)
		return sk
	}

	SecUser getUserFromToken(String token) throws InvalidSessionTokenException {
		keyValueStoreService.resetExpiration(token, new DateTime().plusHours(TTL_HOURS).toDate())
		String userID = keyValueStoreService.get(token)
		if (userID == null) {
			throw new InvalidSessionTokenException("Invalid token: "+token)
		}
		return SecUser.get(userID.toLong())
	}

	void invalidateSession(String sessionToken) {
		keyValueStoreService.delete(sessionToken)
	}

}
