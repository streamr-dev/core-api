package com.unifina.service

import com.lambdaworks.redis.RedisClient
import com.lambdaworks.redis.RedisConnection
import com.lambdaworks.redis.RedisURI
import com.unifina.crypto.ECRecover
import com.unifina.security.Challenge
import com.unifina.utils.MapTraversal
import grails.util.Holders
import org.springframework.util.Assert

class ChallengeService {
	static final int TTL_SECONDS = 300
	static final int CHALLENGE_LENGTH = 30

	private RedisClient redisClient
	private RedisConnection<String, String> connection

	Challenge createChallenge() {
		String challengeText = "This is a challenge created by Streamr to prove private key ownership by signing this random data with it:\n\n"
		Challenge ch = new Challenge(challengeText, CHALLENGE_LENGTH, TTL_SECONDS)
		getConnection().setex(ch.id, TTL_SECONDS, ch.challenge)
		return ch
	}

	boolean verifyChallengeResponse(String challengeID, String challenge, String signature, String publicKey) {
		String challengeRedis = getConnection().get(challengeID)
		boolean invalidChallenge = challengeRedis == null || challenge != challengeRedis
		if (invalidChallenge) {
			return false
		}
		byte[] messageHash = ECRecover.calculateMessageHash(challenge)
		String address = ECRecover.recoverAddress(messageHash, signature)
		boolean valid = address.toLowerCase() == publicKey.toLowerCase()
		if (valid) {
			getConnection().del(challengeID)
		}
		return valid
	}

	String verifyChallengeAndGetAddress(String challengeID, String challenge, String signature) {
		String challengeRedis = getConnection().get(challengeID)
		boolean invalidChallenge = challengeRedis == null || challenge != challengeRedis
		if (invalidChallenge) {
			return null
		} else {
			getConnection().del(challengeID)
		}
		byte[] messageHash = ECRecover.calculateMessageHash(challenge)
		return ECRecover.recoverAddress(messageHash, signature)
	}

	private RedisConnection<String, String> getConnection() {
		if (connection == null) {
			List<String> hosts = MapTraversal.getList(Holders.getConfig(), "streamr.redis.hosts");
			String password = MapTraversal.getString(Holders.getConfig(), "streamr.redis.password");

			Assert.notNull(hosts, "streamr.redis.hosts is null!")
			Assert.notEmpty(hosts, "streamr.redis.hosts is empty!")

			redisClient = RedisClient.create(RedisURI.create("redis://" + password + "@" + hosts.get(0)))
			connection = redisClient.connect()
		}
		return connection
	}
}
