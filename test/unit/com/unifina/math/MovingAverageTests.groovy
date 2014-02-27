package com.unifina.math

import static org.junit.Assert.*

import grails.test.mixin.*
import grails.test.mixin.support.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class MovingAverageTests {

    void setUp() {
        // Setup logic here
    }

    void tearDown() {
        // Tear down logic here
    }

    void testLength() {
		MovingAverage ma = new MovingAverage(10)
		assert ma.getLength() == 10
		ma.setLength(20)
		assert ma.getLength() == 20
		ma.setLength(5)
		assert ma.getLength() == 5
		ma.clear()
		assert ma.getLength() == 5
    }
	
	void testSize() {
		MovingAverage ma = new MovingAverage(2)
		assert ma.getLength() == 2
		assert ma.size() == 0
		ma.add(5)
		assert ma.size() == 1
		ma.add(3)
		assert ma.size() == 2
		ma.add(8)
		assert ma.size() == 2
	}
	
	void testValue() {
		MovingAverage ma = new MovingAverage(2)
		ma.add(5)
		assert ma.getValue() == 5
		ma.add(3)
		assert ma.getValue() == 4
		ma.add(4)
		assert ma.getValue() == 3.5	
	}
	
	void testSetLength() {
		MovingAverage ma = new MovingAverage(3)
		ma.add(5)
		ma.add(3)
		ma.add(4)
		assert ma.getValue() == 4
		ma.setLength(2)
		assert ma.getValue() == 3.5
	}
}
