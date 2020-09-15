package com.unifina.signalpath.utils


import com.unifina.ModuleTestingSpecification
import com.unifina.domain.Stream
import com.unifina.service.StreamService
import com.unifina.utils.Globals

class ConfigurableStreamModuleSpec extends ModuleTestingSpecification {

	Globals globals
	ConfigurableStreamModule module
	StreamService streamService
	Stream stream

	def setup() {
		stream = new Stream()
		stream.config = [fields: [[name: "out", type: "string"]]]
		stream.partitions = 3

		streamService = mockBean(StreamService, Mock(StreamService))
		streamService.getStream(_ as String) >> stream

		module = new ConfigurableStreamModule()
		module.init()
		module.name = "streamModule"
		module.globals = globals = mockGlobals()
	}

	void "it creates outputs for selected stream on configuration"() {
		when:
		module.configure([
			params: [
				[name: "stream", value: "stream-0"]
			],
		])

		then:
		module.getOutput("out") != null
	}

	void "it selects all partitions by default"() {
		when:
		module.configure([
			params: [
				[name: "stream", value: "stream-0"]
			],
		])

		then:
		module.getConfiguration().partitions == [0, 1, 2]
	}

	void "it reads partitions from config"() {
		when:
		module.configure([
			params: [
				[name: "stream", value: "stream-0"]
			],
			partitions: [0, 2]
		])

		then:
		module.getConfiguration().partitions == [0, 2]
	}

	void "getStreamPartitions() defaults to all partitions if no partitions are selected"() {
		when:
		module.configure([
			params: [
				[name: "stream", value: "stream-0"]
			],
			partitions: []
		])

		then:
		module.getStreamPartitions().size() == 3
	}

	void "when stream modules stream is not defined"() {
		when:
		module.configure([
			params: [
				[name: "stream", value: ""]
			],
		])

		then:
		1 * streamService.getStream(_ as String) >> null
		thrown(InvalidStreamConfigException)
	}
}
