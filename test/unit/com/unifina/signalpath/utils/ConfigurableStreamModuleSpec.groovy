package com.unifina.signalpath.utils

import com.unifina.ModuleTestingSpecification
import com.unifina.domain.data.Stream
import com.unifina.service.StreamService
import com.unifina.utils.Globals

class ConfigurableStreamModuleSpec extends ModuleTestingSpecification {

	Globals globals
	ConfigurableStreamModule module
	StreamService streamService

	def setup() {
		Stream stream = new Stream()
		stream.config = [fields: [[name: "out", type: "string"]]]

		streamService = mockBean(StreamService, Mock(StreamService))
		streamService.getStream(_ as String) >> stream

		module = new ConfigurableStreamModule()
		module.init()
		module.name = "streamModule"
		module.globals = globals = mockGlobals()
	}

	void "configurableStreamModule creates outputs for selected stream on configuration"() {
		when:
		module.configure([
			params: [
				[name: "stream", value: "stream-0"]
			],
		])

		then:
		module.getOutput("out") != null
	}
}