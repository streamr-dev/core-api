package com.unifina.utils

import com.unifina.utils.window.EventWindow
import com.unifina.utils.window.WindowListener
import spock.lang.Specification

class EventWindowSpec extends Specification {

    def setup() {

    }

    def cleanup() {

    }

	void "adding and removing values and setting length"() {
		setup:
		WindowListener<Double> listener = Mock(WindowListener)
		EventWindow window = new EventWindow(2,listener)
		
		when:
		window.add(1)

		then:
		window.size == 1
		1 * listener.onAdd(1)
		0 * listener.onRemove(_)

		when:
		window.add(2)

		then:
		1 * listener.onAdd(2)
		0 * listener.onRemove(_)

		when:
		window.add(3)

		then:
		window.size == 2
		1 * listener.onAdd(3)
		1 * listener.onRemove(1)

		when:
		window.setLength(1)

		then:
		window.size == 1
		1 * listener.onRemove(2)
	}

	void "infinite window"() {
		setup:
		WindowListener<Double> listener = Mock(WindowListener)
		EventWindow window = new EventWindow(0,listener)

		when:
		window.add(1)
		window.add(2)

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

}
