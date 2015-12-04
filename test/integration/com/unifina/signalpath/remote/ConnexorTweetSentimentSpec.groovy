package com.unifina.signalpath.remote

import grails.test.spock.IntegrationSpec

class ConnexorTweetSentimentSpec extends IntegrationSpec {
	
	ConnexorTweetSentiment cts

	def setup() {
		cts = new ConnexorTweetSentiment()
		cts.init()
	}

    void "it should return positive sentiment with positive tweet"() {
		when:
			cts.getInput("tweet").receive("Cheek on ihana")
			cts.sendOutput()
		then:
			cts.getOutput("sentiment").getValue() == 1
	}

    void "it should return negative sentiment with negative tweet"() {
		when:
			cts.getInput("tweet").receive("Kaikki on paskaa")
			cts.sendOutput()
		then:
			cts.getOutput("sentiment").getValue() == -1
	}

}
