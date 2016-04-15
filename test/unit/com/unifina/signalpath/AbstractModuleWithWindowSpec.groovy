package com.unifina.signalpath

import com.unifina.utils.GlobalsFactory
import com.unifina.utils.window.WindowListener
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin) // provides grailsApplication
class AbstractModuleWithWindowSpec extends Specification {

	WindowingModule m
	List<WindowListener<Double>> windowListeners = []

	def setup() {
		m = new WindowingModule()
		m.globals = GlobalsFactory.createInstance([:], grailsApplication)
		m.globals.time = new Date(0)
		m.init()

		windowListeners.clear()
		windowListeners.push(Mock(WindowListener))
		windowListeners.push(Mock(WindowListener))
	}

	def "the windowLength and windowType inputs are the first parameters, the rest are autodetected"() {
		expect:
		m.getInputs().length == 3
		m.getInputs()[0].name == "windowLength"
		m.getInputs()[1].name == "windowType"
		m.getInputs()[2].name == "test"
	}

	def "adding events to EVENTS window"() {
		m.configure([inputs: [
			[name: "windowLength", value: "5"],
			[name: "windowType", value: "EVENTS"]
		]])
		m.connectionsReady()

		when:
		(1..10).each { m.addTestValue(it) }

		then:
		10 * windowListeners[0].onAdd(_)
		5 * windowListeners[0].onRemove(_)
	}

	def "adding events to SECONDS window"() {
		m.configure([inputs: [
				[name: "windowLength", value: "5"],
				[name: "windowType", value: "SECONDS"]
		]])
		m.connectionsReady()

		when:
		(1..5).each { m.addTestValue(it) }

		then:
		5 * windowListeners[0].onAdd(_)
		0 * windowListeners[0].onRemove(_)

		when:
		m.globals.time = new Date(4999)
		m.setTime(m.globals.time)
		(1..3).each { m.addTestValue(it) }

		then:
		3 * windowListeners[0].onAdd(_)
		0 * windowListeners[0].onRemove(_)

		when:
		m.globals.time = new Date(5001)
		m.setTime(m.globals.time)

		then:
		5 * windowListeners[0].onRemove(_)
	}

	def "adding events to MINUTES window"() {
		m.configure([inputs: [
				[name: "windowLength", value: "5"],
				[name: "windowType", value: "MINUTES"]
		]])
		m.connectionsReady()

		when:
		(1..5).each { m.addTestValue(it) }

		then:
		5 * windowListeners[0].onAdd(_)
		0 * windowListeners[0].onRemove(_)

		when:
		m.globals.time = new Date(4*60*1000 + 59*1000)
		m.setTime(m.globals.time)
		(1..3).each { m.addTestValue(it) }

		then:
		3 * windowListeners[0].onAdd(_)
		0 * windowListeners[0].onRemove(_)

		when:
		m.globals.time = new Date(5*60*1000)
		m.setTime(m.globals.time)

		then:
		5 * windowListeners[0].onRemove(_)
	}

	def "two-dimensional window"() {
		m.setDimensions(2)
		m.configure([inputs: [
				[name: "windowLength", value: "5"],
				[name: "windowType", value: "EVENTS"]
		]])
		m.connectionsReady()

		when:
		(1..10).each { m.addTestValue(it, 0) }
		(1..5).each { m.addTestValue(it, 1) }

		then:
		10 * windowListeners[0].onAdd(_)
		5 * windowListeners[0].onRemove(_)
		5 * windowListeners[1].onAdd(_)
		0 * windowListeners[1].onRemove(_)
	}

	class WindowingModule extends AbstractModuleWithWindow<Double> {
		protected StringParameter test = new StringParameter(this, "test", "value")

		@Override
		protected WindowListener<Double> createWindowListener(int dimension) {
			return windowListeners[dimension]
		}

		public void addTestValue(Double d) {
			addToWindow(d)
		}

		public void addTestValue(Double d, int dimension) {
			addToWindow(d, dimension)
		}

		@Override
		protected void handleInputValues() {

		}

		@Override
		protected void doSendOutput() {

		}
	}

}
