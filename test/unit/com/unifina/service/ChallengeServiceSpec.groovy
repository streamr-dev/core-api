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
}
