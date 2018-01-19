package com.unifina.signalpath

import com.unifina.domain.security.SecUser
import com.unifina.utils.GlobalsFactory
import com.unifina.utils.window.WindowListener
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin) // provides grailsApplication
class AbstractModuleWithWindowSpec extends Specification {

	private WindowingModule makeModule(boolean minSamples = true, Map config = [:], int dimensions = 1) {
		Map<Object, WindowListener<Double>> windowListeners = [:]
		for (int i=0; i<dimensions; i++) {
			windowListeners.put(i, Mock(WindowListener))
		}

		WindowingModule m = new WindowingModule(minSamples, dimensions, windowListeners, Mock(AbstractModuleWithWindow))
		m.globals = GlobalsFactory.createInstance([:], grailsApplication, new SecUser())
		m.globals.time = new Date(0)
		m.init()
		m.configure(config)
		m.connectionsReady()
		return m
	}

	def "the windowLength, windowType and minSamples inputs are the first parameters, the rest are autodetected"() {
		WindowingModule m = makeModule()

		expect:
		m.getInputs().length == 4
		m.getInputs()[0].name == "windowLength"
		m.getInputs()[1].name == "windowType"
		m.getInputs()[2].name == "minSamples"
		m.getInputs()[3].name == "test"
	}

	def "minSamples can be suppressed"() {
		WindowingModule m = makeModule(false)

		expect:
		m.getInputs().length == 3
		m.getInputs()[0].name == "windowLength"
		m.getInputs()[1].name == "windowType"
		m.getInputs()[2].name == "test"
	}

	def "event listeners get correct calls for EVENTS window"() {
		WindowingModule m = makeModule(true, [inputs: [
				[name: "windowLength", value: "5"],
				[name: "windowType", value: "EVENTS"]
		]])

		when:
		(1..10).each { m.addTestValue(it) }

		then:
		10 * m.windowListeners[0].onAdd(_)
		5 * m.windowListeners[0].onRemove(_)
	}

	def "event listeners get correct calls for SECONDS window"() {
		WindowingModule m = makeModule(true, [inputs: [
				[name: "windowLength", value: "5"],
				[name: "windowType", value: "SECONDS"]
		]])

		when:
		(1..5).each { m.addTestValue(it) }

		then:
		5 * m.windowListeners[0].onAdd(_)
		0 * m.windowListeners[0].onRemove(_)

		when:
		m.globals.time = new Date(4999)
		m.setTime(m.globals.time)
		(1..3).each { m.addTestValue(it) }

		then:
		3 * m.windowListeners[0].onAdd(_)
		0 * m.windowListeners[0].onRemove(_)

		when:
		m.globals.time = new Date(5001)
		m.setTime(m.globals.time)

		then:
		5 * m.windowListeners[0].onRemove(_)
	}

	def "event listeners get correct calls for MINUTES window"() {
		WindowingModule m = makeModule(true, [inputs: [
				[name: "windowLength", value: "5"],
				[name: "windowType", value: "MINUTES"]
		]])

		when:
		(1..5).each { m.addTestValue(it) }

		then:
		5 * m.windowListeners[0].onAdd(_)
		0 * m.windowListeners[0].onRemove(_)

		when:
		m.globals.time = new Date(4*60*1000 + 59*1000)
		m.setTime(m.globals.time)
		(1..3).each { m.addTestValue(it) }

		then:
		3 * m.windowListeners[0].onAdd(_)
		0 * m.windowListeners[0].onRemove(_)

		when:
		m.globals.time = new Date(5*60*1000)
		m.setTime(m.globals.time)

		then:
		5 * m.windowListeners[0].onRemove(_)
	}

	def "listeners for two-dimensional window"() {
		WindowingModule m = makeModule(
				true,
				[inputs: [
						[name: "windowLength", value: "5"],
						[name: "windowType", value: "EVENTS"]
				]],
				2)

		when:
		(1..10).each { m.addTestValue(it, 0) }
		(1..5).each { m.addTestValue(it, 1) }

		then:
		10 * m.windowListeners[0].onAdd(_)
		5 * m.windowListeners[0].onRemove(_)
		5 * m.windowListeners[1].onAdd(_)
		0 * m.windowListeners[1].onRemove(_)
	}

	def "sendOutput must call handleInputValues() and doSendOutput()"() {
		WindowingModule m = makeModule(true, [inputs: [
				[name: "windowLength", value: "5"],
				[name: "windowType", value: "MINUTES"],
				[name: "minSamples", value: "1"]
		]])

		when:
		(1..5).each {
			m.addTestValue(it)
			m.sendOutput()
		}


		then:
		5 * m.mock.handleInputValues()
		5 * m.mock.doSendOutput()
	}

	def "must not call doSendOutput before minSamples is reached"() {
		WindowingModule m = makeModule(true, [inputs: [
				[name: "windowLength", value: "5"],
				[name: "windowType", value: "MINUTES"],
				[name: "minSamples", value: "5"]
		]])

		when:
		(1..4).each {
			m.addTestValue(it)
			m.sendOutput()
		}

		then:
		4 * m.mock.handleInputValues()
		0 * m.mock.doSendOutput()

		when:
		(5..7).each {
			m.addTestValue(it)
			m.sendOutput()
		}

		then:
		3 * m.mock.handleInputValues()
		3 * m.mock.doSendOutput()
	}

	class WindowingModule extends AbstractModuleWithWindow<Double> {

		StringParameter test = new StringParameter(this, "test", "value")
		Map<Object, WindowListener<Double>> windowListeners
		AbstractModuleWithWindow<Double> mock
		Integer dimensions

		public WindowingModule(boolean supportsMinSamples, int dimensions, Map<Object, WindowListener<Double>> windowListeners, AbstractModuleWithWindow<Double> mock) {
			this.windowListeners = windowListeners
			this.mock = mock
			this.supportsMinSamples = supportsMinSamples
			this.dimensions = dimensions
		}

		@Override
		protected WindowListener<Double> createWindowListener(Object key) {
			return windowListeners[key]
		}

		public void addTestValue(Double d) {
			addToWindow(d)
		}

		public void addTestValue(Double d, int dimension) {
			addToWindow(d, dimension)
		}

		@Override
		protected void handleInputValues() {
			mock.handleInputValues()
		}

		@Override
		protected void doSendOutput() {
			mock.doSendOutput()
		}
	}

}
