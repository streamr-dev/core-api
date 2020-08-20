package com.unifina.signalpath.blockchain

import com.google.gson.JsonParser
import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.signalpath.BooleanOutput
import com.unifina.signalpath.ListOutput
import com.unifina.signalpath.StringOutput
import com.unifina.signalpath.TimeSeriesOutput
import spock.lang.Specification

class EthereumToStreamrTypesSpec extends Specification {
	AbstractSignalPathModule module
	def setup() {
		module = new AbstractSignalPathModule() {
			@Override void sendOutput() {}
			@Override void clearState() {}
		}
	}

	def "ConvertAndSend works for boolean output"() {
		def output = new BooleanOutput(module, "out")
		def input = new JsonParser().parse("true")
		def output2 = new BooleanOutput(module, "out")
		def input2 = new JsonParser().parse("1")

		when:
		EthereumToStreamrTypes.convertAndSend(output, input)
		EthereumToStreamrTypes.convertAndSend(output2, input2)
		then:
		output.getValue() == true
		output2.getValue() == false
	}

	def "ConvertAndSend works for string output"() {
		def output = new StringOutput(module, "out")
		def input = new JsonParser().parse("test")

		when:
		EthereumToStreamrTypes.convertAndSend(output, input)
		then:
		output.getValue() == "test"
	}

	def "ConvertAndSend works for number output"() {
		def output = new TimeSeriesOutput(module, "out")
		def input = new JsonParser().parse("1")

		when:
		EthereumToStreamrTypes.convertAndSend(output, input)
		then:
		output.getValue() == 1
	}

	def "ConvertAndSend works for array output"() {
		def output = new ListOutput(module, "out")
		def input = new JsonParser().parse('["asdf",4,"qwer",true]')

		when:
		EthereumToStreamrTypes.convertAndSend(output, input)
		then:
		output.getValue() == ["asdf", 4, "qwer", true]
	}
}
