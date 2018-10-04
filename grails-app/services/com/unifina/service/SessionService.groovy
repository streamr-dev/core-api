package com.unifina.service

import com.unifina.domain.security.SessionToken
import org.joda.time.DateTime

class SessionService {
	static final int TTL_HOURS = 3

    SessionToken generateToken(String address) {
		SessionToken sk = new SessionToken(token: "-", expiration: new DateTime().plusHours(TTL_HOURS), associatedAddress: address)
		sk.save()
		sk.token = sk.id
		sk.save()
		return sk
	}
}
