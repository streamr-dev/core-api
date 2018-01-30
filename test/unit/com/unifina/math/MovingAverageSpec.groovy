package com.unifina.math

import spock.lang.Specification

class MovingAverageSpec extends Specification {

    void "length"() {
		when:
		MovingAverage ma = new MovingAverage(10)
		then:
		ma.getLength() == 10

		when:
		ma.setLength(20)
		then:
		ma.getLength() == 20

		when:
		ma.setLength(5)
		then:
		ma.getLength() == 5

		when:
		ma.clear()
		then:
		ma.getLength() == 5
    }
	
	void "size"() {
		when:
		MovingAverage ma = new MovingAverage(2)
		then:
		ma.getLength() == 2
		ma.size() == 0

		when:
		ma.add(5)
		then:
		ma.size() == 1

		when:
		ma.add(3)
		then:
		ma.size() == 2

		when:
		ma.add(8)
		then:
		ma.size() == 2
	}
	
	void "value"() {
		when:
		MovingAverage ma = new MovingAverage(2)
		ma.add(5)
		then:
		ma.getValue() == 5

		when:
		ma.add(3)
		then:
		ma.getValue() == 4

		when:
		ma.add(4)
		then:
		ma.getValue() == 3.5
	}
	
	void "set length"() {
		when:
		MovingAverage ma = new MovingAverage(3)
		ma.add(5)
		ma.add(3)
		ma.add(4)
		then:
		ma.getValue() == 4

		when:
		ma.setLength(2)
		then:
		ma.getValue() == 3.5
	}
}
