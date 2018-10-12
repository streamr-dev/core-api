package com.unifina.service

import com.lambdaworks.redis.RedisClient
import com.lambdaworks.redis.RedisConnection
import com.lambdaworks.redis.RedisURI
import com.unifina.domain.security.SecUser
import com.unifina.security.SessionToken
import com.unifina.utils.MapTraversal
import grails.test.spock.IntegrationSpec
import grails.util.Holders
import org.springframework.util.Assert

class SessionServiceIntegrationSpec extends IntegrationSpec {

	SessionService service
	RedisConnection<String, String> connection

	void setup() {
		service = Holders.getApplicationContext().getBean(SessionService)

		List<String> hosts = MapTraversal.getList(Holders.getConfig(), "streamr.redis.hosts");
		String password = MapTraversal.getString(Holders.getConfig(), "streamr.redis.password");

		Assert.notNull(hosts, "streamr.redis.hosts is null!")
		Assert.notEmpty(hosts, "streamr.redis.hosts is empty!")

		RedisClient redisClient = RedisClient.create(RedisURI.create("redis://" + password + "@" + hosts.get(0)))
		connection = redisClient.connect()
	}

	void "should generate session token and store it in redis"() {
		when:
		SecUser user = new SecUser(
			id: 123L,
			username: "username",
			password: "password",
			name: "name",
			email: "email@email.com",
			timezone: "timezone"
		).save(failOnError: true, validate: false)
		SessionToken token = service.generateToken(user)
		then:
		connection.get(token.token) == user.id.toString()
		service.getUserFromToken(token.token).id == user.id
	}

	void "should update expiration"() {
		SecUser user = new SecUser(
			id: 123L,
			username: "username",
			password: "password",
			name: "name",
			email: "email@email.com",
			timezone: "timezone"
		).save(failOnError: true, validate: false)
		SessionToken token = service.generateToken(user)
		Long ttl1 = connection.ttl(token.token)
		Thread.sleep(2000)
		when:
		service.getUserFromToken(token.token)
		then:
		Long ttl2 = connection.ttl(token.token)
		ttl2 - ttl1 <= 10
	}

	void "invalidate should delete token from redis"() {
		SecUser user = new SecUser(
			id: 123L,
			username: "username",
			password: "password",
			name: "name",
			email: "email@email.com",
			timezone: "timezone"
		).save(failOnError: true, validate: false)
		SessionToken token = service.generateToken(user)

		when:
		service.invalidateSession(token.token)
		then:
		connection.get(token.token) == null
	}
}
