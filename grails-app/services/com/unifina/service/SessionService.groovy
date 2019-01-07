package com.unifina.service

import com.unifina.api.InvalidSessionTokenException
import com.unifina.domain.security.Key
import com.unifina.domain.security.SecUser
import com.unifina.security.SessionToken
import com.unifina.security.Userish
import org.joda.time.DateTime

class SessionService {
	static final int TOKEN_LENGTH = 64
	static final int TTL_HOURS = 3

	KeyValueStoreService keyValueStoreService

	SessionToken generateToken(Userish userish) {
		SessionToken sk = new SessionToken(TOKEN_LENGTH, userish, TTL_HOURS)
		keyValueStoreService.setWithExpiration(sk.getToken(), userish.getClassAndId(), TTL_HOURS * 3600)
		return sk
	}

	Userish getUserishFromToken(String token) throws InvalidSessionTokenException {
		keyValueStoreService.resetExpiration(token, new DateTime().plusHours(TTL_HOURS).toDate())
		String userishClassAndId = keyValueStoreService.get(token)

		if (userishClassAndId == null) {
			throw new InvalidSessionTokenException("Invalid token: "+token)
		}
		if(userishClassAndId.startsWith("SecUser")) {
			String id = userishClassAndId.substring(7)
			return SecUser.get(id)
		} else if (userishClassAndId.startsWith("Key")) {
			String id = userishClassAndId.substring(3)
			return Key.get(id)
		} else {
			throw new InvalidSessionTokenException("Invalid token: "+token)
		}

	}

	void invalidateSession(String sessionToken) {
		keyValueStoreService.delete(sessionToken)
	}

}
