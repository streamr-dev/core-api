package com.unifina.signalpath.blockchain

import com.unifina.ModuleTestingSpecification
import com.unifina.utils.testutils.ModuleTestHelper

class VerifySignatureSpec extends ModuleTestingSpecification {

	def module = setupModule(new VerifySignature())

	String message = "Dear Pacifics user,\n" +
		"in order to track your parcels you need to proof that you own the address below.\n"
	
	void "VerifySignature gives the right answer"() {
		when:
		Map inputValues = [
			signature: [
				"0x5b174c4c4f03e2eefa95a6d10c568e4c8f374f460a9794e9ccb3fec183b687c060d065bb33cbf4271c282d1bdd68f87b0c4f539abcfedea08de36ed56aaf62bc1b", // sender
				"0xcb8e1e80d668f4237e393c68bbd898f990d2d8e498e11abccbdc46f19579c03549cfedd9c56ebfe8cb9a1dcfd53d2e3c5041a295d400fced363a2522e3823daa1c", // courier
				"0x2b5d49483ea1fa356844b442c54740f0503b7177ee79e58c213010ac91ead468119a41c3c1529864cd8b41a491675753832963e592a95f5addf280a155edc3261c", // receiver
				"0x4c3b5382b1d4e424e515e5649d5246baf2250ebcbd651f6ef4adc7f57806d1ed65e514c4c9063245c928a5509cbfb8bbcdc048e315b4becad7c10e6ac5038dcb1c", // creator
				"0xbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", // [error]
			],
			message: [message, message, message, message, message]
		]
		Map outputValues = [
			address: [
				"0x39a8d268f379266cb5839e2f7dd60cbfaba2ba2b",
				"0x23a4c01b0d904a71824bb351ffeeeb3ad07444b0",
				"0x3ffd6691e2c0d277206729e560be0058fe5909ea",
				"0x330895627a316ba97cdc9e7afd17abaa1b5c3d25",
				"0x330895627a316ba97cdc9e7afd17abaa1b5c3d25"
			],
			error: [
			    null,
				null,
				null,
				null,
				'Header byte out of range: -69'
			]
		]
		
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
