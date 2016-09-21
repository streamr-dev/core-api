package com.unifina.signalpath

import com.unifina.signalpath.simplemath.Multiply
import spock.lang.Specification

class ListOrMapInputSpec extends Specification {

	def input = new ListOrMapInput(new Multiply(), "name")

	void "receivedList() is true when list received"() {
		when:
		input.receive([])
		then:
		input.receivedList()
	}

	void "receivedList() is false when map received"() {
		when:
		input.receive([:])
		then:
		!input.receivedList()
	}

	void "mapKeys() returns null when received list"() {
		when:
		input.receive([])
		then:
		input.mapKeys() == null
	}

	void "mapKeys() returns received map's keys"() {
		when:
		input.receive([a: 5, b: "666", c: [d: "e"]])
		then:
		input.mapKeys() == ["a", "b", "c"]
	}

	void "getValue() returns iterable of values when received list"() {
		when:
		input.receive([1, 9, 4, 9, 5])
		then:
		input.getValue() == [1, 9, 4, 9, 5]
	}

	void "getValue() returns iterable of values when received map"() {
		when:
		input.receive([a: 5, b: 6, c: 9, z: -666])
		then:
		input.getValue() == [5, 6, 9, -666]
	}

	void "its state can change"() {
		when:
		input.receive([4, 2, 1, 6])
		input.receive([a: "a", b: "b"])
		input.receive([9, 9, 9])

		then:
		input.getValue() == [9, 9, 9]
		input.receivedList()
		input.mapKeys() == null
	}
}
