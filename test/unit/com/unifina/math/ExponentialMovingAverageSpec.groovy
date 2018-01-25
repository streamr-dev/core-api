package com.unifina.math

import spock.lang.Specification

class ExponentialMovingAverageSpec extends Specification {

    void "length"() {
		when:
		ExponentialMovingAverage ma = new ExponentialMovingAverage(10)
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
		ExponentialMovingAverage ma = new ExponentialMovingAverage(2)
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
		ExponentialMovingAverage ma = new ExponentialMovingAverage(2)
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
		ma.getValue() == 4

		when:
		ma.add(0)
		then:
		Math.round(ma.getValue() * 10000) == Math.round(4 * 1/3 * 10000)
	}
	
	void "set length"() {
		when:
		ExponentialMovingAverage ma = new ExponentialMovingAverage(3)
		ma.add(5)
		ma.add(3)
		ma.add(4)
		then:
		ma.getValue() == 4

		when:
		ma.setLength(2)
		then:
		ma.getValue() == 4 // only changes value after new additions

		when:
		ma.add(0)
		then:
		Math.round(ma.getValue() * 10000) == Math.round(4 * 1/3 * 10000)
	}
}
