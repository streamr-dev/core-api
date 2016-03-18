package com.unifina.signalpath.remote

import com.unifina.utils.testutils.FakeConnexorTweetSentiment
import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class ConnexorTweetSentimentModuleSpec extends Specification {

	FakeConnexorTweetSentiment module

	def setup() {
		module = new FakeConnexorTweetSentiment()
		module.init()
		module.configure([:])
	}

	void "connexorTweetSentiment gives the right answer"() {
		when:
		Map inputValues = [
			tweet: ["paska", "hyvä", "ok", "hieman huonohko"],
		]
		Map outputValues = [
			sentiment : [-1, 1, 0.5, -0.5].collect { it?.doubleValue() },

		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
