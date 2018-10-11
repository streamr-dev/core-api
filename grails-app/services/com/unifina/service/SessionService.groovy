package com.unifina.service

import com.lambdaworks.redis.RedisAsyncConnection
import com.lambdaworks.redis.RedisClient
import com.lambdaworks.redis.RedisConnection
import com.lambdaworks.redis.RedisURI
import com.unifina.domain.security.SecUser
import com.unifina.security.SessionToken
import com.unifina.utils.MapTraversal
import grails.util.Holders
import org.joda.time.DateTime
import org.springframework.util.Assert

class SessionService {
	static final int TOKEN_LENGTH = 64
	static final int TTL_HOURS = 3

	private RedisClient redisClient
	private RedisAsyncConnection<String, String> connection

	SessionToken generateToken(SecUser user) {
		SessionToken sk = new SessionToken(TOKEN_LENGTH, user, TTL_HOURS)
		getConnection().setex(sk.getToken(), TTL_HOURS * 3600, sk.getUser().id.toString())
		return sk
		redisClient.connectAsync().expireat()
	}

	SecUser getUserFromToken(String token) {
		getConnection().expireat(token, new DateTime().plusHours(TTL_HOURS).toDate())
		String userID = getConnection().get(token).get()
		if (userID == null) {
			return null
		}
		return SecUser.get(userID.toLong())
	}

	private RedisAsyncConnection<String, String> getConnection() {
		if (connection == null) {
			List<String> hosts = MapTraversal.getList(Holders.getConfig(), "streamr.redis.hosts");
			String password = MapTraversal.getString(Holders.getConfig(), "streamr.redis.password");

			Assert.notNull(hosts, "streamr.redis.hosts is null!")
			Assert.notEmpty(hosts, "streamr.redis.hosts is empty!")

			redisClient = RedisClient.create(RedisURI.create("redis://" + password + "@" + hosts.get(0)))
			connection = redisClient.connectAsync()
		}
		return connection
	}

}
