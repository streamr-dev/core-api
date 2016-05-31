package com.unifina.utils

import com.unifina.utils.window.TimeWindow
import com.unifina.utils.window.WindowListener
import spock.lang.Specification

import java.util.concurrent.TimeUnit

class TimeWindowSpec extends Specification {

    def setup() {

    }

    def cleanup() {

    }

	void "adding and removing values and setting length"() {
		setup:
		WindowListener<Double> listener = Mock(WindowListener)
		TimeWindow window = new TimeWindow(2, TimeUnit.SECONDS, listener)
		
		when:
		window.add(1, new Date(0))

		then:
		window.size == 1
		1 * listener.onAdd(1)
		0 * listener.onRemove(_)

		when:
		window.add(2, new Date(1*1000))

		then:
		1 * listener.onAdd(2)
		0 * listener.onRemove(_)

		when:
		window.add(3, new Date(2*1000))

		then:
		window.size == 2
		1 * listener.onAdd(3)
		1 * listener.onRemove(1)

		when:
		window.add(4, new Date(10*1000))

		then:
		window.size == 1
		1 * listener.onAdd(4)
		1 * listener.onRemove(2)
		then:
		1 * listener.onRemove(3)

		when:
		window.add(5, new Date(11500))
		window.add(6, new Date(11500))

		then:
		window.size == 3
		1 * listener.onAdd(5)
		1 * listener.onAdd(6)
		0 * listener.onRemove(_)

		when:
		window.setLength(1) // 1 second

		then:
		window.size == 2
		1 * listener.onRemove(4)
	}

	void "infinite window"() {
		setup:
		WindowListener<Double> listener = Mock(WindowListener)
		TimeWindow window = new TimeWindow(0, TimeUnit.SECONDS, listener)

		when:
		window.add(1, new Date(0))
		window.add(2, new Date())

		then: "values should not be removed despite extraValues (hasExtraValues should not be called)"
		window.size == 2
		1 * listener.onAdd(1)
		0 * listener.onRemove(_)
		then:
		1 * listener.onAdd(2)
		0 * listener.onRemove(_)

		when: "length changes to non-zero"
		window.setLength(1)

		then:
		thrown(IllegalArgumentException)
	}

	void "setting time on empty TimeWindow does not throw"() {
		WindowListener<Double> listener = Mock(WindowListener)
		TimeWindow window = new TimeWindow(2, TimeUnit.SECONDS, listener)

		when:
		window.setTime(new Date(0))
		window.setTime(new Date(1000))
		window.setTime(new Date(2000))

		then:
		noExceptionThrown()
	}

}
