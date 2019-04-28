package com.unifina.signalpath.list

import com.unifina.ModuleTestingSpecification
import com.unifina.data.Event
import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.signalpath.TimeSeriesInput
import com.unifina.utils.testutils.ModuleTestHelper

class ListToEventsSpec extends ModuleTestingSpecification {
	ListToEvents module
	Map<String, List<List>> inputs
	Map<String, List> outputs
	List queue
	List<Event<ListToEvents.QueuedItem>> eventsSentToEventQueue

	private boolean test() {
		return new ModuleTestHelper.Builder(module, inputs, outputs)
			.onModuleInstanceChange { newInstance -> module = newInstance }
			.afterEachTestCase {
				assert eventsSentToEventQueue.collect {it.content.item} == queue
				eventsSentToEventQueue = []
			}
			.test()
	}

	def setup() {
		module = new ListToEvents()
		module.globals = mockGlobals()

		eventsSentToEventQueue = []
		module.globals.dataSource.accept(_ as Event) >> { Event<ListToEvents.QueuedItem> e->
			eventsSentToEventQueue.add(e)
		}

		module.init()
		module.configure(module.configuration)
	}

	void "empty list sends nothing"() {
		inputs = [list: [[], [], []]]
		outputs = [item: [null, null, null]]
		queue = []
		expect:
		test()
	}

	void "lists items are queued"() {
		inputs = [list: [[1, "test"], [2, 3, 4, 5, 6], [true, [2, 3]]]]
		outputs = [item: [null, null, null]]
		queue = [1, "test", 2, 3, 4, 5, 6, true, [2, 3]]
		expect:
		test()
	}

	void "propagates values when queued events are dispatched"() {
		AbstractSignalPathModule target = new AbstractSignalPathModule() {
			TimeSeriesInput input = new TimeSeriesInput(this, "in")

			@Override
			void sendOutput() {

			}

			@Override
			void clearState() {

			}
		}
		target.init()
		module.getOutput("item").connect(target.getInput("in"))
		module.getInput("list").receive([1])

		when:
		module.sendOutput() // queues events
		eventsSentToEventQueue*.dispatch() // handles them

		then:
		module.getOutput("item").getValue() == 1
		target.getInput("in").getValue() == 1
	}
}
