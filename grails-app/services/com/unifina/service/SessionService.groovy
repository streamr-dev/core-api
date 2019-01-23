package com.unifina.service

import com.unifina.api.InvalidArgumentsException
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

	void updateUsersLoginDate(SecUser user, Date date) {
		user.lastLogin = date
		user.save(failOnError: true, validate: false)
	}

	SessionToken generateToken(Userish userish) {
		SessionToken sk = new SessionToken(TOKEN_LENGTH, userish, TTL_HOURS)
		keyValueStoreService.setWithExpiration(sk.getToken(), userishToString(userish), TTL_HOURS * 3600)
		if (userish instanceof SecUser) {
			updateUsersLoginDate((SecUser) userish, new Date())
		}
		return sk
	}

	Userish getUserishFromToken(String token) throws InvalidSessionTokenException {
		keyValueStoreService.resetExpiration(token, new DateTime().plusHours(TTL_HOURS).toDate())
		String userishClassAndId = keyValueStoreService.get(token)

		if (userishClassAndId == null) {
			throw new InvalidSessionTokenException("Invalid token: "+token)
		}
		return stringToUserish(userishClassAndId)
	}

	void invalidateSession(String sessionToken) {
		keyValueStoreService.delete(sessionToken)
	}

	String userishToString(Userish u) {
		if (u instanceof SecUser) {
			return "SecUser"+u.id.toString()
		} else if (u instanceof Key) {
			return "Key"+u.id.toString()
		}
		throw new InvalidArgumentsException("Unrecognized userish")
	}

	Userish stringToUserish(String s) {
		if(s.startsWith("SecUser")) {
			String id = s.substring(7)
			return SecUser.get(id)
		} else if (s.startsWith("Key")) {
			String id = s.substring(3)
			return Key.get(id)
		} else {
			throw new InvalidArgumentsException("Unrecognized string")
		}
	}

}
