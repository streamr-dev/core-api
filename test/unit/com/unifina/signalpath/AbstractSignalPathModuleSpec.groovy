package com.unifina.signalpath


import com.unifina.ModuleTestingSpecification
import com.unifina.data.Event
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.service.PermissionService
import com.unifina.utils.Globals
import groovy.transform.CompileStatic
import org.apache.log4j.Level
import org.apache.log4j.Logger
import spock.lang.Shared

import java.util.concurrent.TimeUnit

class AbstractSignalPathModuleSpec extends ModuleTestingSpecification {
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
	SignalPath mockSignalPath
	PermissionService mockedPermissionService
	Queue<Event> mockedEventQueue

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

	def setup() {
		mockSignalPath = Mock(SignalPath)
		mockedPermissionService = Mock(PermissionService)
		mockBean(PermissionService, mockedPermissionService)
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
		globals = mockGlobals()

		mockedEventQueue = new ArrayDeque<>()
		globals.dataSource.enqueue(_ as Event) >> { Event event ->
			mockedEventQueue.add(event)
		}

		module = new Module()
		module.init()
		module.setGlobals(globals)
		module.setName("MyModule")
		module.setDisplayName("MyModuleDisplayName")
		module.setParentSignalPath(mockSignalPath)
	}

	private RuntimeResponse sendRuntimeRequest(LinkedHashMap<String, Object> msg, SecUser user) {
		def request = new RuntimeRequest(msg, user, null, "request/1", "request/1", [] as Set)
		// Will insert an event to the event queue
		def future = module.onRequest(request, request.getPathReader())
		// Dispatch the event from the event queue, executing the future
		mockedEventQueue.poll().dispatch()
		return future.get(1, TimeUnit.SECONDS)
	}

	void "supports 'ping' runtime requests"() {
		setUpModuleWithRuntimeRequestEnv()

		when:
		Map msg = [type: "ping"]
		def response = sendRuntimeRequest(msg, null)

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
		def response = sendRuntimeRequest(msg, new SecUser())

		then:
		response == new RuntimeResponse(true, [request: msg])
		module.param.value == -123
		1 * mockedPermissionService.check(_, _, Permission.Operation.CANVAS_EDIT) >> true
		0 * module.parentSignalPath.pushToUiChannel(_)
	}

	void "'paramChange' pushes error to uiChannel if not permitted"() {
		setUpModuleWithRuntimeRequestEnv()

		when:
		Map msg = [
			type: "paramChange",
			param: "param",
			value: -123
		]
		def response = sendRuntimeRequest(msg, null)

		then:
		response == new RuntimeResponse([request: msg])
		module.param.value == 666
		1 * module.parentSignalPath.pushToUiChannel(new ErrorMessage("Parameter change failed!"))
	}

	void "supports 'json' runtime requests"() {
		setUpModuleWithRuntimeRequestEnv()

		when:
		Map msg = [type: "json"]
		def response = sendRuntimeRequest([type: "json"], new SecUser())

		then:
		response == new RuntimeResponse(true, [request: msg, json: module.configuration])
	}

	void "getRootSignalPath() returns null for module with no parent"() {
		module = new Module()

		expect:
		module.getRootSignalPath() == null
	}

	void "getRootSignalPath() returns root reported by parent if it has a parent"() {
		module = new Module()
		module.setParentSignalPath(mockSignalPath)

		when:
		SignalPath root = module.getRootSignalPath()
		then:
		1 * module.getParentSignalPath().getRootSignalPath() >> mockSignalPath
		root == mockSignalPath
	}
}
