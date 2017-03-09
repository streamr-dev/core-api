package com.unifina.signalpath.list;

import com.unifina.datasource.DataSource;
import com.unifina.datasource.DataSourceEventQueue
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
			getEventQueue() >> Stub(DataSourceEventQueue) {
				enqueue(_) >> { feedEventList ->
					actualQueue.add(feedEventList[0].content.payload)
				}
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

	void "single item is sent out immediately"() {
		inputs = [list: [[1], ["test"], [[asdf: 3]]]]
		outputs = [item: [1, "test", [asdf: 3]]]
		queue = []
		expect:
		test()
	}

	void "lists with more items are queued"() {
		inputs = [list: [[1, "test"], [2, 3, 4, 5, 6], [true, [2, 3]]]]
		outputs = [item: [1, 2, true]]
		queue = ["test", 3, 4, 5, 6, [2, 3]]
		expect:
		test()
	}
}
