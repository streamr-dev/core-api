package com.streamr.core.service

import com.streamr.core.domain.User
import org.apache.log4j.Logger

import java.time.Instant
import java.time.temporal.ChronoUnit

class SessionService {
	static final int TOKEN_LENGTH = 64
	static final int TTL_HOURS = 3

	KeyValueStoreService keyValueStoreService

	private static final Logger log = Logger.getLogger(SessionService)

	void updateUsersLoginDate(User user, Date date) {
		// Using update query to avoid StaleObjectStateException in case of concurrent logins.
		// Not unit testable, but there's coverage in end-to-end tests.
		User.executeUpdate("update User u set u.lastLogin = ? where u.id = ?", [date, user.id])
	}

	SessionToken generateToken(User user) {
		SessionToken sk = new SessionToken(TOKEN_LENGTH, user, TTL_HOURS)
		keyValueStoreService.setWithExpiration(sk.getToken(), userToString(user), TTL_HOURS * 3600)
		updateUsersLoginDate((User) user, new Date())
		return sk
	}

	User getUserFromToken(String token) throws InvalidSessionTokenException {
		Date date = Date.from(Instant.now().plus(TTL_HOURS, ChronoUnit.HOURS))
		keyValueStoreService.resetExpiration(token, date)
		String userishClassAndId = keyValueStoreService.get(token)

		if (userishClassAndId == null) {
			throw new InvalidSessionTokenException("Invalid token: " + token)
		}
		return stringToUser(userishClassAndId)
	}

	void invalidateSession(String sessionToken) {
		keyValueStoreService.delete(sessionToken)
	}

	String userToString(User u) {
		if (u == null || u.id == null) {
			throw new InvalidArgumentsException("Unrecognized user")
		}
		return "User" + u.id.toString()
	}

	User stringToUser(String s) {
		if (s.startsWith("User")) {
			String id = s.substring(4)
			return User.get(id)
		} else {
			throw new InvalidArgumentsException("Unrecognized string")
		}
	}
}
