package com.unifina.signalpath.list

import com.unifina.data.FeedEvent;
import com.unifina.datasource.DataSource;
import com.unifina.datasource.DataSourceEventQueue
import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.signalpath.TimeSeriesInput
import com.unifina.utils.Globals
import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification;

public class ListToEventsSpec extends Specification {
	ListToEvents module
	Map<String, List<List>> inputs
	Map<String, List> outputs
	List queue

	/** Mocked event queue. Works manually in tests, please call module.receive(queuedEvent) */
	List actualQueue = []
	def mockGlobals = Stub(Globals) {
		getDataSource() >> Stub(DataSource) {
			enqueueEvent(_) >> { feedEvent ->
				actualQueue.add(feedEvent.content[0].item)
			}
		}
		isRealtime() >> true
	}

	private boolean test() {
		return new ModuleTestHelper.Builder(module, inputs, outputs)
			.overrideGlobals { mockGlobals }
			.onModuleInstanceChange { newInstance -> module = newInstance }
			.afterEachTestCase { assert actualQueue == queue; actualQueue = [] }
			.test()
	}

	def setup() {
		module = new ListToEvents()
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

	void "receive(Packet) sends output and propagates"() {
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

		when:
		module.receive(new FeedEvent(new ListToEvents.QueuedItem(1, new Date(0)), new Date(0), module))
		then:
		module.getOutput("item").getValue() == 1
		target.getInput("in").getValue() == 1
	}
}
