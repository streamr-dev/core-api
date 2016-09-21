package com.unifina.signalpath

import com.unifina.data.FeedEvent
import com.unifina.datasource.DataSource
import com.unifina.feed.MasterClock
import com.unifina.signalpath.simplemath.Count
import com.unifina.signalpath.time.ClockModule
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory
import org.codehaus.groovy.grails.commons.GrailsApplication
import spock.lang.Specification

class PropagatorSpec extends Specification {

	Globals globals

	def setup() {
		def grailsApplication = Mock(GrailsApplication)
		grailsApplication.getConfig() >> [unifina: [globals: [className: Globals.class.getName()]]]
		globals = GlobalsFactory.createInstance([:], grailsApplication, null)
	}

	def "Propagator should activate interdependent modules in originSet"() {
		MasterClock masterClock = new MasterClock(globals, Mock(DataSource))

		Count count = new Count()
		ClockModule clock = new ClockModule()

		[count, clock].each {
			it.globals = globals
			it.init()
			it.configure[:]
		}

		// Connect
		clock.getOutput("timestamp").connect(count.getInput("in"))
		[count, clock]*.connectionsReady()

		// Register the time-listening modules with the MasterClock
		masterClock.register(clock)
		masterClock.register(count)

		when: "The clock ticks"
		FeedEvent e = new FeedEvent(null, new Date(0), null)
		masterClock.receive(e)

		then: "Count must be activated"
		count.wasReady()
		count.getOutput("count").getValue() == 1
	}

	def "Propagator should activate interdependent modules in originSet, more complex case with indirect dependencies"() {
		MasterClock masterClock = new MasterClock(globals, Mock(DataSource))

		Count count = new Count()
		ClockModule clock = new ClockModule()
		AbstractSignalPathModule mod = new AbstractSignalPathModule() {
			TimeSeriesInput input = new TimeSeriesInput(this, "in")
			TimeSeriesOutput output = new TimeSeriesOutput(this, "out")
			@Override
			void sendOutput() {
				output.send(input.getValue())
			}
			@Override
			void clearState() {}
		}
		AbstractSignalPathModule mod2 = new AbstractSignalPathModule() {
			TimeSeriesInput input = new TimeSeriesInput(this, "in")
			TimeSeriesOutput output = new TimeSeriesOutput(this, "out")
			@Override
			void sendOutput() {
				output.send(input.getValue())
			}
			@Override
			void clearState() {}
		}

		[count, clock, mod, mod2].each {
			it.globals = globals
			it.init()
			it.configure[:]
		}

		// Connect clock-mod-count-mod2
		clock.getOutput("timestamp").connect(mod.getInput("in"))
		mod.getOutput("out").connect(count.getInput("in"))
		count.getOutput("count").connect(mod2.getInput("in"))
		[count, clock, mod, mod2]*.connectionsReady()

		// Register the time-listening modules with the MasterClock
		masterClock.register(clock)
		masterClock.register(count)

		when: "The clock ticks"
		FeedEvent e = new FeedEvent(null, new Date(0), null)
		masterClock.receive(e)

		then: "mod must be activated first"
		mod.wasReady()

		then: "count must be activated"
		count.wasReady()

		then: "mod2 must be activated"
		mod2.wasReady()
	}

}
