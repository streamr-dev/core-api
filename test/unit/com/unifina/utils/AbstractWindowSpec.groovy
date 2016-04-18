package com.unifina.utils

import com.unifina.utils.window.AbstractWindow
import com.unifina.utils.window.WindowListener
import spock.lang.Specification

class AbstractWindowSpec extends Specification {

    def setup() {

    }

    def cleanup() {

    }

	void "adding and removing values and setting length"() {
		setup:
		int extraValues = 0
		WindowListener<Double> listener = Mock(WindowListener)
		AbstractWindow window = new AbstractWindow(1,listener) { // length can be any non-zero value in this test
			@Override
			protected boolean hasExtraValues() {
				if (extraValues > 0) {
					extraValues--
					return true
				}
				else return false
			}
		}
		
		when:
		window.add(1)
		window.add(2)

		then:
		window.size == 2
		1 * listener.onAdd(1)
		0 * listener.onRemove(_)
		then:
		1 * listener.onAdd(2)
		0 * listener.onRemove(_)

		when:
		extraValues = 1
		window.add(3)

		then:
		window.size == 2
		1 * listener.onAdd(3)
		1 * listener.onRemove(1)

		when: "the length changes"
		extraValues = 2
		window.setLength(100) // length does not matter, it is only interpreted by subclasses of AbstractWindow

		then: "extra values must be purged"
		window.size == 0
		1 * listener.onRemove(2)
		then:
		1 * listener.onRemove(3)
	}

	void "infinite window"() {
		setup:
		int extraValues
		WindowListener<Double> listener = Mock(WindowListener)
		AbstractWindow window = new AbstractWindow(0,listener) { // length zero means infinite window
			@Override
			protected boolean hasExtraValues() {
				if (extraValues > 0) {
					extraValues--
					return true
				}
				else return false
			}
		}

		when:
		extraValues = 100
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
		extraValues = 1
		window.setLength(1)

		then: "exception must be thrown"
		thrown(IllegalArgumentException)
	}

	void "invalid window lengths"() {
		WindowListener<Double> listener = Mock(WindowListener)

		when: "a window with negative length is created"
		new AbstractWindow(-1,listener) {
			@Override
			protected boolean hasExtraValues() {
				return true
			}
		}

		then: "exception must be thrown"
		thrown(IllegalArgumentException)

		when: "window length is set to negative"
		AbstractWindow window = new AbstractWindow(10,listener) { // length zero means infinite window
			@Override
			protected boolean hasExtraValues() {
				return true
			}
		}
		window.setLength(-5)

		then: "exception must be thrown"
		thrown(IllegalArgumentException)
	}

	void "listener can be null"() {
		setup:
		AbstractWindow window = new AbstractWindow(5, null) {
			@Override
			protected boolean hasExtraValues() {
				return false
			}
		}

		when:
		(1..10).each { window.add(it) }

		then:
		notThrown(Exception)
	}

}
