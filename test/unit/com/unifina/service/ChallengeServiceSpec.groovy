package com.unifina.service

import com.unifina.api.ChallengeVerificationFailedException
import com.unifina.security.Challenge
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import spock.lang.Specification

@TestMixin(ControllerUnitTestMixin)
@TestFor(ChallengeService)
class ChallengeServiceSpec extends Specification {
	KeyValueStoreService keyValueStoreService

	void setup() {
		keyValueStoreService = service.keyValueStoreService = Mock(KeyValueStoreService)
	}

	void "should generate challenge"() {
		String address = "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6"
		String text = getChallengeText(address)
		when:
		Challenge challenge = service.createChallenge(address)
		then:
		1 * keyValueStoreService.setWithExpiration(_, _, _)
		challenge.getChallenge().startsWith(text)
		challenge.getId().length() == ChallengeService.CHALLENGE_LENGTH
	}

	void "response to challenge should pass - case 1"() {
		String address = "0x880486b86b86a430813e1a884ebb0641bf5c3514"
		String text = getChallengeText(address)
		Challenge challenge = new Challenge("6AyCyyvQFjtk4fUQyFcIrurHmh13YZ", text, ChallengeService.TTL_SECONDS)
		when:
		String signature = "0x504ad44cec8b06101784544668a28fa756875cc9ce850f2981c07ee6b7ad3ae91958ecb1d804f2e2d8b744669badcd103f9b8b50bc4a4ab887cda05a9d5f20531c"
		service.checkValidChallengeResponse(challenge.getId(), challenge.getChallenge(), signature, address)
		then:
		1 * keyValueStoreService.get(challenge.getId()) >> challenge.challenge
		1 * keyValueStoreService.delete(challenge.getId())
	}

	void "response to challenge should pass - case 2"() {
		String address = "0x71bbf8f66ce4c370073489253809dd6515a879b4"
		String text = getChallengeText(address)
		Challenge challenge = new Challenge("xraT9RmtQEfk6JMdQvGHzKxTYCbAJg", text, ChallengeService.TTL_SECONDS)
		when:
		String signature = "0x1e1883a37b228a6702bdc2e7d25a97f8f75867cdb222f57994cc07fa37cd3c555b500330fc8194694152cea1712a6096ae2095673285a9feffe3dcacca1cc4181c"
		service.checkValidChallengeResponse(challenge.getId(), challenge.getChallenge(), signature, address)
		then:
		1 * keyValueStoreService.get(challenge.getId()) >> challenge.challenge
		1 * keyValueStoreService.delete(challenge.getId())
	}

	void "response to challenge should pass - case 3"() {
		String address = "0xf4458af7ab87db1d68b1bcd524922ed13ff1347f"
		String text = getChallengeText(address)
		Challenge challenge = new Challenge("DglN8UTwVTXjF2AbumvGczdAEHNqYJ", text, ChallengeService.TTL_SECONDS)
		when:
		String signature = "0x1c8d085d4e3101355b07da9cc886ede83171139ee5d27d762a634e6a9c9cb00157016296798942466bbdb777355aeca13a239ae7ac0ba795efcb0419d7fdd0961b"
		service.checkValidChallengeResponse(challenge.getId(), challenge.getChallenge(), signature, address)
		then:
		1 * keyValueStoreService.get(challenge.getId()) >> challenge.challenge
		1 * keyValueStoreService.delete(challenge.getId())
	}

	void "response to challenge should pass - case 4"() {
		String address = "0xF915eD664e43C50eB7b9Ca7CfEB992703eDe55c4"
		String text = getChallengeText(address)
		Challenge challenge = new Challenge("DglN8UTwVTXjF2AbumvGczdAEHNqYJ", text, ChallengeService.TTL_SECONDS)
		when:
		String signature = "0xcb168209298e75f27d3f179d236c07960de39e4b424c51b390306fc6e7e442bb415ecea1bd093320dd91fd9177374828671c972548c52a95179822a014dc84931b"
		service.checkValidChallengeResponse(challenge.getId(), challenge.getChallenge(), signature, address)
		then:
		1 * keyValueStoreService.get(challenge.getId()) >> challenge.challenge
		1 * keyValueStoreService.delete(challenge.getId())
	}

	void "response to challenge should fail"() {
		String address = "0x99a3ae3f5e713f01eca8c2bcf4c32702c2e7ea03"
		String text = getChallengeText(address)
		Challenge challenge = new Challenge("DglN8UTwVTXjF2AbumvGczdAEHNqYJ", text, ChallengeService.TTL_SECONDS)
		when:
		String signature = "0x50ba6f6df25ba593cb8188df29ca27ea0a7cd38fadc4d40ef9fad455117e190f2a7ec880a76b930071205fee19cf55eb415bd33b2f6cb5f7be36f79f740da6e81b"
		service.checkValidChallengeResponse(challenge.getId(), challenge.getChallenge(), signature, address)
		then:
		1 * keyValueStoreService.get(challenge.getId()) >> challenge.challenge
		0 * keyValueStoreService.delete(challenge.getId())
		thrown ChallengeVerificationFailedException
	}

	private static String getChallengeText(String address) {
		String textPart1 = "This is a challenge created by Streamr for address "
		String textPart2 = " to prove private key ownership by signing this random data with it:\n\n"
		return textPart1+address+textPart2
	}

	void "response to challenge should pass - case 1"() {
		when:
		String address = "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6"
		String signature = "0x50ba6f6df25ba593cb8188df29ca27ea0a7cd38fadc4d40ef9fad455117e190f2a7ec880a76b930071205fee19cf55eb415bd33b2f6cb5f7be36f79f740da6e81b"
		Challenge challenge = new Challenge(
			id: "cc_KtW2PQT-ak4VV2DJJjgF48-j7GPQNejd-1dQENE1A",
			challenge: "This is a challenge created by Streamr to prove private key ownership by signing this random data with it:\n\ncc_KtW2PQT-ak4VV2DJJjgF48-j7GPQNejd-1dQENE1A"
		).save(failOnError: true, validate: true)
		then:
		service.verifyChallengeResponse(challenge.id, challenge.challenge, signature, address)
	}

	void "response to challenge should pass - case 2"() {
		when:
		String address = "0x381caa291a8c96f4e1b18fe93dd1742c23e6a5a4"
		String signature = "0x16befea3fb9cc1863c669d8ba735094aa57b9382f618f68904a512b0f57a1b1c203b02836a70b223b26a3e6af0d24f138eab792379825ba44eccbdc7f19919541c"
		Challenge challenge = new Challenge(
			id: "q9iq56WVQN6_AjACbN-vHQzI6P4j99R8qUkMK61C6jHw",
			challenge: "This is a challenge created by Streamr to prove private key ownership by signing this random data with it:\n\nq9iq56WVQN6_AjACbN-vHQzI6P4j99R8qUkMK61C6jHw"
		).save(failOnError: true, validate: true)
		then:
		service.verifyChallengeResponse(challenge.id, challenge.challenge, signature, address)
	}

	void "response to challenge should pass - case 3"() {
		when:
		String address = "0xa50e97f6a98dd992d9ecb8207c2aa58f54970729"
		String signature = "0x4df68b33c0282686a53a9dcb1e157ab7812d59bf30bfdbd2fcc115b1079644b6610bee3bc3b3f80d4b942f941c1667577fd84423ef3e0a79458abc510616acf51c"
		Challenge challenge = new Challenge(
			id: "21kcVxd9Q8OLUA-NVZaUsQn6oULLwORBCK_h-Cp5vT7Q",
			challenge: "This is a challenge created by Streamr to prove private key ownership by signing this random data with it:\n\n21kcVxd9Q8OLUA-NVZaUsQn6oULLwORBCK_h-Cp5vT7Q"
		).save(failOnError: true, validate: true)
		then:
		service.verifyChallengeResponse(challenge.id, challenge.challenge, signature, address)
	}

	void "response to challenge should fail"() {
		when:
		String address = "0x99a3ae3f5e713f01eca8c2bcf4c32702c2e7ea03"
		String signature = "0x50ba6f6df25ba593cb8188df29ca27ea0a7cd38fadc4d40ef9fad455117e190f2a7ec880a76b930071205fee19cf55eb415bd33b2f6cb5f7be36f79f740da6e81b"
		Challenge challenge = new Challenge(
			id: "cc_KtW2PQT-ak4VV2DJJjgF48-j7GPQNejd-1dQENE1A",
			challenge: "This is a challenge created by Streamr to prove private key ownership by signing this random data with it:\n\ncc_KtW2PQT-ak4VV2DJJjgF48-j7GPQNejd-1dQENE1A"
		).save(failOnError: true, validate: true)
		then:
		!service.verifyChallengeResponse(challenge.id, challenge.challenge, signature, address)
	}
}
