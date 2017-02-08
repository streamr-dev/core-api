package com.unifina.signalpath

import com.unifina.datasource.RealtimeDataSource
import com.unifina.domain.security.SecUser
import com.unifina.utils.Globals
import com.unifina.utils.testutils.FakePushChannel
import groovy.transform.CompileStatic
import org.apache.log4j.Level
import org.apache.log4j.Logger
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.TimeUnit

class AbstractSignalPathModuleSpec extends Specification {
	static class Module extends AbstractSignalPathModule {
		def param = new IntegerParameter(this, "param", 666)
		def a = new Input<Object>(this, "in2", "Object")
		def input = new Input<Object>(this, "in", "Object")
		def output = new StringOutput(this, "out")

		@Override
		void sendOutput() {}

		@Override
		void clearState() {}
	}

	Module module
	Globals globals
	FakePushChannel uiChannel

	@Shared Level oldGlobalsLoggingLevel
	@Shared Level oldAbstractSignalPathModuleLoggingLevel

	def setupSpec() {
		oldGlobalsLoggingLevel = Logger.getLogger(Globals).getLevel()
		oldAbstractSignalPathModuleLoggingLevel = Logger.getLogger(AbstractSignalPathModule).getLevel()
		Logger.getLogger(Globals).setLevel(Level.OFF)
		Logger.getLogger(AbstractSignalPathModule).setLevel(Level.OFF)
	}

	def cleanupSpec() {
		Logger.getLogger(Globals).setLevel(oldGlobalsLoggingLevel)
		Logger.getLogger(AbstractSignalPathModule).setLevel(oldAbstractSignalPathModuleLoggingLevel)
	}

	void "init() adds endpoints to class"() {
		when: "on module instantiation"
		module = new Module()
		then: "inputs and outputs not yet added"
		module.inputs == [] as Input[]
		module.outputs == [] as Output[]

		when: "init() invoked"
		module.init()
		then: "inputs and outputs added"
		module.inputs == [module.a, module.input, module.param] as Input[]
		module.outputs == [module.output] as Output[]
	}

	void "getConfiguration() gives configuration"() {
		when:
		module = new Module()
		module.init()
		module.setName("MyModule")
		module.setDisplayName("MyModuleDisplayName")
		def configuration = module.configuration

		then:
		configuration.keySet() == ["params", "inputs", "outputs", "name", "displayName", "canClearState", "canRefresh"] as Set
		configuration.name == "MyModule"
		configuration.displayName == "MyModuleDisplayName"
		configuration.canClearState
		configuration.params == [module.param.configuration]
		configuration.inputs == [module.a.configuration, module.input.configuration]
		configuration.outputs == [module.output.configuration]
	}

	@CompileStatic
	private void setUpModuleWithRuntimeRequestEnv() {
		uiChannel = new FakePushChannel()

		globals = new Globals()
		globals.setDataSource(new RealtimeDataSource(globals))
		globals.setUiChannel(uiChannel)
		globals.init()

		module = new Module()
		module.init()
		module.setGlobals(globals)
		module.setName("MyModule")
		module.setDisplayName("MyModuleDisplayName")
		module.setParentSignalPath(new SignalPath())
	}

	@CompileStatic
	private RuntimeResponse sendRuntimeRequest(LinkedHashMap<String, Object> msg, boolean authenticated = false) {
		SecUser user = authenticated ? new SecUser() : null
		def request = new RuntimeRequest(msg, user, null, "request/1", "request/1", [] as Set)
		def future = module.onRequest(request, request.getPathReader())
		globals.getDataSource().getEventQueue().process(globals.getDataSource().getEventQueue().poll())
		return future.get(1, TimeUnit.SECONDS)
	}

	void "supports 'ping' runtime requests"() {
		setUpModuleWithRuntimeRequestEnv()

		when:
		Map msg = [type: "ping"]
		def response = sendRuntimeRequest(msg)

		then:
		response == new RuntimeResponse(true, [request: msg])
	}

	void "supports 'paramChange' runtime requests"() {
		setUpModuleWithRuntimeRequestEnv()

		when:
		Map msg = [
			type: "paramChange",
			param: "param",
			value: -123
		]
		def response = sendRuntimeRequest(msg, true)

		then:
		response == new RuntimeResponse(true, [request: msg])
		module.param.value == -123
		uiChannel.receivedContentByChannel == [:]
	}

	void "'paramChange' pushes error to uiChannel if not permitted"() {
		setUpModuleWithRuntimeRequestEnv()

		when:
		Map msg = [
			type: "paramChange",
			param: "param",
			value: -123
		]
		def response = sendRuntimeRequest(msg)

		then:
		response == new RuntimeResponse([request: msg])
		module.param.value == 666
		uiChannel.receivedContentByChannel.values().flatten() == [new ErrorMessage("Parameter change failed!")]
	}

	void "supports 'json' runtime requests"() {
		setUpModuleWithRuntimeRequestEnv()

		when:
		Map msg = [type: "json"]
		def response = sendRuntimeRequest([type: "json"], true)

		then:
		response == new RuntimeResponse(true, [request: msg, json: module.configuration])
	}
}
