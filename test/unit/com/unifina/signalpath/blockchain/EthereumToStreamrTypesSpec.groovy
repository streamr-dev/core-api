package com.unifina.signalpath.blockchain

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.unifina.signalpath.BooleanOutput
import com.unifina.signalpath.ListOutput
import com.unifina.signalpath.StringOutput
import com.unifina.signalpath.TimeSeriesOutput
import spock.lang.Specification

class EthereumToStreamrTypesSpec extends Specification {
	def "ConvertAndSend works for boolean output"() {
		def output = new BooleanOutput(module, "out")
		def input = new JsonParser().parse("1")

		when:
		module.convertAndSend(output, input)
		then:
		output.getValue() == true
	}

	def "ConvertAndSend works for string output"() {
		def output = new StringOutput(module, "out")
		def input = new JsonParser().parse("test")

		when:
		module.convertAndSend(output, input)
		then:
		output.getValue() == "test"
	}

	def "ConvertAndSend works for number output"() {
		def output = new TimeSeriesOutput(module, "out")
		def input = new JsonParser().parse("1")

		when:
		module.convertAndSend(output, input)
		then:
		output.getValue() == 1
	}

	def "ConvertAndSend works for array output"() {
		def output = new ListOutput(module, "out")
		def input = new JsonParser().parse('["asdf":4,"qwer":"zxcv"]')

		when:
		module.convertAndSend(output, input)
		then:
		output.getValue() == [
		    asdf: 4,
			qwer: "zxcv"
		]
	}
}
